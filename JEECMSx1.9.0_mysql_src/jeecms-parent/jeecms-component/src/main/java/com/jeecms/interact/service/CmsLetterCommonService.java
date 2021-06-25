package com.jeecms.interact.service;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.domain.IBaseFlow;
import com.jeecms.common.exception.GlobalException;

import java.util.Collection;

/**
 * 信件开放接口服务
 *
 * @author: tom
 * @date:
 */
public interface CmsLetterCommonService {
    /**
     * 获取流转数据对象
     *
     * @param dataId
     * @return
     */
    IBaseFlow getFlowObject(Integer dataId);

    /**
     * 撤回待办理状态
     *
     * @param ids
     * @throws GlobalException
     */
    void revokeToUnprocess(Collection<Integer> ids, CoreUser user) throws GlobalException;
}
