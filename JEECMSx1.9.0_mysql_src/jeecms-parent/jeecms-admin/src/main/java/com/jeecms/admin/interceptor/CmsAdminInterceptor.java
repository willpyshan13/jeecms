package com.jeecms.admin.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.constants.AuthConstant;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.auth.service.LoginService;
import com.jeecms.common.base.domain.RequestLoginTarget;
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.constants.SysConstants;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.local.UserThreadLocal;
import com.jeecms.common.manage.annotation.OperatingIntercept;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.CookieUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.web.util.ResponseUtils;
import com.jeecms.common.web.util.UrlUtil;
import com.jeecms.manage.service.OperatingInterceptService;
import com.jeecms.sso.constants.SsoContants;
import com.jeecms.sso.dto.response.SyncResponseUserVo;
import com.jeecms.sso.service.CmsSsoService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.domain.SysIptables;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.system.service.SysIptablesService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.UnknownHostException;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.jeecms.system.domain.SysIptables.*;
import static java.lang.String.*;

/**
 * 后台拦截器
 *
 * @author: tom
 * @date: 2018年3月9日 下午3:22:24
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Component
public class CmsAdminInterceptor implements HandlerInterceptor {
	static Logger logger = LoggerFactory.getLogger(CmsAdminInterceptor.class);

	@Value("${spring.profiles.active}")
	private String serverMode;
	String numRegular = "^\\d+$";

	/**
	 * 进入SpringMVC的Controller之前开始记录日志实体
	 *
	 * @param request  请求对象
	 * @param response 响应对象
	 * @param obj      Controller对象
	 * @return true 允许通过
	 * @throws Exception Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) 
			throws Exception {
        request.setAttribute("start",System.currentTimeMillis());
		/**管理后台此处固定认为是pc访问，这样默认访问的栏目、内容、首页的url就是pc的*/
		SystemContextUtils.setPc(true);
		SystemContextUtils.setMobile(false);
		SystemContextUtils.setTablet(false);
		// 设置请求开始时间
		request.setAttribute(WebConstants.LOGGER_SEND_TIME, System.currentTimeMillis());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CoreUser user = SystemContextUtils.getCoreUser();
		//防火墙校验
		if (!firewall(RequestUtils.getRemoteAddr(request), RequestUtils.getServerUrl(request))) {
			String msg = MessageResolver.getMessage(SystemExceptionEnum.FIREWALL_INTERCEPTION.getCode(),
					SystemExceptionEnum.FIREWALL_INTERCEPTION.getDefaultMessage());
			String code = SystemExceptionEnum.FIREWALL_INTERCEPTION.getCode();
			ResponseInfo responseInfo = new ResponseInfo(code, msg);
			ResponseUtils.renderJson(response, JSON.toJSONString(responseInfo));
			return false;
		}
        if (authentication != null && authentication.isAuthenticated()
				&& !WebConstants.ANONYMOUSUSER.equals(authentication.getPrincipal())) {
			Object detailObj = authentication.getDetails();
			UserDetails userDetails;
			/**
			 * WebAuthenticationDetails 下次登录认证对象放在了的auth principal属性中
			 * token传递的认证对象放在了 details属性中
			 */
			if (detailObj instanceof WebAuthenticationDetails) {
				detailObj = authentication.getPrincipal();
				userDetails = (UserDetails) detailObj;
			} else {
				userDetails = (UserDetails) authentication.getDetails();
			}
            // 如果是回放操作直接过滤api权限
            if (SystemContextUtils.replay()) {
                user = coreUserService.findById(Integer.valueOf(request.getHeader(WebConstants.REQUEST_USER)));
            } else {
                user = coreUserService.findByUsername(userDetails.getUsername());
            }
		}
		// 判断是否开启单点登录
		GlobalConfig config = globalConfigService.get();
		GlobalConfigAttr configAttr = config.getConfigAttr();
		if (configAttr.getSsoLoginAppOpen() && user == null) {
			/** 判断cookie是否存在SSO token,存在则 获取用户信息*/
			Cookie ssoCookie = CookieUtils.getCookie(request, SsoContants.CLIENT_AUTHTOKEN);
			if (ssoCookie != null && StringUtils.isNoneBlank(ssoCookie.getValue())) {
				// 拿到登录用户信息
				SyncResponseUserVo vo = cmsSsoService.userVaild(ssoCookie.getValue());
				if (vo != null) {
					Map<String, Object> map = loginService.login(RequestLoginTarget.admin,
							vo.getUsername(), null);
					user = coreUserService.findByUsername(vo.getUsername());
					//JsonFilterResponseBodyAdvice 续期token
					request.setAttribute(tokenHeader, map.get(tokenHeader));
				}
			}
		}

		if (!isExcludeUrl(request) && authentication != null) {
			// LOOK 修改用户权限后权限是否被清空
			Set<String> auths = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
			String uri = request.getRequestURI();
			String ctx = request.getContextPath();
			if (StringUtils.isNoneBlank(ctx)) {
				uri = uri.substring(ctx.length());
			}
			/** 应对详情http://a.com/admin/channel/111 接口拦截只匹配前缀，数字ID截掉后去检查 */
			int lastPathIndex = uri.lastIndexOf("/");
			String uriLastStr = uri.substring(lastPathIndex + 1);
			if (StringUtils.isNotBlank(uriLastStr) && StrUtils.isNumeric(uriLastStr)) {
				uri = uri.substring(0, lastPathIndex);
			}
			uri = request.getMethod().toUpperCase() + ":" + uri;
			// 开发、模式下不拦截api权限
			if (!ServerModeEnum.dev.toString().equals(serverMode)&&!SystemContextUtils.replay()) {
                // 如果是回放操作直接过滤api权限
				String msg = MessageResolver.getMessage(SystemExceptionEnum.HAS_NOT_PERMISSION.getCode(),
						SystemExceptionEnum.HAS_NOT_PERMISSION.getDefaultMessage());
				String code = SystemExceptionEnum.HAS_NOT_PERMISSION.getCode();
				if (user == null) {
					msg = MessageResolver.getMessage(SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getCode(),
							SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getDefaultMessage());
					code = SystemExceptionEnum.ACCOUNT_NOT_LOGIN.getCode();
					ResponseInfo responseInfo = new ResponseInfo(code, msg);
					ResponseUtils.renderJson( response, JSON.toJSONString(responseInfo));
					return false;
				}
				/**验证菜单权限 权限集合没有包含URI或者是*所有权限*/
				boolean hasNotPerm = !auths.isEmpty() && !auths.contains(uri) && !"*".equals(auths.iterator().next())
						|| auths.isEmpty();
				if (hasNotPerm) {
					ResponseInfo responseInfo = new ResponseInfo(code, msg);
					ResponseUtils.renderJson( response, JSON.toJSONString(responseInfo));
					return false;
				}
				/**验证站群权限*/
				if(user!=null){
					CmsSite site = SystemContextUtils.getSite(RequestUtils.getHttpServletRequest());
					Set<CmsSite> sites = user.getOwnerSites();
					/**站群权限不包含当前站点*/
					if(!sites.contains(site)){
						msg = MessageResolver.getMessage(SystemExceptionEnum.HAS_NOT_PERMISSION.getCode(),
								SystemExceptionEnum.HAS_NOT_PERMISSION.getDefaultMessage());
						code = SystemExceptionEnum.HAS_NOT_PERMISSION.getCode();
						ResponseInfo responseInfo = new ResponseInfo(code, msg);
						ResponseUtils.renderJson(response, JSON.toJSONString(responseInfo));
						return false;
					}
				}
			}
		}
		/** 设置最后操作时间 */
		if (user != null) {
			/**用户登录后被禁用，强制退出*/
			if(!user.getEnabled()){
				String msg = MessageResolver.getMessage(SystemExceptionEnum.ACCOUNT_LOCKED.getCode(),
						SystemExceptionEnum.ACCOUNT_LOCKED.getDefaultMessage());
				String code = SystemExceptionEnum.ACCOUNT_LOCKED.getCode();
				ResponseInfo responseInfo = new ResponseInfo(code, msg);
				ResponseUtils.renderJson( response, JSON.toJSONString(responseInfo));
				loginService.forceLogout(request,response);
				return false;
			}else{
				/** 是否强制要求修改密码 且用户未修改密码*/
				if (user.getNeedUpdatePassword()) {
					response.setHeader(AuthConstant.NEED_CHANGE_PDW,"true");
				}
			}
			sessionProvider.setAttribute(request, AuthConstant.LAST_OPERATE_TIME, Calendar.getInstance().getTime());
		}
        if (obj instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) obj;
            Annotation annotation = handlerMethod.getMethodAnnotation(OperatingIntercept.class);
			if (annotation!=null&&service != null) {
				boolean intercept = service.isIntercept(request);
				if (!intercept) {
					return true;
				}
				if (!SystemContextUtils.replay()) {
					String a = readAsChars(request);
					service.saveManageLog(request,a);
					throw new GlobalException(SystemExceptionEnum.OPERATION_HAS_BEEN_SUBMITTED_TO_TRINITY_MANAGEMENT);
				}
			}
            request.setAttribute(SysConstants.HANDLER_METHOD, handlerMethod.getMethod());
        }
		return true;
	}


    @Autowired(required = false)
    private OperatingInterceptService service;

    public static String readAsChars(HttpServletRequest request)
    {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try
        {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null)
            {
                sb.append(str);
            }
            br.close();
        }
        catch (IOException e)
        {
        	logger.error(e.getMessage());
        }
        finally
        {
            if (null != br)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage());
                }
            }
        }
        return sb.toString();
    }

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
		SystemContextUtils.resetCoreUser();
		UserThreadLocal.removeUser();
		Long start  = (Long)request.getAttribute("start");
		long end = System.currentTimeMillis();
		logger.debug(format("%scomplete time ->%d", request.getRequestURI(), end - start));
	}

	private boolean isExcludeUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (StringUtils.isNoneBlank(ctx)) {
			uri = uri.substring(ctx.length());
		}
		for (String str : UrlUtil.getAdminExcludeUrls()) {
			if (uri.startsWith(str)) {
				return true;
			}
		}
		return false;
	}


	private boolean firewall(String addressIp, String serverUrl) {
		SysIptables iptables = iptablesService.getSysIptables();
		//是否开启(0-否 1-是)
		Boolean isEnable = iptables.getIsEnable();
		if (isEnable!=null&&isEnable) {
			Calendar calendar = Calendar.getInstance();
			//当前小时数
			int hour = MyDateUtils.getHour(calendar);
			int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			if (week <= 0) {
				week = 7;
			}

			//非内网ip不需要判断白名单和黑名单
			if (RequestUtils.ipIsInner(addressIp) && !"127.0.0.1".equals(addressIp)
					&& !"0:0:0:0:0:0:0:1".equals(addressIp) && !"localhost".equals(addressIp)) {

				//allowLoginWeek 允许后台登录星期，多个是用逗号分隔，如：1,2,3,4,5,6,7
				String allowLoginWeek = iptables.getAllowLoginWeek();
				boolean isWeek = allowLoginWeek.contains(valueOf(week));
				if (!isWeek) {
					logger.info("今天不允许访问");
					return false;
				}
				//允许后台登录时间点,多个用逗号分隔,如：0,1,2,3
				String allowLoginHours = iptables.getAllowLoginHours();
				String[] hours = allowLoginHours.split(WebConstants.ARRAY_SPT);
				boolean isHour = false;
				for (String s : hours) {
					if (s.equals(valueOf(hour))) {
						isHour = true;
						break;
					}
				}
				if (!isHour) {
					logger.info("当前时段不允许访问");
					return false;
				}

				//限制访问域名
				String limitDomain = iptables.getLimitDomain();
				if (StringUtils.isNotBlank(limitDomain)) {
					String[] domains = limitDomain.split(WebConstants.ARRAY_SPT);
					List<String> list = Arrays.asList(domains);
					if (!list.contains(addressIp)) {
						logger.info("该域名无法访问后台");
						return false;
					}
				}

				//内网模式限制号段集json格式
				String innetworkIpJson = iptables.getInNetworkIpJson();
				JSONArray jsonArray = parseArray(innetworkIpJson);
				//是否在ip段内，默认为true
				boolean isInnetwork = true;
				for (Object o : jsonArray) {
					JSONObject jsonObject = (JSONObject) o;
					String startIp = jsonObject.getString("startIp");
					String endIp = jsonObject.getString("endIp");
					try {
						//存在于ip段内则跳出，不再判断
						isInnetwork = iptablesService.checkIp(startIp, endIp, addressIp);
						if (isInnetwork) {
							break;
						}
					} catch (UnknownHostException e) {
						logger.error("ip地址错误异常");
					}
				}
				//限制内网ip模式（1-不限制 2-设置白名单 3-设置黑名单
				Integer type = iptables.getLimitInNetworkModel();
				if (NETWORK_NOT_LIMITED.equals(type)) {
					//1-不限制
				} else if (NETWORK_WHITELIST.equals(type)) {
					//2-白名单
					if (!isInnetwork) {
						logger.info("不在白名单，无法访问");
						return false;
					}
				} else if (NETWORK_BLACKLIST.equals(type)&&isInnetwork) {
					//3-黑名单
					logger.info("在黑名单内，无法访问");
					return false;
				}
			}
		}
		return true;
	}

	@Autowired
	private SysIptablesService iptablesService;
	@Value("${token.header}")
	private String tokenHeader;
	@Autowired
	private CoreUserService coreUserService;
	@Autowired
	private LoginService loginService;
	@Autowired
	private SessionProvider sessionProvider;
	@Autowired
	private CmsSsoService cmsSsoService;
	@Autowired
	private GlobalConfigService globalConfigService;
}
