/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.dao.impl;

import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.domain.querydsl.QChannel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.dao.ext.ContentFrontDaoExt;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.querydsl.QContent;
import com.jeecms.content.domain.querydsl.QContentExt;
import com.jeecms.content.domain.vo.ContentContributeVo;
import com.jeecms.content.service.ContentService;
import com.jeecms.system.domain.querydsl.QContentTag;
import com.jeecms.system.domain.querydsl.QContentType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.*;

import static com.jeecms.content.constants.ContentConstant.*;

/**
 * 前台内容Dao扩展实现
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/7/19 9:29
 */

public class ContentFrontDaoImpl implements ContentFrontDaoExt {
	@Override
	public Page<Content> getPage(Integer channelOption, Integer[] channelIds, Integer[] tagIds,
								 String[] channelPaths, Integer siteId,
								 Integer[] typeIds, String title, Date date,
								 Integer releaseTarget, Boolean isTop,
								 Date timeBegin, Date timeEnd,
								 Integer[] excludeId, Integer[] modelId,
								 Integer orderBy, List<Integer> contentSecretIds, Pageable pageable) {
		QContent qContent = QContent.content;
		QChannel qChannel = QChannel.channel;
		JPAQuery<Integer> query = new JPAQuery<>(this.em);
		/**查询更少的字段查询列表更快*/
		query.select(qContent.id);
		query.from(qContent);
		BooleanBuilder builder = new BooleanBuilder();
		builder = appendContentTag(tagIds, qContent, query, builder, excludeId);
		query = appendQuery(query, qContent, qChannel, builder, channelOption, channelIds, channelPaths, typeIds,
				siteId, modelId, title, date, isTop, releaseTarget, timeBegin, timeEnd, contentSecretIds);
		//appendOrderTypeQueryBuild(builder,qContent,orderBy);
		query = orderTypeQueryByTuple(query, qContent, orderBy);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		query.offset(pageable.getOffset());
		query.limit(pageable.getPageSize());
		List<Integer> result = query.fetch();
		List<Content> contents = new ArrayList<>(10);
		for (Integer id : result) {
			Content c = contentService.findById(id);
			contents.add(c);
		}
		Page<Content> page = new PageImpl<>(contents, pageable, query.fetchCount());
		return page;
	}

	@Override
	public Page<Content> getPage(Integer siteId, Integer userId, Integer status, String title,
								 Date startDate, Date endDate, List<Integer> contentSecretIds, Pageable pageable) {
		JPAQuery<Content> query = new JPAQuery<>(this.em);
		QContent qContent = QContent.content;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(qContent.siteId.eq(siteId));
		builder.and(qContent.recycle.eq(false));
		builder.and(qContent.hasDeleted.eq(false));
		//内容密级id为空则标识内容密级未开启
		if (contentSecretIds != null) {
			//内容密级不存在则表示不能查看存在密级的用户
			if (contentSecretIds.isEmpty()) {
				builder.and(qContent.contentSecretId.eq(0));
			} else {
				builder.and(qContent.contentSecretId.in(contentSecretIds).or(qContent.contentSecretId.eq(0)));
			}
		}
		// 当状态为
		if (status != null) {
			switch (status) {
				case ContentConstant.CONTRIBUTE_PENDING_REVIEW:
					builder.and(qContent.status.ne(ContentConstant.STATUS_PUBLISH));
					builder.and(qContent.status.ne(ContentConstant.STATUS_TEMPORARY_STORAGE));
					break;
				case ContentConstant.CONTRIBUTE_TEMPORARY_STORAGE:
					builder.and(qContent.status.eq(ContentConstant.STATUS_TEMPORARY_STORAGE));
					break;
				case ContentConstant.CONTRIBUTE_RELEASE:
					builder.and(qContent.status.eq(ContentConstant.STATUS_PUBLISH));
					break;
				default:
					break;
			}
		}
		// 显示创建方式为投稿
		builder.and(qContent.createType.eq(ContentConstant.CONTENT_CREATE_TYPE_CONTRIBUTE));
		builder.and(qContent.userId.eq(userId));
		if (StringUtils.isNotBlank(title)) {
			builder.and(qContent.title.like("%" + title + "%"));
		}
		if (startDate != null) {
			builder.and(qContent.createTime.goe(startDate));
		}
		if (endDate != null) {
			builder.and(qContent.createTime.loe(endDate));
		}
		query.select(qContent).from(qContent).where(builder).orderBy(qContent.id.desc());
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		return QuerydslUtils.page(query, pageable, qContent);
	}

