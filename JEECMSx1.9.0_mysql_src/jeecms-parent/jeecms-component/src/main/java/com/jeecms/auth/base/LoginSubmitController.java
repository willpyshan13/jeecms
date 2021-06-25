package com.jeecms.auth.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.dto.RequestLoginUser;
import com.jeecms.auth.service.LoginService;
import com.jeecms.common.annotation.ContentSecurity;
import com.jeecms.common.annotation.ContentSecurityAttribute;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.LoggerUtils;
import com.jeecms.common.util.UserAgentUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.SysLog;
import com.jeecms.system.service.SysLogService;
import com.jeecms.util.SystemContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

/**
 * 登录controller
 * 
 * @author: tom
 * @date: 2018年3月3日 下午3:13:10
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@RestController
public class LoginSubmitController {

        @Autowired
        private LoginService loginService;
        @Autowired
        private SysLogService logService;

        /**
         * 自定义token head标识符
         */
        @Value("${token.header}")
        protected String tokenHeader;
        @Value("${user.auth}")
        protected String userAuth;

        /**
         * 登录
         * 
         * @Title: login
         * @param request
         *                HttpServletRequest
         * @param response
         *                HttpServletResponse
         * @param requestLoginUser
         *                RequestLoginUser
         * @throws GlobalException
         *                 GlobalException
         */
        @ContentSecurity
        public ResponseInfo login(HttpServletRequest request, HttpServletResponse response,
                        @ContentSecurityAttribute("requestLoginUser") @Valid RequestLoginUser requestLoginUser)
                                        throws GlobalException {
                Map<String, Object> data = null;
                data = loginService.login(requestLoginUser, request, response);
                CmsSite site = SystemContextUtils.getSite(request);
                String redirectUrl = site.getMemberRedirectUrl();
                return new ResponseInfo(SystemExceptionEnum.SUCCESSFUL.getCode(),
                                SystemExceptionEnum.SUCCESSFUL.getDefaultMessage(),  redirectUrl, data);
        }

        /**
         * 退出
         * 
         * @Title: logout
         * @param token
         *                token
         * @param request
         *                HttpServletRequest
         * @param response
         *                HttpServletResponse
         * @throws GlobalException
         *                 GlobalException
         */
        public ResponseInfo logout(String token, HttpServletRequest request, HttpServletResponse response)
                        throws GlobalException {
                CoreUser user = SystemContextUtils.getUser(request);
                CmsSite site = SystemContextUtils.getSite(request);
                loginService.logout(token,request,response);
                ResponseInfo responseInfo = new ResponseInfo(SystemExceptionEnum.SUCCESSFUL.getCode(),
                        SystemExceptionEnum.SUCCESSFUL.getDefaultMessage(), new Object());
                //当前请求路径非后台路径不进行日志记录
                String urlPath = RequestUtils.getRequestUrl(request);
                String methods = request.getMethod();
                //调用线程池中的线程进行异步处理
                SysLog log = new SysLog();
                // 设置客户端ip
                log.setClientIp(LoggerUtils.getCliectIp(request));
                // 设置请求方法
                log.setMethod(request.getMethod());
                // 设置请求参数内容json字符串
                String paramData = JSON.toJSONString(request.getParameterMap(),
                        SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue);
                log.setParamData(paramData);
                // 设置sessionId
                String sessionId = request.getRequestedSessionId();
                log.setSessionId(sessionId);
                // 请求耗时
                Object sendTime = request.getAttribute(WebConstants.LOGGER_SEND_TIME);
                if (sendTime != null) {
                        long time = Long.parseLong(sendTime.toString());
                        long currentTime = System.currentTimeMillis();
                        log.setTimeConsuming(Integer.valueOf((currentTime - time) + ""));
                        //接口返回时间
                        log.setReturmTime(currentTime + "");
                }
                //浏览器
                log.setBrowser(UserAgentUtils.getBrowserInfo(request));
                //操作系统
                log.setOs(UserAgentUtils.getClientOS(request));
                //用户代理
                log.setUserAgent(UserAgentUtils.getBrowerUserAgent(request));
                // 设置请求地址
                log.setUri(urlPath);
                logService.asyncLog(methods, log, responseInfo, user, site);
                return responseInfo;
        }

        public String getTokenHeader() {
                return tokenHeader;
        }

        public String getUserAuth() {
                return userAuth;
        }

        public void setTokenHeader(String tokenHeader) {
                this.tokenHeader = tokenHeader;
        }

        public void setUserAuth(String userAuth) {
                this.userAuth = userAuth;
        }

        final String[] allowedFields = new String[] { "identity", "desStr", "target", "captcha", "uniquePushId",
                "token", "codeMessage" };

        @InitBinder
        public void initBinder(WebDataBinder binder) {
                binder.setAllowedFields(allowedFields);
        }

}
