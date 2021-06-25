package com.jeecms.backup.dao.ext;

import com.jeecms.backup.domain.DatabaseBackupRecord;

import java.util.Date;
import java.util.List;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2019/8/3 10:30
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public interface DatabaseBackupRecordDaoExt {

	long fixLostState();

	/**
	 * 查找大于传入时间的记录
	 *
	 * @param time 时间
	 * @return 数量
	 */
	long findByThanCreateTime(Date time);

	/**
	 * 查找小于于传入时间的记录
	 *
	 * @param time 时间
	 * @return 数量
	 */
	long findByLessCreateTime(Date time);

	/**
	 * 查找小于于传入时间的备份记录
	 *
	 * @param time 时间
	 * @return long
	 */
	List<DatabaseBackupRecord> findByLessCreateTimeList(Date time);

}
