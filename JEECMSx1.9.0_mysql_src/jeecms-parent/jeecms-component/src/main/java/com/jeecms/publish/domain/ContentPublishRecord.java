/*
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/
package com.jeecms.publish.domain;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.common.base.domain.AbstractIdDomain;
import com.jeecms.content.domain.Content;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.CmsSite;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * 内容发布统计记录实体
* @author ljw
* @version 基于x1.4.0
* @date 2020-06-03

*/
@Entity
@Table(name = "jc_content_publish_record")
public class ContentPublishRecord extends AbstractIdDomain<Integer> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /** 站点ID */
    private  Integer siteId;
    /** 组织ID */
    private  Integer orgId;
    /** 用户ID */
    private  Integer userId;
    /** 栏目ID */
    private  Integer channelId;
    /** 内容ID */
    private  Integer contentId;
    /** 发布时间 */
    private Date publishTime;

    /**关联对象**/
    private CmsSite site;
    private CmsOrg org;
    private CoreUser user;
    private Channel channel;
    private Content content;

	public ContentPublishRecord() {}

	/**有参构造函数**/
    public ContentPublishRecord(Content content) {
	    this.siteId = content.getSiteId();
	    this.channelId = content.getChannelId();
	    this.contentId = content.getId();
    }
	
    @Override
    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_content_publish_record", pkColumnValue = "jc_content_publish_record", initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_content_publish_record")
    public Integer getId() {
        return this.id;
    }

    public void setId (Integer id) {
        this.id = id;
    }

    @Column(name = "site_id", nullable = false, length = 11)
    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId (Integer siteId) {
        this.siteId = siteId;
    }
    
    @Column(name = "org_id", nullable = false, length = 11)
    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId (Integer orgId) {
        this.orgId = orgId;
    }

    @Column(name = "user_id", nullable = false, length = 11)
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Column(name = "channel_id", nullable = false, length = 11)
    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId (Integer channelId) {
        this.channelId = channelId;
    }
    
    @Column(name = "content_id", nullable = false, length = 11)
    public Integer getContentId() {
        return contentId;
    }

    public void setContentId (Integer contentId) {
        this.contentId = contentId;
    }

    @Column(name = "publish_time", nullable = false, length = 10)
    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public CmsSite getSite() {
        return site;
    }

    public void setSite(CmsSite site) {
        this.site = site;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public CmsOrg getOrg() {
        return org;
    }

    public void setOrg(CmsOrg org) {
        this.org = org;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public CoreUser getUser() {
        return user;
    }

    public void setUser(CoreUser user) {
        this.user = user;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}