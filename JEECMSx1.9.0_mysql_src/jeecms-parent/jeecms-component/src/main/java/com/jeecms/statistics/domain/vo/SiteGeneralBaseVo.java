package com.jeecms.statistics.domain.vo;

import java.io.Serializable;

/**
 * 网站信息基础VO
 * @author: ljw
 * @version: 基于x1.4.0
 * @date 2020-06-03
 */
public class SiteGeneralBaseVo implements Serializable {

    private static final long serialVersionUID = -8082221937871455821L;

    /**今日**/
    private Integer today;
    /**昨日**/
    private Integer yesterday;
    /**历史最高**/
    private Integer high;
    /**累计**/
    private Integer sum;

    public SiteGeneralBaseVo() {
    }

    public Integer getToday() {
        return today;
    }

    public void setToday(Integer today) {
        this.today = today;
    }

    public Integer getYesterday() {
        return yesterday;
    }

    public void setYesterday(Integer yesterday) {
        this.yesterday = yesterday;
    }

    public Integer getHigh() {
        return high;
    }

    public void setHigh(Integer high) {
        this.high = high;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }
}
