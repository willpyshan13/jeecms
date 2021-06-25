/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.channel.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreRole;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreRoleService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.channel.constants.ChannelConstant;
import com.jeecms.channel.dao.ChannelDao;
import com.jeecms.channel.domain.*;
import com.jeecms.channel.domain.dto.*;
import com.jeecms.channel.domain.dto.ChannelSetIndexDto.ChannelOpenSet;
import com.jeecms.channel.domain.vo.ChannelModelTplVo;
import com.jeecms.channel.service.*;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.ChannelExceptionInfo;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.ChannelErrorCodeEnum;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.component.listener.ChannelListener;
import com.jeecms.component.listener.WorkflowListener;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.*;
import com.jeecms.content.service.*;
import com.jeecms.protection.service.ParameterDecryptionUtilService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.*;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.system.service.SiteModelTplService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 栏目service实现类
 *
 * @author: tom
 * @date: 2019年3月19日 下午6:32:33
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ChannelServiceImpl extends BaseServiceImpl<Channel, ChannelDao, Integer>
        implements ChannelService, WorkflowListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelServiceImpl.class);
    @Autowired
    private CmsSiteService cmsSiteService;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private CmsModelItemService cmsModelItemService;
    @Autowired
    private ChannelContentTplService channelContentTplService;
    @Autowired
    private List<ChannelListener> channelListeners;
    @Autowired
    private ChannelTxtService channelTxtService;
    @Autowired
    @Lazy
    private ChannelDtoService channelDtoService;
    @Autowired
    private ChannelAttrService channelAttrService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private ChannelAttrResService channelAttrResService;
    @Autowired
    private SiteModelTplService siteModelTplService;
    @Autowired
    private CmsModelTplService cmsModelTplService;
    @Autowired
    private CoreUserService userService;
    @Autowired
    private CmsOrgService orgService;
    @Autowired
    private CoreRoleService roleService;
    @Autowired
    private FlowService flowService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;
    @Autowired
    private CmsModelTplService tplService;
    @Autowired
    private ParameterDecryptionUtilService parameterDecryptionUtilService;

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> findTopList(Integer siteId, boolean containNotDisplay) {
        /**
         * TODO 此处获取的不是顶层栏目，而恰恰是查询全部
         */
        return super.dao.findList(siteId, null, null, containNotDisplay, null,
                new PaginableRequest(0, Integer.MAX_VALUE), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> findListByParentId(Integer siteId, Integer parentId, boolean containNotPublish,
                                            boolean containNotDisplay) {
        return super.dao.findList(siteId, null, parentId, containNotDisplay, null,
                new PaginableRequest(0, Integer.MAX_VALUE), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Page<Channel> findPageByParentId(Integer siteId, Integer parentId, boolean containNotPublish,
                                            boolean containNotDisplay, Pageable pageable) {
        return super.dao.findPage(siteId, null, parentId, containNotDisplay, null,
                pageable, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> findList(Integer siteId, boolean containNotDisplay) {
        List<Channel> topChannels = findTopList(siteId, containNotDisplay);
        List<Channel> channels = Channel.getListForSelect(topChannels, null, containNotDisplay, null);
        return channels;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> findListBySiteId(Integer siteId) {
        return dao.findBySiteIdAndHasDeletedOrderBySortNumAscCreateTimeDesc(siteId, false);
    }

    /**
     * 底下的新增、修改没有做强校验因为此处的可能性太多了
     */
    @Override
    public Channel save(ChannelSaveDto dto) throws GlobalException {
        ChannelSaveAllDto saveAllDto = new ChannelSaveAllDto();
        MyBeanUtils.copyProperties(dto, saveAllDto);
        CmsSite site = cmsSiteService.findById(dto.getSiteId());

        Integer sortNum = dao.findBySortNum(site.getId(), dto.getChannelParentId());
        if (sortNum != null) {
            saveAllDto.setSortNum(sortNum + 2);
        }

        Channel channel = this.save(saveAllDto, site);
        return channel;
    }

    /**
     * 新增具体逻辑
     */
    private Channel save(ChannelSaveAllDto dto, CmsSite site) throws GlobalException {
        if (ChannelConstant.CHANNEL_ILLEGAL_PATH.equals(dto.getChannelPath())) {
            throw new GlobalException(ChannelErrorCodeEnum.CHANNEL_PATH_IS_NOT_INDEX);
        }
        Channel channel = new Channel();
        MyBeanUtils.copyProperties(dto, channel);
        /** 从站点中取出默认设置 */
        CmsSiteConfig cmsSiteConfig = site.getConfig();
        channel.setSite(site);
        CmsModel cmsModel = cmsModelService.findById(dto.getModelId());
        channel.setModel(cmsModel);
        // 此处不处理栏目PC模板和手机端模板
        ChannelSaveAllDto.initChannel(channel, cmsSiteConfig, dto);
        /** 定义栏目扩展的一些字段的默认值 */
        ChannelExt channelExt = new ChannelExt();
        ChannelSaveAllDto.initChannelExt(channelExt, cmsSiteConfig);
        channel.setChannelExt(channelExt);
        channel.getChannelExt().setChannel(channel);
        // 此处如此处理是因为之前出现channelExt中查询channel为空，所以特地如此操作
        Channel bean = super.save(channel);
//        bean.getSite().getChannels().add(bean);
        // 解决：在批量插入中，插入子集找不到子集报：No entity found for query
        super.flush();
        // 虽然新增时此处不会传入，但是站点此处配置了，所以此处必须配置
        List<ChannelContentTpl> contentTpls = channelContentTplService.save(null, bean.getId(), dto.getSiteId());
        bean.setContentTpls(contentTpls);
        List<ChannelAttr> attrs = new ArrayList<ChannelAttr>();
        attrs = channelAttrService.splicDefValue(bean.getModelId(), attrs, false, bean);
        if (attrs.size() > 0) {
            channelAttrService.saveAll(attrs);
            channelAttrService.flush();
        }
        this.initConentObject(bean);
        this.initContentExtObject(bean.getChannelExt());
        /**
         * 新增之后调用监听器，正确的逻辑操作都在监听器中防止减耦合
         */
        CoreUser user = SystemContextUtils.getCoreUser();
        user.clearPermCache();
        user.getOrg().clearPermCache();
        for (CoreRole role : user.getRoles()) {
            role.clearPermCache();
        }
        ThreadPoolService.getInstance().execute(() -> {
            try {
                for (ChannelListener listener : channelListeners) {
                    listener.afterChannelSave(bean);
                }
            } catch (GlobalException e) {
                LOGGER.error(e.getMessage());
            }
        });
        return bean;
    }

    @Override
    public List<Channel> saveAll(List<ChannelSaveAllDto> dtos, CmsSite site) throws GlobalException {
        Boolean isCheakName = site.getConfig().getChannelNameRepeat();
        nameList = dao.checkNameAndPath(true, false, site.getId());
        if (isCheakName) {
            nameList = null;
        }
        pathMap = dao.checkNameAndPath(false, true, site.getId()).stream()
                .collect(Collectors.groupingBy(s -> s.toString(), Collectors.counting()));
        channels = new ArrayList<Channel>();
        this.cycleSave(dtos, null, site);
        return channels;
    }

    /**
     * 站点之下的所有栏目名称List
     */
    private List<String> nameList = new ArrayList<String>();
    /**
     * 站点之下的所有站点路径Map
     */
    private Map<String, Long> pathMap = new HashMap<String, Long>();

    /**
     * 批量新增具体实现
     */
    List<Channel> channels;

    private List<ChannelSaveAllDto> cycleSave(List<ChannelSaveAllDto> dtos, Integer parentId, CmsSite site)
            throws GlobalException {
        for (ChannelSaveAllDto channelSavaAllDto : dtos) {
            if (channelSavaAllDto.getChannelParentId() == null) {
                channelSavaAllDto.setChannelParentId(parentId);
            }
            Boolean a = true;
            String path = channelSavaAllDto.getChannelPath();
            while (a) {
                Long value = pathMap.get(path);
                if (pathMap.get(path) != null) {
                    pathMap.put(path, value + 1L);
                    path = path + pathMap.get(path);
                    channelSavaAllDto.setChannelPath(path);
                } else {
                    pathMap.put(path, 1L);
                    a = false;
                }
            }
            if (nameList != null) {
                if (nameList.contains(channelSavaAllDto.getChannelName())) {
                    throw new GlobalException(
                            new SystemExceptionInfo(SettingErrorCodeEnum.CHANNEL_NAME_ALREADY_EXIST.getDefaultMessage(),
                                    SettingErrorCodeEnum.CHANNEL_NAME_ALREADY_EXIST.getCode()));
                }
            }
            Channel channel = this.save(channelSavaAllDto, site);
            channels.add(channel);
            if (channelSavaAllDto.getChildren() != null) {
                this.cycleSave(channelSavaAllDto.getChildren(), channel.getId(), site);
            }
        }
        return dtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public boolean checkElement(Channel channel, String name, String path, Integer siteId) throws GlobalException {
        if (name != null) {
            CmsSiteConfig cmsSiteConfig = cmsSiteService.findById(siteId).getCmsSiteCfg();
            // 查询该站点是否允许栏目名称重复
            if (cmsSiteConfig.getChannelNameRepeat()) {
                return false;
            }
            nameList = dao.checkNameAndPath(true, false, siteId);
            if (channel != null) {
                if (channel.getName().equals(name)) {
                    return false;
                }
            }
            return nameList.contains(name);
        }
        if (path != null) {
            List<String> pathList = dao.checkNameAndPath(false, true, siteId);
            if (channel != null) {
                if (channel.getPath().equals(path)) {
                    return false;
                }
            }
            return pathList.contains(path);
        }
        return false;
    }

    /**
     * 查询栏目路径前缀相同的数量
     *
     * @param path       栏目路径
     * @param recycle    是否加入回收站
     * @param hasDeleted 是否逻辑删除
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public long countByPathBeforeAndRecycleAndHasDeleted(Integer siteId, String path, Boolean recycle, Boolean hasDeleted) {
        return dao.countBySiteIdAndPathLikeAndRecycleAndHasDeleted(siteId, path, recycle, hasDeleted);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public ChannelFindVo findById(Integer id, CmsSite site) throws GlobalException {
        Channel channel = super.findById(id);
        if (channel.getChannelExt().getResourceId() != null) {
            ResourcesSpaceData data = resourcesSpaceDataService.findById(channel.getChannelExt().getResourceId());
            if (data != null) {
                channel.getChannelExt().setResourcesSpaceData(data);
            } else {
                channel.getChannelExt().setResourcesSpaceData(null);
            }
        } else {
            channel.getChannelExt().setResourcesSpaceData(null);
        }
        CmsModel model = cmsModelService.getChannelOrContentModel(channel.getModelId());
        List<CmsModelItem> modelItems = cmsModelItemService.findByModelId(channel.getModelId());
        List<ChannelAttr> attrs = channelAttrService.findByChannelId(channel.getId());
        GlobalConfig globalConfig = SystemContextUtils.getGlobalConfig(RequestUtils.getHttpServletRequest());
        //查找该PC模板是否存在
        List<CmsModelTpl> pcTpls = tplService.findByTplPath(site.getSiteId(), channel.getTplPc(), site.getPcSolution());
        if (pcTpls.size() <= 0) {
            channel.setTplPc("");
        }
        //查找该手机模板是否存在
        List<CmsModelTpl> mobileTpls = tplService.findByTplPath(site.getSiteId(), channel.getTplMobile(), site.getMobileSolution());
        if (mobileTpls.size() <= 0) {
            channel.setTplMobile("");
        }
        ChannelFindVo findVo = new ChannelFindVo();
        List<Integer> cmsOrgIds = new ArrayList<Integer>();
        for (ChannelAttr attr : attrs) {
            if (CmsModelConstant.TISSUE.equals(attr.getAttrType())) {
                cmsOrgIds.add(attr.getOrgId());
            }
        }
        Map<Integer, CmsOrg> cmsOrgMap = null;
        if (cmsOrgIds.size() > 0) {
            List<CmsOrg> cmsOrgs = orgService.findAllById(cmsOrgIds);
            if (cmsOrgs != null && cmsOrgs.size() > 0) {
                cmsOrgMap = cmsOrgs.stream().collect(Collectors.toMap(CmsOrg::getId, c -> c));
            }
        }
        findVo = findVo.splicChannelFindVo(channel, globalConfig, model, modelItems, attrs, cmsOrgMap);
        return findVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Channel findByPath(String path, Integer siteId) {
        return dao.findByPath(path, siteId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> findByPath(String[] paths, Integer siteId) {
        return dao.findByPath(paths, siteId, false);
    }

    @Override
    public void updateChannel(ChannelDto channelDto) throws GlobalException {
        List<ChannelContentTpl> tplList = channelDto.getContentTpls();
        List<ChannelContentTpl> newTplList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tplList)) {
            for (ChannelContentTpl tpl : tplList) {
                tpl.setTplPc(parameterDecryptionUtilService.decryChannelOrContentParameter(tpl.getTplPc()));
                tpl.setTplMobile(parameterDecryptionUtilService.decryChannelOrContentParameter(tpl.getTplMobile()));
                newTplList.add(tpl);
            }
        }
        channelDto.setContentTpls(newTplList);
        channelDto.setTplPc(parameterDecryptionUtilService.decryChannelOrContentParameter(channelDto.getTplPc()));
        channelDto.setTplMobile(parameterDecryptionUtilService.decryChannelOrContentParameter(channelDto.getTplMobile()));
        JSONObject link = channelDto.getLink();
        if (link != null) {
            String linkName = link.getString(ChannelConstant.CHANNEL_LIKE_NAME);
            if (StringUtils.isBlank(linkName)) {
                linkName = link.getString(ChannelConstant.CHANNEL_LIKE_NAME_OTHER);
                link.put(ChannelConstant.CHANNEL_LIKE_NAME_OTHER, linkName);
            } else {
                linkName = parameterDecryptionUtilService.decryChannelOrContentParameter(linkName);
                link.put(ChannelConstant.CHANNEL_LIKE_NAME, linkName);
            }
            channelDto.setLink(link);
        }

        /** 栏目路径不可同index相同，是因为index.html是站点首页的静态页路径，静态页面栏目页和首页均放在站点目录下了 */
        if (ChannelConstant.CHANNEL_ILLEGAL_PATH.equals(channelDto.getChannelPath())) {
            throw new GlobalException(ChannelErrorCodeEnum.CHANNEL_PATH_IS_NOT_INDEX);
        }
        Channel channel = super.findById(channelDto.getId());

        if (channel.getParentId() != null) {
            if (!channel.getParentId().equals(channelDto.getChannelParentId())) {
                channel = this.updateSortNum(channelDto.getChannelParentId(), channel);
            }
        } else {
            if (channelDto.getChannelParentId() != null) {
                channel = this.updateSortNum(channelDto.getChannelParentId(), channel);
            }
        }

        final Integer oldWorkflowId = channel.getRealWorkflowId();
        List<CmsModelItem> items = cmsModelItemService.findByModelId(channel.getModelId());
        for (String groupType : CmsModelItem.CHANNEL_GROUP_TYPE) {
            if (groupType.equals(channelDto.getGroupType())) {
                items = items.stream().filter(item -> !item.getGroupType().equals(groupType))
                        .collect(Collectors.toList());
            }
        }
        ChannelDto.initChannelDto(items, channel, channelDto);
        link = channelDto.getLink();
        channelDto.setLink(null);
        MyBeanUtils.copyPropertiesContentNull(channelDto, channel);
        channel.setLinkTarget(false);
        if (link != null) {
            String linkName = link.getString(ChannelConstant.CHANNEL_LIKE_NAME);
            link.put(ChannelConstant.CHANNEL_LIKE_NAME, linkName);
            if (StringUtils.isBlank(linkName)) {
                linkName = link.getString(ChannelConstant.CHANNEL_LIKE_NAME_OTHER);
            }
            channel.setLink(linkName);
            channel.setLinkTarget(link.getBoolean(Channel.LINK_TARGET_NAME));
        }
        ChannelExt channelExt = channel.getChannelExt();
        MyBeanUtils.copyPropertiesContentNull(channelDto, channelExt);
        channel.setChannelExt(channelExt);
        // 底下部分数据无法通过copy，因为copy只是浅复制且有些字段名称不同
        channel.setName(channelDto.getChannelName());
        channel.setPath(channelDto.getChannelPath());
        channel.setParentId(channelDto.getChannelParentId());
        if (channel.getParentId() == null) {
            channel.setParent(null);
        }
        channel.setContentTpls(null);
        channel.setTxts(null);
        if (StringUtils.isNotBlank(channelExt.getTxt())) {
            channel.setDescription(channelExt.getTxt());
        }
        Channel bean = super.update(channel);
        super.flush();
        Integer newWorkflowId = channelDto.getWorkflowId();
        boolean isChangeWorkflow = oldWorkflowId != null && !oldWorkflowId.equals(newWorkflowId);
        /** 之前存在工作流且调整了工作流，则将现有流转中内容变更内容状态为初稿 */
        if (isChangeWorkflow) {
            List<Content> contents = contentService.findByChannels(new Integer[]{bean.getId()});
            if (contents != null && contents.size() > 0) {
                List<Integer> contentIds = contents.stream().filter(
                        content -> content.getStatus() == ContentConstant.STATUS_WAIT_PUBLISH || content.getStatus() == ContentConstant.STATUS_FLOWABLE)
                        .map(Content::getId).collect(Collectors.toList());
                if (contentIds.size() > 0) {
                    flowService.doInterruptDataFlow(ContentConstant.WORKFLOW_DATA_TYPE_CONTENT, contentIds,
                            SystemContextUtils.getCoreUser());
                }
            }
        }
        List<String> fields = new ArrayList<String>();
        if (items.size() > 0) {
            items = items.stream().filter(item -> CmsModelConstant.RICH_TEXT.equals(item.getDataType()))
                    .collect(Collectors.toList());
            if (items.size() > 0) {
                fields = items.stream().map(CmsModelItem::getField).collect(Collectors.toList());
            }
        }
        // 初始化并处理txts
        Map<String, String> txtMap = channelTxtService.toChannelTxtMap(channelDto.getJson(), channel.getModelId());
        txtMap = parameterDecryptionUtilService.decryChannelOrContentContent(txtMap);
        List<ChannelTxt> txts = channelTxtService.findByChannelId(bean.getId());
        if (txtMap != null) {
            if (txts != null) {
                List<ChannelTxt> deleteTxts = new ArrayList<ChannelTxt>();
                List<ChannelTxt> updatTxts = new ArrayList<ChannelTxt>();
                for (ChannelTxt channelTxt : txts) {
                    if (!fields.contains(channelTxt.getAttrKey())) {
                        String attrValue = txtMap.get(channelTxt.getAttrKey());
                        if (attrValue != null) {
                            channelTxt.setAttrTxt(attrValue);
                            updatTxts.add(channelTxt);
                            txtMap.remove(channelTxt.getAttrKey());
                        } else {
                            deleteTxts.add(channelTxt);
                        }
                    }
                }
                if (deleteTxts.size() > 0) {
                    channelTxtService.physicalDeleteInBatch(deleteTxts);
                }
                if (updatTxts.size() > 0) {
                    channelTxtService.batchUpdate(updatTxts);
                }
                if (txtMap != null || txtMap.keySet().size() > 0) {
                    List<ChannelTxt> newTxts = new ArrayList<ChannelTxt>();
                    for (String txtKey : txtMap.keySet()) {
                        ChannelTxt txt = new ChannelTxt(bean.getId(), txtKey, txtMap.get(txtKey));
                        newTxts.add(txt);
                    }
                    channelTxtService.saveAll(newTxts);
                }
            } else {
                List<ChannelTxt> newTxts = new ArrayList<ChannelTxt>();
                for (String txtKey : txtMap.keySet()) {
                    ChannelTxt txt = new ChannelTxt(bean.getId(), txtKey, txtMap.get(txtKey));
                    newTxts.add(txt);
                }
                channelTxtService.saveAll(newTxts);
            }
        }
        List<ChannelTxt> newTxts = channelTxtService.findByChannelId(bean.getId());
        bean.setTxts(newTxts);
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        CmsSite site = SystemContextUtils.getSite(request);
        // channelContentTpl此处配置级联出问题，所以直接使用异步插入的方式
        if (channelDto.getContentTpls() != null && !channelDto.getContentTpls().isEmpty()) {
            // 底下的模板和attrs都不使用级联的方式，而是使用其它方式
            //前台传值的情况下才去删除，不然会出现无法回填的Bug
            List<ChannelContentTpl> contentTpls = channelContentTplService.findByChannelId(channelDto.getId());
            channelContentTplService.physicalDeleteInBatch(contentTpls);
            List<ChannelContentTpl> newContentTpls = channelContentTplService.save(channelDto.getContentTpls(),
                    bean.getId(), site.getId());
            bean.setContentTpls(newContentTpls);
        }
        // 初始化并转换成channelAttr集合
        List<ChannelAttr> attrs = channelAttrService.init(channelDto.getJson(), bean.getModelId(), bean.getId(),
                channelDto.getGroupType());

        if (attrs.size() > 0) {
            attrs = channelAttrService.splicDefValue(bean.getModelId(), attrs, true, null);
            GlobalConfig globalConfig = SystemContextUtils.getGlobalConfig(RequestUtils.getHttpServletRequest());
            attrs = channelAttrService.splic(attrs, bean.getId(), globalConfig);
            for (ChannelAttr channelAttr : attrs) {
                ChannelAttr attrBean = channelAttrService.save(channelAttr);
                channelAttrService.flush();
                List<ChannelAttrRes> attrRes = channelAttr.getChannelAttrRes();
                if (attrRes != null && attrRes.size() > 0) {
                    for (ChannelAttrRes attrRe : attrRes) {
                        attrRe.setChannelAttr(attrBean);
                        attrRe.setChannelAttrId(attrBean.getChannelAttrId());
                    }
                    channelAttrResService.saveAll(attrRes);
                    channelAttrResService.flush();
                }
            }
        }
        this.initConentObject(bean);
        this.initContentExtObject(bean.getChannelExt());
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (ChannelListener listener : channelListeners) {
                    try {
                        listener.afterChannelChange(bean);
                    } catch (GlobalException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void delete(Integer[] ids, boolean isThorough) throws GlobalException {
        List<Channel> channelList = super.findAllById(Arrays.asList(ids));
        if (CollectionUtils.isEmpty(channelList)) {
            return;
        }
        CmsSite site = null;
        for (Channel c : channelList) {
            /** 主动维护站点栏目集合，方便权限根据站点取栏目数据 */
            site = c.getSite();
            site.getChannels().remove(c);
            if (c.getParent() != null) {
                c.getParent().getChild().remove(c);
            }
            cmsSiteService.update(site);
        }
        cmsSiteService.flush();
        /** 获取到所有节点，此处需要考虑一种情况，比如某个父级栏目有删除权限，但是因为某些原因，子集没有删除权限，所以此处查询所有，强制性删除 */
        List<Channel> channels = dao.findBySiteIdAndHasDeleted(site.getId(), false);
        List<Channel> newChannels = new ArrayList<Channel>();
        for (Channel channel : channelList) {
            List<Channel> snapChannels = new ArrayList<Channel>();
            snapChannels = channels.stream().filter(c -> c.getLft() > channel.getLft() && c.getRgt() < channel.getRgt())
                    .collect(Collectors.toList());
            if (snapChannels.size() > 0) {
                newChannels.addAll(snapChannels);
            }
        }
        channelList.addAll(newChannels);
        channelList = channelList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Channel::getId))), ArrayList::new));
        // isThorough为true说明是"彻底删除"，所以将其全部进行逻辑删除
        if (isThorough) {
            // 此处做一层校验，传入的Id的对象，必须是已经加入了回收站的对象，否则此对象不进行任何处理
            channelList = channelList.stream().filter(a -> a.getRecycle().equals(true)).collect(Collectors.toList());
            if (channelList.size() > 0) {
                super.delete(channelList);
                super.flush();
                List<Integer> channelIds = channelList.stream().map(Channel::getId).collect(Collectors.toList());
                ThreadPoolService.getInstance().execute(() -> {
                    try {
                        for (ChannelListener listener : channelListeners) {
                            listener.beforeChannelDelete(channelIds.toArray(new Integer[channelIds.size()]));
                        }
                    } catch (GlobalException e) {
                        LOGGER.error(e.getMessage());
                    }
                });
            }
        } else {
            // 此处做一层校验，此处需要过滤过已经被加入到回收站的子集，否则将进行重复修改操作
            channelList = channelList.stream().filter(a -> a.getRecycle().equals(false)).collect(Collectors.toList());
            for (Channel c : channelList) {
                c.setRecycle(true);
                c.setRecycleTime(new Date());
            }
            if (channelList.size() > 0) {
                super.batchUpdate(channelList);
                // 加入回收站后执行监听器方法，将关联的数据再监听器的执行中全部删除
                List<Integer> channelIds = channelList.stream().map(Channel::getId).collect(Collectors.toList());
                List<Content> contents = contentService
                        .findByChannels(channelIds.toArray(new Integer[channelIds.size()]));
                if (contents != null && contents.size() > 0) {
                    List<Integer> contentIds = contents.stream().filter(
                            content -> content.getStatus() == ContentConstant.STATUS_WAIT_PUBLISH || content.getStatus() == ContentConstant.STATUS_FLOWABLE)
                            .map(Content::getId).collect(Collectors.toList());
                    if (contentIds.size() > 0) {
                        flowService.doInterruptDataFlow(ContentConstant.WORKFLOW_DATA_TYPE_CONTENT, contentIds,
                                SystemContextUtils.getCoreUser());
                    }
                }
            }
            final List<Channel> ccs = channelList;
            ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (ChannelListener listener : channelListeners) {
                            listener.afterChannelRecycle(ccs);
                        }
                    } catch (GlobalException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            });
        }
        final List<Channel> ccs = channelList;
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Channel c : ccs) {
                        for (ChannelListener listener : channelListeners) {
                            listener.afterChannelChange(c);
                        }
                    }
                } catch (GlobalException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        });
    }

    @Override
    public void reduction(ChannelReductionDto dto) throws GlobalException {
        List<Integer> ids = dto.getIds();
        List<Channel> channelList = super.findAllById(ids);
        // 此处做一层校验，能够还原的List集合一定是已经加入回收站、未被删除的
        channelList = channelList.stream().filter(a -> a.getRecycle() && !a.getHasDeleted())
                .collect(Collectors.toList());
        List<Integer> recycleIds = new ArrayList<Integer>();
        List<Integer> notRecycleIds = new ArrayList<Integer>();

        Map<Integer, Integer> sortNumMap = new LinkedHashMap<Integer, Integer>();
        Integer sortNum = null;
        for (Channel channel : channelList) {
            // 没有加入回收站抛出异常
            if (!channel.getRecycle()) {
                throw new GlobalException(new ChannelExceptionInfo(
                        ChannelErrorCodeEnum.THE_CHANNEL_NOT_ADDED_TO_THE_RECYCLE_BIN.getDefaultMessage(),
                        ChannelErrorCodeEnum.THE_CHANNEL_NOT_ADDED_TO_THE_RECYCLE_BIN.getCode()));
            }
            Channel parent = channel.getParent();
            if (parent != null) {
                if (parent.getRecycle()) {
                    recycleIds.add(parent.getId());

                } else {
                    notRecycleIds.add(parent.getId());

                    sortNum = sortNumMap.get(parent.getId());
                    if (sortNum != null) {
                        channel.setSortNum(sortNum);
                    } else {
                        sortNum = dao.findBySortNum(channel.getSiteId(), channel.getParentId()) + 2;
                        channel.setSortNum(sortNum);
                    }
                    sortNumMap.put(parent.getId(), sortNum + 2);

                }

                // 父类已经在回收站中但是又没有传入，这就是勾选子集不勾选父集，直接报错
                if (parent.getRecycle() && !ids.contains(parent.getId())) {
                    throw new GlobalException(new ChannelExceptionInfo(
                            ChannelErrorCodeEnum.SELECT_A_SUBSET_MUST_SELECT_A_PARENT.getDefaultMessage(),
                            ChannelErrorCodeEnum.SELECT_A_SUBSET_MUST_SELECT_A_PARENT.getCode()));
                }

            } else {
                sortNum = sortNumMap.get(0);

                if (sortNum != null) {
                    channel.setSortNum(sortNum);
                } else {
                    sortNum = dao.findBySortNum(channel.getSiteId(), channel.getParentId()) + 2;
                    channel.setSortNum(sortNum);
                }

                sortNumMap.put(0, sortNum + 2);
            }
            channel.setRecycle(false);
        }
        // 如果同时还原内容，此处需要校验
        boolean returnStatus = false;
        returnStatus = this.reductionIsException(recycleIds, dto.getIsContent(), true);
        if (returnStatus) {
            throw new GlobalException(
                    new ChannelExceptionInfo(ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT.getDefaultMessage(),
                            ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT.getCode()));
        }
        returnStatus = this.reductionIsException(notRecycleIds, dto.getIsContent(), false);
        if (returnStatus) {
            throw new GlobalException(
                    new ChannelExceptionInfo(ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT.getDefaultMessage(),
                            ChannelErrorCodeEnum.PARENT_CLASS_HAS_CONTENT.getCode()));
        }
        if (dto.getIsContent()) {
            List<Content> contents = contentService.findByChannels(ids.toArray(new Integer[ids.size()]));
            if (contents != null && contents.size() > 0) {
                List<Integer> contentIds = contents.stream().map(Content::getId).collect(Collectors.toList());
                contentService.restore(contentIds, dto.getSiteId(), ids);
            }
        }
        clearPermCache();
        super.batchUpdate(channelList);
        super.flush();
    }

    private boolean reductionIsException(List<Integer> channelIds, boolean isContent, boolean isRecycle) {
        /**
         * 1. 传进来的channelId为父类id集合 2. 如果是回收站的父类id集合则会排除不想还原的父类id集合
         */
        boolean returnStatus = false;
        if (channelIds.size() > 0) {
            Integer[] ids = channelIds.toArray(new Integer[channelIds.size()]);
            if (isContent) {
                /**
                 * 此处为还原栏目和对应的内容 1. 如果传入的id是回收站的id集合，那么判断看是否存在数据，存在则直接应该抛出异常 2.
                 * 如果传入的id不是回收站的id集合(未被加入回收站的栏目)，那么判断看是否存在回收站的栏目中未被删除的则应该抛出异常
                 */
                if (isRecycle) {
                    long count = contentService.countByChannelIdInAndRecycle(ids, true);
                    count += contentService.countByChannelIdInAndRecycle(ids, false);
                    if (count > 0) {
                        returnStatus = true;
                    }
                } else {
                    long count = contentService.countByChannelIdInAndRecycle(ids, false);
                    if (count > 0) {
                        returnStatus = true;
                    }
                }
            } else {
                /**
                 * 此处为仅还原栏目 1.
                 * 如果传入的channelIds是回收站的栏目id集合，那么判断看是否存在回收站的栏目中未被删除的则应该抛出异常->正常逻辑不可能出现此种情况只是为了排除极端情况
                 * 2. 如果传入的cahnenlIds不是回收站的栏目id集合(未被加入回收站的栏目)，那么判断看是否存在回收站的栏目中未被删除的则应该抛出异常
                 */
                long count = contentService.countByChannelIdInAndRecycle(ids, false);
                if (count > 0) {
                    returnStatus = true;
                }
            }
        }
        return returnStatus;
    }

    @Override
    public void setOpenIndex(ChannelSetIndexDto channelOpenSet) throws GlobalException {
        for (ChannelOpenSet c : channelOpenSet.getChannelOpens()) {
            Channel channel = findById(c.getChannelId());
            channel.getChannelExt().setIsOpenIndex(c.getOpen());
            update(channel);
        }
    }

    @Override
    public void claimWorkflow(ChannelWorkflowDto dto) throws GlobalException {
        List<Channel> channels = null;
        // 如果all为true说明是勾选了全部，则在controller已经处理了channels
        if (dto.getAll()) {
            channels = dto.getChannels();
        } else {
            if (dto.getIds() != null && dto.getIds().size() > 0) {
                channels = super.findAllById(dto.getIds());
            }
        }
        List<Integer> channelIds = new ArrayList<Integer>();
        // 如果channels为一个有值的数组则进行修改操作
        if (channels != null && channels.size() > 0) {
            // 过滤处理掉栏目模型中不存在工作流字段的栏目
            channels = channels.stream().filter(channel -> channel.getWorkflowField()).collect(Collectors.toList());
            Integer workflow = super.findById(dto.getChannelId()).getWorkflowId();
            for (Channel channel : channels) {
                channel.setWorkflowId(workflow);
                if (channel.getRealWorkflowId() != null) {
                    if (channel.getRealWorkflowId().equals(workflow)) {
                        channelIds.add(channel.getId());
                    }
                }
            }
            super.batchUpdateAll(channels);
        }
        if (channelIds.size() > 0) {
            List<Content> contents = contentService.findByChannels(channelIds.toArray(new Integer[channels.size()]));
            if (contents != null && contents.size() > 0) {
                List<Integer> contentIds = contents.stream().filter(
                        content -> content.getStatus() == ContentConstant.STATUS_WAIT_PUBLISH || content.getStatus() == ContentConstant.STATUS_FLOWABLE)
                        .map(Content::getId).collect(Collectors.toList());
                if (contentIds.size() > 0) {
                    flowService.doInterruptDataFlow(ContentConstant.WORKFLOW_DATA_TYPE_CONTENT, contentIds,
                            SystemContextUtils.getCoreUser());
                }
            }

        }
    }

    @Override
    public void channelSort(ChannelSortDto dto, List<Channel> channelList) throws GlobalException {
        /**
         * 如果是isMove为true说明是移动，则是修改 dto.getIds的[0]->需要移动/修改的值，[1]->移动的位置或者修改的位置
         */
        /**
         * 当dto.getIds的长度只有1的时候，则说明将此栏目移动到统一序列的栏目的最上层
         */
        Integer parentId = super.findById(dto.getChannelId()).getParentId();
        if (parentId != null) {
            channelList = channelList.stream()
                    .filter(channel -> !channel.getHasDeleted() && !channel.getRecycle()
                            && parentId.equals(channel.getParentId()))
                    .sorted(Comparator.comparing(Channel::getSortNum)
                            .thenComparing(Comparator.comparing(Channel::getCreateTime)))
                    .collect(Collectors.toList());
        } else {
            channelList = channelList.stream().filter(
                    channel -> !channel.getHasDeleted() && !channel.getRecycle() && channel.getParentId() == null)
                    .sorted(Comparator.comparing(Channel::getSortNum)
                            .thenComparing(Comparator.comparing(Channel::getCreateTime)))
                    .collect(Collectors.toList());
        }
        // 最大排序值
        int i = 0;
        // 需要移动的对象的排序值
        int newSort = 0;
        // 需要移动的对象在list集合中的位置
        int index = 0;
        for (int j = 0; j < channelList.size(); j++) {
            Channel channel = channelList.get(j);
            channel.setSortNum(i);
            // 如果传入了移动到的位置，并且当前遍历的是栏目需要修改的值，那么将newSort最为此栏目+1刚好在需要修改的这个栏目的下方
            if (dto.getOnId() != null && channel.getId().intValue() == dto.getOnId()) {
                newSort = channel.getSortNum() + 1;
            }
            if (dto.getNextId() != null && channel.getId().intValue() == dto.getNextId()) {
                newSort = channel.getSortNum() - 1;
            }
            // 取出需要移动的值在list集合中的位置
            if (channel.getId().intValue() == dto.getChannelId()) {
                index = j;
            }
            i += 2;
        }
        channelList.get(index).setSortNum(newSort);
        super.batchUpdate(channelList);

    }

    @Override
    public void mergeChannel(Channel channel, Integer[] ids) throws GlobalException {
        List<Integer> channelIds = Arrays.asList(ids);
        List<Channel> channels = super.findAllById(channelIds);
        // 处理内容主表
        List<Content> contents = contentService.findByChannels(ids);
        if (contents != null && contents.size() > 0) {
            for (Content content : contents) {
                content.setChannelId(channel.getId());
            }
            contentService.batchUpdateAll(contents);
        }
        super.delete(channels);
        super.flush();
        clearPermCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<ChannelModelTplVo> findModelTplVo(Integer siteId, Integer channelId) throws GlobalException {
        List<SiteModelTpl> tpls = siteModelTplService.findBySiteId(siteId);
        Map<Integer, List<SiteModelTpl>> tplMap = tpls.stream()
                .collect(Collectors.groupingBy(SiteModelTpl::getModelId));
        List<CmsModel> cmsModels = cmsModelService.findList(CmsModel.CONTENT_TYPE, siteId);
        CmsSite site = SystemContextUtils.getSite(RequestUtils.getHttpServletRequest());
        CmsSiteConfig siteConfig = site.getCmsSiteCfg();
        // 通过站点配置的模板方案名查询出该站点下该模型配置的PC模板路径
        List<CmsModelTpl> pcTpls = cmsModelTplService.models(site.getId(), null, siteConfig.getPcSolution());
        Map<Integer, List<CmsModelTpl>> pcTplMap = new LinkedHashMap<Integer, List<CmsModelTpl>>();
        Map<Integer, List<String>> pcTplNameMap = new LinkedHashMap<Integer, List<String>>();
        if (pcTpls != null && pcTpls.size() > 0) {
            pcTplMap = pcTpls.stream().collect(Collectors.groupingBy(CmsModelTpl::getModelId));
            for (Integer i : pcTplMap.keySet()) {
                pcTplNameMap.put(i, pcTplMap.get(i).stream().map(CmsModelTpl::getTplPath).collect(Collectors.toList()));
            }
        }
        // 通过站点配置的模板方案名查询出该站点下该模型配置的手机模板路径
        List<CmsModelTpl> modelTpls = cmsModelTplService.models(site.getId(), null, siteConfig.getMobileSolution());
        Map<Integer, List<CmsModelTpl>> modelTplMap = new HashMap<>(16);
        Map<Integer, List<String>> modelTplNameMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(modelTpls)) {
            modelTplMap = modelTpls.stream().collect(Collectors.groupingBy(CmsModelTpl::getModelId));
            for (Integer i : modelTplMap.keySet()) {
                modelTplNameMap.put(i,
                        modelTplMap.get(i).stream().map(CmsModelTpl::getTplPath).collect(Collectors.toList()));
            }
        }
        Map<Integer, String> tplPcMap = new HashMap<>(16);
        Map<Integer, String> tplModelMap = new HashMap<>(16);
        if (channelId != null) {
            List<ChannelContentTpl> tplList = channelContentTplService.findByChannelId(channelId);
            if (!CollectionUtils.isEmpty(tplList)) {
                for (ChannelContentTpl channelContentTpl : tplList) {
                    tplPcMap.put(channelContentTpl.getModelId(), channelContentTpl.getTplPc());
                    tplModelMap.put(channelContentTpl.getModelId(), channelContentTpl.getTplMobile());
                }
            }
        }
        List<ChannelModelTplVo> vos = new ArrayList<>();
        for (CmsModel cmsModel : cmsModels) {
            Integer cmsModelId = cmsModel.getId();
            ChannelModelTplVo vo = new ChannelModelTplVo(cmsModel.getModelName(), cmsModelId);
            String pcTplPath = "";
            if (tplMap.get(cmsModelId) != null) {
                if (tplPcMap.get(cmsModelId) != null) {
                    pcTplPath = tplPcMap.get(cmsModelId);
                } else {
                    pcTplPath = tplMap.get(cmsModelId).get(0).getPcTplPath();
                }
            }
            vo.setTplPc(pcTplPath);
            String mobileTplPath = "";
            if (tplMap.get(cmsModelId) != null) {
                if (tplModelMap.get(cmsModelId) != null) {
                    mobileTplPath = tplModelMap.get(cmsModelId);
                } else {
                    mobileTplPath = tplMap.get(cmsModelId).get(0).getMobileTplPath();
                }
            }
            vo.setTplMobile(mobileTplPath);

            vo.setMobileTplPaths(modelTplNameMap.get(cmsModelId) != null ? modelTplNameMap.get(cmsModelId)
                    : new ArrayList<String>());
            vo.setPcTplPaths(
                    pcTplNameMap.get(cmsModelId) != null ? pcTplNameMap.get(cmsModelId) : new ArrayList<String>());
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Channel get(Integer id, Integer siteId, String path) {
        Channel channel;
        if (id != null) {
            channel = super.findById(id);
        } else {
            channel = findByPath(path, siteId);
        }
        return channel;
    }

    @Override
    public Channel getByAttr(Integer parentId, String attrName, String attrVal) {
        Channel c = findById(parentId);
        if(c!=null){
            List<Channel> list = dao.findByAttr(c.getLft(),c.getRgt(),attrName,attrVal);
            if(!list.isEmpty()){
                return  list.get(0);
            }
        }
        return null;
    }

    @Override
    public void deleteWorkflow(Integer modelId) throws GlobalException {
        // 查询出使用了改模型的未删除的栏目集合
        List<Channel> channels = dao.findByModelIdAndHasDeleted(modelId, false);
        if (!CollectionUtils.isEmpty(channels)) {
            // 查询出之前已经设置了工作流并且未加入回收站的栏目Id集合
            List<Integer> channelIds = channels.stream()
                    .filter(channel -> !channel.getRecycle() && channel.getWorkflowId() != null).map(Channel::getId)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(channelIds)) {
                // 查询出栏目集合的内容集合(未加入回收站、未删除)
                List<Content> contents = contentService
                        .findByChannels(channelIds.toArray(new Integer[channels.size()]));
                if (!CollectionUtils.isEmpty(contents)) {
                    List<Integer> contentIds = contents.stream().filter(
                            content -> content.getStatus() == ContentConstant.STATUS_WAIT_PUBLISH || content.getStatus() == ContentConstant.STATUS_FLOWABLE)
                            .map(Content::getId).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(contentIds)) {
                        flowService.doInterruptDataFlow(ContentConstant.WORKFLOW_DATA_TYPE_CONTENT, contentIds,
                                SystemContextUtils.getCoreUser());
                    }
                }
            }
            if (!CollectionUtils.isEmpty(channels)) {
                for (Channel channel : channels) {
                    channel.setWorkflowId(null);
                }
                super.batchUpdate(channels);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> getChanelByRecycle(Integer siteId, Boolean recycle) {
        return dao.findList(siteId, null, null, null, null, null, null, recycle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Integer> getContentByModel(Integer modelId) {
        List<Channel> channels = dao.findByModelIdAndHasDeleted(modelId, false);
        List<Integer> ids = new ArrayList<>();
        if (!CollectionUtils.isEmpty(channels)) {
            ids = channels.stream().filter(channel -> !channel.getRecycle()).map(Channel::getId)
                    .collect(Collectors.toList());
        }
        List<Integer> contentIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ids)) {
            List<Content> contents = contentService.findByChannels(ids.toArray(new Integer[0]));
            if (!CollectionUtils.isEmpty(contents)) {
                contentIds = contents.stream().filter(
                        content -> content.getStatus() == ContentConstant.STATUS_WAIT_PUBLISH || content.getStatus() == ContentConstant.STATUS_FLOWABLE)
                        .map(Content::getId).collect(Collectors.toList());
            }
        }
        return contentIds;
    }

    private void clearPermCache() {
        userService.clearAllUserCache();
        orgService.clearAllOrgCache();
        roleService.clearAllRoleCache();
    }

    /**
     * 新增修改初始化栏目对象的关联的对象
     */
    private void initConentObject(Channel channel) throws GlobalException {
        if (channel.getSiteId() != null) {
            channel.setSite(cmsSiteService.findById(channel.getSiteId()));
        }
        if (channel.getModelId() != null) {
            channel.setModel(cmsModelService.findById(channel.getModelId()));
        }
        List<ChannelContentTpl> contentTpls = channelContentTplService.findByChannelId(channel.getId());
        if (!CollectionUtils.isEmpty(contentTpls)) {
            channel.setContentTpls(contentTpls);
        } else {
            channel.setContentTpls(null);
        }
        List<ChannelTxt> txts = channelTxtService.findByChannelId(channel.getId());
        if (!CollectionUtils.isEmpty(txts)) {
            channel.setTxts(txts);
        } else {
            channel.setTxts(null);
        }
    }

    /**
     * 新增、修改初始化栏目扩展对象的关联的对象
     */
    private void initContentExtObject(ChannelExt channelExt) {
        if (channelExt.getResourceId() != null) {
            channelExt.setResourcesSpaceData(resourcesSpaceDataService.findById(channelExt.getResourceId()));
        } else {
            channelExt.setResourcesSpaceData(null);
        }
    }

    @Override
    public void beforeWorkflowDelete(Integer[] ids) throws GlobalException {
        channelDtoService.workflowDelete(ids, SystemContextUtils.getCoreUser());
    }

    private Channel updateSortNum(Integer parentId, Channel channel) {
        Integer sortNum = dao.findBySortNum(channel.getSiteId(), parentId) + 2;
        channel.setSortNum(sortNum);
        return channel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<Channel> findByIds(Collection<Integer> ids) {
        return dao.findByIds(ids);
    }

    @Override
    public JSONArray getChannelsByUserChannelPerm(CoreUser user, Integer siteId, List<Channel> childs) {
        JSONArray result = new JSONArray();
        // 没有栏目或者用户为空则直接返回空
        if (CollectionUtils.isEmpty(childs) || user == null) {
            return result;
        }
        JSONArray dataSource = new JSONArray();
        JSONObject jsonObject = null;
        Map<Integer, JSONObject> hashDatas = new HashMap<>(childs.size());
        List<Integer> editChannelIds = user.getEditChannelIds();
        List<Integer> delChannelIds = user.getDelChannelIds();
        List<Integer> createChannelIds = user.getCreateChannelIds();
        List<Integer> permAssignChannelIds = user.getPermAssignChannelIds();
        List<Integer> mergeChannelIds = user.getMergeChannelIds();
        List<Integer> staticChannelIds = user.getStaticChannelIds();
        List<Integer> viewChannelIds = user.getViewChannelIds();
        for (Channel t : childs) {
            jsonObject = new JSONObject();
            jsonObject.put("id", t.getId());
            jsonObject.put("editAble", false);
            jsonObject.put("createChildAble", false);
            jsonObject.put("permAssignAble", false);
            jsonObject.put("mergeAble", false);
            jsonObject.put("staticAble", false);
            jsonObject.put("viewAble", false);
            jsonObject.put("deleteAble", false);
            if (editChannelIds.contains(t.getId())) {
                jsonObject.put("editAble", true);
            }
            if (createChannelIds.contains(t.getId())) {
                jsonObject.put("createChildAble", true);
            }
            if (permAssignChannelIds.contains(t.getId())) {
                jsonObject.put("permAssignAble", true);
            }
            if (mergeChannelIds.contains(t.getId())) {
                jsonObject.put("mergeAble", true);
            }
            if (staticChannelIds.contains(t.getId())) {
                jsonObject.put("staticAble", true);
            }
            if (viewChannelIds.contains(t.getId())) {
                jsonObject.put("viewAble", true);
            }
            if (delChannelIds.contains(t.getId())) {
                jsonObject.put("deleteAble", true);
            }
            jsonObject.put("sortNum", t.getSortNum());
            jsonObject.put("name", t.getName());
            jsonObject.put("parentId", t.getParentId());
            // 没有子节点则过滤childs
            long count = childs.stream().filter(
                    c -> null != c.getParentId() && ((Integer) t.getId()).intValue()
                            == c.getParentId().intValue()).count();
            if (count > 0) {
                jsonObject.put("children", new ArrayList<>());
            }
            dataSource.add(jsonObject);
            hashDatas.put((Integer) jsonObject.get("id"), jsonObject);
        }
        childs.clear();

        // 遍历菜单集合
        for (int i = 0; i < dataSource.size(); i++) {
            // 当前节点
            JSONObject json = (JSONObject) dataSource.get(i);
            // 当前的父节点
            JSONObject parent = hashDatas.get(json.get("parentId"));
            if (parent != null) {
                // 表示当前节点为子节点
                ((List<JSONObject>) parent.get("children")).add(json);
            } else {
                // parentId为null和获取匹配parentId的节点(生成某节点的子节点树时需要用到)
                result.add(json);
            }
        }
        return result;
    }

    @Override
    public long getSum(Integer siteId, Boolean recycle) {
        return dao.getSum(siteId, recycle);
    }

    @Override
    public List<Channel> getListForDelete(Integer siteId) {
        return dao.findBySiteIdAndHasDeleted(siteId, true);
    }

    @Override
    public List<Integer> findByParentIds(List<Integer> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<Integer> channelIds = dao.findByParentIds(ids);
            ids.addAll(channelIds);
        }
        return ids;
    }

    @Override
    public Channel findByIdAndRecycleAndHasDeleted(Integer id) {
        return dao.findByIdAndRecycleAndHasDeleted(id, false, false);
    }

    @Override
    public List<Channel> getList(Integer siteId, Boolean recycle) {
        return dao.findList(siteId, recycle);
    }

    /**
     * 获取所有底层栏目id
     * @param siteId            站点id
     * @param containNotDisplay false则不包含不显示的栏目 true包含不显示的栏目
     * @return
     */
    @Override
    public List<Integer> getChildChannelList(Integer siteId, boolean containNotDisplay) {
        List<Channel> channelList = dao.findList(siteId,false);
        //所有的id
        Set<Integer> idList = channelList.stream().map(m->m.getId()).collect(Collectors.toSet());
        //所有的父节点id
        Set<Integer> parentIdList = channelList.stream().map(m->m.getParentId()).collect(Collectors.toSet());
        idList.removeAll(parentIdList);
        return idList.stream().collect(Collectors.toList());
    }

    @Override
    public List<Channel> findTopList(Integer siteId) {
        return super.dao.findList(siteId, null, 0, false, null,
                new PaginableRequest(0, Integer.MAX_VALUE), null, null);
    }

}