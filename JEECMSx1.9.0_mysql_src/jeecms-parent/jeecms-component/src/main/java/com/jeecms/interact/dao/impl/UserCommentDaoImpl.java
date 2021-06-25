package com.jeecms.interact.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.interact.constants.UserCommentConstan;
import com.jeecms.interact.dao.ext.UserCommentDaoExt;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.interact.domain.querydsl.QUserComment;
import com.jeecms.publish.domain.vo.ContentLikeVo;
import com.jeecms.system.domain.vo.MassScoreVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * 用户评论dao实现类
 * @author: chenming
 * @date: 2019年5月6日 下午4:13:47
 */
public class UserCommentDaoImpl extends BaseDao<UserComment> implements UserCommentDaoExt {

	@Override
	public Page<UserComment> findByList(Integer siteId, Short status, Boolean isTop, Boolean isReply, Integer channelId,
			Date startTime, Date endTime, Date replyStartTime, Date replyEndTime, String fuzzySearch, 
			String userName, String ip, String commentText, String replytText, String title, 
			Pageable pageable) throws GlobalException {
		QUserComment userComment = QUserComment.userComment;
		JPAQuery<UserComment> query = new JPAQuery<>(this.em);
		BooleanBuilder exp = appendQuery(userComment, siteId, status, isTop, isReply, channelId, 
				startTime, endTime, replyStartTime, replyEndTime, fuzzySearch, userName, ip, 
				commentText, replytText, title, null, null, null,true);
		query.from(userComment)
			 .where(exp).orderBy(userComment.createTime.desc());
		return QuerydslUtils.page(query, pageable, userComment);
	}
	
	@Override
	public Page<UserComment> findTermByList(Integer siteId, Short status, Boolean isTop, Boolean isReply, Integer channelId,
			Date startTime, Date endTime, Date replyStartTime, Date replyEndTime, String fuzzySearch, 
			String userName, String ip, String commentText, String replytText, String title, 
			Integer contentId, Integer userId, String precisionIp, Pageable pageable) 
					throws GlobalException {
		QUserComment userComment = QUserComment.userComment;
		JPAQuery<UserComment> query = new JPAQuery<>(this.em);
		BooleanBuilder exp = appendQuery(userComment, siteId, status, isTop, isReply, channelId, startTime, endTime,
				replyStartTime, replyEndTime, fuzzySearch, userName, ip, commentText, replytText, 
				title, contentId, userId, precisionIp,true);
		query.setHint("org.hibernate.cacheable", true).from(userComment).where(exp).orderBy(userComment.createTime.desc());
		return QuerydslUtils.page(query, pageable, userComment);
	}
	
	private BooleanBuilder appendQuery(QUserComment userComment,Integer siteId, 
			Short status, Boolean isTop, Boolean isReply, Integer channelId,
			Date startTime, Date endTime, Date replyStartTime, Date replyEndTime, String fuzzySearch, 
			String userName, String ip, String commentText, String replytText, String title, 
			Integer contentId, Integer userId, String precisionIp, Boolean isBackstage)  {
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(userComment.siteId.eq(siteId));
		if (status != null) {
			exp.and(userComment.status.eq(status));
		}
		if (isTop != null) {
			exp.and(userComment.isTop.eq(isTop));
		}
		if (isReply != null) {
			if (isReply) {
				exp.and(userComment.replyCommentId.isNotNull());
			} else {
				exp.and(userComment.replyCommentId.isNull());
			}
		}
		if (channelId != null) {
			exp.and(userComment.content.channelId.eq(channelId));
		}
		if (startTime != null) {
			exp.and(userComment.createTime.goe(startTime));
		}
		if (endTime != null) {
			exp.and(userComment.createTime.loe(endTime));
		}
		if (replyStartTime != null) {
			exp.and(userComment.replyTime.goe(replyStartTime));
		}
		if (replyEndTime != null) {
			exp.and(userComment.replyTime.loe(replyEndTime));
		}
		if (contentId != null) {
			exp.and(userComment.contentId.eq(contentId));
		}
		if (userId != null) {
			exp.and(userComment.userId.eq(userId));
		}
		if (precisionIp != null) {
			exp.and(userComment.ip.eq(precisionIp));
		}
		if (fuzzySearch != null) {
			exp.and(userComment.user.username.like("%" + fuzzySearch + "%")
				.or(userComment.ip.like("%" + fuzzySearch + "%"))
				.or(userComment.commentText.like("%" + fuzzySearch + "%"))
//				.or(userComment.replytText.like("%" + fuzzySearch + "%"))
				.or(userComment.replyAdminComment.commentText.like("%" + replytText + "%"))
				.or(userComment.content.title.like("%" + fuzzySearch + "%")));
		}
		if (userName != null) {
			exp.and(userComment.user.username.like("%" + userName + "%"));
		}
		if (ip != null) {
			exp.and(userComment.ip.like("%" + ip + "%"));
		}
		if (commentText != null) {
			exp.and(userComment.commentText.like("%" + commentText + "%"));
		}
		if (replytText != null) {
			exp.and(userComment.replyAdminCommentId.isNotNull());
			exp.and(userComment.replyAdminComment.commentText.like("%" + replytText + "%"));
		}
		if (title != null) {
			exp.and(userComment.content.title.like("%" + title + "%"));
		}
		if (isBackstage) {
			exp.and(userComment.isReply.eq(false));
		}
		exp.and(userComment.content.status.eq(ContentConstant.STATUS_PUBLISH));
		exp.and(userComment.content.recycle.eq(false));
		exp.and(userComment.content.hasDeleted.eq(false));
		exp.and(userComment.hasDeleted.eq(false));
		return exp;
	}
	
