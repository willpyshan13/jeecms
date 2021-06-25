package com.jeecms.common.wechat.api.mp.impl;


import com.jeecms.common.wechat.Const;
import com.jeecms.common.wechat.annotations.ValidWeChatToken;
import com.jeecms.common.wechat.api.mp.NewsAnalyzeApiService;
import com.jeecms.common.wechat.bean.ValidateToken;
import com.jeecms.common.wechat.bean.request.mp.userstatistics.UserStatistics;
import com.jeecms.common.wechat.bean.response.mp.summary.NewsSummaryResponse;
import com.jeecms.common.wechat.util.client.HttpUtil;
import com.jeecms.common.wechat.util.serialize.SerializeUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 图文分析数据统计
 * @author ljw
 * @date 2020年6月22日
 */
@Service
public class NewsAnalyzeApiServiceImpl implements NewsAnalyzeApiService {

	/**获取图文群发每日数据*/
    private final String API_NEWS_SUMMARY = Const.DoMain.API_URI.concat("/datacube/getarticlesummary");

    public final String ACCESS_TOKEN="access_token";

	@Override
	@ValidWeChatToken()
	public NewsSummaryResponse getArticlesummary(UserStatistics date, ValidateToken validToken) {
		Map<String, String> params=new HashMap<String, String>(20);
		params.put(ACCESS_TOKEN, validToken.getAccessToken());
		return HttpUtil.postJsonBean(API_NEWS_SUMMARY, params,
				SerializeUtil.beanToJson(date), NewsSummaryResponse.class);
	}
}
