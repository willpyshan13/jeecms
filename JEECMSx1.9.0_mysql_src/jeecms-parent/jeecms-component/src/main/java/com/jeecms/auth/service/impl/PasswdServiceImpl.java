package com.jeecms.auth.service.impl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.constants.ContentSecurityConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.DesUtil;
import com.jeecms.system.service.GlobalConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jeecms.auth.dto.RequestLoginUser;
import com.jeecms.auth.service.LoginService;
import com.jeecms.common.security.PasswdService;
import com.jeecms.common.security.CredentialsDigest;
import com.jeecms.common.util.Encodes;
import com.jeecms.common.web.util.RequestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 密码service
 *
 * @author: tom
 * @date: 2018年3月20日 下午2:14:17
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Service
@ConditionalOnMissingClass(value="com.jeecms.common.security.SM3CredentialsDigest")
public class PasswdServiceImpl implements PasswdService, PasswordEncoder {
    Logger logger = LoggerFactory.getLogger(PasswdServiceImpl.class);

    @Override
    public String entryptPassword(byte[] saltBytes, String rawPass) {
        String encPass = credentialsDigest.digest(rawPass, saltBytes);
        return encPass;
    }

    @Override
    public boolean isPasswordValid(String rawPass, String salt, String encPass) {
        byte[] saltBytes = Encodes.decodeHex(salt);
        String rawPassEnc = credentialsDigest.digest(rawPass, saltBytes);
        return rawPassEnc.equals(encPass);
    }

    @Override
    public void changeCredentialsDigest(boolean smCredentialsDigest, String password) throws GlobalException {

    }

    @Override
    public String encode(CharSequence rawPassword) {
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        String salt = "";
        if (request != null) {
            Object saltObj = request.getAttribute(RequestLoginUser.LOGIN_SALT);
            if (saltObj != null) {
                salt = (String) saltObj;
            }
        }
        byte[] saltBytes = Encodes.decodeHex(salt);
        return entryptPassword(saltBytes, rawPassword.toString());
    }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if(StringUtils.isBlank(rawPassword)||StringUtils.isBlank(encodedPassword)){
                        return false;
                }
                HttpServletRequest request = RequestUtils.getHttpServletRequest();
                /** 密码已加密传输，需要解密得到原明文密码 */
                rawPassword = loginService.getPStr(request);
                String salt = "";
                if (request != null) {
                        Object saltObj = request.getAttribute(RequestLoginUser.LOGIN_SALT);
                        if (saltObj != null) {
                                salt = (String) saltObj;
                        }
                }
                return isPasswordValid(rawPassword.toString(), salt, encodedPassword);
        }

    /**
     * 默认采用原来SHA1算法
     */
    @Resource(name = "credentialsDigest")
    private CredentialsDigest credentialsDigest;


    @Override
    @Transactional(readOnly = true)
    public  String decrypt(String encryptText){
        return DesUtil.decrypt(encryptText, ContentSecurityConstants.DES_KEY,
                ContentSecurityConstants.DES_IV);
    }

    @Override
    public String encrypt(String plainText) {
        return DesUtil.encrypt(plainText, ContentSecurityConstants.DES_KEY,
                ContentSecurityConstants.DES_IV);
    }

    @Autowired
    private LoginService loginService;

}
