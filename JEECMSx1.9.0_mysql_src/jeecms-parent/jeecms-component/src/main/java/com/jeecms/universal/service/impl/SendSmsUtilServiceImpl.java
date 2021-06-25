/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.universal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.ExceptionInfo;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.message.MqConstants;
import com.jeecms.message.MqSendMessageService;
import com.jeecms.message.dto.CommonMqConstants;
import com.jeecms.system.domain.MessageTpl;
import com.jeecms.system.domain.MessageTplDetails;
import com.jeecms.system.domain.dto.SendValidateCodeDTO;
import com.jeecms.system.domain.dto.ValidateCodeDTO;
import com.jeecms.system.service.MessageTplService;
import com.jeecms.universal.service.SendSmsUtilService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import static com.jeecms.common.exception.error.SettingErrorCodeEnum.*;
import static com.jeecms.system.domain.dto.ValidateCodeConstants.*;

/**
 * 发送消息
 * @author: chenming
 * @date: 2020/6/23 16:48
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SendSmsUtilServiceImpl implements SendSmsUtilService {

	@Override
	public int validateCode(String attrName, String validateCode) {
		Serializable targetBean = cacheProvider.getCache(CacheConstants.SMS, attrName);
		int status = STATUS_NEW;
		String targetStr;
		ValidateCodeDTO codeDto;
		// 服务端不存在对应验证码,如果是前台验证码验证操作的话则代表非法,否则代表获取验证码.
		if (targetBean == null || StringUtils.isBlank(targetStr = targetBean.toString())) {
			return StringUtils.isNotBlank(validateCode) ? STATUS_ILLEGAL : status;
		}
		codeDto = JSONObject.parseObject(targetStr, ValidateCodeDTO.class);
		// 验证码需要验证时.
		if (StringUtils.isNotBlank(validateCode)) {
			if (codeDto.isExpire()) {
				status = STATUS_EXPIRED;
			} else if (!validateCode.equals(codeDto.getCode())) {
				status = STATUS_UNTHROUGH;
			} else {
				status = STATUS_PASS;
                cacheProvider.clearCache(CacheConstants.SMS, attrName);
			}
			return status;
		}
		return status;
	}

    @Override
    public int validateCodeSpec(String attrName, String validateCode, boolean email) {
        Serializable targetBean = cacheProvider.getCache(CacheConstants.SMS, attrName);
        int status = STATUS_NEW;
        String targetStr;
        ValidateCodeDTO codeDto;
        // 服务端不存在对应验证码,如果是前台验证码验证操作的话则代表非法,否则代表获取验证码.
        if (targetBean == null || StringUtils.isBlank(targetStr = targetBean.toString())) {
            return StringUtils.isNotBlank(validateCode) ? STATUS_ILLEGAL : status;
        }
        codeDto = JSONObject.parseObject(targetStr, ValidateCodeDTO.class);
        // 验证码需要验证时.
        if (StringUtils.isNotBlank(validateCode)) {
            if (codeDto.isExpire()) {
                status = STATUS_EXPIRED;
            } else if (!validateCode.equals(codeDto.getCode())) {
                status = STATUS_UNTHROUGH;
            } else {
                status = STATUS_PASS;
                //特殊处理修改邮箱注册
                if (!email) {
                    cacheProvider.clearCache(CacheConstants.SMS, attrName);
                }
            }
            return status;
        }
        return status;
    }

    @Override
    public int notValidateSendCountCode(String attrName) {
        Serializable targetBean = cacheProvider.getCache(CacheConstants.SMS, attrName);
        int status = STATUS_PASS;
        String targetStr;
        ValidateCodeDTO codeDto;
        // 获取验证码.
        if (targetBean == null || StringUtils.isBlank(targetStr = targetBean.toString())) {
            return status;
        }
        codeDto = JSONObject.parseObject(targetStr, ValidateCodeDTO.class);
        // 获取验证码时.
        if (!codeDto.isSendExpire()) {
            status = STATUS_UNEXPIRED;
        } else {
            cacheProvider.clearCache(CacheConstants.SMS, attrName);
        }
        return status;
    }

    @Override
    public ResponseInfo sendPhoneMsg(SendValidateCodeDTO bean) throws GlobalException {
        // 拼接服务端验证码key
        String attrName = WebConstants.KCAPTCHA_PREFIX + bean.getSecondLevelName() + bean.getTargetNumber();
        int status = this.notValidateSendCountCode(attrName);
        // 小于0则返回错误信息
        if (STATUS_PASS != status) {
            ExceptionInfo exceptionInfo = ValidateCodeDTO.exceptionAdapter(status);
            return new ResponseInfo(exceptionInfo.getCode(), exceptionInfo.getDefaultMessage());
        }
        // 创建验证码
        if (StringUtils.isBlank(bean.getValidateCode())) {
            bean.setValidateCode(StrUtils.getRandStr(DEFAULT_VALIDATE_CODE_LENGTH));
        }
        String toPhone = bean.getTargetNumber();
        //查询站点
        CoreUser user = coreUserService.findByPhone(toPhone);
        Integer siteId;
        if (user == null) {
            siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        } else {
            siteId = user.getSourceSiteId();
        }
        String msgCode = bean.getMessageTplCode();
        MessageTpl tpl = messageTplService.findByMesCode(msgCode,siteId);
        // 未配置消息模板
        if (tpl == null) {
            return new ResponseInfo(MESSAGE_TPL_UNCONFIGURED.getCode(),
                    MESSAGE_TPL_UNCONFIGURED.getDefaultMessage(), false);
        }
        Optional<MessageTplDetails> optional = tpl.getDetails().stream()
                .filter(messageTplDetail -> MessageTplDetails.MESTYPE_PHONE
                        .equals(messageTplDetail.getMesType())).findFirst();
        // 未配置短信模板
        if (!optional.isPresent()) {
            return new ResponseInfo(SMS_TPL_UNCONFIGURED.getCode(),
                    SMS_TPL_UNCONFIGURED.getDefaultMessage(), false);
        }
        MessageTplDetails details = optional.get();
        ValidateCodeDTO codeDto = new ValidateCodeDTO(bean.getValidateCode(), new Date(), status, toPhone);
        // 服务端存储验证码
        cacheProvider.setCache(CacheConstants.SMS, attrName, JSON.toJSONString(codeDto));

        // 发送消息
        JSONObject data = new JSONObject();
        data.put(CommonMqConstants.EXT_DATA_KEY_SMS, bean.getTreeMapParam());
        mqSendMessageService.sendValidateCodeMsg(tpl.getMesCode(), CommonMqConstants.MessageSceneEnum.VALIDATE_CODE,
                details.getMesTitle(), details.getMesContent(),
                toPhone, null, MqConstants.SEND_SMS, siteId, data);
        return new ResponseInfo();
    }

    @Override
	public ResponseInfo sendEmailMsg(SendValidateCodeDTO bean) throws GlobalException {
		// 拼接服务端验证码key
		String attrName = WebConstants.KCAPTCHA_PREFIX + bean.getSecondLevelName() + bean.getTargetNumber();
		int status = this.notValidateSendCountCode(attrName);
		// 小于pass则表示不通过
		if (STATUS_PASS != status) {
			ExceptionInfo exceptionInfo = ValidateCodeDTO.exceptionAdapter(status);
			return new ResponseInfo(exceptionInfo.getCode(), exceptionInfo.getDefaultMessage(), false);
		}
		// 创建验证码
		if (StringUtils.isBlank(bean.getValidateCode())) {
			bean.setValidateCode(StrUtils.getRandStr(DEFAULT_VALIDATE_CODE_LENGTH));
		}
        String toEmail = bean.getTargetNumber();
        //查询站点
        CoreUser user = coreUserService.findByEmailOrUsername(toEmail);
        Integer siteId;
        if (user != null && !user.getAdmin()) {
            siteId = user.getSourceSiteId();
        } else {
            siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        }
		String msgCode = bean.getMessageTplCode();
		MessageTpl tpl = messageTplService.findByMesCode(msgCode,siteId);
		if (tpl == null) {
			return new ResponseInfo(MESSAGE_TPL_UNCONFIGURED.getCode(),
					MESSAGE_TPL_UNCONFIGURED.getDefaultMessage(), false);
		}
		Optional<MessageTplDetails> optional = tpl.getDetails().stream()
				.filter(messageTplDetail -> MessageTplDetails.MESTYPE_MAIL
				.equals(messageTplDetail.getMesType())).findFirst();
		if (!optional.isPresent()) {
			return new ResponseInfo(EMAIL_TPL_UNCONFIGURED.getCode(),
					EMAIL_TPL_UNCONFIGURED.getDefaultMessage(), false);
		}
		MessageTplDetails details = optional.get();

		ValidateCodeDTO codeDto = new ValidateCodeDTO(bean.getValidateCode(), new Date(), status, toEmail);
		// 服务端存储验证码
		cacheProvider.setCache(CacheConstants.SMS, attrName, JSONObject.toJSONString(codeDto));

		// 发送消息
		JSONObject data = new JSONObject();
		data.put(CommonMqConstants.EXT_DATA_KEY_EMAIL, bean.getTreeMapParam());
		mqSendMessageService.sendValidateCodeMsg(tpl.getMesCode(), CommonMqConstants.MessageSceneEnum.VALIDATE_CODE,
				details.getMesTitle(), details.getMesContent(),
				null, toEmail, MqConstants.SEND_EMAIL, siteId, data);
		return new ResponseInfo();
	}


    @Autowired
    private CacheProvider cacheProvider;
    @Autowired
    private MessageTplService messageTplService;
    @Autowired
    private MqSendMessageService mqSendMessageService;
    @Autowired
    private CoreUserService coreUserService;
}
