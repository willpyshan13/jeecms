package com.jeecms.system.domain.vo;

import java.io.Serializable;

/**
 * 计算时效性VO
 * @author ljw
 */
public class MassScoreVo implements Serializable {
    private static final long serialVersionUID = -6824688071789008928L;

    /**内容ID**/
    private Integer contentId;
    /**内容count**/
    private Long counts;

    public MassScoreVo(){}

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Long getCounts() {
        return counts;
    }

    public void setCounts(Long counts) {
        this.counts = counts;
    }
}
