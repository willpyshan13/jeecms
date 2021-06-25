package com.jeecms.interact.domain;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.common.base.domain.AbstractDomain;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.util.SystemContextUtils;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author: tom
 * @date: 2020/1/3 18:36   
 */
@Entity
@Table(name = "jc_ex_form")
public class CmsFormEntity extends AbstractDomain<Integer> {
    private Integer id;
    private String title;
    private String description;
    private String bgConfig;
    private String headConfig;
    private String fontConfig;
    private String contConfig;
    private String subConfig;
    private String componentConfig;
    private Short processType;
    private String prompt;
    private Boolean submitLimitLogin;
    private Integer userSubLimit;
    private Short userSubLimitUnit;
    private Integer ipSubLimit;
    private Short ipSubLimitUnit;
    private Integer deviceSubLimit;
    private Short deviceSubLimitUnit;
    private Boolean isOnlyWechat;
    private Integer wechatSubLimit;
    private Short wechatSubLimitUnit;
    private String shareDesc;
    private Date beginTime;
    private Date endTime;
    private Boolean isCaptcha;
    private Short formScene;

    private Integer typeId;
    private Integer viewCount;//浏览量
    private Integer joinCount;//参与人数
    private Short status;//状态（0未发布 1进行中 2已结束)
    private CmsFormTypeEntity formType;
    private Integer siteId;

    private Integer bgImgId;
    private Integer headImgId;
    private Integer shareLogoId;
    private Integer coverPicId;
    private ResourcesSpaceData bgImg;
    private ResourcesSpaceData headImg;
    private ResourcesSpaceData shareLogo;
    private ResourcesSpaceData coverPic;
    private CmsSite site;

    private List<CmsFormItemEntity>items;


    private JSONArray fields;

    /**
     *查看表单状态 0未发布 1进行中 2已结束
     * @return
     */
    @Transient
    public Short getViewStatus(){
        if(getStatus()!=null){
            if(CmsFormConstant.FORM_STATU_NO_PUBLISH.equals(getStatus())
                    ||CmsFormConstant.FORM_STATU_STOP.equals(getStatus())){
                return CmsFormConstant.FORM_VIEW_STATU_NO_PUBLISH;
            }else{
                Date now  = Calendar.getInstance().getTime();
                if(getEndTime()!=null&&getEndTime().before(now)){
                    return CmsFormConstant.FORM_VIEW_STATU_STOP;
                }
                return  CmsFormConstant.FORM_VIEW_STATU_PUBLISH;
            }
        }
        return CmsFormConstant.FORM_VIEW_STATU_NO_PUBLISH;
    }

