/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller.interview.dto;

/**
 * 提问Dto
 *
 * @author xiaohui
 * @version 1.2
 * @date 2020/4/22 15:37
 */
public class QuestionDto {
	/**
	 * 访谈id
	 */
	private Long onlineId;
	/**
	 * 内容
	 */
	private String content;
	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 验证码
	 */
	private String captcha;
	/**
	 * sessionId
	 */
	private String sessionId;

	public Long getOnlineId() {
		return onlineId;
	}

	public void setOnlineId(Long onlineId) {
		this.onlineId = onlineId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
