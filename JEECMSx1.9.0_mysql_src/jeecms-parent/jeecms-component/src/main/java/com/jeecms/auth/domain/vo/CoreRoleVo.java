package com.jeecms.auth.domain.vo;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * 角色vo
 * @author: tom
 * @date: 2020/8/21 17:27   
 */
public class CoreRoleVo {
    /** 唯一标识符 **/
    private int id;
    /** 角色名称 **/
    private String roleName;
    String orgName;
    /** 创建时间 */
    Date createTime ;
    /** 创建用户名 */
    String createUser ;
    Boolean notCurrUserRole;
    boolean deleteAble;
    boolean managerAble;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Boolean getNotCurrUserRole() {
        return notCurrUserRole;
    }

    public void setNotCurrUserRole(Boolean notCurrUserRole) {
        this.notCurrUserRole = notCurrUserRole;
    }

    public boolean isDeleteAble() {
        return deleteAble;
    }

    public void setDeleteAble(boolean deleteAble) {
        this.deleteAble = deleteAble;
    }

    public boolean isManagerAble() {
        return managerAble;
    }

    public void setManagerAble(boolean managerAble) {
        this.managerAble = managerAble;
    }
}
