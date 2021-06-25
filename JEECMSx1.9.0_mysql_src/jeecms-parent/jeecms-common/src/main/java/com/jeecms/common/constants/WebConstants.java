package com.jeecms.common.constants;

/**
 * web常量
 *
 * @author: tom
 * @date: 2018年12月10日 下午3:20:51
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class WebConstants {
    /**
     * false字符串
     */
    public static final String FALSE = "false";
    /**
     * true字符串
     */
    public static final String TRUE = "true";

    public static final String PROTOCOL_HTTP = "http://";

    public static final String PROTOCOL_HTTPS = "https://";

    /**
     * 路径分隔符
     */
    public static final String SPT = "/";
    /**
     * 内网路径前缀
     */
    public static final String INTRANET_PREFIX = "/c/";
    /**
     * 数组分隔符
     */
    public static final String ARRAY_SPT = ",";
    /**
     * 销售属性分隔符
     */
    public static final String SALE_ATTR_SPT = "-";
    /**
     * 首页
     */
    public static final String INDEX = "index";
    /**
     * 手机首页
     */
    public static final String INDEX_MOBILE = "indexMobile";
    /**
     * 首页模板
     */
    public static final String INDEX_HTML = "/index.html";
    /**
     * 手机首页模板
     */
    public static final String INDEX_HTML_MOBILE = "/indexMobile.html";
    /**
     * 默认模板
     */
    public static final String DEFAULT = "default";
    /**
     * 静态页文件后缀html
     */
    public static final String STATIC_SUFFIX_HTML = ".html";
    /**
     * 静态页文件后缀shtml
     */
    public static final String STATIC_SUFFIX_SHTML = ".shtml";
    /**
     * 动态地址后缀 htm
     */
    public static final String DYNAMIC_SUFFIX = ".htm";
    /**
     * 动态地址后缀栏目内容通用动态后缀 jhtml
     */
    public static final String DYNAMIC_CONTENT_SUFFIX = ".jhtml";
    /**
     * 静态页目录 pc端文件夹
     */
    public static final String STATIC_PC_PATH = "p";
    /**
     * 静态页目录 手机端文件夹
     */
    public static final String STATIC_MOBILE_PATH = "m";
    /**
     * 正则匹配静态化 /p前缀
     */
    public static final String REG_STATIC_PC_PATH = "^/p([0-9]+)/.*";
    /**
     * 正则匹配静态化 /m前缀
     */
    public static final String REG_STATIC_MOBILE_PATH = "^/m([0-9]+)/.*";
    /**
     * UTF-8编码
     */
    public static final String UTF8 = "UTF-8";
    /**
     * 提示信息
     */
    public static final String MESSAGE = "message";
    /**
     * cookie中的JSESSIONID名称
     */
    public static final String JSESSION_COOKIE = "JSESSIONID";
    /**
     * cookie中的用户标识
     */
    public static final String IDENTITY_COOKIE = "JIDENTITY";
    /**
     * url中的jsessionid名称
     */
    public static final String JSESSION_URL = "jsessionid";

    /**
     * 微信openid
     */
    public static final String WX_OPENID = "wxopenId";
    /**
     * HTTP POST请求
     */
    public static final String POST = "POST";
    /**
     * HTTP GET请求
     */
    public static final String GET = "GET";

    /**
     * HTTP OPTIONS请求
     */
    public static final String OPTIONS = "OPTIONS";
    /**
     * 国际化 英文
     */
    public static final String LAN_EN_US = "en_US";
    /**
     * 国际化 中文简体
     */
    public static final String LAN_ZH_CN = "zh_CN";
    /**
     * 国际化 中文繁体
     */
    public static final String LAN_ZH_TW = "zh_TW";
    /**
     * 上传路径
     */
    public static final String UPLOAD_PATH = "/u/cms/";

    /**
     * 在线访谈资源目录
     */
    public static final String INTERVIEW_PATH = "/interview/";
    /**
     * 系统默认资源路径
     */
    public static final String SYS_RESOURCE_PATH = "/resource/";
    /**
     * 上传文档文库路径
     */
    public static final String UPLOAD_WENKU_PATH = "/wenku/";
    /**
     * 全文检索索引路径
     */
    public static final String LUCENE_PATH = "/WEB-INF/lucene";
    /**
     * 全文检索栏目分类索引路径
     */
    public static final String LUCENE_TOXONOMY_PATH = "/WEB-INF/taxonomyIndex";
    /**
     * 页面禁止访问
     */
    public static final String ERROR_403 = "error/403";

    /**
     * 通用参数字符串前缀
     */
    public static final String QUERY_PARAM_PREFIX = "query_";

    /**
     * 验证码key前缀
     */
    public static final String KCAPTCHA_PREFIX = "kcaptcha_";

    /**
     * 请求开始时间标识
     */
    public static final String LOGGER_SEND_TIME = "_send_time";

    public static final String ADMIN_PREFIX = "/cmsmanager";
    public static final String MEMBER_PREFIX = "/member";
    public static final String COMMON_PREFIX = "/common";
    public static final String LOGIN_URL = "/login";
    /**
     * 手机端登录地址
     */
    public static final String MOBILE_LOGIN_URL = "/h5center/index.html#/pages/login/login";
    public static final String MOBILE_LOGIN_URL_PREFIX = "/h5center/index.html";
    public static final String LOGOUT_URL = "/logout";
    public static final String GLOBAL_GET_URL = "/globalInfo/get";

    public static final String PREVIEW_URL = "preview";
    public static final String SITE_CLOSE = "/close";

    public static final String ADMIN_URL = "jeecms/index.html";

    public static final String SEARCH_PREFIX = "/search";

    public static final String ERROR_URL = "/error";

    public static final String TEMPLATE_URL = "/cmsmanager/template";

    public static final String RESOURCE_URL = "/cmsmanager/resource";

    public static final String CONTENT_URL = "/cmsmanager/content";


    public static final String ERROR_404 = "/error/404";

    public static final String TPL_404 = "/tpl/404";

    public static final String ERROR_500 = "/error/500";
    /**
     * 单点登录同步地址
     **/
    public static final String JEESSO_SYNC = "/sso/sync";
    /**
     * 单点登录修改地址
     **/
    public static final String JEESSO_UPDATE = "/sso/update";
    /**
     * 单点登录删除地址
     **/
    public static final String JEESSO_DELETE = "/sso/delete";
    /**
     * 单点登录状态
     **/
    public static final String JEESSO_STATUS = "/sso/status";
    /**
     * 单点登录获取用户信息地址
     **/
    public static final String JEESSO_GETINFO = "/sso/getInfo";
    /**
     * 获取是否国密加密方式地址
     **/
    public static final String GET_CONFIG_IS_SM_ENCRYPT = "/config/global/isSmEncrypt";
    /**
     * 获取是否开启双因子
     **/
    public static final String OPEN_ELEMENT = "/config/element/status";
    /**
     * 发送双因子登录验证
     **/
    public static final String ELEMENT_VALID = "/config/element/sendLogin";
    /**
     * 发送产品信息
     **/
    public static final String PRODUCT_APP_INFO = "/authorizations/product";

    /**
     * 分级保护
     */
    public static final String CLASSIFIED_PROTECTION = "/reinsurance/isShow";

    /**
     * 前台没有权限地址
     */
    public static final String FRONT_NO_PERM = "common-errorauth.htm";

    /**
     * cookie记住我
     */
    public static final String COOKIE_REMEMBER_ME = "rememberMe";
    /**
     * cookie预览设备
     */
    public static final String COOKIE_PREVIEW_DEVICE = "preview-device";
    /**
     * freemarker模板类型 FTP
     */
    public static final String FREEMARKER_RES_TYPE = "ftp";

    /**
     * 预览类型站点
     */
    public static final String PREVIEW_TYPE_SITE = "site";

    /**
     * 预览类型内容
     */
    public static final String PREVIEW_TYPE_CONTENT = "content";

    /**
     * 预览类型栏目
     */
    public static final String PREVIEW_TYPE_CHANNEL = "channel";
    /**
     * 预览类型投票问卷
     */
    public static final String PREVIEW_TYPE_QUESTIONNAIRE = "questionnaire";
    /**
     * 预览类型表单问卷
     */
    public static final String PREVIEW_TYPE_FORM = "form";
    /**
     * 预览类型领导信箱
     */
    public static final String PREVIEW_TYPE_LETTER = "letter";
    /**
     * 预览设备 手机
     */
    public static final String PREVIEW_DEVICE_MOBILE = "mobile";
    /**
     * 预览设备 PC
     */
    public static final String PREVIEW_DEVICE_PC = "pc";
    /**
     * 预览设备 平板
     */
    public static final String PREVIEW_DEVICE_TABLET = "tablet";

    /**
     * 匿名用户
     */
    public static final String ANONYMOUSUSER = "anonymousUser";

    /**
     * sso
     */
    public static final String SSO_OPEN = "ssoOpen";

    /**
     * 微信用户openid session key
     */
    public static final String WECHAT_OPEN_ID = "wechat_open_id";

    /**
     * 回放操作(使用三元管理)
     */
    public static final String REPLAY_OPERATING = "replayOperating";

    /**
     * 操作用户(提交三员管理员)名
     */
    public static final String OPERATING_USER_NAME = "operatingUserName";

    /**
     * 操作用户(提交三员管理员)
     */
    public static final String REQUEST_USER = "requestUser";

    /**
     * request的POST请求
     */
    public static final String REQUEST_POST = "POST";

    /**
     * request的DELETE请求
     */
    public static final String REQUEST_DELETE = "DELETE";

    /**
     * request的PUT请求
     */
    public static final String REQUEST_PUT = "PUT";

    /**
     * 权限管理状态：1-组织权限管理、2-角色权限管理、3-用户权限管理
     */
    public static final String DATA_PERM_STATUS = "dataPermStatus";

    /**
     * 三员管理修改状态
     * 1-修改状态
     * 2-变更三元角色
     * 3-修改应用模块
     * 4-修改状态+变更三元角色
     * 5-修改状态+修改应用模块
     * 6-变更三元角色+修改应用模块
     */
    public static final String SAFE_MANAGE_STATUS = "safeManageStatus";

    /**
     * response请求的code标识
     */
    public static final String RESPONSE_CODE_MARK = "code";

    /**
     * response请求的data标识
     */
    public static final String RESPONSE_DATA_MARK = "data";

    /**
     * response请求的正确的code
     */
    public static final int RESPONSE_CODE_CORRECT = 200;

    /**
     * 请求域名
     */
    public interface DoMain {
        /**
         * 云平台请求域名
         */
        String PLATFORM_URL = "http://api.jeecms.com";
        /**
         * 请求云平台是否授权
         **/
        String IF_AUTH_SERVER_URL = PLATFORM_URL.concat("/MODULE-APP/client/v1/userClient");
        /**
         * 同步子站的消息模板
         */
        String SYNCH_CHILD_SITE_TPL = PLATFORM_URL.concat("/MODULE-TINY-SERVE-COLLECT/client/v1/sms/template/sync");
        /**
         * 获取手机模板集合
         */
        String GET_PHONE_TPL_LIST = PLATFORM_URL.concat("/MODULE-TINY-SERVE-COLLECT/client/v1/sms/template/list");
        /**
         * 校验是否拥有短信余量
         */
        String CHECK_HAVE_PHONE_NUM = PLATFORM_URL.concat("/MODULE-APP/client/v1/balance/check");
        /**
         * 发送短信功能
         */
        String SEND_SMS = PLATFORM_URL.concat("/MODULE-TINY-SERVE-COLLECT/client/v1/sms/send");
    }

    /**
     * 字符串常量：true
     */
    public static final String STRING_TRUE = "true";
    /**
     * 字符串常量：false
     */
    public static final String STRING_FALSE = "false";


    public static final int INTEGER_TRUE = 1;
    public static final int INTEGER_FALSE = 0;



}
