/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.publish.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.publish.dao.ext.StatisticsPublishDetailsDaoExt;
import com.jeecms.publish.domain.StatisticsPublishDetails;
import com.jeecms.publish.domain.querydsl.QStatisticsPublishDetails;
import com.jeecms.publish.domain.vo.PublishPageVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;

import java.util.Date;
import java.util.List;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
 * 内容发布统计DAO实现
 * @author ljw
 * @version 基于x1.4.0
 * @date 2020-06-03
 */
public class StatisticsPublishDetailsDaoImpl extends BaseDao<StatisticsPublishDetails>
        implements StatisticsPublishDetailsDaoExt {

    @Override
    public List<PublishPageVo> publishData(Integer siteId, Integer publishType, Boolean sort, String key,
                                           Date startDate, Date endDate) {
        return query(siteId, publishType, sort, key, startDate, endDate).fetch();
    }

    /**通用查询**/
    private JPAQuery<PublishPageVo> query(Integer siteId, Integer publishType, Boolean sort, String key,
                                          Date startDate, Date endDate) {
        QStatisticsPublishDetails content = QStatisticsPublishDetails.statisticsPublishDetails;
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
        JPAQuery<PublishPageVo> query = null;
        // 按栏目
        if (publishType.equals(PUBLISH_CHANNEL_TYPE)) {
            boolbuild.and(content.types.eq(PUBLISH_CHANNEL_TYPE));
            query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
                    content.channel.id.as("id"),
                    content.channel.name.as("name"),
                    content.numbers.sum().as("value")))
                    .from(content);
            query.where(boolbuild).groupBy(content.channelId,content.channel.name);
            if (sort != null && sort) {
                query.orderBy(content.channelId.asc());
            } else {
                query.orderBy(content.channelId.desc());
            }
            //增加使用查询缓存
            query.setHint(QueryHints.HINT_CACHEABLE, true);
        } else if (publishType.equals(PUBLISH_USER_TYPE)) {
            boolbuild.and(content.types.eq(PUBLISH_USER_TYPE));
            //名字或者真实姓名
            if (StringUtils.isNotBlank(key)) {
                boolbuild.and(content.user.username.like("%" + key + "%")
                        .or(content.user.userExt.realname.like("%" + key + "%")));
            }
            query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
                    content.user.id.as("id"),
                    content.user.username.as("name"),
                    content.user.userExt.realname.as("realName"),
                    content.numbers.sum().as("value")))
                    .from(content);
            query.where(boolbuild).groupBy(content.userId,content.user.username,content.user.userExt.realname);
            if (sort != null && sort) {
                query.orderBy(content.userId.asc());
            } else {
                query.orderBy(content.userId.desc());
            }
            //增加使用查询缓存
            query.setHint(QueryHints.HINT_CACHEABLE, true);
        } else if (publishType.equals(PUBLISH_ORG_TYPE)) {
            boolbuild.and(content.types.eq(PUBLISH_ORG_TYPE));
            if (StringUtils.isNotBlank(key)) {
                boolbuild.and(content.org.name.like("%" + key + "%"));
            }
            query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
                    content.org.id.as("id"),
                    content.org.name.as("name"),
                    content.numbers.sum().as("value")))
                    .from(content);
            query.where(boolbuild).groupBy(content.orgId,content.org.name);
            if (sort != null && sort) {
                query.orderBy(content.orgId.asc());
            } else {
                query.orderBy(content.orgId.desc());
            }

            //增加使用查询缓存
            query.setHint(QueryHints.HINT_CACHEABLE, true);
        } else if (publishType.equals(PUBLISH_SITE_TYPE)) {
            boolbuild.and(content.types.eq(PUBLISH_SITE_TYPE));
            if (StringUtils.isNotBlank(key)) {
                boolbuild.and(content.site.name.like("%" + key + "%"));
            }
            query = super.getJpaQueryFactory().select(Projections.bean(PublishPageVo.class,
                    content.site.id.as("id"),
                    content.site.name.as("name"),
                    content.numbers.sum().as("value")))
                    .from(content);
            query.where(boolbuild).groupBy(content.siteId,content.site.name);
            if (sort != null && sort) {
                query.orderBy(content.siteId.asc());
            } else {
                query.orderBy(content.siteId.desc());
            }
            //增加使用查询缓存
            query.setHint(QueryHints.HINT_CACHEABLE, true);
        }
        return query;
    }

    @Override
    public List<StatisticsPublishDetails> getList(Integer type,Integer siteId, Date startDate, Date endDate) {
        QStatisticsPublishDetails details = QStatisticsPublishDetails.statisticsPublishDetails;
        BooleanBuilder boolbuild = new BooleanBuilder();
        if (type != null) {
            boolbuild.and(details.types.eq(type));
        }
        if (siteId != null) {
            boolbuild.and(details.siteId.eq(siteId));
        }
        if (startDate != null) {
            boolbuild.and(details.statisticsDay.goe(MyDateUtils.getStartDate(startDate)));
        }
        if (endDate != null) {
            boolbuild.and(details.statisticsDay.loe(MyDateUtils.getFinallyDate(endDate)));
        }
        return getJpaQueryFactory()
                .selectFrom(details).where(boolbuild).orderBy(details.numbers.desc()).fetch();
    }
}
