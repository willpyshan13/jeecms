/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.job;

import com.jeecms.common.base.scheduler.IBaseJob;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.content.domain.Content;
import com.jeecms.content.service.ContentService;
import com.jeecms.system.service.SysJobService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内容限时置顶JOB
 * 
 * @author: ljw
 * @date: 2019年5月15日 上午10:30:37
 */
public class ContentTopJob implements IBaseJob {
        private Logger logger = LoggerFactory.getLogger(ContentTopJob.class);

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
                JobDataMap map = context.getMergedJobDataMap();
                logger.info("Running Job name : {} ", map.getString("name"));
                Integer contentId = Integer.parseInt((String) map.get("params"));
                long startTime = System.currentTimeMillis();
                initService();
                try {
                        Content content = contentService.findById(contentId);
                        content.setTopStartTime(null);
                        content.setTopEndTime(null);
                        content.setTop(false);
                        contentService.update(content);
                } catch (GlobalException e) {
                        logger.error("ContentTopJob job contentId =" + contentId + " not find");
                }
                long endTime = System.currentTimeMillis();
                logger.info(">>>>>>>>>>>>> Running Job has been completed , cost time :  " + (endTime - startTime)
                                + "ms\n");
        }

        private void initService() {
                contentService = ApplicationContextProvider.getBean(ContentService.class);
        }

        private ContentService contentService;
        private SysJobService jobService;

}
