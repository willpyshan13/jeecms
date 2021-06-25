/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.content;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.UserModelRecord;
import com.jeecms.auth.service.UserModelRecordService;
import com.jeecms.auth.service.UserModelSortService;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.*;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.CmsModelTpl;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentAttrRes;
import com.jeecms.content.domain.dto.*;
import com.jeecms.content.domain.vo.ContentButtonVo;
import com.jeecms.content.domain.vo.ResetSecretVo;
import com.jeecms.content.service.*;
import com.jeecms.content.service.impl.ContentDocServiceImpl;
import com.jeecms.publish.service.StatisticsContentDataService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.*;
import com.jeecms.system.service.*;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import static com.jeecms.publish.constants.PublishConstant.SORT_TYPE_1;

/**
 * 内容controller层
 *
 * @author: chenming
 * @date: 2019年5月16日 上午8:49:33
 */
@RequestMapping("/content")
@RestController
@Validated
public class ContentController extends BaseController<Content, Integer> {
    static Logger logger = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    private ContentService service;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private CmsModelTplService cmsModelTplService;
    @Autowired
    private SysSecretService secretService;
    @Autowired
    private ContentSourceService contentSourceService;
    @Autowired
    private ContentMarkService contentMarkService;
    @Autowired
    private ContentLuceneService luceneService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CmsSiteService cmsSiteService;
    @Autowired
    private UserModelRecordService userModelRecordService;
    @Autowired
    private ContentDocServiceImpl docServiceImpl;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;
    @Autowired
    private UserModelSortService userModelSortService;
    @Autowired
    private ContentUpdateCheckService contentUpdateCheckService;
    @Autowired
    private StatisticsContentDataService statisticsContentDataService;
    @Autowired
    private CacheProvider cacheProvider;
    @Autowired
    private GlobalConfigService globalConfigService;

    @PostConstruct
    public void init() {
        String[] queryParams = {};
        super.setQueryParams(queryParams);
    }

    /**
     * 模型字段中有一个附件上传，附件上传的密级是否显示应当通过全局配置获取
     */

    /**
     * 通过模型内容id获取到其相应的字段
     */
    @MoreSerializeField({@SerializeField(clazz = CmsModel.class, includes = {"enableJson"})})
    @RequestMapping(value = "/plus/{modelId}", method = RequestMethod.GET)
    public ResponseInfo getSave(@PathVariable(name = "modelId") Integer modelId, HttpServletRequest request)
            throws GlobalException {
        CmsModel model = cmsModelService.getChannelOrContentModel(modelId);
        if (CmsModel.CONTENT_TYPE.equals(model.getTplType())) {
            return new ResponseInfo(model);
        }
        return new ResponseInfo();
    }

    /**
     * 通用的校验栏目的方法
     */
    private ResponseInfo checkChannel(Channel channel) {
        if (channel == null) {
            return new ResponseInfo(SettingErrorCodeEnum.CHANNEL_IS_NOT_NULL.getCode(),
                    SettingErrorCodeEnum.CHANNEL_IS_NOT_NULL.getDefaultMessage(), false);
        }
        if (!channel.getIsBottom()) {
            return new ResponseInfo(SettingErrorCodeEnum.NOT_THE_BOTTOM_CHANNEL.getCode(),
                    SettingErrorCodeEnum.NOT_THE_BOTTOM_CHANNEL.getDefaultMessage(), false);
        }
        return new ResponseInfo();
    }

    /**
     * 通用的校验内容标题的方法
     */
    private boolean checkTitle(HttpServletRequest request, String title, Integer channelId, boolean isReturn)
            throws GlobalException {
        Integer titleRepeat = SystemContextUtils.getSite(request).getConfig().getTitleRepeat();
        boolean titleStatus = true;
        // 站点内不允许重复
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        if (titleRepeat == 2) {
            if (service.checkTitle(title, null,siteId)) {
                if (isReturn) {
                    titleStatus = false;
                } else {
                    throw new GlobalException(
                            ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT);
                }
            }
        }
        // 同一栏目内不允许重复
        if (titleRepeat == 3) {
            if (service.checkTitle(title, channelId, siteId)) {
                if (isReturn) {
                    titleStatus = false;
                } else {
                    throw new GlobalException(
                            ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT);
                }
            }
        }
        return titleStatus;
    }

