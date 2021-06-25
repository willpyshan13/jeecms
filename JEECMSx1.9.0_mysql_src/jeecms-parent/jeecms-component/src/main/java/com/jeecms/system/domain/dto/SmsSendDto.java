/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.domain.dto;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 发送手机短信dto
 *
 * @author: chenming
 * @date: 2020/6/19 11:39
 */
public class SmsSendDto {
    /**
     * 手机消息模板id
     */
    private String tplId;
    /**
     * 站点id
     */
    private Integer siteId;
    /**
     * 占位符JSONObject（key->占位符(${code}),value->占位符对应的值(123456)）
     */
    private Map<String,String> placeholder;
    /**
     * 手机号list集合
     */
    private List<String> phoneList;
    /**
     * 手机号(绑定服务市场的手机号)
     */
    private String phone;

    public SmsSendDto(String tplId, Integer siteId, Map<String,String> placeholder, List<String> phoneList, String phone) {
        this.tplId = tplId;
        this.siteId = siteId;
        this.placeholder = placeholder;
        this.phoneList = phoneList;
        this.phone = phone;
    }

    public SmsSendDto() {
    }

    public String getTplId() {
        return tplId;
    }

    public void setTplId(String tplId) {
        this.tplId = tplId;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public Map<String,String> getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Map<String,String> placeholder) {
        this.placeholder = placeholder;
    }

    public List<String> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<String> phoneList) {
        this.phoneList = phoneList;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
