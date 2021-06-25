package com.jeecms.protection.service.impl;/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.protection.service.GradeProtectionService;
import com.jeecms.protection.service.ParameterDecryptionUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author: chenming
 * @date: 2020/7/31 10:12
 */
@Service
public class ParameterDecryptionUtilServiceImpl implements ParameterDecryptionUtilService {

    @Autowired(required = false)
    private GradeProtectionService gradeProtectionService;

    private boolean checkGradeProtection() {
        if (gradeProtectionService == null) {
            return false;
        }
        return gradeProtectionService.checkGradeProtection();
    }

    @Override
    public Map<String, String> decryChannelOrContentContent(Map<String, String> txtMap) {
        if (!checkGradeProtection()) {
            return txtMap;
        }
        if (!CollectionUtils.isEmpty(txtMap)) {
            for (String key:txtMap.keySet()) {
                String value = txtMap.get(key);
                txtMap.put(key, gradeProtectionService.decryptStr(value));
            }
        }
        return txtMap;
    }

    @Override
    public String decryChannelOrContentParameter(String parameter) {
        if (!checkGradeProtection()) {
            return parameter;
        }
        return gradeProtectionService.decryptStr(parameter);
    }
}
