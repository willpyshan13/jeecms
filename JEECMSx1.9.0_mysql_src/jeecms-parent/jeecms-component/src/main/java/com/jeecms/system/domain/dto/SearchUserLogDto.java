/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain.dto;

import java.util.Date;

/**
 * 用户日志查询（分级保护）
 *
 * @author xiaohui
 * @version 1.0
 * @Date 2020-07-29 10:57
 */
public class SearchUserLogDto {
    /**
     * 用户名
     */
    private String username;
    /**
     * 客户ip
     */
    private String clientIp;
    /**
     * 子事件类型
     */
    private String subEventType;
    /**
     * 日志级别
     */
    private Integer logLevel;
    /**
     * 操作类型
     */
    private Integer operateType;
    /**
     * 请求结果
     */
    private Integer requestResult;
    /**
     * 开始时间
     */
    private Date beginDate;
    /**
     * 时间时间
     */
    private Date endDate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getSubEventType() {
        return subEventType;
    }

    public void setSubEventType(String subEventType) {
        this.subEventType = subEventType;
    }

    public Integer getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }

    public Integer getRequestResult() {
        return requestResult;
    }

    public void setRequestResult(Integer requestResult) {
        this.requestResult = requestResult;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