	@Override
	public List<Content> getList(Integer channelOption, Integer[] channelIds, Integer[] tagIds,
								 String[] channelPaths, Integer siteId,
								 Integer[] typeIds, String title, Date date,
								 Integer releaseTarget, Boolean isTop, Date timeBegin,
								 Date timeEnd, Integer[] excludeId, Integer[] modelId,
								 Integer orderBy, Integer count, List<Integer> contentSecretIds) {
		QContent qContent = QContent.content;
		QChannel qChannel = QChannel.channel;
		JPAQuery<Integer> query = new JPAQuery<Integer>(this.em);
		query.select(qContent.id);
		query.from(qContent);
		BooleanBuilder builder = new BooleanBuilder();
		builder = appendContentTag(tagIds, qContent, query, builder, excludeId);
		query = appendQuery(query, qContent, qChannel, builder, channelOption, channelIds, channelPaths, typeIds,
				siteId, modelId, title, date, isTop, releaseTarget, timeBegin, timeEnd, contentSecretIds);
		query = orderTypeQueryByTuple(query, qContent, orderBy);
		if (count != null) {
			query.limit(count);
		} else {
			//默认5000
			query.limit(5000);
		}
		//todo 2021/1/13 开启缓存会导致isTop属性的问题
		query.setHint(QueryHints.HINT_CACHEABLE, false);
		List<Integer> result = query.fetch();
		List<Content> contents = new ArrayList<>(10);
		for (Integer id : result) {
			Content c = contentService.findById(id);
			contents.add(c);
		}
		return contents;
	}

	@Override
	public List<Content> getList(Integer[] relationIds, Integer orderBy, Integer count, List<Integer> contentSecretIds) {
		JPAQuery<Content> query = new JPAQuery<Content>(this.em);
		QContent qContent = QContent.content;
		query.from(qContent);
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(qContent.recycle.eq(false));
		builder.and(qContent.hasDeleted.eq(false));
		builder.and(qContent.status.eq(ContentConstant.STATUS_PUBLISH));
		if (relationIds != null) {
			/*QContentRelation qContentRelation = QContentRelation.contentRelation;
			query.innerJoin(qContent.contentRelations, qContentRelation);
			builder.and(qContentRelation.id.in(Arrays.asList(relationIds)));*/
			builder.and(qContent.id.in(Arrays.asList(relationIds)));
		}
		//内容密级id为空则标识内容密级未开启
		if (contentSecretIds != null) {
			//内容密级不存在则表示不能查看存在密级的用户
			if (contentSecretIds.isEmpty()) {
				builder.and(qContent.contentSecretId.eq(0));
			} else {
				builder.and(qContent.contentSecretId.in(contentSecretIds).or(qContent.contentSecretId.eq(0)));
			}
		}
		query.where(builder);
		query = orderType(query, qContent, orderBy);
		if (count != null) {
			query.limit(count);
		}
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		return query.fetch();
	}

	@Override
	public List<Content> findByIds(List<Integer> ids, Integer orderBy, List<Integer> contentSecretIds) {
		JPAQuery<Content> query = new JPAQuery<>(this.em);
		QContent qContent = QContent.content;
		query.from(qContent).distinct();
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(qContent.hasDeleted.eq(false));
		builder.and(qContent.status.eq(ContentConstant.STATUS_PUBLISH));
		builder.and(qContent.id.in(ids));
		//内容密级id为空则标识内容密级未开启
		if (contentSecretIds != null) {
			//内容密级不存在则表示不能查看存在密级的用户
			if (!contentSecretIds.isEmpty()) {
				builder.and(qContent.contentSecretId.in(contentSecretIds).or(qContent.contentSecretId.eq(0)));
			} else {
				builder.or(qContent.contentSecretId.isNull());
			}
		}
		query.where(builder);
		query = orderType(query, qContent, orderBy);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		return query.fetch();
	}