	@Override
	public Page<UserComment> findReportByList(Integer siteId, Pageable pageable) throws GlobalException {
		QUserComment userComment = QUserComment.userComment;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(userComment.siteId.eq(siteId));
		exp.and(userComment.isReport.eq(true));
		exp.and(userComment.hasDeleted.eq(false));
		JPAQuery<UserComment> query = new JPAQuery<>(this.em);
		query.from(userComment).where(exp).orderBy(userComment.createTime.desc());
		return QuerydslUtils.page(query, pageable, userComment);
	}

	@Override
	public long getCount(Date beginTime, Date endTime, Integer siteId, Short status) {
		JPAQuery<UserComment> jpaQuery = new JPAQuery<>(this.em);
		QUserComment userComment = QUserComment.userComment;
		BooleanBuilder builder = new BooleanBuilder();
		if (beginTime != null){
			builder.and(userComment.createTime.goe(beginTime));
		}
		if (endTime != null){
			builder.and(userComment.createTime.loe(endTime));
		}
		if (siteId != null){
			builder.and(userComment.siteId.eq(siteId));
		}
		if (status != null){
			builder.and(userComment.status.eq(status));
			builder.and(userComment.isReply.eq(false));
			builder.and(userComment.hasDeleted.eq(false));
			builder.and(userComment.content.status.eq(ContentConstant.STATUS_PUBLISH));
			builder.and(userComment.content.recycle.eq(false));
			builder.and(userComment.content.hasDeleted.eq(false));
		}
		builder.and(userComment.hasDeleted.eq(false));
		return jpaQuery.from(userComment).where(builder).fetchCount();
	}

	@Override
	public Page<UserComment> getPcPage(Integer siteId, Integer contentId, Short sortStatus,  boolean mobile, Pageable pageable) throws GlobalException {
		QUserComment userComment = QUserComment.userComment;
		JPAQuery<UserComment> query = new JPAQuery<UserComment>(this.em);
		BooleanBuilder exp = appendQuery(userComment, siteId, null, null, null, null, 
				null, null, null, null, null, null, null, 
				null, null, null, contentId, null, null,false);
		exp.and(userComment.status.eq(UserCommentConstan.CHECK_BY));
		exp.and(userComment.parentId.isNull());
		if (mobile) {
			// 手机端分页查询出来的只有可能是最新评论
			exp.and(userComment.isTop.eq(false));
			query.from(userComment).where(exp).orderBy(userComment.createTime.desc());
		} else {
			// PC端最热的是推荐在前，再用创建时间倒序
			query.from(userComment).where(exp);
			if (UserCommentConstan.SORT_HOTTEST.equals(sortStatus)) {
				query.orderBy(userComment.isTop.desc()).orderBy(userComment.createTime.desc());
			}
			if (UserCommentConstan.SORT_LATEST.equals(sortStatus)) {
				query.orderBy(userComment.createTime.desc());
			}
		}
		return QuerydslUtils.page(query, pageable, userComment);
	}
	
	@Override
	public List<UserComment> getList(Integer siteId, Integer contentId) throws GlobalException {
		QUserComment userComment = QUserComment.userComment;
		BooleanBuilder exp = appendQuery(userComment, siteId, null, null, null, null, 
				null, null, null, null, null, null, null, 
				null, null, null, contentId, null, null,false);
		exp.and(userComment.status.eq(UserCommentConstan.CHECK_BY));
		exp.and(userComment.parentId.isNull());
		exp.and(userComment.isTop.eq(true));
		return getJpaQueryFactory().select(userComment).from(userComment).where(exp).orderBy(userComment.createTime.desc()).fetch();
	}
	
	@Override
	public List<UserComment> getInteractions(Date startTime, Date endTime, Integer userId, List<Integer> replys) {
		QUserComment userComment = QUserComment.userComment;
		BooleanBuilder exp = new BooleanBuilder();
		if (startTime != null && endTime != null) {
			exp.and(userComment.createTime.between(startTime, endTime));
		}
		if (userId != null) {
			exp.and(userComment.userId.eq(userId));
		}
		//根据这个条件得到回复我的数据
		if (replys != null && !replys.isEmpty()) {
			exp.and(userComment.replyCommentId.in(replys));
		}
		exp.and(userComment.hasDeleted.eq(false));
		//只得到发布状态的内容
		exp.and(userComment.content.status.eq(ContentConstant.STATUS_PUBLISH));
		JPAQuery<UserComment> query = new JPAQuery<>(this.em);
		query.from(userComment)
				//设置缓存
				.setHint("org.hibernate.cacheable", true)
				.where(exp);
		return QuerydslUtils.list(query, null, userComment);
	}

