package com.jeecms.publish.domain.vo;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 内容浏览记录VO
 * @author ljw
 */
public class ContentViewVo {

    /** 阅读量 */
    private  Integer readCount;
    /** 阅读人数 */
    private  Integer peopleCount;
    /** 点赞数 */
    private  Integer likeCount;
    /** 评论数 */
    private  Integer commentCount;
    /** 计算机阅读数 */
    private  Integer pc;
    /** 移动设备阅读数 */
    private  Integer mobile;
    /** 计算机阅读数百分比 */
    private Integer pcPrecent;
    /** 移动设备阅读数百分比 */
    private  Integer mobilePrecent;
    /**地图数据**/
    private List<JSONObject> map;

    public ContentViewVo() {}

    public ContentViewVo(Integer readCount, Integer peopleCount, Integer likeCount, Integer commentCount,
                         Integer pc, Integer mobile, Integer pcPrecent,Integer mobilePrecent) {
        this.readCount = readCount;
        this.peopleCount = peopleCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.pc = pc;
        this.mobile = mobile;
        this.pcPrecent = pcPrecent;
        this.mobilePrecent = mobilePrecent;
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

    public Integer getPc() {
        return pc;
    }

    public void setPc(Integer pc) {
        this.pc = pc;
    }

    public Integer getMobile() {
        return mobile;
    }

    public void setMobile(Integer mobile) {
        this.mobile = mobile;
    }

    public Integer getPcPrecent() {
        return pcPrecent;
    }

    public void setPcPrecent(Integer pcPrecent) {
        this.pcPrecent = pcPrecent;
    }

    public Integer getMobilePrecent() {
        return mobilePrecent;
    }

    public void setMobilePrecent(Integer mobilePrecent) {
        this.mobilePrecent = mobilePrecent;
    }

    public List<JSONObject> getMap() {
        return map;
    }

    public void setMap(List<JSONObject> map) {
        this.map = map;
    }
}
