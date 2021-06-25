package com.jeecms.channel.dao.impl;

import com.jeecms.channel.dao.ext.ChannelDaoExt;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.domain.querydsl.QChannel;
import com.jeecms.channel.domain.querydsl.QChannelAttr;
import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

/**
 * 栏目扩展dao实现
 * 
 * @author: tom
 * @date: 2019年3月20日 上午8:44:57
 */
public class ChannelDaoImpl extends BaseDao<Channel> implements ChannelDaoExt {

	@Override
	public List<Channel> findList(Integer siteId, Integer modelId, Integer parentId, Boolean display,
			Boolean staticChannel, Paginable paginable, String path, Boolean recycle) {
		JPAQuery<Channel> query = new JPAQuery<Channel>(this.em);
		QChannel channel = QChannel.channel;
		appendQuery(query, channel, siteId, modelId, parentId, display, staticChannel, path, recycle);
		return QuerydslUtils.list(query, paginable, channel);
	}

	@Override
	public Page<Channel> findPage(Integer siteId, Integer modelId, Integer parentId, Boolean display,
			Boolean staticChannel, Pageable pageable, String path, Boolean recycle) {
		JPAQuery<Channel> query = new JPAQuery<Channel>(this.em);
		QChannel channel = QChannel.channel;
		appendQuery(query, channel, siteId, modelId, parentId, display, staticChannel, path, recycle);
		query.orderBy(channel.sortNum.asc());
		return QuerydslUtils.page(query, pageable, channel);
	}

	private void appendQuery(JPAQuery<Channel> query, QChannel channel, Integer siteId, Integer modelId,
			Integer parentId, Boolean display, Boolean staticChannel, String path, Boolean recycle) {
		query.from(channel);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		BooleanBuilder exp = new BooleanBuilder();
		if (siteId != null) {
			exp.and(channel.siteId.eq(siteId));
		}
		if (modelId != null) {
			exp.and(channel.modelId.eq(modelId));
		}
		if (parentId != null) {
			if (parentId != 0) {
				exp.and(channel.parentId.eq(parentId));
			} else {
				exp.and(channel.parentId.isNull());
			}
		}
//		else {
//			exp.and(channel.parentId.isNull());
//		}
		/** true则包含未显示，false 则不包含不显示的栏目 */
		if (display != null && !display) {
			exp.and(channel.display.eq(true));
		}
		if (staticChannel != null) {
			exp.and(channel.staticChannel.eq(staticChannel));
		}
		if (path != null) {
			exp.and(channel.path.eq(path));
		}
		if (recycle != null) {
			exp.and(channel.recycle.eq(recycle));
		}
		exp.and(channel.hasDeleted.eq(false));
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		query.where(exp);
	}

	private EntityManager em;

	@javax.persistence.PersistenceContext
	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public List<String> checkNameAndPath(boolean name, boolean path, Integer siteId) {
		QChannel channel = QChannel.channel;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(channel.hasDeleted.eq(false));
		exp.and(channel.siteId.eq(siteId));
		if (name) {
			return getJpaQueryFactory().select(channel.name).setHint(QueryHints.HINT_CACHEABLE,true).from(channel).where(exp).fetch();
		}
		if (path) {
			return getJpaQueryFactory().select(channel.path).setHint(QueryHints.HINT_CACHEABLE,true).from(channel).where(exp).fetch();
		}
		return null;
	}

