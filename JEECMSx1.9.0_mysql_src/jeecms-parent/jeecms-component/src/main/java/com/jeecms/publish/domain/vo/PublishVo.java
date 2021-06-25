package com.jeecms.publish.domain.vo;

/**
 * 发布统计VO
 * @author ljw
 */
public class PublishVo {

    /** 名称 */
    private  String name;
    /** 计数 */
    private  Integer value;

    public PublishVo(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
