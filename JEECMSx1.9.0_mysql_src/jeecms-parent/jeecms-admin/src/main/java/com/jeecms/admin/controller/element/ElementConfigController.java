/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.admin.controller.element;

import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 双因素控制器
 * @author: ljw
 * @date:   2019年4月13日 下午3:54:44     
 */
@RestController
@RequestMapping("/config/element")
@Validated
public class ElementConfigController extends BaseController<GlobalConfig, Integer> {

	/**
	 * 登录是否开启双因子手机验证
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 * @since x1.4.0
	 */
	@GetMapping(value = "/status")
	public ResponseInfo status(String username) throws GlobalException {
		return new ResponseInfo(service.openElement(username));
	}

	/**
	 * 是否强制绑定手机验证
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 * @since x1.4.0
	 */
	@GetMapping(value = "/hasBind")
	public ResponseInfo hasBind() throws GlobalException {
		GlobalConfig config = service.get();
		boolean result = config.getConfigAttr().getElementOpen()
				&& StringUtils.isBlank(SystemContextUtils.getCoreUser().getTelephone());
		return new ResponseInfo(result);
	}

	@Autowired
	private GlobalConfigService service;


}