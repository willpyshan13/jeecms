package com.jeecms.interact.dao.ext;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.page.Paginable;
import com.jeecms.interact.domain.CmsFormEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 表单扩展查询接口
 * @author: tom
 * @date:
 */
public interface CmsFormDaoExt {
    Page<CmsFormEntity> getPage(Short scene, Integer siteId, Integer typeId, Short status, String name, Pageable pageable);

    List<CmsFormEntity> getList(Short scene, Integer siteId, Integer typeId, Short status, String name, Paginable paginable);

    Long getCount(Short scene, Integer siteId, Integer typeId, Short status, String name);
}
