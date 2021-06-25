package com.jeecms.content.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.common.wechat.util.client.HttpUtil;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentButtonConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.constants.ContentConstant.ContentCheckFieldAndDataType;
import com.jeecms.content.constants.ContentReviewConstant;
import com.jeecms.content.domain.*;
import com.jeecms.content.service.ContentService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.CmsSiteConfig;
import com.jeecms.system.domain.ContentMark;
import com.jeecms.system.domain.ContentSource;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import sun.misc.BASE64Encoder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 内容进行初始化的util
 *
 * @author: chenming
 * @date: 2019年8月12日 下午3:21:56
 */
public class ContentInitUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentInitUtils.class);
    private static void initService() {
        contentService = ApplicationContextProvider.getBean(ContentService.class);
    }

    private static ContentService contentService;

    /**
     * 初始化content的数值数据(使用于新增时)
     */
    public static Content initContentNum(Content content) {
        content.setSortWeight(0);
        content.setViews(0);
        content.setPeopleViews(0);
        content.setComments(0);
        content.setUps(0);
        content.setDowns(0);
        content.setDownloads(0);
        return content;
    }

    /**
     * 初始化content的部分默认字段
     */
    public static Content initContentDefault(Content content) {
        initService();
        content.setTop(false);
        //新增内容时，为了排序方便，需要查询最大排序值，然后加1，此处查询可能会慢，线程后续更新
        Content maxNumContent = contentService.findFirstByOrderByIdDesc();
        content.setSortNum(maxNumContent != null ? maxNumContent.getSortNum() : 10);
        content.setEdit(false);
        content.setRecycle(false);
        return content;
    }

    /**
     * 初始化content发布平台为true
     */
    public static Content initTrueContentRelease(Content content) {
        content.setReleasePc(true);
        content.setReleaseWap(true);
        content.setReleaseApp(true);
        content.setReleaseMiniprogram(true);
        return content;
    }

    /**
     * 初始化content发布平台为false
     */
    public static Content initFalseContentRelease(Content content) {
        content.setReleasePc(false);
        content.setReleaseWap(false);
        content.setReleaseApp(false);
        content.setReleaseMiniprogram(false);
        return content;
    }

    /**
     * 初始化contentExt的数值数据(使用于新增时)
     */
    public static ContentExt initContentExtCount(ContentExt contentExt) {
        contentExt.setViewsMonth(0);
        contentExt.setCommentsMonth(0);
        contentExt.setDownloadsMonth(0);
        contentExt.setUpsMonth(0);
        contentExt.setDownsMonth(0);
        contentExt.setViewsWeek(0);
        contentExt.setCommentsWeek(0);
        contentExt.setDownloadsWeek(0);
        contentExt.setUpsWeek(0);
        contentExt.setDownsWeek(0);
        contentExt.setViewsDay(0);
        contentExt.setCommentsDay(0);
        contentExt.setDownloadsDay(0);
        contentExt.setUpsDay(0);
        contentExt.setDownsDay(0);
        return contentExt;
    }

    /**
     * 手动copy内容对象
     */
    public static Content copyContent(Content content, Content newContent, CoreUser user, Integer siteId) {
        newContent.setUserId(user.getId());
        if (siteId != null) {
            newContent.setSiteId(siteId);
        } else {
            newContent.setSiteId(content.getSiteId());
        }
        newContent.setTitle(content.getTitle());
        newContent.setTitleIsBold(content.getTitleIsBold());
        newContent.setTitleColor(content.getTitleColor());
        newContent.setShortTitle(content.getShortTitle());
        newContent.setReleaseTime(content.getReleaseTime());
        newContent.setOfflineTime(content.getOfflineTime());
        newContent.setCommentControl(content.getCommentControl());
        newContent.setViewControl(content.getViewControl());
        newContent.setReleasePc(content.getReleasePc());
        newContent.setReleaseWap(content.getReleaseWap());
        newContent.setReleaseApp(content.getReleaseApp());
        newContent.setReleaseMiniprogram(content.getReleaseMiniprogram());
        newContent.setHasStatic(false);
        newContent.setCollection(false);
        newContent.setPayPrice(content.getPayPrice());
        newContent.setPayPraise(content.getPayPraise());
        newContent.setPayRead(content.getPayRead());
        newContent.setTrialReading(content.getTrialReading());
        return newContent;
    }

    /**
     * 初始化copy是进行初始化content部分默认字段(使用场景：复制、推送到站群)
     */
    public static Content clearContentObject(Content content) {
        content.setId(null);
        content.setContentRecords(null);
        content.setContentTypes(null);
        content.setUserComments(null);
        content.setChannel(null);
        content.setContentExt(null);
        content.setUser(null);
        content.setPublishUser(null);
        content.setContentTxts(null);
        content.setContentTags(null);
        content.setModel(null);
        content.setContentAttrs(null);
        content.setContentVersions(null);
        content.setSecret(null);
        content.setContentCopys(null);
        content.setUpdateTime(null);
        content.setUpdateUser(null);
        content.setContentTxts(null);
        content.setTopStartTime(null);
        content.setTopEndTime(null);
        // 发布管理员id，因为无论是复制还是推送到站群的场景下此处都不可能是发布状态
        content.setPublishUserId(null);
        content.setContentRelations(null);
        return content;
    }

    /**
     * 初始化copy进行初始化contentExt默认字段(使用场景：复制、推送到站群)
     */
    public static ContentExt initCopyContentExtDefault(ContentExt contentExt) {
        contentExt.setId(null);
        contentExt.setWxMediaId(null);
        contentExt.setWbMediaId(null);
        contentExt.setSueOrg(null);
        contentExt.setSueYear(null);
        contentExt.setContentSource(null);
        contentExt.setReData(null);
        return contentExt;
    }

    /**
     * copy时初始化contentExt(推送到站群与复制功能皆都使用此功能所以放到功能列表中) 复用的尽量通用
     */
    public static ContentExt initCopyContentExt(ContentExt contentExt, Integer siteId, ContentMark sueOrg,
                                                ContentMark sueYear, ContentSource source, ResourcesSpaceData reData) throws GlobalException {
        ContentExt newContentExt = new ContentExt();
        newContentExt = ContentInitUtils.initCopyContentExt(contentExt, newContentExt);
        newContentExt = ContentInitUtils.initCopyContentExtDefault(newContentExt);
        if (newContentExt.getIssueOrg() != null) {
            newContentExt.setSueOrg(sueOrg);
        }
        if (newContentExt.getIssueYear() != null) {
            newContentExt.setSueYear(sueYear);
        }
        newContentExt = ContentInitUtils.initContentExtCount(newContentExt);
        if (newContentExt.getContentSourceId() != null) {
            newContentExt.setContentSource(source);
        }
        if (newContentExt.getPicResId() != null) {
            newContentExt.setReData(reData);
        }
        return newContentExt;
    }

    /**
     * 内容复制时进行内容扩展初始化
     *
     * @param contentExt    复制源内容扩展对象
     * @param newContentExt 复制目标内容扩展对象
     * @return ContentExt
     */
    public static ContentExt initCopyContentExt(ContentExt contentExt, ContentExt newContentExt) {
        newContentExt.setKeyWord(contentExt.getKeyWord());
        newContentExt.setDescription(contentExt.getDescription());
        newContentExt.setAuthor(contentExt.getAuthor());
        newContentExt.setContentSourceId(contentExt.getContentSourceId());
        newContentExt.setOutLink(contentExt.getOutLink());
        newContentExt.setIsNewTarget(contentExt.getIsNewTarget());
        newContentExt.setTplMobile(contentExt.getTplMobile());
        newContentExt.setTplPc(contentExt.getTplPc());
        newContentExt.setPicResId(contentExt.getPicResId());
        newContentExt.setIssueNum(contentExt.getIssueNum());
        newContentExt.setIssueOrg(contentExt.getIssueOrg());
        newContentExt.setIssueYear(contentExt.getIssueYear());
        newContentExt.setDocResourceId(contentExt.getDocResourceId());
        return newContentExt;
    }

    /**
     * 初始化浏览设置(与栏目配置和站点配置不相符)
     */
    public static Short initViewControl(Short viewControl) {
        switch (viewControl) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 2;
            default:
                return 1;
        }
    }

    /**
     * 校验状态
     *
     * @param oldType  内容未修改前状态
     * @param type     内容准备修改的状态
     * @param workflow 是否有工作流
     * @param isUpdate 是否是修改(true->修改，false->新增)
     */
    public static void checkStatus(Integer oldType, Integer type, boolean workflow, boolean isUpdate)
            throws GlobalException {
        if (isUpdate) {
            if (workflow) {
                switch (oldType) {
                    case ContentConstant.STATUS_DRAFT:
                        ContentInitUtils.workflowThrowException(type);
                        break;
                    case ContentConstant.STATUS_FIRST_DRAFT:
                        ContentInitUtils.workflowThrowException(type);
                        break;
                    case ContentConstant.STATUS_FLOWABLE:
                        ContentInitUtils.throwException();
                        break;
                    case ContentConstant.STATUS_BACK:
                        ContentInitUtils.workflowThrowException(type);
                        break;
                    case ContentConstant.STATUS_WAIT_PUBLISH:
                        ContentInitUtils.workflowThrowException(type);
                        break;
                    case ContentConstant.STATUS_PUBLISH:
                        ContentInitUtils.workflowThrowException(type);
                        break;
                    case ContentConstant.STATUS_NOSHOWING:
                        ContentInitUtils.workflowThrowException(type);
                        break;
                    case ContentConstant.STATUS_PIGEONHOLE:
                        ContentInitUtils.throwException();
                        break;
                    default:
                        break;
                }

            } else {
                if (ContentConstant.STATUS_DRAFT == oldType || ContentConstant.STATUS_FIRST_DRAFT == oldType
                        || ContentConstant.STATUS_PUBLISH == oldType || ContentConstant.STATUS_NOSHOWING == oldType) {
                    ContentInitUtils.throwException(type);
                }
                if (ContentConstant.STATUS_PIGEONHOLE == oldType) {
                    ContentInitUtils.throwException();
                }
            }
        } else {
            if (workflow) {
                ContentInitUtils.workflowThrowException(type);
            } else {
                ContentInitUtils.throwException(type);
            }

        }
    }

    /**
     * 抛出异常
     */
    public static void throwException() throws GlobalException {
        throw new GlobalException(new SystemExceptionInfo(ContentErrorCodeEnum.CONTENT_STATUS_ERROR.getDefaultMessage(),
                ContentErrorCodeEnum.CONTENT_STATUS_ERROR.getCode()));
    }

    /**
     * 判断有工作流的场景下如果type传入的不满足场景抛出异常 场景：存为草稿、存为初稿、提交审核(流转中)
     *
     * @param type 内容格式
     */
    public static void workflowThrowException(Integer type) throws GlobalException {
        if (!Arrays.asList(ContentConstant.STATUS_DRAFT, ContentConstant.STATUS_FIRST_DRAFT,
                ContentConstant.STATUS_FLOWABLE,ContentConstant.STATUS_PUBLISH).contains(type)) {
            ContentInitUtils.throwException();
        }
    }

    /**
     * 判断无工作流的场景下如果type传入的不满足场景抛出异常 场景：存为草稿、存为初稿、发布
     *
     * @param type 内容格式
     */
    public static void throwException(Integer type) throws GlobalException {
        if (!Arrays.asList(ContentConstant.STATUS_DRAFT, ContentConstant.STATUS_FIRST_DRAFT,
                ContentConstant.STATUS_PUBLISH, ContentConstant.STATUS_SMART_AUDIT).contains(type)) {
            ContentInitUtils.throwException();
        }
    }

    /**
     * 将内容转换成map
     *
     * @param content 内容对象
     * @return Map
     */
    public static Map<String, Object> toContentMap(Content content) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(CmsModelConstant.FIELD_SYS_DESCRIPTION, content.getDescription());
        map.put(CmsModelConstant.FIELD_SYS_SHORT_TITLE, content.getShortTitle());
        map.put(CmsModelConstant.FIELD_SYS_TITLE, content.getTitle());
        map.put(CmsModelConstant.FIELD_SYS_TPL_PC, content.getTplPcPath());
        map.put(CmsModelConstant.FIELD_SYS_TPL_MOBILE, content.getTplMobilePath());
        map.put(CmsModelConstant.FIELD_SYS_STATIC_CHANNEL, content.getHasStatic());
        return map;
    }

    /**
     * 将内容扩展对象转换成map
     *
     * @param contentExt 内容扩展对象
     * @return Map
     */
    public static Map<String, Object> toContentExtMap(ContentExt contentExt) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put(CmsModelConstant.FIELD_SYS_KEY_WORD, contentExt.getKeyWord());
        map.put(CmsModelConstant.FIELD_SYS_AUTHOR, contentExt.getAuthor());
        return map;
    }

    /**
     * 将map转换成list内容的内容集合
     */
    public static List<ContentTxt> toListTxt(Map<String, String> txtMap) {
        List<ContentTxt> contentTxts = new ArrayList<ContentTxt>();
        for (String txt : txtMap.keySet()) {
            ContentTxt contentTxt = new ContentTxt();
            contentTxt.setAttrKey(txt);
            contentTxt.setAttrTxt(txtMap.get(txt));
            contentTxts.add(contentTxt);
        }
        return contentTxts;
    }

    /**
     * 将内容的内容的list集合转换成map
     */
    public static Map<String, String> toMapTxt(List<ContentTxt> contentTxts) {
        Map<String, String> txtMap = new LinkedHashMap<String, String>();
        if (contentTxts != null) {
            for (ContentTxt contentTxt : contentTxts) {
                txtMap.put(contentTxt.getAttrKey(), contentTxt.getAttrTxt());
            }
        }
        return txtMap;
    }

    /**
     * 内容审核将内容对象转换成 JSON
     *
     * @param content    内容对象
     * @param contentExt 内容扩展对象
     * @Title: checkContentToJson
     * @return: JSONObject
     */
    public static JSONObject checkContentToJson(Content content, ContentExt contentExt) {
        JSONObject json = new JSONObject();
        json.put(CmsModelConstant.FIELD_SYS_TITLE, content.getTitle());
        String sortTitle = content.getShortTitle();
        if (StringUtils.isNotBlank(sortTitle)) {
            json.put(CmsModelConstant.FIELD_SYS_SHORT_TITLE, sortTitle);
        }
        String description = contentExt.getDescription();
        if (StringUtils.isNotBlank(description)) {
            json.put(CmsModelConstant.FIELD_SYS_DESCRIPTION, description);
        }
        List<String> tagNames = content.getTagNames();
        if (!CollectionUtils.isEmpty(tagNames)) {
            String contentTag = tagNames.stream().collect(Collectors.joining(","));
            json.put(CmsModelConstant.FIELD_SYS_CONTENT_CONTENTTAG, contentTag);
        }
        ContentSource contentSource = contentExt.getContentSource();
        if (contentSource != null) {
            List<String> contentSources = new ArrayList<String>();
            String sourceName = contentSource.getSourceName();
            if (StringUtils.isNotBlank(sourceName)) {
                contentSources.add(sourceName);
            }
            String sourceLink = contentSource.getSourceLink();
            if (StringUtils.isNotBlank(sourceLink)) {
                contentSources.add(sourceLink);
            }
            if (!CollectionUtils.isEmpty(contentSources)) {
                json.put(CmsModelConstant.FIELD_SYS_CONTENT_SOURCE, contentSources);
            }
        }
        String author = contentExt.getAuthor();
        if (StringUtils.isNotBlank(author)) {
            json.put(CmsModelConstant.FIELD_SYS_AUTHOR, author);
        }
        String keyword = contentExt.getKeyWord();
        if (StringUtils.isNotBlank(keyword)) {
            json.put(CmsModelConstant.FIELD_SYS_KEY_WORD, keyword);
        }
        Integer resourceId = contentExt.getPicResId();
        if (resourceId != null) {
            json.put(CmsModelConstant.FIELD_SYS_CONTENT_RESOURCE, resourceId);
        }
        List<ContentTxt> contents = content.getContentTxts();
        if (!CollectionUtils.isEmpty(contents)) {
            for (ContentTxt contentTxt : contents) {
                String attrTxt = contentTxt.getAttrTxt();
                if (StringUtils.isNotBlank(attrTxt)) {
                    json.put(contentTxt.getAttrKey(), attrTxt);
                }
            }
        }
        List<ContentAttr> attrs = content.getContentAttrs();
        if (!CollectionUtils.isEmpty(attrs)) {
            for (ContentAttr contentAttr : attrs) {
                switch (contentAttr.getAttrType()) {
                    case CmsModelConstant.SINGLE_CHART_UPLOAD:
                        if (contentAttr.getResId() != null) {
                            json.put(contentAttr.getAttrName(), contentAttr.getResId());
                        }
                        break;
                    case CmsModelConstant.MANY_CHART_UPLOAD:
                        List<ContentAttrRes> contentAttrRes = contentAttr.getContentAttrRes();
                        if (!CollectionUtils.isEmpty(contentAttrRes)) {
                            JSONArray array = new JSONArray();
                            for (ContentAttrRes res : contentAttrRes) {
                                JSONObject resJson = new JSONObject();
                                resJson.put(ContentReviewConstant.MANY_IMAGE_RESOURCE_ID, res.getResId());
                                resJson.put("description", res.getDescription());
                                array.add(resJson);
                            }
                            json.put(contentAttr.getAttrName(), array);
                        }
                        break;
                    default:
                        String attrValue = contentAttr.getAttrValue();
                        if (StringUtils.isNotBlank(attrValue)) {
                            json.put(contentAttr.getAttrName(), attrValue);
                        }
                        break;
                }
            }
        }
        return json;
    }

    /**
     * 内容富文本中删除HTML标签(包括图片标签)
     *
     * @param htmlStr 富文本字符串
     * @Title: removeHtmlTag
     * @return: String
     */
    public static String removeHtmlTag(String htmlStr) {
        // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
        String regExScript = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
        // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
        String regExStyle = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
        // 定义HTML标签的正则表达式
        String regExHtml = "<[^>]+>";
        // 定义一些特殊字符的正则表达式 如：&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        String regExSpecial = "\\&[a-zA-Z]{1,10};";
        // 定义图片标签的正则表达式	TODO 此处没有使用还未考虑好，误删
        // String regExImg = "<img[^>]+>";

        //1.过滤script标签
        Pattern pScript = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
        Matcher mScript = pScript.matcher(htmlStr);
        htmlStr = mScript.replaceAll("");
        //2.过滤style标签
        Pattern pStyle = Pattern.compile(regExStyle, Pattern.CASE_INSENSITIVE);
        Matcher mStyle = pStyle.matcher(htmlStr);
        htmlStr = mStyle.replaceAll("");
        //3.过滤html标签
        Pattern pHtml = Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
        Matcher mHtml = pHtml.matcher(htmlStr);
        htmlStr = mHtml.replaceAll("");
        //4.过滤特殊标签
        Pattern pSpectial = Pattern.compile(regExSpecial, Pattern.CASE_INSENSITIVE);
        Matcher mSprctial = pSpectial.matcher(htmlStr);
        htmlStr = mSprctial.replaceAll("");
        if (StringUtils.isNotBlank(htmlStr)) {
            return htmlStr;
        }
        return null;
    }

    /**
     * 从内容富文本中获取到图片url
     *
     * @param htmlStr 富文本字符串
     * @param webUrl  网址URL
     * @Title: obtainImgUrl
     * @return: List
     */
    public static List<String> obtainImgUrl(String htmlStr, String webUrl) {
        Element doc = Jsoup.parseBodyFragment(htmlStr).body();
        Elements pngs = doc.select("img[src]");
        List<String> imgUrls = new ArrayList<String>();
        // 匹配到检测出的img标签集合，遍历处理
        for (Element element : pngs) {
            // 获取到图片url
            String imgUrl = element.attr("src");
            // 没有匹配到正则则进行数据处理
            imgUrl = ContentInitUtils.processImgUrlFullPath(imgUrl, webUrl);
            imgUrls.add(imgUrl);
        }
        if (CollectionUtils.isEmpty(imgUrls)) {
            return null;
        }
        return imgUrls;
    }

    public static String processImgUrlFullPath(String imgUrl, String webUrl) {
        // 判断图片是否存在http开头(存在http开头说明不需要添加上网址url)
        Pattern p = Pattern.compile(ContentReviewConstant.CHECK_IMG_URL_REGULAR);
        Matcher matcher = p.matcher(imgUrl);
        if (!matcher.find()) {
            webUrl = webUrl.substring(0, webUrl.length() - 1);
            imgUrl = webUrl + imgUrl;
        }
        return imgUrl;
    }


    // 如果模型字段检测出来的为null，直接过滤无需审核
    public static JSONArray initReviewContent(JSONObject contentUpdateJson, List<CmsModelItem> items, List<CmsModelItem> auditItems, ContentCheckFieldAndDataType type) {
        // 该模型应当可以选择的字段集合
        items = CmsModelUtil.checkContentCmsModelItem(items, type);
        // 该内容模型中可以被选择的模型字段集合
        List<String> fields = items.stream().map(CmsModelItem::getField).collect(Collectors.toList());
        // 审核策略中配置的字段集合，然后过滤掉不在模型字段中的，就是	有效的审核策略中配置的字段集合
        auditItems = auditItems.stream().filter(item -> fields.contains(item.getField())).collect(Collectors.toList());
        JSONArray array = new JSONArray();
        if (CollectionUtils.isEmpty(auditItems)) {
            return array;
        }

        for (CmsModelItem item : auditItems) {
            String field = item.getField();
            Object value = contentUpdateJson.get(field);
            if (value != null) {
                JSONObject json = new JSONObject();
                List<String> txts = new ArrayList<String>();
                List<String> imgs = new ArrayList<String>();
                List<String> imgValues = new ArrayList<String>();
                switch (item.getDataType()) {
                    case CmsModelConstant.CONTENT_TXT:
                        JSONObject valueJson = JSONObject.parseObject(String.valueOf(value));
                        String contentTxt = valueJson.getString(ContentReviewConstant.CONTENT_TXT);
                        if (StringUtils.isNotBlank(contentTxt)) {
                            txts.add(contentTxt);
                        }
                        List<String> contentImgs = JSONObject.parseArray(valueJson.getString(ContentReviewConstant.CONTENT_IMG), String.class);
                        if (!CollectionUtils.isEmpty(contentImgs)) {
                            imgs.addAll(contentImgs);
                        }
                        break;
                    case CmsModelConstant.SINGLE_CHART_UPLOAD:
                        if (StringUtils.isNotBlank(String.valueOf(value))) {
                            imgs.add(String.valueOf(value));
                        }
                        break;
                    case CmsModelConstant.TYPE_SYS_CONTENT_RESOURCE:
                        if (StringUtils.isNotBlank(String.valueOf(value))) {
                            imgs.add(String.valueOf(value));
                        }
                        break;
                    case CmsModelConstant.MANY_CHART_UPLOAD:
                        JSONArray manyImgs = (JSONArray) value;
                        for (int i = 0; i < manyImgs.size(); i++) {
                            JSONObject manyImg = manyImgs.getJSONObject(i);
                            String description = manyImg.getString("description");
                            if (StringUtils.isNotBlank(description)) {
                                txts.add(description);
                            }
                            String imgUrl = manyImg.getString(ContentReviewConstant.MANY_IMAGE_URL);
                            if (StringUtils.isNotBlank(imgUrl)) {
                                imgs.add(imgUrl);
                            }
                        }
                        break;
                    case CmsModelConstant.SOURCE:
                        @SuppressWarnings("unchecked")
                        List<String> sources = (List<String>) value;
                        txts.addAll(sources);
                        break;
                    case CmsModelConstant.ADDRESS:
//					JSONObject addressJson = (JSONObject)value;
//					String address = addressJson.getString("address");
//					if (StringUtils.isNotBlank(address)) {
//						txts.add(address);
//					}
                        String addressValue = String.valueOf(value);
                        if (StringUtils.isNotBlank(addressValue)) {
                            txts.add(addressValue);
                        }
                        break;
                    default:
                        String valueTxt = String.valueOf(value);
                        txts.add(valueTxt);
                        break;
                }
                json.put(ContentReviewConstant.REVIEW_VALUE_TXT, txts);
                json.put(ContentReviewConstant.REVIEW_VALUE_FIELD, field);
                if (!CollectionUtils.isEmpty(imgs)) {
                    for (String imgUrl : imgs) {
                        String imgByteData = ContentInitUtils.imgToBase64(imgUrl);
                        if (imgByteData != null) {
                            imgValues.add(imgByteData);
                        } else {
                            return null;
                        }
                    }
                }
                json.put("imgValue", imgs);
                json.put(ContentReviewConstant.REVIEW_VALUE_IMG, imgValues);
                array.add(json);
            }
        }
        return array;
    }

    public static String imgToBase64(String url) {
        byte[] data = null;
        try {
            data = HttpUtil.readURLImage(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // data为null的情况是：
        if (data != null) {
            return new BASE64Encoder().encode(data);
        } else {
            return null;
        }
    }

    /**
     * 内容修改API
     */
    private static final Map<String, String> CONTENT_UPDATE_API = ImmutableMap.of(WebConstants.ADMIN_PREFIX+"/content", "PUT");
    /**
     * 内容删除API
     */
    private static final Map<String, String> CONTENT_DELETE_API = ImmutableMap.of(WebConstants.ADMIN_PREFIX+"/contentext/rubbish", "POST");
    /**
     * 内容物理删除API
     */
    private static final Map<String, String> CONTENT_PHYSICAL_DELETE_API = ImmutableMap.of(WebConstants.ADMIN_PREFIX+"/contentext", "DELETED");

    /**
     * 获取按钮绑定的api
     *
     * @param contentStatus 当前内容的状态
     * @param operateStatus 内容操作的状态
     * @return apiMap：key-api地址，value-请求方式
     */
    public static Map<String, String> getButtonApi(Integer contentStatus, Integer operateStatus) {
        // 如果该按钮是删除按钮
        if (ContentButtonConstant.OPERATE_DELETE == operateStatus) {
            // 如果该内容是审核中的内容，直接删除
            if (ContentConstant.STATUS_SMART_AUDIT == contentStatus) {
                return ContentInitUtils.CONTENT_PHYSICAL_DELETE_API;
            } else {
                return ContentInitUtils.CONTENT_DELETE_API;
            }
        }
        // 如果符合内容修改操作的地址，全部返回内容修改api
        if (ContentButtonConstant.Api.CONTENT_UPDATE.contains(operateStatus)) {
            return ContentInitUtils.CONTENT_UPDATE_API;
        }

        if (ContentButtonConstant.Api.CONTENT_UPDATE_STATUS.get(operateStatus) != null) {
            return ContentButtonConstant.Api.CONTENT_UPDATE_STATUS.get(operateStatus);
        }
        return null;
    }

    public static List<Integer> getOperatePieces(ContentButtonConstant.OperatePiece operatePiece, Content content, Boolean forceReleaseButton) {
        return pieces(operatePiece, content.getStatus(), content.getWorkflow(), content, forceReleaseButton);
    }


    public static List<Integer> pieces(ContentButtonConstant.OperatePiece operatePiece, Integer status, Boolean isWorkflow, Content content, Boolean forceReleaseButton) {
        List<Integer> operateRows = null;
        switch (status) {
            case ContentConstant.STATUS_DRAFT:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    if (isWorkflow) {
                        operateRows = Collections.singletonList(ContentConstant.STATUS_FLOWABLE);
                    } else {
                        operateRows = Collections.singletonList(ContentConstant.STATUS_PUBLISH);
                    }
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }
                return operateRows;
            case ContentConstant.STATUS_FIRST_DRAFT:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    if (isWorkflow) {
                        operateRows = Collections.singletonList(ContentConstant.STATUS_FLOWABLE);
                    } else {
                        operateRows = Collections.singletonList(ContentConstant.STATUS_PUBLISH);
                    }
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }
                return operateRows;
            case ContentConstant.STATUS_FLOWABLE:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    operateRows = Collections.singletonList(ContentButtonConstant.OPERATE_RECALL);
                }
                return operateRows;
            case ContentConstant.STATUS_WAIT_PUBLISH:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    if (isWorkflow) {
                        operateRows = Arrays.asList(ContentConstant.STATUS_FLOWABLE,ContentConstant.STATUS_PUBLISH);
                    } else {
                        operateRows = Collections.singletonList(ContentConstant.STATUS_PUBLISH);
                    }
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }
                return operateRows;
            case ContentConstant.STATUS_PUBLISH:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    if (isWorkflow) {
                        operateRows = Arrays.asList(ContentConstant.STATUS_FLOWABLE, ContentConstant.STATUS_NOSHOWING);
                    } else {
                        operateRows = Arrays.asList(ContentConstant.STATUS_PUBLISH, ContentConstant.STATUS_NOSHOWING);
                    }
                }
                if (ContentButtonConstant.OperatePiece.scanPiece.equals(operatePiece)) {
                    operateRows.add(ContentButtonConstant.OPERATE_BROWSE);
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }

                return operateRows;
            case ContentConstant.STATUS_BACK:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    operateRows = Collections.singletonList(ContentConstant.STATUS_FLOWABLE);
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }
                return operateRows;
            case ContentConstant.STATUS_NOSHOWING:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    if (isWorkflow) {
                        operateRows = Arrays.asList(ContentConstant.STATUS_PUBLISH, ContentConstant.STATUS_FLOWABLE);
                    } else {
                        operateRows = Collections.singletonList(ContentConstant.STATUS_PUBLISH);
                    }
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }
                return operateRows;
            case ContentConstant.STATUS_PIGEONHOLE:
                operateRows = Collections.singletonList(ContentButtonConstant.OPERATE_OUTOFFILE);
                return operateRows;
            case ContentConstant.STATUS_TEMPORARY_STORAGE:
                break;
            case ContentConstant.STATUS_SMART_AUDIT:
                List<Integer> operateBehaviors = Collections.singletonList(ContentButtonConstant.OPERATE_DELETE);
                return operateBehaviors;
            case ContentConstant.STATUS_SMART_AUDIT_SUCCESS:
                List<Integer> operateConducts = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    operateConducts = new ArrayList<Integer>();
                    if (isWorkflow) {
                        operateConducts.add(ContentConstant.STATUS_FLOWABLE);
                    } else {
                        operateConducts.add(ContentConstant.STATUS_PUBLISH);
                    }
                    if (forceReleaseButton) {
                        operateConducts.add(ContentButtonConstant.OPERATE_FORCE_PUBLISH);
                    }
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateConducts = ContentInitUtils.processTopAndQuote(content, operateConducts);
                }
                return operateConducts;

            case ContentConstant.STATUS_SMART_AUDIT_FAILURE:
                operateRows = new ArrayList<Integer>(ContentButtonConstant.OperatePiece.getPieces(operatePiece));
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    operateRows = new ArrayList<Integer>();
                    if (isWorkflow) {
                        operateRows.add(ContentConstant.STATUS_FLOWABLE);
                    } else {
                        operateRows.add(ContentConstant.STATUS_PUBLISH);
                    }
                    if (forceReleaseButton) {
                        operateRows.add(ContentButtonConstant.OPERATE_FORCE_PUBLISH);
                    }
                }
                if (ContentButtonConstant.OperatePiece.morePiece.equals(operatePiece)) {
                    operateRows = ContentInitUtils.processTopAndQuote(content, operateRows);
                }
                return operateRows;
            default:
                break;
        }
        return operateRows;
    }

    private static List<Integer> processTopAndQuote(Content content, List<Integer> pieces) {
        if (content.getTop()) {
            pieces.remove(Integer.valueOf(ContentButtonConstant.OPERATE_STICKY));
        } else {
            pieces.remove(Integer.valueOf(ContentButtonConstant.OPERATE_NOT_STICKY));
        }
        if (content.isHaveQuote()) {
            pieces.remove(Integer.valueOf(ContentButtonConstant.OPERATE_QUOTE));
        } else {
            pieces.remove(Integer.valueOf(ContentButtonConstant.OPERATE_NOT_QUOTE));
        }
        return pieces;
    }

    public static Boolean operatePurview(Integer operate, Content content, CmsSiteConfig config) {
        switch (operate) {
            case ContentConstant.STATUS_DRAFT:
            case ContentConstant.STATUS_FIRST_DRAFT:
            case ContentConstant.STATUS_FLOWABLE:
                if (ContentConstant.STATUS_PUBLISH == content.getStatus() && !config.getContentCommitAllowUpdate()) {
                    return false;
                }
                return content.getEditContentAble();
            case ContentConstant.STATUS_PUBLISH:
                if (ContentConstant.STATUS_PUBLISH == content.getStatus() && !config.getContentCommitAllowUpdate()) {
                    return false;
                }
                return content.getPublishContentAble();
            case ContentConstant.STATUS_NOSHOWING:
            case ContentButtonConstant.OPERATE_FORCE_PUBLISH:
                return content.getPublishContentAble();
            case ContentConstant.STATUS_PIGEONHOLE:
            case ContentButtonConstant.OPERATE_OUTOFFILE:
                return content.getFileContentAble();
            case ContentButtonConstant.OPERATE_PUSH_SITES:
                return content.getSitePushContentAble();
            case ContentButtonConstant.OPERATE_PUSH_WECHAT:
                return content.getWechatPushContentAble();
            case ContentButtonConstant.OPERATE_PUSH_WEIBO:
                return content.getWeiboPushContentAble();
            case ContentButtonConstant.OPERATE_DELETE:
                return content.getDeleteContentAble();
            case ContentButtonConstant.OPERATE_STICKY:
            case ContentButtonConstant.OPERATE_NOT_STICKY:
                return content.getTopContentAble();
            case ContentButtonConstant.OPERATE_COPY:
                return content.getCopyContentAble();
            case ContentButtonConstant.OPERATE_QUOTE:
            case ContentButtonConstant.OPERATE_NOT_QUOTE:
                return content.getQuoteContentAble();
            case ContentButtonConstant.OPERATE_SORT:
                return content.getSortContentAble();
            case ContentButtonConstant.OPERATE_RECALL:
            case ContentButtonConstant.OPERATE_RELATED_CONTENT:
            case ContentButtonConstant.OPERATE_VERSION:
                return content.getEditContentAble();
            case ContentButtonConstant.OPERATE_BROWSE_RECORDS:
                return content.getBrowsingContentAble();
            default:
                break;
        }
        return null;
    }

    public static Boolean operatePurview(Integer operate, Channel channel) {
        switch (operate) {
            case ContentConstant.STATUS_DRAFT:
            case ContentConstant.STATUS_FIRST_DRAFT:
            case ContentConstant.STATUS_FLOWABLE:
                return channel.getCreateContentAble();
            case ContentConstant.STATUS_PUBLISH:
                return channel.getPublishContentAble();
            default:
                break;
        }
        return null;
    }

    /**
     * String转换成String的list集合
     * @param modelFieldSet 内容的模型字段快照
     * @return  List<String>
     */
    public static List<String> stringToList(String modelFieldSet) {
        if (StringUtils.isBlank(modelFieldSet) || modelFieldSet.length() < 2) {
            return new ArrayList<>();
        }
        modelFieldSet = modelFieldSet.substring(1,modelFieldSet.length()-1);
        List<String> fields = Arrays.stream(modelFieldSet.split(",")).collect(Collectors.toList());
        List<String> newFields = new ArrayList<>(fields.size());
        for (String field:fields) {
            // 将转换的元素去空格
            newFields.add(field.trim());
        }
        return newFields;
    }
}
