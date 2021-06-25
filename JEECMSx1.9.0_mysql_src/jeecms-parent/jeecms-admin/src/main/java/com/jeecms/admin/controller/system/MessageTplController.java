/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.OtherErrorCodeEnum;
import com.jeecms.common.exception.error.RPCErrorCodeEnum;
import com.jeecms.common.web.util.HttpClientUtil;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.content.constants.ContentReviewConstant;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.dto.MessageTplDetailsStatusDto;
import com.jeecms.system.domain.dto.SynchChildSiteTplDto;
import com.jeecms.system.domain.vo.SynchPlatformTplVo;
import com.jeecms.system.service.MessageTplDetailsService;
import com.jeecms.universal.service.HttpRequestPlatformUtilService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.system.domain.MessageTpl;
import com.jeecms.system.domain.MessageTplDetails;
import com.jeecms.system.service.MessageTplService;
import com.jeecms.util.SystemContextUtils;

/**
 * 消息模板设置
 *
 * @version 1.0
 * @author: ljw
 * @date: 2019年8月15日 上午9:57:12
 */
@RestController
@RequestMapping("/messageTpl")
public class MessageTplController extends BaseController<MessageTpl, Integer> {

    /**
     * 分页查询
     *
     * @param request  request请求
     * @param pageable 分页对象
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    @GetMapping(value = "/page")
    @MoreSerializeField({
            @SerializeField(clazz = MessageTpl.class, includes = {"id", "mesTitle", "mesCode",
                    "remark", "tplType","tplName","openLetter","openMail","openPhone","detailsMap"}),
            @SerializeField(clazz = MessageTplDetails.class, includes = {"tplId","tplName",
                    "mesTitle", "mesContent", "extendedField"})
    })
    public ResponseInfo page(HttpServletRequest request,
                             @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable)
            throws GlobalException {
        Map<String, String[]> params = new HashMap<String, String[]>(2);
        //区分模板类型
        params.put("EQ_tplType_Integer",
                new String[]{request.getParameter("tplType")});
        if (SystemContextUtils.getSiteId(request) != null) {
            params.put("EQ_siteId_Integer",
                    new String[]{SystemContextUtils.getSiteId(request).toString()});
        }
        return new ResponseInfo(service.getPage(params, pageable, false));
    }

    /**
     * 查询单个消息模板
     *
     * @param id 消息模板id
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    @GetMapping(value = "/{id:[0-9]+}")
    @MoreSerializeField({
            @SerializeField(clazz = MessageTpl.class, includes = {"id", "mesTitle", "mesCode",
                    "remark", "siteId", "tplType", "details"}),
            @SerializeField(clazz = MessageTplDetails.class, includes = {"id", "mesTplId",
                    "isOpen", "mesType", "tplId", "mesTitle", "mesContent", "extendedField",
                    "tplName"})})
    @Override
    public ResponseInfo get(@PathVariable("id") Integer id) throws GlobalException {
        return super.get(id);
    }

    /**
     * 新增消息模板
     *
     * @param request request请求
     * @param tpl     消息模板对象
     * @param result  参数校验对象
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    @PostMapping
    public ResponseInfo save(HttpServletRequest request,
                             @RequestBody @Valid MessageTpl tpl, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        if (CollectionUtils.isEmpty(tpl.getDetails())) {
            return new ResponseInfo(SystemExceptionEnum.INCOMPLETE_PARAM,"模板详情集合不允许为空");
        }
        Integer siteId = SystemContextUtils.getSiteId(request);
        if (messageTplService.findByMesCode(tpl.getMesCode(),siteId) != null) {
            return new ResponseInfo(SettingErrorCodeEnum.MSG_CODE_EXIST.getCode(),
                    SettingErrorCodeEnum.MSG_CODE_EXIST.getDefaultMessage());
        } else {
            tpl.setSiteId(siteId);
            messageTplService.saveMessageTpl(tpl,siteId);
        }
        return new ResponseInfo();
    }

    /**
     * 修改消息模板
     *
     * @param tpl    消息模板
     * @param result 参数校验对象
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    @PutMapping
    @Override
    public ResponseInfo update(@RequestBody @Valid MessageTpl tpl, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        if (CollectionUtils.isEmpty(tpl.getDetails())) {
            return new ResponseInfo(SystemExceptionEnum.INCOMPLETE_PARAM,"模板详情集合不允许为空");
        }
        messageTplService.updateMessageTpl(tpl);
        return new ResponseInfo();

    }

    /**
     * 修改单个模板详情(邮件、短信、站内信)的状态
     *
     * @param dto    修改模板详情的状态dto
     * @param result 参数校验对象
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    @PutMapping(value = "/details/status")
    public ResponseInfo updateMessageTplDetailsStatus(@RequestBody @Valid MessageTplDetailsStatusDto dto, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        if (dto.getOpen()) {
            if (dto.getTplDetails() == null) {
                return new ResponseInfo(SystemExceptionEnum.INCOMPLETE_PARAM,"开启状态下详情对象一定是必填的");
            }
        }
        messageTplService.updateMessageTplDetailsStatus(dto);
        return new ResponseInfo();
    }

    /**
     * 查询单个模板详细信息
     *
     * @param id      模板id
     * @param mesType 消息类型(消息类型 1、站内信 2、邮件 3、手机)
     * @return ResponseInfo
     */
    @GetMapping(value = "/mesType/detail")
    @SerializeField(clazz = MessageTplDetails.class,includes = {"isOpen","mesType","tplId","tplName","mesTitle","mesContent","extendedField"})
    public ResponseInfo getMessageTplDetails(@RequestParam Integer id, @RequestParam Short mesType) {
        return new ResponseInfo(messageTplService.getMessageTplDetails(id, mesType));
    }

