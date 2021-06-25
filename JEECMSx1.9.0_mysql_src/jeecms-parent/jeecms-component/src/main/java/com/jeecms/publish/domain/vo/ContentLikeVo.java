package com.jeecms.publish.domain.vo;

/**
 * 点赞VO
 * @author ljw
 */
public class ContentLikeVo {

    /** 名称 */
    private  Integer key;
    /** 计数 */
    private  Long value;

    public ContentLikeVo(){}

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
