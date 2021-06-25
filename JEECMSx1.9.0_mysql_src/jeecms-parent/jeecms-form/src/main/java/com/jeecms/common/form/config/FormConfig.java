package com.jeecms.common.form.config;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


/**
 * 表单配置bean
 * @author: tom
 * @date: 2020/3/9 17:28
 */
@Configuration
@PropertySource({ "classpath:config/spring.form.jpa.properties" })
public class FormConfig {
}
