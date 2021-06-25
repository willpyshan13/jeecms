package com.jeecms.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 版本
 *
 * @author: tom
 * @date: 2019年1月10日 下午5:24:15
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Component
public class Version {

	public static String version;

	public static final  String MAJOR_NAME = "JEECMS";

	@Value("${product.version}")
	public void setVersion(String version) {
		Version.version = version;
	}

	public static String getVersionNumber() {
		return Version.version;
	}

	public static String getVersion() {
		return Version.MAJOR_NAME;
	}
}
