package com.jeecms.interact.domain.dto;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 表单创建(简单)dto
 * @author: tom
 * @date: 2020/1/6 14:05   
 */
public class CmsFormFastDto  implements Serializable {
    private String title;
    private String description;
    private Integer typeId;//分组ID 0未分组

    private Integer coverPicId;
    private Short processType;
    private String prompt;
    private Date beginTime;
    private Date endTime;
    private Boolean submitLimitLogin;
    private Integer userSubLimit;
    private Short userSubLimitUnit;
    private Integer ipSubLimit;
    private Short ipSubLimitUnit;
    private Integer deviceSubLimit;
    private Short deviceSubLimitUnit;
    private Boolean isOnlyWechat;
    private Integer wechatSubLimit;
    private Short wechatSubLimitUnit;
    private String shareDesc;
    private Integer shareLogoId;
    /**智能表单的表单区分站点管理，领导信箱的不区分，则需要区分则传递此参数*/
    private Integer siteId;

    @Length(max = 15)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getCoverPicId() {
        return coverPicId;
    }

    public void setCoverPicId(Integer coverPicId) {
        this.coverPicId = coverPicId;
    }

    public Short getProcessType() {
        return processType;
    }

    public void setProcessType(Short processType) {
        this.processType = processType;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Boolean getSubmitLimitLogin() {
        return submitLimitLogin;
    }

    public void setSubmitLimitLogin(Boolean submitLimitLogin) {
        this.submitLimitLogin = submitLimitLogin;
    }

    public Integer getUserSubLimit() {
        return userSubLimit;
    }

    public void setUserSubLimit(Integer userSubLimit) {
        this.userSubLimit = userSubLimit;
    }

    public Short getUserSubLimitUnit() {
        return userSubLimitUnit;
    }

    public void setUserSubLimitUnit(Short userSubLimitUnit) {
        this.userSubLimitUnit = userSubLimitUnit;
    }

    public Integer getIpSubLimit() {
        return ipSubLimit;
    }

    public void setIpSubLimit(Integer ipSubLimit) {
        this.ipSubLimit = ipSubLimit;
    }

    public Short getIpSubLimitUnit() {
        return ipSubLimitUnit;
    }

    public void setIpSubLimitUnit(Short ipSubLimitUnit) {
        this.ipSubLimitUnit = ipSubLimitUnit;
    }

    public Integer getDeviceSubLimit() {
        return deviceSubLimit;
    }

    public void setDeviceSubLimit(Integer deviceSubLimit) {
        this.deviceSubLimit = deviceSubLimit;
    }

    public Short getDeviceSubLimitUnit() {
        return deviceSubLimitUnit;
    }

    public void setDeviceSubLimitUnit(Short deviceSubLimitUnit) {
        this.deviceSubLimitUnit = deviceSubLimitUnit;
    }

    public Boolean getOnlyWechat() {
        return isOnlyWechat;
    }

    public void setOnlyWechat(Boolean onlyWechat) {
        isOnlyWechat = onlyWechat;
    }

    public Integer getWechatSubLimit() {
        return wechatSubLimit;
    }

    public void setWechatSubLimit(Integer wechatSubLimit) {
        this.wechatSubLimit = wechatSubLimit;
    }

    public Short getWechatSubLimitUnit() {
        return wechatSubLimitUnit;
    }

    public void setWechatSubLimitUnit(Short wechatSubLimitUnit) {
        this.wechatSubLimitUnit = wechatSubLimitUnit;
    }

    public String getShareDesc() {
        return shareDesc;
    }

    public void setShareDesc(String shareDesc) {
        this.shareDesc = shareDesc;
    }

    public Integer getShareLogoId() {
        return shareLogoId;
    }

    public void setShareLogoId(Integer shareLogoId) {
        this.shareLogoId = shareLogoId;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }
}
