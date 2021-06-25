/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.publish.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.content.domain.vo.ContentPerfVo;
import com.jeecms.publish.dao.ext.ContentPublishRecordDaoExt;
import com.jeecms.publish.domain.ContentPublishRecord;
import com.jeecms.publish.domain.querydsl.QContentPublishRecord;
import com.jeecms.publish.domain.vo.PublishPageVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
 * 内容发布记录统计DAO实现
 * @author ljw
 * @version 基于x1.4.0
 * @date 2020-06-03
 */
public class ContentPublishRecordDaoImpl extends BaseDao<ContentPublishRecord>  implements ContentPublishRecordDaoExt {

	@Override
	public List<ContentPublishRecord> getList(Integer siteId, Integer orgId, Integer userId, Integer channelId,
										   Date start, Date end) {
		QContentPublishRecord content = QContentPublishRecord.contentPublishRecord;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (siteId != null) {
			boolbuild.and(content.siteId.eq(siteId));
		}
		if (orgId != null) {
			boolbuild.and(content.orgId.eq(orgId));
		}
		if (userId != null) {
			boolbuild.and(content.userId.eq(userId));
		}
		if (channelId != null) {
			boolbuild.and(content.channelId.eq(channelId));
		}
		if (start != null) {
			boolbuild.and(content.publishTime.goe(MyDateUtils.getStartDate(start)));
		}
		if (end != null) {
			boolbuild.and(content.publishTime.loe(MyDateUtils.getFinallyDate(end)));
		}
		return getJpaQueryFactory().selectFrom(content).where(boolbuild).fetch();
	}

	@Override
	public Long count(Integer siteId, Integer orgId, Integer userId, Integer channelId, Date start, Date end) {
		QContentPublishRecord content = QContentPublishRecord.contentPublishRecord;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (siteId != null) {
			boolbuild.and(content.siteId.eq(siteId));
		}
		if (orgId != null) {
			boolbuild.and(content.orgId.eq(orgId));
		}
		if (userId != null) {
			boolbuild.and(content.userId.eq(userId));
		}
		if (channelId != null) {
			boolbuild.and(content.channelId.eq(channelId));
		}
		if (start != null) {
			boolbuild.and(content.publishTime.goe(MyDateUtils.getStartDate(start)));
		}
		if (end != null) {
			boolbuild.and(content.publishTime.loe(MyDateUtils.getFinallyDate(end)));
		}
		return getJpaQueryFactory().selectFrom(content).where(boolbuild).fetchCount();
	}

	@Override
	public Page<PublishPageVo> publishData(Integer siteId, Integer publishType, Boolean sort, String key,
										   Date startDate, Date endDate, Pageable pageable) {
		JPAQuery<PublishPageVo> query = query(siteId, publishType, sort, key, startDate, endDate);
		List<PublishPageVo> content = Collections.emptyList();
		long total = 0;
		JPAQuery<Integer> queryCount = queryCount(siteId, publishType, key, startDate, endDate);
		if (queryCount !=null && query != null) {
			total = queryCount.fetchCount();
			if (total > pageable.getOffset()) {
				query.offset(pageable.getOffset());
				query.limit(pageable.getPageSize());
				content = query.fetch();
			}
		}
		return new PageImpl<>(content, pageable, total);
	}

	@Override
	public List<PublishPageVo> publishDataList(Integer siteId, Integer publishType, Boolean sort, String key, Date startDate, Date endDate) {
		JPAQuery<PublishPageVo> query = query(siteId, publishType, sort, key, startDate, endDate);
		if(query != null) {
			return query.fetch();
		}
		return Collections.emptyList();

	}

	@Override
	public List<ContentPerfVo> getList(Date start, Date end, List<Integer> channels, List<Integer> users, List<Integer> orgs, List<Integer> sites) {
		QContentPublishRecord content = QContentPublishRecord.contentPublishRecord;
		BooleanBuilder builder = new BooleanBuilder();
		if (start != null) {
			builder.and(content.publishTime.goe(start));
		}
		if (end != null) {
			builder.and(content.publishTime.loe(end));
		}
		if (channels != null && !channels.isEmpty()) {
			builder.and(content.channelId.in(channels));
		}
		if (users != null && !users.isEmpty()) {
			builder.and(content.userId.in(users));
		}
		if (orgs != null && !orgs.isEmpty()) {
			builder.and(content.orgId.in(orgs));
		}
		if (sites != null && !sites.isEmpty()) {
			builder.and(content.siteId.in(sites));
		}
		return getJpaQueryFactory().select(Projections.bean(ContentPerfVo.class,
				content.contentId.as("id"),
				content.publishTime.as("releaseTime"),
				content.channelId.as("channelId"),
				content.channel.name.as("channelName"),
				content.content.title.as("title"),
				content.siteId.as("siteId"),
				content.orgId.as("orgId"),
				content.userId.as("userId")))
				.from(content).where(builder)
				.setHint(QueryHints.HINT_CACHEABLE, true).fetch();
	}

