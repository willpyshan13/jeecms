/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.resource.domain.dto;

import java.util.List;

/**
 * 资源库分享Dto
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/5/24 11:54:25
 */

public class ResourcesSpaceShareDto {

	private Integer id;

	private Integer[] ids;
	/**
	 * 组织id数组
	 */
	private List<Integer> orgIds;
	/**
	 * 角色id数组
	 */
	private List<Integer> roleIds;
	/**
	 * 用户id数组
	 */
	private List<Integer> userIds;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer[] getIds() {
		return ids;
	}

	public void setIds(Integer[] id) {
		this.ids = id;
	}

	public List<Integer> getOrgIds() {
		return orgIds;
	}

	public void setOrgIds(List<Integer> orgIds) {
		this.orgIds = orgIds;
	}

	public List<Integer> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Integer> roleIds) {
		this.roleIds = roleIds;
	}

	public List<Integer> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Integer> userIds) {
		this.userIds = userIds;
	}
	
}
