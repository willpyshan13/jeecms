/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain;

import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.system.domain.vo.MessageTplDetailsMapVo;
import org.hibernate.validator.constraints.Length;
import com.jeecms.common.base.domain.AbstractDomain;
import org.springframework.util.CollectionUtils;

/**
 * 模版信息实体类
 *
 * @author ljw
 * @date 2019年8月14日 上午9:49:25
 */
@Entity
@Table(name = "jc_sys_message_tpl")
public class MessageTpl extends AbstractDomain<Integer> {
    private static final long serialVersionUID = 1L;

    /**
     * 模板类型：会员验证
     */
    public static final int TPL_TYPE_MEMBER_VALIDATE = 1;
    /**
     * 模板类型：用户消息
     */
    public static final int TPL_TYPE_USER_MESSAGE = 2;

    /**
     * 主键值
     */
    private Integer id;
    /**
     * 所属站点
     */
    private Integer siteId;
    /**
     * 模板标题
     */
    private String mesTitle;
    /**
     * 模板唯一标识
     */
    private String mesCode;
    /**
     * 备注
     */
    private String remark;
    /**
     * 模板类型（1.会员验证 2.用户消息）
     */
    private Integer tplType;
    /**
     * 消息模板详情对象集合
      */
    private List<MessageTplDetails> details = new ArrayList<MessageTplDetails>();

    public MessageTpl() {

    }

    public MessageTpl(Integer siteId, String mesTitle, String mesCode, String remark, Integer tplType) {
        this.siteId = siteId;
        this.mesTitle = mesTitle;
        this.mesCode = mesCode;
        this.remark = remark;
        this.tplType = tplType;
    }

    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_sys_message_tpl", pkColumnValue = "jc_sys_message_tpl",
            initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_sys_message_tpl")
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

    @NotBlank
    @Length(max = 150)
    @Column(name = "mes_title", nullable = false, length = 150)
    public String getMesTitle() {
        return mesTitle;
    }

    public void setMesTitle(String mesTitle) {
        this.mesTitle = mesTitle;
    }

    @NotBlank
    @Length(max = 150)
    @Column(name = "mes_code", nullable = false, length = 150)
    public String getMesCode() {
        return mesCode;
    }

    public void setMesCode(String mesCode) {
        this.mesCode = mesCode;
    }

    @Column(name = "remark", nullable = true, length = 500)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "messageTpl")
    public List<MessageTplDetails> getDetails() {
        return details;
    }

    public void setDetails(List<MessageTplDetails> details) {
        this.details = details;
    }

    @Column(name = "tpl_type", nullable = false, length = 6)
    public Integer getTplType() {
        return tplType;
    }

    public void setTplType(Integer tplType) {
        this.tplType = tplType;
    }

    @Transient
    public String getTplName() {
        List<MessageTplDetails> messageTplDetailsList = getDetails();
        messageTplDetailsList = messageTplDetailsList.stream().filter(x -> MessageTplDetails.PHONE == x.getMesType()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(messageTplDetailsList)) {
            return messageTplDetailsList.get(0).getTplName();
        }
        return null;
    }

    @Transient
    public Boolean getOpenLetter() {
        List<MessageTplDetails> messageTplDetailsList = getDetails();
        messageTplDetailsList = messageTplDetailsList.stream().filter(messageTplDetails -> MessageTplDetails.LETTER == messageTplDetails.getMesType()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageTplDetailsList)) {
            return false;
        }
        return messageTplDetailsList.get(0).getIsOpen();
    }

    @Transient
    public Boolean getOpenMail() {
        List<MessageTplDetails> messageTplDetailsList = getDetails();
        messageTplDetailsList = messageTplDetailsList.stream().filter(messageTplDetails -> MessageTplDetails.MAIL == messageTplDetails.getMesType()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageTplDetailsList)) {
            return false;
        }
        return messageTplDetailsList.get(0).getIsOpen();
    }

    @Transient
    public Boolean getOpenPhone() {
        List<MessageTplDetails> messageTplDetailsList = getDetails();
        messageTplDetailsList = messageTplDetailsList.stream().filter(messageTplDetails -> MessageTplDetails.PHONE == messageTplDetails.getMesType()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageTplDetailsList)) {
            return false;
        }
        return messageTplDetailsList.get(0).getIsOpen();
    }

    @Transient
    public Map<String,MessageTplDetails> getDetailsMap() {
        List<MessageTplDetails> tpls = getDetails();
        Map<Short,List<MessageTplDetails>> detailsListMap = tpls.stream().collect(Collectors.groupingBy(MessageTplDetails::getMesType));
        Map<String,MessageTplDetails> detailsMap = new HashMap<>();
        detailsMap.put("letter",CollectionUtils.isEmpty(detailsListMap.get(MessageTplDetails.MESTYPE_SITE) ) ? null : detailsListMap.get(MessageTplDetails.MESTYPE_SITE).get(0));
        detailsMap.put("mail",CollectionUtils.isEmpty(detailsListMap.get(MessageTplDetails.MESTYPE_MAIL) ) ? null : detailsListMap.get(MessageTplDetails.MESTYPE_MAIL).get(0));
        detailsMap.put("phone",CollectionUtils.isEmpty(detailsListMap.get(MessageTplDetails.MESTYPE_PHONE) ) ? null : detailsListMap.get(MessageTplDetails.MESTYPE_PHONE).get(0));
        return detailsMap;
    }

//    @Transient
//    public List<MessageTplDetailsMapVo> getDetailsMap() {
//        List<MessageTplDetails> tpls = getDetails();
//        Map<Short,List<MessageTplDetails>> detailsListMap = tpls.stream().collect(Collectors.groupingBy(MessageTplDetails::getMesType));
//        List<MessageTplDetailsMapVo> vos = new ArrayList<>()
//        vos.add(new MessageTplDetailsMapVo(MessageTplDetails.MESTYPE_SITE,CollectionUtils.isEmpty(detailsListMap.get(MessageTplDetails.MESTYPE_SITE) ) ? null : detailsListMap.get(MessageTplDetails.MESTYPE_SITE).get(0)));
//        vos.add(new MessageTplDetailsMapVo(MessageTplDetails.MESTYPE_MAIL,CollectionUtils.isEmpty(detailsListMap.get(MessageTplDetails.MESTYPE_MAIL) ) ? null : detailsListMap.get(MessageTplDetails.MESTYPE_MAIL).get(0)));
//        vos.add(new MessageTplDetailsMapVo(MessageTplDetails.MESTYPE_PHONE,CollectionUtils.isEmpty(detailsListMap.get(MessageTplDetails.MESTYPE_PHONE) ) ? null : detailsListMap.get(MessageTplDetails.MESTYPE_PHONE).get(0)));
//        return detailsMap;
//    }

}