package com.jeecms.form.domain.dto;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 表单类型删除dto
 * @author: tom
 * @date: 2020/4/1 15:04   
 */
public class CmsFormTypeDelDto implements Serializable {
    private static final long serialVersionUID = -4394146357595374903L;
    private Integer[] ids;
    private Boolean  cascadeDelForm;/**是否级联删除，true分组内的所有表单（以及表单内的填写数据）*/

    @NotNull
    @Size(min = 1, max = 1000000000)
    public Integer[] getIds() {
        return ids;
    }

    public void setIds(Integer[] ids) {
        this.ids = ids;
    }

    @NotNull
    public Boolean getCascadeDelForm() {
        return cascadeDelForm;
    }

    public void setCascadeDelForm(Boolean cascadeDelForm) {
        this.cascadeDelForm = cascadeDelForm;
    }
}
