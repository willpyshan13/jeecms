package com.jeecms.form.service;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.form.domain.CmsFormDataAttrEntity;
import com.jeecms.system.domain.CmsSite;
import org.springframework.scheduling.annotation.Async;

/**
 * 表单属性service接口
 * @author: tom
 * @date:
 */
public interface CmsFormDataAttrService extends IBaseService<CmsFormDataAttrEntity, Integer> {
    /**
     * 处理文档预览生成pdf
     * @param id
     * @param site
     * @return
     * @throws GlobalException
     */
     String uploadDoc(Integer id, CmsSite site) throws GlobalException;
}
