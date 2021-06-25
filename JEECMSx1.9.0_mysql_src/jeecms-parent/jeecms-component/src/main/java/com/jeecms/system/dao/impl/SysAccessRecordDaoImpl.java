package com.jeecms.system.dao.impl;

import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.publish.constants.PublishConstant;
import com.jeecms.publish.domain.vo.ContentFlowVo;
import com.jeecms.statistics.domain.dto.StatisticsFlowDto;
import com.jeecms.statistics.domain.dto.StatisticsFlowRealTimeItemDto;
import com.jeecms.system.dao.ext.SysAccessRecordDaoExt;
import com.jeecms.system.domain.SysAccessRecord;
import com.jeecms.system.domain.querydsl.QSysAccessRecord;
import com.jeecms.system.domain.vo.MassScoreVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 访问记录dao实现类
 *
 * @author: chenming
 * @date: 2019年6月26日 上午8:58:54
 */
public class SysAccessRecordDaoImpl extends BaseDao<SysAccessRecord> implements SysAccessRecordDaoExt {

	@Override
	public List<SysAccessRecord> getFlowPv(Date startTime, Date endTime) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		exp.and(accessRecord.siteId.isNotNull());
		return null;
	}

	@Override
	public List<SysAccessRecord> getSource(Date startTime, Date endTime, Integer siteId, Boolean newVisitor,
										   Short accessSourceClient, Integer sourceType) {
		QSysAccessRecord record = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(record.hasDeleted.eq(false));
		if (startTime != null) {
			exp.and(record.createTime.goe(startTime));
		}
		if (endTime != null) {
			exp.and(record.createTime.loe(endTime));
		}
		if (siteId != null) {
			exp.and(record.siteId.eq(siteId));
		}
		if (newVisitor != null) {
			exp.and(record.newVisitor.eq(newVisitor));
		}
		if (sourceType != null) {
			exp.and(record.sorceUrlType.eq(sourceType));
		}
		if (accessSourceClient != null) {
			//等于1 pc端  不等于1 移动端
			if (accessSourceClient == 1) {
				exp.and(record.accessSourceClient.eq(SysAccessRecord.ACCESS_TYPE_PC));
			} else {
				exp.and(record.accessSourceClient.ne(SysAccessRecord.ACCESS_TYPE_PC));
			}
		}
		return getJpaQueryFactory().select(record)
				.from(record)
				.where(exp)
				.fetch();
	}

	@Override
	public List<SysAccessRecord> getAccessPage(Date startTime, Date endTime, Integer siteId, Boolean newVisitor,
											   Integer sorceUrlType) {
		QSysAccessRecord record = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(record.hasDeleted.eq(false));
		if (startTime != null) {
			exp.and(record.createTime.goe(startTime));
		}
		if (endTime != null) {
			exp.and(record.createTime.loe(endTime));
		}
		if (siteId != null) {
			exp.and(record.siteId.eq(siteId));
		}
		if (newVisitor != null) {
			exp.and(record.newVisitor.eq(newVisitor));
		}
		if (sorceUrlType != null) {
			exp.and(record.sorceUrlType.eq(sorceUrlType));
		}
		return getJpaQueryFactory().select(record)
				.from(record)
				.where(exp)
				.setHint("org.hibernate.cacheable", true)
				.fetch();
	}

	@Override
	public List<SysAccessRecord> getList(Date start, Date end, Integer siteId, Integer sourceType,
										 String province, Boolean visitor) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		if (start != null && end != null) {
			exp.and(accessRecord.createTime.between(MyDateUtils.getStartDate(start),
					MyDateUtils.getFinallyDate(end)));
		}
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (sourceType != null) {
			exp.and(accessRecord.sorceUrlType.eq(sourceType));
		}
		if (StringUtils.isNotBlank(province)) {
			exp.and(accessRecord.accessProvince.like("%" + province));
		}
		if (visitor != null) {
			exp.and(accessRecord.newVisitor.eq(visitor));
		}
		JPAQuery<SysAccessRecord> query = super.getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true).where(exp);
		return query.fetch();
	}

	@Override
	public List<SysAccessRecord> findByDate(Date startTime, Date endTime, Integer siteId, Integer contentId) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (startTime != null) {
			exp.and(accessRecord.createTime.goe(startTime));
		}
		if (endTime != null) {
			exp.and(accessRecord.createTime.loe(endTime));
		}
		if (contentId != null) {
			exp.and(accessRecord.contentId.eq(contentId));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp).fetch();
	}

	@Override
	public long getContentByDate(Date date, Integer siteId) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (date != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(date)));
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(date)));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp).fetchCount();
	}

	@Override
	public long countIp(Date date, Integer siteId) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (date != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(date)));
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(date)));
		}
		return getJpaQueryFactory().
				select(accessRecord.accessIp).
				from(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp).groupBy(accessRecord.accessIp).fetchCount();
	}

	@Override
	public List<SysAccessRecord> getRealTimeItem(Date startTime, Date endTime, StatisticsFlowRealTimeItemDto dto) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		exp.and(accessRecord.siteId.isNotNull());
		exp.and(accessRecord.siteId.eq(dto.getSiteId()));
		exp.and(accessRecord.createTime.goe(startTime));
		exp.and(accessRecord.createTime.loe(endTime));
		if (dto.getSorceUrlType() != null) {
			exp.and(accessRecord.sorceUrlType.eq(dto.getSorceUrlType()));
		}
		if (dto.getProvince() != null) {
			exp.and(accessRecord.accessProvince.eq(dto.getProvince()));
			if (dto.getCity() != null) {
				exp.and(accessRecord.accessCity.eq(dto.getCity()));
			}
		}
		if (dto.getAccessSourceClient() != null) {
			exp.and(accessRecord.deviceType.eq(dto.getAccessSourceClient()));
		}
		if (dto.getNewVisitor() != null) {
			exp.and(accessRecord.newVisitor.eq(dto.getNewVisitor()));
		}
		if (dto.getIsLogin() != null) {
			exp.and(accessRecord.isLogin.eq(dto.getIsLogin()));
		}
		if (dto.getUserName() != null) {
			exp.and(accessRecord.loginUserName.like("%" + dto.getUserName() + "%"));
		}
		if (dto.getAccessIp() != null) {
			exp.and(accessRecord.accessIp.like("%" + dto.getAccessIp() + "%"));
		}
		if (dto.getEntranceUrl() != null) {
			exp.and(accessRecord.accessUrl.like("%" + dto.getEntranceUrl() + "%"));
		}
		if (StringUtils.isNotBlank(dto.getEngineName())) {
			exp.and(accessRecord.engineName.eq(dto.getEngineName()));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp).fetch();
	}

	@Override
	public List<SysAccessRecord> getFlow(StatisticsFlowDto dto) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		exp.and(accessRecord.siteId.isNotNull());
		exp.and(accessRecord.siteId.eq(dto.getSiteId()));
		// 等于空说明查询的是今天的数据
		if (dto.getMinTime() == null) {
			dto.setMinTime(MyDateUtils.getStartDate(new Date()));
			dto.setMaxTime(new Date());
		}
		exp.and(accessRecord.createTime.goe(dto.getMinTime()));
		exp.and(accessRecord.createTime.loe(dto.getMaxTime()));
		if (dto.getSorceUrlType() != null) {
			exp.and(accessRecord.sorceUrlType.eq(dto.getSorceUrlType()));
		}
		if (dto.getProvince() != null) {
			exp.and(accessRecord.accessProvince.eq(dto.getProvince()));
			if (dto.getCity() != null) {
				exp.and(accessRecord.accessCity.eq(dto.getCity()));
			}
		}
		if (dto.getAccessSourceClient() != null) {
			exp.and(accessRecord.deviceType.eq(dto.getAccessSourceClient()));
		}
		if (dto.getNewVisitor() != null) {
			exp.and(accessRecord.newVisitor.eq(dto.getNewVisitor()));
		}
		if (StringUtils.isNotBlank(dto.getEngineName())) {
			exp.and(accessRecord.engineName.eq(dto.getEngineName()));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp).fetch();

	}

	@Override
	public SysAccessRecord findBySession(String sessionId, Integer siteId, Date date) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (sessionId != null) {
			exp.and(accessRecord.sessionId.eq(sessionId))
					.or(accessRecord.cookieId.eq(sessionId));
		}
		if (date != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(date)));
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(date)));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true).where(exp)
				.orderBy(accessRecord.createTime.desc()).fetchFirst();
	}

	@Override
	public SysAccessRecord findByIp(String ip, Integer siteId, Date date) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.hasDeleted.eq(false));
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (ip != null) {
			exp.and(accessRecord.accessIp.eq(ip));

		}
		if (date != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(date)));
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(date)));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp)
				.orderBy(accessRecord.createTime.desc()).fetchFirst();
	}

	@Override
	public List<ContentFlowVo> flowContribution(Integer siteId, Date startDate, Date endDate) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		if (siteId != null) {
			exp.and(accessRecord.siteId.eq(siteId));
		}
		if (startDate != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(startDate)));

		}
		if (endDate != null) {
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(endDate)));
		}
		JPAQuery<ContentFlowVo> query = super.getJpaQueryFactory().select(Projections.bean(ContentFlowVo.class,
				accessRecord.pageType.as("pageType"),
				accessRecord.contentId.as("contentId"),
				accessRecord.channelId.as("channelId")))
				.from(accessRecord).where(exp);
		return query.fetch();
	}

	@Override
	public List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		if (contentId != null && !contentId.isEmpty()) {
			exp.and(accessRecord.contentId.in(contentId));
		}
		if (startDate != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(startDate)));

		}
		if (endDate != null) {
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(endDate)));
		}
		JPAQuery<MassScoreVo> query = super.getJpaQueryFactory().select(Projections.bean(MassScoreVo.class,
				accessRecord.contentId.as("contentId"),
				accessRecord.contentId.count().as("counts")))
				.from(accessRecord).where(exp).groupBy(accessRecord.contentId);
		return query.fetch();
	}

	@Override
	public List<SysAccessRecord> typeList(Integer type, Date startDate, Date endDate) {
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		if (type != null) {
			exp.and(accessRecord.pageType.eq(type));
		}
		if (startDate != null) {
			exp.and(accessRecord.createTime.goe(MyDateUtils.getStartDate(startDate)));

		}
		if (endDate != null) {
			exp.and(accessRecord.createTime.loe(MyDateUtils.getFinallyDate(endDate)));
		}
		return getJpaQueryFactory().selectFrom(accessRecord)
				.setHint("org.hibernate.cacheable", true)
				.where(exp).fetch();
	}

	@Override
	public long getPeopleViews(Integer contentId) {
		//访客数（UV）：当前站点下，
		//有user_id，以user_id分组，统计user_id数量，记为：C1；
		//无user_id，以cookie分组，统计cookie数量，记为：C2；
		//访客数（UV）= C1+C2。
		QSysAccessRecord accessRecord = QSysAccessRecord.sysAccessRecord;
		BooleanBuilder exp = new BooleanBuilder();
		exp.and(accessRecord.pageType.eq(PublishConstant.PAGE_TYPE_CONTENT));
		exp.and(accessRecord.contentId.eq(contentId));
		exp.and(accessRecord.loginUserId.isNotNull());
		long c1 = super.getJpaQueryFactory().select(accessRecord.loginUserId
				.countDistinct()).from(accessRecord).where(exp)
				.groupBy(accessRecord.loginUserId).fetchCount();
		BooleanBuilder exp1 = new BooleanBuilder();
		exp1.and(accessRecord.pageType.eq(PublishConstant.PAGE_TYPE_CONTENT));
		exp1.and(accessRecord.contentId.eq(contentId));
		exp1.and(accessRecord.loginUserId.isNull());
		long c2 = super.getJpaQueryFactory().select(accessRecord.cookieId
				.countDistinct()).from(accessRecord).where(exp1)
				.groupBy(accessRecord.cookieId).fetchCount();
		return c1 + c2;
	}

}
