/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.universal.service;

import com.jeecms.common.exception.GlobalException;
import org.apache.http.ParseException;

import java.io.IOException;

/**
 * request请求云平台util的service接口
 * @author: chenming
 * @date: 2020/6/15 14:29
 */
public interface HttpRequestPlatformUtilService {

    /**
     * 获取用户授权信息
     * @param isAppId   true->appId、false->手机号
     * @return String
     * @throws GlobalException 全局异常
     */
    String getUserParameter(boolean isAppId) throws GlobalException,IOException,ParseException;
}
