package com.jeecms.form.domain;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.domain.AbstractDomain;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.domain.CmsModelItem;
import com.jeecms.content.domain.ContentAttr;
import com.jeecms.form.domain.vo.CmsFormDataAttrImgVo;
import com.jeecms.form.domain.vo.CmsFormDataAttrVo;
import com.jeecms.form.domain.vo.CmsFormResVo;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.Area;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author: tom
 * @date: 2020/2/13 16:17   
 */
@Entity
@Table(name = "jc_ex_form_data")
public class CmsFormDataEntity extends AbstractDomain<Integer> {
    private Integer id;
    private Integer formId;
    private Integer userId;
    private String ip;
    private String cityCode;
    private String provinceCode;
    private Boolean isRead;
    private Boolean isRecycle;
    private String cookieIdentity;
    private String wxopenId;
    private String systemInfo;
    private Boolean isPc;

    private CoreUser user;
    private CmsFormEntity form;

    /**
     * 省级对象
     */
    private Area province;
    /**
     * 城市对象
     */
    private Area city;

    private List<CmsFormDataAttrEntity> attrs;

    /***
     * 获取地址拼接(省市地址)
     *
     * @Title: getAddress
     * @return: String
     */
    @Transient
    public String getAddress() {
        StringBuffer address = new StringBuffer("");
        /**数据库存入的就是地区名称*/
        if (getProvinceCode() != null) {
            address.append(getProvinceCode());
        }
        if (getCityCode() != null) {
            address.append(getCityCode());
        }
        return address.toString();
    }

    /**
     * 获取多资源集合
     *
     * @Title: getAttachments
     * @return: List
     */
    @Transient
    public Set<CmsFormDataAttrEntity> getAttachments() {
        List<CmsFormDataAttrEntity> attrs = getAttrs();
        Set<CmsFormDataAttrEntity> attachs = new HashSet<CmsFormDataAttrEntity>();
        for (CmsFormDataAttrEntity a : attrs) {
            /**字段存在才认定数据有效*/
            if(getForm().existItem(a.getAttrName())) {
                if (!a.getAttrRes().isEmpty()) {
                    attachs.add(a);
                }
                if(a.getResourcesSpaceData()!=null){
                    attachs.add(a);
                }
            }
        }
        return attachs;
    }

    @Transient
    public Set<ResourcesSpaceData> getFileDatas() {
        Set<ResourcesSpaceData> datas = new HashSet<>();
        Set<CmsFormDataAttrEntity> attachs =getAttachments();
        for (CmsFormDataAttrEntity a : attachs) {
            datas.add(a.getResourcesSpaceData());
            datas.addAll(a.getAttrResSpaceData());
        }
        datas = datas.stream().filter(d->d!=null).collect(Collectors.toSet());
        return datas;
    }