	@Override
	public Channel findByPath(String path, Integer siteId, Boolean recycle) {
		List<Channel> list = findList(siteId, null, null, null, null, new PaginableRequest(0, 1), path, recycle);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<Channel> findByPath(String[] paths, Integer siteId, Boolean recycle) {
		JPAQuery<Channel> query = new JPAQuery<Channel>(this.em);
		QChannel channel = QChannel.channel;
		query.from(channel);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		BooleanBuilder exp = new BooleanBuilder();
		if (siteId != null) {
			exp.and(channel.siteId.eq(siteId));
		}
		if (paths != null && paths.length > 0) {
			exp.and(channel.path.in(paths));
		}
		if (recycle != null) {
			exp.and(channel.recycle.eq(recycle));
		}
		exp.and(channel.hasDeleted.eq(false));
		query.where(exp);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		return QuerydslUtils.list(query, null, channel);
	}

	@Override
	public List<Channel> findByAttr(Integer lft,Integer rgt, String attrName, String attrVal) {
		JPAQuery<Channel> query = new JPAQuery<>(this.em);
		QChannel channel = QChannel.channel;
		QChannelAttr  attr = QChannelAttr.channelAttr;
		query.from(channel);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		BooleanBuilder exp = new BooleanBuilder();
		query.innerJoin(attr).on(channel.id.eq(attr.channelId));
		if (lft != null) {
			exp.and(channel.lft.gt(lft));
		}
		if (rgt != null) {
			exp.and(channel.rgt.lt(rgt));
		}
		if (attrName != null) {
			exp.and(attr.attrName.eq(attrName));
		}
		if (attrVal != null) {
			exp.and(attr.attrValue.eq(attrVal));
		}
		exp.and(channel.hasDeleted.eq(false));
		exp.and(channel.recycle.eq(false));
		query.where(exp);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		return QuerydslUtils.list(query, null, channel);
	}

	@Override
	public Integer findBySortNum(Integer siteId, Integer parentId) {
		JPAQuery<Channel> query = new JPAQuery<Channel>(this.em);
		QChannel qchannel = QChannel.channel;
		query.from(qchannel);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		BooleanBuilder exp = new BooleanBuilder();
		if (siteId != null) {
			exp.and(qchannel.siteId.eq(siteId));
		}
		if (parentId != null) {
			exp.and(qchannel.parentId.eq(parentId));
		} else {
			exp.and(qchannel.parentId.isNull());
		}
		exp.and(qchannel.hasDeleted.eq(false));
		exp.and(qchannel.recycle.eq(false));
		// 查询出排序值同一列中最大的排序值
		query.where(exp).orderBy(qchannel.sortNum.desc());
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		Channel channel = QuerydslUtils.findOne(query, qchannel);
		if (channel != null) {
			return channel.getSortNum();
		} else {
			return 0;
		}
	}

	@Override
	public List<Channel> findByIds(Collection<Integer> ids) {
		JPAQuery<Channel> query = new JPAQuery<Channel>(this.em);
		QChannel qchannel = QChannel.channel;
		query.from(qchannel);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(qchannel.id.in(ids));
		exp.and(qchannel.hasDeleted.eq(false));
		exp.and(qchannel.recycle.eq(false));
		query.where(exp).orderBy(qchannel.sortNum.desc());
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		return  query.fetch();
	}

	@Override
	public long getSum(Integer siteId, Boolean recycle) {
		JPAQuery<Channel> query = new JPAQuery<Channel>(this.em);
		QChannel qchannel = QChannel.channel;
		query.from(qchannel);
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(qchannel.hasDeleted.eq(false));
		if (recycle != null) {
			exp.and(qchannel.recycle.eq(recycle));
		}
		if (siteId != null) {
			exp.and(qchannel.siteId.eq(siteId));
		}
		query.setHint(QueryHints.HINT_CACHEABLE, true);
		query.where(exp);
		return  query.fetchCount();
	}

    @Override
    public Page<Channel> getPage(Pageable pageable, List<Integer> modelIds) {
        QChannel channel = QChannel.channel;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(channel.hasDeleted.eq(false));
        builder.and(channel.modelId.in(modelIds));
        builder.and(channel.modelFieldSet.isNull());
        JPAQuery<Channel> query = new JPAQuery<>(this.em);
        query.from(channel).where(builder);
		query.setHint(QueryHints.HINT_CACHEABLE, true);
        return QuerydslUtils.page(query, pageable, channel);
    }

    @Override
    public List<Integer> findByParentIds(List<Integer> ids) {
        QChannel qchannel = QChannel.channel;
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(qchannel.parentId.in(ids));
        exp.and(qchannel.hasDeleted.eq(false));
        exp.and(qchannel.recycle.eq(false));
        return getJpaQueryFactory().select(qchannel.id).setHint(QueryHints.HINT_CACHEABLE,true).from(qchannel).where(exp).fetch();
    }

    @Override
    public List<Integer> getRecycleIds(Integer siteId) {
	    QChannel qChannel = QChannel.channel;
	    BooleanBuilder exp = new BooleanBuilder();
	    exp.and(qChannel.hasDeleted.eq(false));
	    exp.and(qChannel.recycle.eq(true));
	    exp.and(qChannel.siteId.eq(siteId));
        return getJpaQueryFactory().select(qChannel.id).setHint(QueryHints.HINT_CACHEABLE,true).from(qChannel).where(exp).fetch();
    }

    @Override
    public List<Channel> findList(Integer siteId, Boolean recycle) {
	    QChannel channel = QChannel.channel;
	    BooleanBuilder exp = new BooleanBuilder();
	    exp.and(channel.siteId.eq(siteId));
	    exp.and(channel.recycle.eq(recycle));
	    exp.and(channel.hasDeleted.eq(false));
        return getJpaQueryFactory().select(channel).setHint(QueryHints.HINT_CACHEABLE, true)
                .from(channel).where(exp).orderBy(channel.sortNum.asc(),channel.createTime.asc()).fetch();
    }

}
