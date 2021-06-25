/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.publish.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.publish.dao.ext.StatisticsPublishDaoExt;
import com.jeecms.publish.domain.StatisticsPublish;
import com.jeecms.publish.domain.querydsl.QStatisticsPublish;
import com.querydsl.core.BooleanBuilder;

import java.util.Date;
import java.util.List;

/**
 * 内容发布统计DAO实现
 * @author ljw
 * @version 基于x1.4.0
 * @date 2020-06-03
 */
public class StatisticsPublishDaoImpl extends BaseDao<StatisticsPublish>  implements StatisticsPublishDaoExt {

	@Override
	public List<StatisticsPublish> getList(Integer siteId, Integer type,
										   Date start, Date end) {
		QStatisticsPublish publish = QStatisticsPublish.statisticsPublish;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (siteId != null) {
			boolbuild.and(publish.siteId.eq(siteId));
		}
		if (type != null) {
			boolbuild.and(publish.types.eq(type));
		}
		if (start != null) {
			boolbuild.and(publish.statisticsDay.goe(start));
		}
		if (end != null) {
			boolbuild.and(publish.statisticsDay.loe(end));
		}
		return getJpaQueryFactory().selectFrom(publish).where(boolbuild).fetch();
	}

	@Override
	public Integer countHigh(Integer type, Integer siteId) {
		QStatisticsPublish publish = QStatisticsPublish.statisticsPublish;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (siteId != null) {
			boolbuild.and(publish.siteId.eq(siteId));
		}
		if (type != null) {
			boolbuild.and(publish.types.eq(type));
		}
		StatisticsPublish fetch = getJpaQueryFactory().selectFrom(publish).where(boolbuild)
				.orderBy(publish.numbers.desc()).fetchFirst();
		if(fetch == null) {
			return 0;
		}
		return fetch.getNumbers();
	}
}
