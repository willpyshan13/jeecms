package com.jeecms.interact.service.impl;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.domain.IBaseFlow;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.interact.service.CmsLetterCommonService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * 信件实现类
 *
 * @author: tom
 * @date: 2021/2/1 11:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
@ConditionalOnMissingClass(value = "com.jeecms.letter.service.impl.CmsLetterLogServiceImpl")
public class CmsLetterCommonServiceImpl implements CmsLetterCommonService {
    @Override
    public IBaseFlow getFlowObject(Integer dataId) {
        return null;
    }

    @Override
    public void revokeToUnprocess(Collection<Integer> ids, CoreUser user) throws GlobalException {

    }
}
