/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.common.web.filter;

/**   
* TODO
* @author: tom
* @date:   2019年8月15日 下午5:29:45     
*/
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.constants.SysConstants;
import com.jeecms.common.util.MyPropertiesUtil;
import com.jeecms.common.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域过滤器
 * 
 * @author: tom
 * @date: 2019年8月15日 下午5:47:04
 */
public class HeaderCorsFilter implements Filter {
	protected static final Logger log = LoggerFactory.getLogger(HeaderCorsFilter.class);
	/**
	 * 请求执行开始时间
	 */
	public static final String START_TIME = "_start_time";
	static{
		try {
			serverMode = PropertiesUtil.loadSystemProperties().getProperty("spring.profiles.active");
		}catch (IOException e){
			log.error(e.getMessage());
		}
	}

	@Override
	public void init(FilterConfig filterConfig)  {
	}

	/**
	 * 过滤
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		long time = System.currentTimeMillis();
		req.setAttribute(START_TIME, time);
		HttpServletResponse resp = (HttpServletResponse) response;
		//resp.setHeader("Pragma", "No-cache");
		//resp.setHeader("Cache-Control", "no-cache");
		//resp.setDateHeader("Expires", 0);
		/**安全软件认为*是安全漏洞此处调整*/
		String originHeader=(req).getHeader("Origin");
		/**限定来自自身服务端或者来自云服务的域名，不可删除originHeader.contains("jeecms.com")判断，否则云服务功能使用有问题*/
		if (originHeader!=null&&originHeader.contains("jeecms.com")||PropertiesUtil.getExcludeOrigins().contains(originHeader)) {
			resp.setHeader("Access-Control-Allow-Origin", originHeader);
		}if (originHeader!=null&&(originHeader.contains(request.getServerName()+":"+request.getServerPort()))) {
			resp.setHeader("Access-Control-Allow-Origin", "");
		}
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
		resp.setHeader("Access-Control-Max-Age", "3600");
		resp.setHeader("Access-Control-Allow-Headers",
				SysConstants.DEFAULT_ALLOW_HEADERS + tokenHeader + "," + redirectHeader + "," + siteIdHeader);
		resp.setHeader("Access-Control-Allow-Credentials", "false");
		resp.setHeader("Access-Control-Expose-Headers", "true");
		//resp.setHeader("X-Content-Type-Options", "nosniff");
		chain.doFilter(request, resp);
		time = System.currentTimeMillis() - time;
		log.debug("process in {} ms: {}", time, req.getRequestURI());
	}

	/**
	 * 
	 */
	public void destroy() {
	}

	public HeaderCorsFilter(String tokenHeader, String redirectHeader, String siteIdHeader) {
		super();
		this.tokenHeader = tokenHeader;
		this.redirectHeader = redirectHeader;
		this.siteIdHeader = siteIdHeader;
	}

	private String tokenHeader;
	private String redirectHeader;
	private String siteIdHeader;
	private static String serverMode;
}