/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.component;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.jeecms.auth.constants.AuthConstant;
import com.jeecms.auth.constants.AuthConstant.LoginFailProcessMode;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.LoginDetail;
import com.jeecms.auth.dto.RequestLoginUser;
import com.jeecms.auth.service.CaptchaService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.auth.service.LoginService;
import com.jeecms.common.base.domain.RequestLoginTarget;
import com.jeecms.common.captcha.KaptchaConfig;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.MemberExceptionEnum;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.web.util.ResponseUtils;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.system.service.GlobalConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义登录成功处理类
 *
 * @author: tom
 * @date: 2019年7月25日 上午8:49:38
 */
@Component
public class MyAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    static Logger logger = LoggerFactory.getLogger(MyAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        /**Security认证通过后生成token*/
        String msg = MessageResolver.getMessage(SystemExceptionEnum.SUCCESSFUL.getCode(),
                SystemExceptionEnum.SUCCESSFUL.getDefaultMessage());
        String code = SystemExceptionEnum.SUCCESSFUL.getCode();
        Map<String, Object> loginResult = new HashMap<>();
        try {
            Object identityObj = request.getAttribute(RequestLoginUser.LOGIN_IDENTITY);
            if (identityObj != null) {
                CoreUser user = userService.findByUsername((String) identityObj);
                GlobalConfig config = globalConfigService.get();
                Integer configLoginErrorCount = config.getLoginErrorCount();
                boolean validCaptcha = false;
                if (user != null) {
                    String captcha = RequestUtils.getParam(request, RequestLoginUser.LOGIN_CAPTCHA);
                    /**
                     * 登录错误次数超过限定次数，验证码校验或者前端用户输入了验证码则验证码必须要校验
                     */
                    validCaptcha = user.getLoginErrorCount() >= configLoginErrorCount;
                    if (validCaptcha) {
                        String captchaSessionid = request.getParameter(KaptchaConfig.PARAM_SESSION_ID);
                        String ip = RequestUtils.getRemoteAddr(request);
                        loginResult.put(AuthConstant.NEXT_NEED_CAPTCHA, true);
                        boolean captchaSucc = true;
                        /** 需要验证码的时候如果验证码为空则提示需要验证码，但是code编码 是200 方便第一次加载验证码时候 */
                        if (StringUtils.isBlank(captcha)
                                || !captchaService.validCaptcha(request, response, captcha, captchaSessionid)) {
                            captchaSucc = false;
                        }
                        if (!captchaSucc) {
                            userService.userLogin(user.getUsername(), ip, false, config.getProcessingMode());
                            msg = MessageResolver.getMessage(
                                    SystemExceptionEnum.CAPTCHA_ERROR.getCode(),
                                    SystemExceptionEnum.CAPTCHA_ERROR.getDefaultMessage());
                            code = SystemExceptionEnum.CAPTCHA_ERROR.getCode();
                            //loginService.logout(null, request, response);
                            user = null;
                        }
                    }
                }
                if (user != null) {
                    /** 会员功能是否关闭，关闭则不可登录 */
                    if (!config.getMemberOpen()) {
                        msg = MessageResolver.getMessage(
                                SystemExceptionEnum.MEMBER_CLOSE.getCode(),
                                SystemExceptionEnum.MEMBER_CLOSE.getDefaultMessage());
                        code = SystemExceptionEnum.MEMBER_CLOSE.getCode();
                        loginService.logout(null, request, response);
                    }
                    /**非审核通过用户不可登录*/
                    else if (!user.getChecked()) {
                        msg = MessageResolver.getMessage(
                                MemberExceptionEnum.MEMBER_NEED_CHECKED.getCode(),
                                MemberExceptionEnum.MEMBER_NEED_CHECKED.getDefaultMessage());
                        code = MemberExceptionEnum.MEMBER_NEED_CHECKED.getCode();
                        loginService.logout(null, request, response);
                    } else if (!user.getEnabled()) {
                        /** 是否禁用的账户 */
                        msg = MessageResolver.getMessage(
                                SystemExceptionEnum.ACCOUNT_LOCKED.getCode(),
                                SystemExceptionEnum.ACCOUNT_LOCKED.getDefaultMessage());
                        code = SystemExceptionEnum.ACCOUNT_LOCKED.getCode();
                        loginService.logout(null, request, response);
                    } else {
                        loginResult = loginService.login(RequestLoginTarget.member,
                                (String) identityObj, null);
                    }
                    /**是管理员追加账户安全验证*/
                    if(user.getAdmin()&&config.getConfigAttr().getSecurityOpen()){
                        LoginDetail loginDetail  = loginService.getLoginDetail(RequestLoginTarget.member,user.getUsername());
                        String ip = RequestUtils.getRemoteAddr(request);
                        /** 管理员登录验证 是否要求强制修改密码、账户安全应用锁定账户 */
                        /***是否长期未登录*/
                        boolean longTimeNoLogin = false;
                        Date lastOpenAccountSecurityTime = config.getConfigAttr().getLastOpenAccountSecurityTime();
                        Date now = Calendar.getInstance().getTime();
                        if(config.getConfigAttr().getSecurityOpen()&&lastOpenAccountSecurityTime!=null&&user.getLastLoginTime()!=null){
                            /**最后登录时间超过限制天数，则认为是长时间未登录，若最后修改时间在这限制天数内则认定为不是/
                             */
                            Date lastCompareTime = now;
                            if(user.getLastLoginTime().before(DateUtil.offsetDay(lastCompareTime,-config.getConfigAttr().getAccountLockAutoDay()))){
                                longTimeNoLogin = true;
                            }
                            if(longTimeNoLogin&&lastOpenAccountSecurityTime.after(DateUtil.offsetDay(now,-config.getConfigAttr().getAccountLockAutoDay()))){
                                longTimeNoLogin = false;
                            }
                        }
                        /**长时间未登录自动退出返回被锁定*/
                        if(longTimeNoLogin){
                            loginService.loginAfter(loginDetail, ip, false, null, LoginFailProcessMode.lock);
                            user.setEnabled(false);
                            userService.update(user);
                            msg = MessageResolver.getMessage(
                                    SystemExceptionEnum.ACCOUNT_LOCKED.getCode(),
                                    SystemExceptionEnum.ACCOUNT_LOCKED.getDefaultMessage());
                            code = SystemExceptionEnum.ACCOUNT_LOCKED.getCode();
                            loginService.logout(null, request, response);
                            /** 从新登录从新拉取用户权限 */
                            user.clearPermCache();
                        }
                        /** 是否强制要求修改密码 */
                        if (user.getNeedUpdatePassword()) {
                            /**标注需要用户需要重置密码*/
                            user.setIsResetPassword(false);
                            userService.update(user);
                            /**通知前端需要重置密码*/
                            response.setHeader(AuthConstant.NEED_CHANGE_PDW,"true");
                        }
                    }
                    /**记录前台登录信息*/
                    String ip = RequestUtils.getRemoteAddr(request);
                    LoginDetail loginDetail = loginService.getLoginDetail(RequestLoginTarget.member, user.getUsername());
                    loginService.loginAfter(loginDetail, ip, true, null, null);
                }
            }
        } catch (GlobalException e) {
            logger.error(e.getMessage());
        }
        CmsSite site;
        String redirectUrl = "";
        try {
            site = siteService.getCurrSite(request, response);
            redirectUrl = site.getMemberRedirectUrl();
        } catch (GlobalException e) {
            logger.error(e.getMessage());
        }
        ResponseInfo responseInfo = new ResponseInfo(code, msg, request, redirectUrl, loginResult);
        ResponseUtils.renderJson(response, JSON.toJSONString(responseInfo));
    }

    @Autowired
    private LoginService loginService;
    @Autowired
    private CoreUserService userService;
    @Autowired
    private CmsSiteService siteService;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private CaptchaService captchaService;
}
