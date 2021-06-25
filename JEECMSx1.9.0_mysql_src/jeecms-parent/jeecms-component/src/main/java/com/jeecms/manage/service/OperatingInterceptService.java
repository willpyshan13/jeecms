/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.manage.service;

import com.jeecms.common.exception.GlobalException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 操作拦截处理接口
 * @author: chenming
 * @date: 2020/3/17 16:04  
 */
public interface OperatingInterceptService {

    /**
     * 校验其是否拦截
     * @param request
     * @throws IOException IO异常
     * @return Integer  1-阻止    2-不阻止
     */
    boolean isIntercept(HttpServletRequest request) throws IOException, GlobalException;

    /**
     * 新增操作拦截日志
     * @param request   此次request请求
     * @throws GlobalException  全局异常
     */
    void saveManageLog(HttpServletRequest request,String requestParameter) throws GlobalException, IOException;
}
