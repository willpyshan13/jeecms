/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.system.constants.LogConstants.LogGroup;
import com.jeecms.system.constants.LogConstants.StatisticsType;
import com.jeecms.system.dao.ext.SysLogDaoExt;
import com.jeecms.system.domain.SysLog;
import com.jeecms.system.domain.dto.SearchUserLogDto;
import com.jeecms.system.domain.querydsl.QSysLog;
import com.jeecms.system.domain.vo.SysLogSelectVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author xiaohui
 * @version 1.0
 * @date 2019/6/18 15:53
 */

public class SysLogDaoImpl extends BaseDao<SysLog> implements SysLogDaoExt {

    @Override
    public List groupByCreateDate(LogGroup em, Integer requestResult, Integer eventType,
                                  Integer operateType) {
        JPAQuery<SysLog> query = new JPAQuery<SysLog>(this.em);
        QSysLog log = QSysLog.sysLog;
        query.from(log);
        BooleanBuilder exp = new BooleanBuilder();
        if (requestResult != null) {
            exp.and(log.requestResult.eq(requestResult));
        }
        if (eventType != null) {
            exp.and(log.eventType.eq(eventType));
        }
        if (operateType != null) {
            exp.and(log.operateType.eq(operateType));
        }
        exp.and(log.hasDeleted.eq(false));
        Date date = new Date();
        Date finallyDate = null;
        Date startDate = null;
        NumberExpression<Integer> cycle;
        switch (em) {
            case HOUR:
                //当天最后一刻 23:59:59
                finallyDate = MyDateUtils.getFinallyDate(date);
                //当天起始时间 00:00:00
                startDate = MyDateUtils.getStartDate(date);
                cycle = log.createTime.hour();
                break;
            case DAY:
                //当月最后一天
                finallyDate = MyDateUtils.getSpecficMonthEnd(date, 0);
                //当月起始时间
                startDate = MyDateUtils.getSpecficMonthStart(date, 0);
                cycle = log.createTime.dayOfMonth();
                break;
            case MONTH:
                //当年最后时间
                finallyDate = MyDateUtils.getSpecficYearEnd(date, 0);
                //当年起始时间
                startDate = MyDateUtils.getSpecficYearStart(date, 0);
                cycle = log.createTime.month();
                break;
            case YEAR:
                //按年不需要限制时间
                cycle = log.createTime.year();
                break;
            default:
                //默认按小时
                finallyDate = MyDateUtils.getFinallyDate(date);
                startDate = MyDateUtils.getStartDate(date);
                cycle = log.createTime.dayOfMonth();
                break;
        }
        if (startDate != null && finallyDate != null) {
            exp.and(log.createTime.goe(startDate));
            exp.and(log.createTime.loe(finallyDate));
        }
        query.where(exp);
        query.select(cycle, log.count());
        query.groupBy(cycle);
        query.orderBy(log.createTime.asc());
        return query.createQuery().getResultList();
    }

    @Override
    public List hourlyStatistics(StatisticsType type) {
        JPAQuery<SysLog> query = new JPAQuery<SysLog>(this.em);
        QSysLog log = QSysLog.sysLog;
        query.from(log);
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(log.hasDeleted.eq(false));
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        String beginTime = simpleDateFormat.format(date);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:59:59");
        String endTime = simpleDateFormat.format(date);

        simpleDateFormat.format(date);
        exp.and(log.createTime.goe(MyDateUtils.parseDate(beginTime, MyDateUtils.COM_Y_M_D_H_M_S_PATTERN)));
        exp.and(log.createTime.loe(MyDateUtils.parseDate(endTime, MyDateUtils.COM_Y_M_D_H_M_S_PATTERN)));
        query.where(exp);
        getGroup(type, query, log);
        return query.createQuery().getResultList();
    }

    /**
     * 获取需要分组的类型
     *
     * @param type  {@link StatisticsType}
     * @param query {@link JPAQuery}
     * @param log   QSysLog
     */
    private void getGroup(StatisticsType type, JPAQuery<SysLog> query, QSysLog log) {
        switch (type) {
            case OPERATIONRESULT:
                //请求结果分组
                query.groupBy(log.requestResult);
                query.select(log.requestResult, log.count());
                break;
            case EVENTTYPE:
                //事件类型分组
                query.groupBy(log.eventType);
                query.select(log.eventType, log.count());
                break;
            case OPERATIONTYPE:
                //操作类型分组
                query.groupBy(log.operateType);
                query.select(log.operateType, log.count());
                break;
            case USERNAME:
                //用户名分组
                query.groupBy(log.username);
                query.select(log.username, log.count());
                break;
            default:
                //默认请求结果分组
                query.groupBy(log.requestResult);
                query.select(log.requestResult, log.count());
                break;
        }
    }

