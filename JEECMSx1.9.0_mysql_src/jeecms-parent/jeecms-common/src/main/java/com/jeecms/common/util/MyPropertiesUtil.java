package com.jeecms.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.io.IOException;
import java.util.*;

/**
 * Spring properties工具类
 *
 * @author tom
 *
 */
public class MyPropertiesUtil  implements BeanFactoryAware {

	private static final Logger logger = LoggerFactory.getLogger(MyPropertiesUtil.class);
	/**Content-Security-Policy(csp)属性名称*/
	private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
	/**Content-Security-Policy(csp)属性值*/
	private static String contentSecurityPolicyStr = "";

	static {
		try {
			Properties properties1 = PropertiesUtil.loadSystemProperties();
			String str = properties1.getProperty(CONTENT_SECURITY_POLICY);
			if(StringUtils.isNotBlank(str)){
				StringBuilder securityPolicyBuff = new StringBuilder();
				securityPolicyBuff.append("default-src 'self' blob:");
				securityPolicyBuff.append(" ").append(str);
				/**unsafe-inline font-src data:会被认为不安全，此处网页有使用没办法 unsafe-eval不开放这个编辑器也无法使用*/
				securityPolicyBuff.append(";script-src 'self' 'unsafe-inline' 'unsafe-eval' data:; style-src 'self' 'unsafe-inline';font-src 'self' data:");
				contentSecurityPolicyStr = securityPolicyBuff.toString();
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static String getContentSecurityPolicyStr(){
		return contentSecurityPolicyStr;
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	private BeanFactory beanFactory;
}
