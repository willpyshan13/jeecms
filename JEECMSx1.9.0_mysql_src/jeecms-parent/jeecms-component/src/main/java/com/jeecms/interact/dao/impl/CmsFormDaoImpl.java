package com.jeecms.interact.dao.impl;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.common.page.Paginable;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.interact.dao.ext.CmsFormDaoExt;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.querydsl.QCmsFormEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 表单扩展查询实现
 * @author: tom
 * @date: 2020/2/14 10:55   
 */
public class CmsFormDaoImpl implements CmsFormDaoExt {

    @Override
    public Page<CmsFormEntity> getPage(Short scene, Integer siteId, Integer typeId, Short status, String name, Pageable pageable) {
        JPAQuery<CmsFormEntity> query = new JPAQuery<CmsFormEntity>(this.em);
        QCmsFormEntity form = QCmsFormEntity.cmsFormEntity;
        appendQuery(query, form, siteId, scene, typeId,status,name );
        return QuerydslUtils.page(query, pageable, form);
    }

    @Override
    public List<CmsFormEntity> getList(Short scene, Integer siteId, Integer typeId, Short status, String name, Paginable paginable) {
        JPAQuery<CmsFormEntity> query = new JPAQuery<CmsFormEntity>(this.em);
        QCmsFormEntity form = QCmsFormEntity.cmsFormEntity;
        appendQuery(query, form, siteId, scene, typeId,status,name );
        return QuerydslUtils.list(query, paginable, form);
    }

    @Override
    public Long getCount(Short scene, Integer siteId, Integer typeId, Short status, String name) {
        JPAQuery<CmsFormEntity> query = new JPAQuery<CmsFormEntity>(this.em);
        QCmsFormEntity form = QCmsFormEntity.cmsFormEntity;
        appendQuery(query, form, siteId, scene, typeId,status,name );
        return query.fetchCount();
    }

    private void appendQuery(JPAQuery<CmsFormEntity> query, QCmsFormEntity form, Integer siteId,
                             Short scene, Integer typeId, Short status, String name) {
        query.from(form);
        // 条件查询连接对象
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(form.hasDeleted.eq(false));
        if(siteId!=null){
            exp.and(form.siteId.eq(siteId));
        }
        if(scene!=null){
            exp.and(form.formScene.eq(scene));
        }
        if(typeId!=null){
            if(typeId.equals(0)){
                exp.and(form.typeId.isNull());
            }else{
                exp.and(form.typeId.eq(typeId));
            }
        }
        if(status!=null){
            Date now = Calendar.getInstance().getTime();
            if(CmsFormConstant.FORM_VIEW_STATU_NO_PUBLISH.equals(status)){
                /** 0未发布 查询未发布和已暂停的*/
                exp.and(form.status.in(CmsFormConstant.FORM_STATU_NO_PUBLISH,CmsFormConstant.FORM_STATU_STOP));
            }else if(CmsFormConstant.FORM_VIEW_STATU_STOP.equals(status)){
                /**已结束 结束时间不为空且早于现在*/
                exp.and(form.status.in(CmsFormConstant.FORM_STATU_PUBLISH));
                exp.and(form.endTime.isNotNull());
                exp.and(form.endTime.before(now));
            }else if(CmsFormConstant.FORM_VIEW_STATU_PUBLISH.equals(status)){
                /**运行中 结束时间为空或晚于现在*/
                exp.and(form.status.in(CmsFormConstant.FORM_STATU_PUBLISH));
                BooleanBuilder subQuery = new BooleanBuilder();
                subQuery.or(form.endTime.isNull());
                subQuery.or(form.endTime.after(now));
                exp.and(subQuery);
            }
        }
        if (StringUtils.isNotBlank(name)) {
            exp.and(form.title.like("%" + name + "%"));
        }
        query.orderBy(form.id.desc());
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.where(exp);
    }

    private EntityManager em;

    @javax.persistence.PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }

}

