/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.ChannelErrorCodeEnum;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.UnitUtils;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.util.CookieUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.dao.ContentFrontDao;
import com.jeecms.content.domain.*;
import com.jeecms.content.domain.dto.ContentContributeDto;
import com.jeecms.content.domain.dto.ContentSaveDto;
import com.jeecms.content.domain.dto.SpliceCheckUpdateDto;
import com.jeecms.content.domain.vo.ContentContributeVo;
import com.jeecms.content.domain.vo.ContentFrontVo;
import com.jeecms.content.service.*;
import com.jeecms.member.domain.MemberScoreDetails;
import com.jeecms.member.service.MemberScoreDetailsService;
import com.jeecms.publish.domain.ContentLikeRecord;
import com.jeecms.publish.service.ContentLikeRecordService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.system.service.SysAccessRecordService;
import com.jeecms.util.SystemContextUtils;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.jeecms.content.constants.ContentConstant.CONTENT_RELEASE_TERRACE_PC_NUMBER;
import static com.jeecms.content.constants.ContentConstant.CONTENT_RELEASE_TERRACE_WAP_NUMBER;

/**
 * 前台内容Service实现
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/7/19 9:24
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentFrontServiceImpl implements ContentFrontService {

    private Logger logger = LoggerFactory.getLogger(ContentFrontServiceImpl.class);

    private static final String LIKE_COOKIE = "_like_cookie_";

    private static final int INTERVAL = 60 * 30 * 1000;
    /**
     * 最后刷新时间,线程对volatile变量的修改会立刻被其他线程所感知，即不会出现数据脏读的现象，从而保证数据的“可见性”。
     */
    private volatile Long refreshTime = System.currentTimeMillis();
    @Autowired
    private ContentFrontDao dao;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CmsSiteService cmsSiteService;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private ContentExtService contentExtService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private ContentVersionService contentVersionService;
    @Autowired
    private ContentTxtService contentTxtService;
    @Autowired
    private CmsModelItemService cmsModelItemService;
    @Autowired
    private CoreUserService userService;
    @Resource(name = CacheConstants.CONTENT_NUM)
    private Ehcache cache;
    @Autowired
    private CacheProvider cacheProvider;
    @Autowired
    private MemberScoreDetailsService memberScoreDetailsService;
    @Autowired
    private ContentLikeRecordService contentLikeRecordService;
    @Autowired
    private SysAccessRecordService sysAccessRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Page<Content> getPage(Integer channelOption, Integer[] channelIds, Integer[] tagIds, String[] channelPaths,
                                 Integer siteId, Integer[] typeIds, String title, Boolean isNew, Integer releaseTarget,
                                 Boolean isTop, Date timeBegin, Date timeEnd, Integer[] excludeId, Integer[] modelId,
                                 Integer orderBy, Pageable pageable, CmsSite site, List<Integer> contentSecretIds) {
        if (releaseTarget == null) {
            releaseTarget = SystemContextUtils.isPc() ? CONTENT_RELEASE_TERRACE_PC_NUMBER : CONTENT_RELEASE_TERRACE_WAP_NUMBER;
        }
        Date date = null;
        if (isNew != null && isNew) {
            date = getNewContentTime(site);
        }
        List<Integer> channels = new ArrayList<>();
        if (channelIds != null && channelIds.length > 0) {
            channels.addAll(Arrays.asList(channelIds));
        }
        if (channelPaths != null) {
            List<Channel> ccs = channelService.findByPath(channelPaths, site.getId());
            channels.addAll(Channel.fetchIds(ccs));
        }
        if (!channels.isEmpty()) {
            channels = channels.parallelStream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            channelPaths = null;
            channelIds = channels.toArray(new Integer[0]);
        }
        if (siteId == null && (channelIds == null || channelIds.length == 0) && typeIds == null) {
            siteId = site.getId();
        }
        return dao.getPage(channelOption, channelIds, tagIds, channelPaths, siteId, typeIds, title, date,
                releaseTarget, isTop, timeBegin, timeEnd, excludeId, modelId, orderBy, contentSecretIds, pageable);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Page<Content> getPage(Integer siteId, Integer userId, Integer status, String title,
                                 Date startDate, Date endDate, List<Integer> contentSecretIds, Pageable pageable) {
        return dao.getPage(siteId, userId, status, title, startDate, endDate, contentSecretIds, pageable);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Content> getList(Integer channelOption, Integer[] channelIds, Integer[] tagIds,
                                 String[] channelPaths, Integer siteId,
                                 Integer[] typeIds, String title,
                                 Boolean isNew, Integer releaseTarget,
                                 Boolean isTop, Date timeBegin,
                                 Date timeEnd, Integer[] excludeId,
                                 Integer[] modelId, Integer orderBy,
                                 Integer count, CmsSite site, List<Integer> contentSecretIds) {
        if (releaseTarget == null) {
            releaseTarget = SystemContextUtils.isPc() ? CONTENT_RELEASE_TERRACE_PC_NUMBER : CONTENT_RELEASE_TERRACE_WAP_NUMBER;
        }
        Date date = null;
        if (isNew != null && isNew) {
            date = getNewContentTime(site);
        }
        List<Integer> channels = new ArrayList<>();
        if (channelIds != null) {
            channels.addAll(Arrays.asList(channelIds));
        }
        if (channelPaths != null) {
            List<Channel> ccs = channelService.findByPath(channelPaths, site.getId());
            channels.addAll(Channel.fetchIds(ccs));
        }
        if (!CollectionUtils.isEmpty(channels)) {
            channels = channels.parallelStream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            channelPaths = null;
            channelIds = channels.toArray(new Integer[0]);
        }
        if (siteId == null && (channelIds == null || channelIds.length == 0) && typeIds == null) {
            siteId = site.getId();
        }
        List<Content> list = dao.getList(channelOption, channelIds, tagIds, channelPaths, siteId, typeIds, title, date,
                releaseTarget, isTop, timeBegin, timeEnd, excludeId, modelId, orderBy, count, contentSecretIds);
        List<Content> realContent = new ArrayList<>();
        for (Content content : list) {
            if(content.getOriContentId() != null) {
                realContent.add(content.getOriContent());
            } else {
                realContent.add(content);
            }
        }
        return realContent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Content> getList(Integer[] relationIds, Integer orderBy, Integer count, List<Integer> contentSecretIds) {
        return dao.getList(relationIds, orderBy, count, contentSecretIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Content> findAllById(List<Integer> ids, Integer orderBy, List<Integer> contentSecretIds) {
        if (ids == null) {
            return new ArrayList<>(0);
        }
        List<Content> byIds = dao.findByIds(ids, orderBy, contentSecretIds);
        List<Content> realContent = new ArrayList<>();
        for (Content content : byIds) {
            if(content.getOriContentId() != null) {
                realContent.add(content.getOriContent());
            } else {
                realContent.add(content);
            }
        }
        return realContent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Content findById(Integer id) {
        return contentService.findById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Content getSide(Integer id, Integer siteId, Integer channelId, Boolean next, List<Integer> contentSecretIds) {
        return dao.getSide(id, siteId, channelId, next, contentSecretIds, true);
    }

    @Override
    public void contribute(ContentContributeDto dto) throws GlobalException {
        /*
         * 此处可能会出现一种情况：
         * 1. 后台查看可能不存在正文(模型不存在正文)
         * 2. 前台查看可能出现正文(模型字段写死)
         */
        Channel channel = channelService.findById(dto.getChannnelId());
        if (channel == null) {
            throw new GlobalException(
                    new SystemExceptionInfo(
                            ChannelErrorCodeEnum.CHANNEL_ID_PASSED_ERROR.getDefaultMessage(),
                            ChannelErrorCodeEnum.CHANNEL_ID_PASSED_ERROR.getCode()));
        }
        CmsSite site = cmsSiteService.findById(channel.getSiteId());
        Content content = new Content();
        // 前台投稿使用的模型是默认为"排序第一"的模型
        List<CmsModel> cmsModels = cmsModelService.findList(CmsModel.CONTENT_TYPE, site.getId());
        if (CollectionUtils.isEmpty(cmsModels)) {
            throw new GlobalException(
                    new SystemExceptionInfo(
                            SettingErrorCodeEnum.MODEL_NOT_SET.getDefaultMessage(),
                            SettingErrorCodeEnum.MODEL_NOT_SET.getCode()));
        }
        Integer modelId = cmsModels.get(0).getId();
        content.setModelId(modelId);
        content.setModel(cmsModelService.findById(modelId));
        content.setUserId(SystemContextUtils.getUserId(RequestUtils.getHttpServletRequest()));
        content = dto.initContent(content, dto, channel, site.getCmsSiteCfg(), site.getId(), channel.getRealWorkflowId() != null ? true : false, false);
        ContentExt contentExt = new ContentExt();
        contentExt = contentExtService.initContributeContentExt(contentExt, site.getId(), dto, dto.getChannnelId(), modelId);
        content.setContentExt(contentExt);
        contentExt.setContent(content);
        content.setContentSecretId(0);
        Content bean = contentService.save(content);
        bean.setSite(site);
        // 如果是提交，获取积分
        contentService.flush();
        if (dto.getIsSubmit()) {
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.CONTRIBUTOR_SCORE_TYPE, bean.getUserId(), bean.getSiteId(), null);
        }
        List<CmsModelItem> items = cmsModelItemService.findByModelId(modelId);
        if (!CollectionUtils.isEmpty(items)) {
            items = items.stream().filter(item -> CmsModelConstant.CONTENT_TXT.equals(item.getDataType())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(items)) {
                if (items.size() > 1) {
                    items = items.stream()
                            .sorted(Comparator.comparing(CmsModelItem::getSortNum)).collect(Collectors.toList());
                }
                String field = items.get(0).getField();
                Map<String, String> txtMap = new HashMap<>(16);
                txtMap.put(field, dto.getContxt());
                if (Content.AUTOMATIC_SAVE_VERSION_TRUE.equals(site.getConfig().getContentSaveVersion())) {
                    // 此处Map无需处理为空的情况在其具体方法中已经处理了
                    contentVersionService.save(txtMap, bean.getId(), null);
                }
                ContentTxt contentTxt = new ContentTxt(field, dto.getContxt(), bean.getId());
                contentTxt.setContent(bean);
                contentTxtService.save(contentTxt);
            }
        }
        bean.setCreateType(ContentConstant.CONTENT_CREATE_TYPE_CONTRIBUTE);
        contentService.initContentObject(bean);
        contentService.initContentExtObject(bean.getContentExt());
        if (channel.getRealWorkflowId() != null) {
            contentService.submit(null, null, true, bean);
        }
    }


    @Override
    public void updateContribute(ContentContributeDto dto, Channel channel,
                                 Content content, HttpServletRequest request,
                                 ContentExt contentExt) throws GlobalException {
        final SpliceCheckUpdateDto oldDto = contentService.initSpliceCheckUpdateDto(content);
        content.setChannelId(dto.getChannnelId());
        content.setChannel(channel);
        content.setTitle(dto.getTitle());
        contentExt.setDescription(dto.getDescription());
        contentExt.setAuthor(dto.getAuthor());
        ContentExt beanExt = contentExtService.updateAll(contentExt);
        content.setContentExt(beanExt);
        if (dto.getIsSubmit()) {
            content.setStatus(ContentConstant.STATUS_FIRST_DRAFT);
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.CONTRIBUTOR_SCORE_TYPE, content.getUserId(), content.getSiteId(), null);
        }
        content.setContentSecretId(0);
        if (dto.getPayRead() != null) {
            content.setPayRead(dto.getPayRead().getPayRead());
            if (WebConstants.INTEGER_TRUE == dto.getPayRead().getPayRead()) {
                content.setPayPrice(dto.getPayRead().getPayPrice());
                content.setTrialReading(dto.getPayRead().getTrialReading());
            }
        }
        content.setPayPraise(dto.getReward());
        Content bean = contentService.update(content);
        contentService.flush();
        CmsSite site = cmsSiteService.findById(content.getSiteId());
        List<CmsModelItem> items = cmsModelItemService.findByModelId(content.getModelId());
        if (CollUtil.isNotEmpty(items)) {
            items = items.stream().filter(item -> CmsModelConstant.CONTENT_TXT.equals(item.getDataType()))
                    .collect(Collectors.toList());
            if (items.size() > 1) {
                items = items.stream().sorted(Comparator.comparing(CmsModelItem::getSortNum))
                        .collect(Collectors.toList());
            }
            String field = items.get(0).getField();
            Map<String, String> txtMap = new HashMap<>(16);
            List<ContentTxt> contentTxts = contentTxtService.getTxts(bean.getId());
            for (ContentTxt contentTxt : contentTxts) {
                txtMap.put(contentTxt.getAttrKey(), contentTxt.getAttrTxt());
            }
            if (Content.AUTOMATIC_SAVE_VERSION_TRUE.equals(site.getConfig().getContentSaveVersion())) {
                // 此处Map无需处理为空的情况在其具体方法中已经处理了
                contentVersionService.save(txtMap, bean.getId(), null);
            }
            if (CollUtil.isNotEmpty(contentTxts)) {
                for (ContentTxt contentTxt : contentTxts) {
                    if (field.equals(contentTxt.getAttrKey())) {
                        contentTxt.setAttrTxt(dto.getContxt());
                    }
                }
                contentTxtService.batchUpdate(contentTxts);
            } else {
                ContentTxt contentTxt = new ContentTxt(field, dto.getContxt(), bean.getId());
                contentTxt.setContent(bean);
                contentTxtService.save(contentTxt);
            }
        }
        SpliceCheckUpdateDto newDto = contentService.initSpliceCheckUpdateDto(bean);
        GlobalConfig globalConfig = SystemContextUtils.getGlobalConfig(request);
        contentService.checkUpdate(oldDto, newDto, globalConfig, bean, SystemContextUtils.getUserId(request));
        contentService.initContentObject(bean);
        contentService.initContentExtObject(bean.getContentExt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public ContentContributeVo splicContributeVo(Integer contentId, Integer userId) {
        ContentContributeVo contentContributeVo = dao.findContributoVo(contentId, null);
        if (!userId.equals(contentContributeVo.getUserId())) {
            return null;
        }
        String modelFiled = cmsModelItemService.getModelItemByDataType(contentContributeVo.getModelId(), CmsModelConstant.CONTENT_TXT);
        if (StringUtils.isNotBlank(modelFiled)) {
            List<ContentTxt> txts = contentTxtService.getTxts(contentId);
            if (CollUtil.isNotEmpty(txts)) {
                for (ContentTxt contentTxt : txts) {
                    if (modelFiled.equals(contentTxt.getAttrKey())) {
                        contentContributeVo.setContxt(contentTxt.getAttrTxt());
                        break;
                    }
                }
            }
        }
        ContentContributeVo.ContentPayRead payRead = new ContentContributeVo.ContentPayRead();
        if (contentContributeVo.getPayPrice() != null) {
            payRead.setPayPrice(UnitUtils.convertHaoToYuan(contentContributeVo.getPayPrice()));
        }
        payRead.setPayRead(contentContributeVo.getPayread());
        payRead.setTrialReading(contentContributeVo.getTrialReading());
        contentContributeVo.setPayRead(payRead);
        return contentContributeVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public boolean isUp(CoreUser user, Integer contentId, HttpServletRequest request) {
        if (user != null) {
            Content content = findById(contentId);
            List<Content> list = user.getLikeContents();
            //如果存在该点赞记录
            return list.contains(content);
        } else {
            String cookieName = LIKE_COOKIE + contentId;
            Cookie cookie = CookieUtils.getCookie(request, cookieName);
            String cookieValue;
            if (cookie != null && !StringUtils.isBlank(cookie.getValue())) {
                cookieValue = cookie.getValue();
            } else {
                cookieValue = null;
            }
            //如果cookieValue不为空表示已点赞
            return cookieValue != null;
        }
    }

    @Override
    public void up(CoreUser user, Integer contentId, HttpServletRequest request,
                   HttpServletResponse response) throws GlobalException {
        Content content = findById(contentId);
        if (user != null) {
            List<Content> list = user.getLikeContents();
            if (!list.contains(content)) {
                list.add(content);
                user.setLikeContents(list);
                userService.update(user);
                //新增点赞数据记录表
                contentLikeRecordService.save(new ContentLikeRecord(user.getId(),
                        content.getChannelId(), content.getId()));
            }
        } else {
            String cookieName = LIKE_COOKIE + contentId;
            Cookie cookie = CookieUtils.getCookie(request, cookieName);
            String cookieValue;
            if (cookie != null && !StringUtils.isBlank(cookie.getValue())) {
                cookieValue = cookie.getValue();
            } else {
                cookieValue = null;
            }
            //如果cookieValue为空表示可以点赞
            if (cookieValue == null) {
                cookieValue = StringUtils.remove(UUID.randomUUID().toString(), "-");
                CookieUtils.addCookie(request, response, cookieName, cookieValue, Integer.MAX_VALUE, null, null);
                //新增点赞数据记录表
                contentLikeRecordService.save(new ContentLikeRecord(cookieValue,
                        content.getChannelId(), content.getId()));
            }
        }
    }

    @Override
    public void cancelUp(CoreUser user, Integer contentId, HttpServletRequest request,
                         HttpServletResponse response) throws GlobalException {
        if (user == null) {
            String cookieName = LIKE_COOKIE + contentId;
            Cookie cookie = CookieUtils.getCookie(request, cookieName);
            if (null != cookie && !StringUtils.isBlank(cookie.getValue())) {
                CookieUtils.cancleCookie(request, response, cookieName, null);
                //取消点赞删除
                contentLikeRecordService.deleteByCookieAndContentId(cookieName, contentId);
            }
        } else {
            List<Content> list = user.getLikeContents();
            List<Content> contents = new ArrayList<>();
            for (Content bean : list) {
                if (!bean.getId().equals(contentId)) {
                    contents.add(bean);
                }
            }
            user.setLikeContents(contents);
            userService.update(user);
            //取消点赞删除
            contentLikeRecordService.deleteByUserIdAndContentId(user.getId(), contentId);
        }
    }

    @Override
    public JSONObject saveOrUpdateNum(Integer contentId, Integer commentNum, String type, boolean isDeleted)
            throws GlobalException {
        JSONObject returnJson = new JSONObject();
        if (cache.isKeyInCache(contentId) && cache.getQuiet(contentId) != null) {
            JSONObject json = JSONObject.parseObject(String.valueOf(cache.get(contentId).getObjectValue()));
            Integer num = 0;
            Integer dayNum = 0;
            switch (type) {
                case ContentConstant.CONTENT_NUM_TYPE_VIEWS:
                    num = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_VIEWS);
                    num = this.countNum(num, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_VIEWS, num);
                    dayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_VIEWS);
                    dayNum = this.countNum(dayNum, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_VIEWS, dayNum);
                    break;
                case ContentConstant.CONTENT_NUM_TYPE_COMMENTS:
                    num = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_COMMENTS);
                    num = this.countNum(num, commentNum, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_COMMENTS, num);
                    dayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_COMMENTS);
                    dayNum = this.countNum(dayNum, commentNum, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_COMMENTS, dayNum);
                    break;
                case ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS:
                    num = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS);
                    num = this.countNum(num, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS, num);
                    dayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNLOADS);
                    dayNum = this.countNum(dayNum, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNLOADS, dayNum);
                    break;
                case ContentConstant.CONTENT_NUM_TYPE_UPS:
                    num = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_UPS);
                    num = this.countNum(num, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_UPS, num);
                    dayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_UPS);
                    dayNum = this.countNum(dayNum, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_UPS, dayNum);
                    break;
                case ContentConstant.CONTENT_NUM_TYPE_DOWNS:
                    num = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DOWNS);
                    num = this.countNum(num, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DOWNS, num);
                    dayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNS);
                    dayNum = this.countNum(dayNum, null, isDeleted);
                    json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNS, dayNum);
                    break;
                default:
                    break;
            }
            cache.put(new Element(contentId, json));
            returnJson = JSONObject.parseObject(String.valueOf(cache.get(contentId).getObjectValue()));
            this.refreshToDB();
        } else {
            returnJson = this.saveEhcache(contentId);
            if (returnJson == null) {
                return null;
            }
            this.saveOrUpdateNum(contentId, commentNum, type, isDeleted);
        }
        return returnJson;
    }

    /**
     * 计算数值
     */
    private int countNum(int num, Integer commentNum, boolean isDeleted) {
        if (isDeleted) {
            if (commentNum != null) {
                if (commentNum > 0) {
                    num = num - commentNum;
                }
                if (num < 0) {
                    num = 0;
                }
            } else {
                num--;
                if (num < 0) {
                    num = 0;
                }
            }
        } else {
            num++;
        }
        return num;
    }

    /**
     * 新增缓存
     */
    private JSONObject saveEhcache(Integer contentId) {
        Content content = contentService.findById(contentId);
        if (content == null) {
            return null;
        }
        ContentExt contentExt = content.getContentExt();
        JSONObject json = new JSONObject();
        json.put(ContentConstant.CONTENT_NUM_TYPE_VIEWS, content.getViews());
        json.put(ContentConstant.CONTENT_NUM_TYPE_COMMENTS, content.getComments());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS, content.getDownloads());
        json.put(ContentConstant.CONTENT_NUM_TYPE_UPS, content.getUps());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DOWNS, content.getDowns());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_VIEWS, contentExt.getViewsDay());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_COMMENTS, contentExt.getCommentsDay());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNLOADS, contentExt.getDownloadsDay());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_UPS, contentExt.getUpsDay());
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNS, contentExt.getDownsDay());
        cache.put(new Element(contentId, json));
        // 使用refreshTime是为了防止缓存丢失的情况
        if (cacheProvider.exist(CacheConstants.TIME, ContentConstant.CONTENT_NUM_TYPE_END_TIME)) {
            cacheProvider.setCache(CacheConstants.TIME, ContentConstant.CONTENT_NUM_TYPE_END_TIME, System.currentTimeMillis());
        } else {
            cacheProvider.setCache(CacheConstants.TIME, ContentConstant.CONTENT_NUM_TYPE_END_TIME, refreshTime);
        }
        return json;
    }

    /**
     * 判断持久化到数据库与处理数据
     * 每周、每月、每日处理缓存与更新数据库
     */
    private void refreshToDB() throws GlobalException {
        if (cacheProvider.exist(CacheConstants.TIME, ContentConstant.CONTENT_NUM_TYPE_END_TIME)) {
            cacheProvider.setCache(CacheConstants.TIME,
                    ContentConstant.CONTENT_NUM_TYPE_END_TIME, refreshTime);
        }
        long newRefreshTime = Long.valueOf(
                String.valueOf(cacheProvider.getCache(CacheConstants.TIME,
                        ContentConstant.CONTENT_NUM_TYPE_END_TIME)));
        long time = System.currentTimeMillis();
        if (time > newRefreshTime + INTERVAL) {
            this.saveToDB(cache);
            //直接加重锁
            synchronized (refreshTime) {
                refreshTime = time;
            }
        }
        long dayInitTime = MyDateUtils.getStartDate(new Date()).getTime();
        if (newRefreshTime < dayInitTime) {
            this.processDayNum(cache);
        }
        long weekInitTime = MyDateUtils.getSpecficWeekStart(new Date(), 0).getTime();
        if (newRefreshTime < weekInitTime) {
            this.processWeekNum(cache);
        }
        long monthInitTime = MyDateUtils.getSpecficMonthStart(new Date(), 0).getTime();
        if (newRefreshTime < monthInitTime) {
            this.processMonthNum(cache);
        }
        // 处理结束后更新重置时间
        cacheProvider.setCache(CacheConstants.TIME, ContentConstant.CONTENT_NUM_TYPE_END_TIME, System.currentTimeMillis());
    }

    /**
     * 每隔一段时间将数据初始化到数据库中
     */
    @SuppressWarnings("unchecked")
    private void saveToDB(Ehcache cache) throws GlobalException {
        ThreadUtil.execute(() -> {
            List<Integer> keys = cache.getKeys();
            if (CollectionUtils.isEmpty(keys)) {
                return;
            }
            int count = 1;
            /**分批处理*/
            Iterator<Integer> it = keys.iterator();
            List<Integer> toUpdateIds = new ArrayList<>();
            try {
                while (it.hasNext()) {
                    toUpdateIds.add(it.next());
                    count++;
                    if (count % 100 == 0) {
                        doBatchSaveToDB(toUpdateIds);
                        toUpdateIds.clear();
                        ThreadUtil.safeSleep(1000);
                    }
                }
                doBatchSaveToDB(toUpdateIds);
            } catch (GlobalException e) {
                logger.error(e.getMessage());
            }
            toUpdateIds.clear();
        });
    }

    private void doBatchSaveToDB(List<Integer> keys) throws GlobalException {
        List<Content> contents = contentService.findAllById(keys);
        Map<Integer, Content> contentMap = new HashMap<>(16);
        if (!CollectionUtils.isEmpty(contents)) {
            contents = contents.stream().filter(content -> !content.getRecycle() && !content.getHasDeleted()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(contents)) {
                contentMap = contents.stream().collect(Collectors.toMap(Content::getId, content -> content));
            }
        }
        contents.clear();
        Element element;
        JSONObject json;
        for (Integer it : keys) {
            element = cache.get(it);
            if (element != null) {
                Content content = contentMap.get(it);
                if (content != null) {
                    json = JSONObject.parseObject(String.valueOf(element.getObjectValue()));
                    content.setViews(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_VIEWS));
                    content.setComments(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_COMMENTS));
                    content.setUps(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_UPS));
                    content.setDowns(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DOWNS));
                    content.setDownloads(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS));
                    ContentExt contentExt = content.getContentExt();
                    contentExt.setViewsDay(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_VIEWS));
                    contentExt.setCommentsDay(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_COMMENTS));
                    contentExt.setDownloadsDay(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNLOADS));
                    contentExt.setUpsDay(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_UPS));
                    contentExt.setDownsDay(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNS));
                    content.setContentExt(contentExt);
                    contents.add(content);
                }
            }
        }
        channge(keys);
        contentService.batchUpdate(contents);
    }

    public void channge(List<Integer> keys) throws GlobalException {
        logger.info("keys.size->" + keys.size());
        int count = 1;
        /**分批处理*/
        Iterator<Integer> it = keys.iterator();
        List<Integer> toUpdateIds = new ArrayList<>();
        while (it.hasNext()) {
            toUpdateIds.add(it.next());
            count++;
            if (count % 100 == 0) {
                doStaticContentViews(toUpdateIds);
                toUpdateIds.clear();
                ThreadUtil.safeSleep(1000);
            }
        }
        doStaticContentViews(toUpdateIds);
        toUpdateIds.clear();
    }

    private void doStaticContentViews(List<Integer> keys) {
        List<Content> contents = new ArrayList<>();
        for(Integer id:keys){
            Content content = contentService.findById(id);
            content.setPeopleViews((int) sysAccessRecordService.getPeopleViews(id));
            contents.add(content);
        }
        //异步处理浏览人数
        try {
            contentService.batchUpdate(contents);
        } catch (GlobalException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 初始化修改本日的缓存和数据
     */
    private void processDayNum(Ehcache cache) throws GlobalException {
        this.processNum(cache, 1);
    }

    /**
     * 初始化修改本周的缓存和数据
     */
    private void processWeekNum(Ehcache cache) throws GlobalException {
        this.processNum(cache, 2);
    }

    /**
     * 初始化修改本月的缓存和数据
     */
    private void processMonthNum(Ehcache cache) throws GlobalException {
        this.processNum(cache, 3);
    }

    /**
     * 处理数据
     */
    @SuppressWarnings("unchecked")
    private void processNum(Ehcache cache, Integer status) throws GlobalException {
        List<Integer> keys = cache.getKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        List<Content> contents = contentService.findAllById(keys);
        Map<Integer, Content> contentMap = new HashMap<>(16);
        if (!CollectionUtils.isEmpty(contents)) {
            contentMap = contents.stream().collect(Collectors.toMap(Content::getId, content -> content));
        }
        contents.clear();
        Element element;
        JSONObject json;
        for (Integer it : keys) {
            element = cache.get(it);
            if (element != null) {
                Content content = contentMap.get(it);
                if (content != null) {
                    json = JSONObject.parseObject(String.valueOf(element.getObjectValue()));
                    this.splicContentNum(json, content, status);
                    this.initEhcache(it, json);
                    contents.add(content);
                }
            }
        }
        contentService.batchUpdate(contents);
        cacheProvider.setCache(CacheConstants.TIME, ContentConstant.CONTENT_NUM_TYPE_END_TIME, System.currentTimeMillis());
    }

    /**
     * 初始化缓存
     */
    private void initEhcache(Integer contentId, JSONObject json) {
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_VIEWS, 0);
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_COMMENTS, 0);
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNLOADS, 0);
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_UPS, 0);
        json.put(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNS, 0);
        cache.put(new Element(contentId, json));
    }

    /**
     * 计算内容中的数值数据
     */
    private void splicContentNum(JSONObject json, Content content, Integer status) {
        content.setViews(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_VIEWS));
        content.setComments(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_COMMENTS));
        content.setUps(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_UPS));
        content.setDowns(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DOWNS));
        content.setDownloads(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS));
        ContentExt contentExt = content.getContentExt();
        Integer viewsDayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_VIEWS);
        contentExt.setViewsDay(0);
        contentExt.setViewsWeek(contentExt.getViewsWeek() + viewsDayNum);
        contentExt.setViewsMonth(contentExt.getViewsMonth() + viewsDayNum);
        Integer commentsDayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_COMMENTS);
        contentExt.setCommentsDay(0);
        contentExt.setCommentsWeek(contentExt.getCommentsWeek() + commentsDayNum);
        contentExt.setCommentsMonth(contentExt.getCommentsMonth() + commentsDayNum);
        Integer downloadsDayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNLOADS);
        contentExt.setDownloadsDay(0);
        contentExt.setDownloadsWeek(contentExt.getCommentsWeek() + downloadsDayNum);
        contentExt.setDownloadsMonth(contentExt.getCommentsMonth() + downloadsDayNum);
        Integer upsDayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_UPS);
        contentExt.setUpsDay(0);
        contentExt.setUpsWeek(contentExt.getUpsWeek() + upsDayNum);
        contentExt.setUpsMonth(contentExt.getCommentsMonth() + upsDayNum);
        Integer downsDayNum = json.getInteger(ContentConstant.CONTENT_NUM_TYPE_DAY_DOWNS);
        contentExt.setDownsDay(0);
        contentExt.setDownsWeek(contentExt.getDownloadsWeek() + downsDayNum);
        contentExt.setDownsMonth(contentExt.getDownloadsMonth() + downsDayNum);

        if (status >= 2) {
            contentExt.setViewsWeek(0);
            contentExt.setCommentsWeek(0);
            contentExt.setDownloadsWeek(0);
            contentExt.setUpsWeek(0);
            contentExt.setDownsWeek(0);
        }
        if (status == 3) {
            contentExt.setViewsMonth(0);
            contentExt.setCommentsMonth(0);
            contentExt.setDownloadsMonth(0);
            contentExt.setUpsMonth(0);
            contentExt.setDownsMonth(0);
        }
        content.setContentExt(contentExt);
    }

    /**
     * 获取新新闻时间
     *
     * @param site 站点
     * @return Date
     */
    private Date getNewContentTime(CmsSite site) {
        boolean openContentNewFlag = site.getConfig().getOpenContentNewFlag();
        Integer contentFlagType = site.getConfig().getContentNewFlagType();
        Integer contentNewFlag = site.getConfig().getContentNewFlag();
        if (openContentNewFlag && contentFlagType != null && contentNewFlag != null) {
            Date date = Calendar.getInstance().getTime();
            return contentFlagType == 1
                    ? MyDateUtils.getSpecficDate(date, -contentNewFlag)
                    : MyDateUtils.getHourAfterTime(date, -contentNewFlag);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Page<ContentFrontVo> getMobilePage(Integer siteId, Integer userId, Integer status, List<Integer> contentSecretIds, Pageable pageable) {
        /*
         * PC端已写死，已发布、待审核、暂存的情况，但是手机端只存在已发布及待审核，所以此次进行此处理
         */
        if (status == ContentConstant.CONTRIBUTE_TEMPORARY_STORAGE) {
            status = ContentConstant.CONTRIBUTE_RELEASE;
        }
        Page<Content> contents = dao.getPage(siteId, userId, status, null, null, null, contentSecretIds, pageable);
        List<ContentFrontVo> vos = this.initMobileVos(contents);
        vos = vos.stream()
                .skip(pageable.getPageSize() * (pageable.getPageNumber()))
                .limit(pageable.getPageSize()).collect(Collectors.toList());
        return new PageImpl<>(vos, pageable, vos.size());
    }

    /**
     * 初始化"内容手机版显示vo"集合
     *
     * @param contents 分页内容集合
     * @Title: initMobileVos
     * @return: List
     */
    private List<ContentFrontVo> initMobileVos(Page<Content> contents) {
        List<ContentFrontVo> vos = new ArrayList<>();
        for (Content content : contents) {
            ContentFrontVo vo = new ContentFrontVo();
            vo = this.initMobileVo(vo, content);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 初始化"内容手机版显示vo"对象
     *
     * @param vo      内容手机版显示vo
     * @param content 内容对象
     * @Title: initMobileVo
     * @return: ContentMobileVo
     */
    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public ContentFrontVo initMobileVo(ContentFrontVo vo, Content content) {
        Integer contentId = content.getId();
        vo.setId(contentId);
        vo.setTitle(content.getTitle());
        vo.setTitleIsBold(content.getTitleIsBold());
        vo.setTitleColor(content.getTitleColor());
        vo.setChannelName(content.getChannelName());
        vo.setCreateTime(MyDateUtils.getTime(content.getCreateTime()));
        vo.setUrl(content.getUrlWhole());
        vo.setPublishTime(content.getReleaseTimeString());
        vo.setModelId(content.getModelId());
        JSONObject json = null;
        // 校验是否存在缓存块，如果是则直接处理初始化数值操作，如果不存在，则初始化缓存，再处理初始化数值操作
        if (cache.isKeyInCache(contentId) && cache.getQuiet(contentId) != null) {
            json = JSONObject.parseObject(String.valueOf(cache.get(contentId).getObjectValue()));
            this.initMobileNumVo(vo, json);
        } else {
            json = this.saveEhcache(contentId);
            this.initMobileNumVo(vo, json);
        }
        if (json == null) {
            return null;
        }
        vo = this.initMobileJsonVo(content, vo);
        return vo;
    }

    private ContentFrontVo initMobileJsonVo(Content content, ContentFrontVo vo) {
        List<CmsModelItem> items = null;
        try {
            items = cmsModelItemService.findByModelId(content.getModelId());
        } catch (GlobalException e) {
            e.printStackTrace();
        }
        if (items == null) {
            items = new ArrayList<>();
        }
        List<ContentAttr> attrs = content.getContentAttrs();
        if (attrs == null) {
            attrs = new ArrayList<>();
        }
        // 初始化视频、图片的JSON串，和"多图上传"字段，string集合
        vo.setVideoJson(vo.initVideoJson(items, attrs));
        vo.setImageJson(vo.initImageJson(items, attrs, content));
        vo.setMultiImageUploads(vo.initMultiImageUploads(items));
        return vo;
    }

    /**
     * 初始化"内容手机版显示vo"的数值
     *
     * @param vo   内容手机版显示vo对象
     * @param json 储存content数值的缓存的json串
     * @Title: initMobileNumVo
     * @return: ContentMobileVo
     */
    private ContentFrontVo initMobileNumVo(ContentFrontVo vo, JSONObject json) {
        vo.setViews(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_VIEWS));
        vo.setComments(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_COMMENTS));
        vo.setUps(json.getInteger(ContentConstant.CONTENT_NUM_TYPE_UPS));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public ContentFrontVo initPartVo(Content content) throws GlobalException {
        ContentFrontVo vo = new ContentFrontVo();
        vo.setModelId(content.getModelId());
        return this.initMobileJsonVo(content, vo);
    }

}