    /**
     * 根据字段名称获取模型项
     *
     * @Title: getItem
     * @param field
     *                字段名
     * @return: CmsModelItem
     */
    @Transient
    public CmsFormItemEntity getItem(String field) {
        for (CmsFormItemEntity item : getInputItems()) {
            if (item.getField().equals(field)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 是否存在某字段
     * @param field
     * @return
     */
    @Transient
    public boolean existItem(String field) {
        if(getItem(field)!=null){
            return true;
        }
        return false;
    }

    /**
     * 前台浏览地址（全路径）
     * @return
     */
    @Transient
    public String getUrl() {
        CmsSite site = getSite();
        if (CmsFormConstant.FORM_SCENE_FORM.equals(getFormScene()) && site != null) {
            return site.getUrlDynamicForFix(true) + "interact-intelligrnceForm.htm?id=" + getId();
        }
        if (CmsFormConstant.FORM_SCENE_LETTER.equals(getFormScene())) {
            /**领导信箱的表单不区分站点，所以此处只能从当前请求中获取站点*/
            site = SystemContextUtils.getSite(RequestUtils.getHttpServletRequest());
            return site.getUrlDynamicForFix(true) + "interact-letterView.htm?id=" + getId();
        }
        return "";
    }

    /**
     * 前台真实预览浏览地址（全路径）,加上/preview 前缀为了设备识别切换只是在预览模式下有效
     *
     * @return
     */
    @Transient
    public String getViewLoadUrl() {
        CmsSite site = getSite();
        if (CmsFormConstant.FORM_SCENE_FORM.equals(getFormScene()) && site != null) {
            return site.getUrlDynamicForFix(true) + WebConstants.PREVIEW_URL + "/interact-intelligrnceForm.htm?id=" + getId();
        }
        if (CmsFormConstant.FORM_SCENE_LETTER.equals(getFormScene())) {
            /**领导信箱的表单不区分站点，所以此处只能从当前请求中获取站点*/
            site = SystemContextUtils.getSite(RequestUtils.getHttpServletRequest());
            return site.getUrlDynamicForFix(true) + WebConstants.PREVIEW_URL + "/interact-letterView.htm?id=" + getId();
        }
        return "";
    }

    /** 表单预览地址 **/
    @Transient
    public String getPreviewUrl() {
        StringBuilder url = new StringBuilder();
        /**默认智能表单*/
        if (CmsFormConstant.FORM_SCENE_FORM.equals(getFormScene()) && getSite() != null) {
            url.append(getSite().getSitePreviewUrl()).append("?formId=").append(this.id).append("&type=")
                    .append(WebConstants.PREVIEW_TYPE_FORM);
        }
        /**领导信箱*/
        if (CmsFormConstant.FORM_SCENE_LETTER.equals(getFormScene())) {
            CmsSite site = SystemContextUtils.getSite(RequestUtils.getHttpServletRequest());
            url.append(site.getSitePreviewUrl()).append("?formId=").append(this.id).append("&type=")
                    .append(WebConstants.PREVIEW_TYPE_LETTER);
        }
        return url.toString();
    }

    /**
     * 获取调用的js调用方式代码
     * @return
     */
    @Transient
    public String getCallScript() {
        String url = getUrl();
        String script = "<script>document.writeln(\"<iframe src='"+url+"' width='799' height='800' frameborder='0' style='overflow:auto'></iframe>\");</script>";
        return script;
    }

    /**
     * 获得所有的id的List集合
     */
    @Transient
    @JSONField(serialize = false)
    public static Integer[] fetchIds(Collection<CmsFormEntity> forms) {
        if (forms == null) {
            return null;
        }
        // 过滤值为null
        forms = forms.stream().filter(x -> x != null).collect(Collectors.toList());
        List<Integer> ids = new ArrayList<Integer>();
        for (CmsFormEntity s : forms) {
            ids.add(s.getId());
        }
        Integer[] integers = new Integer[ids.size()];
        integers = ids.toArray(integers);
        return integers;
    }

    public void init(){
        if(getIsOnlyWechat()==null){
            setIsOnlyWechat(false);
        }
        if(getSubmitLimitLogin()==null){
            setSubmitLimitLogin(false);
        }
        if(getIsCaptcha()==null){
            setIsCaptcha(false);
        }
        setStatus(CmsFormConstant.FORM_STATU_NO_PUBLISH);
        setViewCount(0);
        setJoinCount(0);
    }

    /***
     * 获取表单组件，过滤布局组件，布局组件不需要显示数据和接收数据
     * @return
     */
    @Transient
    public List<CmsFormItemEntity> getInputItems(){
        return getItems().stream().filter(item->CmsFormConstant.GROUP_INPUT.equals(item.getGroupType()) ).collect(Collectors.toList());
    }

    @Id
    @Column(name = "form_id")
    @TableGenerator(name = "jc_ex_form", pkColumnValue = "jc_ex_form", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_ex_form")
    public Integer getId() {
        return id;
    }

    public void setId(Integer formId) {
        this.id = formId;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "bg_config")
    public String getBgConfig() {
        return bgConfig;
    }

    public void setBgConfig(String bgConfig) {
        this.bgConfig = bgConfig;
    }

    @Basic
    @Column(name = "head_config")
    public String getHeadConfig() {
        return headConfig;
    }

    public void setHeadConfig(String headConfig) {
        this.headConfig = headConfig;
    }

    @Basic
    @Column(name = "font_config")
    public String getFontConfig() {
        return fontConfig;
    }

    public void setFontConfig(String fontConfig) {
        this.fontConfig = fontConfig;
    }

    @Basic
    @Column(name = "cont_config")
    public String getContConfig() {
        return contConfig;
    }

    public void setContConfig(String contConfig) {
        this.contConfig = contConfig;
    }

    @Basic
    @Column(name = "sub_config")
    public String getSubConfig() {
        return subConfig;
    }

    public void setSubConfig(String subConfig) {
        this.subConfig = subConfig;
    }

    @Basic
    @Column(name = "component_config")
    public String getComponentConfig() {
        return componentConfig;
    }

    public void setComponentConfig(String componentConfig) {
        this.componentConfig = componentConfig;
    }

    @Basic
    @Column(name = "bg_img_id")
    public Integer getBgImgId() {
        return bgImgId;
    }

    public void setBgImgId(Integer bgImgId) {
        this.bgImgId = bgImgId;
    }

    @Basic
    @Column(name = "head_img_id")
    public Integer getHeadImgId() {
        return headImgId;
    }

    public void setHeadImgId(Integer headImgId) {
        this.headImgId = headImgId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bg_img_id", insertable = false, updatable = false)
    public ResourcesSpaceData getBgImg() {
        return bgImg;
    }

    public void setBgImg(ResourcesSpaceData bgImg) {
        this.bgImg = bgImg;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_img_id", insertable = false, updatable = false)
    public ResourcesSpaceData getHeadImg() {
        return headImg;
    }

    public void setHeadImg(ResourcesSpaceData headImg) {
        this.headImg = headImg;
    }

    @Basic
    @Column(name = "cover_pic")
    public Integer getCoverPicId() {
        return coverPicId;
    }

    public void setCoverPicId(Integer coverPicId) {
        this.coverPicId = coverPicId;
    }

    @Basic
    @Column(name = "process_type")
    public Short getProcessType() {
        return processType;
    }

    public void setProcessType(Short processType) {
        this.processType = processType;
    }

    @Basic
    @Column(name = "prompt")
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Basic
    @Column(name = "submit_limit_login")
    public Boolean getSubmitLimitLogin() {
        return submitLimitLogin;
    }

    public void setSubmitLimitLogin(Boolean submitLimitLogin) {
        this.submitLimitLogin = submitLimitLogin;
    }

    @Basic
    @Column(name = "user_sub_limit")
    public Integer getUserSubLimit() {
        return userSubLimit;
    }

    public void setUserSubLimit(Integer userSubLimit) {
        this.userSubLimit = userSubLimit;
    }

    @Basic
    @Column(name = "user_sub_limit_unit")
    public Short getUserSubLimitUnit() {
        return userSubLimitUnit;
    }

    public void setUserSubLimitUnit(Short userSubLimitUnit) {
        this.userSubLimitUnit = userSubLimitUnit;
    }

    @Basic
    @Column(name = "ip_sub_limit")
    public Integer getIpSubLimit() {
        return ipSubLimit;
    }

    public void setIpSubLimit(Integer ipSubLimit) {
        this.ipSubLimit = ipSubLimit;
    }

    @Basic
    @Column(name = "ip_sub_limit_unit")
    public Short getIpSubLimitUnit() {
        return ipSubLimitUnit;
    }

    public void setIpSubLimitUnit(Short ipSubLimitUnit) {
        this.ipSubLimitUnit = ipSubLimitUnit;
    }

    @Basic
    @Column(name = "device_sub_limit")
    public Integer getDeviceSubLimit() {
        return deviceSubLimit;
    }

    public void setDeviceSubLimit(Integer deviceSubLimit) {
        this.deviceSubLimit = deviceSubLimit;
    }

    @Basic
    @Column(name = "device_sub_limit_unit")
    public Short getDeviceSubLimitUnit() {
        return deviceSubLimitUnit;
    }

    public void setDeviceSubLimitUnit(Short deviceSubLimitUnit) {
        this.deviceSubLimitUnit = deviceSubLimitUnit;
    }

    @Basic
    @Column(name = "is_only_wechat")
    public Boolean getIsOnlyWechat() {
        return isOnlyWechat;
    }

    public void setIsOnlyWechat(Boolean isOnlyWechat) {
        this.isOnlyWechat = isOnlyWechat;
    }

    @Basic
    @Column(name = "wechat_sub_limit")
    public Integer getWechatSubLimit() {
        return wechatSubLimit;
    }

    public void setWechatSubLimit(Integer wechatSubLimit) {
        this.wechatSubLimit = wechatSubLimit;
    }

    @Basic
    @Column(name = "wechat_sub_limit_unit")
    public Short getWechatSubLimitUnit() {
        return wechatSubLimitUnit;
    }

    public void setWechatSubLimitUnit(Short wechatSubLimitUnit) {
        this.wechatSubLimitUnit = wechatSubLimitUnit;
    }

    @Basic
    @Column(name = "share_logo")
    public Integer getShareLogoId() {
        return shareLogoId;
    }

    public void setShareLogoId(Integer shareLogoId) {
        this.shareLogoId = shareLogoId;
    }

    @Basic
    @Column(name = "share_desc")
    public String getShareDesc() {
        return shareDesc;
    }

    public void setShareDesc(String shareDesc) {
        this.shareDesc = shareDesc;
    }

    @Basic
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @Column(name = "begin_time")
    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    @Basic
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


    @Basic
    @Column(name = "is_captcha")
    public Boolean getIsCaptcha() {
        return isCaptcha;
    }

    public void setIsCaptcha(Boolean isCaptcha) {
        this.isCaptcha = isCaptcha;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_logo", insertable = false, updatable = false)
    public ResourcesSpaceData getShareLogo() {
        return shareLogo;
    }

    public void setShareLogo(ResourcesSpaceData shareLogo) {
        this.shareLogo = shareLogo;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_pic", insertable = false, updatable = false)
    public ResourcesSpaceData getCoverPic() {
        return coverPic;
    }

    public void setCoverPic(ResourcesSpaceData coverPic) {
        this.coverPic = coverPic;
    }

    @Basic
    @Column(name = "form_scene")
    public Short getFormScene() {
        return formScene;
    }

    public void setFormScene(Short formScene) {
        this.formScene = formScene;
    }

    @OneToMany(mappedBy = "form")
    @Where(clause = " deleted_flag=false ")
    public List<CmsFormItemEntity> getItems() {
        return items;
    }

    public void setItems(List<CmsFormItemEntity> items) {
        this.items = items;
    }

    @Basic
    @Column(name = "type_id")
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    @Basic
    @Column(name = "view_count")
    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    @Basic
    @Column(name = "join_count")
    public Integer getJoinCount() {
        return joinCount;
    }

    public void setJoinCount(Integer joinCount) {
        this.joinCount = joinCount;
    }

    @Basic
    @Column(name = "status")
    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    @Transient
    public JSONArray getFields() {
        return fields;
    }

    public void setFields(JSONArray fields) {
        this.fields = fields;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    public CmsFormTypeEntity getFormType() {
        return formType;
    }

    public void setFormType(CmsFormTypeEntity formType) {
        this.formType = formType;
    }

    @Column(name = "site_id", length = 11)
    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    public CmsSite getSite() {
        return site;
    }

    public void setSite(CmsSite site) {
        this.site = site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CmsFormEntity that = (CmsFormEntity) o;
        return id.equals(that.id) &&
                submitLimitLogin.equals( that.submitLimitLogin) &&
                isOnlyWechat.equals( that.isOnlyWechat) &&
                isCaptcha.equals(that.isCaptcha) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(bgConfig, that.bgConfig) &&
                Objects.equals(headConfig, that.headConfig) &&
                Objects.equals(fontConfig, that.fontConfig) &&
                Objects.equals(contConfig, that.contConfig) &&
                Objects.equals(subConfig, that.subConfig) &&
                Objects.equals(bgImgId, that.bgImgId) &&
                Objects.equals(headImgId, that.headImgId) &&
                Objects.equals(coverPic, that.coverPic) &&
                Objects.equals(processType, that.processType) &&
                Objects.equals(prompt, that.prompt) &&
                Objects.equals(userSubLimit, that.userSubLimit) &&
                Objects.equals(userSubLimitUnit, that.userSubLimitUnit) &&
                Objects.equals(ipSubLimit, that.ipSubLimit) &&
                Objects.equals(ipSubLimitUnit, that.ipSubLimitUnit) &&
                Objects.equals(deviceSubLimit, that.deviceSubLimit) &&
                Objects.equals(deviceSubLimitUnit, that.deviceSubLimitUnit) &&
                Objects.equals(wechatSubLimit, that.wechatSubLimit) &&
                Objects.equals(wechatSubLimitUnit, that.wechatSubLimitUnit) &&
                Objects.equals(shareLogo, that.shareLogo) &&
                Objects.equals(shareDesc, that.shareDesc) &&
                Objects.equals(beginTime, that.beginTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(formScene, that.formScene) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(updateUser, that.updateUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, bgConfig, headConfig, fontConfig, contConfig, subConfig, bgImgId, headImgId, coverPic,  processType, prompt, submitLimitLogin, userSubLimit, userSubLimitUnit, ipSubLimit, ipSubLimitUnit, deviceSubLimit, deviceSubLimitUnit, isOnlyWechat, wechatSubLimit, wechatSubLimitUnit, shareLogo, shareDesc, beginTime, endTime, isCaptcha, formScene, createTime, updateTime, createUser, updateUser);
    }
}
