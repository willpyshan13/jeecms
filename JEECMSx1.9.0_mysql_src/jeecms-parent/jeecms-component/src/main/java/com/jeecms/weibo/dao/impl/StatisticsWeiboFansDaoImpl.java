/**
 *  * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */

package com.jeecms.weibo.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.weibo.dao.ext.StatisticsWeiboFansDaoExt;
import com.jeecms.weibo.domain.StatisticsWeiboFans;
import com.jeecms.weibo.domain.querydsl.QStatisticsWeiboFans;
import com.querydsl.core.BooleanBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 微博扩展
 * @author: ljw
 * @date: 2020年6月22日 上午11:05:40
 */
public class StatisticsWeiboFansDaoImpl extends BaseDao<StatisticsWeiboFans>
		implements StatisticsWeiboFansDaoExt {

	@Override
	public List<StatisticsWeiboFans> getList(Date startDate, Date endDate, List<String> uids) {
		QStatisticsWeiboFans fans = QStatisticsWeiboFans.statisticsWeiboFans;
		BooleanBuilder builder = new BooleanBuilder();
		if (startDate != null && endDate != null) {
			builder.and(fans.statisticsDay.between(startDate, endDate));
		}
		if (uids == null || uids.isEmpty()) {
			return new ArrayList<>(0);
		}
		builder.and(fans.weiboUid.in(uids));
		//统计时间倒序
		return getJpaQueryFactory().selectFrom(fans).orderBy(fans.statisticsDay.desc()).where(builder).fetch();
	}
}
