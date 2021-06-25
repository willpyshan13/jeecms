package com.jeecms.component;

import com.jeecms.auth.service.LoginService;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.common.web.util.ResponseUtils;
import com.jeecms.util.SystemContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 解析 token 的过滤器 配置在 Spring Security 的配置类中 用于解析 token ，将用户所有的权限写入本次 Spring Security 的会话中
 * 
 * @Author tom
 */
public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

        static Logger logger = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                        throws IOException, ServletException {
                initServiceBean();
                // 将 ServletRequest 转换为 HttpServletRequest 才能拿到请求头中的 token
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                if (WebConstants.OPTIONS.equals(httpRequest.getMethod())) {
                        ResponseUtils.renderJson(httpResponse, "");
                        return;
                }
                /** 是否允许同一账号异地登录 ,不允许则 若是已存在登陆用户则踢出之前的登录用户 */
                loginService.tryValidRepeatSession(httpRequest, httpResponse);
                /**分离部署下，三员管理回放请求操作过快可能会取到之前请求放置的user对象，所以此如果是三员请求处强制执行初始化security会话*/
                if(SystemContextUtils.getCoreUser()==null||SystemContextUtils.replay()){
                        /** 尝试token登录 */
                        try {
                                loginService.tryTokenLogin(httpRequest, httpResponse);
                        } catch (GlobalException e) {
                                logger.error(e.getMessage());
                        }
                        /** 尝试form登录，实现下次登录 */
                        loginService.tryFormLogin(httpRequest, httpResponse);
                }
                chain.doFilter(request, response);
        }
       
        private void initServiceBean() {
                if (loginService == null) {
                        loginService = ApplicationContextProvider.getBean(LoginService.class);
                }
                if(session==null){
                        session = ApplicationContextProvider.getBean(SessionProvider.class);
                }
        }

        private LoginService loginService;
        private SessionProvider session;

}
