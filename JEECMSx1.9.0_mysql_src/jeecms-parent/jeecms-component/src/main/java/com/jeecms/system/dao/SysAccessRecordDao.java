/**
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.system.dao.ext.SysAccessRecordDaoExt;
import com.jeecms.system.domain.SysAccessRecord;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.Date;


/**
 * 访问记录DAO
 *
 * @author ljw
 * @version 1.0
 * @date 2019-06-22
 */
public interface SysAccessRecordDao extends IBaseDao<SysAccessRecord, Integer>, SysAccessRecordDaoExt {

    /**
     * 查询Cookie个数
     *
     * @param cookie cookie值
     * @return
     * @Title: countByCookieId
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Long countByCookieId(String cookie);

    /**
     * 查询Cookie个数
     *
     * @param userId 用户ID
     * @return
     * @Title: countByLoginUserId
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Long countByLoginUserId(Integer userId);

    /**
     * 删除传入时间前的数据
     *
     * @param time 传入时间
     */
    void deleteAllByCreateTimeBefore(Date time);
}
