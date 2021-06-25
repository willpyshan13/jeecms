/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.domain.vo;

/**
 * @author: chenming
 * @date: 2020/8/18 16:57
 */
public class ResetSecretVo {
    /**
     * 标识(将请求的时间戳作为储存请求的标识后续通过该请求标识请求识别是否处理成功)
     */
    private String code;
    /**
     * 是否拥有进度条：预估时间大于5秒才能拥有进度条
     */
    private Integer predictionTimeConsuming;

    public ResetSecretVo(String code, Integer predictionTimeConsuming) {
        this.code = code;
        this.predictionTimeConsuming = predictionTimeConsuming;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getPredictionTimeConsuming() {
        return predictionTimeConsuming;
    }

    public void setPredictionTimeConsuming(Integer predictionTimeConsuming) {
        this.predictionTimeConsuming = predictionTimeConsuming;
    }
}
