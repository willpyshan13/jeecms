package com.jeecms.form.domain.vo;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据统计省份vo
 * @author: tom
 * @date: 2020/2/24 18:49   
 */
public class CmsFormDataProviceVo implements Serializable {
    private static final long serialVersionUID = 7843760056023864232L;
    private String  province;
    private long dataCount;
    private BigDecimal ratio;

    public long getDataCount() {
        return dataCount;
    }

    public void setDataCount(long dataCount) {
        this.dataCount = dataCount;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }
}
