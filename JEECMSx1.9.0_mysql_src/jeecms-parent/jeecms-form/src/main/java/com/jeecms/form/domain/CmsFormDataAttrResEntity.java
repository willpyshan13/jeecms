package com.jeecms.form.domain;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.domain.AbstractIdDomain;
import com.jeecms.resource.domain.ResourcesSpaceData;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author: tom
 * @date: 2020/2/13 16:39
 */
@Entity
@Table(name = "jc_ex_form_data_attr_res")
public class CmsFormDataAttrResEntity extends AbstractIdDomain implements Serializable {
    private static final long serialVersionUID = 3529324469666827352L;
    private Integer id;
    private Integer resId;
    private Integer attrId;
    private String resDesc;
    private String pdfPath;

    private CmsFormDataAttrEntity attr;
    private ResourcesSpaceData resourcesSpaceData;

    /**
     * 获取资源对象别名
     *
     * @return 资源对象别名
     */
    @Transient
    public String getAlias() {
        return getResourcesSpaceData() != null ? getResourcesSpaceData().getAlias() : "";
    }

    @Transient
    public String getResAlias() {
        return getAlias();
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
    @Column(name = "data_attr_res_id")
    @TableGenerator(name = "jc_ex_form_data_attr_res", pkColumnValue = "jc_ex_form_data_attr_res", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_ex_form_data_attr_res")
    public Integer getId() {
        return id;
    }

    public void setId(Integer leAttrId) {
        this.id = leAttrId;
    }

    @Basic
    @Column(name = "res_desc")
    public String getResDesc() {
        return resDesc;
    }

    public void setResDesc(String resDesc) {
        this.resDesc = resDesc;
    }

    @Basic
    @Column(name = "res_id", nullable = false)
    public Integer getResId() {
        return resId;
    }

    public void setResId(Integer resId) {
        this.resId = resId;
    }

    @Basic
    @Column(name = "pdf_path", nullable = true, length = 255)
    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    @Basic
    @Column(name = "data_attr_id", nullable = false)
    public Integer getAttrId() {
        return attrId;
    }

    public void setAttrId(Integer attrId) {
        this.attrId = attrId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_attr_id", insertable = false, updatable = false)
    public CmsFormDataAttrEntity getAttr() {
        return attr;
    }

    public void setAttr(CmsFormDataAttrEntity attr) {
        this.attr = attr;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_id", insertable = false, updatable = false)
    public ResourcesSpaceData getResourcesSpaceData() {
        return resourcesSpaceData;
    }

    public void setResourcesSpaceData(ResourcesSpaceData resourcesSpaceData) {
        this.resourcesSpaceData = resourcesSpaceData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CmsFormDataAttrResEntity that = (CmsFormDataAttrResEntity) o;
        return id  .equals(that.id) &&
                resId .equals(that.resId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resId);
    }
}
