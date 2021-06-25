package com.jeecms.form.domain;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.domain.AbstractIdDomain;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.Area;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author: tom
 * @date: 2020/2/1316:39
 */
@Entity
@Table(name = "jc_ex_form_data_attr")
public class CmsFormDataAttrEntity extends AbstractIdDomain implements Serializable {
    private Integer id;
    private Integer dataId;
    private String attrName;
    private String attrValue;
    private Short attrType;
    private Integer resId;
    private String provinceCode;
    private String cityCode;
    private String areaCode;
    private String pdfPath;
    private Integer coverImgId;
    private String otherInput;

    private String value;

    /**
     * 省级对象
     */
    private Area province;
    /**
     * 城市对象
     */
    private Area city;
    /**
     * 区级对象
     */
    private Area area;
    /**
     * 表单对象
     */
    private CmsFormDataEntity data;
    /**
     * 资源对象
     */
    private ResourcesSpaceData resourcesSpaceData;
    /**
     * 视频封面图资源对象
     */
    private ResourcesSpaceData coverImg;

    /***
     * 附件多图资源
     */
    private List<CmsFormDataAttrResEntity>attrRes;

    /***
     *  是否有效值，空的判断无效
     * @param item
     * @return
     */
    @Transient
    public boolean getEffectiveValue(CmsFormItemEntity item ){
        if(CmsFormConstant.TEXT.equals(item.getDataType())
                ||CmsFormConstant.TEXTS.equals(item.getDataType())
                ||CmsFormConstant.SINGLE_CHOOSE.equals(item.getDataType())
                ||CmsFormConstant.MANY_CHOOSE.equals(item.getDataType())
                ||CmsFormConstant.DROP_DOWN.equals(item.getDataType())
                ||CmsFormConstant.CASCADE.equals(item.getDataType())
                ||CmsFormConstant.DATE_SECTION.equals(item.getDataType())
                ||CmsFormConstant.SEX.equals(item.getDataType())
                ||CmsFormConstant.DATE.equals(item.getDataType())){
            return StringUtils.isNotBlank(getAttrValue());
        }else if(CmsFormConstant.ADDRESS.equals(item.getDataType())
                ||CmsFormConstant.CITY.equals(item.getDataType())){
            return StringUtils.isNotBlank(getProvinceCode());
        }
        return true;
    }

    /***
     * 多资源属性 资源库对象
     * @return
     */
    @Transient
    public List<ResourcesSpaceData>getAttrResSpaceData(){
        List<ResourcesSpaceData>datas= new ArrayList<>();
        for(CmsFormDataAttrResEntity resEntity:getAttrRes()){
            datas.add(resEntity.getResourcesSpaceData());
        }
        return datas;
    }

    /***
     * 获取地址拼接(省市县详细地址)
     *
     * @Title: getAddress
     * @return: String
     */
    @Transient
    public String getAddress() {
        StringBuffer address = new StringBuffer("");
        if (getProvince() != null) {
            address.append(getProvince().getAreaName());
        }
        if (getCity() != null) {
            address.append(getCity().getAreaName());
        }
        if (getArea() != null) {
            address.append(getArea().getAreaName());
        }
        if(StringUtils.isNotBlank(getAttrValue())){
            address.append(getAttrValue());
        }
        String addressStr = address.toString();
        if("null".equals(addressStr.toLowerCase())){
            addressStr="";
        }
        return addressStr;
    }


    @Transient
    public String getValue() {
        return value;
    }

