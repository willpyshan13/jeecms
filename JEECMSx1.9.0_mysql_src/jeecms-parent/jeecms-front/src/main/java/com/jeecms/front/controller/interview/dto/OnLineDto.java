/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller.interview.dto;

import java.util.Calendar;
import java.util.Date;

/**
 * @author xiaohui
 * @version 1.2
 * @date 2020/6/4 15:28
 */
public class OnLineDto {
	private String cookie;
	private String username;
	private Integer userId;
	private Long onlineId;
	private Date time;

	public OnLineDto() {
		super();
	}

	public OnLineDto(String cookie, String username, Integer userId, Long onlineId) {
		this.cookie = cookie;
		this.username = username;
		this.userId = userId;
		this.onlineId = onlineId;
		this.time = Calendar.getInstance().getTime();
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Long getOnlineId() {
		return onlineId;
	}

	public void setOnlineId(Long onlineId) {
		this.onlineId = onlineId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
