package com.jeecms.form.service;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.form.domain.CmsFormDataAttrResEntity;
import com.jeecms.system.domain.CmsSite;

import java.util.List;

/**
 * 信件多资源service接口
 * @author: tom
 * @date:
 */
public interface CmsFormDataAttrResService extends IBaseService<CmsFormDataAttrResEntity, Integer> {
    /**
     * 处理文档预览生成pdf
     * @param id
     * @param site
     * @return
     * @throws GlobalException
     */
    void uploadDoc(List<Integer> id, CmsSite site) throws GlobalException;
}
