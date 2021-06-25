/**
 *  * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */

package com.jeecms.wechat.dao.ext;

import com.jeecms.wechat.domain.WechatNewsAnalyze;

import java.util.Date;
import java.util.List;

/**
 * WechatNewsAnalyzeDaoExt
 *
 * @author: ljw
 * @date: 2020年6月22日 上午11:05:40
 */

public interface WechatNewsAnalyzeDaoExt {

	/**
	 * 查询内容列表
	 * @param start 开始发布时间
	 * @param end 结束发布时间
	 * @param appids 微信公众号AppId集合
	 * @return List
	 */
	List<WechatNewsAnalyze> getList(Date start, Date end, List<String> appids);
}
