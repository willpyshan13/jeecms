package com.jeecms.common.configuration;

import com.jeecms.common.jpa.BaseJpaRepositoryFactoryBean;
import com.jeecms.common.security.Sha1CredentialsDigest;
import com.jeecms.common.util.PropertiesUtil;
import com.jeecms.common.web.filter.HeaderCorsFilter;
import com.jeecms.common.web.filter.XssFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.io.IOException;
import java.util.Properties;

/**
 * 公用组件扫描包
 * 
 * @Description: 含entity dao service
 * @author: wangqq
 * @date: 2018年7月24日 下午5:04:07
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Configuration
@EnableJpaAuditing
@EnableCaching
@EnableAspectJAutoProxy
@EntityScan(basePackages = { "com.jeecms.common.wechat.domain", "com.jeecms.**.domain",
		"com.jeecms.collect.data.model" })
@EnableJpaRepositories(basePackages = { "com.jeecms.common.wechat.dao",
		"com.jeecms.**.dao" }, entityManagerFactoryRef = "entityManagerFactory", repositoryFactoryBeanClass = BaseJpaRepositoryFactoryBean.class)
@ComponentScan({"com.jeecms.config.commonpay","com.jeecms.common", "com.jeecms.component", "com.jeecms.**.component", "com.jeecms.**.service",
		"com.jeecms.*.listener", "com.jeecms.rabbitmq.*", "com.jeecms.threadmsg.*", "com.jeecms.system.*", "com.jeecms.**.interceptor"})
public class ContextConfig {

	/**
	 * open-in-view
	 * 
	 * @Title: openEntityManagerInViewFilterRegistrationBean
	 * @Description: open-in-view
	 * @param: @return
	 * @return: FilterRegistrationBean
	 */
	@Bean
	public FilterRegistrationBean<OpenEntityManagerInViewFilter> openEntityManagerInViewFilterRegistrationBean() {
		FilterRegistrationBean<OpenEntityManagerInViewFilter> filterRegistration;
		filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(new OpenEntityManagerInViewFilter());
		filterRegistration.setEnabled(true);
		filterRegistration.addUrlPatterns("/*");
		filterRegistration.setDispatcherTypes(DispatcherType.REQUEST);
		filterRegistration.setOrder(10);
		return filterRegistration;
	}

	/**
	 * 注册xss Filter
	 * 
	 * @Title: filterRegistrationBean
	 * @return: FilterRegistrationBean Filter
	 */
	@Bean
	public FilterRegistrationBean<Filter> filterRegistrationBean() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<Filter>();
		registration.setFilter(new XssFilter());
		registration.addUrlPatterns("/*");
		registration.setEnabled(true);
		registration.setOrder(2);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<Filter> headerCrosFilterRegistrationBean() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
		registration.setFilter(getHeadCorsFilter());
		registration.addUrlPatterns("/*");
		registration.setEnabled(true);
		registration.setOrder(-1);
		return registration;
	}

	@Bean
	public HeaderCorsFilter getHeadCorsFilter(){
		return  new HeaderCorsFilter(tokenHeader, redirectHeader,siteIdHeader);
	}

	@Bean("properties")
	public Properties properties() throws IOException {
		Properties properties = PropertiesUtil.loadSystemProperties();
		return properties;
	}

	/**
	 * 属性工具bean
	 * 
	 * @Title: propertyUtils
	 * @throws IOException
	 *             IOException
	 * @return: PropertiesUtil
	 */
	@Bean("propertyUtils")
	public PropertiesUtil propertyUtils() throws IOException {
		PropertiesUtil propertiesUtil = new PropertiesUtil();
		propertiesUtil.setProperties(properties());
		return propertiesUtil;
	}

	@Bean("credentialsDigest")
	public Sha1CredentialsDigest credentialsDigest() {
		return new Sha1CredentialsDigest();
	}

	
	/**
	 * 注册 SessionRegistry bean 用于在登录时将UserDetails对象设置绑定当前session
	 * 然后在AuthenticationTokenFilter 取得当前的UserDetails的sessionId是否相同
	 * sessionId不相同则自动退出以实现不允许账户同时登录功能
	 * 
	 * @Title: sessionRegistry
	 * @return: SessionRegistry
	 */
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	/**
	 * 获取 EntityManager ，用于TreeIntercptor获取
	 * 
	 * @Title: entityManager
	 * @return: EntityManager EntityManager
	 */
	@Bean
	public EntityManager entityManager() {
		return entityManager;
	}

	@Bean
	public freemarker.template.Configuration configuration() {
		return freeMarkerConfigurer.getConfiguration();
	}


	@Bean
	public ErrorPageFilter errorPageFilter() {
		return new ErrorPageFilter();
	}

	/***
	 * 禁用spring ErrorPageFilter 容易导致被人攻击导致系统宕机
	 * @param filter
	 * @return
	 */
	@Bean
	public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(filter);
		filterRegistrationBean.setEnabled(false);
		return filterRegistrationBean;
	}

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;

	@Bean(name = "multipartResolver")
	public MultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("UTF-8");
		//resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
		resolver.setResolveLazily(true);
		resolver.setMaxInMemorySize(409600000);
		//上传文件大小 200M 200*1024*1024 default -1
		//resolver.setMaxUploadSize(200 * 1024 * 1024);
		return resolver;
	}

	@PersistenceContext
	protected EntityManager entityManager;
	@Value("${token.header}")
	private String tokenHeader;
	@Value("${redirect.header}")
	private String redirectHeader;
	@Value("${siteId.header}")
	private String siteIdHeader;
}
