package com.jeecms.element.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.ChastityUtil;
import com.jeecms.common.wechat.util.client.HttpUtil;
import com.jeecms.element.service.ElementConfigService;
import com.jeecms.sso.dto.response.SyncResponseBaseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.jeecms.constants.SiteHealthConstant.DOMAIN;

/**
 * 实现类
 * @author ljw
 */
@Service
@Transactional(rollbackFor = Exception.class)
@ConditionalOnMissingClass(value = "com.jeecms.domain.ElementDto")
public class ElementConfigServiceImpl implements ElementConfigService {

    /** 请求云平台该产品是否有余量 Get **/
    private static final String CHECK_SERVER_URL = DOMAIN + "/MODULE-APP/client/v1/balance/check";
    /** 请求云平台是否授权 Get **/
    private static final String IF_AUTH_SERVER_URL = DOMAIN + "/MODULE-APP/client/v1/userClient";

    @Autowired
    private ChastityUtil chastityUtil;

    @Override
    public int validateCode(String attrName, String validateCode) throws GlobalException {
        return 0;
    }

    @Override
    public boolean check(Integer siteId) {
        // 获取该系统AppId
        String appId = chastityUtil.getId();
        Map<String, String> params = new HashMap<>(10);
        params.put("productAppId", appId);
        //判断是否授权
        ResponseInfo response = HttpUtil.getJsonBean(IF_AUTH_SERVER_URL, params, ResponseInfo.class);
        if (response != null && SyncResponseBaseVo.SUCCESS_CODE.equals(response.getCode())) {
            String json = response.getData().toString();
            JSONObject object = JSONObject.parseObject(json);
            if (object.getBooleanValue("auth")) {
                //检测余量
                params.put("mobile", object.getString("mobile"));
                params.put("siteId", siteId != null ? siteId.toString() : "1");
                params.put("type", "5");
                ResponseInfo responses = HttpUtil.getJsonBean(CHECK_SERVER_URL, params, ResponseInfo.class);
                if (responses != null && SyncResponseBaseVo.SUCCESS_CODE.equals(responses.getCode())) {
                    return (boolean) responses.getData();
                }
            }
        }
        return false;
    }
}
