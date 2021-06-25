/*
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/
package com.jeecms.wechat.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
* @author ljw
* @version 1.0
* @date 2020-06-22

*/
@Entity
@Table(name = "jc_wechat_news_analyze")
public class WechatNewsAnalyze  implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /** 文章标识 */
    private  String appId;
    /** 文章标识 */
    private  String msgId;
    /** 标题 */
    private  String title;
    /** 图文页（点击群发图文卡片进入的页面）的阅读人数 */
    private  Integer intPageReadUser;
    /** 图文页的阅读次数 */
    private  Integer intPageReadCount;
    /** 原文页（点击图文页“阅读原文”进入的页面）的阅读人数，无原文页时此处数据为0 */
    private  Integer oriPageReadUser;
    /** 原文页的阅读次数 */
    private  Integer oriPageReadCount;
    /** 分享的人数 */
    private  Integer shareUser;
    /** 分享的次数 */
    private  Integer shareCount;
    /** 收藏的人数 */
    private  Integer addToFavUser;
    /** 收藏的次数 */
    private  Integer addToFavCount;
    /** 统计日期 */
    private Date refDate;
	public WechatNewsAnalyze() {}
	
    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_wechat_news_analyze", pkColumnValue = "jc_wechat_news_analyze", initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_wechat_news_analyze")
    public Integer getId() {
        return this.id;
    }

    public void setId (Integer id) {
        this.id = id;
    }

    @Column(name = "app_id", nullable = false, length = 255)
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Column(name = "msg_id", nullable = true, length = 50)
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId (String msgId) {
        this.msgId = msgId;
    }
    
    @Column(name = "title", nullable = true, length = 255)
    public String getTitle() {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }
    
    @Column(name = "int_page_read_user", nullable = true, length = 11)
    public Integer getIntPageReadUser() {
        return intPageReadUser;
    }

    public void setIntPageReadUser (Integer intPageReadUser) {
        this.intPageReadUser = intPageReadUser;
    }
    
    @Column(name = "int_page_read_count", nullable = true, length = 11)
    public Integer getIntPageReadCount() {
        return intPageReadCount;
    }

    public void setIntPageReadCount (Integer intPageReadCount) {
        this.intPageReadCount = intPageReadCount;
    }
    
    @Column(name = "ori_page_read_user", nullable = true, length = 11)
    public Integer getOriPageReadUser() {
        return oriPageReadUser;
    }

    public void setOriPageReadUser (Integer oriPageReadUser) {
        this.oriPageReadUser = oriPageReadUser;
    }
    
    @Column(name = "ori_page_read_count", nullable = true, length = 11)
    public Integer getOriPageReadCount() {
        return oriPageReadCount;
    }

    public void setOriPageReadCount (Integer oriPageReadCount) {
        this.oriPageReadCount = oriPageReadCount;
    }
    
    @Column(name = "share_user", nullable = true, length = 11)
    public Integer getShareUser() {
        return shareUser;
    }

    public void setShareUser (Integer shareUser) {
        this.shareUser = shareUser;
    }
    
    @Column(name = "share_count", nullable = true, length = 11)
    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount (Integer shareCount) {
        this.shareCount = shareCount;
    }
    
    @Column(name = "add_to_fav_user", nullable = true, length = 11)
    public Integer getAddToFavUser() {
        return addToFavUser;
    }

    public void setAddToFavUser (Integer addToFavUser) {
        this.addToFavUser = addToFavUser;
    }
    
    @Column(name = "add_to_fav_count", nullable = true, length = 11)
    public Integer getAddToFavCount() {
        return addToFavCount;
    }

    public void setAddToFavCount (Integer addToFavCount) {
        this.addToFavCount = addToFavCount;
    }
    
    @Column(name = "ref_date", nullable = true, length = 10)
    public Date getRefDate() {
        return refDate;
    }

    public void setRefDate (Date refDate) {
        this.refDate = refDate;
    }
    


}