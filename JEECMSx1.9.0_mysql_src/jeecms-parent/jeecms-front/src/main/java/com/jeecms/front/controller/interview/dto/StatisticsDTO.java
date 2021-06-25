/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.front.controller.interview.dto;

import java.time.LocalDateTime;

/**
 * @author xiaohui
 * @date 2021/1/22 16:23
 */

public class StatisticsDTO {

    private Long onlineId;
    private String productAppId;
    private String cookie;
    private String ip;
    private LocalDateTime time;
    private String username;

    public Long getOnlineId() {
        return onlineId;
    }

    public void setOnlineId(Long onlineId) {
        this.onlineId = onlineId;
    }

    public String getProductAppId() {
        return productAppId;
    }

    public void setProductAppId(String productAppId) {
        this.productAppId = productAppId;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "StatisticsDTO{" +
                "onlineId=" + onlineId +
                ", productAppId='" + productAppId + '\'' +
                ", cookie='" + cookie + '\'' +
                ", ip='" + ip + '\'' +
                ", time=" + time +
                ", username='" + username + '\'' +
                '}';
    }
}
