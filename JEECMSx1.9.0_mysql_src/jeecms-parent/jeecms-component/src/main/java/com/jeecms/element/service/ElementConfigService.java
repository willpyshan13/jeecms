package com.jeecms.element.service;

import com.jeecms.common.exception.GlobalException;

/**
 * @author ljw
 */
public interface ElementConfigService {

    /**
     * 重写验证
     * @param attrName 验证码key
     * @param validateCode 验证码
     * @return int
     * @throws GlobalException 异常
     */
    int validateCode(String attrName, String validateCode) throws GlobalException;

    /**
     * 是否允许开启双因子
     * @param siteId 站点ID
     * @return boolean
     */
    boolean check(Integer siteId);
}
