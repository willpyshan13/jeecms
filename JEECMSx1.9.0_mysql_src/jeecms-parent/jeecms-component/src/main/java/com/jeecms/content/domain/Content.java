/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.domain;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HtmlUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.ImmutableMap;
import com.jeecms.audit.domain.AuditStrategy;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.common.base.domain.AbstractSortDomain;
import com.jeecms.common.base.domain.IBaseFlow;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.SubStringUtil;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.vo.ContentFrontVo;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.*;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.jeecms.common.constants.WebConstants.INTRANET_PREFIX;
import static com.jeecms.common.constants.WebConstants.SPT;

/**
 * 内容主体实体类
 *
 * @author: chenming
 * @date: 2019年5月6日 下午2:32:20
 */
@Entity
@Table(name = "jc_content")
public class Content extends AbstractSortDomain<Integer> implements IBaseFlow, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String CONTENT_CACHE_KEY = "CONTENT";

    /**
     * id名称
     */
    public static final String ID_NAME = "id";

    /**
     * 内容状态
     */
    public static final Map<Integer, String> STATUS_DEPICT = ImmutableMap.<Integer, String>builder().put(1, "草稿")
            .put(2, "初稿").put(3, "流转中").put(4, "已审核").put(5, "已发布").put(6, "退回").put(7, "下线").put(8, "归档")
            .put(9, "暂存").put(10, "驳回").build();

    /**
     * 评论设置
     */
    public static final Map<Integer, String> COMMENT_DEPICT = ImmutableMap.of(1, "允许游客评论", 2, "登录后评论", 3, "不允许评论");

    /**
     * 浏览设置
     */
    public static final Map<Integer, String> VIEW_DEPICT = ImmutableMap.of(1, "允许游客访问", 2, "登录后访问");

    /**
     * 发布平台-平台
     */
    public static final String RELEASE_PC_NAME = "releasePc";
    /**
     * 发布平台-wap
     */
    public static final String RELEASE_WAP_NAME = "releaseWap";
    /**
     * 发布平台-app
     */
    public static final String RELEASE_APP_NAME = "releaseApp";
    /**
     * 发布平台-小程序
     */
    public static final String RELEASE_MINIPROGRAM_NAME = "releaseMiniprogram";

    /**
     * 自动保存版本
     */
    public static final String AUTOMATIC_SAVE_VERSION_TRUE = "1";
    /**
     * 非自动保存版本
     */
    public static final String AUTOMATIC_SAVE_VERSION_FALSE = "0";

    public static final String TITLE_NAME = "title";
    public static final String TITLE_IS_BOLD_NAME = "titleIsBold";
    public static final String TITLE_COLOR_NAME = "titleColor";

    /**
     * 付费阅读-付费价格
     */
    public static final String PAY_READ_PRICE = "payPrice";

    /**
     * 付费阅读-试读字数
     */
    public static final String PAY_READ_TRIAL_READING = "trialReading";

    private Integer id;
    /**
     * 栏目ID
     */
    private Integer channelId;
    /**
     * 撰写管理员ID
     */
    private Integer userId;
    /**
     * 组织ID
     */
    private Integer orgId;
    /**
     * 发布管理员
     */
    private Integer publishUserId;
    /**
     * 模型ID
     */
    private Integer modelId;
    /**
     * 站点ID
     */
    private Integer siteId;
    /**
     * 内容标题
     */
    private String title;
    /**
     * 内容标题是否加粗
     */
    private Boolean titleIsBold;
    /**
     * 内容标题的颜色
     */
    private String titleColor;
    /**
     * 简短标题
     */
    private String shortTitle;
    /**
     * 发布时间
     */
    private Date releaseTime;
    /**
     * 下线时间
     **/
    private Date offlineTime;
    /**
     * 内容密级
     */
    private Integer contentSecretId;
    /**
     * 内容状态(1:草稿; 2-初稿 3:流转中; 4:已审核; 5:已发布; 6:退回; 7:下线; 8:归档; 9:暂存;10:驳回 )
     */
    private Integer status;
    /**
     * 创建方式（1:直接创建 2:投稿 3:站群推送 4:站群采集 5:复制 6:链接型引用 7:镜像型引用）8 外部采集
     **/
    private Integer createType;
    /**
     * 是否编辑（0-否 1-是）
     **/
    private Boolean edit;
    /**
     * 排序值权重(排序值相同情况下，权重越大，排序越前)
     */
    private Integer sortWeight;
    /**
     * 浏览量
     */
    private Integer views;
    /**
     * 浏览人数
     */
    private Integer peopleViews;
    /**
     * 评论量
     */
    private Integer comments;
    /**
     * 点赞数
     **/
    private Integer ups;
    /**
     * 点踩数
     **/
    private Integer downs;
    /**
     * 下载量
     */
    private Integer downloads;
    /**
     * 评论设置(1允许游客评论 2登录后评论 3不允许评论)
     **/
    private Integer commentControl;
    /**
     * 是否置顶
     **/
    private Boolean top;
    /**
     * 置顶开始时间
     **/
    private Date topStartTime;
    /**
     * 置顶结束时间
     **/
    private Date topEndTime;
    /**
     * 浏览设置（1-允许游客访问 2-登录后访问）
     */
    private Short viewControl;
    /**
     * 是否发布至pc（0-否 1-是）
     **/
    private Boolean releasePc;
    /**
     * 是否发布至wap（0-否 1-是）
     **/
    private Boolean releaseWap;
    /**
     * 是否发布至app（0-否 1-是）
     **/
    private Boolean releaseApp;
    /**
     * 是否发布至小程序（0-否 1-是）
     **/
    private Boolean releaseMiniprogram;
    /**
     * 是否加入回收站（0-否 1-是）
     **/
    private Boolean recycle;
    /**
     * 复制来源内容id
     **/
    private Integer copySourceContentId;
    /**
     * 是否已生成静态化页面
     */
    private Boolean hasStatic;

    private String checkMark;

    private String modelFieldSet;

    /**
     * 是否开启付费阅读：0-关闭、1-开启
     */
    private Integer payRead;

    /**
     * 售价(默认毫)
     */
    private Long payPrice;

    /**
     * 试读字数
     */
    private Integer trialReading;

    /**
     * 是否开启赞赏
     */
    private Integer payPraise;

    /**
     * 内容类型关联
     **/
    private List<ContentType> contentTypes = new ArrayList<ContentType>(10);
    /**
     * 评论集合
     */
    private List<UserComment> userComments;
    /**
     * 栏目对象
     */
    private Channel channel;
    /**
     * 内容扩展对象
     */
    private ContentExt contentExt;
    /**
     * 撰写管理员
     **/
    private CoreUser user;
    /**
     * 发布管理员
     **/
    private CoreUser publishUser;
    /**
     * 内容操作记录集合
     */
    private List<ContentRecord> contentRecords;
    /**
     * 正文list集合
     */
    private List<ContentTxt> contentTxts;
    /**
     * tag词
     */
    private List<ContentTag> contentTags;
    /**
     * 模型对象
     **/
    private CmsModel model;
    /**
     * 内容自定义属性集合
     */
    private List<ContentAttr> contentAttrs;
    /**
     * 内容版本List集合contentSecretId
     */
    private List<ContentVersion> contentVersions;
    /**
     * 内容、附件密级
     */
    private SysSecret secret;
    /**
     * 站点
     */
    private CmsSite site;

    /**
     * 内容复制集合
     **/
    private List<Content> contentCopys;

    /**
     * 内容相关内容集合
     */
    private List<ContentRelation> contentRelations;

    /**
     * 是否已收藏
     */
    private Boolean collection;
    /**
     * 内容前台显示封装vo(仅在前台检索使用)
     */
    private ContentFrontVo contentFrontVo;

    private AuditStrategy auditStrategy;

    /**
     * 内容列表位置(1 内容列表 2智能审核 3回收站 4已归档) 为了解决范围查询慢新增字段
     */
    private Short pos;

    /**
     * 引用内容原内容id
     */
    private Integer oriContentId;
    /**
     * 引用内容原内容
     */
    private Content oriContent;
    /**
     * 关联的引用内容
     */
    private List<Content> quoteContents;

    /**
     * 累计打赏人数
     */
    private Integer rewardCount;

    /**
     * 累计内容付费人数
     */
    private Integer paidCount;

    /**
     * 付费总金额
     */
    private Long totalAmount;

    /**
     * 打赏金额
     */
    private Long rewardAmount;

    /**
     * 付费阅读金额
     */
    private Long paidAmount;

    /**
     * 最后支付时间
     */
    private LocalDateTime lastPayTime;

    /**
     * 付费总金额(未扣除平台收益)
     */
    private Long totalAmountInitial;

    /**
     * 打赏金额(未扣除平台收益)
     */
    private Long rewardAmountInitial;

    /**
     * 付费阅读金额(未扣除平台收益)
     */
    private Long paidAmountInitial;

    public Content() {

    }

    /**
     * 初始化
     */
    @Transient
    public void init() {
        if (getHasStatic() == null) {
            setHasStatic(false);
        }
    }

    /**
     * 获取预览 URL
     *
     * @Title: getPreviewUrl
     * @return: String
     */
    @Transient
    public String getPreviewUrl() {
        StringBuilder url = new StringBuilder();
        url.append(getSite().getSitePreviewUrl());
        return url.append("?contentId=").append(this.id).append("&type=")
                .append(WebConstants.PREVIEW_TYPE_CONTENT).toString();
    }

    /**
     * 获得URL地址（站点统一配置的是采用绝对路径还是相对路径）
     *
     * @return
     */
    @Transient
    public String getUrl() {
        /**镜像型引用，链接依然是原来的链接；*/
        if (getOriContent() != null && ContentConstant.CONTENT_CREATE_TYPE_URL == getCreateType().intValue()) {
            return getOriContent().getUrl();
        }
        if (!StringUtils.isBlank(getOutLink())) {
            return getOutLink();
        }
        if (getOpenStatic() && getHasStatic()) {
            return getUrlStatic(null, 1);
        } else {
            return getUrlDynamic(null);
        }
    }

    /**
     * 获取url绝对路径
     *
     * @Title: getUrlWhole
     * @return: String
     */
    @Transient
    public String getUrlWhole() {
        /**镜像型引用，链接依然是原来的链接；*/
        if (getOriContent() != null && ContentConstant.CONTENT_CREATE_TYPE_URL == getCreateType().intValue()) {
            return getOriContent().getUrlWhole();
        }
        if (!StringUtils.isBlank(getOutLink())) {
            return getOutLink();
        }
        if (getOpenStatic() && getHasStatic()) {
            return getUrlStatic(true, 1);
        } else {
            return getUrlDynamic(true);
        }
    }

    @Transient
    public String getUrlStatic(int pageNo) {
        return getUrlStatic(null, pageNo);
    }

    /**
     * 获取静态URL
     *
     * @param pageNo 页码
     * @Title: getUrlStatic
     * @return: String
     */
    @Transient
    public String getUrlStatic(Boolean whole, int pageNo) {
        if (!StringUtils.isBlank(getOutLink())) {
            return getOutLink();
        }
        CmsSite site = getSite();
        StringBuilder url = site.getUrlStaticBuff(whole, SystemContextUtils.isPc());
        // 默认静态页面访问路径
        url.append(SPT).append(getChannel().getPath());
        url.append(SPT).append(MyDateUtils.formatDate(getReleaseTime(), MyDateUtils.COM_YMD_PATTERN));
        url.append(SPT).append(getId());
        if (pageNo > 1) {
            url.append("_").append(pageNo);
        }
        url.append(".").append(getSite().getConfig().getStaticHtmlSuffix());
        return url.toString();
    }

    /**
     * 获取动态URL
     *
     * @param whole 是否全路径 未传值则采用站点统一配置
     * @Title: getUrlDynamic
     * @return: String
     */
    @Transient
    public String getUrlDynamic(Boolean whole) {
        if (!StringUtils.isBlank(getOutLink())) {
            return getOutLink();
        }
        CmsSite site = getSite();
        StringBuilder url = site.getUrlBuffer(whole);
        /** 是内网模式在后面追加站点路径访问 */
        if (site.getGlobalConfig().getIsIntranet()) {
            url.append(INTRANET_PREFIX).append(site.getPath());
        }
        url.append(SPT).append(getChannel().getPath());
        url.append(SPT).append(getId());
        if (getSite().getGlobalConfig().getConfigAttr().getUrlSuffixJhtml()) {
            url.append(WebConstants.DYNAMIC_CONTENT_SUFFIX);
        }
        return url.toString();
    }

    /**
     * 获取外部链接
     *
     * @Title: getOutLink
     * @return: String
     */
    @Transient
    public String getOutLink() {
        ContentExt ext = getContentExt();
        if (ext != null) {
            return ext.getOutLink();
        } else {
            return null;
        }
    }

    /**
     * 是否开启了静态化（综合站点设置和是否已生成内容页静态页）
     *
     * @Title: getOpenStatic
     * @return: boolean
     */
    @Transient
    public boolean getOpenStatic() {
        Boolean openStatic = false;
        openStatic = getSite().getOpenStatic();
        return openStatic;
    }

    /**
     * 获取PC模板路径
     *
     * @Title: getTplChannelOrDefForPc
     * @return: String
     */
    @Transient
    public String getTplContentOrDefForPc() {
        /** 内容自身设置的模板 */
        String tpl = getTplPcPath();
        /** 内容上配置了模板并且模板字段存在则取自身配置的模板 */
        if (!StringUtils.isBlank(tpl)) {
            return tpl;
        } else {
            return getChannel().getTplContentOrDefForPc(getModel());
        }
    }

    /**
     * 获取手机模板路径
     *
     * @Title: getTplChannelOrDefForMobile
     * @return: String
     */
    @Transient
    public String getTplContentOrDefForMobile() {
        /** 内容自身设置的模板 */
        String tpl = getTplMobilePath();
        /** 内容上配置了模板并且模板字段存在则取自身配置的模板 */
        if (!StringUtils.isBlank(tpl)) {
            return tpl;
        } else {
            return getChannel().getTplContentOrDefForMobile(getModel());
        }
    }

    /**
     * 获取内容手机端模板（模型字段不存在 则返回空）
     *
     * @Title: getTplMobilePath
     * @return: String
     */
    @Transient
    public String getTplMobilePath() {
        ContentExt ext = getContentExt();
        if (ext != null) {
            if (getModel().existItem(CmsModelConstant.FIELD_SYS_TPL_MOBILE)
                    && StringUtils.isNotBlank(ext.getTplMobile())) {
                return getSite().getSolutionPath() + ext.getTplMobile();
            }
        }
        return null;
    }

    /**
     * 获取内容手机端模板（模型字段不存在 则返回空）
     *
     * @Title: getTplPcPath
     * @return: String
     */
    @Transient
    public String getTplPc() {
        ContentExt ext = getContentExt();
        if (ext != null) {
            if (getModel().existItem(CmsModelConstant.FIELD_SYS_TPL_PC)
                    && StringUtils.isNotBlank(ext.getTplPc())) {
                return ext.getTplPc();
            }
        }
        return null;
    }

    /**
     * 获取内容手机端模板（模型字段不存在 则返回空）
     *
     * @Title: getTplMobilePath
     * @return: String
     */
    @Transient
    public String getTplMobile() {
        ContentExt ext = getContentExt();
        if (ext != null) {
            if (getModel().existItem(CmsModelConstant.FIELD_SYS_TPL_MOBILE)
                    && StringUtils.isNotBlank(ext.getTplMobile())) {
                return ext.getTplMobile();
            }
        }
        return null;
    }

    /**
     * 获取内容手机端模板（模型字段不存在 则返回空）
     *
     * @Title: getTplPcPath
     * @return: String
     */
    @Transient
    public String getTplPcPath() {
        ContentExt ext = getContentExt();
        if (ext != null) {
            if (getModel().existItem(CmsModelConstant.FIELD_SYS_TPL_PC)
                    && StringUtils.isNotBlank(ext.getTplPc())) {
                return getSite().getSolutionPath() + ext.getTplPc();
            }
        }
        return null;
    }

    /**
     * 获取内容页数，获取第一个正文的页数
     *
     * @Title: getPageCount
     * @return: int
     */
    @Transient
    public int getPageCount() {
        List<ContentTxt> txts = getContentTxts();
        if (txts != null && txts.size() > 0) {
            return txts.get(0).getTxtCount();
        } else {
            return 1;
        }
    }

    /**
     * 获取正文的页数
     *
     * @param field 字段名称
     * @Title: getTxtCountByField
     * @return: int
     */
    @Transient
    public int getPageCountByField(String field) {
        ContentTxt txt = getTxtByField(field);
        if (txt != null) {
            return txt.getTxtCount();
        }
        return 1;
    }

    /**
     * 获取富文本的值集合
     *
     * @Title: getTxts
     * @return: List
     */
    @Transient
    @JSONField(serialize = false)
    public List<String> getTxts() {
        List<ContentTxt> txts = getContentTxts();
        if (txts != null && txts.size() > 0) {
            return txts.stream().map(t -> t.getAttrTxt()).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取tagId关键词集合
     *
     * @Title: getTagIds
     * @return: List
     */
    @Transient
    public List<Integer> getTagIds() {
        List<ContentTag> tags = getContentTags();
        if (tags != null && tags.size() > 0) {
            return tags.stream().map(t -> t.getId()).collect(Collectors.toList());
        }
        return null;
    }

    @Transient
    public List<String> getTagNames() {
        List<ContentTag> tags = getContentTags();
        if (tags != null && tags.size() > 0) {
            return tags.stream().map(t -> t.getTagName()).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取内容分页内容，获取第一个正文的分页内容
     *
     * @param pageNo 分页数
     * @Title: getTxtByNo
     * @return: String
     */
    @Transient
    public String getTxtByNo(int pageNo) {
        List<ContentTxt> txts = getContentTxts();
        if (txts != null && txts.size() > 0) {
            return txts.get(0).getTxtByNo(pageNo);
        } else {
            return null;
        }
    }

    @Transient
    public String getTxt() {
        List<ContentTxt> txts = getContentTxts();
        if (txts != null && txts.size() > 0) {
            return txts.get(0).getTxtByNo(1);
        } else {
            return null;
        }
    }

    /**
     * 获取去除html之后的内容
     *
     * @return
     */
    @Transient
    public String getTxtCleanHtml() {
        String txt = getTxt();
        if (StringUtils.isNotBlank(txt)) {
            return HtmlUtil.cleanHtmlTag(txt);
        }
        return txt;
    }

    /**
     * 获取富文本字段分页内容，根据字段名称获取
     *
     * @param field  字段名称
     * @param pageNo 分页数
     * @Title: getTxtForFieldByNo
     * @return: String
     */
    @Transient
    public String getTxtForFieldByNo(String field, int pageNo) {
        ContentTxt txt = getTxtByField(field);
        if (txt != null) {
            return txt.getTxtByNo(pageNo);
        }
        return null;
    }

    /**
     * 获取ContentTxt 根据字段名称
     *
     * @param field 字段名称
     * @Title: getTxtByField
     * @return: ContentTxt
     */
    @Transient
    public ContentTxt getTxtByField(String field) {
        if (StringUtils.isBlank(field)) {
            return null;
        }
        List<ContentTxt> txts = getContentTxts();
        for (ContentTxt txt : txts) {
            if (field.equals(txt.getAttrKey())) {
                return txt;
            }
        }
        return null;
    }

    /**
     * 获取富文本数量
     *
     * @Title: getContentTxtCounts
     * @return: int
     */
    @Transient
    public int getTxtCounts() {
        List<ContentTxt> txts = getContentTxts();
        if (txts != null) {
            return txts.size();
        }
        return 0;
    }

    /**
     * 获取内容是否多富文本
     *
     * @Title: getMutiTxt
     * @return: boolean true是多富文本内容
     */
    @Transient
    public boolean getMutiTxt() {
        if (getTxtCounts() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 获取静态化栏目文件的路径
     *
     * @param pageNo 页码
     * @param pc     是否pc页码
     * @Title: getStaticFilename
     * @return: String
     */
    @Transient
    public String getStaticFilename(boolean pc, int pageNo) {
        StringBuilder url = new StringBuilder();
        // 默认静态路径
        url.append(SPT);
        if (pc) {
            url.append(WebConstants.STATIC_PC_PATH);
        } else {
            url.append(WebConstants.STATIC_MOBILE_PATH);
        }
        url.append(getSiteId());
        url.append(SPT).append(getChannel().getPath());
        url.append(SPT).append(MyDateUtils.formatDate(getReleaseTime(), MyDateUtils.COM_YMD_PATTERN));
        url.append(SPT).append(getId());
        if (pageNo > 1) {
            url.append("_").append(pageNo);
        }
        url.append(".");
        String suffix = getSite().getCmsSiteCfg().getStaticHtmlSuffix();
        url.append(suffix);
        return url.toString();
    }

    /**
     * 获取访问设置 1.都不需要登录2.仅内容页需登录 3.都需要登录
     *
     * @Title: getRealViewControl
     * @return: Short
     */
    @Transient
    public Short getRealViewControl() {
        // 如果当前模型存在这个字段上一次修改也存在这个字段，说明其是上一次修改的值，然后给出当前数据库的值
        boolean exist = getModel().existItem(CmsModelConstant.FIELD_SYS_VIEW_CONTROL);
        if (!exist) {
            return getChannel().getViewControl();
        }
        return getViewControl();
    }

    /**
     * 获取内容类型ID集合
     *
     * @Title: getTypeIds
     * @return: List
     */
    @Transient
    public List<Integer> getTypeIds() {
        return ContentType.fetchIds(getContentTypes());
    }

    /**
     * 获取内容类型ID集合
     *
     * @Title: getTypeNames
     * @return: List
     */
    @Transient
    public List<String> getTypeNames() {
        List<String> typeNames = new ArrayList<String>();
        for (ContentType t : getContentTypes()) {
            typeNames.add(t.getTypeName());
        }
        return typeNames;
    }

    /**
     * 获得所有的id的List集合
     */
    @Transient
    @JSONField(serialize = false)
    public static List<Integer> fetchIds(Collection<Content> contents) {
        if (contents == null) {
            return null;
        }
        List<Integer> ids = new ArrayList<Integer>();
        for (Content s : contents) {
            ids.add(s.getId());
        }
        return ids;
    }

    /**
     * 是否发布状态
     *
     * @Title: isPublish
     * @return: boolean
     */
    @Transient
    public boolean isPublish() {
        return ContentConstant.STATUS_PUBLISH == getStatus();
    }

    /**
     * 是否刪除状态(已删除、进入回收站、归档)
     *
     * @Title: isDelete
     * @return: boolean
     */
    @Transient
    public boolean isDelete() {
        return getHasDeleted() || getRecycle() || ContentConstant.STATUS_PIGEONHOLE == getStatus();
    }

    /**
     * 获取内容所在栏目（自身栏目+引用栏目）(非回收站和发布状态)
     *
     * @Title: getRelateChannels
     * @return: List 栏目集合
     */
    @Transient
    public List<Channel> getRelateChannels() {
        List<Channel> channels = getQuoteContents().stream()
                .filter(channel -> channel.getStatus().equals(ContentConstant.STATUS_PUBLISH))
                .filter(channel -> channel.getRecycle().equals(false)).map(Content::getChannel)
                .filter(channel -> channel != null)
                .collect(Collectors.toList());
        return channels;
    }

    /**
     * 不去除内容状态
     *
     * @return
     */
    @Transient
    public List<Channel> getRelateChannelsAll() {
        List<Channel> channels = new ArrayList<>();
        if (getQuoteContents() != null) {
            channels = getQuoteContents().stream().map(Content::getChannel)
                    .filter(channel -> channel != null)
                    .collect(Collectors.toList());
            if (channels.size() <= 0) {
                channels.add(getChannel());
            }
        }
        channels.addAll(getChannel().getNodeList());
        return channels;
    }


    @Transient
    public ResourcesSpaceData getReData() {
        return getContentExt().getReData();
    }

    @Transient
    public ResourcesSpaceData getDocResource() {
        return getContentExt().getDocResource();
    }

    /**
     * 获取关键词
     *
     * @Title: getKeyword
     * @return: String
     */
    @Transient
    public String getKeyword() {
        return getContentExt().getRealKeyWord();
    }

    /**
     * 获取描述
     *
     * @Title: getDescription
     * @return: String
     */
    @Transient
    public String getDescription() {
        return getContentExt().getRealDescription();
    }

    @Transient
    public String getAuthor() {
        return getContentExt().getRealAuthor();
    }

    /**
     * 获取新内容标识（如果是新内容）
     *
     * @return String
     */
    @Transient
    public String getNewContentUrl() {
        boolean openContentNewFlag = getSite().getConfig().getOpenContentNewFlag();
        Integer contentFlagType = getSite().getConfig().getContentNewFlagType();
        Integer contentNewFlag = getSite().getConfig().getContentNewFlag();
        if (openContentNewFlag && contentFlagType != null && contentNewFlag != null) {
            Date date = Calendar.getInstance().getTime();
            Date time = contentFlagType == 1 ? MyDateUtils.getSpecficDate(date, -contentNewFlag)
                    : MyDateUtils.getHourAfterTime(date, -contentNewFlag);
            if (time.getTime() <= getReleaseTime().getTime()) {
                return getSite().getConfig().getContentNewDefinitionUrl();
            }
        }
        return "";
    }

    /**
     * 获取来源
     *
     * @Title: getSource
     * @return: ContentSource
     */
    @Transient
    public ContentSource getSource() {
        return getContentExt().getRealContentSource();
    }

    /**
     * 是否新窗口打开
     *
     * @Title: getTarget
     * @return: Boolean
     */
    @Transient
    public Boolean getTarget() {
        return getContentExt().getRealIsNewTarget();
    }

    /**
     * 获取图片资源URL
     *
     * @Title: getIconUrl
     * @return: String
     */
    @Transient
    public String getIconUrl() {
        ResourcesSpaceData iconRes = getContentExt().getRealReData();
        if (iconRes != null) {
            return iconRes.getUrl();
        }
        return null;
    }

    /**
     * 发文-机关代号
     *
     * @Title: getIssueOrg
     * @return: String
     */
    @Transient
    public String getIssueOrg() {
        if (getContentExt().getSueOrg() != null) {
            return getContentExt().getSueOrg().getMarkName();
        }
        return "";
    }

    /**
     * 发文字号-年份
     *
     * @Title: getIssueYear
     * @return: String
     */
    @Transient
    public String getIssueYear() {
        if (getContentExt().getSueYear() != null) {
            return getContentExt().getSueYear().getMarkName();
        }
        return "";
    }

    @Transient
    public String getPdfUrl() {
        return getContentExt().getPdfUrl();
    }

    /**
     * 发文字号-顺序号
     *
     * @Title: getIssueNum
     * @return: String
     */
    @Transient
    public String getIssueNum() {
        return getContentExt().getIssueNum();
    }

    /**
     * 以map的方式读取自定义属性，方便单个取字段
     *
     * @Title: getAttr
     * @return: Map
     */
    @Transient
    public Map<String, ContentAttr> getAttr() {
        List<ContentAttr> attrs = getContentAttrs();
        Map<String, ContentAttr> map = new HashMap<String, ContentAttr>();
        if (attrs != null && attrs.size() > 0) {
            Integer num = 999;
            for (ContentAttr attr : attrs) {
                if (CmsModelConstant.MANY_CHOOSE.equals(attr.getAttrType())
                        || CmsModelConstant.DROP_DOWN.equals(attr.getAttrType())) {
                    StringBuilder labelValue = new StringBuilder();
                    // 多选
                    // 获取到多选框的值json
                    CmsModelItem item = getModel().getItem(attr.getAttrName());
                    if (item != null) {
                        String jsonString = item.getContent();
                        JSONObject jsonObject = JSONObject.parseObject(jsonString)
                                .getJSONObject("value");
                        JSONArray array = jsonObject.getJSONArray("options");
                        // 遍历获取多选框的每个值，与内容选择的值比较
                        String[] value = null;
                        String other = "";
                        if (attr.getAttrValue() != null) {
                            JSONObject attrValueJson = JSONObject
                                    .parseObject(attr.getAttrValue());
                            String obj = JSONObject
                                    .toJSONString(attrValueJson.get("value"));
                            if (obj.startsWith("[")) {
                                value = obj.substring(1, obj.length() - 1).split(",");
                            } else {
                                value = obj.split(",");
                            }
                            other = JSONObject.toJSONString(attrValueJson.get("attrValue"));
                        }
                        if (value != null) {
                            for (Object o : array) {
                                JSONObject json = (JSONObject) o;
                                String key = json.getString("value");
                                String label = json.getString("label");
                                for (String integer : value) {
                                    if (integer.equals(key)) {
                                        labelValue.append(label);
                                    }
                                    if (String.valueOf(num).equals(integer)) {
                                        labelValue.append(other);
                                    }
                                }
                            }
                        }
                    }
                    attr.setValue(labelValue.toString());
                } else if (CmsModelConstant.SINGLE_CHOOSE.equals(attr.getAttrType()) || CmsModelConstant.SEX.equals(attr.getAttrType())) {
                    // 单选
                    StringBuilder labelValue = new StringBuilder();
                    // 多选
                    // 获取到多选框的值json
                    CmsModelItem item = getModel().getItem(attr.getAttrName());
                    if (item != null) {
                        String jsonString = item.getContent();
                        JSONObject jsonObject = JSONObject.parseObject(jsonString)
                                .getJSONObject("value");
                        JSONArray array = jsonObject.getJSONArray("options");
                        // 遍历获取多选框的每个值，与内容选择的值比较
                        String value = null;
                        String other = "";
                        if (attr.getAttrValue() != null) {
                            JSONObject attrValueJson = JSONObject
                                    .parseObject(attr.getAttrValue());
                            value = JSONObject
                                    .toJSONString(attrValueJson.get("value"));
                            other = JSONObject.toJSONString(attrValueJson.get("attrValue"));
                        }
                        if (value != null) {
                            for (Object o : array) {
                                JSONObject json = (JSONObject) o;
                                Integer key = json.getInteger("value");
                                String label = json.getString("label");
                                if (value.equals(key.toString())) {
                                    labelValue.append(label);
                                } else if (num.toString().equals(value)) {
                                    labelValue.append(other);
                                }
                            }
                        }
                    }
                    attr.setValue(labelValue.toString());
                } else {
                    attr.setValue(attr.getAttrValue());
                }
                map.put(attr.getAttrName(), attr);
            }
        }
        return map;
    }

    @Transient
    public Map<String, String> getTxtAttr() {
        List<ContentTxt> attrs = getContentTxts();
        Map<String, String> map = new HashMap<>();
        if (attrs != null && !attrs.isEmpty()) {
            for (ContentTxt attr : attrs) {
                String attrTxt = attr.getAttrTxt();
                if (getRealPayRead() && getTrialReading() != null) {
                    attrTxt = SubStringUtil.interceptTxt(attrTxt, getTrialReading());
                    map.put(attr.getAttrKey(), attrTxt);
                } else {
                    map.put(attr.getAttrKey(), attrTxt);
                }

            }
        }
        return map;
    }

    /**
     * 获取清除html之后的富文本
     *
     * @return
     */
    @Transient
    public Map<String, String> getTxtAttrCleanHtml() {
        Map<String, String> txtAttrs = getTxtAttr();
        if (txtAttrs != null) {
            for (Map.Entry<String, String> entry : txtAttrs.entrySet()) {
                txtAttrs.put(entry.getKey(), HtmlUtil.cleanHtmlTag(entry.getValue()));
            }
        }
        return txtAttrs;
    }

    /**
     * 获取附件集合
     *
     * @Title: getAttachments
     * @return: List
     */
    @Transient
    public List<ContentAttr> getAttachments() {
        List<ContentAttr> attrs = getContentAttrs();
        List<ContentAttr> attachs = new ArrayList<ContentAttr>();
        for (ContentAttr a : attrs) {
            if (CmsModelConstant.ANNEX_UPLOAD.equals(a.getAttrType()) && !a.getContentAttrRes().isEmpty()) {
                attachs.add(a);
            }
        }
        return attachs;
    }


    /**
     * 获取附件字段的资源、多资源的map（resId,attrId）资源id，属性id的map
     *
     * @return
     */
    @Transient
    public Map<Integer, Integer> getAttachmentResAttrMap() {
        List<ContentAttr> attrs = getAttachments();
        return convertAttrsToResAttrMap(attrs);
    }

    /**
     * 获取附件集合
     *
     * @Title: getAttachments
     * @return: List
     */
    @Transient
    public List<ContentAttr> getAttachmentsByField(String field) {
        List<ContentAttr> attachs = getAttachments();
        if (attachs != null && attachs.size() > 0 && org.apache.commons.lang3.StringUtils.isNotBlank(field)) {
            return attachs.stream().filter(a -> field.equals(a.getAttrName())).collect(Collectors.toList());
        }
        return null;
    }

    @Transient
    public Map<Integer, Integer> getAttachmentResAttrMapByField(String field) {
        List<ContentAttr> attrs = getAttachmentsByField(field);
        return convertAttrsToResAttrMap(attrs);
    }

    private Map<Integer, Integer> convertAttrsToResAttrMap(List<ContentAttr> attrs) {
        Map<Integer, Integer> resAttrMap = new HashMap<>();
        for (ContentAttr a : attrs) {
            if (a.getResId() != null) {
                resAttrMap.put(a.getResId(), a.getId());
            }
            for (ContentAttrRes ares : a.getContentAttrRes()) {
                if (ares.getResId() != null) {
                    resAttrMap.put(ares.getResId(), a.getId());
                }
            }
        }
        return resAttrMap;
    }

    @Transient
    public Integer getViewsMonth() {
        return getContentExt().getViewsMonth();
    }

    @Transient
    public Integer getCommentsMonth() {
        return getContentExt().getCommentsMonth();
    }

    @Transient
    public Integer getDownloadsMonth() {
        return getContentExt().getDownloadsMonth();
    }

    @Transient
    public Integer getUpsMonth() {
        return getContentExt().getUpsMonth();
    }

    @Transient
    public Integer getViewsWeek() {
        return getContentExt().getViewsWeek();
    }

    @Transient
    public Integer getCommentsWeek() {
        return getContentExt().getCommentsWeek();
    }

    @Transient
    public Integer getDownloadsWeek() {
        return getContentExt().getDownloadsWeek();
    }

    @Transient
    public Integer getUpsWeek() {
        return getContentExt().getUpsWeek();
    }

    @Transient
    public Integer getViewsDay() {
        return getContentExt().getViewsDay();
    }

    @Transient
    public Integer getCommentsDay() {
        return getContentExt().getCommentsDay();
    }

    @Transient
    public Integer getDownloadsDay() {
        return getContentExt().getDownloadsDay();
    }

    @Transient
    public Integer getUpsDay() {
        return getContentExt().getUpsDay();
    }

    @Transient
    public Integer getDownsDay() {
        return getContentExt().getUpsDay();
    }

    @Id
    @TableGenerator(name = "jc_content", pkColumnValue = "jc_content", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_content")
    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "channel_id", nullable = false, length = 11)
    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    @Column(name = "user_id", nullable = false, length = 11)
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Column(name = "org_id", length = 11)
    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    @Column(name = "publish_user_id", nullable = true, length = 11)
    public Integer getPublishUserId() {
        return publishUserId;
    }

    public void setPublishUserId(Integer publishUserId) {
        this.publishUserId = publishUserId;
    }

    @Column(name = "model_id", nullable = false, length = 11)
    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    @Column(name = "site_id", nullable = false, length = 11)
    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @Column(name = "title", nullable = false, length = 150)
    public String getTitle() {
        /**引用内容读取标题采用源内容标题，后台内容列表目前只有标题需要如此重写，id还是得用自己的id，可改变不同状态*/
        if (getOriContent() != null) {
            return getOriContent().getTitle();
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "title_is_bold", nullable = false, length = 1)
    public Boolean getTitleIsBold() {
        return titleIsBold;
    }

    public void setTitleIsBold(Boolean titleIsBold) {
        this.titleIsBold = titleIsBold;
    }

    @Column(name = "title_color", nullable = false, length = 50)
    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    @Column(name = "short_title", nullable = true, length = 150)
    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "release_time", nullable = true)
    public Date getReleaseTime() {
        return releaseTime;
    }

    @Transient
    public Date getSourceReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    @Column(name = "offline_time", nullable = true)
    public Date getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(Date offlineTime) {
        this.offlineTime = offlineTime;
    }

    @Column(name = "status", nullable = false, length = 6)
    public Integer getStatus() {
        return status;
    }

    @Override
    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "sort_weight", nullable = false, length = 11)
    public Integer getSortWeight() {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight) {
        this.sortWeight = sortWeight;
    }

    @Column(name = "views", nullable = true, length = 11)
    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    @Column(name = "comments", nullable = true, length = 11)
    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    @Column(name = "downs", nullable = true, length = 11)
    public Integer getDowns() {
        return downs;
    }

    public void setDowns(Integer downs) {
        this.downs = downs;
    }

    @OneToMany(mappedBy = "content")
    public List<UserComment> getUserComments() {
        return userComments;
    }

    public void setUserComments(List<UserComment> userComments) {
        this.userComments = userComments;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Column(name = "is_edit", length = 1)
    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    @Column(name = "create_type", length = 1)
    public Integer getCreateType() {
        return createType;
    }

    public void setCreateType(Integer createType) {
        this.createType = createType;
    }

    @Column(name = "ups", nullable = true, length = 11)
    public Integer getUps() {
        return ups;
    }

    public void setUps(Integer ups) {
        this.ups = ups;
    }

    @Column(name = "downloads", nullable = true, length = 11)
    public Integer getDownloads() {
        return downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    @Column(name = "is_top", nullable = false, length = 1)
    public Boolean getTop() {
        return top;
    }

    public void setTop(Boolean top) {
        this.top = top;
    }

    @JSONField(format = "yyyy-MM-dd")
    @Column(name = "top_start_time")
    public Date getTopStartTime() {
        return topStartTime;
    }

    public void setTopStartTime(Date topStartTime) {
        this.topStartTime = topStartTime;
    }

    @JSONField(format = "yyyy-MM-dd")
    @Column(name = "top_end_time")
    public Date getTopEndTime() {
        return topEndTime;
    }

    public void setTopEndTime(Date topEndTime) {
        this.topEndTime = topEndTime;
    }

    @Column(name = "is_release_pc", length = 1)
    public Boolean getReleasePc() {
        return releasePc;
    }

    public void setReleasePc(Boolean releasePc) {
        this.releasePc = releasePc;
    }

    @Column(name = "is_release_wap", length = 1)
    public Boolean getReleaseWap() {
        return releaseWap;
    }

    public void setReleaseWap(Boolean releaseWap) {
        this.releaseWap = releaseWap;
    }

    @Column(name = "is_release_app", length = 1)
    public Boolean getReleaseApp() {
        return releaseApp;
    }

    public void setReleaseApp(Boolean releaseApp) {
        this.releaseApp = releaseApp;
    }

    @Column(name = "is_release_miniprogram", length = 1)
    public Boolean getReleaseMiniprogram() {
        return releaseMiniprogram;
    }

    public void setReleaseMiniprogram(Boolean releaseMiniprogram) {
        this.releaseMiniprogram = releaseMiniprogram;
    }

    @Column(name = "is_recycle", nullable = false, length = 1)
    public Boolean getRecycle() {
        return recycle;
    }

    public void setRecycle(Boolean recycle) {
        this.recycle = recycle;
    }

    @Column(name = "copy_source_content_id", length = 11)
    public Integer getCopySourceContentId() {
        return copySourceContentId;
    }

    public void setCopySourceContentId(Integer copySourceContentId) {
        this.copySourceContentId = copySourceContentId;
    }

    @Column(name = "has_static")
    public Boolean getHasStatic() {
        return hasStatic;
    }

    public void setHasStatic(Boolean hasStatic) {
        this.hasStatic = hasStatic;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinTable(name = "jc_tr_content_type", joinColumns = @JoinColumn(name = "content_id"), inverseJoinColumns = @JoinColumn(name = "content_type_id"))
    public List<ContentType> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(List<ContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "content", cascade = {CascadeType.PERSIST})
    public ContentExt getContentExt() {
        return contentExt;
    }

    public void setContentExt(ContentExt contentExt) {
        this.contentExt = contentExt;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    public CoreUser getUser() {
        return user;
    }

    public void setUser(CoreUser user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publish_user_id", insertable = false, updatable = false)
    public CoreUser getPublishUser() {
        return publishUser;
    }

    public void setPublishUser(CoreUser publishUser) {
        this.publishUser = publishUser;
    }

    @OneToMany(mappedBy = "content")
    public List<ContentRecord> getContentRecords() {
        return contentRecords;
    }

    public void setContentRecords(List<ContentRecord> contentRecords) {
        this.contentRecords = contentRecords;
    }

    //        @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @JoinTable(name = "jc_tr_content_tag", joinColumns = @JoinColumn(name = "content_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public List<ContentTag> getContentTags() {
        return contentTags;
    }

    public void setContentTags(List<ContentTag> contentTags) {
        this.contentTags = contentTags;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", insertable = false, updatable = false)
    public CmsModel getModel() {
        return model;
    }

    public void setModel(CmsModel model) {
        this.model = model;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    public CmsSite getSite() {
        return site;
    }

    public void setSite(CmsSite site) {
        this.site = site;
    }

    @Column(name = "content_secret_id", nullable = true, length = 11)
    public Integer getContentSecretId() {
        return contentSecretId;
    }

    public void setContentSecretId(Integer contentSecretId) {
        this.contentSecretId = contentSecretId;
    }

    @Column(name = "view_control", nullable = true, length = 6)
    public Short getViewControl() {
        return viewControl;
    }

    public void setViewControl(Short viewControl) {
        this.viewControl = viewControl;
    }

    @Column(name = "comment_control", nullable = false, length = 6)
    public Integer getCommentControl() {
        return commentControl;

    }

    public void setCommentControl(Integer commentControl) {
        this.commentControl = commentControl;
    }

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
    public List<ContentAttr> getContentAttrs() {
        return contentAttrs;
    }

    public void setContentAttrs(List<ContentAttr> contentAttrs) {
        this.contentAttrs = contentAttrs;
    }

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
    public List<ContentVersion> getContentVersions() {
        return contentVersions;
    }

    public void setContentVersions(List<ContentVersion> contentVersions) {
        this.contentVersions = contentVersions;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_copy_content", joinColumns = @JoinColumn(name = "content_id"), inverseJoinColumns = @JoinColumn(name = "target_content_id"))
    public List<Content> getContentCopys() {
        return contentCopys;
    }

    public void setContentCopys(List<Content> contentCopys) {
        this.contentCopys = contentCopys;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_secret_id", referencedColumnName = "secret_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public SysSecret getSecret() {
        return secret;
    }

    public void setSecret(SysSecret secret) {
        this.secret = secret;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "content")
    public List<ContentTxt> getContentTxts() {
        return contentTxts;
    }

    public void setContentTxts(List<ContentTxt> contentTxts) {
        this.contentTxts = contentTxts;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "content")
    public List<ContentRelation> getContentRelations() {
        if (contentRelations == null) {
            return null;
        }
        List<ContentRelation> collect = contentRelations.stream()
                .filter(o -> !o.getRelationContent().getRecycle() && !o.getRelationContent().getHasDeleted())
                .collect(Collectors.toList());
        return collect;
    }

    public void setContentRelations(List<ContentRelation> contentRelations) {
        this.contentRelations = contentRelations;
    }

    /**
     * 栏目名称
     *
     * @return
     * @Title: getChannel
     */
    @Transient
    public String getChannelName() {
        if (getChannel() != null) {
            return getChannel().getName();
        } else {
            return null;
        }
    }

    /**
     * 发布时间简短中文格式 刚刚，1分钟前...
     *
     * @Title: getReleaseTimeString
     * @return: String
     */
    @Transient
    public String getReleaseTimeString() {
        if (getReleaseTime() != null) {
            return MyDateUtils.getTime(getReleaseTime());
        }
        return "";
    }

    @Transient
    public String getOfflineTimeString() {
        if (getOfflineTime() != null) {
            return MyDateUtils.getTime(getOfflineTime());
        }
        return "";
    }

    @Override
    public void setFlowProcessId(String flowProcessId) {
        getContentExt().setFlowProcessId(flowProcessId);
    }

    @Override
    public void setFlowStartUserId(Integer flowStartUserId) {
        getContentExt().setFlowStartUserId(flowStartUserId);
    }

    @Override
    @Transient
    public Integer getFlowStartUserId() {
        return getContentExt().getFlowStartUserId();
    }

    @Override
    @Transient
    public String getFlowProcessId() {
        return getContentExt().getFlowProcessId();
    }

    @Override
    @Transient
    public Integer getFlowId() {
        return getContentExt().getWorkflowId();
    }

    @Override
    public void setFlowId(Integer workflowId) {
        getContentExt().setWorkflowId(workflowId);
    }

    @Override
    @Transient
    public String getMsgPlace() {
        return getTitle();
    }

    @Override
    @Transient
    public Integer getCurrNodeId() {
        return getContentExt().getCurrNodeId();
    }

    @Override
    public void setCurrNodeId(Integer nodeId) {
        getContentExt().setCurrNodeId(nodeId);
    }

    /**
     * 获取引用栏目构建对象
     *
     * @return
     * @Title: getQuoteChannelName
     */
    @Transient
    public JSONArray getQuoteChannel() {
        JSONArray array = new JSONArray();
        List<Content> ccs = getQuoteContents().stream()
                .filter(cc -> !cc.getChannelId().equals(this.channelId))
                .filter(cc -> cc.getRecycle().equals(false)).collect(Collectors.toList());
        if (!ccs.isEmpty()) {
            Map<Integer, List<Content>> map = ccs.stream()
                    .collect(Collectors.groupingBy(Content::getCreateType));
            for (Integer cc : map.keySet()) {
                List<Content> list = map.get(cc);
                // 链接型引用
                if (cc.equals(ContentConstant.CONTENT_CREATE_TYPE_URL)) {
                    StringBuilder link = new StringBuilder();
                    JSONObject obj = new JSONObject();
                    for (Content c : list) {
                        link.append(c.getChannelName()).append(" ");
                    }
                    obj.put("createType", cc);
                    obj.put("name", link.toString());
                    array.add(obj);
                } else if (cc.equals(ContentConstant.CONTENT_CREATE_TYPE_MIRROR)) {
                    StringBuilder mirror = new StringBuilder();
                    JSONObject obj = new JSONObject();
                    for (Content contentChannel : list) {
                        mirror.append(contentChannel.getChannelName()).append(" ");
                    }
                    obj.put("createType", cc);
                    obj.put("name", mirror.toString());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    /**
     * 获取引用栏目构建对象
     *
     * @return
     * @Title: getQuoteChannelName
     */
    @Transient
    public List<Integer> getQuoteChannelIds() {
        List<Integer> linkIntegers = new ArrayList<>(10);
        List<Content> ccs = getQuoteContents().stream()
                .filter(cc -> !cc.getChannelId().equals(this.channelId))
                .filter(cc -> cc.getRecycle().equals(false)).collect(Collectors.toList());
        if (!ccs.isEmpty()) {
            linkIntegers = ccs.stream().map(Content::getChannelId).collect(Collectors.toList());
        }
        return linkIntegers;
    }

    @Transient
    public Boolean isHaveQuote() {
        if (CollectionUtils.isEmpty(getQuoteChannel())) {
            return false;
        }
        return true;
    }

    /**
     * 复制来源栏目名称
     **/
    @Transient
    public String getCopyName() {
        String name = "";
        if (!contentCopys.isEmpty()) {
            name = contentCopys.get(0).getChannelName();
        }
        return name;
    }

    @Transient
    public Boolean getWorkflow() {
        if (getChannel() != null) {
            if (getChannel().getRealWorkflowId() != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * 是否可查看内容
     */
    @Transient
    public boolean getViewContentAble() {
        if (getChannel() != null) {
            return getChannel().getViewContentAble();
        }
        return false;
    }

    /**
     * 是否可修改内容
     */
    @Transient
    public boolean getEditContentAble() {
        if (getChannel() != null) {
            return getChannel().getEditContentAble();
        }
        return false;
    }

    /**
     * 是否可删除
     */
    @Transient
    public boolean getDeleteContentAble() {
        if (getChannel() != null) {
            return getChannel().getDeleteContentAble();
        }
        return false;
    }

    /**
     * 是否可归档
     */
    @Transient
    public boolean getFileContentAble() {
        if (getChannel() != null) {
            return getChannel().getFileContentAble();
        }
        return false;
    }

    /**
     * 是否可置顶
     */
    @Transient
    public boolean getTopContentAble() {
        if (getChannel() != null) {
            return getChannel().getTopContentAble();
        }
        return false;
    }

    /**
     * 是否可移动
     */
    @Transient
    public boolean getMoveContentAble() {
        if (getChannel() != null) {
            return getChannel().getMoveContentAble();
        }
        return false;
    }

    /**
     * 是否可排序
     */
    @Transient
    public boolean getSortContentAble() {
        if (getChannel() != null) {
            return getChannel().getSortContentAble();
        }
        return false;
    }

    /**
     * 是否可复制
     */
    @Transient
    public boolean getCopyContentAble() {
        if (getChannel() != null) {
            return getChannel().getCopyContentAble();
        }
        return false;
    }

    /**
     * 是否可引用
     */
    @Transient
    public boolean getQuoteContentAble() {
        if (getChannel() != null) {
            return getChannel().getQuoteContentAble();
        }
        return false;
    }

    /**
     * 是否可操作内容类型
     */
    @Transient
    public boolean getTypeContentAble() {
        if (getChannel() != null) {
            return getChannel().getTypeContentAble();
        }
        return false;
    }

    /**
     * 是否可建内容
     */
    @Transient
    public boolean getCreateContentAble() {
        if (getChannel() != null) {
            return getChannel().getCreateContentAble();
        }
        return false;
    }

    /**
     * 是否可发布内容
     */
    @Transient
    public boolean getPublishContentAble() {
        if (getChannel() != null) {
            return getChannel().getPublishContentAble();
        }
        return false;
    }

    /**
     * 是否可站群推送
     */
    @Transient
    public boolean getSitePushContentAble() {
        if (getChannel() != null) {
            return getChannel().getSitePushContentAble();
        }
        return false;
    }

    /**
     * 是否可微信推送
     */
    @Transient
    public boolean getWechatPushContentAble() {
        if (getChannel() != null) {
            return getChannel().getWechatPushContentAble();
        }
        return false;
    }

    /**
     * 是否可微博推送
     */
    @Transient
    public boolean getWeiboPushContentAble() {
        if (getChannel() != null) {
            return getChannel().getWeiboPushContentAble();
        }
        return false;
    }

    /**
     * 是否可浏览记录
     */
    @Transient
    public boolean getBrowsingContentAble() {
        if (getChannel() != null) {
            return getChannel().getBrowsingContentAble();
        }
        return false;
    }

    @Transient
    public List<ContentTag> getRealContentTags() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_CONTENT_CONTENTTAG)) {
            return contentTags;
        }
        return null;
    }

    @Transient
    public Integer getRealContentSecretId() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_CONTENT_SECRET)) {
            return contentSecretId;
        }
        return null;
    }

    @Transient
    public Date getRealOfflineTime() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_OFFLINE_TIME)) {
            return offlineTime;
        }
        return null;
    }

    @Transient
    public Boolean getRealReleasePc() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_CONTENT_RELEASE_TERRACE)) {
            return releasePc;
        }
        return true;
    }

    @Transient
    public Boolean getRealRealaseWap() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_CONTENT_RELEASE_TERRACE)) {
            return releaseWap;
        }
        return true;
    }

    @Transient
    public Boolean getRealReleaseApp() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_CONTENT_RELEASE_TERRACE)) {
            return releaseApp;
        }
        return true;
    }

    @Transient
    public Boolean getRealReleaseMiniprogram() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_CONTENT_RELEASE_TERRACE)) {
            return releaseMiniprogram;
        }
        return true;
    }

    @Transient
    public Integer getRealCommentControl() {
        if (getModel().existItem(CmsModelConstant.FIELD_SYS_COMMENT_CONTROL)) {
            return commentControl;
        }
        if (getChannel() != null) {
            return Integer.valueOf(getChannel().getChannelExt().getCommentControl() + "");
        } else {
            return getSite().getConfig().getCommentSet();
        }
    }

