/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.domain.vo;

/**
 * 同步云平台模板返回VO
 * @author: chenming
 * @date: 2020/6/12 10:01
 */
public class SynchPlatformTplVo {
    /**
     * 站点id
     */
    private Integer siteId;
    /**
     * 来源的消息模板id
     */
    private String sourceTplId;
    /**
     * 返回的消息模板id
     */
    private String targetTplId;

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getSourceTplId() {
        return sourceTplId;
    }

    public void setSourceTplId(String sourceTplId) {
        this.sourceTplId = sourceTplId;
    }

    public String getTargetTplId() {
        return targetTplId;
    }

    public void setTargetTplId(String targetTplId) {
        this.targetTplId = targetTplId;
    }
}
