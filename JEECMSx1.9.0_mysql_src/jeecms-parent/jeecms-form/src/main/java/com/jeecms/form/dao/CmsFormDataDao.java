package com.jeecms.form.dao;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.form.domain.CmsFormDataEntity;
import com.jeecms.form.dao.ext.CmsFormDataDaoExt;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 表单提交数据dao
 * @author: tom
 * @date:
 */
public interface CmsFormDataDao extends IBaseDao<CmsFormDataEntity,Integer>, CmsFormDataDaoExt {
    void deleteAllByFormId(Integer formId);
}
