package com.jeecms.form.domain.vo;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;

/**
 * 表单字段属性-图片单选多选vo
 * @author: tom
 * @date: 2020/4/16 13:39   
 */
public class CmsFormDataAttrImgVo implements Serializable {
    String url;/**图片地址*/
    String label;/**选项label*/
    Integer val;

    public CmsFormDataAttrImgVo(String url, String label, Integer val) {
        this.url = url;
        this.label = label;
        this.val = val;
    }

    /**选项id值*/

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }
}
