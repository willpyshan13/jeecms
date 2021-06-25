/**
 *
 */

package com.jeecms.system.service.impl;

import com.jeecms.auth.service.CoreUserService;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.scheduler.IBaseJob;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.SysConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.JobExceptionInfo;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.system.constants.SysJobConstants;
import com.jeecms.system.dao.SysJobDao;
import com.jeecms.system.domain.SysJob;
import com.jeecms.system.job.SysJobUtil;
import com.jeecms.system.job.factory.JobFactory;
import com.jeecms.system.service.SysJobService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.jeecms.system.constants.SysJobConstants.EXEC_CYCLE_TYPE_TIME;

/**
 * @Description:定时任务service实现
 * @author: tom
 * @date: 2018年6月12日 上午9:45:03
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobServiceImpl extends BaseServiceImpl<SysJob, SysJobDao, Integer> implements SysJobService,
        InitializingBean {

    private Logger logger = LoggerFactory.getLogger(SysJobServiceImpl.class);

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CoreUserService coreUserService;

    @Override
    public void addJob(SysJob job) throws Exception {
        if (job.getStatus()) {
            try {
                // 启动调度器
                scheduler.start();
                // 构建job信息
                JobKey jobKey = getJobKey(job);
                JobDataMap map = getJobDataMap(job);
                JobDetail jobDetail = geJobDetail(jobKey, job.getClassPath(), job.getRemark(), map);
                // 按新的cronExpression表达式构建一个新的trigger
                Trigger trigger = getSimpleTrigger(job);
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                logger.error(java.text.Normalizer.normalize(e.getMessage(), java.text.Normalizer.Form.NFKD));
            }
        }
    }

    @Override
    public void jobPause(SysJob job) {
        try {
            String jobGroupName = job.getGroupName();
            String jobName = job.getCronName();
            logger.info(java.text.Normalizer.normalize(String.format("jobPause %s", job.getCronName()),
                    java.text.Normalizer.Form.NFKD));
            scheduler.pauseJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (SchedulerException e) {
            logger.error(java.text.Normalizer.normalize(e.getMessage(), java.text.Normalizer.Form.NFKD));
        }
    }

    @Override
    public void jobResume(SysJob job) {
        if (job.getStatus()) {
            try {
                String jobGroupName = job.getGroupName();
                String jobName = job.getCronName();
                logger.info(java.text.Normalizer.normalize(
                        String.format("jobResume %s", job.getCronName()),
                        java.text.Normalizer.Form.NFKD));
                scheduler.resumeJob(JobKey.jobKey(jobName, jobGroupName));
            } catch (SchedulerException e) {
                logger.error(java.text.Normalizer.normalize(e.getMessage(),
                        java.text.Normalizer.Form.NFKD));
            }
        }
    }

    @Override
    public void jobReschedule(SysJob job) {
        String jobGroupName = job.getGroupName();
        String jobName = job.getCronName();
        JobKey jobKey = getJobKey(job);
        /* 先删除任务 */
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            logger.error(java.text.Normalizer.normalize(e.getMessage(), java.text.Normalizer.Form.NFKD));
        }
        /* 启用状态则新增任务 */
        if (job.getStatus()) {
            try {
                JobDataMap map = getJobDataMap(job);
                JobDetail jobDetail = geJobDetail(jobKey, job.getClassPath(), job.getRemark(), map);
                scheduler.scheduleJob(jobDetail, getSimpleTrigger(job));
            } catch (Exception e) {
                logger.error(java.text.Normalizer.normalize(e.getMessage(), java.text.Normalizer.Form.NFKD));
            }
        }
    }

    @Override
    public Iterable<SysJob> batchUpdate(Iterable<SysJob> entities) throws GlobalException {
        List<SysJob> jobs = (List<SysJob>) super.batchUpdate(entities);
        for (SysJob job : jobs) {
            jobReschedule(job);
        }
        return jobs;
    }

    @Override
    public SysJob update(SysJob bean) throws GlobalException {
        bean.setGroupName(SysJobConstants.geiJobGroupName(bean.getCronType()));
        bean = initChannel(bean);
        SysJob job = super.update(bean);
        jobReschedule(job);
        return job;
    }

    @Override
    public SysJob updateAll(SysJob bean) throws GlobalException {
        bean.setGroupName(SysJobConstants.geiJobGroupName(bean.getCronType()));
        SysJob job = super.updateAll(bean);
        jobReschedule(job);
        return job;
    }

    @Override
    public void jobDelete(SysJob job) {
        String jobGroupName = job.getGroupName();
        String jobName = job.getCronName();
        try {
            logger.info(java.text.Normalizer.normalize(String.format("jobDelete %s", job.getCronName()), java.text.Normalizer.Form.NFKD));
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (SchedulerException e) {
            logger.error(java.text.Normalizer.normalize(e.getMessage(), java.text.Normalizer.Form.NFKD));
        }
    }

    @Override
    public void deleteJob(Integer[] ids) throws GlobalException {
        List<SysJob> jobs = findAllById(Arrays.asList(ids));
        if (jobs.isEmpty()) {
            throw new GlobalException(new SystemExceptionInfo("对象不存在或已删除", "500"));
        }
        super.physicalDelete(ids);
        ThreadPoolService.getInstance().execute(() -> {
            for (SysJob job : jobs) {
                jobDelete(job);
            }
        });
    }

    /**
     * 获取JobDataMap.(Job参数对象)
     */
    @Override
    public JobDataMap getJobDataMap(SysJob job) {
        JobDataMap map = new JobDataMap();
        map.put("name", job.getCronName());
        map.put("groupName", job.getGroupName());
        map.put("cron", job.getCron());
        map.put("params", job.getParams());
        map.put("remark", job.getRemark());
        map.put("classPath", job.getClassPath());
        map.put("status", job.getStatus());
        map.put("siteId", job.getSiteId());
        if (job.getChannels() != null) {
            map.put("channelIds", job.getChannels());
        } else {
            map.put("channelIds", "");
        }
        return map;
    }

    @Override
    public JobDataMap getCurrJobDataMap(SysJob job) throws SchedulerException {
        /** 构建job KEY 信息 */
        JobKey jobKey = getJobKey(job);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail != null) {
            return jobDetail.getJobDataMap();
        }
        return getJobDataMap(job);
    }

    /**
     * 获取JobDetail,JobDetail是任务的定义,而Job是任务的执行逻辑,JobDetail里会引用一个Job Class来定义
     */
    @Override
    public JobDetail geJobDetail(JobKey jobKey, String className, String description, JobDataMap map)
            throws Exception {
        return JobBuilder.newJob(getClass(className)).withIdentity(jobKey).withDescription(description)
                .setJobData(map).storeDurably().build();
    }

    /**
     * 获取Trigger (Job的触发器,执行规则) （所有misfire的任务会马上执行)
     */
    @Override
    public Trigger getTrigger(SysJob job) {
        return TriggerBuilder.newTrigger().withIdentity(job.getCronName(), job.getGroupName())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron())
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    /**
     * 获取Trigger (Job的触发器,执行规则) （所有misfire的任务会在某个时间后开始执行)
     */
    @Override
    public Trigger getSimpleTrigger(SysJob job) {
        Date startTime = job.getStartTime();
        Date nowDate = Calendar.getInstance().getTime();
        if (startTime == null) {
            return getTrigger(job);
        }
        if (StringUtils.isBlank(job.getCron())) {
            return getSimpleTriggerOne(job);
        }
        //如果开始时间小于当前时间，则把当前时间当作开始时间
        if (startTime.before(nowDate)) {
            startTime = nowDate;
        }
        startTime.setTime(startTime.getTime() + 6000L);
        //startTime.setTime(startTime.getTime());
        return TriggerBuilder.newTrigger().withIdentity(job.getCronName(), job.getGroupName())
                .startAt(startTime).withSchedule(CronScheduleBuilder.cronSchedule(job.getCron())
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    private Trigger getSimpleTriggerOne(SysJob job) {
        Date startTime = job.getStartTime();
        Date nowDate = Calendar.getInstance().getTime();
        //如果开始时间小于当前时间，则把当前时间当作开始时间
        if (startTime.before(nowDate)) {
            startTime = nowDate;
        }
        return TriggerBuilder.newTrigger()
                .withIdentity(job.getCronName(), job.getGroupName())
                .startAt(startTime)
                .forJob(job.getCronName(), job.getGroupName())
                .build();
    }

    /**
     * 获取JobKey,包含Name和Group
     */
    @Override
    public JobKey getJobKey(SysJob job) {
        return JobKey.jobKey(job.getCronName(), job.getGroupName());
    }

    @SuppressWarnings("unchecked")
    public static Class<IBaseJob> getClass(String classname) throws Exception {
        Class<?> class1 = Class.forName(classname);
        return (Class<IBaseJob>) class1;
    }

    @Override
    public boolean checkJobExist(SysJob job) throws SchedulerException {
        JobKey jobKey = this.getJobKey(job);
        return scheduler.checkExists(jobKey);
    }

    @Override
    public boolean checkJobExist(JobKey jobKey) throws SchedulerException {
        return scheduler.checkExists(jobKey);
    }

    @Override
    public SysJob findByName(String jobName) throws GlobalException {
        return dao.findByCronNameAndHasDeleted(jobName, false);
    }

    @Override
    public List<Integer> findCronType(Integer siteId) {
        return dao.findCronType(siteId);
    }

    @Override
    public SysJob add(SysJob job) throws GlobalException {
        if (findByName(job.getCronName()) != null) {
            throw new GlobalException(new JobExceptionInfo(
                    SettingErrorCodeEnum.JOB_NAME.getDefaultMessage(),
                    SettingErrorCodeEnum.JOB_NAME.getCode()));
        }
        save(job);
        try {
            addJob(job);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return job;
    }

    @Override
    public SysJob updateAndReschedule(SysJob job) throws GlobalException {
        SysJob sysJob = findByName(job.getCronName());
        if (sysJob != null && !sysJob.getId().equals(job.getId())) {
            throw new GlobalException(
                    new JobExceptionInfo(SettingErrorCodeEnum.JOB_NAME.getDefaultMessage(),
                            SettingErrorCodeEnum.JOB_NAME.getCode()));
        }
        SysJob job1 = findById(job.getId());
        job.setCronType(job1.getCronType());
        // 执行周期类型为设置类型时转为cron表达式
        if (EXEC_CYCLE_TYPE_TIME.equals(job.getExecCycleType())) {
            String cron = null;
            if (SysJobConstants.INTERVAL_TYPE_HOUR == job.getIntervalType()) {
                cron = SysJobUtil.createCron(job.getStartTime(), job.getIntervalNum(), SysConstants.TimeUnit.hour);
            } else if (SysJobConstants.INTERVAL_TYPE_DAY == job.getIntervalType()) {
                cron = SysJobUtil.createCron(job.getStartTime(), job.getIntervalNum(), SysConstants.TimeUnit.day);
            } else {
                cron = SysJobUtil.createCron(job.getStartTime(), job.getIntervalNum(), SysConstants.TimeUnit.minute);
            }
            job.setCron(cron);
        }
        job.setGroupName(SysJobConstants.geiJobGroupName(job.getCronType()));
        job.setClassPath(SysJobConstants.getClassPath(job.getCronType()));
        job = update(job);
        jobReschedule(job);
        return job;
    }


    /**
     * 启动系统内置的定时任务
     */
    public void startUpJob() {
        try {
            SysJob job = JobFactory.createStatisticsAccessJob(new Date());
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("生成数据统计任务启动失败, time:{}", Calendar.getInstance().getTime());
        }
        try {
            SysJob job = JobFactory.createLogStatisticsJob(new Date());
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("生成日志统计任务启动失败, time:{}", Calendar.getInstance().getTime());
        }
        try {
            SysJob job = JobFactory.createLogAlarmJob(new Date());
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("生成日志告警任务启动失败, time:{}", Calendar.getInstance().getTime());
        }
        try {
            SysJob job = JobFactory.createLogAlertJob(new Date());
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("生成日志预警任务启动失败, time:{}", Calendar.getInstance().getTime());
        }
        try {
            SysJob job = JobFactory.createUserSummaryJob(new Date());
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("粉丝统计任务启动失败, time:{}", Calendar.getInstance().getTime());
        }
        try {
            //凌晨1点多执行
            String corn = "0 38 1 * * ?";
            SysJob job = JobFactory.createPerformanceJob(corn);
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("内容统计任务启动失败, time:{}", Calendar.getInstance().getTime());
        }
        try {
            //凌晨执行
            String corn = "0 0 0 * * ?";
            SysJob job = JobFactory.createDeleteLogDataJob(corn);
            if (!checkJobExist(job)) {
                addJob(job);
            } else {
                jobDelete(job);
                addJob(job);
            }
        } catch (Exception e) {
            logger.error("定时清除日志, time:{}", Calendar.getInstance().getTime());
        }
    }

    @Override
    public SysJob initChannel(SysJob job) {
        if (SysJobConstants.CRON_TYPE_CHANNEL == job.getCronType() || SysJobConstants.CRON_TYPE_CONTENT == job.getCronType()) {
            if (job.getIsAll() != null && job.getIsAll()) {
                List<Channel> list = channelService.findAll(false);
                StringBuilder channels = new StringBuilder();
                Integer[] channelIds = new Integer[list.size()];
                int i = 0;
                for (Channel channel : list) {
                    channels.append(channel.getId()).append(",");
                    channelIds[i] = channel.getId();
                    i++;
                }
                job.setChannels(channels.substring(0, channels.length() - 1));
                job.setChannelIds(channelIds);
            } else {
                StringBuilder channels = new StringBuilder();
                if (job.getChannelIds() != null) {
                    for (Integer channelId : job.getChannelIds()) {
                        channels.append(channelId).append(",");
                    }
                    job.setChannels(channels.substring(0, channels.length() - 1));
                }

            }
        }
        return job;
    }

    /**Bean全部初始化完成之后开始执行数据**/
    @Override
    public void afterPropertiesSet() {
        /**
         * 以子线程方式启动，是因为部分不清的环境下有启动卡住的问题
         */
        ThreadPoolService.getInstance().execute(this::startUpJob);
    }
}