package com.jeecms.publish.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

/**
 * 发布统计VO
 * @author ljw
 */
public class DataVo implements Serializable {

    private static final long serialVersionUID = 2704156900615661706L;
    /** id */
    private Integer id;
    /** 名称 */
    private  String name;
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
    /** 发布时间 */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;
    /** 预览地址 */
    private String url;

    public DataVo(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