	/**请求common**/
	private JPAQuery<PublishPageVo> query(Integer siteId, Integer publishType, Boolean sort, String key,
										  Date startDate, Date endDate) {
		QContentPublishRecord content = QContentPublishRecord.contentPublishRecord;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (siteId != null) {
			boolbuild.and(content.siteId.eq(siteId));
		}
		if (startDate != null) {
			boolbuild.and(content.publishTime.goe(MyDateUtils.getStartDate(startDate)));
		}
		if (endDate != null) {
			boolbuild.and(content.publishTime.loe(MyDateUtils.getFinallyDate(endDate)));
		}
		JPAQuery<PublishPageVo> query = null;
		// 按栏目
		//这里使用group-by多个字段是因为oracle的报错ORA-00979不是GROUP BY表达式
		if (publishType.equals(PUBLISH_CHANNEL_TYPE)) {
			query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
					content.channelId.count().as("value"),
					content.channel.name.as("name")))
					.from(content);
			query.where(boolbuild).groupBy(content.channelId, content.channel.name);
			if (sort != null && !sort) {
				query.orderBy(content.channelId.count().asc());
			} else {
				query.orderBy(content.channelId.count().desc());
			}
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		} else if (publishType.equals(PUBLISH_USER_TYPE)) {
			//名字或者真实姓名
			if (StringUtils.isNotBlank(key)) {
				boolbuild.and(content.user.username.like("%" + key + "%")
						.or(content.user.userExt.realname.like("%" + key + "%")));
			}
			query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
					content.userId.count().as("value"),
					content.user.username.as("name"),
					content.user.userExt.realname.as("realName")))
					.from(content);
			query.where(boolbuild).groupBy(content.userId,content.user.username,content.user.userExt.realname);
			if (sort != null && !sort) {
				query.orderBy(content.userId.count().asc());
			} else {
				query.orderBy(content.userId.count().desc());
			}
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		} else if (publishType.equals(PUBLISH_ORG_TYPE)) {
			if (StringUtils.isNotBlank(key)) {
				boolbuild.and(content.org.name.like("%" + key + "%"));
			}
			query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
					content.orgId.count().as("value"),
					content.org.name.as("name")))
					.from(content);
			query.where(boolbuild).groupBy(content.orgId, content.org.name);
			if (sort != null && !sort) {
				query.orderBy(content.orgId.count().asc());
			} else {
				query.orderBy(content.orgId.count().desc());
			}
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		} else if (publishType.equals(PUBLISH_SITE_TYPE)) {
			if (StringUtils.isNotBlank(key)) {
				boolbuild.and(content.site.name.like("%" + key + "%"));
			}
			query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
					content.siteId.count().as("value"),
					content.site.name.as("name")))
					.from(content);
			query.where(boolbuild).groupBy(content.siteId,content.site.name);
			if (sort != null && !sort) {
				query.orderBy(content.siteId.count().asc());
			} else {
				query.orderBy(content.siteId.count().desc());
			}
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		}
		return query;
	}

	private JPAQuery<Integer> queryCount(Integer siteId, Integer publishType, String key,
										  Date startDate, Date endDate) {
		QContentPublishRecord content = QContentPublishRecord.contentPublishRecord;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (siteId != null) {
			boolbuild.and(content.siteId.eq(siteId));
		}
		if (startDate != null) {
			boolbuild.and(content.publishTime.goe(MyDateUtils.getStartDate(startDate)));
		}
		if (endDate != null) {
			boolbuild.and(content.publishTime.loe(MyDateUtils.getFinallyDate(endDate)));
		}
		JPAQuery<Integer> query = null;
		// 按栏目
		if (publishType.equals(PUBLISH_CHANNEL_TYPE)) {
			query = super.getJpaQueryFactory().select(content.channelId)
					.from(content);
			query.where(boolbuild).groupBy(content.channelId);
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		} else if (publishType.equals(PUBLISH_USER_TYPE)) {
			//名字或者真实姓名
			if (StringUtils.isNotBlank(key)) {
				boolbuild.and(content.user.username.like("%" + key + "%")
						.or(content.user.userExt.realname.like("%" + key + "%")));
			}
			query = super.getJpaQueryFactory().select(content.userId)
					.from(content);
			query.where(boolbuild).groupBy(content.userId);
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		} else if (publishType.equals(PUBLISH_ORG_TYPE)) {
			if (StringUtils.isNotBlank(key)) {
				boolbuild.and(content.org.name.like("%" + key + "%"));
			}
			query = super.getJpaQueryFactory().select(content.orgId)
					.from(content);
			query.where(boolbuild).groupBy(content.orgId);
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		} else if (publishType.equals(PUBLISH_SITE_TYPE)) {
			if (StringUtils.isNotBlank(key)) {
				boolbuild.and(content.site.name.like("%" + key + "%"));
			}
			query = super.getJpaQueryFactory().select(content.siteId)
					.from(content);
			query.where(boolbuild).groupBy(content.siteId);
			//增加使用查询缓存
			query.setHint(QueryHints.HINT_CACHEABLE, true);
		}
		return query;
	}
}
