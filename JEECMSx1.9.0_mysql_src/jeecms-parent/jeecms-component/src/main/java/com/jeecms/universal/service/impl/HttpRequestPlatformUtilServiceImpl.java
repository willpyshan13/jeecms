/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.universal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.RPCErrorCodeEnum;
import com.jeecms.common.util.ChastityUtil;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.util.HttpClientUtil;
import com.jeecms.content.constants.ContentReviewConstant;
import com.jeecms.sso.dto.response.SyncResponseBaseVo;
import com.jeecms.universal.service.HttpRequestPlatformUtilService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * request请求云平台util的service实现类
 * @author: chenming
 * @date: 2020/6/15 14:29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class HttpRequestPlatformUtilServiceImpl implements HttpRequestPlatformUtilService {

    @Override
    public String getUserParameter(boolean isAppId) throws GlobalException,IOException,ParseException {
        String appId = null;
        // 判断缓存中是否存在appId
        if (cacheProvider.exist(ContentReviewConstant.CONTENT_REVIEW_CACHE, ContentReviewConstant.REVIEW_APPID)) {
            appId = cacheProvider
                    .getCache(ContentReviewConstant.CONTENT_REVIEW_CACHE, ContentReviewConstant.REVIEW_APPID)
                    .toString();
        }
        // 如果appId不存在，则从util中取appId，并将其放入缓存中
        if (StringUtils.isBlank(appId)) {
            appId = chastityUtil.getId();
            if (StringUtils.isNotBlank(appId)) {
                cacheProvider.setCache(ContentReviewConstant.CONTENT_REVIEW_CACHE, ContentReviewConstant.REVIEW_APPID,
                        appId);
            } else {
                throw new GlobalException(RPCErrorCodeEnum.THIRD_PARTY_CALL_ERROR);
            }
        }
        if (isAppId) {
            return appId;
        }
        String phone = null;
        // 判断缓存中是否存在phone
        if (cacheProvider.exist(ContentReviewConstant.CONTENT_REVIEW_CACHE, ContentReviewConstant.REVIEW_PHONE)) {
            phone = cacheProvider
                    .getCache(ContentReviewConstant.CONTENT_REVIEW_CACHE, ContentReviewConstant.REVIEW_PHONE)
                    .toString();
        }
        // 如果phone不存在则从云平台中取phone中，并将其放入到缓存中
        if (StringUtils.isBlank(phone)) {
            String url = WebConstants.DoMain.IF_AUTH_SERVER_URL.concat("?").concat(ContentReviewConstant.GET_PHONE_REQUEST_KEY).concat("=").concat(appId);
            String responseStr = HttpClientUtil.timeLimitGetJson(url, null, 50000);
            if (JSONObject.isValidObject(responseStr)) {
                JSONObject jsonObject = JSONObject.parseObject(responseStr);
                if (SyncResponseBaseVo.SUCCESS_CODE.equals(jsonObject.getInteger(WebConstants.RESPONSE_CODE_MARK))) {
                    JSONObject json = jsonObject.getJSONObject(WebConstants.RESPONSE_DATA_MARK);
                    if (json != null) {
                        phone = json.getString(ContentReviewConstant.GET_PHONE_OBTAIN_KEY);
                        if (StringUtils.isNotBlank(phone)) {
                            cacheProvider.setCache(ContentReviewConstant.CONTENT_REVIEW_CACHE,
                                    ContentReviewConstant.REVIEW_PHONE, phone);
                        }
                        return phone;
                    }
                    return null;
                }
            }
        }
        return phone;
    }

    @Autowired
    private CacheProvider cacheProvider;
    @Autowired
    private ChastityUtil chastityUtil;
}