    /**
     * 获取除多资源外的 属性值（单资源获取id，城市、地址类型获取省份代码）
     * @return
     */
    @Transient
    public String getAttrValueForType() {
        if(CmsFormConstant.FIELD_ATTR_FILE_PROP_ADDRESS.equals(getAttrType())){
            return getProvinceCode();
        }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_CITY.equals(getAttrType())){
            return getProvinceCode();
        }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_RES.equals(getAttrType())){
            return getDataId().toString();
        }
        return getAttrValue();
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取省市拼接(省市)
     *
     * @Title: getCityArea
     * @return: String
     */
    @Transient
    public String getCityArea() {
        StringBuffer address = new StringBuffer("");
        if (getProvince() != null) {
            address.append(getProvince().getAreaName());
        }
        if (getCity() != null) {
            address.append(getCity().getAreaName());
        }
        return address.toString();
    }

    /**
     * 获取资源地址
     *
     * @Title: getResUrl
     * @return: String
     */
    @Transient
    public String getResUrl() {
        if (getResourcesSpaceData() != null) {
            return getResourcesSpaceData().getUrl();
        }
        return null;
    }

    /**兼容之前v9调用方式*/
    @Transient
    public String getPath() {
        return getResUrl();
    }

    /**兼容之前v9调用方式*/
    @Transient
    public String getName() {
        return getResAlias();
    }
    /**
     * 获取资源名称
     *
     * @Title: getResAlias
     * @return: String
     */
    @Transient
    public String getResAlias() {
        if (getResourcesSpaceData() != null) {
            return getResourcesSpaceData().getAlias();
        }
        return null;
    }

    /**
     * 获取资源时长(时间秒)
     *
     * @Title: getResDuration
     * @return: String
     */
    @Transient
    public String getResDuration() {
        if (getResourcesSpaceData() != null) {
            return getResourcesSpaceData().getDuration();
        }
        return "00:00:00";
    }

    @Transient
    public String getResSize() {
        Integer size = 0;
        if(getResourcesSpaceData() != null ) {
                size += getResourcesSpaceData().getSize();
        }
        // 定义GB的计算常量
        int gb = 1024 * 1024;
        // 定义MB的计算常量
        int mb = 1024;
        // 格式化小数
        DecimalFormat df = new DecimalFormat("0.00");
        String resultSize;
        if (0 == size) {
            resultSize = "1KB";
        } else if (size / gb >= 1) {
            // 如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) gb) + "GB";
        } else if (size / mb >= 1) {
            // 如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) mb) + "MB";
        } else {
            resultSize = size + "KB";
        }
        return resultSize;
    }

    /**
     * 获取省级名称
     *
     * @return 省级名称
     */
    @Transient
    public String getProvinceName() {
        return getProvince() != null ? getProvince().getAreaName() : "";
    }

    /**
     * 获取城市名称
     *
     * @return 城市名称
     */
    @Transient
    public String getCityName() {
        return getCity() != null ? getCity().getAreaName() : "";
    }

    /**
     * 获取区级名称
     *
     * @return 区级名称
     */
    @Transient
    public String getAreaName() {
        return getArea() != null ? getArea().getAreaName() : "";
    }

    /**
     * 获取资源对象别名
     *
     * @return 资源对象别名
     */
    @Transient
    public String getAlias() {
        return getResourcesSpaceData() != null ? getResourcesSpaceData().getAlias() : "";
    }

    /**
     * 获取资源对象类型（1图片 2视频 3音频 4附件)）
     *
     * @return 资源对象类型
     */
    @Transient
    public Short getResourceType() {
        return getResourcesSpaceData() != null ? getResourcesSpaceData().getResourceType() : null;
    }

    /**
     * 获取资源对象大小
     *
     * @return 资源对象大小
     */
    @Transient
    public String getSizeUnit() {
        return getResourcesSpaceData() != null ? getResourcesSpaceData().getSizeUnit() : "";
    }

    /**
     * 获取资源对象地址
     *
     * @return 资源对象地址
     */
    @Transient
    public String getUrl() {
        return getResourcesSpaceData() != null ? getResourcesSpaceData().getUrl() : "";
    }

    @Id
    @Column(name = "data_attr_id")
    @TableGenerator(name = "jc_ex_form_data_attr", pkColumnValue = "jc_ex_form_data_attr", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_ex_form_data_attr")
    public Integer getId() {
        return id;
    }

    public void setId(Integer leAttrId) {
        this.id = leAttrId;
    }

    @Basic
    @Column(name = "data_id", nullable = false)
    public Integer getDataId() {
        return dataId;
    }

    public void setDataId(Integer letterId) {
        this.dataId = letterId;
    }


    @Basic
    @Column(name = "attr_name", nullable = false, length = 150)
    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    @Basic
    @Column(name = "attr_value", nullable = true, length = 1500)
    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    @Basic
    @Column(name = "attr_type", nullable = false)
    public Short getAttrType() {
        return attrType;
    }

    public void setAttrType(Short attrType) {
        this.attrType = attrType;
    }

    @Basic
    @Column(name = "res_id", nullable = true)
    public Integer getResId() {
        return resId;
    }

    public void setResId(Integer resId) {
        this.resId = resId;
    }

    @Basic
    @Column(name = "province_code", nullable = true, length = 50)
    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Basic
    @Column(name = "city_code", nullable = true, length = 50)
    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    @Basic
    @Column(name = "area_code", nullable = true, length = 50)
    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    @Basic
    @Column(name = "pdf_path", nullable = true, length = 255)
    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_id", insertable = false, updatable = false)
    public CmsFormDataEntity getData() {
        return data;
    }

    public void setData(CmsFormDataEntity data) {
        this.data = data;
    }


    @Basic
    @Column(name = "cover_id", nullable = true, length =11)
    public Integer getCoverImgId() {
        return coverImgId;
    }

    public void setCoverImgId(Integer coverImgId) {
        this.coverImgId = coverImgId;
    }

    @Basic
    @Column(name = "other_input", nullable = true, length =200)
    public String getOtherInput() {
        return otherInput;
    }

    public void setOtherInput(String otherInput) {
        this.otherInput = otherInput;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_id", insertable = false, updatable = false)
    public ResourcesSpaceData getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(ResourcesSpaceData coverImg) {
        this.coverImg = coverImg;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_id", insertable = false, updatable = false)
    public ResourcesSpaceData getResourcesSpaceData() {
        return resourcesSpaceData;
    }

    public void setResourcesSpaceData(ResourcesSpaceData resourcesSpaceData) {
        this.resourcesSpaceData = resourcesSpaceData;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "area_code", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @OneToMany(mappedBy = "attr", fetch = FetchType.LAZY,cascade = CascadeType.REMOVE)
    public List<CmsFormDataAttrResEntity> getAttrRes() {
        return attrRes;
    }

    public void setAttrRes(List<CmsFormDataAttrResEntity> attrRes) {
        this.attrRes = attrRes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CmsFormDataAttrEntity that = (CmsFormDataAttrEntity) o;
        return id .equals(that.id ) &&
                dataId .equals(that.dataId) &&
                attrType .equals(that.attrType) &&
                Objects.equals(attrName, that.attrName) &&
                Objects.equals(attrValue, that.attrValue) &&
                Objects.equals(resId, that.resId) &&
                Objects.equals(provinceCode, that.provinceCode) &&
                Objects.equals(cityCode, that.cityCode) &&
                Objects.equals(areaCode, that.areaCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataId, attrName, attrValue, attrType, resId, provinceCode, cityCode, areaCode);
    }
}
