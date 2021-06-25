/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.content.dao.ext.CmsModelItemDaoExt;
import com.jeecms.content.domain.CmsModelItem;
import com.jeecms.content.domain.querydsl.QCmsModelItem;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.lang.StringUtils;
import org.hibernate.jpa.QueryHints;

/**
 * 模型字段dao实现类
 * @author: chenming
 * @date: 2020/3/27 10:55   
 */
public class CmsModelItemDaoImpl extends BaseDao<CmsModelItem> implements CmsModelItemDaoExt {

    @Override
    public String getModelItemByDataType(Integer modelId,String dataType) {
        QCmsModelItem qCmsModelItem = QCmsModelItem.cmsModelItem;
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(qCmsModelItem.hasDeleted.eq(false));
        exp.and(qCmsModelItem.modelId.eq(modelId));
        if (StringUtils.isNotBlank(dataType)) {
            exp.and(qCmsModelItem.dataType.eq(dataType));
        }
        return super.getJpaQueryFactory().select(qCmsModelItem.field).from(qCmsModelItem)
                .setHint(QueryHints.HINT_CACHEABLE, true)
                .orderBy(qCmsModelItem.sortNum.asc(),qCmsModelItem.sortWeight.asc()).where(exp)
                .fetchFirst();
    }
}