    /**
     * 获取字段map key为字段名，value为字段值{资源类型是文件别名，城市地址是详细拼接}
     * @return
     */
    @Transient
    public Map<String,Object> getAttrMap() {
        List<CmsFormDataAttrEntity> attrs = getAttrs();
        Map<String,Object> map = new HashMap<>();
        ResourcesSpaceDataService resourcesSpaceDataService = ApplicationContextProvider.getBean(ResourcesSpaceDataService.class);
        for (CmsFormDataAttrEntity a : attrs) {
            /**表单存在字段才显示*/
            if(getForm().existItem(a.getAttrName())){
                if (!a.getAttrRes().isEmpty()) {
                    /**多资源*/
                    List<CmsFormResVo> vos = new ArrayList<>();
                    for(CmsFormDataAttrResEntity r:a.getAttrRes()){
                        //buffer.append(r.getAlias()+",");
                        CmsFormResVo vo = new CmsFormResVo();
                        MyBeanUtils.copyProperties(r,vo);
                        vos.add(vo);
                    }
                    map.put(a.getAttrName(),vos);
                }else if(a.getResourcesSpaceData()!=null){
                    CmsFormResVo vo = new CmsFormResVo();
                    MyBeanUtils.copyProperties(a,vo);
                    vo.setCoverImg(a.getCoverImg());
                    /**单资源*/
                    map.put(a.getAttrName(),vo);
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_CITY.equals(a.getAttrType())
                        ||CmsFormConstant.FIELD_ATTR_FILE_PROP_ADDRESS.equals(a.getAttrType())){
                    /**城市地址类型*/
                    map.put(a.getAttrName(),a.getAddress());
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_ITEM.equals(a.getAttrType())){
                    /**单选 下拉 类型等使用option的label 从表单字段的option map 取lable*/
                    String value = a.getAttrValue();
                    String label = getForm().getItem(a.getAttrName()).getOptionLabel(value);
                    /**其他选项追加用户填写的其他选项值*/
                    if(CmsFormConstant.OTHER_OPTION.equals(value)&& StringUtils.isNotBlank(a.getOtherInput())){
                        label = label+"("+a.getOtherInput()+")";
                    }
                    map.put(a.getAttrName(),label);
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_ITEM_MULTI.equals(a.getAttrType())){
                    /**多选、级联 选择等使用option的label 从表单字段的option map 取lable*/
                    List<String>optionLabels = new ArrayList<String>();
                    /**数组字符串*/
                    List<String> vals = JSONArray.parseArray(a.getAttrValue(), String.class);
                    for(String v:vals){
                        String label = getForm().getItem(a.getAttrName()).getOptionLabel(v);
                        String value = label;
                        /**其他选项追加用户填写的其他选项值*/
                        if(CmsFormConstant.OTHER_OPTION.equals(v)&&StringUtils.isNotBlank(a.getOtherInput())){
                            value = value+"("+a.getOtherInput()+")";
                        }
                        optionLabels.add(value);
                    }
                    map.put(a.getAttrName(),optionLabels);
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_IMG_RADIO.equals(a.getAttrType())){
                    /**图片单选类型等使用val从资源表加载路径 */
                    if(StrUtils.isNumeric(a.getAttrValue())){
                        Integer resId = getForm().getItem(a.getAttrName()).getOptionPicMap().get(a.getAttrValue());
                        String  optionLabel = getForm().getItem(a.getAttrName()).getOptionMap().get(a.getAttrValue());
                        if(resId!=null){
                            ResourcesSpaceData res = resourcesSpaceDataService.findById(resId);
                            if(res!=null){
                                CmsFormDataAttrImgVo imgVo = new CmsFormDataAttrImgVo(res.getUrl(),optionLabel,resId);
                                map.put(a.getAttrName(),imgVo);
                            }
                        }
                    }
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_IMG_CHECKBOX.equals(a.getAttrType())){
                    /**图片多选类型等使用val从资源表加载路径，集合 */
                    if(a.getAttrValue()!=null){
                        List<Integer> ids = JSONArray.parseArray(a.getAttrValue(), Integer.class);
                        List<CmsFormDataAttrImgVo>imgVos = new ArrayList<CmsFormDataAttrImgVo>();
                        Map<String,String>optionMap = getForm().getItem(a.getAttrName()).getOptionMap();
                        if(ids!=null){
                            for(Integer id:ids){
                                Integer resId = getForm().getItem(a.getAttrName()).getOptionPicMap().get(id.toString());
                                if(resId!=null){
                                    ResourcesSpaceData res = resourcesSpaceDataService.findById(resId);
                                    if(res!=null){
                                        String  optionLabel = optionMap.get(id.toString());
                                        CmsFormDataAttrImgVo imgVo = new CmsFormDataAttrImgVo(res.getUrl(),optionLabel,resId);
                                        imgVos.add(imgVo);
                                    }
                                }
                            }
                            map.put(a.getAttrName(),imgVos);
                        }
                    }
                }else{
                    map.put(a.getAttrName(),a.getAttrValue());
                }
            }
        }
        return map;
    }

