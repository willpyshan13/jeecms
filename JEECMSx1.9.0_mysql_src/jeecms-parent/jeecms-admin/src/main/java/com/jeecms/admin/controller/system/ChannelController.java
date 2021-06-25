package com.jeecms.admin.controller.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.admin.controller.BaseTreeAdminController;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.domain.ChannelAttrRes;
import com.jeecms.channel.domain.ChannelContentTpl;
import com.jeecms.channel.domain.dto.*;
import com.jeecms.channel.domain.util.ChannelAgent;
import com.jeecms.channel.service.ChannelDtoService;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.error.ChannelErrorCodeEnum;
import com.jeecms.common.exception.error.RPCErrorCodeEnum;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.manage.annotation.OperatingIntercept;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.CmsModelTpl;
import com.jeecms.content.service.CmsModelService;
import com.jeecms.content.service.CmsModelTplService;
import com.jeecms.content.service.ContentService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.*;
import com.jeecms.system.domain.CmsDataPerm.OpeChannelEnum;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.util.SystemContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 栏目管理控制controller
 *
 * @author: chenming
 * @date: 2019年4月29日 下午4:01:46
 */
@RestController
@RequestMapping("/channel")
public class ChannelController extends BaseTreeAdminController<Channel, Integer> {

    @Autowired
    private CmsSiteService cmsSiteService;
    @Autowired
    private ChannelService service;
    @Autowired
    private CmsModelTplService cmsModelTplService;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private ChannelDtoService channelDtoService;
    @Autowired
    private ContentService contentService;

    /**
     * 1. 加载出来的列表必须是树形的列表，且是该用户可管理的列表 2.
     * 选择parentId时是否可以选择"无"必须根据channelParentId作为路由标识去用户路由列表中查询如果有就显示否则不显示 3.
     * 在controller层中必须校验是否有拥有该栏目的添加子栏目的权限，该用户是否拥有channelParentId作为路由标识
     *
     * 4. 可以通过
     * SystemContextUtils.getUser(request).getViewChannels();查询出该用户可以加载删除的各种列表，
     * 必须修改
     */

    /**
     * 新增栏目
     */
    @RequestMapping(method = RequestMethod.POST)
    @OperatingIntercept
    public ResponseInfo save(@RequestBody @Valid ChannelSaveDto dto, BindingResult result,
                             HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        if (dto.getChannelParentId() == null || dto.getChannelParentId() == 0) {
            return new ResponseInfo(
                    SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                    SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage());
        }
        return this.save(dto, request);
    }

    final transient ReentrantLock lock = new ReentrantLock();