    @Override
    public List<SysLogSelectVo> getList(Date beginTime, Date endTime) {
        JPAQuery<SysLogSelectVo> query = new JPAQuery<SysLogSelectVo>(this.em);
        QSysLog log = QSysLog.sysLog;
        query.select(Projections.bean(
                SysLogSelectVo.class,
                log.requestResult.as("requestResult"),
                log.eventType.as("eventType"),
                log.operateType.as("operateType"),
                log.username.as("username"),
                log.createTime.as("createTime")
        ));
        query.from(log);
        BooleanBuilder exp = new BooleanBuilder();
        if (beginTime != null) {
            exp.and(log.createTime.goe(beginTime));
        }
        if (endTime != null) {
            exp.and(log.createTime.loe(endTime));
        }
        query.where(exp);
        return query.fetch();
    }

    @Override
    public Page<SysLog> getPage(SearchUserLogDto dto, Set<String> name, Pageable pageable) {
        JPAQuery<SysLog> query = new JPAQuery<>(this.em);
        QSysLog log = QSysLog.sysLog;
        query.select(log);
        query.from(log);
        BooleanBuilder exp = new BooleanBuilder();
        //用户名
        if (StringUtils.isNotBlank(dto.getUsername())) {
            exp.and(log.username.like("%" + dto.getUsername() + "%"));
        }
        //事件子类型
        if (StringUtils.isNotBlank(dto.getSubEventType())) {
            exp.and(log.subEventType.like("%" + dto.getSubEventType() + "%"));
        }
        //客户端ip
        if (StringUtils.isNotBlank(dto.getClientIp())) {
            exp.and(log.clientIp.like("%" + dto.getClientIp() + "%"));
        }
        //开始时间
        if (dto.getBeginDate() != null) {
            exp.and(log.createTime.goe(dto.getBeginDate()));
        }
        //结束时间
        if (dto.getEndDate() != null) {
            exp.and(log.createTime.loe(dto.getEndDate()));
        }
        //日志级别
        if (dto.getLogLevel() != null) {
            exp.and(log.logLevel.eq(dto.getLogLevel()));
        }
        //操作类型
        if (dto.getOperateType() != null) {
            exp.and(log.operateType.eq(dto.getOperateType()));
        }
        if (dto.getRequestResult() != null) {
            exp.and(log.requestResult.eq(dto.getRequestResult()));
        }
        if (CollUtil.isNotEmpty(name)) {
            exp.and(log.username.notIn(name));
        }
        exp.and(log.logCategory.eq(SysLog.LOG_CATEGORY_BUS)
                .or(log.logCategory.eq(SysLog.LOG_CATEGORY_SYSTEM))
                .or(log.logCategory.eq(SysLog.LOG_CATEGORY_AUDIT)));
        query.where(exp);
        return QuerydslUtils.page(query, pageable, log);
    }

    @Override
    public List<SysLog> getList(SearchUserLogDto dto, Set<String> name) {
        JPAQuery<SysLog> query = new JPAQuery<>(this.em);
        QSysLog log = QSysLog.sysLog;
        query.select(log);
        query.from(log);
        BooleanBuilder exp = new BooleanBuilder();
        //用户名
        if (StringUtils.isNotBlank(dto.getUsername())) {
            exp.and(log.username.like("%" + dto.getUsername() + "%"));
        }
        //事件子类型
        if (StringUtils.isNotBlank(dto.getSubEventType())) {
            exp.and(log.subEventType.like("%" + dto.getSubEventType() + "%"));
        }
        //客户端ip
        if (StringUtils.isNotBlank(dto.getClientIp())) {
            exp.and(log.clientIp.like("%" + dto.getClientIp() + "%"));
        }
        //开始时间
        if (dto.getBeginDate() != null) {
            exp.and(log.createTime.goe(dto.getBeginDate()));
        }
        //结束时间
        if (dto.getEndDate() != null) {
            exp.and(log.createTime.loe(dto.getEndDate()));
        }
        //日志级别
        if (dto.getLogLevel() != null) {
            exp.and(log.logLevel.eq(dto.getLogLevel()));
        }
        //操作类型
        if (dto.getOperateType() != null) {
            exp.and(log.operateType.eq(dto.getOperateType()));
        }
        if (dto.getRequestResult() != null) {
            exp.and(log.requestResult.eq(dto.getRequestResult()));
        }
        if (CollUtil.isNotEmpty(name)) {
            exp.and(log.username.notIn(name));
        }
        exp.and(log.logCategory.eq(SysLog.LOG_CATEGORY_BUS)
                .or(log.logCategory.eq(SysLog.LOG_CATEGORY_SYSTEM))
                .or(log.logCategory.eq(SysLog.LOG_CATEGORY_AUDIT)));
        query.where(exp);
        return query.fetch();
    }

    private EntityManager em;

    @javax.persistence.PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }

}
