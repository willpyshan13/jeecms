/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.error.SiteErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.*;
import com.jeecms.content.service.ContentFrontService;
import com.jeecms.content.service.ContentRelationService;
import com.jeecms.member.service.UserCollectionService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.CmsSiteConfig;
import com.jeecms.system.domain.ContentTag;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.jeecms.common.constants.WebConstants.ARRAY_SPT;

/**
 * 内容详情
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/7/22 11:38
 */

@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentFrontService service;
    @Autowired
    private ContentRelationService relationService;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private UserCollectionService userCollectionService;

    /**
     * 获取内容分页
     *
     * @param channelIds    栏目id集合
     * @param tagIds        tagId集合
     * @param channelPaths  栏目地址集合
     * @param siteId        站点id
     * @param typeIds       内容类型集合
     * @param title         标题
     * @param isNew         true 新内容
     * @param releaseTarget 1pc 2手机
     * @param isTop         true 置顶
     * @param timeBegin     开始时间
     * @param timeEnd       结束时间
     * @param excludeId     排除内容id集合（tagId不为空生效）
     * @param modelId       模型id数组
     * @param orderBy       排序 {@link ContentConstant}
     * @param pageable      数量
     * @return Page
     */
    @GetMapping("/page")
    @MoreSerializeField({@SerializeField(clazz = Content.class, excludes = {"userComments", "user",
            "publishUser", "contentRecords", "model", "contentChannels", "contentVersions", "contentCopys",
            "secret", "contentTxts", "contentRelations", "site", "channel", "txtByField", "relateChannels",
            "oriContentChannel", "copyContentAble", "relateChannelsAll", "attachmentResAttrMap", "quoteContents", "oriContent"}),
            @SerializeField(clazz = ResourcesSpaceData.class, excludes = {"uploadFtp", "uploadOss", "users",
                    "space", "user", "videoCover"}),
            @SerializeField(clazz = ContentExt.class, excludes = {"content", "sueOrg", "sueYear"}),
            @SerializeField(clazz = ContentTag.class, excludes = {"contentList", "site"}),
            @SerializeField(clazz = ContentAttr.class, excludes = {"cmsOrg", "province", "city", "area", "content"}),
            @SerializeField(clazz = ContentAttrRes.class, excludes = {"secret", "contentAttr"}),
            @SerializeField(clazz = ContentTxt.class, excludes = {"content"}),
    })
    public ResponseInfo page(Integer channelOption, String channelIds, String tagIds, String channelPaths,
                             Integer siteId, String typeIds, String title,
                             Boolean isNew, Integer releaseTarget, Boolean isTop,
                             Date timeBegin, Date timeEnd, String excludeId,
                             String modelId, Integer orderBy, Pageable pageable,
                             HttpServletRequest request) {
        CmsSite site = SystemContextUtils.getSite(request);
        if (siteId == null) {
            siteId = site.getSiteId();
        }
        if (channelOption == null) {
            channelOption = 0;
        }
        String[] channelpath = null;
        if (StringUtils.isNotBlank(channelPaths)) {
            channelpath = StringUtils.split(channelPaths, ARRAY_SPT);
        }
        Security security = getSecurity(request);
        Page<Content> page = service.getPage(channelOption, getIntArray(channelIds), getIntArray(tagIds), channelpath, siteId,
                getIntArray(typeIds), title, isNew, releaseTarget, isTop, timeBegin, timeEnd, getIntArray(excludeId),
                getIntArray(modelId), orderBy == null ? ContentConstant.ORDER_TYPE_SORT_NUM_DESC : orderBy, pageable,
                site, security.getContentSecretIds());
        if (security.isOpenContentSecurity() && security.isOpenAttachmentSecurity()) {
            for (Content content : page) {
                initAnnexSecret(security.getAnnexSecretIds(), content);
            }
        }
        return new ResponseInfo(page);
    }

    /**
     * 相关内容
     *
     * @param contentId 内容id
     * @param orderBy   排序 {@link ContentConstant}
     * @param count     数量
     * @return List
     */
    @MoreSerializeField({@SerializeField(clazz = Content.class, excludes = {"userComments", "user",
            "publishUser", "contentRecords", "model", "contentChannels", "contentVersions", "contentCopys",
            "secret", "contentTxts", "contentRelations", "site", "channel", "txtByField", "relateChannels",
            "oriContentChannel", "copyContentAble", "relateChannelsAll", "attachmentResAttrMap", "quoteContents", "oriContent"}),
            @SerializeField(clazz = ResourcesSpaceData.class, excludes = {"uploadFtp", "uploadOss", "users",
                    "space", "user"}),
            @SerializeField(clazz = ContentExt.class, excludes = {"content", "sueOrg", "sueYear"}),
            @SerializeField(clazz = ContentTag.class, excludes = {"contentList", "site"}),
            @SerializeField(clazz = ContentAttr.class, excludes = {"cmsOrg", "province", "city", "area", "content"}),
            @SerializeField(clazz = ContentAttrRes.class, excludes = {"secret", "contentAttr"}),
            @SerializeField(clazz = ContentTxt.class, excludes = {"content"}),
    })
    @GetMapping("/relation/list")
    public ResponseInfo list(Integer contentId, Integer orderBy, Integer count, HttpServletRequest request) {
        if (contentId == null) {
            return new ResponseInfo(new ArrayList<Content>(0));
        }
        List<ContentRelation> relations = relationService.findByContentId(contentId);
        Integer[] relationIds = new Integer[relations.size()];
        for (int i = 0; i < relations.size(); i++) {
            relationIds[i] = relations.get(i).getRelationContentId();
        }
        Security security = getSecurity(request);
        List<Content> list = service.getList(relationIds,
                orderBy != null ? orderBy : ContentConstant.ORDER_TYPE_SORT_NUM_DESC,
                count, security.getContentSecretIds());
        if (security.isOpenContentSecurity() && security.isOpenAttachmentSecurity()) {
            for (Content content : list) {
                initAnnexSecret(security.getAnnexSecretIds(), content);
            }
        }
        return new ResponseInfo(list);
    }

    private Security getSecurity(HttpServletRequest request) {
        Security security = new Security();
        try {
            Map<String, String> attrs = globalConfigService.get().getAttrs();
            if (attrs.get(GlobalConfigAttr.OPEN_CONTENT_SECURITY) != null && GlobalConfigAttr.TRUE_STRING.equals(attrs.get(GlobalConfigAttr.OPEN_CONTENT_SECURITY))) {
                security.setOpenContentSecurity(true);
                //获取附件密级是否开启
                if (attrs.get(GlobalConfigAttr.OPEN_ATTACHMENT_SECURITY) != null && GlobalConfigAttr.TRUE_STRING.equals(attrs.get(GlobalConfigAttr.OPEN_ATTACHMENT_SECURITY))) {
                    security.setOpenAttachmentSecurity(true);
                }
                CoreUser user = SystemContextUtils.getUser(request);
                if (user != null) {
                    if (user.getUserSecret() != null) {
                        //获取附件密级
                        security.setAnnexSecretIds(new ArrayList<>(user.getUserSecret().getAnnexSecretIds()));
                        //获取内容密级
                        security.setContentSecretIds(new ArrayList<>(user.getUserSecret().getContentSecretIds()));
                    }
                } else {
                    security.setContentSecretIds(new ArrayList<>());
                }
            }
        } catch (GlobalException e) {
            e.getMessage();
        }
        return security;
    }

    /**
     * 通过内容id获取内容
     *
     * @param contentIds 内容id集合
     * @param orderBy    排序 {@link ContentConstant}
     * @return 内容
     */
    @GetMapping("/ids")
    @MoreSerializeField({@SerializeField(clazz = Content.class, excludes = {"userComments", "user",
            "publishUser", "contentRecords", "model", "contentChannels", "contentVersions", "contentCopys",
            "secret", "contentTxts", "contentRelations", "site", "channel", "txtByField", "relateChannels",
            "oriContentChannel", "copyContentAble", "relateChannelsAll", "attachmentResAttrMap", "quoteContents", "oriContent"}),
            @SerializeField(clazz = ResourcesSpaceData.class, excludes = {"uploadFtp", "uploadOss", "users",
                    "space", "user"}),
            @SerializeField(clazz = ContentExt.class, excludes = {"content", "sueOrg", "sueYear"}),
            @SerializeField(clazz = ContentTag.class, excludes = {"contentList", "site"}),
            @SerializeField(clazz = ContentAttr.class, excludes = {"cmsOrg", "province", "city", "area", "content"}),
            @SerializeField(clazz = ContentAttrRes.class, excludes = {"secret", "contentAttr"}),
            @SerializeField(clazz = ContentTxt.class, excludes = {"content"}),
    })
    public ResponseInfo list(String contentIds, Integer orderBy, HttpServletRequest request) {
        Security security = getSecurity(request);
        List<Content> list = service.findAllById(Arrays.asList(getIntArray(contentIds)), orderBy, security.getContentSecretIds());
        if (security.isOpenContentSecurity() && security.isOpenAttachmentSecurity()) {
            for (Content content : list) {
                initAnnexSecret(security.getAnnexSecretIds(), content);
            }
        }
        return new ResponseInfo(list);
    }

    /**
     * 获取列表
     *
     * @param channelOption 0底层栏目查询，只查询一个效率高  1父栏目查询,只查询一个  2引用内容的，查询关联表 效率低适用于数据少数据大不可使用
     * @param channelIds    栏目id集合
     * @param tagIds        tagId集合
     * @param channelPaths  栏目地址集合
     * @param siteId        站点id
     * @param typeIds       内容类型集合
     * @param title         标题
     * @param isNew         true 新内容
     * @param releaseTarget 1pc 2手机
     * @param isTop         true 置顶
     * @param timeBegin     开始时间
     * @param timeEnd       结束时间
     * @param excludeId     排除内容id集合（tagId不为空生效）
     * @param modelId       模型id数组
     * @param orderBy       排序 {@link ContentConstant}}
     * @param count         数量
     * @return 内容集合
     */
    @GetMapping("/list")
    @MoreSerializeField({@SerializeField(clazz = Content.class, excludes = {"userComments", "user",
            "publishUser", "contentRecords", "model", "contentChannels", "contentVersions", "contentCopys",
            "secret", "contentTxts", "contentRelations", "site", "channel", "txtByField", "relateChannels",
            "oriContentChannel", "copyContentAble", "relateChannelsAll", "attachmentResAttrMap", "quoteContents", "oriContent"}),
            @SerializeField(clazz = ResourcesSpaceData.class, excludes = {"uploadFtp", "uploadOss", "users",
                    "space", "user"}),
            @SerializeField(clazz = ContentExt.class, excludes = {"content", "sueOrg", "sueYear"}),
            @SerializeField(clazz = ContentTag.class, excludes = {"contentList", "site"}),
            @SerializeField(clazz = ContentAttr.class, excludes = {"cmsOrg", "province", "city", "area", "content"}),
            @SerializeField(clazz = ContentAttrRes.class, excludes = {"secret", "contentAttr"}),
            @SerializeField(clazz = ContentTxt.class, excludes = {"content"}),
    })
    public ResponseInfo list(Integer channelOption, String channelIds, String tagIds, String channelPaths,
                             Integer siteId, String typeIds, String title,
                             Boolean isNew, Integer releaseTarget, Boolean isTop,
                             Date timeBegin, Date timeEnd, String excludeId,
                             String modelId, Integer orderBy, Integer count,
                             HttpServletRequest request) {
        String[] channelpath = null;
        if (StringUtils.isNotBlank(channelPaths)) {
            channelpath = StringUtils.split(channelPaths, ARRAY_SPT);
        }
        if (channelOption == null) {
            channelOption = ContentConstant.CHANNEL_OPTION_SELF;
        }
        Security security = getSecurity(request);
        List<Content> list = service.getList(channelOption, getIntArray(channelIds), getIntArray(tagIds),
                channelpath, siteId, getIntArray(typeIds), title, isNew, releaseTarget, isTop,
                timeBegin, timeEnd, getIntArray(excludeId), getIntArray(modelId),
                orderBy == null ? ContentConstant.ORDER_TYPE_SORT_NUM_DESC : orderBy, count,
                SystemContextUtils.getSite(request), security.getContentSecretIds());
        if (security.isOpenContentSecurity() && security.isOpenAttachmentSecurity()) {
            for (Content content : list) {
                initAnnexSecret(security.getAnnexSecretIds(), content);
            }
        }
        return new ResponseInfo(list);
    }

    /**
     * 获取内容
     *
     * @param siteId    站点id
     * @param id        内容id
     * @param channelId 栏目id
     * @param next      true 下一个， false 上一个
     * @return 内容
     */
    @GetMapping("/{id:[0-9]+}")
    @MoreSerializeField({@SerializeField(clazz = Content.class, excludes = {"userComments", "user",
            "publishUser", "contentRecords", "model", "contentChannels", "contentVersions", "contentCopys",
            "secret", "contentRelations", "site", "channel", "txtByField", "relateChannels",
            "oriContentChannel", "copyContentAble", "relateChannelsAll", "attachmentResAttrMap", "quoteContents", "oriContent"}),
            @SerializeField(clazz = ResourcesSpaceData.class, excludes = {"uploadFtp", "uploadOss", "users",
                    "space", "user"}),
            @SerializeField(clazz = ContentExt.class, excludes = {"content", "sueOrg", "sueYear"}),
            @SerializeField(clazz = ContentTag.class, excludes = {"contentList", "site"}),
            @SerializeField(clazz = ContentAttr.class, excludes = {"cmsOrg", "province", "city", "area", "content"}),
            @SerializeField(clazz = ContentAttrRes.class, excludes = {"secret", "contentAttr"}),
            @SerializeField(clazz = ContentTxt.class, excludes = {"content"}),
    })
    public ResponseInfo get(Integer siteId, @PathVariable("id") Integer id, Integer channelId,
                            Boolean next, HttpServletRequest request) {
        Security security = getSecurity(request);
        Content content = service.getSide(id, siteId, channelId, next, security.getContentSecretIds());
        /**引用内容采用原内容数据*/
        if (content != null && content.getOriContent() != null) {
            content = content.getOriContent();
            if (security.isOpenContentSecurity() && security.isOpenAttachmentSecurity()) {
                initAnnexSecret(security.getAnnexSecretIds(), content);
            }
        }
        return new ResponseInfo(content);
    }

    /**
     * 统计内容浏览量
     *
     * @param contentId 内容id
     * @return ResponseInfo
     * @throws GlobalException 异常
     */
    @GetMapping("/view")
    public ResponseInfo get(Integer contentId) throws GlobalException {
        if (contentId == null) {
            return new ResponseInfo();
        }
        //统计内容访问量到缓存
        JSONObject jsonObject = service.saveOrUpdateNum(contentId, null, ContentConstant.CONTENT_NUM_TYPE_VIEWS, false);
        //设置收藏状态
        CoreUser user = SystemContextUtils.getCoreUser();
        if (user != null) {
            boolean f = userCollectionService.isHaveCollection(contentId, user.getId());
            jsonObject.put("isCollection", f);
        }
        return new ResponseInfo(jsonObject);
    }


    /**
     * 判断内容是否点赞
     *
     * @param contentId 内容id
     * @param request   {@link HttpServletRequest}
     * @return true 已点赞 false 未点赞
     */
    @GetMapping("/isUp")
    public ResponseInfo get(Integer contentId, HttpServletRequest request) {
        CoreUser user = SystemContextUtils.getUser(request);
        return new ResponseInfo(service.isUp(user, contentId, request));
    }

    /**
     * 内容点赞
     *
     * @param map      内容id
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return ResponseInfo
     * @throws GlobalException 异常
     */
    @PostMapping("/up")
    public ResponseInfo up(@RequestBody Map<String, Integer> map, HttpServletRequest request,
                           HttpServletResponse response) throws GlobalException {
        Integer contentId = map.get("contentId");

        if (contentId == null) {
            return new ResponseInfo(SystemExceptionEnum.INCOMPLETE_PARAM.getCode(),
                    SystemExceptionEnum.INCOMPLETE_PARAM.getDefaultMessage());
        }
        CmsSite site = SystemContextUtils.getSite(request);
        CoreUser user = SystemContextUtils.getUser(request);
        String likeLogin = site.getConfig().getContentLikeLogin();
        //判断点赞是否需要登录
        if (CmsSiteConfig.TRUE_STRING.equals(likeLogin) && user == null) {
            return new ResponseInfo(SiteErrorCodeEnum.LIKE_TO_LOG_IN.getCode(),
                    SiteErrorCodeEnum.LIKE_TO_LOG_IN.getDefaultMessage());
        }
        service.up(user, contentId, request, response);
        //统计新闻点赞数到缓存
        service.saveOrUpdateNum(contentId, null, ContentConstant.CONTENT_NUM_TYPE_UPS, false);
        return new ResponseInfo(true);
    }

    /**
     * 内容取消点赞
     *
     * @param map      内容id
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return ResponseInfo
     * @throws GlobalException 异常
     */
    @PostMapping("/cancelUp")
    public ResponseInfo cancelUp(@RequestBody Map<String, Integer> map, HttpServletRequest request,
                                 HttpServletResponse response) throws GlobalException {
        Integer contentId = map.get("contentId");
        if (contentId == null) {
            return new ResponseInfo(SystemExceptionEnum.INCOMPLETE_PARAM.getCode(),
                    SystemExceptionEnum.INCOMPLETE_PARAM.getDefaultMessage());
        }
        CoreUser user = SystemContextUtils.getUser(request);
        service.cancelUp(user, contentId, request, response);
        //统计新闻点赞数到缓存
        service.saveOrUpdateNum(contentId, null, ContentConstant.CONTENT_NUM_TYPE_UPS, true);
        return new ResponseInfo(true);
    }

    private Integer[] getIntArray(String str) {
        if (StringUtils.isBlank(str)) {
            return new Integer[0];
        }
        String[] arr = StringUtils.split(str, ARRAY_SPT);
        Integer[] ids = new Integer[arr.length];
        int i = 0;
        for (String s : arr) {
            ids[i++] = Integer.valueOf(s);
        }
        return ids;
    }


    private void initAnnexSecret(List<Integer> annexSecretIds, Content bean) {
        Collection<ContentAttr> values = bean.getAttr().values();
        for (ContentAttr attr : values) {
            //判断是否开启了内容密级和附件密级
            if (CmsModelConstant.ANNEX_UPLOAD.equals(attr.getAttrType())) {
                List<ContentAttrRes> attrResList = new ArrayList<>();
                for (ContentAttrRes contentAttrRe : attr.getContentAttrRes()) {
                    //没有密级的附件直接跳过
                    if (contentAttrRe.getSecretId() == null) {
                        continue;
                    }
                    //获取有权限的附件
                    if (annexSecretIds != null && annexSecretIds.contains(contentAttrRe.getSecretId())) {
                        attrResList.add(contentAttrRe);
                    }
                }
                //先清除再添加
                attr.getContentAttrRes().clear();
                attr.getContentAttrRes().addAll(attrResList);
            }
        }
    }

}