    /**
     * 获取字段map key为字段名，value为字段值{资源类型是文件别名，城市地址是详细拼接}
     * @return
     */
    @Transient
    public List<CmsFormDataAttrVo> getDatas() {
        Map<String,Object> map = getAttrMap();
        List<CmsFormDataAttrVo>vos = new ArrayList<>();
        /**按字段顺序添加显示*/
        for (CmsFormItemEntity itemEntity: getForm().getInputItems()) {
            CmsFormDataAttrVo vo = new CmsFormDataAttrVo(itemEntity.getDataType(),itemEntity.getField(),
                    itemEntity.getItemLabel(),"",itemEntity.getIsRequired());
            if(map.containsKey(itemEntity.getField())) {
                vo.setVal(map.get(itemEntity.getField()));
            }
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 获取字段map key为字段label，value为字段值{资源类型是文件别名，城市地址是详细拼接}
     * @return
     */
    @Transient
    @JSONField(serialize = false)
    public Map<String,Object> getAttrLabelMap() {
        Map<String,Object> map = new HashMap<>();
        Map<String,String> itemMap = new HashMap<>();
        Map<String,Object> dataAttrMap = getAttrMap();
        List<CmsFormItemEntity>items = getForm().getInputItems();
        for(CmsFormItemEntity item:items){
            itemMap.put(item.getField(),item.getItemLabel());
        }
        for(String key:itemMap.keySet()){
            map.put(itemMap.get(key),dataAttrMap.get(key));
        }
        return map;
    }

    /**
     * 以map的方式读取自定义属性，方便单个取字段
     *
     * @Title: getAttr
     * @return: Map
     */
//    @Transient
//    public Map<String, CmsFormDataAttrEntity> getAttr() {
//        List<CmsFormDataAttrEntity> attrs = getAttrs();
//        Map<String, CmsFormDataAttrEntity> map = new HashMap<String, CmsFormDataAttrEntity>();
//        if (attrs != null && attrs.size() > 0) {
//            Integer num = 999;
//            for (CmsFormDataAttrEntity attr : attrs) {
//                /**表单存在字段才显示*/
//                if(getForm().existItem(attr.getAttrName())) {
//                    if (CmsModelConstant.MANY_CHOOSE.equals(attr.getAttrType())) {
//                        StringBuilder labelValue = new StringBuilder();
//                        // 多选
//                        // 获取到多选框的值json
//                        CmsFormItemEntity item = getForm().getItem(attr.getAttrName());
//                        if (item != null) {
//                            String jsonString = item.getContent();
//                            JSONObject jsonObject = JSONObject.parseObject(jsonString)
//                                    .getJSONObject("value");
//                            JSONArray array = jsonObject.getJSONArray("options");
//                            // 遍历获取多选框的每个值，与内容选择的值比较
//                            String[] value = null;
//                            String other = "";
//                            if (attr.getAttrValue() != null) {
//                                JSONObject attrValueJson = JSONObject
//                                        .parseObject(attr.getAttrValue());
//                                String obj = JSONObject
//                                        .toJSONString(attrValueJson.get("value"));
//                                if (obj.startsWith("[")) {
//                                    value = obj.substring(1, obj.length() - 1).split(",");
//                                } else {
//                                    value = obj.split(",");
//                                }
//                                other = JSONObject.toJSONString(attrValueJson.get("attrValue"));
//                            }
//                            if (value != null) {
//                                for (Object o : array) {
//                                    JSONObject json = (JSONObject) o;
//                                    String key = json.getString("value");
//                                    String label = json.getString("label");
//                                    for (String integer : value) {
//                                        if (integer.equals(key)) {
//                                            labelValue.append(label);
//                                        }
//                                        if (String.valueOf(num).equals(integer)) {
//                                            labelValue.append(other);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        attr.setValue(labelValue.toString());
//                    } else if (CmsModelConstant.SINGLE_CHOOSE.equals(attr.getAttrType())
//                            || CmsModelConstant.DROP_DOWN.equals(attr.getAttrType())) {
//                        // 单选
//                        StringBuilder labelValue = new StringBuilder();
//                        // 多选
//                        // 获取到多选框的值json
//                        CmsFormItemEntity item = getForm().getItem(attr.getAttrName());
//                        if (item != null) {
//                            String jsonString = item.getContent();
//                            JSONObject jsonObject = JSONObject.parseObject(jsonString)
//                                    .getJSONObject("value");
//                            JSONArray array = jsonObject.getJSONArray("options");
//                            // 遍历获取多选框的每个值，与内容选择的值比较
//                            Integer value = null;
//                            String other = "";
//                            if (attr.getAttrValue() != null) {
//                                JSONObject attrValueJson = JSONObject
//                                        .parseObject(attr.getAttrValue());
//                                value = Integer.valueOf(JSONObject
//                                        .toJSONString(attrValueJson.get("value")));
//                                other = JSONObject.toJSONString(attrValueJson.get("attrValue"));
//                            }
//                            if (value != null) {
//                                for (Object o : array) {
//                                    JSONObject json = (JSONObject) o;
//                                    Integer key = json.getInteger("value");
//                                    String label = json.getString("label");
//                                    if (value.equals(key)) {
//                                        labelValue.append(label);
//                                    } else if (num.equals(value)) {
//                                        labelValue.append(other);
//                                    }
//                                }
//                            }
//                        }
//                        attr.setValue(labelValue.toString());
//                    } else {
//                        attr.setValue(attr.getAttrValue());
//                    }
//                    map.put(attr.getAttrName(), attr);
//                }
//            }
//        }
//        return map;
//    }

    @Transient
    public Map<String, CmsFormDataAttrEntity> getAttr() {
        List<CmsFormDataAttrEntity> attrs = getAttrs();
        Map<String, CmsFormDataAttrEntity> map = new HashMap<String, CmsFormDataAttrEntity>();
        if (attrs != null && attrs.size() > 0) {
            for (CmsFormDataAttrEntity attr : attrs) {
                /**表单存在字段才显示*/
                if(getForm().existItem(attr.getAttrName())) {
                    map.put(attr.getAttrName(), attr);
                }
            }
        }
        return map;
    }

    @Id
    @Column(name = "data_id")
    @TableGenerator(name = "jc_ex_form_data", pkColumnValue = "jc_ex_form_data", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_ex_form_data")
    public Integer getId() {
        return id;
    }

    public void setId(Integer dataId) {
        this.id = dataId;
    }

    @Basic
    @Column(name = "form_id")
    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    @Basic
    @Column(name = "user_id")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Basic
    @Column(name = "city_code")
    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    @Basic
    @Column(name = "province_code")
    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Basic
    @Column(name = "is_read")
    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    @Basic
    @Column(name = "is_recycle")
    public Boolean getIsRecycle() {
        return isRecycle;
    }

    public void setIsRecycle(Boolean isRecycle) {
        this.isRecycle = isRecycle;
    }

    @Basic
    @Column(name = "cookie_identity")
    public String getCookieIdentity() {
        return cookieIdentity;
    }

    public void setCookieIdentity(String cookieIdentity) {
        this.cookieIdentity = cookieIdentity;
    }

    @Basic
    @Column(name = "u_open_id")
    public String getWxopenId() {
        return wxopenId;
    }

    public void setWxopenId(String wxopenId) {
        this.wxopenId = wxopenId;
    }

    @Basic
    @Column(name = "system_info")
    public String getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(String systemInfo) {
        this.systemInfo = systemInfo;
    }

    @Basic
    @Column(name = "is_pc")
    public Boolean getIsPc() {
        return isPc;
    }

    public void setIsPc(Boolean isPc) {
        this.isPc = isPc;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    public CoreUser getUser() {
        return user;
    }

    public void setUser(CoreUser user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    public CmsFormEntity getForm() {
        return form;
    }

    public void setForm(CmsFormEntity form) {
        this.form = form;
    }

    @OneToMany(mappedBy = "data", fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    public List<CmsFormDataAttrEntity> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<CmsFormDataAttrEntity> attrs) {
        this.attrs = attrs;
    }
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "area_code", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_code", referencedColumnName = "area_code", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public Area getCity() {
        return city;
    }

    public void setCity(Area city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CmsFormDataEntity that = (CmsFormDataEntity) o;
        return id.equals(that.id) &&
                formId.equals(that.formId) &&
                isRead.equals(that.isRead) &&
                isRecycle.equals(that.isRecycle) &&
                isPc.equals(that.isPc) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(cityCode, that.cityCode) &&
                Objects.equals(provinceCode, that.provinceCode) &&
                Objects.equals(cookieIdentity, that.cookieIdentity) &&
                Objects.equals(systemInfo, that.systemInfo) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(updateUser, that.updateUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formId, userId, ip, cityCode, provinceCode, isRead, isRecycle, cookieIdentity, systemInfo, isPc, createTime, updateTime, createUser, updateUser);
    }
}
