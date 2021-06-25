package com.jeecms.interact.domain.dto;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 是否发布表单dto
 * @author: tom
 * @date: 2020/2/19 10:07
 */
public class CmsFormPublishDto implements Serializable {

    private Integer[] ids;
    private Boolean publish;//true发布  false暂停

    @NotNull
    public Integer[] getIds() {
        return ids;
    }

    public void setIds(Integer[] ids) {
        this.ids = ids;
    }

    @NotNull
    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }
}
