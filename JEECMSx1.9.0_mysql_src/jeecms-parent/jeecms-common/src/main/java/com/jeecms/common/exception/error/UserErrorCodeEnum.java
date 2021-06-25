package com.jeecms.common.exception.error;

import com.jeecms.common.exception.ExceptionInfo;

/**
 * 用户信息枚举 号段范围 14001~14500
 * 
 * @author: tom
 * @date: 2018年11月13日 上午9:54:44
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public enum UserErrorCodeEnum implements ExceptionInfo {

	/** 邮箱已经存在 */
	EMAIL_ALREADY_EXIST("14001", "邮箱已经存在!"),
	/** 电话号码已经存在 */
	PHONE_ALREADY_EXIST("14002", "电话号码已经存在!"),
	/** 等级会员名称已经存在 */
	MEMBERLEVEL_NAME_ALREADY_EXIST("14006", " 等级会员名称已经存在!"),
	/** 账户密码错误、默认提示信息。 */
	ACCOUNT_CREDENTIAL_ERROR("14008", " 账户密码错误 !"),
	/** 两次密码填写不一致 */
	PASSWORD_INCONSISTENT_ERROR("14009", "两次密码填写不一致"),
	/** 账户已经存在 **/
	USERNAME_ALREADY_EXIST("14010", "账户已经存在 !"),
	/** 验证码已经发送过 */
	VALIDATE_CODE_ALREADY_SEND("14011", "验证码已经发送过 !"),
	/** 验证码已经超出次数(发送) */
	VALIDATE_CODE_EXCEEDCOUNT("14012", "验证码已经超出次数!"),
	/** 验证码未通过 */
	VALIDATE_CODE_UNTHROUGH("14013", "验证码未通过!"),
	/** 验证码不合法 */
	VALIDATE_CODE_ILLEGAL("14014", "验证码不合法!"),
	/** 邮箱地址无效 */
	EMAIL_INVALID("14015", "邮箱地址无效!"),
	/** 电话号码无效 */
	PHONE_INVALID("14016", "电话号码无效!"),
	/** 手机号未绑定会员 */
	PHONE_UNBOUND_MEMBER("14017", "手机号未绑定会员!"),
	/** 邮箱未绑定会员 */
	EMAIL_UNBOUND_MEMBER("14018", "邮箱未绑定会员!"),
	/** 组织不可为空 */
	ORG_CANNOT_EMPTY("14019", "组织不可为空!"),
	/** 非法操作 */
	ILLEGAL_OPERATION("14020", "非法操作!"),
	/** 组织名称不可为空 */
	ORGNAME_CANNOT_EMPTY("14021", "组织名称不可为空!"),
	/** 密码格式不正确 */
	PASSWORD_FORMAT_IS_INCORRECT("14022", "密码格式不正确!"),
	/** 存在不可操作数据 **/
	ALREADY_DATA_NOT_OPERATION("14023", "存在不可操作数据 "),
	/** 组织名称已经存在 */
	ORGNAME_ALREADY_EXIST("14024", "组织名称已经存在 !"),
	/** 用户必须是管理员*/
	USER_MUST_BE_AN_ADMINISTRATOR("14025", "用户必须是管理员"),
	/** 邮箱格式错误*/
	EMAIL_FORM_ERROR("14026","邮箱格式错误"),
	/** 短信格式错误*/
	SMS_FORM_ERROR("14027","短信格式错误"),
	/** 当前用户无权操作公众号、小程序及授权微博账号*/
	NO_OPERATE_WECHAT_PERMISSION("14028","当前用户无权操作公众号、小程序及授权微博账号"),
	/** 用户已绑定第三方信息 */
	USER_ALREADY_BINDED_THIEDPARTY("14029", "用户已绑定第三方信息!"),
	/** 绑定第三方失败 */
	THIRDPARTY_BINDING_FAIL("14030", "绑定第三方失败!"),
	/** 未找到第三方账号信息 */
	THIRDPARTY_INFO_NOTFOUND("14031", "未找到第三方账号信息!"),
	/** 未成功绑定第三方账号，请绑定后重试*/
	THIRDPARTY_INFO_UNSUCCESSFUL_BINDING("14032", "未成功绑定第三方账号，请绑定后重试 "),
	/** 该内容不允许评论*/
	THE_CONTENT_NOT_COMMENT("14033","该内容不允许评论!"),
	/** 该内容不允许游客评论*/
	THE_CONTENT_NOT_TOURIST_REVIEWS("14034","该内容不允许游客评论!"),
	/** 传入的内容错误*/
	INCOMING_CONTENT_ERROR("14035","传入的内容错误!"),
	/** 该评论包含敏感词无法发布*/
	THE_COMMENT_CONTAIN_SENSITIVE_WORD("14036","评论包含敏感词无法发布!"),
	/** 该评论无法继续发送*/
	THE_COMMENT_UNABLE_TO_SEND("14037","评论无法继续发送!"),
	/** 该评论包含链接无法发布*/
	THE_COMMENT_CONTAIN_LINK("14038","该评论包含链接无法发布!"),
	/** 系统检测您为后台管理员，请到后台修改密码*/
	MOVE_TO_BACKGROUND_UPDATE("14039", "系统检测您为后台管理员，请到后台修改密码"),
	/** 该用户不允许评论*/
	THE_USER_NOT_COMMENT("14040","该用户不允许评论!"),
	/** 该ip不允许评论*/
	THE_IP_NOT_COMMENT("14041","该ip不允许评论!"),
	/** 用户未登录*/
	THE_USER_NOT_LOGIN("14042","用户未登录!"),
	/** 新密码不能与原密码相同*/
	PASSWORD_SAME_OLD_ERROR("14043", "新密码不能与原密码相同 !"),
	/** 密码重置无效，不能重置自身的密码*/
	PASSWORD_ERROR_RESET_MYSELF("14044", "密码重置无效，不能重置自身的密码!"),
	/** 传入的微信、小程序错误*/
	INCOMING_WECHAT_ERROR("14045","传入的微信、小程序错误"),
	/** 父级组织不存在*/
	PARENT_ORG_NOT_EXIST("14046","父级组织不存在"),
	/** 会员组不存在*/
	MEMBER_GROUP_NOT_EXIST("14047","会员组不存在"),
	/** 会员等级不存在*/
	MEMBER_LEVEL_NOT_EXIST("14048","会员等级不存在"),
	/** 角色不存在*/
	ROLE_NOT_EXIST("14049","角色不存在"),
	/** 组织不存在*/
	ORG_NOT_EXIST("14050","组织不存在"),
	/** 子结点不能操作为父结点*/
	ORG_CHILD_NOT_PARENT_FOR_SELF("14051","子结点不能操作为父结点"),
	;
	
	/** 异常代码。 */
	private String code;

	/** 异常对应的默认提示信息。 */
	private String defaultMessage;

	/** 异常对应的原始提示信息。 */
	private String originalMessage;

	/** 当前请求的URL。 */
	private String requestUrl;

	/** 需转向（重定向）的URL，默认为空。 */
	private String defaultRedirectUrl = "";

	/** 异常对应的响应数据。 */
	private Object data = new Object();

	/**
	 * Description: 根据异常的代码、默认提示信息构建一个异常信息对象。
	 *
	 * @param code           异常的代码。
	 * 
	 * @param defaultMessage 异常的默认提示信息。
	 */
	UserErrorCodeEnum(String code, String defaultMessage) {
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getDefaultMessage() {
		return defaultMessage;
	}

	@Override
	public String getOriginalMessage() {
		return originalMessage;
	}

	@Override
	public void setOriginalMessage(String originalMessage) {
		this.originalMessage = originalMessage;
	}

	@Override
	public String getRequestUrl() {
		return requestUrl;
	}

	@Override
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	@Override
	public String getDefaultRedirectUrl() {
		return defaultRedirectUrl;
	}

	@Override
	public void setDefaultRedirectUrl(String defaultRedirectUrl) {
		this.defaultRedirectUrl = defaultRedirectUrl;
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

}
