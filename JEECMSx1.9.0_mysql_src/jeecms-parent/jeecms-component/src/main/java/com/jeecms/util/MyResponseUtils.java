package com.jeecms.util;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.web.util.RequestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 扩展的Response工具类
 * @author: tom
 * @date: 2020/7/31 15:02   
 */
public abstract class MyResponseUtils  {

    /**
     * 重定向到登录页
     *
     * @Title: redirectToLogin
     * @param request
     *            HttpServletRequest
     * @param response
     *            HttpServletResponse
     * @return: void
     * @throws IOException
     *             IOException
     */
    public static void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String loginUrl = WebConstants.LOGIN_URL;
        /**非pc都定位到手机端地址*/
        if(!SystemContextUtils.isPc()){
            loginUrl = WebConstants.MOBILE_LOGIN_URL;
        }
        String location = RequestUtils.getLocation(request);
        /** 非本站地址 定位到首页，防止 Open Redirect ，但fortify依然还是会报 */
        if (StringUtils.isNoneBlank(location)) {
            if (!location.startsWith(RequestUtils.getServerUrl(request))) {
                location = "/";
            }else{
                /**这里截掉域名和端口是可能会出现登录页面加载不了js 和css*/
                location = location.substring(RequestUtils.getServerUrl(request).length());
            }
        }
        if (StringUtils.isNoneBlank(request.getContextPath())) {
            loginUrl = request.getContextPath() + loginUrl;
        }
        response.sendRedirect(String.format("%s?backUrl=%s", loginUrl, location));
    }
}
