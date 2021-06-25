/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.job;

import com.jeecms.common.base.scheduler.IBaseJob;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.system.service.SysAccessRecordService;
import com.jeecms.system.service.SysLogService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 删除日志数据定时任务
 *
 * @author xiaohui
 * @date 2020/11/30 10:59
 */
public class DeleteLogDataJob implements IBaseJob {

    private static final Logger logger = LoggerFactory.getLogger(CronTypeIndexJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date time = new Date();
        Date dayAfterTime = MyDateUtils.getDayAfterTime(time, -7);
        try {
            initService();
            accessRecordService.deleteByDaysAgo(dayAfterTime);
            logService.deleteByDaysAgo(dayAfterTime);
        } catch (Exception e) {
            logger.info("删除多余的日志，时间{}", dayAfterTime);
        }
    }

    private SysAccessRecordService accessRecordService;
    private SysLogService logService;

    private void initService() {
        accessRecordService = ApplicationContextProvider.getBean(SysAccessRecordService.class);
        logService = ApplicationContextProvider.getBean(SysLogService.class);
    }
}
