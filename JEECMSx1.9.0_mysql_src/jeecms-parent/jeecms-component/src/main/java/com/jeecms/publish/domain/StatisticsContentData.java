/*
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/
package com.jeecms.publish.domain;

import com.jeecms.channel.domain.Channel;
import com.jeecms.content.domain.Content;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * 内容数据统计表
* @author ljw
* @version 1.0
* @date 2020-06-16
*/
@Entity
@Table(name = "jc_statistics_content_data")
public class StatisticsContentData  implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /** 站点ID */
    private  Integer siteId;
    /** 栏目ID */
    private  Integer channelId;
    /** 内容ID */
    private  Integer contentId;
    /** 阅读量 */
    private  Integer readCount;
    /** 阅读人数 */
    private  Integer peopleCount;
    /** 点赞数 */
    private  Integer likeCount;
    /** 评论数 */
    private  Integer commentCount;
    /** 访问设备，1.计算机 2.移动设备 */
    private  Integer device;
    /** 省份 */
    private  String province;
    /** 统计类型 1.栏目 2.内容 3.浏览记录 4.访问设备 5.访问地域*/
    private Integer type;
    /** 统计时间 */
    private Date statisticsDay;

    /** 栏目对象 */
    private Channel channel;
    /** 内容对象 */
    private Content content;

	public StatisticsContentData() {}
	
    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_statistics_content_data", pkColumnValue = "jc_statistics_content_data", initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_statistics_content_data")
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
    
    @Column(name = "read_count", nullable = true, length = 11)
    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount (Integer readCount) {
        this.readCount = readCount;
    }
    
    @Column(name = "people_count", nullable = true, length = 11)
    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount (Integer peopleCount) {
        this.peopleCount = peopleCount;
    }
    
    @Column(name = "like_count", nullable = true, length = 11)
    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount (Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    @Column(name = "comment_count", nullable = true, length = 11)
    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount (Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    @Column(name = "device", nullable = true, length = 2)
    public Integer getDevice() {
        return device;
    }

    public void setDevice (Integer device) {
        this.device = device;
    }
    
    @Column(name = "province", nullable = true, length = 50)
    public String getProvince() {
        return province;
    }

    public void setProvince (String province) {
        this.province = province;
    }
    
    @Column(name = "statistics_day", nullable = false, length = 10)
    public Date getStatisticsDay() {
        return statisticsDay;
    }

    public void setStatisticsDay (Date statisticsDay) {
        this.statisticsDay = statisticsDay;
    }

    @Column(name = "type", nullable = false, length = 10)
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

}