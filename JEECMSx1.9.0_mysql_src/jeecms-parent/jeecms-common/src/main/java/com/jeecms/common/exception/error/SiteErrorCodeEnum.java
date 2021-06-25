package com.jeecms.common.exception.error;

import com.jeecms.common.exception.ExceptionInfo;

/**
 * 站点类错误码枚举 号段范围 11501~12000
 * @author: tom
 * @date:   2018年11月6日 下午6:53:45     
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public enum SiteErrorCodeEnum implements ExceptionInfo {
	/** 跨站点数据错误、默认提示信息。 */
	CROSS_SITE_DATA_ERROR("11501", "跨站点数据错误 !"),
	/**复制模板资源错误*/
	COPY_TEMPLATE_RES_ERROR("11502", "复制模板资源错误!"),
	/**站点资源目录重复或者为空错误*/
	SITE_PATH_ERROR("11503", "站点资源目录重复或者为空错误!"),
	
	/**站点域名重复或者为空错误*/
	SITE_DOMAIN_ERROR("11504", "站点域名重复或者为空错误!"),
	/**没有该站点修改权限*/
	NO_PERMISSION_MODIFY_SITE_ERROR("11505", "没有该站点修改权限!"),
	/**没有该站点删除权限*/
	NO_PERMISSION_DEL_SITE_ERROR("11506", "没有该站点删除权限!"),
	/**没有该站点查看权限*/
	NO_PERMISSION_VIEW_SITE_ERROR("11507", "没有该站点查看权限!"),
	/**没有该站点新建子站点权限*/
	NO_PERMISSION_NEW_CHILD_SITE_ERROR("11508", "没有该站点新建子站点权限!"),
	/**没有该站点开启关闭权限*/
	NO_PERMISSION_OPEN_CLOSE_SITE_ERROR("11509", "没有该站点开启关闭权限!"),
	/**没有该站点分配权限*/
	NO_PERMISSION_PERM_ASSIGN_SITE_ERROR("11510", "没有该站点分配权限!"),
	/**没有该站点静态化权限*/
	NO_PERMISSION_STATIC_SITE_ERROR("11511", "没有该站点静态化权限*!"),
	/**父级站点不能为空**/
	SITE_PARENTID_NOT_EMPLY("11518","父级站点不能为空"),
	/** 站群推送秘钥错误*/
	PUSH_SECRET_ERROR("11512","站群推送秘钥错误"),
	/** 点赞需要登录*/
	LIKE_TO_LOG_IN("11513", "点赞需要登录"),
	/** FTP连接错误 */
	FTP_CONNECTION_ERROR("11514", "FTP连接错误"),
	/** 站点无法推送内容*/
	SITE_CANNOT_PUSH_CONTENT("11515", "站点无法推送内容"),
	/** 顶级站点不能删除*/
	TOP_SITE_CANNOT_DELETE("11516", "顶级站点不能删除"),
	/** 子站点不在当前站点之下*/
	CHILD_SITE_IS_NOT_UNDER_THE_CURRENT_SITE("11518","Child site is not under the current site!"),
	PARENT_SITE_ALREADY_DELETE("11517", "父级站点已删除"),
	/** 子结点不能操作为父结点*/
	ORG_CHILD_NOT_PARENT_FOR_SELF("11518","子结点不能操作为父结点"),
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
	 * @param code
	 *            异常的代码。
	 * 
	 * @param defaultMessage
	 *            异常的默认提示信息。
	 */
	SiteErrorCodeEnum(String code, String defaultMessage) {
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
