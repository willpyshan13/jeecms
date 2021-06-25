/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.protection.service.impl;

import com.jeecms.protection.service.GradeProtectionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 等级保护service实现类
 * @author: chenming
 * @date: 2020/7/30 15:23
 */
@Service
@Transactional(rollbackFor = Exception.class)
@ConditionalOnMissingClass(value = "com.jeecms.reinsurance.service.impl.CmsReinsuranceConfigServiceImpl")
public class GradeProtectionServiceImpl implements GradeProtectionService {

    @Override
    public boolean checkGradeProtection()  {
        return false;
    }

    @Override
    public List<String> decryptStrList(List<String> requestStrList) {
        return requestStrList;
    }

    @Override
    public String decryptStr(String requestStr) {
        return requestStr;
    }
}
