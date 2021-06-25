/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller.directive;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.web.freemarker.DefaultObjectWrapperBuilderFactory;
import com.jeecms.common.web.freemarker.DirectiveUtils;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentRelation;
import com.jeecms.content.service.ContentFrontService;
import com.jeecms.content.service.ContentRelationService;
import com.jeecms.member.domain.vo.MemberVo;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.FrontUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import static com.jeecms.common.web.freemarker.DirectiveUtils.OUT_LIST;
import static com.jeecms.content.constants.ContentConstant.ORDER_TYPE_SORT_NUM_DESC;

/**
 * 内容列表
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/7/15 17:52
 */

public class ContentDirectiveList extends ContentDirectiveAbstract {

    /**
     * 模板名称
     */
    public static final String TPL_NAME = "cms_content_list";

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        Integer[] channelIds = getChannelId(params);
        Integer[] ids = getIds(params);
        Integer[] tagIds = getTagId(params);
        String[] channelPaths = getChannelPath(params);
        Integer siteId = getSiteId(params);
        Integer[] typeIds = getTypeId(params);
        String title = getTitle(params);
        Boolean isNew = getNew(params);
        Boolean isTop = getIsTop(params);
        Date timeBegin = getTimeBegin(params);
        Date timeEnd = getTimeEnd(params);
        int count = FrontUtils.getCount(params);
        Integer[] excludeId = getExcludeId(params);
        Integer[] modelId = getModelId(params);
        Integer orderBy = getOrderNy(params);
        Integer channelOption = getChannelOption(params);
        Integer id = getId(params);
        Integer relate = getRelate(params);
        List<Content> contents = new ArrayList<>();
        orderBy = orderBy != null ? orderBy : ORDER_TYPE_SORT_NUM_DESC;
        //用户的内容密级id
        List<Integer> contentSecretIds = null;
        //用户的附件密级id
        List<Integer> annexSecretIds = null;
        //默认没有开启内容密级
        boolean openContentSecurity = false;
        //默认没有开启附件密级
        boolean openAttachmentSecurity = false;
        MemberVo memberVo = FrontUtils.getUser(env);
        try {
            Map<String, String> attrs = globalConfigService.get().getAttrs();
            if (attrs.get(GlobalConfigAttr.OPEN_CONTENT_SECURITY) != null && GlobalConfigAttr.TRUE_STRING.equals(attrs.get(GlobalConfigAttr.OPEN_CONTENT_SECURITY))) {
                openContentSecurity = true;
                //获取附件密级是否开启
                if (attrs.get(GlobalConfigAttr.OPEN_ATTACHMENT_SECURITY) != null && GlobalConfigAttr.TRUE_STRING.equals(attrs.get(GlobalConfigAttr.OPEN_ATTACHMENT_SECURITY))) {
                    openAttachmentSecurity = true;
                }
                if (memberVo != null) {
                    CoreUser user = coreUserService.findById(memberVo.getId());
                    //获取附件密级
                    annexSecretIds = getAnnexSecretIds(user);
                    contentSecretIds = getContentSecretIds(user);
                }else {
                    contentSecretIds = new ArrayList<>();
                }
            }
        } catch (GlobalException e) {
            e.getMessage();
        }
        //ids 如果有值 则只取固定ID的内容列表  第一优先
        if (ids != null && ids.length > 0) {
            contents = contentFrontService.findAllById(Arrays.asList(ids), orderBy, contentSecretIds);
        } else {
            CmsSite site = FrontUtils.getSite(env);
            if (siteId != null) {
                site = siteService.findById(siteId);
            }
            if (site != null) {
                siteId = site.getId();
            }
            if (ASSOCIATION_HAND.equals(relate)) {
                //手动关联内容
                List<ContentRelation> relations = contentRelationService.findByContentId(id);
                Integer[] relationIds = new Integer[relations.size()];
                for (int i = 0; i < relations.size(); i++) {
                    relationIds[i] = relations.get(i).getRelationContentId();
                }
                contents = contentFrontService.getList(relationIds, orderBy, count, contentSecretIds);
            } else if (ASSOCIATION_TAG.equals(relate)) {
                //tag词关联内容
                contents = contentFrontService.getList(channelOption, channelIds, tagIds,
                        channelPaths, siteId, typeIds, title, isNew, null, isTop,
                        timeBegin, timeEnd, excludeId, modelId, orderBy, count, site, contentSecretIds);
            } else if (ASSOCIATION_ALL.equals(relate)) {
                //手动关联
                if (id != null) {
                    List<ContentRelation> relations = contentRelationService.findByContentId(id);
                    Integer[] relationIds = new Integer[relations.size()];
                    for (int i = 0; i < relations.size(); i++) {
                        relationIds[i] = relations.get(i).getRelationContentId();
                    }
                    contents = contentFrontService.getList(relationIds, orderBy, count, contentSecretIds);
                }
                if (contents.size() < count) {
                    //tag词关联
                    contents.addAll(contentFrontService.getList(channelOption, channelIds, tagIds, channelPaths,
                            siteId, typeIds, title, isNew, null, isTop, timeBegin, timeEnd,
                            excludeId, modelId, orderBy, count - contents.size(), site, contentSecretIds));
                }
            } else {
                //普通条件查询
                contents = contentFrontService.getList(channelOption, channelIds, null,
                        channelPaths, siteId, typeIds, title, isNew, null, isTop,
                        timeBegin, timeEnd, excludeId, modelId, orderBy, count, site, contentSecretIds);
            }
        }
        if (openContentSecurity && openAttachmentSecurity) {
            for (Content content : contents) {
                initAnnexSecret(annexSecretIds, content);
            }
        }
        Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(params);
        paramWrap.put(OUT_LIST, DefaultObjectWrapperBuilderFactory.getDefaultObjectWrapper().wrap(contents));
        Map<String, TemplateModel> origMap = DirectiveUtils.addParamsToVariable(env, paramWrap);
        body.render(env.getOut());
        DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
    }

    @Autowired
    private CmsSiteService siteService;
    @Autowired
    private ContentFrontService contentFrontService;
    @Autowired
    private ContentRelationService contentRelationService;
    @Autowired
    private CoreUserService coreUserService;
    @Autowired
    private GlobalConfigService globalConfigService;
}
