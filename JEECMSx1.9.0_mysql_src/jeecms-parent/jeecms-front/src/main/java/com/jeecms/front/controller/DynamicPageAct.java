package com.jeecms.front.controller;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.SubStringUtil;
import com.jeecms.common.util.XssUtil;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.web.util.UrlHelper;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.Content;
import com.jeecms.content.service.ContentService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.system.service.SysHotWordService;
import com.jeecms.util.FrontUtils;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Controller
public class DynamicPageAct {
    private static final Logger log = LoggerFactory.getLogger(DynamicPageAct.class);
    public static final String GROUP_FORBIDDEN = "login.groupAccessForbidden";
    public static final String CONTENT_STATUS_FORBIDDEN = "content.notChecked";

    /**
     * TOMCAT的默认路径
     *
     * @param request HttpServletRequest
     * @param model   ModelMap
     * @throws GlobalException GlobalException
     */
    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String index(HttpServletRequest request, HttpServletResponse response, ModelMap model)
            throws GlobalException {
        CmsSite site = SystemContextUtils.getSite(request);
        FrontUtils.frontData(request, model);
        // 带有其他路径则是非法请求(非内网)
        String uri = UrlHelper.getURI(request);
        if (StringUtils.isNotBlank(uri) && (!(uri.equals("/") || uri.equals("/index")))) {
            GlobalConfig config = configService.get();
            if (!config.getIsIntranet()) {
                return FrontUtils.pageNotFound(request, response, model);
            }
        }
        // 使用静态首页而且静态首页存在
        if (site.getOpenStatic() && existIndexPage(site)) {
            String ctx = "";
            if (StringUtils.isNotBlank(site.getContextPath())) {
                ctx = site.getContextPath();
            }
            String indexPage;
            if (SystemContextUtils.isPc()) {
                indexPage = ctx + "/" + site.getStaticIndexForPcPagePath();
            } else {
                indexPage = ctx + "/" + site.getStaticIndexForMobilePagePath();
            }
            try {
                response.sendRedirect(indexPage);
            } catch (IOException e) {

            }
            //return "redirect:" + indexPage;
        }
        String tpl = "";
        if (SystemContextUtils.isPc()) {
            tpl = site.getTplIndexOrDefForPc();
        } else {
            tpl = site.getTplIndexOrDefForMobile();
        }
        return tpl;
    }


    /**
     * 首页页入口（内网）
     */
    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/"}, method = RequestMethod.GET)
    public String intranetIndex(@PathVariable String site, HttpServletRequest request,
                                HttpServletResponse response, ModelMap model) throws GlobalException {
        return index(request, response, model);
    }

