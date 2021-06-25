/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain;

import com.jeecms.common.base.domain.AbstractDomain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 访问记录
 *
 * @author ljw
 * @version 1.0
 * @date 2019-06-22
 */
@Entity
@Table(name = "jc_sys_access_record")
public class SysAccessRecord extends AbstractDomain<Integer> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	/** 是否登录访问（0-否 1-是） */
	private Boolean isLogin;
	/** 登录用户id */
	private Integer loginUserId;
	/** 登录用户名 */
	private String loginUserName;
	/** 会话标识 */
	private String sessionId;
	/** cookie标识 */
	private String cookieId;
	/** 站点id */
	private Integer siteId;
	/** 访问来源(1:PC  2:移动端H5  3:微信客户端H5 4:IOS 5:安卓 6:小程序) */
	private Short accessSourceClient;
	/** 访问网址 */
	private String accessUrl;
	/** 来源网址 */
	private String sourceUrl;
	/** 来源域名 */
	private String sourceDomain;
	/** 来源网站类型 （1-搜索引擎 2-外部链接 3-直接访问） */
	private Integer sorceUrlType;
	/** 访客ip */
	private String accessIp;
	/** 访客设备系统（如：Win10 Mac10 Android8） */
	private String accessDevice;
	/** 访客所属城市 */
	private String accessCity;
	/** 访客浏览器类型 */
	private String accessBrowser;
	/** 访客所属国家 */
	private String accessCountry;
	/** 访客所属省份 */
	private String accessProvince;
	/**是否新客户**/
	private Boolean newVisitor;
	/**搜索引擎名称**/
	private String engineName;
	/**设备类型1.计算机2.移动设备**/
	private Short deviceType;
	/**页面类型 1.首页 2.栏目页 3.内容页 4.其他页 **/
	private Integer pageType;
	/** 栏目ID **/
	private Integer channelId;
	/** 内容ID **/
	private Integer contentId;

	private Integer pageSize;

	private Integer accessTime;

	public SysAccessRecord() {
	}

	@Id
	@Column(name = "id", nullable = false, length = 11)
	@TableGenerator(name = "jc_sys_access_record", pkColumnValue = "jc_sys_access_record",
		initialValue = 1, allocationSize = 100)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_sys_access_record")
