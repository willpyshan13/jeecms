package com.jeecms.common.util.social;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 第三方账户工具类
 *
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/6/28 14:56
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class SocialUtil {



    private static final String WEIXIN_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String WEIXIN_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
    private static final String WEIXIN_OPEN_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/component/access_token?appid=%s&code=%s&grant_type=authorization_code&component_appid=%s&component_access_token=%s";

    private static final String QQ_ACCESS_TOKEN_URL = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&fmt=json";
    private static final String QQ_OPEN_ID_URL = "https://graph.qq.com/oauth2.0/me?access_token=%s&fmt=json";
    private static final String QQ_USER_INFO_URL = "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s";

    private static final String WEIBO_ACCESS_TOKEN_URL = "https://api.weibo.com/oauth2/access_token?client_id=%s&client_secret=%s&grant_type=authorization_code&redirect_uri=%s&code=%s";
    private static final String WEIBO_USER_IFNO_URL = "https://api.weibo.com/2/users/show.json?access_token=%s&uid=%s";

    private static Logger log = LoggerFactory.getLogger(SocialUtil.class);


    /**
     * 获取微信用户唯一id
     * 只保证获取唯一id, 用户的其他信息(如用户名,头像,性别等等)并不存在
     * 如需要获取详细的用户信息, 请使用{@link SocialUtil#getWeiXinUserInfo(String, String, String)}
     *
     * @param appid 网站应用appid
     * @param appSecret 网站应用appSecret
     * @param code
     * @author Zhu Kaixiao
     * @date 2020/6/28 15:19
     */
    public static SocialUserInfo getWeiXinUid(String appid, String appSecret, String code) {

        String accessTokenRet;
        try {
            String accessTokenUrl = String.format(WEIXIN_ACCESS_TOKEN_URL, appid, appSecret, code);
            accessTokenRet = HttpUtil.get(accessTokenUrl);
        } catch (Exception e) {
            log.error("获取微信用户token失败", e);
            throw new RuntimeException("获取微信用户token失败", e);
        }

        Map<String, Object> accessTokenMap;
        try {
            accessTokenMap = JSONObject.parseObject(accessTokenRet, HashMap.class);
        } catch (Exception e) {
            log.error("解析json失败", e);
            throw new RuntimeException("解析json失败", e);
        }

        // 微信接口调用报错
        if (accessTokenMap.containsKey("errcode")) {
            log.error("获取微信用户唯一id失败: errcode: [{}], errmsg: [{}]", accessTokenMap.get("errcode"), accessTokenMap.get("errmsg"));
            throw new RuntimeException(accessTokenMap.get("errmsg").toString());
        }


        accessTokenMap.put("appid", appid);
        SocialUserInfo socialUserInfo = parseMapToSocialUserInfo(SocialPlatform.WeiXin, accessTokenMap);

        return socialUserInfo;
    }

    /**
     * 微信开发平台代公众号获取用户信息
     *
     * @param appid                公众号appid
     * @param code                 授权码
     * @param componentAppId       开放平台appid
     * @param componentAccessToken 开放平台token
     * @author Zhu Kaixiao
     * @date 2020/8/28 15:05
     */
    public static SocialUserInfo getWeiXinUid(String appid, String code, String componentAppId, String componentAccessToken) {

        String accessTokenRet;
        try {
            String accessTokenUrl = String.format(WEIXIN_OPEN_ACCESS_TOKEN_URL, appid, code, componentAppId, componentAccessToken);
            accessTokenRet = HttpUtil.get(accessTokenUrl);
        } catch (Exception e) {
            log.error("获取微信用户token失败", e);
            throw new RuntimeException("获取微信用户token失败", e);
        }

        Map<String, Object> accessTokenMap;
        try {
            accessTokenMap = JSONObject.parseObject(accessTokenRet, HashMap.class);
        } catch (Exception e) {
            log.error("解析json失败", e);
            throw new RuntimeException("解析json失败", e);
        }

        // 微信接口调用报错
        if (accessTokenMap.containsKey("errcode")) {
            log.error("获取微信用户唯一id失败: errcode: [{}], errmsg: [{}]", accessTokenMap.get("errcode"), accessTokenMap.get("errmsg"));
            throw new RuntimeException(accessTokenMap.get("errmsg").toString());
        }


        accessTokenMap.put("appid", appid);
        SocialUserInfo socialUserInfo = parseMapToSocialUserInfo(SocialPlatform.WeiXin, accessTokenMap);

        return socialUserInfo;
    }


    public static SocialUserInfo getWeiXinUserInfo(String appid, String appSecret, String code) {
        SocialUserInfo userInfo = getWeiXinUid(appid, appSecret, code);
        getWeiXinUserInfo(userInfo);
        return userInfo;
    }

    public static SocialUserInfo getWeiXinUserInfo(String appid, String code, String componentAppId, String componentAccessToken) {
        SocialUserInfo userInfo = getWeiXinUid(appid, code, componentAppId, componentAccessToken);
        userInfo = getWeiXinUserInfo(userInfo);
        return userInfo;
    }

    /**
     * 获取微信用户信息
     *
     * @param userInfo 用户信息对象
     * @author Zhu Kaixiao
     * @date 2020/8/28 15:12
     */
    private static SocialUserInfo getWeiXinUserInfo(SocialUserInfo userInfo) {
        if(StringUtils.isNotBlank(userInfo.getOpenId())){
            String userInfoUrl = String.format(WEIXIN_USER_INFO_URL, userInfo.getAccessToken(), userInfo.getOpenId());
            String userInfoRet = HttpUtil.get(userInfoUrl);
            Map<String, Object> userInfoMap = JSONObject.parseObject(userInfoRet, HashMap.class);

            System.out.println("微信用户信息 ~~~~~~~~~~~~~~~~~~~" + userInfoMap.toString());

            if (userInfoMap.containsKey("errcode")) {
                throw new RuntimeException(userInfoMap.get("errmsg").toString());
            }
            userInfo.setSocialNickname(userInfoMap.get("nickname").toString());
            Integer gender = (Integer) userInfoMap.get("sex");
            if (gender != 1 && gender != 2) {
                gender = 3;
            }
            userInfo.setUid(userInfoMap.containsKey("unionid") ? userInfoMap.get("unionid").toString() : null);
            userInfo.setGender(gender);
            userInfo.setHeadImg(userInfoMap.get("headimgurl").toString());
            return userInfo;
        }
        return userInfo;
    }


    public static SocialUserInfo getQqUid(String appid, String secret, String code, String redirectUri) {
        // 1. 获取token
        String accessTokenRet;
        try {
            String accessTokenUrl = String.format(QQ_ACCESS_TOKEN_URL, appid, secret, code, redirectUri);
            accessTokenRet = HttpUtil.get(accessTokenUrl);
        } catch (Exception e) {
            log.debug("获取QQ TOKEN失败", e);
            throw new RuntimeException("获取QQ TOKEN失败", e);
        }

        Map<String, String> accessTokenMap;
        try {
            accessTokenMap = JSONObject.parseObject(accessTokenRet, HashMap.class);
        } catch (Exception e) {
            log.error("解析json失败", e);
            throw new RuntimeException("无法获取用户信息", e);
        }

        // QQ接口调用报错
        if (accessTokenMap.containsKey("error")) {
            log.debug("获取QQ token失败: error: [{}], error_description: [{}]", accessTokenMap.get("error"), accessTokenMap.get("error_description"));
            throw new RuntimeException(accessTokenMap.get("error_description"));
        }

        String accessToken = accessTokenMap.get("access_token");

        // 2. 获取qq openid
        String openIdRet;
        try {
            String qqOpenIdUrl = String.format(QQ_OPEN_ID_URL, accessToken);
            openIdRet = HttpUtil.get(qqOpenIdUrl);
        } catch (Exception e) {
            log.debug("获取QQ openid 失败", e);
            throw new RuntimeException("获取QQ openid 失败", e);
        }


        Map<String, Object> openIdMap;
        try {
            openIdMap = JSONObject.parseObject(openIdRet, HashMap.class);
        } catch (Exception e) {
            log.error("解析json失败", e);
            throw new RuntimeException("解析json失败", e);
        }

        // QQ接口调用报错
        if (accessTokenMap.containsKey("error")) {
            log.error("获取QQ用户唯一id失败: error: [{}], error_description: [{}]", accessTokenMap.get("error"), accessTokenMap.get("error_description"));
            throw new RuntimeException(accessTokenMap.get("error_description"));
        }

        openIdMap.putAll(accessTokenMap);
        openIdMap.put("appid", appid);

        SocialUserInfo socialUserInfo = parseMapToSocialUserInfo(SocialPlatform.QQ, openIdMap);
        return socialUserInfo;
    }

    public static SocialUserInfo getQqUserInfo(String appid, String secret, String code, String redirectUri) {
        SocialUserInfo userInfo = getQqUid(appid, secret, code, redirectUri);
        String userInfoUrl = String.format(QQ_USER_INFO_URL, userInfo.getAccessToken(), appid, userInfo.getUid());
        String userInfoRet = HttpUtil.get(userInfoUrl);
        Map<String, Object> userInfoMap = JSONObject.parseObject(userInfoRet, HashMap.class);
        if ((Integer) userInfoMap.get("ret") != 0) {
            throw new RuntimeException(userInfoMap.get("msg").toString());
        }
        userInfo.setSocialNickname(userInfoMap.get("nickname").toString());
        userInfo.setHeadImg(userInfoMap.get("figureurl_qq").toString());
        if ("男".equals(userInfoMap.get("gender"))) {
            userInfo.setGender(1);
        } else if ("女".equals(userInfoMap.get("gender"))) {
            userInfo.setGender(2);
        } else {
            userInfo.setGender(3);
        }

        return userInfo;
    }


    public static SocialUserInfo getWeiBoUid(String appid, String appSecret, String code, String redirectUri) {
        String accessTokenRet;
        try {
            String accessTokenUrl = String.format(WEIBO_ACCESS_TOKEN_URL, appid, appSecret, redirectUri, code);
            accessTokenRet = HttpUtil.post(accessTokenUrl, "");
        } catch (Exception e) {
            log.debug("获取微博TOKEN失败", e);
            throw new RuntimeException("获取微博TOKEN失败", e);
        }

        Map<String, Object> accessTokenMap;
        try {
            accessTokenMap = JSONObject.parseObject(accessTokenRet, HashMap.class);
        } catch (Exception e) {
            log.debug("解析json失败", e);
            throw new RuntimeException("解析json失败", e);
        }

        if (accessTokenMap.containsKey("error")) {
            log.error("获取微博用户唯一id失败: errcode: [{}], errmsg: [{}]", accessTokenMap.get("error_code"), accessTokenMap.get("error_description"));
            throw new RuntimeException("无法获取用户信息");
        }

        accessTokenMap.put("appid", appid);
        SocialUserInfo socialUserInfo = parseMapToSocialUserInfo(SocialPlatform.WeiBo, accessTokenMap);
        return socialUserInfo;
    }

    /**
     * 获取微博用户信息
     * https://open.weibo.com/wiki/2/users/show
     *
     * @param appid
     * @param appSecret
     * @param code
     * @param redirectUri
     * @author Zhu Kaixiao
     * @date 2020/8/12 16:20
     */
    public static SocialUserInfo getWeiBoUserInfo(String appid, String appSecret, String code, String redirectUri) {
        SocialUserInfo socialUserInfo = getWeiBoUid(appid, appSecret, code, redirectUri);
        String userInfoJsonStr;
        try {
            String userInfoUrl = String.format(WEIBO_USER_IFNO_URL, socialUserInfo.getAccessToken(), socialUserInfo.getUid());
            userInfoJsonStr = HttpUtil.get(userInfoUrl);
        } catch (Exception e) {
            log.debug("获取微博用户信息失败", e);
            throw new RuntimeException("获取微博用户信息失败", e);
        }

        Map<String, Object> userInfoMap;
        try {
            userInfoMap = JSONObject.parseObject(userInfoJsonStr, HashMap.class);
        } catch (Exception e) {
            log.debug("解析json失败", e);
            throw new RuntimeException("解析json失败", e);
        }

        if (userInfoMap.containsKey("error")) {
            throw new RuntimeException(userInfoMap.get("error").toString());
        }

        socialUserInfo.setSocialNickname(userInfoMap.get("name").toString());
        if ("m".equals(userInfoMap.get("gender"))) {
            socialUserInfo.setGender(1);
        } else if ("f".equals(userInfoMap.get("gender"))) {
            socialUserInfo.setGender(2);
        } else {
            socialUserInfo.setGender(3);
        }
        socialUserInfo.setHeadImg(userInfoMap.get("avatar_hd").toString());
        return socialUserInfo;
    }


    public static SocialUserInfo getSocialUid(SocialPlatform platform, String appid, String appSecret, String code, String redirectUri) {
        switch (platform) {
            case QQ:
                return getQqUid(appid, appSecret, code, redirectUri);
            case WeiBo:
                return getWeiBoUid(appid, appSecret, code, redirectUri);
            case WeiXin:
                return getWeiXinUid(appid, appSecret, code);
            default:
                return null;
        }
    }

    public static SocialUserInfo getSocialUserInfo(SocialPlatform platform, String appid, String appSecret, String code, String redirectUri) {
        switch (platform) {
            case QQ:
                return getQqUserInfo(appid, appSecret, code, redirectUri);
            case WeiBo:
                return getWeiBoUserInfo(appid, appSecret, code, redirectUri);
            case WeiXin:
                return getWeiXinUserInfo(appid, appSecret, code);
            default:
                return null;
        }
    }


    private static SocialUserInfo parseMapToSocialUserInfo(SocialPlatform platform, Map<String, Object> socialMap) {
        SocialUserInfo.SocialUserInfoBuilder builder = SocialUserInfo.builder();

        builder
                .platform(platform)
                .appid(socialMap.get("appid").toString());

        switch (platform) {
            case WeiXin:
                builder
                        .uid(socialMap.get("unionid") == null ? "" : socialMap.get("unionid").toString())
                        .openId(socialMap.get("openid").toString())
                        .accessToken(socialMap.get("access_token").toString())
                        .accessTokenExpireIn((Integer) socialMap.get("expires_in"))
                        .refreshToken(socialMap.get("refresh_token").toString());
                break;
            case WeiBo:
                builder
                        .uid(socialMap.get("uid").toString())
                        .accessToken(socialMap.get("access_token").toString())
                        .accessTokenExpireIn((Integer) socialMap.get("expires_in"));
                break;
            case QQ:
                builder
                        .uid(socialMap.get("openid").toString())
                        .accessToken(socialMap.get("access_token").toString())
                        .accessTokenExpireIn(Integer.parseInt(socialMap.get("expires_in").toString()))
                        .refreshToken(socialMap.get("refresh_token").toString());
                break;
        }


        return builder.build();
    }
}
