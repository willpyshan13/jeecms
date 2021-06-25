package com.jeecms.publish.domain.vo;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 内容浏览记录VO
 * @author ljw
 */
public class ContentSingleVo {

    /** 阅读量 */
    private  Integer readCount;
    /** 阅读人数 */
    private  Integer peopleCount;
    /** 点赞数 */
    private  Integer likeCount;
    /** 评论数 */
    private  Integer commentCount;

    public ContentSingleVo() {}

    public ContentSingleVo(Integer readCount, Integer peopleCount, Integer likeCount, Integer commentCount) {
        this.readCount = readCount;
        this.peopleCount = peopleCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
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

}
