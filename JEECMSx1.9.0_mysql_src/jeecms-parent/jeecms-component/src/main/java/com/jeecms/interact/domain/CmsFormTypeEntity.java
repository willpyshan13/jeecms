package com.jeecms.interact.domain;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.domain.AbstractSortDomain;
import com.jeecms.system.domain.CmsSite;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author: tom
 * @date: 2020/2/13 9:53   
 */
@Entity
@Table(name = "jc_ex_form_type")
public class CmsFormTypeEntity extends AbstractSortDomain<Integer> {
    private Integer id;
    private String name;
    private Integer siteId;
    /**排序权重*/
    private Integer sortWeight;
    private CmsSite site;

    private List<CmsFormEntity> forms;

    public void init(){
        if(getSortWeight()==null){
            setSortWeight(10);
        }
        if(getSortNum()==null){
            setSortNum(10);
        }
    }

    @Id
    @Column(name = "type_id")
    @TableGenerator(name = "jc_ex_form_type", pkColumnValue = "jc_ex_form_type", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_ex_form_type")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "sort_weight", length = 11)
    public Integer getSortWeight() {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight) {
        this.sortWeight = sortWeight;
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
        CmsFormTypeEntity that = (CmsFormTypeEntity) o;
        return id.equals(that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(updateUser, that.updateUser);
    }

    @OneToMany(mappedBy = "formType")
    @Where(clause = " deleted_flag=false ")
    public List<CmsFormEntity> getForms() {
        return forms;
    }

    public void setForms(List<CmsFormEntity> forms) {
        this.forms = forms;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createTime, updateTime, createUser, updateUser);
    }
}
