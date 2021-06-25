package com.jeecms.publish.domain.vo;

/**
 * 内容浏览记录VO
 *
 * @author ljw
 */
public class ContentTableVo {

    /**
     * 省份
     */
    private String province;
    /**
     * 阅读量
     */
    private Integer readCount;
    /**
     * 阅读人数
     */
    private Integer peopleCount;
    /**
     * 评论数
     */
    private Integer commentCount;

    public ContentTableVo() {
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
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

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }


}
