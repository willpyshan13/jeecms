package com.jeecms.interact.domain.dto;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.JSONArray;
import com.jeecms.interact.domain.CmsFormEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 表单修改dto
 * @author: tom
 * @date: 2020/1/6 14:41   
 */
public class CmsFormUpdateDto extends CmsFormEntity implements Serializable {
    private Integer id;
    private JSONArray fields;

    @NotNull
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @NotEmpty
    public JSONArray getFields() {
        return fields;
    }

    public void setFields(JSONArray fields) {
        this.fields = fields;
    }
}
