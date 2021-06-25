package com.jeecms.interact.dao;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormTypeEntity;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * 表单类型dao
 * @author: tom
 * @date:
 */
public interface CmsFormTypeDao extends IBaseDao<CmsFormTypeEntity,Integer> {
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<CmsFormTypeEntity> findByNameAndHasDeleted(String name,Boolean hasDeleted);
}
