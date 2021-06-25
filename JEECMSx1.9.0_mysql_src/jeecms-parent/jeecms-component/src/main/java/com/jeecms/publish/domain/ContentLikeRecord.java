/*
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/
package com.jeecms.publish.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
* @author ljw
* @version 1.0
* @date 2020-06-17

*/
@Entity
@Table(name = "jc_content_like_record")
public class ContentLikeRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /** 用户id */
    private  Integer userId;
    /** cookie值 */
    private  String cookie;
    /** 栏目id */
    private  Integer channelId;
    /** 内容id */
    private  Integer contentId;
    /** 点赞时间 **/
    private Date createTime;

	public ContentLikeRecord() {}

	/**构造函数**/
    public ContentLikeRecord(Integer userId, Integer channelId, Integer contentId) {
	    this.userId = userId;
        this.channelId = channelId;
        this.contentId = contentId;
        this.createTime = new Date();
    }

    /**构造函数**/
    public ContentLikeRecord(String cookie, Integer channelId, Integer contentId) {
        this.cookie = cookie;
        this.channelId = channelId;
        this.contentId = contentId;
        this.createTime = new Date();
    }

	
    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_content_like_record", pkColumnValue = "jc_content_like_record", initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_content_like_record")
    public Integer getId() {
        return this.id;
    }

    public void setId (Integer id) {
        this.id = id;
    }

    @Column(name = "user_id", nullable = true, length = 11)
    public Integer getUserId() {
        return userId;
    }

    public void setUserId (Integer userId) {
        this.userId = userId;
    }
    
    @Column(name = "cookie")
    public String getCookie() {
        return cookie;
    }

    public void setCookie (String cookie) {
        this.cookie = cookie;
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

    @Column(name = "create_time", nullable = false)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}