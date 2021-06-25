package com.jeecms.form.domain.vo;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;

/**
 * 表单字段属性vo
 * @author: tom
 * @date: 2020/4/15 10:20   
 */
public class CmsFormDataAttrVo implements Serializable {

    private static final long serialVersionUID = -8967548112495958011L;
    private  String field;
    private String label;
    private String dataType;
    private Object val;
    private Boolean isRequired;

    public CmsFormDataAttrVo(String dataType,String field, String label, Object val) {
        this.dataType = dataType;
        this.field = field;
        this.label = label;
        this.val = val;
    }

    public CmsFormDataAttrVo(String dataType, String field, String label, Object val, Boolean isRequired) {
        this.field = field;
        this.label = label;
        this.dataType = dataType;
        this.val = val;
        this.isRequired = isRequired;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }
}
