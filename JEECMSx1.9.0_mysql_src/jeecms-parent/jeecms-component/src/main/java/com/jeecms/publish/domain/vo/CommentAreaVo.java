package com.jeecms.publish.domain.vo;

import cn.hutool.core.codec.Base64;

/**
 * 评论统计VO
 * @author ljw
 */
public class CommentAreaVo {

    /**内容ID **/
    private Integer contentId;
    /** 省份 **/
    private String province;

    public CommentAreaVo(){}

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public static void main(String[] args) {
        System.out.printf(Base64.encode("其他"));
    }
}