    private ResponseInfo save(ChannelSaveDto dto, HttpServletRequest request) throws GlobalException {
        Channel channel = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            CmsSite site = SystemContextUtils.getSite(request);
            dto.setSiteId(site.getId());
            // 前端已经校验过了，但此处必须进行强校验，防止脏数据
            boolean status = service.checkElement(null, dto.getChannelName(), null, site.getId());
            CmsModel model = cmsModelService.findById(dto.getModelId());
            if (model == null || !model.getIsEnable() || !CmsModel.CHANNEL_TYPE.equals(model.getTplType())) {
                return new ResponseInfo(SettingErrorCodeEnum.INCOMING_MODEL_ERROR.getCode(),
                        SettingErrorCodeEnum.INCOMING_MODEL_ERROR.getDefaultMessage());
            }
            if (status) {
                return new ResponseInfo(SettingErrorCodeEnum.CHANNEL_NAME_ALREADY_EXIST.getCode(),
                        SettingErrorCodeEnum.CHANNEL_NAME_ALREADY_EXIST.getDefaultMessage());
            }
            status = service.checkElement(null, null, dto.getChannelPath(), site.getId());
            if (status) {
                return new ResponseInfo(SettingErrorCodeEnum.CHANNEL_PATH_ALREADY_EXIST.getCode(),
                        SettingErrorCodeEnum.CHANNEL_PATH_ALREADY_EXIST.getDefaultMessage());
            }
            if (dto.getChannelParentId() != null) {
                long count = contentService.countByChannelIdInAndRecycle(new Integer[]{dto.getChannelParentId()}, false);
                if (count > 0) {
                    return new ResponseInfo(ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT_NOT_SAVE.getCode(),
                            ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT_NOT_SAVE.getDefaultMessage());
                }
                // 栏目创建子集栏目校验
                super.checkChannelDataPerm(dto.getChannelParentId(), OpeChannelEnum.CREATE);
            }
            channel = service.save(dto);
        } finally {
            lock.unlock();
        }
        /** 主动维护站点栏目集合，方便权限根据站点取栏目数据 */
        CmsSite channelSite = channel.getSite();
        channelSite.getChannels().add(channel);
        if (channel.getParent() != null) {
            channel.getParent().getChild().add(channel);
        }
        cmsSiteService.update(channelSite);
        cmsSiteService.flush();
        return new ResponseInfo(true);
    }


    @RequestMapping(value = "/top", method = RequestMethod.POST)
    @OperatingIntercept
    public ResponseInfo saveTop(@RequestBody @Valid ChannelSaveDto dto, BindingResult result,
                                HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        if (dto.getChannelParentId() != null && dto.getChannelParentId() != 0) {
            return new ResponseInfo(
                    SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                    SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage());
        }
        return this.save(dto, request);
    }


    /**
     * 校验栏目名称或者路径是否重复，true表示不可用，false表示可用
     */
    @RequestMapping(value = "/element/unique", method = RequestMethod.GET)
    public ResponseInfo checkNameOrPath(@RequestParam(name = "name", required = false) String name,
                                        @RequestParam(name = "path", required = false) String path,
                                        @RequestParam(name = "id", required = false) Integer id, HttpServletRequest request)
            throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        Channel channel = null;
        if (id != null) {
            channel = service.findById(id);
            if (channel == null) {
                return new ResponseInfo(RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode(),
                        RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage());
            }
        }
        return new ResponseInfo(!service.checkElement(channel, name, path, siteId));
    }

    /**
     * 查询栏目路径前缀相同的数量
     *
     * @param path
     * @param request
     * @return
     */
    @RequestMapping(value = "/countPathPrefix", method = RequestMethod.GET)
    public ResponseInfo countPathPrefix(@RequestParam(name = "path", required = false) String path,
                                        HttpServletRequest request) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        return new ResponseInfo(service.countByPathBeforeAndRecycleAndHasDeleted(siteId, path + "%", false, false));
    }


    /**
     * 新增时的字段
     */
    @MoreSerializeField({@SerializeField(clazz = CmsModel.class, includes = {"enableJson"})})
    @RequestMapping(value = "/plus/{modelId}", method = RequestMethod.GET)
    public ResponseInfo getSave(@PathVariable(name = "modelId") Integer modelId) throws GlobalException {
        CmsModel model = cmsModelService.getChannelOrContentModel(modelId);
        if (CmsModel.CHANNEL_TYPE.equals(model.getTplType())) {
            return new ResponseInfo(model);
        }
        return new ResponseInfo();
    }

    /**
     * 批量新增栏目
     */
    @RequestMapping(value = "/multiple", method = RequestMethod.POST)
    public ResponseInfo saveAll(@RequestBody @Valid ChannelSaveMultipleDto dto, HttpServletRequest request,
                                BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        CmsSite site = SystemContextUtils.getSite(request);
        dto.setSiteId(site.getId());
        if (dto.getChannelParentId() != null && dto.getChannelParentId() != 0) {
            long count = contentService.countByChannelIdInAndRecycle(new Integer[]{dto.getChannelParentId()}, false);
            if (count > 0) {
                return new ResponseInfo(
                        ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT_NOT_SAVE
                                .getCode(),
                        ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT_NOT_SAVE
                                .getDefaultMessage());
            }
        } else {
            return new ResponseInfo(ChannelErrorCodeEnum.CHANNEL_PARENT_CLASS_ID_PASSED_ERROR.getCode(),
                    ChannelErrorCodeEnum.CHANNEL_PARENT_CLASS_ID_PASSED_ERROR.getDefaultMessage());
        }
        List<String> channelNames = dto.getChannelNames();
        Integer spaceNum = null;
        Integer parentSpaceNum = 0;
        for (int i = 0; i < channelNames.size(); i++) {
            spaceNum = channelNames.get(i).replaceAll("([ ]*).*", "$1").length();
            if (i == 0 && spaceNum > 0) {
                return new ResponseInfo(
                        ChannelErrorCodeEnum.CHANNEL_NAME_FORMAT_PASSED_ERROR.getCode(),
                        ChannelErrorCodeEnum.CHANNEL_NAME_FORMAT_PASSED_ERROR
                                .getDefaultMessage());
            }
            if ((spaceNum - parentSpaceNum) > 2 || (spaceNum % 2) != 0) {
                return new ResponseInfo(
                        ChannelErrorCodeEnum.CHANNEL_NAME_FORMAT_PASSED_ERROR.getCode(),
                        ChannelErrorCodeEnum.CHANNEL_NAME_FORMAT_PASSED_ERROR
                                .getDefaultMessage());
            }
            parentSpaceNum = spaceNum;
        }
        List<Channel> channels = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            List<ChannelSaveAllDto> dtos = channelDtoService.initChannelSaveAllDto(dto);
            super.checkChannelDataPerm(dto.getChannelParentId(), OpeChannelEnum.CREATE);
            channels = service.saveAll(dtos, site);
        } finally {
            lock.unlock();
        }
        /** 主动维护站点栏目集合，方便权限根据站点取栏目数据 */
        CmsSite channelSite = channels.get(0).getSite();
        for (Channel channel : channels) {
            channelSite.getChannels().add(channel);
        }
        cmsSiteService.update(channelSite);
        cmsSiteService.flush();
        return new ResponseInfo(true);
    }


    /**
     * 修改主体
     */
    @RequestMapping(method = RequestMethod.PUT)
    @OperatingIntercept
    public ResponseInfo updateChannel(@RequestBody @Valid ChannelDto dto, BindingResult result,
                                      HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        if (dto.getChannelParentId() == null || dto.getChannelParentId() == 0) {
            return new ResponseInfo(
                    SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                    SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage());
        }
        return this.updateChannel(dto, request);
    }


    private ResponseInfo updateChannel(ChannelDto dto, HttpServletRequest request) throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        Channel channel = service.findById(dto.getId());
        if (channel == null) {
            return new ResponseInfo(
                    RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode(),
                    RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage());
        }
        // 前端已经校验过了，但此处必须进行强校验，防止脏数据
        boolean status = service.checkElement(channel, dto.getChannelName(), null, siteId);
        if (status) {
            return new ResponseInfo(SettingErrorCodeEnum.CHANNEL_NAME_ALREADY_EXIST.getCode(),
                    SettingErrorCodeEnum.CHANNEL_NAME_ALREADY_EXIST.getDefaultMessage());
        }
        status = service.checkElement(channel, null, dto.getChannelPath(), siteId);
        if (status) {
            return new ResponseInfo(SettingErrorCodeEnum.CHANNEL_PATH_ALREADY_EXIST.getCode(),
                    SettingErrorCodeEnum.CHANNEL_PATH_ALREADY_EXIST.getDefaultMessage());
        }
        if (dto.getChannelParentId() != null) {
            if (dto.getChannelParentId() == 0) {
                dto.setChannelParentId(null);
            } else {
                Channel parentChannel = service.findByIdAndRecycleAndHasDeleted(dto.getChannelParentId());
                long count = contentService.countByChannelIdInAndRecycle(new Integer[]{dto.getChannelParentId()}, false);
                if (parentChannel == null || count > 0) {
                    return new ResponseInfo(
                            ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT_NOT_SAVE
                                    .getCode(),
                            ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT_NOT_SAVE
                                    .getDefaultMessage());
                }
            }
        }
        super.checkChannelDataPerm(dto.getId(), OpeChannelEnum.EDIT);
        service.updateChannel(dto);
        return new ResponseInfo(true);
    }

    /**
     * 修改主体
     */
    @RequestMapping(value = "/top", method = RequestMethod.PUT)
    @OperatingIntercept
    public ResponseInfo updateChannelTop(@RequestBody @Valid ChannelDto dto, BindingResult result,
                                         HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        if (dto.getChannelParentId() != null && dto.getChannelParentId() != 0) {
            return new ResponseInfo(
                    SystemExceptionEnum.ILLEGAL_PARAM.getCode(),
                    SystemExceptionEnum.ILLEGAL_PARAM.getDefaultMessage());
        }
        return this.updateChannel(dto, request);
    }


    /**
     * 所有查询必须注意条件：站点
     */
    @MoreSerializeField({
            @SerializeField(clazz = ResourcesSpaceData.class, includes = {"id", "resourceType",
                    "alias", "url", "suffix"}),
            @SerializeField(clazz = ChannelAttrRes.class, includes = {"resourcesSpaceData",
                    "description", "secret"}),
            @SerializeField(clazz = SysSecret.class, includes = {"id", "name", "secretType"}),
            @SerializeField(clazz = ChannelContentTpl.class, includes = {"modelId", "tplMobile",
                    "tplPc", "select"})})
    @RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
    public ResponseInfo get(@PathVariable(name = "id") Integer id, HttpServletRequest request)
            throws GlobalException {
        CmsSite site = SystemContextUtils.getSite(request);
        return new ResponseInfo(service.findById(id, site));
    }

    @MoreSerializeField({@SerializeField(clazz = Channel.class, includes = {
            "viewContentAble", "editContentAble", "mergeAble", "staticAble",
            "deleteContentAble", "fileContentAble", "topContentAble", "moveContentAble", "sortContentAble",
            "copyContentAble", "quoteContentAble", "typeContentAble", "createContentAble",
            "publishContentAble", "sitePushContentAble", "wechatPushContentAble", "weiboPushContentAble",
            "editAble", "createChildAble", "permAssignAble", "deleteAble", "viewAble"})
    })
    @RequestMapping(value = "/getPerm", method = RequestMethod.GET)
    public ResponseInfo getForPerm(Integer id, HttpServletRequest request) throws GlobalException {
        return new ResponseInfo(service.get(id));
    }

    /**
     * 查询内容模型模板
     */
    @RequestMapping(value = "/content/model", method = RequestMethod.GET)
    @MoreSerializeField({
            @SerializeField(clazz = SiteModelTpl.class, includes = {"modelId", "pcTplPath",
                    "mobileTplPath", "model"}),
            @SerializeField(clazz = CmsModel.class, includes = {"modelName"})
    })
    public ResponseInfo findByModelId(@RequestParam Integer channelId, HttpServletRequest request)
            throws GlobalException {
        Integer siteId = SystemContextUtils.getSite(request).getId();
        return new ResponseInfo(service.findModelTplVo(siteId, channelId));
    }


    /**
     * 根据条件查询list集合
     *
     * @param name    栏目名称 是否栏目列表，true是栏目列表的树 否则是内容列表的树
     * @param permOut 权限是否输出
     * @param request HttpServletRequest
     * @throws GlobalException GlobalException
     * @Title: getList
     * @return: ResponseInfo
     */
    @RequestMapping(value = "/term/tree", method = RequestMethod.GET)
    public ResponseInfo getList(@RequestParam(required = false) String name,
                                Boolean permOut, HttpServletRequest request)
            throws GlobalException {
        long t1 = System.currentTimeMillis();
        CoreUser user = SystemContextUtils.getUser(request);
        List<Channel> channelList = user.getViewNoCycleChannels();
        Integer siteId = SystemContextUtils.getSiteId(request);
        if (channelList.size() > 0) {
            channelList = channelList.stream().filter(
                    channel -> !channel.getHasDeleted()
                            &&
                            !channel.getRecycle()
                            &&
                            siteId.equals(channel.getSiteId()))
                    .collect(Collectors.toList());
        }
        List<Channel> channels = channelList;
        /**
         * 此处使用strem进行检索
         */
        if (name != null) {
            channels = channelList.stream().filter(channel -> channel.getName().indexOf(name) != -1)
                    .collect(Collectors.toList());
        }
        List<Channel> newChannnels = new ArrayList<Channel>();
        for (Channel channel : channels) {
            List<Channel> newChannels = channelList.stream()
                    .filter(c -> c.getLft() < channel.getLft() && c.getRgt() > channel.getRgt())
                    .collect(Collectors.toList());
            if (newChannels != null && newChannels.size() > 0) {
                newChannnels.addAll(newChannels);
            }
        }
        channels.addAll(newChannnels);
        List<Integer> ids = new ArrayList<Integer>();
        Iterator<Channel> iterator = channels.iterator();
        while (iterator.hasNext()) {
            Integer id = iterator.next().getId();
            if (ids.contains(id)) {
                iterator.remove();
            } else {
                ids.add(id);
            }
        }
        channels = channels.stream()
                .sorted(Comparator.comparing(Channel::getSortNum)
                        .thenComparing(
                                Comparator.comparing(Channel::getCreateTime)))
                .collect(Collectors.toList());
        if (permOut == null) {
            permOut = true;
        }
        long t3 = System.currentTimeMillis();
        if (permOut) {
            return new ResponseInfo(service.getChannelsByUserChannelPerm(user, siteId, channels));
        } else {
            return new ResponseInfo(super.getChildTree(channels, false, "id", "name", "sortNum"));
        }
    }

    /**
     * 根据条件查询list集合
     */
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public ResponseInfo getTree(HttpServletRequest request, Boolean recycle) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        if (recycle == null) {
            recycle = false;
        }
        CoreUser user = SystemContextUtils.getCoreUser();
        JSONArray jsonArray = service.getChannelsByUserChannelPerm(user, siteId, service.getList(siteId, recycle));
        return new ResponseInfo(jsonArray);
    }

    /**
     * 忽略权限的栏目树（目前用于索引设置开关）
     */
    @RequestMapping(value = "/getAllTree", method = RequestMethod.GET)
    public ResponseInfo getAllTree(Integer siteId, HttpServletRequest request) throws GlobalException {
        if (siteId == null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        List<Channel> channelList = service.getList(siteId, false);
//        channelList = channelList.stream().filter(channel -> channel.getRecycle().equals(false))
//                .sorted(Comparator.comparing(Channel::getSortNum)
//                        .thenComparing(Comparator.comparing(Channel::getCreateTime)))
//                .collect(Collectors.toList());
        return new ResponseInfo(super.getChildTree(channelList, false, "name", "id",
                "isOpenIndex", "urlWhole"));
    }

    /**
     * 获取可生成栏目静态页的栏目树
     */
    @RequestMapping(value = "/staticChannelTree", method = RequestMethod.GET)
    public ResponseInfo getCreateChannelPageTree(Integer siteId, HttpServletRequest request)
            throws GlobalException {
        if (siteId == null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        final Integer sid = siteId;
        List<Channel> channelList = SystemContextUtils.getUser(request).getChannelsByOperator(sid, CmsDataPerm.OPE_CHANNEL_STATIC);
        channelList = channelList.stream().filter(channel -> channel.getRecycle().equals(false))
                .filter(channel -> channel.getSiteId().equals(sid))
                .sorted(Comparator.comparing(Channel::getSortNum)
                        .thenComparing(Comparator.comparing(Channel::getCreateTime)))
                .collect(Collectors.toList());
        return new ResponseInfo(super.getChildTree(channelList, false, "name", "id", "hasStaticChannel"));
    }

    /**
     * 获取可发布内容的栏目树
     */
    @RequestMapping(value = "/publicContentChannelTree", method = RequestMethod.GET)
    public ResponseInfo getPublishContentPageTree(Integer siteId, HttpServletRequest request)
            throws GlobalException {
        if (siteId == null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        final Integer sid = siteId;
        List<Channel> channelList = SystemContextUtils.getUser(request).getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_PUBLISH);
        channelList = channelList.stream().filter(channel -> channel.getRecycle().equals(false))
                .filter(channel -> channel.getSiteId().equals(sid))
                .sorted(Comparator.comparing(Channel::getSortNum)
                        .thenComparing(Comparator.comparing(Channel::getCreateTime)))
                .collect(Collectors.toList());
        return new ResponseInfo(super.getChildTree(channelList, false, "name", "id", "hasStaticContent"));
    }

    /**
     * 获取内容的通用栏目树
     */
    @RequestMapping(value = "/content/common/tree", method = RequestMethod.GET)
    public ResponseInfo getContentCommentTree(@RequestParam(required = false) String name, Integer siteId,
                                              @RequestParam Short operator, Boolean permOut, HttpServletRequest request) {
        if (siteId == null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        final Integer sid = siteId;
        List<Channel> channels = SystemContextUtils.getUser(request)
                .getContentChannelsByOperator(sid, operator);
        if (channels.size() > 0) {
            /**
             * 此处使用strem进行检索
             */
            if (name != null) {
                channels = channels.stream().filter(channel -> channel.getName().contains(name))
                        .collect(Collectors.toList());
            }
            channels = channels.stream()
                    .filter(channel -> !channel.getRecycle() && !channel.getHasDeleted()
                            && channel.getSiteId().equals(sid))
                    .sorted(Comparator.comparing(Channel::getSortNum)
                            .thenComparing(
                                    Comparator.comparing(Channel::getCreateTime)))
                    .collect(Collectors.toList());
        }
        if (permOut == null) {
            permOut = true;
        }
        if (permOut) {
            return new ResponseInfo(ChannelAgent.convertListToJsonArrayWithContentPerm(channels,SystemContextUtils.getCoreUser()));
        } else {
            JSONArray result = ChannelAgent.convertListToJsonArray(channels);
            return new ResponseInfo(result);
        }
    }

    /**
     * 获取栏目的通用栏目树
     */
    @RequestMapping(value = "/common/tree", method = RequestMethod.GET)
    public ResponseInfo getCommentTree(Integer siteId, @RequestParam Short operator, HttpServletRequest request)
            throws GlobalException {
        if (siteId == null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        final Integer sid = siteId;
        List<Channel> channels = SystemContextUtils.getUser(request).getChannelsByOperator(siteId, operator);
        if (channels.size() > 0) {
            channels = channels.stream()
                    .filter(channel -> !channel.getRecycle() && !channel.getHasDeleted()
                            && channel.getSiteId().equals(sid))
                    .sorted(Comparator.comparing(Channel::getSortNum)
                            .thenComparing(
                                    Comparator.comparing(Channel::getCreateTime)))
                    .collect(Collectors.toList());
        }
        return new ResponseInfo(super.getChildTree(channels, false, "name", "id", "editAble", "createChildAble",
                "deleteAble", "mergeAble", "staticAble", "permAssignAble", "viewAble"));
    }

    /**
     * 查询所有的PC端模板、手机模板(模型之下)
     */
    @RequestMapping(value = "/template/list", method = RequestMethod.GET)
    @MoreSerializeField({@SerializeField(clazz = CmsModelTpl.class, includes = {"tplPath"})})
    public ResponseInfo findByModel(@RequestParam Integer modelId, @RequestParam Short type,
                                    HttpServletRequest request) throws GlobalException {
        CmsSite site = SystemContextUtils.getSite(request);
        CmsSiteConfig siteConfig = site.getCmsSiteCfg();
        List<CmsModelTpl> cmtList = null;
        // 通过站点配置的模板方案名查询出该站点下该模型配置的PC模板路径
        if (CmsModelTpl.TPL_TYPE_CHANNEL.equals(type)) {
            cmtList = cmsModelTplService.models(site.getId(), modelId, siteConfig.getPcSolution());
        }
        // 通过站点配置的模板方案名查询出该站点下该模型配置的手机模板路径
        if (CmsModelTpl.TPL_TYPE_CONTENT.equals(type)) {
            cmtList = cmsModelTplService.models(site.getId(), modelId, siteConfig.getMobileSolution());
        }
        return new ResponseInfo(cmtList);
    }

    /**
     * 用于回收站对栏目操作(未过滤加入回收站的栏目)
     */
    @RequestMapping(value = "/recycle/tree", method = RequestMethod.GET)
    public ResponseInfo getRecycleTree(HttpServletRequest request) throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        // 此处忽略掉权限，因为此处有两种甚至更多种权限：1. 查看栏目、2.修改栏目、3.修改内容、4.查看内容
        List<Channel> channelList = service.findListBySiteId(siteId);
        return new ResponseInfo(super.getChildTree(channelList, false, "name", "id", "recycle"));
    }

    /**
     * 查询所有的栏目模型(全局+站点)
     */
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    @MoreSerializeField({
            @SerializeField(clazz = CmsModel.class, includes = {"id", "modelName"})
    })
    public ResponseInfo findModelList(
            @RequestParam Short tplType, HttpServletRequest request) throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        return new ResponseInfo(cmsModelService.findList(tplType, siteId));
    }

	/**
	 * 加入回收站
	 */
    @PostMapping("/delete")
    @OperatingIntercept
	@Override
	public ResponseInfo delete(@RequestBody @Valid DeleteDto ids, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		for (int i = 0; i < ids.getIds().length; i++) {
			super.checkChannelDataPerm(ids.getIds()[i], OpeChannelEnum.DEL);
		}
		service.delete(ids.getIds(), false);
		return new ResponseInfo(true);
	}

    /**
     * 彻底删除(逻辑删除)
     */
    @RequestMapping(value = "/thorough/delete", method = RequestMethod.POST)
    public ResponseInfo thoroughDelete(@RequestBody @Valid DeleteDto ids, BindingResult result)
            throws GlobalException {
        super.validateBindingResult(result);
        for (int i = 0; i < ids.getIds().length; i++) {
            super.checkChannelDataPerm(ids.getIds()[i], OpeChannelEnum.DEL);
        }
        service.delete(ids.getIds(), true);
        return new ResponseInfo(true);
    }

    /**
     * 还原栏目
     */
    @RequestMapping(value = "/reduction", method = RequestMethod.PUT)
    public ResponseInfo reduction(@RequestBody @Valid ChannelReductionDto dto, BindingResult result,
                                  HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        Integer siteId = SystemContextUtils.getSiteId(request);
        // 此处忽略掉权限，因为此处有两种甚至更多种权限：1. 查看栏目、2.修改栏目、3.修改内容、4.查看内容
        dto.setSiteId(siteId);
        service.reduction(dto);
        return new ResponseInfo();
    }

    /**
     * 设置栏目静态化开关
     */
    @RequestMapping(value = "/setOpenIndex", method = RequestMethod.PUT)
    public ResponseInfo setOpenIndex(@RequestBody @Valid ChannelSetIndexDto channelOpens, BindingResult result)
            throws GlobalException {
        super.validateBindingResult(result);
        service.setOpenIndex(channelOpens);
        return new ResponseInfo();
    }

    /**
     * 应用工作流
     */
    @RequestMapping(value = "/claim/workflow", method = RequestMethod.POST)
    public ResponseInfo claimWorkflow(@RequestBody @Valid ChannelWorkflowDto dto, BindingResult result,
                                      HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        // 测试
        for (int i = 1; i < dto.getIds().size(); i++) {
            super.checkChannelDataPerm(dto.getIds().get(i), OpeChannelEnum.EDIT);
        }

        if (dto.getAll()) {
            List<Channel> channels = SystemContextUtils.getCoreUser().getEditChannels();
            if (channels.size() > 0) {
                Integer siteId = SystemContextUtils.getSiteId(request);
                channels = channels.stream().filter(
                        channel -> channel.getSiteId().equals(siteId)
                                &&
                                !channel.getId().equals(dto.getChannelId()))
                        .collect(Collectors.toList());
            } else {
                channels = new ArrayList<Channel>();
            }
            dto.setChannels(channels);
        }
        service.claimWorkflow(dto);
        return new ResponseInfo(true);
    }

    /**
     * 查询该站点已经删除的栏目ID集合
     */
    @RequestMapping(value = "/recycle/ids", method = RequestMethod.GET)
    public ResponseInfo getrecycleIds(HttpServletRequest request) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        List<Channel> channelList = service.findAll(true);
        List<Integer> ids = new ArrayList<Integer>();
        if (channelList != null && channelList.size() > 0) {
            channelList = channelList.stream().filter(channel -> channel.getSiteId().equals(siteId))
                    .filter(channel -> channel.getRecycle()).collect(Collectors.toList());
            ids = channelList.stream().map(channel -> channel.getId()).collect(Collectors.toList());
        }
        return new ResponseInfo(ids);

    }

    /**
     * 移动排序
     */
    @RequestMapping(value = "/sort", method = RequestMethod.PUT)
    public ResponseInfo channelSort(@RequestBody @Validated ChannelSortDto dto, BindingResult result,
                                    HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        super.checkChannelDataPerm(dto.getChannelId(), OpeChannelEnum.EDIT);
        List<Channel> channelList = SystemContextUtils.getUser(request).getViewChannels();
        service.channelSort(dto, channelList);
        return new ResponseInfo();
    }

    /**
     * 合并栏目
     */
    @RequestMapping(value = "/merge", method = RequestMethod.PUT)
    @OperatingIntercept
    public ResponseInfo merge(@RequestBody @Valid ChannelMergeDto dto, BindingResult result)
            throws GlobalException {
        super.validateBindingResult(result);
        Integer id = dto.getId();
        Channel channel = service.findById(id);
        if (channel == null) {
            return new ResponseInfo(RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode(),
                    RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage());
        }
        if (!channel.getIsBottom()) {
            return new ResponseInfo(ChannelErrorCodeEnum.AIMS_CHANNEL_IS_NOT_BOTTOM_NOT_MERGE.getCode(),
                    ChannelErrorCodeEnum.AIMS_CHANNEL_IS_NOT_BOTTOM_NOT_MERGE.getDefaultMessage());
        }
        Integer[] ids = dto.getIds();
        for (Integer i : ids) {
            if (i.equals(id)) {
                return new ResponseInfo(RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode(),
                        RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage());
            }
            super.checkChannelDataPerm(i, OpeChannelEnum.MERGE);
        }
        super.checkChannelDataPerm(id, OpeChannelEnum.MERGE);
        service.mergeChannel(channel, ids);
        return new ResponseInfo(true);
    }

    /**
     * 校验该栏目是否拥有工作流
     */
    @RequestMapping(value = "/workflow/being/{id:[0-9]+}", method = RequestMethod.GET)
    public ResponseInfo uniqueWorkflow(@PathVariable(name = "id") Integer id) {
        if (id == null) {
            // 如果ID为空，则说明前端处理或者是数据有问题，此处不认为是BUG不抛出异常
            return new ResponseInfo(false);
        }
        Channel channel = service.findById(id);
        if (channel == null || channel.getHasDeleted() || channel.getRecycle()) {
            return new ResponseInfo(RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode(),
                    RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage());
        }
        if (channel.getRealWorkflowId() != null) {
            return new ResponseInfo(true);
        }
        return new ResponseInfo(false);
    }

    /**
     * 新增内容查看部分栏目默认值
     */
    @RequestMapping(value = "/defaults/{id:[0-9]+}", method = RequestMethod.GET)
    public ResponseInfo defaults(@PathVariable(name = "id") Integer id) {
        // 此处不校验权限，因为如果校验的是查看权限可能满足不了，如果校验新增权限，又不符合使用场景
        Channel channel = service.findById(id);
        if (channel == null || channel.getHasDeleted() || channel.getRecycle()) {
            return new ResponseInfo(RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getCode(),
                    RPCErrorCodeEnum.INCOMING_ID_TYPE_IS_INCORRECT.getDefaultMessage());
        }
        // 此处不用对象返回时因为返回VO对象，里面的对象也是JSON，因为里面对象的数据量的大小和对象的名称无法确定
        JSONObject returnJson = new JSONObject();
        returnJson.put(CmsModelConstant.FIELD_SYS_VIEW_CONTROL, channel.getChannelExt().getRealViewControl());
        returnJson.put(
                CmsModelConstant.FIELD_SYS_COMMENT_CONTROL, channel.getChannelExt()
                        .getRealCommentControl());
        return new ResponseInfo(returnJson);
    }
}
