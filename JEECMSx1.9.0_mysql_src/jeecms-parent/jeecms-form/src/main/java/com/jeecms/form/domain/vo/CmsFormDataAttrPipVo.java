package com.jeecms.form.domain.vo;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据属性占比vo
 * @author: tom
 * @date: 2020/4/14 15:24   
 */
public class CmsFormDataAttrPipVo implements Serializable {
    private static final long serialVersionUID = -6834626910830774842L;
    private String label;/**属性label*/
    private Integer count;/**数量*/


//    public CmsFormDataAttrPipVo(String label, BigDecimal ratio,Integer count) {
//        this.label = label;
//        this.ratio = ratio;
//        this.count = count;
//    }

    public CmsFormDataAttrPipVo(String label, Integer count) {
        this.label = label;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**占比*/



    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
