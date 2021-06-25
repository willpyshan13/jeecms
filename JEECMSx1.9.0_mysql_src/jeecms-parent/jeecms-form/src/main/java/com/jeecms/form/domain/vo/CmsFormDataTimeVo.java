package com.jeecms.form.domain.vo;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;

/**
 * 统计-时间vo
 * @author: tom
 * @date: 2020/2/25 9:37   
 */
public class CmsFormDataTimeVo implements Serializable {
    private static final long serialVersionUID = 6647709588750659727L;
    private String time;
    private long count;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
