package com.jeecms.auth.dto;/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 查询三元管理分页dto
 * @author: chenming
 * @date: 2020/5/22 9:09
 */
public class CoreSafeManageDto {
    /** 用户名和真名*/
    private String key;
    /** 组织id*/
    private Integer orgid;
    /** 角色id*/
    private Integer roleid;
    private Integer page;

    private Integer size;
    /** 需要排除的用户集合*/
    private List<Integer> notIds;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getOrgid() {
        return orgid;
    }

    public void setOrgid(Integer orgid) {
        this.orgid = orgid;
    }

    public Integer getRoleid() {
        return roleid;
    }

    public void setRoleid(Integer roleid) {
        this.roleid = roleid;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<Integer> getNotIds() {
        return notIds;
    }

    public void setNotIds(List<Integer> notIds) {
        this.notIds = notIds;
    }
}
