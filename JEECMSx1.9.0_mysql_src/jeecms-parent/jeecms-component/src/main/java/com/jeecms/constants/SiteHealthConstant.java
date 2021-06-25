package com.jeecms.constants;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.constants.WebConstants;

/**
 * 站点监控-常量
 *
 * @author: tom
 * @date: 2020/3/5 14:54
 */
public class SiteHealthConstant {
    public static final Double SCORE_DEF = 20d;
    public static final Double SCORE_DEF_MAX = 100d;
    /**
     * 扣分值
     */
    public static final Double SCORE_DEDUCE_01 = 0.1d;
    public static final Double SCORE_DEDUCE_05 = 0.5d;
    public static final Double SCORE_DEDUCE_1 = 1d;
    public static final Double SCORE_DEDUCE_3 = 3d;
    public static final Double SCORE_DEDUCE_5 = 5d;
    public static final Double SCORE_DEDUCE_10 = 10d;

    public static final String MAP_KEY_LINK_TOTAL = "linkTotal";
    public static final String MAP_KEY_LINK_ERROR = "linkeError";

    /**
     * 检查项标识(1 系统版本 2 辅助插件 3 安全设置检测 4内容更新
     * 5访问分析 6内容回收站 7站点回收站 8数据备份文件 9未用文件 10 未用模板
     * 11未用静态页面12 首页打开速度 13 SEO信息检测 14 错链检测)
     */
    public static final Short CHECK_ITEM_VERSION = 1;
    public static final Short CHECK_ITEM_PLUG = 2;
    public static final Short CHECK_ITEM_SAFESET = 3;
    public static final Short CHECK_ITEM_UPDATE = 4;
    public static final Short CHECK_ITEM_VISIT = 5;
    public static final Short CHECK_ITEM_CONTENT_CYCLE = 6;
    public static final Short CHECK_ITEM_SITE_CYCLE = 7;
    public static final Short CHECK_ITEM_DB_BACK = 8;
    public static final Short CHECK_ITEM_FILE = 9;
    public static final Short CHECK_ITEM_TEMPLATE = 10;
    public static final Short CHECK_ITEM_HTML = 11;
    public static final Short CHECK_ITEM_HOMPAGE = 12;
    public static final Short CHECK_ITEM_SEO = 13;
    public static final Short CHECK_ITEM_LINK = 14;


    /**
     * h5center文件夹
     **/
    public static final String H5CENTER = "/h5center";

    /**
     * JEECMS文件夹
     **/
    public static final String JEECMS = "/jeecms";

    /**
     * classes文件夹
     **/
    public static final String CLASSES = "/WEB-INF/classes";

    /**
     * lib文件夹
     **/
    public static final String LIB = "/WEB-INF/lib";

    public static final String DOMAIN = WebConstants.DoMain.PLATFORM_URL;

    /**
     * 请求云平台站点名称 get
     **/
    public static final String SITENAME_SERVER_URL = DOMAIN + "/MODULE-WHOLE/client/v1/dynamic/site";

    /**
     * 云平台路径
     **/
    public static final String CLOUD_PLATFORM = DOMAIN + "/MODULE-APP/client/v1";

    /**
     * 请求云平台是否授权 Get
     **/
    public static final String IF_AUTH_SERVER_URL = DOMAIN + "/MODULE-APP/client/v1/userClient";

    /**
     * 检测CMS信息上传至云端 POST
     **/
    public static final String SITEHEALTH_REPORT = DOMAIN + "/MODULE-SITE-HEALTH/client/v1/siteHealth";

    /**
     * 回调站点URL POST
     **/
    public static final String SITEHEALTH_CALLBACK = DOMAIN + "/MODULE-SITE-HEALTH/client/v1/update";

    /**
     * 获取未用模板结果URL GET
     **/
    public static final String SITEHEALTH_TEMPLATE = DOMAIN + "/MODULE-SITE-HEALTH/client/v1/update/template";

    /**
     * 获取未用静态页结果URL GET
     **/
    public static final String SITEHEALTH_STATICPAGE = DOMAIN + "/MODULE-SITE-HEALTH/client/v1/update/staticPage";

    /**
     * 获取未用文件地址
     **/
    public static final String GET_UNUSED_FILES = DOMAIN + "/MODULE-SITE-HEALTH/client/v1/siteHealth/unusedFile";

    /**
     * 上传模板及资源
     **/
    public static final String UPLOAD = DOMAIN + "/MODULE-SITE-HEALTH/client/v1/siteHealth/unusedFile/upload";

    /**回调类型**/
    /**
     * 站点SEO
     */
    public static final int SITE_SEO = 1;
    /**
     * 栏目SEO
     */
    public static final int CHANNEL_SEO = 2;
    /**
     * IP地址库
     */
    public static final int IP_ADDRESS = 3;
    /**
     * 索引插件库
     */
    public static final int INDEX_PLUGIN = 4;
    /**
     * 开启ffmpeg
     */
    public static final int FFMPEG = 5;
    /**
     * 开启openoffice
     */
    public static final int OPEN_OFFICE = 6;
    /**
     * 未用模板
     */
    public static final int TEMPLATE = 7;
    /**
     * 未用静态页
     */
    public static final int STATIC_PAGE = 8;
    /**
     * 敏感词过滤
     */
    public static final int TYPE_SENSITIVE_FILTER = 9;
    /**
     * 账号安全
     */
    public static final int TYPE_ACCOUNT_SECURITY = 10;
    /**
     * 数据备份
     */
    public static final int TYPE_BACKUP_DATA = 11;
    /**
     * 会员审核
     */
    public static final int TYPE_MEMBER_REVIEW = 12;
    /**
     * 内容回收
     */
    public static final int TYPE_CONTENT_RECYCLE = 13;
    /**
     * 站点回收
     */
    public static final int TYPE_SITE_RECYCLE = 14;
    /**
     * 数据备份文件
     */
    public static final int TYPE_DATA_BACKUP_FILE = 15;
    /**
     * 未用文件
     */
    public static final int TYPE_UNUSED_FILE = 16;

    /**
     * 返回Ffmpeg文档地址
     **/
    public static final String FFMPEG_URL = DOMAIN + "XXX";
    /**
     * 返回Ffmpeg文档地址
     **/
    public static final String OPENOFFICE_URL = DOMAIN + "XXX";
}