    /**
     * 比对mesCode唯一
     *
     * @param mesCode 模板唯一标识
     * @param id      模板id
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    @GetMapping(value = "/mesCode/unique")
    public ResponseInfo checkMesCode(@RequestParam String mesCode, Integer id) throws GlobalException {
        MessageTpl tpl = messageTplService.findByMesCode(mesCode,SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest()));
        if (tpl != null) {
            if (tpl.getId().equals(id)) {
                return new ResponseInfo(Boolean.TRUE);
            } else {
                return new ResponseInfo(Boolean.FALSE);
            }
        }
        return new ResponseInfo(Boolean.TRUE);
    }

    /**
     * 同步按钮是否存在
     *
     * @param request request请求对象
     * @return ResponseInfo
     */
    @GetMapping(value = "/synch/button")
    public ResponseInfo synchButton(HttpServletRequest request) {
        CmsSite site = SystemContextUtils.getSite(request);
        if (CollectionUtils.isEmpty(site.getChilds())) {
            return new ResponseInfo(false);
        }
        return new ResponseInfo(true);
    }

    @PostMapping(value = "/synch/child/site/tpl")
    public ResponseInfo synchChildSite(HttpServletRequest request,
                                       @RequestBody @Valid SynchChildSiteTplDto dto, BindingResult result)
            throws GlobalException {
        super.validateBindingResult(result);
        Integer siteId = SystemContextUtils.getSiteId(request);
        messageTplService.checkChildSite(dto.getChildIds(), siteId);
        dto.setSiteId(siteId);
        boolean platformSuccess = true;
        String mobile = null;
        try {
            mobile = requestPlatformUtilService.getUserParameter(false);
            if (StringUtils.isBlank(mobile)) {
                platformSuccess = false;
            }
        } catch (ParseException | IOException e) {
            platformSuccess = false;
        }
        List<SynchPlatformTplVo> vos = null;
        if (platformSuccess) {
            dto.setMobile(mobile);
            vos = this.synchTplHttp(dto);
        }
        messageTplService.synchronizeChildSiteTpl(vos,dto.getChildIds(),siteId,!CollectionUtils.isEmpty(vos));
        return new ResponseInfo();
    }


    private List<SynchPlatformTplVo> synchTplHttp(SynchChildSiteTplDto dto) throws GlobalException{
        try {
            // 访问云平台如果5分钟没有响应直接断开
            String responseStr = HttpClientUtil.timeLimitPostJson(WebConstants.DoMain.SYNCH_CHILD_SITE_TPL, dto,this.getHeader(),900000);
            if (JSONObject.isValidObject(responseStr)) {
                JSONObject json = JSONObject.parseObject(responseStr);
                if (WebConstants.RESPONSE_CODE_CORRECT == json.getInteger(WebConstants.RESPONSE_CODE_MARK)) {
                    return json.getJSONArray("data").toJavaList(SynchPlatformTplVo.class);
                }
            }
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        } catch (ParseException e) {
            return null;
        } catch (IOException e) {
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        }
    }

