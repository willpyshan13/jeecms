/**
 *  * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */

package com.jeecms.weibo.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.weibo.dao.ext.WeiboInfoDaoExt;
import com.jeecms.weibo.domain.WeiboInfo;
import com.jeecms.weibo.domain.querydsl.QWeiboInfo;
import com.querydsl.core.BooleanBuilder;

import java.util.List;


/**
 * 微博扩展
 * @author: ljw
 * @date: 2020年6月22日 上午11:05:40
 */
public class WeiboInfoDaoImpl extends BaseDao<WeiboInfo> implements WeiboInfoDaoExt {

	@Override
	public List<WeiboInfo> getList(Integer siteId, List<String> uid) {
		QWeiboInfo info = QWeiboInfo.weiboInfo;
		BooleanBuilder builder = new BooleanBuilder();
		if (siteId != null) {
			builder.and(info.siteId.eq(siteId));
		}
		if (uid != null && !uid.isEmpty()) {
			builder.and(info.uid.in(uid));
		}
		return getJpaQueryFactory().selectFrom(info).where(builder).fetch();
	}
}
