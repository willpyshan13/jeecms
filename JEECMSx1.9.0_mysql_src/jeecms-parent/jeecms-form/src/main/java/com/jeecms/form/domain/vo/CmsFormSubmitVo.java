package com.jeecms.form.domain.vo;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 提交后vo
 * @author: tom
 * @date: 2020/2/18 10:34   
 */
public class CmsFormSubmitVo implements Serializable {

    private static final long serialVersionUID = 9172994666124751338L;
    private Short processType;
    private String prompt;

    public Short getProcessType() {
        return processType;
    }

    public void setProcessType(Short processType) {
        this.processType = processType;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
