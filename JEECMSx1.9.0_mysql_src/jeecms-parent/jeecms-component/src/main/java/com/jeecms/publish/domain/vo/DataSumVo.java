package com.jeecms.publish.domain.vo;

import java.io.Serializable;

/**
 * 发布统计总数VO
 * @author ljw
 */
public class DataSumVo implements Serializable {

    private static final long serialVersionUID = 2704156900615661706L;
    /** 阅读量 */
    private  Integer readCount;
    /** 阅读人数 */
    private  Integer peopleCount;
    /** 点赞数 */
    private  Integer likeCount;
    /** 评论数 */
    private  Integer commentCount;

    public DataSumVo(){}

    /**构造函数**/
    public DataSumVo(Integer readCount, Integer peopleCount, Integer likeCount, Integer commentCount) {
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