//	@GenericGenerator( name="jc_sys_access_record", strategy="enhanced-table",
//			parameters = {
//					@org.hibernate.annotations.Parameter( name = "table_name", value = "hibernate_sequences"),
//					@org.hibernate.annotations.Parameter( name ="value_column_name", value = "next_val"),
//					@org.hibernate.annotations.Parameter( name = "segment_column_name",value = "sequence_name"),
//					@org.hibernate.annotations.Parameter( name = "segment_value", value = "jc_sys_access_record"),
//					@org.hibernate.annotations.Parameter( name = "increment_size", value = "10"),
//					@org.hibernate.annotations.Parameter( name = "optimizer",value = "pooled-lo")
//			})
	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "is_login", nullable = false, length = 1)
	public Boolean getIsLogin() {
		return isLogin;
	}

	public void setIsLogin(Boolean isLogin) {
		this.isLogin = isLogin;
	}

	@Column(name = "login_user_id", nullable = true, length = 11)
	public Integer getLoginUserId() {
		return loginUserId;
	}

	public void setLoginUserId(Integer loginUserId) {
		this.loginUserId = loginUserId;
	}

	@Column(name = "login_user_name", nullable = true, length = 50)
	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

	@Column(name = "session_id", nullable = true, length = 50)
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Column(name = "cookie_id", nullable = true, length = 50)
	public String getCookieId() {
		return cookieId;
	}

	public void setCookieId(String cookieId) {
		this.cookieId = cookieId;
	}

	@Column(name = "site_id", nullable = true, length = 11)
	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	@Column(name = "access_source_client", nullable = true, length = 50)
	public Short getAccessSourceClient() {
		return accessSourceClient;
	}

	public void setAccessSourceClient(Short accessSourceClient) {
		this.accessSourceClient = accessSourceClient;
	}

	@Column(name = "access_url", nullable = false, length = 500)
	public String getAccessUrl() {
		return accessUrl;
	}

	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}

	@Column(name = "source_url", nullable = true, length = 500)
	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getSourceDomain() {
		return sourceDomain;
	}

	@Column(name = "source_domain", nullable = false, length = 255)
	public void setSourceDomain(String sourceDomain) {
		this.sourceDomain = sourceDomain;
	}

	@Column(name = "sorce_url_type", nullable = false, length = 6)
	public Integer getSorceUrlType() {
		return sorceUrlType;
	}

	public void setSorceUrlType(Integer sorceUrlType) {
		this.sorceUrlType = sorceUrlType;
	}

	@Column(name = "access_ip", nullable = true, length = 50)
	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	@Column(name = "access_device", nullable = true, length = 50)
	public String getAccessDevice() {
		return accessDevice;
	}

	public void setAccessDevice(String accessDevice) {
		this.accessDevice = accessDevice;
	}

	@Column(name = "access_city", nullable = true, length = 150)
	public String getAccessCity() {
		return accessCity;
	}

	public void setAccessCity(String accessCity) {
		this.accessCity = accessCity;
	}

	@Column(name = "access_browser", nullable = true, length = 150)
	public String getAccessBrowser() {
		return accessBrowser;
	}

	public void setAccessBrowser(String accessBrowser) {
		this.accessBrowser = accessBrowser;
	}

	@Column(name = "access_country", nullable = true, length = 150)
	public String getAccessCountry() {
		return accessCountry;
	}

	public void setAccessCountry(String accessCountry) {
		this.accessCountry = accessCountry;
	}

	@Column(name = "access_province", nullable = true, length = 150)
	public String getAccessProvince() {
		return accessProvince;
	}

	public void setAccessProvince(String accessProvince) {
		this.accessProvince = accessProvince;
	}

	@Column(name = "is_new_visitor", nullable = true, length = 1)
	public Boolean getNewVisitor() {
		return newVisitor;
	}

	public void setNewVisitor(Boolean newVisitor) {
		this.newVisitor = newVisitor;
	}

	@Column(name = "engine_name", nullable = false, length = 11)
	public String getEngineName() {
		return engineName;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	@Column(name = "device_type", nullable = false, length = 2)
	public Short getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(Short deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name = "page_type", nullable = false, length = 2)
	public Integer getPageType() {
		return pageType;
	}

	public void setPageType(Integer pageType) {
		this.pageType = pageType;
	}

	@Column(name = "channel_id", length = 50)
	public Integer getChannelId() {
		return channelId;
	}

	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}

	@Column(name = "content_id", length = 2)
	public Integer getContentId() {
		return contentId;
	}

	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	@Transient
	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	@Transient
	public Integer getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(Integer accessTime) {
		this.accessTime = accessTime;
	}

	/**
	 * 访问来源(1:PC 2:移动端H5 3:微信客户端H5 4:IOS 5:安卓 6:小程序)
	 */
	public static final Short ACCESS_TYPE_PC = 1;
	public static final Short ACCESS_TYPE_MOBILE_H5 = 2;
	public static final Short ACCESS_TYPE_WECHAT_H5 = 3;
	public static final Short ACCESS_TYPE_IOS = 4;
	public static final Short ACCESS_TYPE_ANDROID = 5;
	public static final Short ACCESS_TYPE_SMALL_PROGRAM = 6;

	/**来源网站类型 （1-搜索引擎 2-外部链接 3-直接访问）**/
	public static Integer RESOURCE_SEARCHER = 1;
	public static Integer RESOURCE_EXT = 2;
	public static Integer RESOURCE_SELF = 3;

	/**访客设备系统（如：Win10 Mac10 Android8）**/
	public static final String DEVICE_WINDOWS_10 = "Windows10";
	public static final String DEVICE_WINDOWS_8 = "Windows8";
	public static final String DEVICE_WINDOWS_7 = "Windows7";
	public static final String DEVICE_IPHONE_12 = "iPhone12";
	public static final String DEVICE_IPHONE_11 = "iPhone11";
	public static final String DEVICE_IPHONE_10 = "iPhone10";
	public static final String DEVICE_ANDROID_8  = "Android8";
	public static final String DEVICE_ANDROID_7  = "Android7";
	public static final String DEVICE_ANDROID_6  = "Android6";
	public static final String DEVICE_MAC = "Mac";
	public static final String OTHERS = "Others";

	/**计算机设备**/
	public static final Short COMPUTER = 1;
	/**移动设备**/
	public static final Short MOBIE = 2;

	/**浏览器类型**/
	public static final String BROWSER_EDGE = "Edge";
	public static final String BROWSER_SAFARI = "Safari";
	public static final String BROWSER_CHROME = "Chrome";
	public static final String BROWSER_FIREFOX = "Firefox";

}