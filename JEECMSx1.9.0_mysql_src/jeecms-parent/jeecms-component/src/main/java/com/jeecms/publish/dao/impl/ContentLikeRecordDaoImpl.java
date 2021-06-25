/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.publish.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.publish.dao.ext.ContentLikeRecordDaoExt;
import com.jeecms.publish.domain.ContentLikeRecord;
import com.jeecms.publish.domain.querydsl.QContentLikeRecord;
import com.jeecms.publish.domain.vo.ContentLikeVo;
import com.jeecms.system.domain.vo.MassScoreVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;

import java.util.Date;
import java.util.List;

/**
 * 内容点赞DAO实现
 * @author ljw
 * @version 基于x1.4.0
 * @date 2020-06-03
 */
public class ContentLikeRecordDaoImpl extends BaseDao<ContentLikeRecord>  implements ContentLikeRecordDaoExt {


	@Override
	public List<ContentLikeVo> count(boolean type, Date start, Date end) {
		List<ContentLikeVo> likeVos;
		QContentLikeRecord contentLikeRecord = QContentLikeRecord.contentLikeRecord;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (start != null) {
			boolbuild.and(contentLikeRecord.createTime.goe(start));
		}
		if (end != null) {
			boolbuild.and(contentLikeRecord.createTime.loe(end));
		}
		if (type) {
			likeVos = getJpaQueryFactory().select(
					Projections.bean(ContentLikeVo.class, contentLikeRecord.channelId.as("key"),
							contentLikeRecord.channelId.count().as("value")))
					.from(contentLikeRecord).where(boolbuild).groupBy(contentLikeRecord.channelId).fetch();
		} else {
			likeVos = getJpaQueryFactory().select(
					Projections.bean(ContentLikeVo.class, contentLikeRecord.contentId.as("key"),
							contentLikeRecord.contentId.count().as("value")))
					.from(contentLikeRecord).where(boolbuild).groupBy(contentLikeRecord.contentId).fetch();
		}
		return likeVos;
	}

	@Override
	public List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate) {
		QContentLikeRecord contentLikeRecord = QContentLikeRecord.contentLikeRecord;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (startDate != null) {
			boolbuild.and(contentLikeRecord.createTime.goe(startDate));
		}
		if (endDate != null) {
			boolbuild.and(contentLikeRecord.createTime.loe(endDate));
		}
		if (contentId != null && !contentId.isEmpty()) {
			boolbuild.and(contentLikeRecord.contentId.in(contentId));
		}
		return getJpaQueryFactory().select(
				Projections.bean(MassScoreVo.class,
						contentLikeRecord.contentId.as("contentId"),
						contentLikeRecord.contentId.count().as("counts")))
				.from(contentLikeRecord).where(boolbuild).groupBy(contentLikeRecord.contentId).fetch();
	}
}
