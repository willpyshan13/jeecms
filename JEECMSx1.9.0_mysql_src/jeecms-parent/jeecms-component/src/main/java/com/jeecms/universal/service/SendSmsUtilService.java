/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.universal.service;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.system.domain.dto.SendValidateCodeDTO;

/**
 * 发送消息util的service接口
 * @author: chenming
 * @date: 2020/6/23 16:48
 */
public interface SendSmsUtilService {

    /**
	 * 验证码有效性验证 0表示通过,小于0表示失败,大于0返回的是当前发送验证码次数,具体看 #ValidateCodeConstants
	 * 存在更改短信平台但是短信次数没刷新问题,但是前台感觉不到
	 * @Title: validateCode
	 * @param attrName 服务端属性名称
	 * @param validateCode 待验证的验证码
	 * @return: int
	 */
	int validateCode(String attrName, String validateCode);

	/**
	 * 验证码有效性验证 0表示通过,小于0表示失败,大于0返回的是当前发送验证码次数,具体看 #ValidateCodeConstants
	 * 存在更改短信平台但是短信次数没刷新问题,但是前台感觉不到
	 * @Title: validateCode
	 * @param attrName 服务端属性名称
	 * @param validateCode 待验证的验证码
	 * @param email 是否注册邮箱
	 * @return: int
	 */
	int validateCodeSpec(String attrName, String validateCode, boolean email);

    /**
	 * 验证码有效性验证,不验证是否超出发送次数 0表示通过,小于0表示失败,大于0返回的是当前发送验证码次数,具体看
	 * #ValidateCodeConstants 存在更改短信平台但是短信次数没刷新问题,但是前台感觉不到
	 * @Title: notValidateSendCountCode
	 * @param attrName 服务端属性名称
	 * @return: int
	 */
    int notValidateSendCountCode(String attrName);

    /**
	 * 发送手机短信消息
	 * @Title: sendPhoneMsg
	 * @param bean 发送codeDto
	 * @throws GlobalException 程序异常
	 * @return: ResponseInfo
	 */
	ResponseInfo sendPhoneMsg(SendValidateCodeDTO bean) throws GlobalException;

	/**
	 * 发送邮箱消息
	 * @Title: sendEmailMsg
	 * @param bean 发送codeDto
	 * @throws GlobalException 程序异常
	 * @return: ResponseInfo
	 */
	ResponseInfo sendEmailMsg(SendValidateCodeDTO bean) throws GlobalException;
}
