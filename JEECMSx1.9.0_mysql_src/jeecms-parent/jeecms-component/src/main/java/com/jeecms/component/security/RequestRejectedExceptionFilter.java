package com.jeecms.component.security;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

/**
 * RequestRejectedExceptionFilter 
 * @author: tom
 * @date: 2020/12/17 15:42   
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRejectedExceptionFilter extends GenericFilterBean {
    static Logger log = LoggerFactory.getLogger(RequestRejectedExceptionFilter.class);
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            RequestRejectedException requestRejectedException=(RequestRejectedException) servletRequest.getAttribute("isNormalized");
            if(Objects.nonNull(requestRejectedException)) {
                throw requestRejectedException;
            }else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } catch (RequestRejectedException requestRejectedException) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
//            log
//                    .error(
//                            "request_rejected: remote={}, user_agent={}, request_url={}",
//                            httpServletRequest.getRemoteHost(),
//                            httpServletRequest.getHeader(HttpHeaders.USER_AGENT),
//                            httpServletRequest.getRequestURL(),
//                            requestRejectedException
//                    );
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
