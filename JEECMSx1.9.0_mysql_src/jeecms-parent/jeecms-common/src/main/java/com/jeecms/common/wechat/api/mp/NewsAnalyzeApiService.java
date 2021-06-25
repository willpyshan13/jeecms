package com.jeecms.common.wechat.api.mp;

import com.jeecms.common.wechat.bean.ValidateToken;
import com.jeecms.common.wechat.bean.request.mp.userstatistics.UserStatistics;
import com.jeecms.common.wechat.bean.response.mp.summary.NewsSummaryResponse;

/**
 * 
 * 图文分析service接口
 * @author: ljw
 * @date:   2020年6月22日 下午2:05:01
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public interface NewsAnalyzeApiService {
	
	/**
	 * 获取图文群发每日数据
	 * @param date 请求时间
	 * @param validToken 令牌
	 * @return NewsSummaryResponse 响应
	 */
	NewsSummaryResponse getArticlesummary(UserStatistics date, ValidateToken validToken);
}
