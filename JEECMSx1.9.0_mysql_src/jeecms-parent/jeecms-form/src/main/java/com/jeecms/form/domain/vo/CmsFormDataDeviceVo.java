package com.jeecms.form.domain.vo;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;

/**
 * 统计设备vo
 * @author: tom
 * @date: 2020/2/24 18:46   
 */
public class CmsFormDataDeviceVo implements Serializable {

    private static final long serialVersionUID = 5008276586169751094L;
    private long pcCount;
    private long mobileCount;

    public long getPcCount() {
        return pcCount;
    }

    public void setPcCount(long pcCount) {
        this.pcCount = pcCount;
    }

    public long getMobileCount() {
        return mobileCount;
    }

    public void setMobileCount(long mobileCount) {
        this.mobileCount = mobileCount;
    }
}
