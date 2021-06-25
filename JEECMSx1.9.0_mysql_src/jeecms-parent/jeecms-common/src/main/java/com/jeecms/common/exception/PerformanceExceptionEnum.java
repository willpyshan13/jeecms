package com.jeecms.common.exception;

/**
 * 绩效管理错误类
 * @author ljw
 */
public enum PerformanceExceptionEnum implements ExceptionInfo {

	/** 规则不能为空 */
	PERFORMANCE_RULE_ERROR("17001", "The rule cannot be null !"),
	/** 规则名称不能重复 */
	PERFORMANCE_RULE_NAME_ERROR("17002", "Rule names cannot be repeated !"),
	/** 评分规则对象已存在*/
	PERFORMANCE_RULE_OBJECT_ERROR("17003", "The scoring rule object already exists !"),
	/** 评分规则至少存在一条，不能为空*/
	PERFORMANCE_RULE_NOT_ONE_ERROR("17004", "There is at least one scoring rule that cannot be null !"),
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
	private Object data;

	/**
	 * Description: 根据异常的代码、默认提示信息构建一个异常信息对象。
	 *
	 * @param code
	 *            异常的代码。
	 *
	 * @param defaultMessage
	 *            异常的默认提示信息。
	 */
	PerformanceExceptionEnum(String code, String defaultMessage) {
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