	@Override
	public List<ContentLikeVo> count(boolean type, Date start, Date end) {
		List<ContentLikeVo> likeVos;
		QUserComment comment = QUserComment.userComment;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (start != null) {
			boolbuild.and(comment.createTime.goe(start));
		}
		if (end != null) {
			boolbuild.and(comment.createTime.loe(end));
		}
		if (type) {
			likeVos = getJpaQueryFactory().select(
					Projections.bean(ContentLikeVo.class, comment.channelId.as("key"),
							comment.channelId.count().as("value")))
					.from(comment).where(boolbuild).groupBy(comment.channelId).fetch();
		} else {
			likeVos = getJpaQueryFactory().select(
					Projections.bean(ContentLikeVo.class, comment.contentId.as("key"),
							comment.contentId.count().as("value")))
					.from(comment).where(boolbuild).groupBy(comment.contentId).fetch();
		}
		return likeVos;
	}

	@Override
	public List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate) {
		QUserComment comment = QUserComment.userComment;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (startDate != null) {
			boolbuild.and(comment.createTime.goe(startDate));
		}
		if (endDate != null) {
			boolbuild.and(comment.createTime.loe(endDate));
		}
		if (contentId != null && !contentId.isEmpty()) {
			boolbuild.and(comment.contentId.in(contentId));
		}
		return getJpaQueryFactory().select(
				Projections.bean(MassScoreVo.class,
						comment.contentId.as("contentId"),
						comment.contentId.count().as("counts")))
				.from(comment).where(boolbuild).groupBy(comment.contentId).fetch();
	}

	@Override
	public List<UserComment> list(List<Integer> contentId, Date startDate, Date endDate, String province) {
		QUserComment comment = QUserComment.userComment;
		BooleanBuilder boolbuild = new BooleanBuilder();
		if (startDate != null) {
			boolbuild.and(comment.createTime.goe(startDate));
		}
		if (endDate != null) {
			boolbuild.and(comment.createTime.loe(endDate));
		}
		if (contentId != null && !contentId.isEmpty()) {
			boolbuild.and(comment.contentId.in(contentId));
		}
		if (StringUtils.isNotBlank(province)) {
			if (province.equals("其他")) {
				province = "其它";
			}
			boolbuild.and(comment.visitorArea.like("%" + province));
		}
		return getJpaQueryFactory().selectFrom(comment).where(boolbuild).fetch();
	}

    @Override
    public Integer getCount(Integer siteId, Integer contentId, Integer type) {
	    QUserComment comment = QUserComment.userComment;
	    BooleanBuilder exp = new BooleanBuilder();
	    if (siteId != null) {
	        exp.and(comment.siteId.eq(siteId));
        }
	    if (contentId != null) {
	        exp.and(comment.contentId.eq(contentId));
        }
	    switch (type) {
            case UserCommentConstan.COUNT_TYPE_ALL:
                break;
            case UserCommentConstan.COUNT_TYPE_CHECK_WAIT:
                exp.and(comment.status.eq(UserCommentConstan.CHECK_WAIT));
                break;
            case UserCommentConstan.COUNT_TYPE_CHECK_BY:
                exp.and(comment.status.eq(UserCommentConstan.CHECK_BY));
                break;
            case UserCommentConstan.COUNT_TYPE_CHECK_FAIL:
                exp.and(comment.status.eq(UserCommentConstan.CHECK_FAIL));
                break;
            case UserCommentConstan.COUNT_TYPE_REPORT:
                exp.and(comment.isReport.isNotNull());
                exp.and(comment.isReport.eq(true));
                break;
            default:
                break;
        }
	    long count = getJpaQueryFactory().select(comment.id).setHint(QueryHints.HINT_CACHEABLE, true).from(comment).where(exp).fetchCount();
        return Integer.parseInt(count+"");
    }

    @Override
    public long getCount(Integer siteId, List<Integer> ids) {
	    JPAQuery<UserComment> jpaQuery = new JPAQuery<>(this.em);
        QUserComment userComment = QUserComment.userComment;
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(userComment.hasDeleted.eq(false));
        exp.and(userComment.siteId.eq(siteId));
        exp.and(userComment.id.in(ids));
        return jpaQuery.setHint(QueryHints.HINT_CACHEABLE, true).from(userComment).where(exp).fetchCount();
    }

    @Override
	public long getCount(Integer contentId,boolean isAll, boolean isTop) {
		JPAQuery<UserComment> jpaQuery = new JPAQuery<>(this.em);
		QUserComment userComment = QUserComment.userComment;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(userComment.contentId.eq(contentId));
		exp.and(userComment.hasDeleted.eq(false));
		exp.and(userComment.status.eq(UserCommentConstan.CHECK_BY));
		if (!isAll) {
			exp.and(userComment.isTop.eq(isTop));
		}
		return jpaQuery.from(userComment).where(exp).fetchCount();
	}

	private EntityManager em;

	@javax.persistence.PersistenceContext
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
