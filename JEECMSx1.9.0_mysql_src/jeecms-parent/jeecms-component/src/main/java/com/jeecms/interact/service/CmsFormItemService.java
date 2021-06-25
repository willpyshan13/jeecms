package com.jeecms.interact.service;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.interact.domain.CmsFormItemEntity;

/**
 * 表单项service接口
 * @author: tom
 * @date:
 */
public interface CmsFormItemService  extends IBaseService<CmsFormItemEntity, Integer> {
    void deleteByFormId(Integer formId);
}
