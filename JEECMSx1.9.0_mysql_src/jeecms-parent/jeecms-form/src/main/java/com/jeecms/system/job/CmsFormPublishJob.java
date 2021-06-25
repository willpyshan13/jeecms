package com.jeecms.system.job;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.scheduler.IBaseJob;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.system.job.factory.JobFactory;
import com.jeecms.system.service.SysJobService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * 表单定时发布
 * @author: tom
 * @date: 2020/2/14 14:31   
 */
public class CmsFormPublishJob implements IBaseJob {
    private Logger logger = LoggerFactory.getLogger(UserReleaseLockJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getMergedJobDataMap();
        logger.info("Running Job name : {} ", map.getString("name"));
        Integer formId = Integer.parseInt((String) map.get("params"));
        long startTime = System.currentTimeMillis();
        initService();
        if(formId!=null){
            try {
                CmsFormEntity entity = formService.findById(formId);
                if(entity!=null){
                    entity.setStatus(CmsFormConstant.FORM_STATU_PUBLISH);
                    formService.update(entity);
                }
            } catch (GlobalException e) {
                logger.error("CmsFormPublishJob job id =" + formId + " not find");
            }
            long endTime = System.currentTimeMillis();
            logger.info(">>>>>>>>>>>>> Running CmsFormPublishJob has been completed , cost time :  " + (endTime - startTime)
                    + "ms\n");
            /**删除任务*/
            jobService.jobDelete(JobFactory.createFormPublishJob(formId, Calendar.getInstance().getTime()));
        }
    }

    private void initService() {
        formService = ApplicationContextProvider.getBean(CmsFormService.class);
        jobService =  ApplicationContextProvider.getBean(SysJobService.class);
    }

    private CmsFormService formService;
    private SysJobService jobService;

}