    /**
     * 校验栏目是否重复
     */
    @RequestMapping(value = "/title/unique", method = RequestMethod.GET)
    public ResponseInfo checkTitle(HttpServletRequest request, @RequestParam String title,
                                   @RequestParam Integer channelId, @RequestParam(required = false) Integer contentId)
            throws GlobalException {
        if (contentId != null) {
            Content content = service.findById(contentId);
            if (title.equals(content.getTitle())) {
                return new ResponseInfo(true);
            }
        }
        return new ResponseInfo(this.checkTitle(request, title, channelId, true));
    }

    final transient ReentrantLock lock = new ReentrantLock();
	/**
	 * 新增
	 */
	@RequestMapping(method = RequestMethod.POST)
    public ResponseInfo save(@RequestBody @Validated ContentSaveDto dto, HttpServletRequest request,
                             BindingResult result) throws GlobalException {
	    long t1 = System.currentTimeMillis();
        super.validateBindingResult(result);
        String title = dto.getTitle().getString(Content.TITLE_NAME);
        Content content = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.checkTitle(request, title, dto.getChannelId(), false);
            Integer userId = SystemContextUtils.getUserId(request);
            dto.setUserId(userId);
            dto.setPublishUserId(userId);
            Channel channel = channelService.findById(dto.getChannelId());
            this.checkChannel(channel);
            if (!channel.getCreateContentAble()) {
                return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
            }
            if (dto.getType() == ContentConstant.STATUS_PUBLISH) {
                if (!channel.getPublishContentAble()) {
                    return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                            UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
                }
                // 当前时间
                Long currentTime = System.currentTimeMillis();
                // 发布时间
                Long releaseTime = MyDateUtils.parseDate(
                        dto.getReleaseTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN).getTime();
                if (StringUtils.isNotBlank(dto.getOfflineTime())) {
                    // 下线时间
                    Long offlineTime = MyDateUtils.parseDate(
                            dto.getOfflineTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN)
                            .getTime();
                    // 如果下线时间小于当前时间并且下线时间小于发布时间抛出异常
                    if (offlineTime < currentTime && offlineTime < releaseTime) {
                        return new ResponseInfo(
                                ContentErrorCodeEnum.CONTENT_CANNOT_BE_PUBLISHED.getCode(),
                                ContentErrorCodeEnum.CONTENT_CANNOT_BE_PUBLISHED
                                        .getDefaultMessage());
                    }
                }
            }
            content = service.save(dto, SystemContextUtils.getSite(request));
        } finally {
            lock.unlock();
        }

