/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.publish.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.publish.dao.ext.StatisticsContentDataDaoExt;
import com.jeecms.publish.domain.StatisticsContentData;
import com.jeecms.publish.domain.querydsl.QStatisticsContentData;
import com.jeecms.publish.domain.vo.DataSumVo;
import com.jeecms.publish.domain.vo.DataVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.jpa.QueryHints;

import java.util.Date;
import java.util.List;

import static com.jeecms.publish.constants.PublishConstant.STATISTICS_TYPE_1;
import static com.jeecms.publish.constants.PublishConstant.STATISTICS_TYPE_2;

/**
 * 内容发布统计DAO实现
 * @author ljw
 * @version 基于x1.4.0
 * @date 2020-06-03
 */
public class StatisticsContentDataDaoImpl extends BaseDao<StatisticsContentData>
        implements StatisticsContentDataDaoExt {

    @Override
    public List<DataVo> getPage(Integer type, Integer siteId, Date startDate, Date endDate) {
        QStatisticsContentData content = QStatisticsContentData.statisticsContentData;
        BooleanBuilder boolbuild = new BooleanBuilder();
        if (siteId != null) {
            boolbuild.and(content.siteId.eq(siteId));
        }
        if (startDate != null) {
            boolbuild.and(content.statisticsDay.goe(startDate));
        }
        if (endDate != null) {
            boolbuild.and(content.statisticsDay.loe(endDate));
        }
        JPAQuery<DataVo> query;
        // 按栏目
        if (type.equals(STATISTICS_TYPE_1)) {
            boolbuild.and(content.type.eq(STATISTICS_TYPE_1));
            //默认排除底层栏目
            boolbuild.and(content.channel.child.isEmpty());
            query = super.getJpaQueryFactory().select(Projections.bean(DataVo.class,
                    content.channel.id.as("id"),
                    content.channel.name.as("name"),
                    content.channel.id.as("channelId"),
                    content.readCount.sum().as("readCount"),
                    content.peopleCount.sum().as("peopleCount"),
                    content.likeCount.sum().as("likeCount"),
                    content.commentCount.sum().as("commentCount")))
                    .from(content);
            query.where(boolbuild).groupBy(content.channelId,content.channel.name,content.channel.id);
            //增加使用查询缓存
            query.setHint(QueryHints.HINT_CACHEABLE, true);
        } else {
                boolbuild.and(content.type.eq(STATISTICS_TYPE_2));
            query = super.getJpaQueryFactory().select(Projections.bean(DataVo.class,
                    content.content.id.as("id"),
                    content.content.title.as("name"),
                    content.content.id.as("contentId"),
                    content.content.releaseTime.as("publishTime"),
                    content.readCount.sum().as("readCount"),
                    content.peopleCount.sum().as("peopleCount"),
                    content.likeCount.sum().as("likeCount"),
                    content.commentCount.sum().as("commentCount")))
                    .from(content);
            query.where(boolbuild).groupBy(content.contentId,content.content.title,content.contentId,
                    content.content.releaseTime);
            //增加使用查询缓存
            query.setHint(QueryHints.HINT_CACHEABLE, true);
        }
        return query.fetch();
    }

    @Override
    public DataSumVo count(Integer siteId, Date startDate, Date endDate, boolean flag) {
        QStatisticsContentData content = QStatisticsContentData.statisticsContentData;
        BooleanBuilder boolbuild = new BooleanBuilder();
        if (siteId != null) {
            boolbuild.and(content.siteId.eq(siteId));
        }
        if (startDate != null) {
            boolbuild.and(content.statisticsDay.goe(MyDateUtils.getStartDate(startDate)));
        }
        if (endDate != null) {
            boolbuild.and(content.statisticsDay.loe(MyDateUtils.getFinallyDate(endDate)));
        }
        // 按栏目
        if (flag) {
            //默认排除底层栏目
            boolbuild.and(content.channel.child.isEmpty());
            boolbuild.and(content.type.eq(STATISTICS_TYPE_1));
            return super.getJpaQueryFactory().select(Projections.bean(DataSumVo.class,
                    content.readCount.sum().as("readCount"),
                    content.peopleCount.sum().as("peopleCount"),
                    content.likeCount.sum().as("likeCount"),
                    content.commentCount.sum().as("commentCount")))
                    .from(content).where(boolbuild).fetchFirst();
        } else {
            //默认排除删除内容
            boolbuild.and(content.type.eq(STATISTICS_TYPE_2));
            boolbuild.and(content.content.hasDeleted.eq(false));
            return super.getJpaQueryFactory().select(Projections.bean(DataSumVo.class,
                    content.readCount.sum().as("readCount"),
                    content.peopleCount.sum().as("peopleCount"),
                    content.likeCount.sum().as("likeCount"),
                    content.commentCount.sum().as("commentCount")))
                    .from(content).where(boolbuild).fetchFirst();
        }

    }

    @Override
    public List<StatisticsContentData> getList(Integer type, Integer device, Integer contentId, Date startDate, Date endDate) {
        QStatisticsContentData content = QStatisticsContentData.statisticsContentData;
        BooleanBuilder boolbuild = new BooleanBuilder();
        if (type != null) {
            boolbuild.and(content.type.eq(type));
        }
        if (device != null) {
            boolbuild.and(content.device.eq(device));
        }
        if (contentId != null) {
            boolbuild.and(content.contentId.eq(contentId));
        }
        if (startDate != null) {
            boolbuild.and(content.statisticsDay.goe(MyDateUtils.getStartDate(startDate)));
        }
        if (endDate != null) {
            boolbuild.and(content.statisticsDay.loe(MyDateUtils.getFinallyDate(endDate)));
        }
        return super.getJpaQueryFactory().selectFrom(content).where(boolbuild).fetch();
    }

    @Override
    public DataSumVo countContent(Integer contentId, Date startDate, Date endDate) {
        QStatisticsContentData content = QStatisticsContentData.statisticsContentData;
        BooleanBuilder boolbuild = new BooleanBuilder();
        if (contentId != null) {
            boolbuild.and(content.contentId.eq(contentId));
        }
        if (startDate != null) {
            boolbuild.and(content.statisticsDay.goe(MyDateUtils.getStartDate(startDate)));
        }
        if (endDate != null) {
            boolbuild.and(content.statisticsDay.loe(MyDateUtils.getFinallyDate(endDate)));
        }
        //默认排除删除内容
        boolbuild.and(content.type.eq(STATISTICS_TYPE_2));
        return super.getJpaQueryFactory().select(Projections.bean(DataSumVo.class,
                content.readCount.sum().as("readCount"),
                content.peopleCount.sum().as("peopleCount"),
                content.likeCount.sum().as("likeCount"),
                content.commentCount.sum().as("commentCount")))
                .from(content).where(boolbuild).fetchFirst();
    }
}
