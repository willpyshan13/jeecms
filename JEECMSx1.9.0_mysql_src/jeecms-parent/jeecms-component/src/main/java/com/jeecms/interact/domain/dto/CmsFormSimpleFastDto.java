package com.jeecms.interact.domain.dto;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
 * 表单创建(简单)dto
 * @author: tom
 * @date: 2020/1/6 14:05   
 */
public class CmsFormSimpleFastDto implements Serializable {
    private String title;
    private String description;
    private Integer typeId;//分组ID 0未分组

    /**智能表单的表单区分站点管理，领导信箱的不区分，则需要区分则传递此参数*/
    private Integer siteId;

    private Integer id;

    @Length(max = 15)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
