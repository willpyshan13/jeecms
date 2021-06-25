package com.jeecms.system.domain.vo;/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.system.domain.MessageTplDetails;

/**
 * @author: chenming
 * @date: 2020/6/28 9:36
 */
public class MessageTplDetailsMapVo {
    private Short status;

    private MessageTplDetails details;

    public MessageTplDetailsMapVo() {
    }

    public MessageTplDetailsMapVo(Short status, MessageTplDetails details) {
        this.status = status;
        this.details = details;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public MessageTplDetails getDetails() {
        return details;
    }

    public void setDetails(MessageTplDetails details) {
        this.details = details;
    }
}
