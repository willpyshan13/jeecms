package com.jeecms.common.util.social;


/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/6/24 13:51
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class SocialUserInfo {

    /**
     * 第三方平台用户唯一id
     */
    private String uid;

    private String openId;

    /**
     * 第三方平台
     */
    private SocialPlatform platform;

    /**
     * 第三方平台accessToken
     */
    private String accessToken;

    /**
     * 第三方平台accessToken有效期
     */
    private Integer accessTokenExpireIn;

    /**
     * 第三方平台refreshToken
     */
    private String refreshToken;

    /**
     * 第三方平台refreshToken有效期
     */
    private Integer refreshTokenExpireIn;


    /**
     * 第三方平台Appid
     */
    private String appid;

    /**
     * 第三方平台昵称
     */
    private String socialNickname;

    /**
     * 第三方平台性别
     * 1 男 2 女 3 未知
     */
    private Integer gender;

    /**
     * 第三方平台头像
     */
    private String headImg;


    public static SocialUserInfo.SocialUserInfoBuilder builder() {
        return new SocialUserInfo.SocialUserInfoBuilder();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getAccessTokenExpireIn() {
        return accessTokenExpireIn;
    }

    public void setAccessTokenExpireIn(Integer accessTokenExpireIn) {
        this.accessTokenExpireIn = accessTokenExpireIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getRefreshTokenExpireIn() {
        return refreshTokenExpireIn;
    }

    public void setRefreshTokenExpireIn(Integer refreshTokenExpireIn) {
        this.refreshTokenExpireIn = refreshTokenExpireIn;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSocialNickname() {
        return socialNickname;
    }

    public void setSocialNickname(String socialNickname) {
        this.socialNickname = socialNickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    @Override
    public String toString() {
        return "SocialUserInfo{" + "uid='" + uid + '\'' + ", openId='" + openId + '\'' + ", platform=" + platform + ", accessToken='" + accessToken + '\'' + ", accessTokenExpireIn=" + accessTokenExpireIn + ", refreshToken='" + refreshToken + '\'' + ", refreshTokenExpireIn=" + refreshTokenExpireIn + ", appid='" + appid + '\'' + ", socialNickname='" + socialNickname + '\'' + ", gender=" + gender + ", headImg='" + headImg + '\'' + '}';
    }

    public SocialUserInfo() {
    }

    public SocialUserInfo(final String uid, final String openId, final SocialPlatform platform, final String accessToken, final Integer accessTokenExpireIn, final String refreshToken, final Integer refreshTokenExpireIn, final String appid, final String socialNickname, final Integer gender, final String headImg) {
        this.uid = uid;
        this.openId = openId;
        this.platform = platform;
        this.accessToken = accessToken;
        this.accessTokenExpireIn = accessTokenExpireIn;
        this.refreshToken = refreshToken;
        this.refreshTokenExpireIn = refreshTokenExpireIn;
        this.appid = appid;
        this.socialNickname = socialNickname;
        this.gender = gender;
        this.headImg = headImg;
    }

    public static class SocialUserInfoBuilder {
        private String uid;
        private String openId;
        private SocialPlatform platform;
        private String accessToken;
        private Integer accessTokenExpireIn;
        private String refreshToken;
        private Integer refreshTokenExpireIn;
        private String appid;
        private String socialNickname;
        private Integer gender;
        private String headImg;

        SocialUserInfoBuilder() {
        }

        public SocialUserInfo.SocialUserInfoBuilder uid(final String uid) {
            this.uid = uid;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder openId(final String openId) {
            this.openId = openId;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder platform(final SocialPlatform platform) {
            this.platform = platform;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder accessToken(final String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder accessTokenExpireIn(final Integer accessTokenExpireIn) {
            this.accessTokenExpireIn = accessTokenExpireIn;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder refreshToken(final String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder refreshTokenExpireIn(final Integer refreshTokenExpireIn) {
            this.refreshTokenExpireIn = refreshTokenExpireIn;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder appid(final String appid) {
            this.appid = appid;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder socialNickname(final String socialNickname) {
            this.socialNickname = socialNickname;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder gender(final Integer gender) {
            this.gender = gender;
            return this;
        }

        public SocialUserInfo.SocialUserInfoBuilder headImg(final String headImg) {
            this.headImg = headImg;
            return this;
        }

        public SocialUserInfo build() {
            return new SocialUserInfo(this.uid, this.openId, this.platform, this.accessToken, this.accessTokenExpireIn, this.refreshToken, this.refreshTokenExpireIn, this.appid, this.socialNickname, this.gender, this.headImg);
        }

        @Override
        public String toString() {
            return "SocialUserInfo.SocialUserInfoBuilder(uid=" + this.uid + ", openId=" + this.openId + ", platform=" + this.platform + ", accessToken=" + this.accessToken + ", accessTokenExpireIn=" + this.accessTokenExpireIn + ", refreshToken=" + this.refreshToken + ", refreshTokenExpireIn=" + this.refreshTokenExpireIn + ", appid=" + this.appid + ", socialNickname=" + this.socialNickname + ", gender=" + this.gender + ", headImg=" + this.headImg + ")";
        }
    }
}
