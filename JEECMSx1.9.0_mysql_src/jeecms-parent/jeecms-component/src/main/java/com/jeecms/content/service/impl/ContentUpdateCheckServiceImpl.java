/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.*;
import com.jeecms.content.domain.dto.ContentUpdateDto;
import com.jeecms.content.service.CmsModelItemService;
import com.jeecms.content.service.ContentService;
import com.jeecms.content.service.ContentUpdateCheckService;
import com.jeecms.content.util.ContentInitUtils;
import com.jeecms.protection.service.GradeProtectionService;
import com.jeecms.system.domain.ContentSource;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.service.GlobalConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容扩展的service实现类
 *
 * @author: chenming
 * @date: 2020/3/31 17:16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentUpdateCheckServiceImpl implements ContentUpdateCheckService {

    /**
     * 校验内容状态集合：已审核、下线
     */
    private static List<Integer> CHECK_STATUS = Arrays.asList(ContentConstant.STATUS_WAIT_PUBLISH, ContentConstant.STATUS_NOSHOWING);

    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private CmsModelItemService cmsModelItemService;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private GradeProtectionService gradeProtectionService;

    @Override
    public void checkUpdate(ContentUpdateDto dto) throws GlobalException {
        /*
         * 1. 修改栏目
         * 2. 如果该内容是已审核或下线状态修改到的栏目是一个有工作流的栏目，那么该操作直接提示
         */
        Channel channel = channelService.findById(dto.getChannelId());
        Content content = contentService.findById(dto.getId());
        // 只有栏目存在工作流并且内容当前状态为已审核或下线情况，并且当前操作是发布操作
        if (channel.getRealWorkflowId() != null && CHECK_STATUS.contains(content.getStatus()) && ContentConstant.STATUS_PUBLISH == dto.getType()) {
            if (!content.getChannelId().equals(channel.getId())) {
                throw new GlobalException(ContentErrorCodeEnum.CHANNEL_HAVE_WORKFLOW_HAVE_CHECK);
            }
            List<String> oldFields = ContentInitUtils.stringToList(content.getModelFieldSet());
            List<CmsModelItem> items = cmsModelItemService.findByModelId(content.getModelId());
            if (CollectionUtils.isEmpty(oldFields)) {
                oldFields = items.stream().map(CmsModelItem::getField).collect(Collectors.toList());
            }
            // 筛选出默认模型字段(不包括正文)
            List<CmsModelItem> defaultItems = items.stream().filter(item -> !item.getIsCustom() && !CmsModelConstant.CONTENT_TXT.equals(item.getDataType())).collect(Collectors.toList());
            // 校验默认模型字段的数据
            this.checkDefaultItem(defaultItems, content, dto,oldFields);
            // 筛选出自定义模型字段(不包括正文)
            List<CmsModelItem> customItems = items.stream().filter(item -> item.getIsCustom() && !CmsModelConstant.CONTENT_TXT.equals(item.getDataType())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(customItems)) {
                this.checkCustomItem(customItems, content.getContentAttrs(), dto.getJson(),oldFields);
            }
            // 筛选出正文字段(把正文单独拿出来是因为正文比较的字符串可能过长，比较耗时)
            List<CmsModelItem> contentItems = items.stream().filter(item -> CmsModelConstant.CONTENT_TXT.equals(item.getDataType())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(contentItems)) {
                this.checkContentItem(contentItems, dto.getJson(), content.getContentTxts());
            }
        }
    }

    /**
     * 校验正文模型字段的数据
     * @param items         正文模型字段集合
     * @param customJson    传入的dto中的JSON数据(正文数据放入自定义的JSON数据中)
     * @param contentTxts   数据库中查询到的内容正文集合
     * @throws GlobalException  全局异常
     */
    private void checkContentItem(List<CmsModelItem> items, JSONObject customJson, List<ContentTxt> contentTxts) throws GlobalException {
        List<String> fields = items.stream().map(CmsModelItem::getField).collect(Collectors.toList());
        // 将内容正文数据集合转换成map数据
        Map<String, String> contentTxtMap = CollectionUtils.isEmpty(contentTxts) ? new HashMap<>(16) : contentTxts.stream().collect(Collectors.toMap(ContentTxt::getAttrKey, ContentTxt::getAttrTxt));
        for (String field : fields) {
            String sourceTxt = customJson.getString(field);
            /***base64反转*/
            sourceTxt = gradeProtectionService.decryptStr(sourceTxt);
            String aims = contentTxtMap.get(field);
            if (StringUtils.isNotBlank(sourceTxt)) {
                // 如果前台传入的该正文字段中存在数据，但是前台传入的该数据和数据库查询的数据不匹配
                if (!sourceTxt.equals(aims)) {
                    throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
                }
            } else {
                // 前台传入该正文字段不存在数据，但是数据库中存在数据
                if (StringUtils.isNotBlank(aims)) {
                    throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
                }
            }
        }
    }

    /**
     * 校验自定义模型字段的数据
     * @param items         自定义模型字段集合
     * @param attrs       数据库中内容自定义数据集合
     * @param customJson    传入的dto中的JSON数据(自定义数据就放在这里)
     * @throws GlobalException  全局异常
     */
    private void checkCustomItem(List<CmsModelItem> items, List<ContentAttr> attrs, JSONObject customJson,List<String> oldFileds) throws GlobalException {
        // 如果前台传入的自定义数据集合为空，如果传入的自定义数据不为空直接报错，如果也为空则直接截至
        if (CollectionUtils.isEmpty(attrs)) {
            if (CollectionUtils.isEmpty(customJson.keySet())) {
                return;
            }
        }
        Map<String, ContentAttr> attrMap = new HashMap<>(CollectionUtils.isEmpty(attrs) ? 1 : attrs.size());
        if (!CollectionUtils.isEmpty(attrs)) {
            for (ContentAttr contentAttr : attrs) {
                attrMap.put(contentAttr.getAttrName(), contentAttr);
            }
        }
        // 遍历自定义模型字段
        for (CmsModelItem item : items) {
            String field = item.getField();
            if (!oldFileds.contains(field)) {
                continue;
            }
            ContentAttr attr = attrMap.get(field);
//            if ( attr == null) {
//                continue;
//            }
            // 来源数据绝大部分是这样，所以直接在这里定义
            Object source = customJson.get(field);
            Object aims = null;
            boolean isCheck = true;
            switch (item.getDataType()) {
                // 单图上传、视频上传、音频上传，目标都是取目标的资源id值，所以直接这样写复用代码
                case CmsModelConstant.SINGLE_CHART_UPLOAD:
                case CmsModelConstant.VIDEO_UPLOAD:
                case CmsModelConstant.AUDIO_UPLOAD:
                    aims = attr != null ? attr.getResId() : null;
                    break;
                case CmsModelConstant.MANY_CHART_UPLOAD:
                    this.checkMultiFile(attr != null ? attr.getContentAttrRes() : null, customJson.getJSONArray(field), true);
                    isCheck = false;
                    break;
                case CmsModelConstant.ANNEX_UPLOAD:
                    this.checkMultiFile(attr != null ? attr.getContentAttrRes() : null, customJson.getJSONArray(field), false);
                    isCheck = false;
                    break;
                case CmsModelConstant.TISSUE:
                    aims = attr != null ? attr.getOrgId() : null;
                    break;
                case CmsModelConstant.ADDRESS:
                    this.checkArea(attr, customJson.getJSONObject(field), true);
                    isCheck = false;
                    break;
                case CmsModelConstant.CITY:
                    this.checkArea(attr, customJson.getJSONObject(field), false);
                    isCheck = false;
                    break;
                // 单选、多选、选择框、性别来源都需要将前台传入的JSON转成字符串，所以直接这样写复用代码
                case CmsModelConstant.SINGLE_CHOOSE:
                case CmsModelConstant.MANY_CHOOSE:
                case CmsModelConstant.DROP_DOWN:
                case CmsModelConstant.SEX:
                    source = JSONObject.toJSONString(customJson.get(field));
                    aims = attr != null ? attr.getAttrValue() : null;
                    break;
                default:
                    // 除了上述需要特殊处理的其它直接这样取值即可
                    aims = attr != null ? attr.getAttrValue() : null;
                    break;
            }
            if (isCheck) {
                this.check(source, aims);
            }
        }
    }

    /**
     * 校验区域格式数据(字段：地址、城市)
     * @param attr  内容对应该字段的自定义对象
     * @param json  前台传入的该JSON对象
     * @param isAddress 是否是地址(地址需要特殊处理部分)
     * @throws GlobalException  全局异常
     */
    private void checkArea(ContentAttr attr, JSONObject json, boolean isAddress) throws GlobalException {
        if (json != null) {
            // 前台传入的JSON不为空但是后台查询的为空直接抛出异常
            if (attr == null) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            // 比较省份编码
            this.check(json.getString(ContentAttr.PROVINCE_CODE_NAME), attr.getProvinceCode());
            // 比较城市编码
            this.check(json.getString(ContentAttr.CITY_CODE_NAME), attr.getCityCode());
            // 地址需要额外比较：区编码和详细地址
            if (isAddress) {
                this.check(json.getString(ContentAttr.AREA_CODE_NAME), attr.getAreaCode());
                this.check(json.getString(ContentAttr.ADDRESS_NAME), attr.getAttrValue());
            }
        } else {
            // 后台传入查询的不为空，并且存在省份编码就说明一定存在抛出异常
            if (attr != null && StringUtils.isNotBlank(attr.getProvinceCode())) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验多资源格式数据(附件格式、多图格式)
     * @param attrResList   前台传入的资源集合
     * @param array     前台传入的该JSON集合
     * @param isImage   true->是多图，false->附件
     * @throws GlobalException  全局异常
     */
    private void checkMultiFile(List<ContentAttrRes> attrResList, JSONArray array, boolean isImage) throws GlobalException {
        // 如果传入的JSON集合不为空
        if (!CollectionUtils.isEmpty(array)) {
            if (CollectionUtils.isEmpty(attrResList)) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            // 如果两个集合的大小不为空，则直接抛出异常
            if (attrResList.size() != array.size()) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            GlobalConfig config = null;
            if (!isImage) {
                config = globalConfigService.get();
            }
            Map<Integer, String> multiImageMap = null;
            Map<Integer, Integer> fileUploadMap = null;
            if (isImage) {
                // 将其转换成资源ID->图片描述
                multiImageMap = attrResList.stream().collect(HashMap::new, (k, v) -> k.put(v.getResId(), v.getDescription()), HashMap::putAll);
            } else {
                // 将其转换成资源ID->附件密级
                fileUploadMap = attrResList.stream().collect(HashMap::new, (k, v) -> k.put(v.getResId(), v.getSecretId()), HashMap::putAll);
            }
            for (int i = 0; i < array.size(); i++) {
                JSONObject json = array.getJSONObject(i);
                //  获取附件资源id
                Integer resId = json.getInteger("resId");
                // 是否是多图，如果是多图则判断图片描述，否则判断附件密级
                if (isImage) {
                    if (!multiImageMap.containsKey(resId)) {
                        throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
                    } else {
                        String description = json.getString("description");
                        this.check(description, multiImageMap.get(resId));
                    }
                } else {
                    if (!fileUploadMap.containsKey(resId)) {
                        throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
                    } else {
                        // 如果不开启附件密级则不需要判断是否有问题
                        if (config.getConfigAttr().getOpenAttachmentSecurity()) {
                            Integer secretId = json.getInteger("secretId");
                            this.check(secretId, fileUploadMap.get(resId));
                        }
                    }
                }
            }
        } else {
            // 如果传入的JSON为空，数据库中查询出来的不为空
            if (!CollectionUtils.isEmpty(attrResList)) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验默认字段
     * @param items     默认字段集合
     * @param content   内容对象
     * @param dto       前台传入DTO对象
     * @throws GlobalException  全局异常
     */
    private void checkDefaultItem(List<CmsModelItem> items, Content content, ContentUpdateDto dto,List<String> oldFields) throws GlobalException {
        List<String> fields = items.stream().map(CmsModelItem::getField).collect(Collectors.toList());
        for (String field : fields) {
            if (!oldFields.contains(field)) {
                continue;
            }
            Object source = null;
            Object aims = null;
            boolean isCheck = true;
            switch (field) {
                case CmsModelConstant.FIELD_SYS_CHANNEL:
                    source = dto.getChannelId();
                    aims = content.getChannelId();
                    break;
                case CmsModelConstant.FIELD_SYS_TITLE:
                    source = dto.getTitle().getString(Content.TITLE_NAME);
                    aims = content.getTitle();
                    break;
                case CmsModelConstant.FIELD_SYS_SHORT_TITLE:
                    source = dto.getShortTitle();
                    aims = content.getShortTitle();
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_CONTENTTAG:
                    this.checkContentTag(dto, content);
                    isCheck = false;
                    break;
                case CmsModelConstant.FIELD_SYS_DESCRIPTION:
                    source = dto.getDescription();
                    aims = content.getDescription();
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_SOURCE:
                    this.checkSource(dto, content);
                    isCheck = false;
                    break;
                case CmsModelConstant.FIELD_SYS_RELEASE_TIME:
                    if (StringUtils.isNotBlank(dto.getReleaseTime())) {
                        source = MyDateUtils.parseDate(dto.getReleaseTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
                    }
                    aims = content.getSourceReleaseTime();
                    break;
                case CmsModelConstant.FIELD_SYS_AUTHOR:
                    source = dto.getAuthor();
                    aims = content.getAuthor();
                    break;
                case CmsModelConstant.FIELD_SYS_VIEW_CONTROL:
                    source = dto.getViewControl();
                    aims = content.getViewControl();
                    break;
                case CmsModelConstant.FIELD_SYS_COMMENT_CONTROL:
                    source = dto.getAllowComment();
                    aims = content.getCommentControl();
                    break;
                case CmsModelConstant.FIELD_SYS_TPL_PC:
                    source = dto.getTplPc();
                    aims = content.getContentExt().getTplPc();
                    break;
                case CmsModelConstant.FIELD_SYS_TPL_MOBILE:
                    source = dto.getTplMobile();
                    aims = content.getContentExt().getTplMobile();
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_SECRET:
                    source = dto.getContentSecretId();
                    aims = content.getContentSecretId();
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_OUTLINK:
                    this.checkOutLink(dto, content);
                    isCheck = false;
                    break;
                case CmsModelConstant.FIELD_SYS_KEY_WORD:
                    source = dto.getKeyword();
                    aims = content.getContentExt().getKeyWord();
                    break;
                case CmsModelConstant.FIELD_SYS_OFFLINE_TIME:
                    if (StringUtils.isNotBlank(dto.getOfflineTime())) {
                        source = MyDateUtils.parseDate(dto.getOfflineTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
                    }
                    aims = content.getOfflineTime();
                    break;
                case CmsModelConstant.FIELD_SYS_TEXTLIBRARY:
                    if (!CollectionUtils.isEmpty(dto.getTextLibrary())) {
                        source = dto.getTextLibrary().getJSONObject(0).getInteger("resId");
                    }
                    aims = content.getContentExt().getDocResourceId();
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_RELEASE_TERRACE:
                    this.checkReleaseTerrace(dto, content);
                    isCheck = false;
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_RESOURCE:
                    source = dto.getResource();
                    aims = content.getContentExt().getPicResId();
                    break;
                case CmsModelConstant.FIELD_SYS_CONTENT_POST_CONTENT:
                    this.checkPostContent(dto, content.getContentExt());
                    isCheck = false;
                    break;
                default:
                    break;
            }
            if (isCheck) {
                this.check(source, aims);
            }
        }
    }

    /**
     * 校验发文字号
     * @param dto   前台传入的dto
     * @param contentExt    内容扩展对象
     * @throws GlobalException  全局异常
     */
    private void checkPostContent(ContentUpdateDto dto, ContentExt contentExt) throws GlobalException {
        // 前台传入的发文字号JSON对象
        JSONObject postContent = dto.getPostContent();
        if (postContent != null) {
            this.check(postContent.getInteger(ContentExt.SUE_ORG_NAME), contentExt.getIssueOrg());
            this.check(postContent.getInteger(ContentExt.SUE_YEAR_NAME), contentExt.getIssueYear());
            this.check(postContent.getString(ContentExt.SUE_NUM_NAME), contentExt.getIssueNum());
        } else {
            if (contentExt.getIssueOrg() != null || contentExt.getIssueYear() != null || contentExt.getIssueNum() != null) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验发布平台
     * @param dto       前台传入dto
     * @param content   内容对象
     * @throws GlobalException  全局异常
     */
    private void checkReleaseTerrace(ContentUpdateDto dto, Content content) throws GlobalException {
        List<String> releaseTerraces = dto.getReleaseTerrace();
        if (!CollectionUtils.isEmpty(releaseTerraces)) {
            if (!content.getReleasePc().equals(releaseTerraces.contains(Content.RELEASE_PC_NAME))) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            if (!content.getReleaseWap().equals(releaseTerraces.contains(Content.RELEASE_WAP_NAME))) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            if (!content.getReleaseApp().equals(releaseTerraces.contains(Content.RELEASE_APP_NAME))) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            if (!content.getReleaseMiniprogram().equals(releaseTerraces.contains(Content.RELEASE_MINIPROGRAM_NAME))) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        } else {
            // 规则如果传入的发布平台为空，则判断其为全平台发布，所以如果前台传入的为空，那么如果后台有一个为false则有问题
            if (!content.getReleasePc() || !content.getReleaseApp() || !content.getReleaseWap() || !content.getReleaseMiniprogram()) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验外部链接
     * @param dto       前台传入的dto
     * @param content   内容对象
     * @throws GlobalException  全局异常
     */
    private void checkOutLink(ContentUpdateDto dto, Content content) throws GlobalException {
        JSONObject outLinkJson = dto.getOutLink();
        if (outLinkJson != null) {
            String outLink = outLinkJson.getString(CmsModelConstant.FIELD_SYS_CONTENT_OUTLINK);
            if (!outLinkJson.getBoolean(ContentExt.IS_NEW_TARGET_NAME).equals(content.getContentExt().getIsNewTarget())) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            this.check(outLink, content.getContentExt().getOutLink());
        } else {
            // 如果外部链接之前没有则默认初始化为false
            if (content.getContentExt().getIsNewTarget() || StringUtils.isNotBlank(content.getContentExt().getOutLink())) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验内容来源
     * @param dto       前台传入dto
     * @param content   内容对象
     * @throws GlobalException  全局异常
     */
    private void checkSource(ContentUpdateDto dto, Content content) throws GlobalException {
        // 数据库来源对象
        ContentSource contentSource = content.getContentExt().getContentSource();
        // 前台传入的来源JSON对象
        JSONObject sourceJson = dto.getContentSourceId();
        if (sourceJson != null) {
            if(StringUtils.isBlank(sourceJson.getString(ContentExt.SOURCE_NAME)) && StringUtils.isBlank(sourceJson.getString(ContentExt.SOURCE_LINK))) {
                sourceJson = null;
            }
            // 如果传入的来源JSON不为空，但是数据库中为空，直接抛出异常
            if (sourceJson != null && contentSource == null) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            this.check(sourceJson == null ? null : sourceJson.getString(ContentExt.SOURCE_NAME), contentSource == null ? null : contentSource.getSourceName());
            this.check(sourceJson == null ? null : sourceJson.getString(ContentExt.SOURCE_LINK), contentSource == null ? null : contentSource.getSourceLink());
        } else {
            if (contentSource != null) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验内容tag词
     * @param dto       前台传入dto
     * @param content   内容对象
     * @throws GlobalException  全局异常
     */
    private void checkContentTag(ContentUpdateDto dto, Content content) throws GlobalException {
        // 前台传入的tag字符串
        String sourceTagName = dto.getContentTag();
        String aimsTagName;
        if (StringUtils.isNotBlank(sourceTagName)) {
            if (CollectionUtils.isEmpty(content.getTagNames())) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
            // 去除来源tag空格
            sourceTagName = sourceTagName.replace(" ", "");
            // 将目标tag集合转换成字符串 aimsTagName = [1,2]
            aimsTagName = StringUtils.join(content.getTagNames());
            // 将tag集合转换成无中括号的的字符串，并去除中间空格   aimsTagName = 1,2
            aimsTagName = aimsTagName.substring(1, aimsTagName.length() - 1).replace(" ", "");
            if (!sourceTagName.equals(aimsTagName)) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        } else {
            if (!CollectionUtils.isEmpty(content.getTagNames())) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    /**
     * 校验基类
     * @param source    来源对象
     * @param aims      目标对象
     * @throws GlobalException  全局异常
     */
    private void check(Object source, Object aims) throws GlobalException {
        if(source instanceof String) {
            source = this.stringObject(source);
        }
        if (aims instanceof String) {
            aims = this.stringObject(aims);
        }

        if (source != null) {
            // 如果来源不为空，直接比较
            if (!source.equals(aims)) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        } else {
            // 如果来源为空，目标不为空直接抛出异常
            if (aims != null) {
                throw new GlobalException(ContentErrorCodeEnum.CONTENT_UPDATE_NOT_PUBLISH);
            }
        }
    }

    private Object stringObject(Object value) {
        String valueStr = String.valueOf(value);
        if ("null".equals(valueStr) || StringUtils.isBlank(valueStr)) {
            return null;
        }
        return value;
    }

}
