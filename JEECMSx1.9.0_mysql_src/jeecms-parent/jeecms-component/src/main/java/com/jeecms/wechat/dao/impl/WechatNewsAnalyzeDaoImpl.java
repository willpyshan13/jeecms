/**
 *  * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */

package com.jeecms.wechat.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.wechat.dao.ext.WechatNewsAnalyzeDaoExt;
import com.jeecms.wechat.domain.WechatNewsAnalyze;
import com.jeecms.wechat.domain.querydsl.QWechatNewsAnalyze;
import com.querydsl.core.BooleanBuilder;

import java.util.Date;
import java.util.List;


/**
 * 图文分析实现类
 * @author: ljw
 * @date: 2018年8月21日 上午11:05:40
 */
public class WechatNewsAnalyzeDaoImpl extends BaseDao<WechatNewsAnalyze> implements WechatNewsAnalyzeDaoExt {


	@Override
	public List<WechatNewsAnalyze> getList(Date start, Date end, List<String> appids) {
		QWechatNewsAnalyze qWechatNewsAnalyze = QWechatNewsAnalyze.wechatNewsAnalyze;
		BooleanBuilder builder = new BooleanBuilder();
		if (start != null && end != null) {
			builder.and(qWechatNewsAnalyze.refDate.between(start, end));
		}
		if (!appids.isEmpty()) {
			builder.and(qWechatNewsAnalyze.appId.in(appids));
		}
		return getJpaQueryFactory().selectFrom(qWechatNewsAnalyze).where(builder).fetch();
	}

}
