package com.jeecms.form.domain.dto;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 标志数据是否已读dto
 * @author: tom
 * @date: 2020/2/19 10:07
 */
public class CmsFormDataReadDto implements Serializable {

    private List<Integer> ids;
    private Boolean read;//true已读  false未读

    @NotEmpty
    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    @NotNull
    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
