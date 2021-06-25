/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */  

package com.jeecms.system.job;

import com.jeecms.common.base.scheduler.IBaseJob;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.publish.service.StatisticsContentDataService;
import com.jeecms.publish.service.StatisticsPublishDetailsService;
import com.jeecms.publish.service.StatisticsPublishService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**   
 * 内容发布统计使用定时JOB
 * @author ljw
 * @date  2020年06月09日 上午9:41:41
 */
public class ContentPublishJob implements IBaseJob {

	private final Logger logger = LoggerFactory.getLogger(ContentPublishJob.class);

	private StatisticsPublishDetailsService statisticsPublishDetailsService;
	private StatisticsPublishService statisticsPublishService;
	private StatisticsContentDataService statisticsContentDataService;

	private void initService() {
		statisticsPublishDetailsService = ApplicationContextProvider.getBean(StatisticsPublishDetailsService.class);
		statisticsPublishService = ApplicationContextProvider.getBean(StatisticsPublishService.class);
		statisticsContentDataService = ApplicationContextProvider.getBean(StatisticsContentDataService.class);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
		logger.info("Running Job name : {} ", map.getString("name"));
		long startTime = System.currentTimeMillis();
		try{
			initService();
			//内容发布频率统计
			statisticsPublishDetailsService.collect(MyDateUtils.getSpecficDate(new Date(),-1));
			//网站信息发布统计
			statisticsPublishService.collect(MyDateUtils.getSpecficDate(new Date(),-1));
			//内容数据统计
			statisticsContentDataService.collect(MyDateUtils.getSpecficDate(new Date(),-1));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		long endTime = System.currentTimeMillis();
		logger.info(">>>>>>>>>>>>> 内容发布统计  Running Job has been completed , cost time :  {} ms\n",
				(endTime - startTime));
	}
}
