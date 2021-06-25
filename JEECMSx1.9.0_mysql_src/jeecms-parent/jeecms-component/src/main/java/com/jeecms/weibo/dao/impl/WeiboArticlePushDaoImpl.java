/**
 *  * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */

package com.jeecms.weibo.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.weibo.dao.ext.WeiboArticlePushDaoExt;
import com.jeecms.weibo.domain.WeiboArticlePush;
import com.jeecms.weibo.domain.querydsl.QWeiboArticlePush;
import com.querydsl.core.BooleanBuilder;

import java.util.Date;
import java.util.List;


/**
 * 微博扩展
 * @author: ljw
 * @date: 2020年6月22日 上午11:05:40
 */
public class WeiboArticlePushDaoImpl extends BaseDao<WeiboArticlePush> implements WeiboArticlePushDaoExt {


	@Override
	public List<WeiboArticlePush> getList(Date start, Date end, List<Integer> users, List<String> uuids) {
		QWeiboArticlePush weiboArticlePush = QWeiboArticlePush.weiboArticlePush;
		BooleanBuilder builder = new BooleanBuilder();
		if (start != null && end != null) {
			builder.and(weiboArticlePush.createTime.between(start, end));
		}
		if (users != null && !users.isEmpty()) {
			builder.and(weiboArticlePush.userId.in(users));
		}
		if (uuids != null && !uuids.isEmpty()) {
			builder.and(weiboArticlePush.uid.in(uuids));
		}
		return getJpaQueryFactory().selectFrom(weiboArticlePush).where(builder).fetch();
	}
}
