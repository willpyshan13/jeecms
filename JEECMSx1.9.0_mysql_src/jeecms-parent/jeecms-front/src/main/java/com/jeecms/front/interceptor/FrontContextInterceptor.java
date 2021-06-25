package com.jeecms.front.interceptor;

import com.alibaba.fastjson.JSON;
import com.jeecms.auth.constants.AuthConstant;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.auth.service.LoginService;
import com.jeecms.common.constants.SysConstants;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.local.UserThreadLocal;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.UserAgentUtils;
import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.CookieUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.web.util.ResponseUtils;
import com.jeecms.common.web.util.UrlUtil;
import com.jeecms.sso.service.CmsSsoService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.FrontUtils;
import com.jeecms.util.MyResponseUtils;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceResolver;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.UUID;

/**
 * 上下文信息拦截器 包括登录信息、权限信息、站点信息
 * 
 * @author: tom
 * @date: 2019年3月11日 下午1:53:12
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class FrontContextInterceptor extends HandlerInterceptorAdapter {
	public static final String SITE_COOKIE = "_site_id_cookie";
	/** 身份标识 过期时间一年 */
	public static final Integer EXPIRE_JIDENTITY_COOKIE = 365 * 24 * 60 * 60;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		SystemContextUtils.setMobile(false);
		SystemContextUtils.setTablet(false);
		/** 优先以cookie，方便预览url跳转 */
		Cookie deviceCookie = CookieUtils.getCookie(request, WebConstants.COOKIE_PREVIEW_DEVICE);
		if (deviceCookie != null) {
			String deviceVal = deviceCookie.getValue();
			if (StringUtils.isBlank(deviceVal) || deviceVal.equals(WebConstants.PREVIEW_DEVICE_PC)) {
				SystemContextUtils.setPc(true);
				SystemContextUtils.setMobile(false);
				SystemContextUtils.setTablet(false);
			} else if (StringUtils.isNotBlank(deviceVal) && deviceVal.equals(WebConstants.PREVIEW_DEVICE_TABLET)) {
				SystemContextUtils.setTablet(true);
				SystemContextUtils.setMobile(false);
				SystemContextUtils.setPc(false);
			} else {
				SystemContextUtils.setMobile(true);
				SystemContextUtils.setPc(false);
				SystemContextUtils.setTablet(false);
			}
		} else {
			Device device = deviceResolver.resolveDevice(request);
			if (device.isNormal()) {
				SystemContextUtils.setPc(true);
			}
			/** 是手机客户端访问 */
			if (device.isMobile()) {
				SystemContextUtils.setMobile(true);
			}
			/** 如果是平板访问 */
			if (device.isTablet()) {
				SystemContextUtils.setTablet(true);
			}
		}
		/**
		 * 是否微信H5
		 */
		SystemContextUtils.setWxH5(UserAgentUtils.isWxBrower(request));
		CoreUser user = SystemContextUtils.getCoreUser();
		/** 设置最后操作时间 */
		if (user != null) {
			/**是管理员应用账户安全策略*/
			/** 是否强制要求修改密码 且用户未修改密码*/
			if (user.getAdmin()) {
				if(user.getNeedUpdatePassword()){
					response.setHeader(AuthConstant.NEED_CHANGE_PDW,"true");
				}
			}
			sessionProvider.setAttribute(request, AuthConstant.LAST_OPERATE_TIME, Calendar.getInstance().getTime());
		}
		CmsSite site = SystemContextUtils.getSite(request);
		String header = RequestUtils.getHeaderOrParam(request, redirectHeader);
		/** 站点关闭且非资源等开放请求URL */
		if (!site.getIsOpen() && !UrlUtil.isOpenRequest(request)) {
			if (StringUtils.isNoneBlank(header) && "false".equals(header.toLowerCase())) {
				String msg = MessageResolver.getMessage(SystemExceptionEnum.SITE_CLOSE.getCode(),
						SystemExceptionEnum.SITE_CLOSE.getDefaultMessage());
				String code = SystemExceptionEnum.SITE_CLOSE.getCode();
				ResponseInfo responseInfo = new ResponseInfo(code, msg, request, request.getRequestURI(), null);
				ResponseUtils.renderJson(response, JSON.toJSONString(responseInfo));
			} else {
				ResponseUtils.redirectToSiteClose(request, response);
			}
			return false;
		}
		/** 登录后才能访问站点 */
		/** 需要登录访问站点 用户为空 且当前不是登录url或资源URL 则重定向登录页或者返回需要登录提示JSON */
		if (site.getLoginSuccessVisitSite() && user == null && !UrlUtil.isOpenRequest(request)) {
			if (StringUtils.isNoneBlank(header) && "false".equals(header.toLowerCase())) {
				String msg = MessageResolver.getMessage(SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getCode(),
						SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getDefaultMessage());
				String code = SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getCode();
				ResponseInfo responseInfo = new ResponseInfo(code, msg, request, request.getRequestURI(), null);
				ResponseUtils.renderJson(response, JSON.toJSONString(responseInfo));
			} else {
				MyResponseUtils.redirectToLogin(request, response);
			}
			return false;
		}
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			request.setAttribute(SysConstants.HANDLER_METHOD, handlerMethod.getMethod());
		}
		String sessionId =  sessionProvider.getSessionId(request);
		/**非IpV6，防止用户填入ip v6的ip地址*/
		if(site.getBaseDomain().indexOf(":")==-1){
            CookieUtils.addCookie(request, response, WebConstants.JSESSION_COOKIE, sessionId, 36000, site.getBaseDomain(), null);
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// 如果当前请求对应的控制器函数返回非视图（即实际的控制器添加@RestController 或函数添加了@ResponseBody 注解），
		// 此处的ModelAndView 为空，下方设置以供页面使用的公用参数就不进行设置
		if (modelAndView != null) {
			FrontUtils.frontData(request, modelAndView.getModel());
		}
        /** 不存在cookie身份标识 写入浏览器cookie标识 */
        if (!CookieUtils.existCookie(request, WebConstants.IDENTITY_COOKIE)) {
            CmsSite site = SystemContextUtils.getSite(request);
            CookieUtils.addCookie(request, response, WebConstants.IDENTITY_COOKIE, UUID.randomUUID().toString(),
                    EXPIRE_JIDENTITY_COOKIE, site.getDomain(), null);
        }
		super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		super.afterCompletion(request, response, handler, ex);
		UserThreadLocal.removeUser();
		SystemContextUtils.resetMobile();
		SystemContextUtils.resetPc();
		SystemContextUtils.resetTablet();
		SystemContextUtils.resetWxH5();
		SystemContextUtils.resetCoreUser();
	}

	@Autowired
	private DeviceResolver deviceResolver;
	@Autowired
	private CoreUserService userService;
	@Autowired
	private SessionProvider sessionProvider;
	@Value("${redirect.header}")
	private String redirectHeader;
	@Value("${token.header}")
	private String tokenHeader;
	@Autowired
	private CmsSsoService cmsSsoService;
	@Autowired
	private GlobalConfigService globalConfigService;
	@Autowired
	private LoginService loginService;

}