/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */  

package com.jeecms.system.job;

import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.wechat.bean.ValidateToken;
import com.jeecms.common.wechat.bean.request.mp.userstatistics.UserStatistics;
import com.jeecms.common.weibo.api.user.WeiboUserService;
import com.jeecms.common.weibo.bean.request.user.WeiboUserRequest;
import com.jeecms.common.weibo.bean.response.user.WeiboUserResponse;
import com.jeecms.wechat.domain.AbstractWeChatInfo;
import com.jeecms.wechat.domain.AbstractWeChatToken;
import com.jeecms.wechat.service.AbstractWeChatInfoService;
import com.jeecms.wechat.service.AbstractWeChatTokenService;
import com.jeecms.wechat.service.WechatNewsAnalyzeService;
import com.jeecms.weibo.domain.StatisticsWeiboFans;
import com.jeecms.weibo.domain.WeiboInfo;
import com.jeecms.weibo.service.StatisticsWeiboFansService;
import com.jeecms.weibo.service.WeiboInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**   
 * 统计图文分析
 * @author ljw
 * @date  2020年06月09日 上午9:41:41
 */
@Component
public class WechatNewsAndWeiboFansJob {

	private Logger logger = LoggerFactory.getLogger(WechatNewsAndWeiboFansJob.class);

	@Autowired
	private WechatNewsAnalyzeService wechatNewsAnalyzeService;
	@Autowired
	private AbstractWeChatInfoService abstractWeChatInfoService;
	@Autowired
	private AbstractWeChatTokenService abstractWeChatTokenService;
	@Autowired
	private WeiboInfoService weiboInfoService;
	@Autowired
	private StatisticsWeiboFansService statisticsWeiboFansService;
	@Autowired
	private WeiboUserService weiboUserService;

	/**
	 * 每一天定时统计---统计图文分析
	 * @Title: collect
	 * @Description 测试每5分钟统计一次,实际可调 0 0/5 * * * ? 实际每天凌晨1点15执行0 15 1 * * ?
	 * @since 基于1.4的cms
	 */
	@Scheduled(cron = "0 15 1 * * ?")
	public void collect() {
		long startTime = System.currentTimeMillis();
		try {
			// 因为只能查询到前一天的数据，所以，开始日期、结束日期都是同一天
			UserStatistics statistics = new UserStatistics();
			// 开始的日期
			statistics.setBeginDate(MyDateUtils.formatDate(MyDateUtils
					.getSpecficDateStart(new Date(), -1)));
			// 结束的日期
			statistics.setEndDate(MyDateUtils.formatDate(MyDateUtils
					.getSpecficDateStart(new Date(), -1)));
			// 查询weChatInfo和toKen，遍历比较appId 获取到token调用方法
			List<AbstractWeChatInfo> infos = abstractWeChatInfoService.findAll(false);
			List<AbstractWeChatToken> chatTokens = abstractWeChatTokenService.findAll(false);
			Map<String, AbstractWeChatToken> tokenMaps = new HashMap<>(10);
			if (chatTokens != null && !chatTokens.isEmpty()) {
				// 将所有公众号token数据List 转换成以appId为key，对象为value的Map
				tokenMaps = chatTokens.stream()
						.collect(Collectors
								.toMap(AbstractWeChatToken::getAppId,
										a -> a, (k1, k2) -> k2));
			}
			for (AbstractWeChatInfo abstractWeChatInfo : infos) {
				AbstractWeChatToken token = tokenMaps.get(abstractWeChatInfo.getAppId());
				if (token != null) {
					ValidateToken validatoken = new ValidateToken();
					validatoken.setAppId(token.getAppId());
					validatoken.setAccessToken(token.getAuthorizerAccessToken());
					wechatNewsAnalyzeService.saveNewsStatistics(statistics, validatoken);
				}
			}
		}  catch (Exception e) {
			logger.error(e.getMessage());
		}
		long endTime = System.currentTimeMillis();
		logger.info(">>>>>>>>>>>>> 微信图文统计数据  Running Job has been completed , cost time :  {} ms\n",
				(endTime - startTime));
	}

	/**
	 * 每一天定时统计---统计微博粉丝净增数
	 * @Title: collect
	 * @Description 测试每5分钟统计一次,实际可调 0 0/5 * * * ? 实际每天凌晨2点执行0 0 2 * * ?
	 * @since 基于1.4的cms
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	public void collectWeibo() {
		long startTime = System.currentTimeMillis();
		try {
			List<StatisticsWeiboFans> list = new ArrayList<>(16);
			List<WeiboInfo> infos = weiboInfoService.findAll(false);
			//统计粉丝数
			for (WeiboInfo info : infos) {
				WeiboUserResponse response = weiboUserService
						.getWeiboUser(new WeiboUserRequest(info.getAccessToken(),
						Long.valueOf(info.getUid())));
				if (response != null) {
					StatisticsWeiboFans fans = new StatisticsWeiboFans();
					fans.setFansCount(response.getFollowersCount());
					fans.setWeiboUid(response.getIdLong().toString());
					fans.setStatisticsDay(MyDateUtils.getSpecficDateEnd(new Date(), -1));
					list.add(fans);
				}
			}
			statisticsWeiboFansService.saveAll(list);
		}  catch (Exception e) {
			logger.error(e.getMessage());
		}
		long endTime = System.currentTimeMillis();
		logger.info(">>>>>>>>>>>>> 微博粉丝数据  Running Job has been completed, cost time :  {} ms\n",
				(endTime - startTime));
	}
}