    /**
     * 内容动态页入口（外网）
     */
    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}/{id:[0-9]+}.jhtml"}, method = RequestMethod.GET)
    public String content(@PathVariable String path, @PathVariable Integer id, HttpServletRequest request,
                          HttpServletResponse response, ModelMap model) {
        return content(id, 1, request, response, model);
    }

    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}/{id:[0-9]+}"}, method = RequestMethod.GET)
    public String contentNoSuffix(@PathVariable String path, @PathVariable Integer id, HttpServletRequest request,
                                  HttpServletResponse response, ModelMap model) {
        return content(id, 1, request, response, model);
    }

    /**
     * 内容动态分页入口（外网）
     */
    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}/{id:[0-9]+}_{page:[0-9]+}.jhtml"}, method = RequestMethod.GET)
    public String contentPage(@PathVariable String path, @PathVariable Integer id, @PathVariable Integer page,
                              HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return content(id, page, request, response, model);
    }

    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}/{id:[0-9]+}_{page:[0-9]+}"}, method = RequestMethod.GET)
    public String contentPageNoSuffix(@PathVariable String path, @PathVariable Integer id, @PathVariable Integer page,
                                      HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return content(id, page, request, response, model);
    }


    /**
     * 内容动态分页入口(内网)
     */
    @RequestMapping(value = {
            "/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}/{/{id:[0-9]+}_{page:[0-9]+}.jhtml"}, method = RequestMethod.GET)
    public String intranetContentPage(@PathVariable Integer id, @PathVariable String path, @PathVariable Integer page,
                                      HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return content(id, page, request, response, model);
    }

    @RequestMapping(value = {
            "/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}/{/{id:[0-9]+}_{page:[0-9]+}"}, method = RequestMethod.GET)
    public String intranetContentPageNoSuffix(@PathVariable Integer id, @PathVariable String path, @PathVariable Integer page,
                                              HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return content(id, page, request, response, model);
    }

    /**
     * 内容动态页入口（内网）
     */
    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}/{id:[0-9]+}.jhtml"}, method = RequestMethod.GET)
    public String intranetContent(@PathVariable String site, @PathVariable String path, @PathVariable Integer id,
                                  HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return content(id, 1, request, response, model);
    }

    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}/{id:[0-9]+}"}, method = RequestMethod.GET)
    public String intranetContentNoSuffix(@PathVariable String site, @PathVariable String path, @PathVariable Integer id,
                                          HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return content(id, 1, request, response, model);
    }

    /**
     * 栏目动态页入口(外网)
     */
    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}.jhtml"}, method = RequestMethod.GET)
    public String channel(@PathVariable String path, HttpServletRequest request, HttpServletResponse response,
                          ModelMap model) throws GlobalException {
        return channel(path, 1, request, response, model);
    }

    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}"}, method = RequestMethod.GET)
    public String channelNoSuffix(@PathVariable String path, HttpServletRequest request, HttpServletResponse response,
                                  ModelMap model) throws GlobalException {
        return channel(path, 1, request, response, model);
    }

    /**
     * 栏目动态分页入口(外网)
     */
    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}_{page:[0-9]+}.jhtml"}, method = RequestMethod.GET)
    public String channelPage(@PathVariable String path, @PathVariable Integer page, HttpServletRequest request,
                              HttpServletResponse response, ModelMap model) throws GlobalException {
        return channel(path, page, request, response, model);
    }

    @RequestMapping(value = {"/{path:[0-9A-Za-z]+}_{page:[0-9]+}"}, method = RequestMethod.GET)
    public String channelPageNosuffix(@PathVariable String path, @PathVariable Integer page, HttpServletRequest request,
                                      HttpServletResponse response, ModelMap model) throws GlobalException {
        return channel(path, page, request, response, model);
    }

    /**
     * 栏目动态页入口（内网）
     */
    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}.jhtml"}, method = RequestMethod.GET)
    public String intranetChannel(@PathVariable String site, @PathVariable String path, HttpServletRequest request,
                                  HttpServletResponse response, ModelMap model) throws GlobalException {
        return channel(path, 1, request, response, model);
    }

    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}"}, method = RequestMethod.GET)
    public String intranetChannelNoSuffix(@PathVariable String site, @PathVariable String path, HttpServletRequest request,
                                          HttpServletResponse response, ModelMap model) throws GlobalException {
        return channel(path, 1, request, response, model);
    }

    /**
     * 栏目动态分页入口（内网）
     */
    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}_{page:[0-9]+}.jhtml"}, method = RequestMethod.GET)
    public String intranetChannelPage(@PathVariable String site, @PathVariable String path, @PathVariable Integer page,
                                      HttpServletRequest request, HttpServletResponse response, ModelMap model) throws GlobalException {
        return channel(path, page, request, response, model);
    }

    @RequestMapping(value = {"/c/{site:[0-9A-Za-z]+}/{path:[0-9A-Za-z]+}_{page:[0-9]+}"}, method = RequestMethod.GET)
    public String intranetChannelPageNoSuffix(@PathVariable String site, @PathVariable String path, @PathVariable Integer page,
                                              HttpServletRequest request, HttpServletResponse response, ModelMap model) throws GlobalException {
        return channel(path, page, request, response, model);
    }

    public String channel(String path, int pageNo, HttpServletRequest request, HttpServletResponse response,
                          ModelMap model) {
        CmsSite site = SystemContextUtils.getSite(request);
        Channel channel = channelService.findByPath(path, site.getId());
        if (channel == null) {
            log.debug("Channel path not found: {}", path);
            return FrontUtils.pageNotFound(request, response, model);
        }
        model.addAttribute("channel", channel);
        FrontUtils.frontData(request, model);
        FrontUtils.frontPageData(request, model);
        String tpl = null;
        if (SystemContextUtils.isMobile() || SystemContextUtils.isTablet()) {
            tpl = channel.getTplChannelOrDefForMobile();
        } else {
            tpl = channel.getTplChannelOrDefForPc();
        }
        /** 将request中所有参数保存至model中 */
        Map<String, Object> params = RequestUtils.getQueryParams(request);
        if (params != null) {
            Set<String> keySet = params.keySet();
            for (String key : keySet) {
                if (params.get(key) instanceof String) {
                    params.put(key, XssUtil.cleanXSS((String) params.get(key)));
                }
            }
        }
        model.putAll(params);
        if (StringUtils.isNotBlank(tpl)) {
            return tpl;
        }
        log.error("channel tpl not find " + tpl);
        /**模板找不到抛出404*/
        return FrontUtils.pageNotFound(request, response, model);
    }

    public String content(Integer id, Integer pageNo, HttpServletRequest request, HttpServletResponse response,
                          ModelMap model) {
        Content content = contentService.findById(id);
        if (content == null || content.getRecycle() || (content.getHasDeleted() != null && content.getHasDeleted())) {
            log.debug("Content id not found: {}", id);
            return FrontUtils.pageNotFound(request, response, model);
        }
        String tpl = null;
        if (SystemContextUtils.isMobile() || SystemContextUtils.isTablet()) {
            tpl = content.getTplContentOrDefForMobile();
        } else {
            tpl = content.getTplContentOrDefForPc();
        }
        /**来自引用内容，自内容数据使用源内容*/
        if (content.getOriContent() != null) {
            /**镜像型引用的内容的模板套用所选栏目的模板*/
            if (SystemContextUtils.isMobile() || SystemContextUtils.isTablet()) {
                tpl = content.getChannel().getTplContentOrDefForMobile(content.getModel());
            } else {
                tpl = content.getChannel().getTplContentOrDefForPc(content.getModel());
            }
            content = content.getOriContent();
        }
        Integer pageCount = content.getPageCount();
        if (pageNo > pageCount || pageNo < 0) {
            return FrontUtils.pageNotFound(request, response, model);
        }
        /** 非发布状态 不可浏览 */
        if (!content.getStatus().equals(ContentConstant.STATUS_PUBLISH)) {
            CoreUser user = SystemContextUtils.getCoreUser();
            /** 当前登录用户是管理员或者作者本人则可浏览，用于实现预览功能 */
            if (user == null || (!user.getAdmin() && !user.getId().equals(content.getUserId()))) {
                return FrontUtils.pageNotFound(request, response, model);
            }
        }
        String txt = content.getTxtByNo(pageNo);
        //判断是否需要截取字符串
        if (content.getRealPayRead() && content.getTrialReading() != null) {
            //只能预览第一页
            if (pageNo > 1) {
                txt = "";
            } else {
                txt = SubStringUtil.interceptTxt(txt, content.getTrialReading());
            }
        }
        // 内容加上热词
        txt = hotWordService.attachKeyword(content.getChannelId(), txt, content.getSite());
        Pageable pageable = PageRequest.of(pageNo - 1, 1);
        Page<Integer> page = new PageImpl<Integer>(Collections.singletonList(pageNo - 1), pageable, pageCount);
        //content = contentFrontService.initialize(content);
        model.addAttribute("pagination", page);
        model.addAttribute("content", content);
        model.addAttribute("txt", txt);
        FrontUtils.frontData(request, model);
        FrontUtils.frontPageData(request, model);
        /** 将request中所有参数保存至model中 */
        Map<String, Object> params = RequestUtils.getQueryParams(request);
        if (params != null) {
            Set<String> keySet = params.keySet();
            for (String key : keySet) {
                if (params.get(key) instanceof String) {
                    params.put(key, XssUtil.cleanXSS((String) params.get(key)));
                }
            }
        }
        model.putAll(params);
        if (StringUtils.isNotBlank(tpl)) {
            return tpl;
        }
        log.error("content tpl not find " + tpl);
        /**模板找不到抛出404*/
        return FrontUtils.pageNotFound(request, response, model);
    }

    private boolean existIndexPage(CmsSite site) {
        boolean exist = false;
        if (site.getOpenStatic()) {
            File indexPage;
            if (SystemContextUtils.isPc()) {
                indexPage = new File(realPathResolver.get(site.getStaticIndexForPcPagePath()));
            } else {
                indexPage = new File(realPathResolver.get(site.getStaticIndexForMobilePagePath()));
            }
            if (indexPage.exists()) {
                exist = true;
            }
        }
        return exist;
    }

    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private SysHotWordService hotWordService;
    @Autowired
    private GlobalConfigService configService;
    @Autowired
    private RealPathResolver realPathResolver;
}
