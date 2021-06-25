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
import com.jeecms.content.service.ContentFrontService;
import com.jeecms.member.domain.vo.MemberVo;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.FrontUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jeecms.common.web.freemarker.DirectiveUtils.OUT_BEAN;

/**
 * @author xiaohui
 * @version 1.0
 * @date 2019/7/16 11:43
 */

public class ContentDirectiveBean extends ContentDirectiveAbstract {

    /**
     * 模板名称
     */
    public static final String TPL_NAME = "cms_content";

    public static final String PARAM_CHANNEL_ID = "channelId";
    public static final String PARAM_SITE_ID = "siteId";

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        Integer id = getId(params);
        Boolean next = getNext(params);
        Content content = null;
        //用户的内容密级id
        List<Integer> contentSecretIds = null;
        //用户的附件密级id
        List<Integer> annexSecretIds = null;
        //默认没有开启内容密级
        boolean openContentSecurity = false;
        //默认没有开启附件密级
        boolean openAttachmentSecurity = false;
        Integer siteId = DirectiveUtils.getInt(PARAM_SITE_ID, params);
        MemberVo memberVo = FrontUtils.getUser(env);
        siteId = siteId == null ? FrontUtils.getSite(env).getId() : siteId;
        try {
            Map<String, String> attrs = globalConfigService.get().getAttrs();
            //判断附件密级是否开启
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
                    //获取内容密级
                    contentSecretIds = getContentSecretIds(user);
                } else {
                    contentSecretIds = new ArrayList<>();
                }
            }
        } catch (GlobalException e) {
            e.getMessage();
        }
        if (id != null) {
            if (next == null) {
                content = contentFrontService.findById(id);
            } else {
                Integer channelId = DirectiveUtils.getInt(PARAM_CHANNEL_ID, params);
                content = contentFrontService.getSide(id, siteId, channelId, next, contentSecretIds);
            }
        }
        if (content != null) {
            if (openContentSecurity && openAttachmentSecurity) {
                content = initAnnexSecret(annexSecretIds, content);
            }
        }
        Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(params);
        paramWrap.put(OUT_BEAN, DefaultObjectWrapperBuilderFactory.getDefaultObjectWrapper().wrap(content));
        Map<String, TemplateModel> origMap = DirectiveUtils.addParamsToVariable(env, paramWrap);
        body.render(env.getOut());
        DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
    }

    @Autowired
    private ContentFrontService contentFrontService;
    @Autowired
    private CoreUserService coreUserService;
    @Autowired
    private GlobalConfigService globalConfigService;
}