        final Content c = content;
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    c.setSortNum(service.findMaxSortNum() + 1);
                    service.update(c);
                }catch (GlobalException e){
                    logger.error(e.getMessage());
                }
            }
        });
        return new ResponseInfo(content.getStatus());
    }

    /**
     * 提交审核
     */
    @PostMapping("/submit")
    public ResponseInfo submit(@RequestBody @Valid ContentUpdateDto dto, BindingResult result,
                               HttpServletRequest request) throws GlobalException {
        super.validateBindingResult(result);
        Channel channel = channelService.findById(dto.getChannelId());
        if (dto.getId() != null) {
            Content content = service.findById(dto.getId());
            if (!dto.getTitle().getString(Content.TITLE_NAME).equals(content.getTitle())) {
                this.checkTitle(request, dto.getTitle().getString(Content.TITLE_NAME),
                        dto.getChannelId(), false);
            }
        } else {
            this.checkTitle(request, dto.getTitle().getString(Content.TITLE_NAME),
                    dto.getChannelId(), false);
        }
        this.checkChannel(channel);
        // TODO 校验栏目是否包含工作流，
        // TODO 校验内容的状态只能是流转中
        // id不等于null说明是修改操作，修改操作则需要判断是否存在修改类权限，否则则是新增操作，需要判断其是否存在新增权限
        if (dto.getId() != null) {
            if (!channel.getEditContentAble()) {
                return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
            }
        } else {
            if (!channel.getCreateContentAble()) {
                return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
            }
        }
        service.submit(dto, request, false, null);
        return new ResponseInfo();
    }

    /**
     * 查询所有的内容模型(全局+站点)
     * ps:升级接口，将接口改成勾选了的才显示；兼容之前的接口
     *
     * @param request   请求
     * @param channelId 栏目ID
     * @throws GlobalException 异常
     * @Title: findModelList
     * @since 1.2
     */
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    @MoreSerializeField({@SerializeField(clazz = CmsModel.class, includes = {"id", "modelName"})})
    public ResponseInfo findModelList(HttpServletRequest request,
                                      Integer channelId) throws GlobalException {
        if (channelId != null) {
            CoreUser user = SystemContextUtils.getUser(request);
            try {
                //判断栏目是否有新增权限,有则返回
                user.checkContentDataPerm(channelId, CmsDataPerm.OpeContentEnum.CREATE);
            } catch (GlobalException e) {
                //没权限直接返回空集合
                return new ResponseInfo(new ArrayList<CmsModel>());
            }
            Integer siteId = SystemContextUtils.getSiteId(request);
            List<CmsModel> models = cmsModelService.findList(CmsModel.CONTENT_TYPE, siteId);
            return new ResponseInfo(userModelSortService.sort(models, channelId, user.getId()));
        } else {
            Integer siteId = SystemContextUtils.getSiteId(request);
            return new ResponseInfo(cmsModelService.findList(CmsModel.CONTENT_TYPE, siteId));
        }

    }

    /**
     * 查询所有的PC端模板、手机模板(模型之下)
     */
    @RequestMapping(value = "/template/list", method = RequestMethod.GET)
    @MoreSerializeField({@SerializeField(clazz = CmsModelTpl.class, includes = {"tplPath"})})
    public ResponseInfo findByModel(@RequestParam Integer modelId, @RequestParam Integer type,
                                    HttpServletRequest request) throws GlobalException {
        CmsSite site = SystemContextUtils.getSite(request);
        CmsSiteConfig siteConfig = site.getCmsSiteCfg();
        List<CmsModelTpl> cmtList = null;
        // 通过站点配置的模板方案名查询出该站点下该模型配置的PC模板路径
        if (type == 1) {
            cmtList = cmsModelTplService.models(site.getId(), modelId, siteConfig.getPcSolution());
        }
        // 通过站点配置的模板方案名查询出该站点下该模型配置的手机模板路径
        if (type == 2) {
            cmtList = cmsModelTplService.models(site.getId(), modelId, siteConfig.getMobileSolution());
        }
        return new ResponseInfo(cmtList);
    }

    /**
     * 内容、附件密级列表
     */
    @RequestMapping(value = "/secret/list", method = RequestMethod.GET)
    @MoreSerializeField({
            @SerializeField(clazz = SysSecret.class, includes = {"id", "name"})
    })
    public ResponseInfo secretList(
            @Range(min = 1, max = 2, message = "类型只有1或者2") @RequestParam Integer secretType) {
        return new ResponseInfo(secretService.findByType(secretType));
    }

    /**
     * 来源列表(可根据来源名称进行模糊检索)
     */
    @RequestMapping(value = "/source/list", method = RequestMethod.GET)
    @MoreSerializeField({@SerializeField(clazz = ContentSource.class,
            includes = {"sourceName", "sourceLink", "isDefault"})})
    public ResponseInfo sourceList(@RequestParam(required = false) String sourceName) {
        HashMap<String, String[]> params = new HashMap<String, String[]>(1);
        if (!StringUtils.isBlank(sourceName)) {
            params.put("LIKE_sourceName_String", new String[]{sourceName});
        }
        return new ResponseInfo(contentSourceService.getList(params, null, false));
    }

    /**
     * 发文字号列表
     */
    @RequestMapping(value = "/mark/list", method = RequestMethod.GET)
    @MoreSerializeField({@SerializeField(clazz = ContentMark.class, includes = {"markName", "id"})})
    public ResponseInfo markList(@Range(min = 1, max = 2, message = "类型只有1或者2") @RequestParam Integer markType) {
        return new ResponseInfo(contentMarkService.getList(markType));
    }

	/**
	 * 通过模型内容id获取到其相应的字段
	 */
	@MoreSerializeField({
			@SerializeField(clazz = ContentAttrRes.class, includes = { "description", "secret", 
					"resourcesSpaceData" }),
			@SerializeField(clazz = ContentSource.class, includes = { "id", "sourceName", "sourceLink" }),
			@SerializeField(clazz = SysSecret.class, includes = { "id", "name" }),
			@SerializeField(clazz = ResourcesSpaceData.class, includes = { "id", "resourceType", 
					"alias", "suffix", "url", "videoCover", "fileUrl" }),
			@SerializeField(clazz = ContentType.class, includes = { "id", "typeName", "logoResource" }) })
	@RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
	public ResponseInfo get(@PathVariable(name = "id") Integer id, HttpServletRequest request) 
			throws GlobalException {
		Content content = service.findById(id);
		if (content == null) {
			return new ResponseInfo(null);
		}
		if (!content.getViewContentAble()) {
			return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
					UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
		}
        //如果是引用内容，直接读取源内容数据
        if(content.getOriContentId() != null){
            id = content.getOriContentId();
        }
		GlobalConfig globalConfig = SystemContextUtils.getGlobalConfig(request);
		return new ResponseInfo(service.findContent(id, globalConfig));
	}

    /**
     * 修改
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseInfo update(@RequestBody @Valid ContentUpdateDto dto, HttpServletRequest request,
                               BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        Content bean = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Content content = service.findById(dto.getId());
            if (dto.getType() == null) {
                return new ResponseInfo(SystemExceptionEnum.INCOMPLETE_PARAM.getCode(),
                        SystemExceptionEnum.INCOMPLETE_PARAM.getDefaultMessage(), false);
            }
            if (dto.getType() == ContentConstant.STATUS_PUBLISH) {
                if (!content.getPublishContentAble()) {
                    return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                            UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(),
                            false);
                }
            } else {
                if (!content.getEditContentAble()) {
                    return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                            UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
                }
            }
            Integer userId = SystemContextUtils.getUserId(request);
            dto.setModelId(null);
            dto.setUserId(userId);
            dto.setPublishUserId(userId);
            dto.setUser(SystemContextUtils.getUser(request));
            contentUpdateCheckService.checkUpdate(dto);
            bean = service.update(dto, request);
            return new ResponseInfo(bean.getStatus());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 复制内容
     */
    @RequestMapping(value = "/duplication", method = RequestMethod.POST)
    public ResponseInfo copy(@RequestBody @Validated ContentCopyDto dto, HttpServletRequest request)
            throws GlobalException {
        Channel channel = channelService.findById(dto.getChannelId());
        this.checkChannel(channel);
        if (!channel.getCreateContentAble()) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        /*
         * 前台选择传入的内容一定是在同一个栏目之下的，本质而言权限是由栏目控制的，既然栏目一样，那么保证一个内容(栏目)有权限那么其它内容一定有权限
         */
        Content content = service.findById(dto.getIds().get(0));
        if (!content.getCopyContentAble()) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        CmsSiteConfig siteConfig = SystemContextUtils.getSite(request).getCmsSiteCfg();
        /* 从站点中取出默认设置 */
        if (siteConfig.getTitleRepeat().equals(2)) {
            throw new GlobalException(new SystemExceptionInfo(
                    ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT.getDefaultMessage(),
                    ContentErrorCodeEnum.CONTENT_TITLE_IS_NOT_ALLOWED_TO_REPEAT.getCode()));
        }

        // 校验栏目内标题是否允许重复
        dto.setCurrUsername(SystemContextUtils.getCurrentUsername());
        if (siteConfig.getTitleRepeat().equals(3)) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                service.copy(dto, request, siteConfig);
            } finally {
                lock.unlock();
            }
        } else {
            ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        service.copy(dto, request, siteConfig);
                    } catch (GlobalException e) {
                        e.printStackTrace();
                    }
                }
            });
        }



        return new ResponseInfo();
    }


    /**
     * 测试复制所有内容
     */