    @GetMapping(value = "/have/number")
    public ResponseInfo ownNumber(HttpServletRequest request) throws GlobalException {
        String appId = null;
        String phone = null;
        try {
            appId = requestPlatformUtilService.getUserParameter(true);
            phone = requestPlatformUtilService.getUserParameter(false);
        } catch (ParseException e) {
            throw new GlobalException(OtherErrorCodeEnum.HTTP_REQUEST_TIMEOUT);
        } catch (IOException e) {
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        }
        if (StringUtils.isBlank(phone)) {
            throw new GlobalException(OtherErrorCodeEnum.SERVICE_MARKET_NOT_AUTHORIZED);
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(WebConstants.DoMain.CHECK_HAVE_PHONE_NUM).append("?mobile=").append(phone)
                .append("&productAppId=").append(appId)
                .append("&siteId=").append(SystemContextUtils.getSiteId(request))
                .append("&type=").append(5);
        return new ResponseInfo(this.ownNumberHttp(buffer.toString()));
    }

    /**
     * 拥有数量进行http请求
     * @param url   请求url
     * @return Boolean  true->有余量、false->没有余量
     * @throws GlobalException  全局异常
     */
    private Boolean ownNumberHttp(String url) throws GlobalException {
        try {
            // 访问云平台如果5分钟没有响应直接断开
            String responseStr = HttpClientUtil.timeLimitGetJson(url, getHeader(), 50000);
            if (JSONObject.isValidObject(responseStr)) {
                JSONObject json = JSONObject.parseObject(responseStr);
                if (WebConstants.RESPONSE_CODE_CORRECT == json.getInteger(WebConstants.RESPONSE_CODE_MARK)) {
                    return json.getBoolean("data");
                }
            }
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        } catch (ParseException e) {
            throw new GlobalException(OtherErrorCodeEnum.HTTP_REQUEST_TIMEOUT);
        } catch (IOException e) {
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        }
    }

    /**
     * 获取header头
     *
     * @return Map<String, String>
     * @throws GlobalException 全局异常
     */
    private Map<String, String> getHeader() throws GlobalException,ParseException,IOException {
        HEADER_MAP.put(ContentReviewConstant.SEND_REQUEST_HEADER, requestPlatformUtilService.getUserParameter(true));
        return HEADER_MAP;
    }

    /**
     * 请求header头map
     */
    private static Map<String, String> HEADER_MAP = new HashMap<String, String>();

    /**
     * 当前短信模板有关联的
     * @param request   request请求
     * @param tplId     短信模板id
     * @return  ResponseInfo
     */
    @GetMapping(value = "/have/related")
    public ResponseInfo haveRelated(HttpServletRequest request,@RequestParam String tplId) {
        return new ResponseInfo(messageTplDetailsService.haveTplId(tplId,SystemContextUtils.getSiteId(request)));
    }

    /**
     * 手机模板集合
     * @param request   request请求
     * @return ResponseInfo
     * @throws GlobalException  全局异常
     */
    @GetMapping(value = "/phone/tpl/list")
    public ResponseInfo getPhoneTplList(HttpServletRequest request) throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        String url = WebConstants.DoMain.GET_PHONE_TPL_LIST.concat("?siteId=").concat(siteId.toString());
        return new ResponseInfo(this.getPhoneTplHttp(url));
    }

    private JSONArray getPhoneTplHttp(String url) throws GlobalException{
        try {
            if (StringUtils.isBlank(requestPlatformUtilService.getUserParameter(false))) {
                throw new GlobalException(OtherErrorCodeEnum.SERVICE_MARKET_NOT_AUTHORIZED);
            }
            // 访问云平台如果5分钟没有响应直接断开
            String responseStr = HttpClientUtil.timeLimitGetJson(url, getHeader(), 50000);
            if (JSONObject.isValidObject(responseStr)) {
                JSONObject json = JSONObject.parseObject(responseStr);
                if (WebConstants.RESPONSE_CODE_CORRECT == json.getInteger(WebConstants.RESPONSE_CODE_MARK)) {
                    return json.getJSONArray("data");
                }
            }
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        } catch (ParseException e) {
            throw new GlobalException(OtherErrorCodeEnum.HTTP_REQUEST_TIMEOUT);
        } catch (IOException e) {
            throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
        }
    }

    @Autowired
    private MessageTplService messageTplService;
    @Autowired
    private HttpRequestPlatformUtilService requestPlatformUtilService;
    @Autowired
    private MessageTplDetailsService messageTplDetailsService;
}
