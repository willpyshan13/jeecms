package com.jeecms.interact.domain.vo;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.io.Serializable;
import java.util.List;

/**
 * 分组类型汇总vo
 * @author: tom
 * @date: 2020/4/1 13:52   
 */
public class CmsFormTypeVoSumary implements Serializable {
    private static final long serialVersionUID = -7040130512740070959L;
    private List<CmsFormTypeVo> forms;
    private Long formTotalCount;/**总表单数量*/
    private Long notGroupFormCount;/**未分组表单数量*/

    public List<CmsFormTypeVo> getForms() {
        return forms;
    }

    public void setForms(List<CmsFormTypeVo> forms) {
        this.forms = forms;
    }

    public Long getFormTotalCount() {
        return formTotalCount;
    }

    public void setFormTotalCount(Long formTotalCount) {
        this.formTotalCount = formTotalCount;
    }

    public Long getNotGroupFormCount() {
        return notGroupFormCount;
    }

    public void setNotGroupFormCount(Long notGroupFormCount) {
        this.notGroupFormCount = notGroupFormCount;
    }
}
