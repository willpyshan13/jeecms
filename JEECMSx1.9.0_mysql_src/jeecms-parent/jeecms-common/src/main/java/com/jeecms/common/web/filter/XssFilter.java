package com.jeecms.common.web.filter;

import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.constants.TplConstants;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.PropertiesUtil;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.CookieUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.web.util.ResponseUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XssFilter
 *
 * @author: tom
 * @date: 2018年11月26日 下午6:58:23
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class XssFilter implements Filter {
    static Logger logger = LoggerFactory.getLogger(XssFilter.class);
    private  String active = "prod";
    /**
     * 字母数字下划线
     */
    private static final String COOKIE_RE = "^[A-Za-z0-9_]+$";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            active = PropertiesUtil.loadSystemProperties().getProperty("spring.profiles.active");
        }catch (IOException e){
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest xssRequest = req;
        /** 不拦截模板和资源 */
        if (!isExcludeUrl(req)) {
            xssRequest = new XssHttpServletRequestWrapper(req);
        }
        String requestStr = RequestUtils.getRequestString(req);
        if (ServerModeEnum.prod.toString().equals(active)) {
            /**验证localeCookie cookie*/
            Cookie localeCookie = CookieUtils.getCookie(req, "localeCookie");
            if (localeCookie != null && StringUtils.isNotBlank(localeCookie.getValue())) {
                String localeCookieVal = localeCookie.getValue();
                /**非法cookie*/
                if (!ReUtil.isMatch(COOKIE_RE, localeCookieVal)) {
                    logger.info("======访问地址发现非法cookie，已拦截======其非法地址为：" + req.getRequestURL().toString());
                    String msg = MessageResolver.getMessage(SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                            SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage());
                    String code = SystemExceptionEnum.ILLEGAL_PARAM.getCode();
                    ResponseInfo responseInfo = new ResponseInfo(code, msg, req, null, null);
                    ResponseUtils.renderJson(res, JSON.toJSONString(responseInfo));
                    return;
                }
            }
            if (RequestUtils.isValidRequestUri(requestStr)
                    ||RequestUtils.isValidRequestUri(req.getRequestURL().toString())) {
                logger.info("======访问地址发现非法字符，已拦截======其非法地址为：" + req.getRequestURL().toString());
                String msg = MessageResolver.getMessage(SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                        SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage());
                String code = SystemExceptionEnum.ILLEGAL_PARAM.getCode();
                ResponseInfo responseInfo = new ResponseInfo(code, msg, req, null, null);
                ResponseUtils.renderJson(res, JSON.toJSONString(responseInfo));
                return;
            }
        }
        chain.doFilter(xssRequest, response);
    }



    private boolean isExcludeUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (StringUtils.isNoneBlank(ctx)) {
            uri = uri.substring(ctx.length());
        }
        if (uri.startsWith(WebConstants.ADMIN_PREFIX + "/template")) {
            return true;
        }
        if (uri.startsWith(WebConstants.ADMIN_PREFIX + "/resource")) {
            return true;
        }
        if (uri.startsWith(WebConstants.UPLOAD_PATH)) {
            return true;
        }
        if (uri.startsWith(TplConstants.RES_PATH)) {
            return true;
        }
        if (uri.endsWith(WebConstants.DYNAMIC_SUFFIX)) {
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
    }

}