	@Override
	public Content getSide(Integer id, Integer siteId, Integer channelId, Boolean next, List<Integer> contentSecretIds, boolean cacheable) {
		JPAQuery<Content> query = new JPAQuery<>(this.em);
		QContent qContent = QContent.content;
		query.from(qContent).distinct();
		BooleanBuilder builder = new BooleanBuilder();
		if (channelId != null) {
			builder.and(qContent.channelId.eq(channelId));
		} else if (siteId != null) {
			builder.and(qContent.siteId.eq(siteId));
		}
		builder.and(qContent.hasDeleted.eq(false));
		builder.and(qContent.recycle.eq(false));
		builder.and(qContent.status.eq(ContentConstant.STATUS_PUBLISH));
		query.setHint(QueryHints.HINT_CACHEABLE, cacheable);
		//内容密级id为空则标识内容密级未开启
		if (contentSecretIds != null) {
			//内容密级不存在则表示不能查看存在密级的用户
			if (contentSecretIds.isEmpty()) {
				builder.and(qContent.contentSecretId.eq(0));
			} else {
				builder.and(qContent.contentSecretId.in(contentSecretIds).or(qContent.contentSecretId.eq(0)));
			}
		}
		if (next != null) {
			if (next) {
				builder.and(qContent.id.gt(id));
				query.where(builder).orderBy(qContent.id.asc());
			} else {
				builder.and(qContent.id.lt(id));
				query.where(builder).orderBy(qContent.id.desc());
			}
		} else {
			builder.and(qContent.id.eq(id));
			query.where(builder);
		}

		return query.fetchFirst();
	}

	@Override
	public ContentContributeVo findContributoVo(Integer id, List<Integer> contentSecretIds) {
		QContent qContent = QContent.content;
		QContentExt qContentExt = QContentExt.contentExt;
		JPAQuery<Content> query = new JPAQuery<>(this.em);
		BooleanBuilder builder = new BooleanBuilder();
		//内容密级id为空则标识内容密级未开启
		if (contentSecretIds != null) {
			//内容密级不存在则表示不能查看存在密级的用户
			if (contentSecretIds.isEmpty()) {
				builder.and(qContent.contentSecretId.eq(0));
			} else {
				builder.and(qContent.contentSecretId.in(contentSecretIds).or(qContent.contentSecretId.eq(0)));
			}
		}
		builder.and(qContent.id.eq(id));
		builder.and(qContent.id.eq(qContentExt.id));
		return query.select(
				Projections.bean(
						ContentContributeVo.class,
						qContent.title,
						qContent.id.as("contentId"),
						qContentExt.author,
						qContentExt.description,
						qContent.channel,
						qContent.modelId,
						qContent.userId,
						qContent.payPraise.as("reward"),
						qContent.payRead.as("payread"),
						qContent.payPrice,
						qContent.trialReading))
				.from(qContent, qContentExt)
				.setHint(QueryHints.HINT_CACHEABLE, true)
				.where(builder)
				.fetchFirst();
	}

	/**
	 * 关联tagIds词
	 *
	 * @param tagIds    tagIds集合
	 * @param qContent  QContent
	 * @param query     JPAQuery
	 * @param builder   BooleanBuilder
	 * @param excludeId 排除id集合
	 * @return BooleanBuilder
	 */
	private BooleanBuilder appendContentTag(Integer[] tagIds, QContent qContent,
											JPAQuery<Integer> query,
											BooleanBuilder builder,
											Integer[] excludeId) {
		if (tagIds != null && tagIds.length > 0) {
			QContentTag qContentTag = QContentTag.contentTag;
			query.innerJoin(qContent.contentTags, qContentTag);
			builder.and(qContentTag.id.in(Arrays.asList(tagIds)));
			if (excludeId != null) {
				builder.and(qContent.id.notIn(Arrays.asList(excludeId)));
			}
		}
		return builder;
	}

