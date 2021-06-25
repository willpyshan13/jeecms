package com.jeecms.publish.domain.vo;

import java.util.List;

/**
 * 发布统计VO
 * @author ljw
 */
public class PublishSumVo {

    /** 总数 */
    private  Integer sum;
    /** 集合 */
    private List<PublishVo> publishs;

    public PublishSumVo(){}

    /**构造函数**/
    public PublishSumVo(Integer sum) {
        this.sum = sum;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public List<PublishVo> getPublishs() {
        return publishs;
    }

    public void setPublishs(List<PublishVo> publishs) {
        this.publishs = publishs;
    }
}
