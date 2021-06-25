package com.jeecms.interact.domain.dto;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import javax.validation.constraints.NotNull;

/**
 * 创建修改(简单)dto
 * @author: tom
 * @date: 2020/1/6 14:08   
 */
public class CmsFormFastEditDto extends CmsFormFastDto {
    private Integer id;
    /**智能表单的表单区分站点管理，领导信箱的不区分，则需要区分则传递此参数*/
    private Integer siteId;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;


    @NotNull
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