	private JPAQuery<Integer> appendQuery(JPAQuery<Integer> query, QContent qContent, QChannel qChannel,
										  BooleanBuilder builder, Integer channelOption,
										  Integer[] channelIds, String[] channelPaths,
										  Integer[] typeIds, Integer siteId, Integer[] modelId, String title,
										  Date date, Boolean isTop, Integer releaseTarget,
										  Date timeBegin, Date timeEnd, List<Integer> contentSecretIds) {
		builder.and(qContent.recycle.eq(false));
		builder.and(qContent.hasDeleted.eq(false));
		builder.and(qContent.status.eq(ContentConstant.STATUS_PUBLISH));
		if (siteId != null && (channelIds == null || channelIds.length <= 0)) {
			builder.and(qContent.siteId.eq(siteId));
		}
		if (title != null) {
			builder.and(qContent.title.like("%" + title + "%"));
		}
		if (date != null) {
			builder.and(qContent.releaseTime.goe(date));
		}
		if (typeIds != null && typeIds.length > 0) {
			QContentType qContentType = QContentType.contentType;
			query.innerJoin(qContent.contentTypes, qContentType);
			builder.and(qContentType.id.in(Arrays.asList(typeIds)));
		}
		if (modelId != null && modelId.length > 0) {
			builder.and(qContent.modelId.in(Arrays.asList(modelId)));
		}
		if (isTop != null) {
			builder.and(isTop ? qContent.top.isTrue() : qContent.top.isFalse());
		}
		if (timeBegin != null) {
			builder.and(qContent.releaseTime.goe(timeBegin));
		}
		if (timeEnd != null) {
			builder.and(qContent.releaseTime.loe(timeEnd));
		}
		if (CONTENT_RELEASE_TERRACE_WAP_NUMBER.equals(releaseTarget)) {
			builder.and(qContent.releaseWap.isTrue());
		} else if (CONTENT_RELEASE_TERRACE_PC_NUMBER.equals(releaseTarget)) {
			builder.and(qContent.releasePc.isTrue());
		} else if (CONTENT_RELEASE_TERRACE_APP_NUMBER.equals(releaseTarget)) {
			builder.and(qContent.releaseApp.isTrue());
		} else if (CONTENT_RELEASE_TERRACE_MINIPROGRAM_NUMBER.equals(releaseTarget)) {
			builder.and(qContent.releaseMiniprogram.isTrue());
		}
		if (ContentConstant.CHANNEL_OPTION_CHILD.equals(channelOption)) {
			if (channelIds != null && channelIds.length > 0) {
				Channel channel = channelService.findById(channelIds[0]);
				if (channel != null) {
					Integer lft = channel.getLft();
					Integer rgt = channel.getRgt();
					QChannel c = qContent.channel;
					query.innerJoin(c);
					builder.and(c.siteId.eq(channel.getSiteId())).and(c.lft.between(lft, rgt));
				} else {
					builder.and(qContent.id.eq(-1));
				}

			}
		} else {
			if (channelIds != null && channelIds.length > 0) {
				builder.and(qContent.channelId.in(Arrays.asList(channelIds)));
			}
		}
		//内容密级id为空则标识内容密级未开启
		if (contentSecretIds != null) {
			//内容密级不存在则表示不能查看存在密级的内容
			if (contentSecretIds.isEmpty()) {
				builder.and(qContent.contentSecretId.eq(0));
			} else {
				builder.and(qContent.contentSecretId.in(contentSecretIds).or(qContent.contentSecretId.eq(0)));
			}
		}
		return query.where(builder);
	}

