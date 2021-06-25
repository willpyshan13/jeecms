/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.jeecms.common.base.domain.AbstractDomain;

/**
 * 模版详细信息实体类
 *
 * @version 1.0
 * @author: wulongwei
 * @date: 2019年4月26日 下午1:37:13
 */
@Entity
@Table(name = "jc_sys_message_tpl_detail")
public class MessageTplDetails extends AbstractDomain<Integer> {
    private static final long serialVersionUID = 1L;

    /**
     * 全局唯一标识符
     */
    private Integer id;
    /**
     * 站点id
     */
    private Integer siteId;
    /**
     * 关联的消息模板id
     */
    private Integer mesTplId;
    /**
     * 是否开启 false 不开启 ；true 开启
     */
    private Boolean isOpen;
    /**
     * 消息类型(消息类型 1、站内信 2、邮件 3、手机)
     */
    private Short mesType;
    /**
     * 模板id,对手机消息模板设置时需要填的值
     */
    private String tplId;
    /**
     * 手机模板名称
     */
    private String tplName;
    /**
     * 标题
     */
    private String mesTitle;
    /**
     * 消息内容
     */
    private String mesContent;
    /**
     * 扩展字段
     */
    private String extendedField;
    /**
     * 消息模板
     **/
    private MessageTpl messageTpl;

    public MessageTplDetails() {

    }

    public MessageTplDetails(Integer siteId, Boolean isOpen, Short mesType) {
        this.siteId = siteId;
        this.isOpen = isOpen;
        this.mesType = mesType;
    }

    public MessageTplDetails(Integer siteId, Integer mesTplId, Short mesType) {
        this.siteId = siteId;
        this.mesTplId = mesTplId;
        this.mesType = mesType;
    }

    public MessageTplDetails(Integer siteId, Integer mesTplId, Boolean isOpen, Short mesType) {
        this.siteId = siteId;
        this.mesTplId = mesTplId;
        this.isOpen = isOpen;
        this.mesType = mesType;
    }

    public MessageTplDetails(Integer siteId, Integer mesTplId, Boolean isOpen, Short mesType, String tplId, String mesTitle, String mesContent, String extendedField, String tplName) {
        this.siteId = siteId;
        this.mesTplId = mesTplId;
        this.isOpen = isOpen;
        this.mesType = mesType;
        this.tplId = tplId;
        this.mesTitle = mesTitle;
        this.mesContent = mesContent;
        this.extendedField = extendedField;
        this.tplName = tplName;
    }

    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_sys_message_tpl_detail", pkColumnValue = "jc_sys_message_tpl_detail",
            initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_sys_message_tpl_detail")
    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "site_id", nullable = false, length = 11)
    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @Column(name = "mes_tpl_id", nullable = false, length = 11)
    public Integer getMesTplId() {
        return mesTplId;
    }

    public void setMesTplId(Integer mesTplId) {
        this.mesTplId = mesTplId;
    }

    @NotNull
    @Column(name = "is_open", nullable = false, length = 1)
    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    @NotNull
    @Column(name = "mes_type", nullable = false, length = 6)
    public Short getMesType() {
        return mesType;
    }

    public void setMesType(Short mesType) {
        this.mesType = mesType;
    }

    @Column(name = "tpl_id", nullable = true, length = 150)
    @Length(max = 150)
    public String getTplId() {
        return tplId;
    }

    public void setTplId(String tplId) {
        this.tplId = tplId;
    }

    @Column(name = "tpl_name", nullable = true, length = 150)
    @Length(max = 150)
    public String getTplName() {
        return tplName;
    }

    public void setTplName(String tplName) {
        this.tplName = tplName;
    }

    @Column(name = "mes_title", nullable = true, length = 150)
    @Length(max = 150)
    public String getMesTitle() {
        return mesTitle;
    }

    public void setMesTitle(String mesTitle) {
        this.mesTitle = mesTitle;
    }

    @Column(name = "mes_content", nullable = true, length = 900)
    public String getMesContent() {
        return mesContent;
    }

    public void setMesContent(String mesContent) {
        this.mesContent = mesContent;
    }

    @Column(name = "extended_field", nullable = true, length = 900)
    @Length(max = 300)
    public String getExtendedField() {
        return extendedField;
    }

    public void setExtendedField(String extendedField) {
        this.extendedField = extendedField;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "mes_tpl_id", insertable = false, updatable = false)
    public MessageTpl getMessageTpl() {
        return messageTpl;
    }

    public void setMessageTpl(MessageTpl messageTpl) {
        this.messageTpl = messageTpl;
    }


    /**
     * 消息类型(1.站内信)
     */
    public static final Short MESTYPE_SITE = 1;
    /**
     * 消息类型(2.邮箱)
     */
    public static final Short MESTYPE_MAIL = 2;
    /**
     * 消息类型(3.手机)
     */
    public static final Short MESTYPE_PHONE = 3;

    public static final short LETTER = 1;
    public static final short MAIL = 2;
    public static final short PHONE = 3;

}
