package com.jeecms.backup.dao.impl;

import com.jeecms.backup.dao.ext.DatabaseBackupRecordDaoExt;
import com.jeecms.backup.domain.DatabaseBackupRecord;
import com.jeecms.backup.domain.querydsl.QDatabaseBackupRecord;
import com.jeecms.common.base.dao.BaseDao;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.poi.hssf.record.BackupRecord;

import java.util.Date;
import java.util.List;

import static com.jeecms.backup.domain.querydsl.QDatabaseBackupRecord.databaseBackupRecord;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2019/8/3 10:26
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class DatabaseBackupRecordDaoImpl extends BaseDao<BackupRecord> implements DatabaseBackupRecordDaoExt {

	@Override
	public long fixLostState() {
		long modifyCount = 0;
		modifyCount += getJpaQueryFactory()
			.update(databaseBackupRecord)
			.set(databaseBackupRecord.state, DatabaseBackupRecord.ERROR_BACKUP)
			.where(databaseBackupRecord.state.eq(DatabaseBackupRecord.IN_BACKUP))
			.execute();
		modifyCount += getJpaQueryFactory()
			.update(databaseBackupRecord)
			.set(databaseBackupRecord.state, DatabaseBackupRecord.ERROR_RECOVERY)
			.where(databaseBackupRecord.state.eq(DatabaseBackupRecord.IN_RECOVERY))
			.execute();

		return modifyCount;
	}

	@Override
	public long findByThanCreateTime(Date time) {
		QDatabaseBackupRecord databaseBackupRecord = QDatabaseBackupRecord.databaseBackupRecord;
		BooleanBuilder builder = new BooleanBuilder();
		if (time != null) {
			builder.and(databaseBackupRecord.createTime.goe(time));
		}
		JPAQuery<DatabaseBackupRecord> query = getJpaQueryFactory().selectFrom(databaseBackupRecord).where(builder);
		return query.fetchCount();
	}

	@Override
	public long findByLessCreateTime(Date time) {
		QDatabaseBackupRecord databaseBackupRecord = QDatabaseBackupRecord.databaseBackupRecord;
		BooleanBuilder builder = new BooleanBuilder();
		if (time != null) {
			builder.and(databaseBackupRecord.createTime.loe(time));
		}
		JPAQuery<DatabaseBackupRecord> query = getJpaQueryFactory().selectFrom(databaseBackupRecord).where(builder);
		return query.fetchCount();
	}

	@Override
	public List<DatabaseBackupRecord> findByLessCreateTimeList(Date time) {
		QDatabaseBackupRecord databaseBackupRecord = QDatabaseBackupRecord.databaseBackupRecord;
		BooleanBuilder builder = new BooleanBuilder();
		if (time != null) {
			builder.and(databaseBackupRecord.createTime.loe(time));
		}
		JPAQuery<DatabaseBackupRecord> query = getJpaQueryFactory().selectFrom(databaseBackupRecord).where(builder);
		return query.fetch();
	}
}
