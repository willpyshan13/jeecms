package com.jeecms.form.domain.vo;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 表单数据字段vo
 *
 * @author: tom
 * @date: 2020/2/24 15:09
 */
public class CmsFormDataAttrStatisticVo implements Serializable {
    private static final long serialVersionUID = 2330286459294588678L;
    String field;
    /**
     * 字段
     */
    String itemLabel;/**字段label*/
    //Map<String,Float> data;
    /**
     * 单选、多选、图片单选、图片多选、下拉、级联、城市、性别时，使用饼图展示 统计数据
     */
    Set<CmsFormDataAttrPipVo> data;

    /**
     * 是否饼状图字段
     */
    Boolean pip;

    /**
     * 数量
     */


    public Boolean getPip() {
        return pip;
    }

    public void setPip(Boolean pip) {
        this.pip = pip;
    }

    /**
     * 是否饼状图字段
     */


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public Set<CmsFormDataAttrPipVo> getData() {
        return data;
    }

    public void setData(Set<CmsFormDataAttrPipVo> data) {
        this.data = data;
    }
}
