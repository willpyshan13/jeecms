package com.jeecms.interact.domain.vo;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;

/**
 * 表单类型Vo
 * @author: tom
 * @date: 2020/4/1 13:48   
 */
public class CmsFormTypeVo implements Serializable {
    private static final long serialVersionUID = 7479916890028121888L;
    private Integer id;
    private String name;
    private Integer formCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFormCount() {
        return formCount;
    }

    public void setFormCount(Integer formCount) {
        this.formCount = formCount;
    }
}
