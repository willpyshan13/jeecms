package com.jeecms.content.domain.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 内容VO
 * @author ljw
 */
public class ContentPerfVo implements Serializable {
    private static final long serialVersionUID = -6824688071789008928L;

    /**内容ID**/
    private Integer id;
    /**发布时间**/
    private Date releaseTime;
    /**栏目ID**/
    private Integer channelId;
    /**栏目名称**/
    private String channelName;
    /**内容标题**/
    private String title;
    /**站点ID**/
    private Integer siteId;
    /**预览地址**/
    private String previewUrl;
    /**组织ID**/
    private Integer orgId;
    /**用户ID**/
    private Integer userId;
    /**日期**/
    private String date;

    public ContentPerfVo(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
