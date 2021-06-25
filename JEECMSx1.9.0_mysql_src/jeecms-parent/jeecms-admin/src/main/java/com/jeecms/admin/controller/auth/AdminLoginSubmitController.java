package com.jeecms.admin.controller.auth;

import com.jeecms.auth.base.LoginSubmitController;
import com.jeecms.auth.domain.LoginDetail;
import com.jeecms.auth.domain.vo.SortMenuVO;
import com.jeecms.auth.dto.RequestLoginUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.annotation.ContentSecurity;
import com.jeecms.common.annotation.ContentSecurityAttribute;
import com.jeecms.common.base.domain.RequestLoginTarget;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.AccountCredentialExceptionInfo;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.element.service.ElementConfigService;
import com.jeecms.sso.constants.SsoContants;
import com.jeecms.sso.service.CmsSsoService;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.service.GlobalConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

import static com.jeecms.common.exception.error.UserErrorCodeEnum.VALIDATE_CODE_UNTHROUGH;
import static com.jeecms.system.domain.dto.ValidateCodeConstants.CODE_SECOND_LEVEL_IDENTITY_ELEMENT_VALID_PHONE;
import static com.jeecms.system.domain.dto.ValidateCodeConstants.STATUS_PASS;

/**
 * 登录controller
 *
 * @author: tom
 * @date: 2018年3月3日 下午3:13:10
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@RestController
public class AdminLoginSubmitController extends LoginSubmitController {


    @SuppressWarnings("unchecked")
    @RequestMapping(value = WebConstants.LOGIN_URL, method = RequestMethod.POST)
    @ContentSecurity
    @MoreSerializeField({@SerializeField(clazz = SortMenuVO.class, excludes = {"parentId"})})
    @Override
    public ResponseInfo login(HttpServletRequest request, HttpServletResponse response,
                              @ContentSecurityAttribute("requestLoginUser") @Valid RequestLoginUser requestLoginUser)
            throws GlobalException {
        requestLoginUser.setTarget(RequestLoginTarget.admin);
        //验证双因子
        validElement(requestLoginUser.getIdentity(), requestLoginUser.getCodeMessage());
        ResponseInfo info = super.login(request, response, requestLoginUser);
        if (SystemExceptionEnum.SUCCESSFUL.getCode().equals(info.getCode().toString())) {
            Map<String, Object> data = (Map<String, Object>) info.getData();
            /**
             * 管理返回权限
             */
            data.put(userAuth, coreUserService.routingTree(requestLoginUser.getIdentity(),request));
            return new ResponseInfo(SystemExceptionEnum.SUCCESSFUL.getCode(),
                    SystemExceptionEnum.SUCCESSFUL.getDefaultMessage(), data);
        } else {
            return info;
        }
    }

    /**验证双因子**/
    private void validElement(String username, String code) throws GlobalException {
        GlobalConfigAttr configAttr = globalConfigService.get().getConfigAttr();
        //验证双因子
        if (configAttr.getElementOpen()) {
            if (StringUtils.isBlank(RequestLoginUser.LOGIN_CODE_MESSAGE)) {
                throw  new GlobalException(new SystemExceptionInfo(VALIDATE_CODE_UNTHROUGH.getDefaultMessage(),
                        VALIDATE_CODE_UNTHROUGH.getCode(), false));
            }
            LoginDetail loginDetail = coreUserService.findByUsername(username);
            if (loginDetail == null) {
                throw new GlobalException(new AccountCredentialExceptionInfo());
            }
            String sessionKeyOld = WebConstants.KCAPTCHA_PREFIX
                    + CODE_SECOND_LEVEL_IDENTITY_ELEMENT_VALID_PHONE + loginDetail.getTelephone();
            int status1 = elementConfigService.validateCode(sessionKeyOld, code);
            if (STATUS_PASS > status1) {
                throw  new GlobalException(new SystemExceptionInfo(VALIDATE_CODE_UNTHROUGH.getDefaultMessage(),
                        VALIDATE_CODE_UNTHROUGH.getCode(), false));
            }
        }
    }

    /**
     * 登出
     *
     * @param request  请求
     * @param response 响应
     * @throws GlobalException 异常
     * @Title: logout
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseInfo logout(HttpServletRequest request,
                               HttpServletResponse response, @RequestBody Map<String, Object> map) throws GlobalException {
        //清除SSO登陆token
        String authToken = map.get(SsoContants.SSO_AUTHTOKEN) != null
                ? map.get(SsoContants.SSO_AUTHTOKEN).toString() : "";
        if (StringUtils.isNotBlank(authToken)) {
            cmsSsoService.logout(authToken);
        }
        String token = map.get("token") != null
                ? map.get("token").toString() : "";
        return super.logout(token, request, response);
    }

    @Value("${token.header}")
    private String tokenHeader;
    @Autowired
    private CmsSsoService cmsSsoService;
    @Autowired
    private ElementConfigService elementConfigService;
    @Autowired
    private CoreUserService coreUserService;
    @Autowired
    private GlobalConfigService globalConfigService;
}
