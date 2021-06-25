/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.domain.dto;

import com.jeecms.system.domain.MessageTplDetails;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 消息模板修改状态(开启/关闭邮件、站内信、短信)功能
 * @author: chenming
 * @date: 2020/6/11 9:08
 */
public class MessageTplDetailsStatusDto {
    /**
     * 模板id
     */
    private Integer id;
    /**
     * 消息类型(消息类型 1、站内信 2、邮件 3、手机)
     */
    private Short mesType;
    /**
     * 是否开启 false 不开启 ；true 开启
     */
    private Boolean open;
    /**
     * 模版详细信息实体类
     */
    private MessageTplDetails tplDetails;

    @NotNull
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @NotNull
    @Min(1)
    @Max(3)
    public Short getMesType() {
        return mesType;
    }

    public void setMesType(Short mesType) {
        this.mesType = mesType;
    }

    @NotNull
    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public MessageTplDetails getTplDetails() {
        return tplDetails;
    }

    public void setTplDetails(MessageTplDetails tplDetails) {
        this.tplDetails = tplDetails;
    }
}