//	@RequestMapping(value = "/copyAll", method = RequestMethod.POST)
//	public ResponseInfo copyAll( HttpServletRequest request)
//			throws GlobalException {
//		ContentCopyDto dto = new ContentCopyDto();
//		List<Content> contents = service.findAll(false);
//		int copyCount =100;
//		CmsSiteConfig siteConfig = SystemContextUtils.getSite(request).getCmsSiteCfg();
//		HibernateProxyUtil.loadHibernateProxy(siteConfig.getAttr());
//		for(int i=0;i<copyCount;i++){
//			ThreadPoolService.getInstance().execute(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						for(Content c:contents){
//							dto.setChannelId(c.getChannelId());
//							dto.setIds(Arrays.asList(c.getId()));
//							service.copy(dto, request, siteConfig);
//						}
//					}catch (GlobalException e){
//
//					}
//				}
//			});
//		}
//		return new ResponseInfo();
//	}

    /**
     * 重置索引
     *
     * @param channelId        栏目Id
     * @param siteId           站点Id
     * @param releaseTimeStart 发布时间开始
     * @param releaseTimeEnd   发布时间结束
     * @throws GlobalException      GlobalException
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @Title: resetIndex
     * @return: ResponseInfo
     */
    @PutMapping(value = "/resetIndex")
    public ResponseInfo resetIndex(Integer channelId, Integer siteId, Date releaseTimeStart, Date releaseTimeEnd,
                                   HttpServletRequest request)
            throws GlobalException, IOException, InterruptedException, ExecutionException {
        if (siteId == null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        Integer sid = siteId;

        File file = new File(this.getClass().getClassLoader().getResource("").getPath().concat("hanlp"));
        if (!file.exists() || file.list().length <= 0) {
            return new ResponseInfo(SettingErrorCodeEnum.THE_SYSTEM_IS_INITIALIZING.getCode(),
                    SettingErrorCodeEnum.THE_SYSTEM_IS_INITIALIZING.getDefaultMessage());
        }

        /**
         * 没有返回结果的异步任务，先返回消息，后台运行生成索引
         */
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    luceneService.resetIndex(channelId, sid, releaseTimeStart, releaseTimeEnd);
                } catch (GlobalException e) {
                    logger.error(e.getMessage());
                }
            }
        });
        return new ResponseInfo(true);
    }

	/**
	 * 推送到站群
	 */
	@PutMapping(value = "/push/sites")
	public ResponseInfo pushSites(@RequestBody @Valid ContentPushSitesDto dto, HttpServletRequest request,
			BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		List<Content> contents = service.findAllById(dto.getContentIds());
		if (CollectionUtils.isEmpty(contents)) {
			return new ResponseInfo(false);
		}
		Map<Integer, Integer> channelIdMap = new HashMap<>();
		for (Content content : contents) {
			channelIdMap.put(content.getChannelId(), 0);
		}
		if (channelIdMap.keySet().size() > 1) {
			return new ResponseInfo(ContentErrorCodeEnum.QUOTED_CONTENT_CANNOT_BE_PUSHED.getCode(),
					ContentErrorCodeEnum.QUOTED_CONTENT_CANNOT_BE_PUSHED.getDefaultMessage());
		}
		if (!service.validType(CmsDataPerm.OPE_CONTENT_SITE_PUSH, contents.get(0).getChannelId())) {
			return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
					UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
		}
		Channel channel = channelService.findById(dto.getChannelId());
		if (!service.validType(CmsDataPerm.OPE_CONTENT_CREATE, channel.getId())) {
			return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
					UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
		}
		CmsSiteConfig config = cmsSiteService.findById(dto.getSiteId()).getCmsSiteCfg();
		if (!config.getSitePushOpen()) {
			return new ResponseInfo(SiteErrorCodeEnum.SITE_CANNOT_PUSH_CONTENT.getCode(),
					SiteErrorCodeEnum.SITE_CANNOT_PUSH_CONTENT.getDefaultMessage(), false);
		}
		if (!channel.getSiteId().equals(dto.getSiteId())) {
			return new ResponseInfo(ChannelErrorCodeEnum.CHANNEL_IS_NOT_UNDER_THE_SITE.getCode(),
					ChannelErrorCodeEnum.CHANNEL_IS_NOT_UNDER_THE_SITE.getDefaultMessage(), false);
		}
		boolean isPushSecret = false;
		String pushSecret = config.getSitePushSecret();
		if (StringUtils.isNotBlank(pushSecret)) {
			if (pushSecret.equals(dto.getPushSecret())) {
				isPushSecret = true;
			}
		} else {
			isPushSecret = true;
		}
		if (isPushSecret) {
			dto.setContents(contents);
			dto.setChannel(channel);
            GlobalConfig globalConfig = SystemContextUtils.getGlobalConfig(request);
			CmsSite site = SystemContextUtils.getSite(request);
			dto.setCurrUsername(SystemContextUtils.getCurrentUsername());
			ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        service.pushSites(dto, request,globalConfig,site);
                    } catch (GlobalException e) {
                        e.printStackTrace();
                    }
                }
            });
		} else {
			return new ResponseInfo(SiteErrorCodeEnum.PUSH_SECRET_ERROR.getCode(),
					SiteErrorCodeEnum.PUSH_SECRET_ERROR.getDefaultMessage(), false);
		}
		return new ResponseInfo();
	}

    /**
     * 用户使用内容模型记录
     *
     * @param request 请求
     * @param modelId 模型ID
     * @throws GlobalException 异常
     * @Title: saveModelRecord
     */
    @RequestMapping(value = "/model/record", method = RequestMethod.GET)
    public ResponseInfo saveModelRecord(HttpServletRequest request,
                                        @NotNull Integer modelId) throws GlobalException {
        //得到当前用户
        CoreUser user = SystemContextUtils.getCoreUser();
        UserModelRecord bean = new UserModelRecord(user.getId(), modelId);
        userModelRecordService.save(bean);
        return new ResponseInfo();
    }

    /**
     * 处理文档(文库文档转换)
     *
     * @param id      资源id
     * @param request request请求
     * @throws GlobalException 全局异常
     * @Title: conversionDoc
     * @return: ResponseInfo
     */
    @RequestMapping(value = "/conversion/doc", method = RequestMethod.GET)
    public ResponseInfo conversionDoc(@RequestParam Integer id, HttpServletRequest request)
            throws GlobalException {
        ResourcesSpaceData space = resourcesSpaceDataService.findById(id);
        String docUrl = docServiceImpl.conversionDoc(space, SystemContextUtils.getSite(request));
        return new ResponseInfo(docUrl);
    }

    /**
     * 查询是否存在强制通过按钮
     *
     * @param contentId 栏目id
     * @Title: forceReleaseButton
     * @return: ResponseInfo
     */
    @RequestMapping(value = "/force/button", method = RequestMethod.GET)
    public ResponseInfo forceReleaseButton(@RequestParam Integer contentId) {
        Content content = service.findById(contentId);
        if (!Arrays.asList(ContentConstant.STATUS_SMART_AUDIT_SUCCESS, ContentConstant.STATUS_SMART_AUDIT_FAILURE)
                .contains(content.getStatus())) {
            return new ResponseInfo(false);
        }
        return new ResponseInfo(service.getForceReleaseButton(content.getId()));
    }

    @MoreSerializeField({
            @SerializeField(clazz = ResourcesSpaceData.class, includes = {"id", "resourceType",
                    "alias", "suffix", "url"})
    })
    @RequestMapping(value = "/button", method = RequestMethod.GET)
    public ResponseInfo findByContentButton(@RequestParam Integer status, @RequestParam(required = false) Integer id,
                                            @RequestParam(required = false) Integer channelId,@RequestParam(required = false) Boolean quote) throws GlobalException {
        switch (status) {
            case ContentButtonVo.REQUEST_STATUS_SAVE:
                if (channelId == null) {
                    return new ResponseInfo(SystemExceptionEnum.ILLEGAL_PARAM.getCode(), "channelId不能为null");
                }
                break;
            case ContentButtonVo.REQUEST_STATUS_DETAILS:
                if (id == null) {
                    return new ResponseInfo(SystemExceptionEnum.ILLEGAL_PARAM.getCode(), "id不能为null");
                }
                break;
            default:
                break;
        }
        return new ResponseInfo(service.findByContentButton(status, id, channelId,quote));
    }

    /**
     * 重置密级
     * @param dto   重置密级dto
     * @return ResponseInfo
     */
    @PostMapping("/reset/secret")
    public ResponseInfo rsetSecret(@RequestBody ResetSecretDto dto,HttpServletRequest request,BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        CoreUser user = SystemContextUtils.getUser(request);
        Integer siteId = SystemContextUtils.getSiteId(request);
        ResetSecretVo vo = service.resetSecret(dto,user,siteId);
        return new ResponseInfo(vo);
    }

    /**
     * 校验重置密级是否完成
     * @param code  缓存中标识
     * @return ResponseInfo
     */
    @GetMapping("/execute/result")
    public ResponseInfo executeResult(@RequestParam String code) {
        Boolean status = Boolean.valueOf(String.valueOf(cacheProvider.getCache(Content.CONTENT_CACHE_KEY, code)));
        return new ResponseInfo(status);
    }

    /**
     * 浏览记录
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return ResponseInfo
     * @apiNote 基于X1.4的代码
     */
    @GetMapping(value = "/view/record")
    public ResponseInfo view(HttpServletRequest request,
            @Valid @NotNull(message = "内容ID不能为空") Integer contentId,
                             Date startDate, Date endDate) throws IllegalAccessException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        return new ResponseInfo(statisticsContentDataService.view(siteId, contentId,startDate,endDate));
    }

    /**
     * 浏览记录表格数据
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return ResponseInfo
     * @apiNote 基于X1.4的代码
     */
    @GetMapping(value = "/view/table")
    public ResponseInfo table(HttpServletRequest request,
                             @Valid @NotNull(message = "内容ID不能为空") Integer contentId,
                             Date startDate, Date endDate, Integer sortType, Boolean sort) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        //排序类型
        if (sortType == null) {
            sortType = SORT_TYPE_1;
        }
        //排序
        if (sort == null) {
            sort = false;
        }
        return new ResponseInfo(statisticsContentDataService.table(siteId, contentId,startDate,endDate, sortType, sort));
    }
}
