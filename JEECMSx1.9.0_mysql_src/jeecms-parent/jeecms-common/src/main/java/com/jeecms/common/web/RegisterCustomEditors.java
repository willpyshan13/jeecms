package com.jeecms.common.web;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;

import java.util.Date;

/**
 * 注册自定义转换器
 * @author: tom
 * @date: 2018年7月20日 下午8:03:12
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class RegisterCustomEditors {

	private RegisterCustomEditors() {
	}

	/**
	 * 注册时间、字符串属性编辑器
	 * @Title: registerCustomEditors  
	 * @param webBindingInitializer    ConfigurableWebBindingInitializer
	 * @return: void
	 */
	public static void registerCustomEditors(ConfigurableWebBindingInitializer webBindingInitializer) {
		DateTypeEditor dateEditor = new DateTypeEditor();
		PropertyEditorRegistrar dateEditorRegistrar = registry -> registry.registerCustomEditor(Date.class, dateEditor);
		PropertyEditorRegistrar stringEditorRegistrar = registry -> registry.registerCustomEditor(String.class, new StringEmptyEditor());
		PropertyEditorRegistrar[] editorRegistrars = new PropertyEditorRegistrar[] { dateEditorRegistrar,
			stringEditorRegistrar };
		webBindingInitializer.setPropertyEditorRegistrars(editorRegistrars);
	}
}
