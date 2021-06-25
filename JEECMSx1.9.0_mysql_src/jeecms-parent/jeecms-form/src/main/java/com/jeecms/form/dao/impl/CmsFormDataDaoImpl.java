package com.jeecms.form.dao.impl;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.common.page.Paginable;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.form.dao.ext.CmsFormDataDaoExt;
import com.jeecms.form.domain.CmsFormDataEntity;
import com.jeecms.form.domain.querydsl.QCmsFormDataEntity;
import com.jeecms.form.domain.vo.CmsFormDataProviceVo;
import com.jeecms.form.domain.vo.CmsFormDataTimeVo;
import com.jeecms.system.service.AreaService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.jpa.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * 信件扩展查询
 *
 * @author: tom
 * @date: 2020/1/8 13:48
 */
public class CmsFormDataDaoImpl extends BaseDao<CmsFormDataEntity> implements CmsFormDataDaoExt {
    @Autowired
    private AreaService areaService;

    @Override
    public Page<CmsFormDataEntity> getPage(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc,
                                           String username, String ip, String cookieIdentity, String wxopenId,
                                           Integer userId, Date createTimeMin, Date createTimeMax, Pageable pageable) {
        JPAQuery<CmsFormDataEntity> query = new JPAQuery<CmsFormDataEntity>(this.em);
        QCmsFormDataEntity form = QCmsFormDataEntity.cmsFormDataEntity;
        appendQuery(query,form,formId,isRead,proviceCode, cityCode, isPc,username,ip,cookieIdentity,wxopenId,userId,createTimeMin,createTimeMax);
        return QuerydslUtils.page(query, pageable, form);
    }

    @Override
    public List<CmsFormDataEntity> getList(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username, String ip, String cookieIdentity, String wxopenId, Integer userId, Date createTimeMin, Date createTimeMax, Paginable paginable) {
        JPAQuery<CmsFormDataEntity> query = new JPAQuery<CmsFormDataEntity>(this.em);
        QCmsFormDataEntity form = QCmsFormDataEntity.cmsFormDataEntity;
        appendQuery(query,form,formId,isRead,proviceCode, cityCode, isPc,username,ip,cookieIdentity,wxopenId,userId,createTimeMin,createTimeMax);
        return QuerydslUtils.list(query, paginable, form);
    }

    @Override
    public Long getCount(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username, String ip,
                         String cookieIdentity, String wxopenId, Integer userId, Date createTimeMin, Date createTimeMax) {
        JPAQuery<CmsFormDataEntity> query = new JPAQuery<CmsFormDataEntity>(this.em);
        QCmsFormDataEntity form = QCmsFormDataEntity.cmsFormDataEntity;
        appendQuery(query,form,formId,isRead,proviceCode, cityCode, isPc,username,ip,cookieIdentity,wxopenId,userId,createTimeMin,createTimeMax);
        return query.fetchCount();
    }

    @Override
    public List<CmsFormDataProviceVo> staticByProvince(Integer formId) {
        QCmsFormDataEntity data = QCmsFormDataEntity.cmsFormDataEntity;
        JPAQuery<Tuple> query = getJpaQueryFactory().select(data.provinceCode,
                data.id.count())
                .from(data);
        // 条件查询连接对象
        BooleanBuilder exp = new BooleanBuilder();
        query.groupBy(data.provinceCode);
        if (formId != null) {
            exp.and(data.formId.eq(formId));
        }
        exp.and(data.hasDeleted.eq(false));
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.where(exp);
        query.orderBy(data.id.count().desc());
        QueryResults<Tuple> queryResults;
        List<CmsFormDataProviceVo> content = new ArrayList<>();
        queryResults = query.fetchResults();
        for (Tuple tuple : queryResults.getResults()) {
            CmsFormDataProviceVo vo = new CmsFormDataProviceVo();
            vo.setProvince(tuple.get(0, String.class));
            vo.setDataCount(tuple.get(1, Long.class));
            content.add(vo);
        }
        return content;
    }

    @Override
    public List<CmsFormDataTimeVo> staticCountGroupByTime(Integer formId, Boolean pc, String province, String city, Integer showType, Date beginTime, Date endTime) {
        QCmsFormDataEntity data = QCmsFormDataEntity.cmsFormDataEntity;
        JPAQuery<Tuple> query = getJpaQueryFactory().select(data.id.count(),data.createTime.hour()).from(data);
        if(CmsFormConstant.GROUP_DAY==showType){
            query = getJpaQueryFactory().select(data.id.count(),data.createTime.dayOfYear()).from(data);
        }
        // 条件查询连接对象
        BooleanBuilder exp = new BooleanBuilder();
        query.groupBy(data.provinceCode);
        if (formId != null) {
            exp.and(data.formId.eq(formId));
        }
        if (pc != null) {
            exp.and(data.isPc.eq(pc));
        }
        if (province != null) {
            exp.and(data.provinceCode.eq(province));
        }
        if (city != null) {
            exp.and(data.cityCode.eq(city));
        }
        if (beginTime != null) {
            exp.and(data.createTime.goe(beginTime));
        }
        if (endTime != null) {
            exp.and(data.createTime.loe(endTime));
        }
        exp.and(data.hasDeleted.eq(false));
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.where(exp);
        query.orderBy(data.id.count().desc());
        QueryResults<Tuple> queryResults;
        List<CmsFormDataTimeVo> content = new ArrayList<>();
        queryResults = query.fetchResults();
        for (Tuple tuple : queryResults.getResults()) {
            CmsFormDataTimeVo vo = new CmsFormDataTimeVo();
            vo.setCount(tuple.get(0, Long.class));
            vo.setTime(tuple.get(1, String.class));
            content.add(vo);
        }
        return content;
    }

    private void appendQuery(JPAQuery<CmsFormDataEntity> query, QCmsFormDataEntity data,
                             Integer formId, Boolean isRead, String provinceCode, String cityCode, Boolean isPc, String username,
                             String ip, String cookieIdentity, String wxopenId, Integer userId,
                             Date createTimeMin, Date createTimeMax) {
        query.from(data);
        // 条件查询连接对象
        BooleanBuilder exp = new BooleanBuilder();
        if (formId != null) {
            exp.and(data.formId.eq(formId));
        }
        if (isRead != null) {
            exp.and(data.isRead.eq(isRead));
        }
        if (provinceCode != null) {
            exp.and(data.provinceCode.eq(provinceCode));
        }
        if (cityCode != null) {
            exp.and(data.cityCode.eq(cityCode));
        }
        if (isPc != null) {
            exp.and(data.isPc.eq(isPc));
        }
        if (username != null) {
            exp.and(data.user.username.like("%"+username+"%"));
        }
        if (userId != null) {
            exp.and(data.userId.eq(userId));
        }
        if (ip != null) {
            exp.and(data.ip.eq(ip));
        }
        if (cookieIdentity != null) {
            exp.and(data.cookieIdentity.eq(cookieIdentity));
        }
        if (wxopenId != null) {
            exp.and(data.wxopenId.eq(wxopenId));
        }
        if (createTimeMin != null) {
            exp.and(data.createTime.goe(createTimeMin));
        }
        if (createTimeMax != null) {
            exp.and(data.createTime.loe(createTimeMax));
        }
        exp.and(data.hasDeleted.eq(false));
        query.setHint(QueryHints.HINT_CACHEABLE, true);
        query.where(exp);
    }

    private EntityManager em;

    @javax.persistence.PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }

}
