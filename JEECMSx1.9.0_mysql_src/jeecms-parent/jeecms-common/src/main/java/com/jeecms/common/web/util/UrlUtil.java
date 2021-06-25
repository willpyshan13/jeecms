package com.jeecms.common.web.util;

import com.jeecms.common.constants.TplConstants;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.image.ImageUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * URL工具类
 * 
 * @author: tom
 * @date: 2018年9月10日 下午2:40:38
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class UrlUtil {

	/**
	 * 是否资源请求url
	 * 
	 * @Title: isResourceUrl
	 * @param request
	 *            HttpServletRequest
	 * @return: boolean true 是
	 */
	public static boolean isResourceRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (StringUtils.isNoneBlank(ctx)) {
			uri = uri.substring(ctx.length());
		}
		if (uri.startsWith(WebConstants.UPLOAD_PATH)) {
			return true;
		}
		if (uri.startsWith(WebConstants.SYS_RESOURCE_PATH)) {
			return true;
		}
		if (uri.startsWith(TplConstants.RES_PATH)) {
			return true;
		}
		String uri1 = ".js";
		String uri2 = ".css";
		if (uri.endsWith(uri1) || uri.endsWith(uri2) || ImageUtils.isValidImageUrl(uri) || uri.endsWith(".ico")) {
			return true;
		}
		return false;
	}

	/**
	 * 是否登录URl
	 * 
	 * @Title: isLoginUrl
	 * @param request
	 *            HttpServletRequest
	 * @return: boolean
	 */
	public static boolean isLoginRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (StringUtils.isNoneBlank(ctx)) {
			uri = uri.substring(ctx.length());
		}
		if (uri.startsWith(WebConstants.LOGIN_URL)) {
			return true;
		}
		/**手机端登录页*/
		if(uri.startsWith(WebConstants.MOBILE_LOGIN_URL_PREFIX)){
			return true;
		}
		/**加载系统图片*/
		if (uri.startsWith("/register/getImage")) {
			return true;
		}
		/**前台获取登录状态地址也不拦截，否则会出现重定向登录页多次的导致页面错乱*/
		if(uri.startsWith("/csi-loginStatus")){
			return true;
		}
		if (uri.equals(WebConstants.ADMIN_PREFIX + WebConstants.LOGIN_URL)) {
			return true;
		}
		if (uri.equals(WebConstants.LOGOUT_URL)) {
			return true;
		}
		if (uri.equals(WebConstants.ADMIN_PREFIX + WebConstants.LOGOUT_URL)) {
			return true;
		}
		if (uri.equals("/" + WebConstants.ADMIN_URL)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是错误url映射请求
	 * 
	 * @Title: isErrorRequest
	 * @param request
	 *            HttpServletRequest
	 * @return: boolean
	 */
	public static boolean isErrorRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (StringUtils.isNoneBlank(ctx)) {
			uri = uri.substring(ctx.length());
		}
		if (uri.startsWith(WebConstants.ERROR_URL)) {
			return true;
		}
		if (uri.startsWith(WebConstants.SITE_CLOSE)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是第三方授权映射请求(含预览URL、自动提取拼音摘要)
	 * 
	 * @Title: isThirdAuthRequest
	 * @param request
	 *            HttpServletRequest
	 * @return: boolean
	 */
	public static boolean isThirdAuthRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (StringUtils.isNoneBlank(ctx)) {
			uri = uri.substring(ctx.length());
		}
		if (uri.startsWith("/thirds") || uri.startsWith("/weibo") || uri.startsWith("/weChat")
				|| uri.startsWith("/" + WebConstants.PREVIEW_URL) || uri.startsWith("/language")
				|| uri.startsWith("/permCheck")) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是包含特殊字符的url
	 *
	 * @param request HttpServletRequest
	 * @Title: isErrorRequest
	 * @return: boolean
	 */
	public static boolean isContainerSpecialCharUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (StringUtils.isNoneBlank(ctx)) {
			uri = uri.substring(ctx.length());
		}
		if (uri.startsWith(WebConstants.TEMPLATE_URL)) {
			return true;
		}
		if (uri.startsWith(WebConstants.RESOURCE_URL)) {
			return true;
		}
		if (uri.startsWith(WebConstants.CONTENT_URL)) {
			return true;
		}
		return false;
	}


	/**
	 * 是否是开放的 request （登录、退出、资源、错误映射url、Options请求、第三方授权是开放的）
	 * 
	 * @Title: isOpenRequest
	 * @param request
	 *            HttpServletRequest
	 * @return: boolean
	 */
	public static boolean isOpenRequest(HttpServletRequest request) {
		if (WebConstants.OPTIONS.equals(request.getMethod())) {
			return true;
		}
		if (isResourceRequest(request)) {
			return true;
		}
		if (isErrorRequest(request)) {
			return true;
		}
		if (isLoginRequest(request)) {
			return true;
		}
		if (isThirdAuthRequest(request)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否资源uri
	 * 
	 * @Title: isResourceUri
	 * @param uri
	 *            uri
	 * @return: boolean true 是
	 */
	public static boolean isResourceUri(String uri) {
		if (uri.startsWith(WebConstants.UPLOAD_PATH)) {
			return true;
		}
		if (uri.startsWith(WebConstants.SYS_RESOURCE_PATH)) {
			return true;
		}
		if (uri.startsWith(TplConstants.RES_PATH)) {
			return true;
		}
		return false;
	}

	public static List<String> getAdminExcludeUrls() {
		return ADMIN_EXCLUDE_URLS;
	}

	public static List<String> getMemberExcludeUrls() {
		return MEMBER_EXCLUDE_URLS;
	}

	public static String getResourceurl() {
		return RESOURCE_URL;
	}

	public static String getSysresurl() {
		return SYS_RES_URL;
	}

	private static final List<String> ADMIN_EXCLUDE_URLS = Arrays.asList(
	        /**排除首页、登录页调用的接口*/
			WebConstants.ADMIN_PREFIX + WebConstants.LOGIN_URL, WebConstants.ADMIN_PREFIX + WebConstants.LOGOUT_URL,
			WebConstants.ADMIN_PREFIX + WebConstants.GLOBAL_GET_URL,
			WebConstants.ADMIN_PREFIX + "/dictTypeManager/list", WebConstants.ADMIN_PREFIX + "/coreUser/routingList", 
			WebConstants.ADMIN_PREFIX + WebConstants.JEESSO_SYNC,
			WebConstants.ADMIN_PREFIX + WebConstants.JEESSO_STATUS,
			WebConstants.ADMIN_PREFIX + WebConstants.JEESSO_GETINFO,
			WebConstants.ADMIN_PREFIX + WebConstants.JEESSO_UPDATE,
			WebConstants.ADMIN_PREFIX + WebConstants.JEESSO_DELETE,
			WebConstants.ADMIN_PREFIX + WebConstants.GET_CONFIG_IS_SM_ENCRYPT,
			WebConstants.ADMIN_PREFIX + WebConstants.OPEN_ELEMENT,
			WebConstants.ADMIN_PREFIX + WebConstants.ELEMENT_VALID,
			WebConstants.ADMIN_PREFIX + WebConstants.PRODUCT_APP_INFO,
			WebConstants.ADMIN_PREFIX + WebConstants.CLASSIFIED_PROTECTION,
			WebConstants.ADMIN_PREFIX + "/collect/consume",
			WebConstants.ADMIN_PREFIX +"/config/global",
			WebConstants.ADMIN_PREFIX +"/sites/ownsite",
			WebConstants.ADMIN_PREFIX +"/menus/getMenus"
	);

	private static final List<String> MEMBER_EXCLUDE_URLS = Arrays.asList(WebConstants.LOGOUT_URL,
			WebConstants.LOGIN_URL, WebConstants.ERROR_URL);

	private static final String RESOURCE_URL = WebConstants.UPLOAD_PATH + "**";
	private static final String SYS_RES_URL = TplConstants.RES_PATH + "**";
}
