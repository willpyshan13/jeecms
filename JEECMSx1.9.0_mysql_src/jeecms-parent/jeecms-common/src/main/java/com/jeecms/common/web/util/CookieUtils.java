package com.jeecms.common.web.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie 辅助类
 * 
 * @author: tom
 * @date: 2018年12月27日 上午9:43:49
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class CookieUtils {
	/**
	 * 每页条数cookie名称
	 */
	public static final String COOKIE_PAGE_SIZE = "_cookie_page_size";
	/**
	 * 默认每页条数
	 */
	public static final int DEFAULT_SIZE = 20;
	/**
	 * 最大每页条数
	 */
	public static final int MAX_SIZE = 200;

	public static final int INTEGER_MAX_LEN = 11;


	/**
	 * cookie中xsrftoken key
	 */
	public static final String XSRF_TOKEN_COOKIE = "XSRF-TOKEN";

	/**
	 * xsrf token key
	 */
	public static final String XSRF_TOKEN_HEADER = "X-XSRF-TOKEN";

	/**
	 * 获得cookie的每页条数 使用_cookie_page_size作为cookie name
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return default:20 max:200
	 */
	public static int getPageSize(HttpServletRequest request) {
		Cookie cookie = getCookie(request, COOKIE_PAGE_SIZE);
		int count = 0;
		if (cookie != null) {
			if (NumberUtils.isDigits(cookie.getValue())) {
				if (cookie.getValue().length() < INTEGER_MAX_LEN) {
					count = Integer.parseInt(cookie.getValue());
				}
			}
		}
		if (count <= 0) {
			count = DEFAULT_SIZE;
		} else if (count > MAX_SIZE) {
			count = MAX_SIZE;
		}
		return count;
	}

	/**
	 * cookie 获取xsrf token
	 * @param request
	 * @return
	 */
	public static String getXsrfToken(HttpServletRequest request) {
		Cookie cookie = getCookie(request, XSRF_TOKEN_COOKIE);
		if (cookie != null) {
				return cookie.getValue();
		}
		return "";
	}

	/**
	 * 获得cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param name
	 *            cookie name
	 * @return if exist return cookie, else return null.
	 */
	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie c : cookies) {
				if (c.getName().equals(name)) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * 是否存在cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param name
	 *            cookie name
	 * @return if exist return cookie, else return null.
	 */
	public static boolean existCookie(HttpServletRequest request, String name) {
		Cookie cookie = getCookie(request, name);
		if (cookie != null) {
			return true;
		}
		return false;
	}

	/**
	 * 根据部署路径，将cookie保存在根目录。
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            名称
	 * @param value
	 *            值
	 * @param expiry
	 *            过期时间 秒为单位
	 * @param domain
	 *            域名
	 * @return
	 */
	public static Cookie addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
                                   Integer expiry, String domain, String path) {
		Cookie cookie = new Cookie(name, value);
		if (expiry != null) {
			cookie.setMaxAge(expiry);
		}
		if (StringUtils.isNotBlank(domain)) {
			cookie.setDomain(domain);
		}
		String ctx = request.getContextPath();
		cookie.setPath(StringUtils.isBlank(ctx) ? "/" : ctx);
        if (StringUtils.isNotBlank(path)) {
            if (cookie.getPath().endsWith("/")) {
                cookie.setPath(cookie.getPath() + path);
            } else {
                cookie.setPath(cookie.getPath() + "/" + path);
            }
        }
		cookie.setHttpOnly(true);
		/**不可加上这个属性，内容、栏目预览功能需要借助cookie实现跨平台切换查看读取cookie，加上后读取不到cookie*/
		//cookie.setSecure(true);
		response.addCookie(cookie);
		return cookie;
	}

	/**
	 * 将cookie保存在指定目录。
	 * 
	 * @Title: addCookie
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            名称
	 * @param value
	 *            值
	 * @param expiry
	 *            过期时间 秒为单位
	 * @param domain
	 *            域名
	 * @return: Cookie
	 */
	public static Cookie addCookie(HttpServletResponse response, String name, String value,
								   Integer expiry, String domain) {
		Cookie cookie = new Cookie(name, value);
		if (expiry != null) {
			cookie.setMaxAge(expiry);
		}
		if (StringUtils.isNotBlank(domain)) {
			cookie.setDomain(domain);
		}
		cookie.setHttpOnly(true);
		/**不可加上这个属性，内容、栏目预览功能需要借助cookie实现跨平台切换查看读取cookie，加上后读取不到cookie*/
		//cookie.setSecure(true);
		response.addCookie(cookie);
		return cookie;
	}

	/**
	 * 取消cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            名称
	 * @param domain
	 *            域名
	 */
	public static void cancleCookie(HttpServletRequest request, HttpServletResponse response, String name,
			String domain) {
		Cookie cookie = new Cookie(name, "");
		cookie.setMaxAge(0);
		String ctx = request.getContextPath();
		cookie.setPath(StringUtils.isBlank(ctx) ? "/" : ctx);
		if (StringUtils.isNotBlank(domain)) {
			cookie.setDomain(domain);
		}
		response.addCookie(cookie);
	}
}