//    @Transient
//    public Integer getRealCommentControl() {
//        if (getModel().existItem(CmsModelConstant.FIELD_SYS_COMMENT_CONTROL)) {
//            return commentControl;
//        }
//        if (getChannel() != null) {
//            return Integer.valueOf(getChannel().getChannelExt().getCommentControl() + "");
//        } else {
//            return getSite().getConfig().getCommentSet();
//        }
//    }

    @Transient
    public Boolean getCollection() {
        return collection;
    }

    public void setCollection(Boolean collection) {
        this.collection = collection;
    }

    @Transient
    public String getTopChannelPath() {
        if (getChannel().getTopChannel() != null) {
            return getChannel().getTopChannel().getPath();
        } else {
            return null;
        }
    }

    @Override
    public void setStatusByChannel(Integer status) {
        /** 同步更新内容栏目关联表的内容状态 **/
        setStatus(status);
    }

    @Transient
    public ContentFrontVo getContentFrontVo() {
        return contentFrontVo;
    }

    public void setContentFrontVo(ContentFrontVo contentFrontVo) {
        this.contentFrontVo = contentFrontVo;
    }

    @Column(name = "check_mark", nullable = false, length = 150)
    public String getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(String checkMark) {
        this.checkMark = checkMark;
    }

    @Column(name = "model_field_set", nullable = false, length = 2000)
    public String getModelFieldSet() {
        return modelFieldSet;
    }

    public void setModelFieldSet(String modelFieldSet) {
        this.modelFieldSet = modelFieldSet;
    }

    @Transient
    public AuditStrategy getAuditStrategy() {
        return auditStrategy;
    }

    public void setAuditStrategy(AuditStrategy auditStrategy) {
        this.auditStrategy = auditStrategy;
    }

    @Column(name = "content_pos", length = 6)
    public Short getPos() {
        return pos;
    }

    public void setPos(Short pos) {
        this.pos = pos;
    }

    @Column(name = "ori_content_id", length = 11)
    public Integer getOriContentId() {
        return oriContentId;
    }

    public void setOriContentId(Integer oriContentId) {
        this.oriContentId = oriContentId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ori_content_id", insertable = false, updatable = false)
    public Content getOriContent() {
        return oriContent;
    }

    public void setOriContent(Content oriContent) {
        this.oriContent = oriContent;
    }

    @Where(clause = " deleted_flag=false ")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "oriContent")
    public List<Content> getQuoteContents() {
        return quoteContents;
    }

    public void setQuoteContents(List<Content> quoteContents) {
        this.quoteContents = quoteContents;
    }

    @Column(name = "people_views", length = 11)
    public Integer getPeopleViews() {
        return peopleViews;
    }

    public void setPeopleViews(Integer peopleViews) {
        this.peopleViews = peopleViews;
    }

    @Column(name = "reward_count")
    public Integer getRewardCount() {
        return rewardCount;
    }

    public void setRewardCount(Integer rewardCount) {
        this.rewardCount = rewardCount;
    }

    @Column(name = "paid_count")
    public Integer getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(Integer paidCount) {
        this.paidCount = paidCount;
    }

    @Column(name = "total_amount")
    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Column(name = "reward_amount")
    public Long getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Long rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    @Column(name = "paid_amount")
    public Long getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Long paidAmount) {
        this.paidAmount = paidAmount;
    }

    @Column(name = "last_pay_time")
    public LocalDateTime getLastPayTime() {
        return lastPayTime;
    }

    public void setLastPayTime(LocalDateTime lastPayTime) {
        this.lastPayTime = lastPayTime;
    }

    @Column(name = "is_pay_read")
    public Integer getPayRead() {
        return payRead;
    }

    public void setPayRead(Integer payRead) {
        this.payRead = payRead;
    }

    /**
     * 开启付费阅读
     */
    @Transient
    public Boolean getRealPayRead() {
        if (payRead == null) {
            return false;
        }
        if (WebConstants.INTEGER_TRUE == payRead) {
            if (getModel().existItem(CmsModelConstant.FIELD_SYS_PAY_READ)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有付费金额，如果没有开启付费阅读，那么付费金额为0
     */
    @Transient
    public Double getRealPayPrice() {
        if (getRealPayRead() && getPayPrice() != null) {
            return NumberUtil.div(getPayPrice().doubleValue(), 10000, 2);
        }
        return 0D;
    }

    @Column(name = "pay_price")
    public Long getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(Long payPrice) {
        this.payPrice = payPrice;
    }

    @Column(name = "trial_reading")
    public Integer getTrialReading() {
        return trialReading;
    }

    public void setTrialReading(Integer trialReading) {
        this.trialReading = trialReading;
    }

    @Column(name = "is_pay_praise")
    public Integer getPayPraise() {
        return payPraise;
    }

    public void setPayPraise(Integer payPraise) {
        this.payPraise = payPraise;
    }

    @Column(name = "total_amount_initial")
    public Long getTotalAmountInitial() {
        return totalAmountInitial;
    }

    public void setTotalAmountInitial(Long totalAmountInitial) {
        this.totalAmountInitial = totalAmountInitial;
    }

    @Column(name = "reward_amount_initial")
    public Long getRewardAmountInitial() {
        return rewardAmountInitial;
    }

    public void setRewardAmountInitial(Long rewardAmountInitial) {
        this.rewardAmountInitial = rewardAmountInitial;
    }

    @Column(name = "paid_amount_initial")
    public Long getPaidAmountInitial() {
        return paidAmountInitial;
    }

    public void setPaidAmountInitial(Long paidAmountInitial) {
        this.paidAmountInitial = paidAmountInitial;
    }

    /**
     * 开启内容赞赏
     */
    @Transient
    public Boolean getRealPayPraise() {
        if (payPraise == null) {
            return false;
        }
        if (WebConstants.INTEGER_TRUE == payPraise) {
            if (getModel().existItem(CmsModelConstant.FIELD_SYS_PAY_REWARD)) {
                return true;
            }
        }
        return false;
    }

}