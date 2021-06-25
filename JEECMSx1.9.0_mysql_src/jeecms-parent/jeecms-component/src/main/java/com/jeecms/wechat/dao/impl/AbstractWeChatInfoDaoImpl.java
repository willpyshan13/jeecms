package com.jeecms.wechat.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.wechat.dao.ext.AbstractWeChatInfoDaoExt;
import com.jeecms.wechat.domain.AbstractWeChatInfo;
import com.jeecms.wechat.domain.querydsl.QAbstractWeChatInfo;
import com.querydsl.core.BooleanBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractWeChatToken
 *
 * @author: qqwang
 * @date: 2018年4月16日 上午11:05:40
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */
public class AbstractWeChatInfoDaoImpl extends BaseDao<AbstractWeChatInfo> implements AbstractWeChatInfoDaoExt {

    @Override
    public List<AbstractWeChatInfo> getList(Integer userId, Integer siteId, Short type) {
        QAbstractWeChatInfo qAbstractWeChatInfo = QAbstractWeChatInfo.abstractWeChatInfo;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qAbstractWeChatInfo.hasDeleted.eq(false));
        if (siteId != null) {
            builder.and(qAbstractWeChatInfo.siteId.eq(siteId));
        }
        if (userId != null) {
            builder.and(qAbstractWeChatInfo.users.any().id.eq(userId));
        }
        if (type != null) {
            builder.and(qAbstractWeChatInfo.type.eq(type));
        }
        return super.getJpaQueryFactory().selectFrom(qAbstractWeChatInfo).where(builder).fetch();
    }

    @Override
    public List<AbstractWeChatInfo> getListForWechat(Integer siteId, List<String> appids) {
        QAbstractWeChatInfo qAbstractWeChatInfo = QAbstractWeChatInfo.abstractWeChatInfo;
        BooleanBuilder builder = new BooleanBuilder();
        if (appids.isEmpty()) {
            return new ArrayList<>(0);
        }
        if(siteId != null) {
            builder.and(qAbstractWeChatInfo.siteId.eq(siteId));
        }
        builder.and(qAbstractWeChatInfo.appId.in(appids));
        builder.and(qAbstractWeChatInfo.type.eq((short) 1));
        return super.getJpaQueryFactory().selectFrom(qAbstractWeChatInfo).where(builder).fetch();
    }
}
