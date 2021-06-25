package com.jeecms.interact.dao;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.interact.domain.CmsFormItemEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 表单项dao
 * @author: tom
 * @date:
 */
public interface CmsFormItemDao extends IBaseDao<CmsFormItemEntity,Integer> {
    @Modifying
    @Query("delete from CmsFormItemEntity bean where 1 = 1 and bean.formId=?1")
    void deleteByFormId(Integer formId);
}
