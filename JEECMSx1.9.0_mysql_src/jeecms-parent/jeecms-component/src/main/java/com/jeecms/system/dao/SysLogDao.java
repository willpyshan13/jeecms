/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.system.dao.ext.SysLogDaoExt;
import com.jeecms.system.domain.SysLog;

import java.util.Date;


/**
 * 日志Dao
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-05-29 16:58:45
 */
public interface SysLogDao extends IBaseDao<SysLog, Integer>, SysLogDaoExt {

    /**
     * 删除传入时间前的数据
     *
     * @param time 传入时间
     */
    void deleteAllByCreateTimeBefore(Date time);
}