	/**
	 * 处理排序
	 *
	 * @param qContent 内容对象
	 * @param type     类型
	 * @return
	 */
	private JPAQuery<Integer> orderTypeQueryByTuple(JPAQuery<Integer> query, QContent qContent, Integer type) {
		int i = type == null ? 29 : type;
		switch (i) {
			case ContentConstant.ORDER_TYPE_CREATETIME_DESC:
				return query.orderBy(qContent.id.desc());
			case ContentConstant.ORDER_TYPE_CREATETIME_ASC:
				return query.orderBy(qContent.id.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_DESC:
				return query.orderBy(qContent.views.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_ASC:
				return query.orderBy(qContent.views.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_MONTH_DESC:
				return query.orderBy(qContent.contentExt.viewsMonth.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_MONTH_ASC:
				return query.orderBy(qContent.contentExt.viewsMonth.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_WEEK_DESC:
				return query.orderBy(qContent.contentExt.viewsWeek.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_WEEK_ASC:
				return query.orderBy(qContent.contentExt.viewsWeek.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_DAY_DESC:
				return query.orderBy(qContent.contentExt.viewsDay.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_DAY_ASC:
				return query.orderBy(qContent.contentExt.viewsDay.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_DESC:
				return query.orderBy(qContent.comments.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_ASC:
				return query.orderBy(qContent.comments.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_MONTH_DESC:
				return query.orderBy(qContent.contentExt.commentsMonth.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_MONTH_ASC:
				return query.orderBy(qContent.contentExt.commentsMonth.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_WEEK_DESC:
				return query.orderBy(qContent.contentExt.commentsWeek.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_WEEK_ASC:
				return query.orderBy(qContent.contentExt.commentsWeek.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_DAY_DESC:
				return query.orderBy(qContent.contentExt.commentsDay.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_DAY_ASC:
				return query.orderBy(qContent.contentExt.commentsDay.asc());
			case ContentConstant.ORDER_TYPE_UPS_DESC:
				return query.orderBy(qContent.ups.desc());
			case ContentConstant.ORDER_TYPE_UPS_ASC:
				return query.orderBy(qContent.ups.asc());
			case ContentConstant.ORDER_TYPE_UPS_MONTH_DESC:
				return query.orderBy(qContent.contentExt.upsMonth.desc());
			case ContentConstant.ORDER_TYPE_UPS_MONTH_ASC:
				return query.orderBy(qContent.contentExt.upsMonth.asc());
			case ContentConstant.ORDER_TYPE_UPS_WEEK_DESC:
				return query.orderBy(qContent.contentExt.upsWeek.desc());
			case ContentConstant.ORDER_TYPE_UPS_WEEK_ASC:
				return query.orderBy(qContent.contentExt.upsWeek.asc());
			case ContentConstant.ORDER_TYPE_UPS_DAY_DESC:
				return query.orderBy(qContent.contentExt.upsDay.desc());
			case ContentConstant.ORDER_TYPE_UPS_DAY_ASC:
				return query.orderBy(qContent.contentExt.upsDay.asc());
			case ContentConstant.ORDER_TYPE_RELEASE_TIME_DESC:
				return query.orderBy(qContent.releaseTime.desc());
			case ContentConstant.ORDER_TYPE_RELEASE_TIME_ASC:
				return query.orderBy(qContent.releaseTime.asc());
			case ContentConstant.ORDER_TYPE_ID_DESC:
				return query.orderBy(qContent.id.desc());
			case ContentConstant.ORDER_TYPE_ID_ASC:
				return query.orderBy(qContent.id.asc());
			case ContentConstant.ORDER_TYPE_DAY_DOWNLOAD_DESC:
				return query.orderBy(qContent.contentExt.downloadsDay.desc());
			case ContentConstant.ORDER_TYPE_WEEK_DOWNLOAD_DESC:
				return query.orderBy(qContent.contentExt.downloadsWeek.desc());
			case ContentConstant.ORDER_TYPE_MONTH_DOWNLOAD_DESC:
				return query.orderBy(qContent.contentExt.downloadsMonth.desc());
			case ContentConstant.ORDER_TYPE_DOWNLOAD_DESC:
				return query.orderBy(qContent.downloads.desc());
			case ContentConstant.ORDER_TYPE_SORT_NUM_DESC:
				/**置顶一般单独属性支持获取，此处只加一个字段排序*/
				return query.orderBy(qContent.top.desc()).orderBy(qContent.sortNum.desc());
			default:
				return query.orderBy(qContent.id.desc());
		}
	}

	/**
	 * 处理排序
	 *
	 * @param qContent 内容对象
	 * @param type     类型
	 * @return
	 */
	private JPAQuery<Content> orderType(JPAQuery<Content> query, QContent qContent, Integer type) {
		int i = type == null ? 29 : type;
		switch (i) {
			case ContentConstant.ORDER_TYPE_CREATETIME_DESC:
				return query.orderBy(qContent.id.desc());
			case ContentConstant.ORDER_TYPE_CREATETIME_ASC:
				return query.orderBy(qContent.id.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_DESC:
				return query.orderBy(qContent.views.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_ASC:
				return query.orderBy(qContent.views.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_MONTH_DESC:
				return query.orderBy(qContent.contentExt.viewsMonth.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_MONTH_ASC:
				return query.orderBy(qContent.contentExt.viewsMonth.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_WEEK_DESC:
				return query.orderBy(qContent.contentExt.viewsWeek.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_WEEK_ASC:
				return query.orderBy(qContent.contentExt.viewsWeek.asc());
			case ContentConstant.ORDER_TYPE_VIEWS_DAY_DESC:
				return query.orderBy(qContent.contentExt.viewsDay.desc());
			case ContentConstant.ORDER_TYPE_VIEWS_DAY_ASC:
				return query.orderBy(qContent.contentExt.viewsDay.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_DESC:
				return query.orderBy(qContent.comments.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_ASC:
				return query.orderBy(qContent.comments.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_MONTH_DESC:
				return query.orderBy(qContent.contentExt.commentsMonth.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_MONTH_ASC:
				return query.orderBy(qContent.contentExt.commentsMonth.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_WEEK_DESC:
				return query.orderBy(qContent.contentExt.commentsWeek.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_WEEK_ASC:
				return query.orderBy(qContent.contentExt.commentsWeek.asc());
			case ContentConstant.ORDER_TYPE_COMMENTS_DAY_DESC:
				return query.orderBy(qContent.contentExt.commentsDay.desc());
			case ContentConstant.ORDER_TYPE_COMMENTS_DAY_ASC:
				return query.orderBy(qContent.contentExt.commentsDay.asc());
			case ContentConstant.ORDER_TYPE_UPS_DESC:
				return query.orderBy(qContent.ups.desc());
			case ContentConstant.ORDER_TYPE_UPS_ASC:
				return query.orderBy(qContent.ups.asc());
			case ContentConstant.ORDER_TYPE_UPS_MONTH_DESC:
				return query.orderBy(qContent.contentExt.upsMonth.desc());
			case ContentConstant.ORDER_TYPE_UPS_MONTH_ASC:
				return query.orderBy(qContent.contentExt.upsMonth.asc());
			case ContentConstant.ORDER_TYPE_UPS_WEEK_DESC:
				return query.orderBy(qContent.contentExt.upsWeek.desc());
			case ContentConstant.ORDER_TYPE_UPS_WEEK_ASC:
				return query.orderBy(qContent.contentExt.upsWeek.asc());
			case ContentConstant.ORDER_TYPE_UPS_DAY_DESC:
				return query.orderBy(qContent.contentExt.upsDay.desc());
			case ContentConstant.ORDER_TYPE_UPS_DAY_ASC:
				return query.orderBy(qContent.contentExt.upsDay.asc());
			case ContentConstant.ORDER_TYPE_RELEASE_TIME_DESC:
				return query.orderBy(qContent.releaseTime.desc());
			case ContentConstant.ORDER_TYPE_RELEASE_TIME_ASC:
				return query.orderBy(qContent.releaseTime.asc());
			case ContentConstant.ORDER_TYPE_ID_DESC:
				return query.orderBy(qContent.id.desc());
			case ContentConstant.ORDER_TYPE_ID_ASC:
				return query.orderBy(qContent.id.asc());
			case ContentConstant.ORDER_TYPE_DAY_DOWNLOAD_DESC:
				return query.orderBy(qContent.contentExt.downloadsDay.desc());
			case ContentConstant.ORDER_TYPE_WEEK_DOWNLOAD_DESC:
				return query.orderBy(qContent.contentExt.downloadsWeek.desc());
			case ContentConstant.ORDER_TYPE_MONTH_DOWNLOAD_DESC:
				return query.orderBy(qContent.contentExt.downloadsMonth.desc());
			case ContentConstant.ORDER_TYPE_DOWNLOAD_DESC:
				return query.orderBy(qContent.downloads.desc());
			default:
				return query.orderBy(qContent.top.desc())
						.orderBy(qContent.sortNum.desc());
		}
	}

	private BooleanBuilder appendOrderTypeQueryBuild(BooleanBuilder builder, QContent qContent, Integer type) {
		QContentExt ext = qContent.contentExt;
		int i = type == null ? 29 : type;
		switch (i) {
			case ContentConstant.ORDER_TYPE_CREATETIME_DESC:
				builder.and(qContent.id.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_CREATETIME_ASC:
				builder.and(qContent.id.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_DESC:
				builder.and(qContent.views.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_ASC:
				builder.and(qContent.views.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_MONTH_DESC:
				builder.and(ext.viewsMonth.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_MONTH_ASC:
				builder.and(ext.viewsMonth.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_WEEK_DESC:
				builder.and(ext.viewsWeek.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_WEEK_ASC:
				builder.and(ext.viewsWeek.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_DAY_DESC:
				builder.and(ext.viewsDay.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_VIEWS_DAY_ASC:
				builder.and(ext.viewsDay.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_DESC:
				builder.and(qContent.comments.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_ASC:
				builder.and(qContent.comments.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_MONTH_DESC:
				builder.and(ext.commentsMonth.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_MONTH_ASC:
				builder.and(ext.commentsMonth.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_WEEK_DESC:
				builder.and(ext.commentsWeek.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_WEEK_ASC:
				builder.and(ext.commentsWeek.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_DAY_DESC:
				builder.and(ext.commentsDay.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_COMMENTS_DAY_ASC:
				builder.and(ext.commentsDay.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_DESC:
				builder.and(qContent.ups.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_ASC:
				builder.and(qContent.ups.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_MONTH_DESC:
				builder.and(ext.upsMonth.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_MONTH_ASC:
				builder.and(ext.upsMonth.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_WEEK_DESC:
				builder.and(ext.upsWeek.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_WEEK_ASC:
				builder.and(ext.upsWeek.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_DAY_DESC:
				builder.and(ext.upsDay.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_UPS_DAY_ASC:
				builder.and(ext.upsDay.gt(-1));
				return builder;
			case ContentConstant.ORDER_TYPE_RELEASE_TIME_DESC:
				builder.and(qContent.releaseTime.loe(Calendar.getInstance().getTime()));
				return builder;
			case ContentConstant.ORDER_TYPE_RELEASE_TIME_ASC:
				builder.and(qContent.releaseTime.loe(Calendar.getInstance().getTime()));
				return builder;
			case ContentConstant.ORDER_TYPE_UPDATETIME_DESC:
				builder.and(qContent.updateTime.loe(Calendar.getInstance().getTime()));
				return builder;
			default:
				break;
		}
		//builder.and(qContent.releaseTime.isNotNull());
		return builder;
	}

	@Autowired
	private ContentService contentService;
	@Autowired
	private ChannelService channelService;
	private EntityManager em;

	@javax.persistence.PersistenceContext
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
