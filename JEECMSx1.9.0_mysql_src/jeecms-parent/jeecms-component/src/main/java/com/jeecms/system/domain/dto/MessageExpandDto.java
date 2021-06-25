/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain.dto;

/**
 * 消息实体扩展参数dto
 *
 * @author: chenming
 * @date: 2020/8/19 9:18
 */
public class MessageExpandDto {
    /*
     * 1. 取这个名字是因为这相当于在消息实体上面加一个扩展的dto，从而可以较少的改动之前的代码
     * 2. 这个dto也可用用来之后的扩展使用
     */
    /**
     * 事件状态：true->事件成功、false->事件失败
     */
    private Boolean eventStatus;
    /**
     * request请求类型：1-重置内容密级
     */
    private Integer requestType;
    /**
     * request请求参数(JSON字符串)
     */
    private String requestParameter;

    public MessageExpandDto(Boolean eventStatus, Integer requestType, String requestParameter) {
        this.eventStatus = eventStatus;
        this.requestType = requestType;
        this.requestParameter = requestParameter;
    }

    public Boolean getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(Boolean eventStatus) {
        this.eventStatus = eventStatus;
    }

    public Integer getRequestType() {
        return requestType;
    }

    public void setRequestType(Integer requestType) {
        this.requestType = requestType;
    }

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(String requestParameter) {
        this.requestParameter = requestParameter;
    }
}
