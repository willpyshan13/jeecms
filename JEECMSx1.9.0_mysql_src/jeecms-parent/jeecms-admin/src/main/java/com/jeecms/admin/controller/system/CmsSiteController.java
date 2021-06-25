/*
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.admin.controller.BaseTreeAdminController;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.vo.CoreUserAgent;
import com.jeecms.auth.service.CoreRoleService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.annotation.EncryptMethod;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.base.domain.DragSortDto;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SiteErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.manage.annotation.OperatingIntercept;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.util.TplUtils;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.HttpClientUtil;
import com.jeecms.component.listener.SiteListener;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.constants.ContentReviewConstant;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.dto.ContentLucene;
import com.jeecms.content.domain.dto.ContentSearchDto;
import com.jeecms.content.service.CmsModelService;
import com.jeecms.content.service.ContentGetService;
import com.jeecms.content.service.FlowService;
import com.jeecms.protection.service.GradeProtectionService;
import com.jeecms.questionnaire.service.SysQuestionnaireService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.resource.service.TplResourceService;
import com.jeecms.resource.service.TplService;
import com.jeecms.resource.service.UploadOssService;
import com.jeecms.system.domain.CmsDataPerm.OpeSiteEnum;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.CmsSiteConfig;
import com.jeecms.system.domain.CmsSiteExt;
import com.jeecms.system.domain.SiteModelTpl;
import com.jeecms.system.domain.dto.CmsSiteAgent;
import com.jeecms.system.domain.dto.CmsSiteExtDto;
import com.jeecms.system.domain.dto.SynchChildSiteTplDto;
import com.jeecms.system.domain.vo.CmsSiteVo;
import com.jeecms.system.domain.vo.SynchPlatformTplVo;
import com.jeecms.system.exception.SiteExceptionInfo;
import com.jeecms.system.service.*;
import com.jeecms.universal.service.HttpRequestPlatformUtilService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.jeecms.common.constants.TplConstants.TPL_BASE;
import static com.jeecms.common.constants.WebConstants.SPT;

/**
 * 站点管理
 *
 * @author: tom/ljw
 * @date: 2018年3月9日 下午3:22:24
 */
@RequestMapping("/sites")
@RestController
public class CmsSiteController extends BaseTreeAdminController<CmsSite, Integer> {

	private static final Logger logger = LoggerFactory.getLogger(CmsSiteController.class);
    private final  ReentrantLock lock = new ReentrantLock();
	/**
	 * 获取用户可查看站点
	 */
	@GetMapping(value = "/tree")
	public ResponseInfo getViewSites(HttpServletRequest request, Paginable paginable)  {
		// 获取用户可查看站点
		List<CmsSite> siteList = SystemContextUtils.getCoreUser().getViewSites();
		if (!siteList.isEmpty()) {
			// 过滤加入回收站，删除
			siteList = siteList.stream().filter(x -> !x.getIsDelete()).filter(x -> !x.getHasDeleted())
					.collect(Collectors.toList());
		}
		// 按照站点排序值，站点创建时间排序
		siteList = CmsSiteAgent.sortBySortNumAndChild(siteList);
		// 讲站点集合转换成JSONArray
		JSONArray responseData = CmsSiteAgent.convertForTree(siteList,SystemContextUtils.getCoreUser());
		return new ResponseInfo(responseData);
	}

	/**
	 * 获取用户可管理站点列表
	 */
	@GetMapping(value = "/ownsite")
	@SerializeField(clazz = CmsSite.class, includes = { "id", "name" })
	public ResponseInfo getOwnerSites(HttpServletRequest request, Paginable paginable)  {
		// 获取用户可管理站点
		List<Integer> siteList = SystemContextUtils.getUser(request).getOwnerSiteIds();
		List<CmsSite> sites = siteService.findByIds(siteList);
		if (!sites.isEmpty()) {
			// 过滤加入回收站，删除
			sites = sites.stream().filter(x -> !x.getIsDelete()).filter(x -> !x.getHasDeleted())
					.collect(Collectors.toList());
		}
		sites = CmsSiteAgent.sortBySortNumAndChild(sites);
		return new ResponseInfo(sites);
	}

	/**
	 * 获取用户可站点推送的站点树型结构
	 */
	@GetMapping(value = "/ownsitepush")
	public ResponseInfo ownsitePush(HttpServletRequest request) throws GlobalException {
		// 获取用户可管理站点
		List<Integer> siteList = SystemContextUtils.getUser(request).getOwnerSiteIds();
		List<CmsSite> sites = service.findAllById(siteList);
		if (!sites.isEmpty()) {
			// 过滤加入回收站，删除,是否接受站群推送
			sites = sites.stream()
					.filter(x -> !x.getIsDelete())
					.filter(x -> !x.getHasDeleted())
					.filter(CmsSite::getSitePushOpen)
					.collect(Collectors.toList());
		}
		sites = CmsSiteAgent.sortByIdAndChild(sites);
		JSONArray responseData;
		responseData = super.getChildTree(sites, false, "name");
		return new ResponseInfo(responseData);
	}

	/**
	 * 查询用户站群权限的站点树(与上面ownsitepush接口功能一致)
	 */
	@GetMapping(value = "/getOwnerTree")
	public ResponseInfo getOwnerTree(HttpServletRequest request, Paginable paginable) throws GlobalException {
		Set<CmsSite> sites = SystemContextUtils.getUser(request).getOwnerSites();
		CoreUserAgent.agentOwnerSites(SystemContextUtils.getUser(request));
		JSONArray responseData = null;
		List<CmsSite> ownerSites = CmsSiteAgent.sortByIdAndChild(sites);
		Integer siteId = SystemContextUtils.getSiteId(request);
		if (!sites.isEmpty()) {
			// 过滤加入回收站，删除,是否接受站群推送
			ownerSites = ownerSites.stream()
					.filter(x -> !x.getIsDelete())
					.filter(x -> !x.getHasDeleted())
					.filter(x -> x.getSitePushOpen())
					.filter(x -> !x.getId().equals(siteId)).collect(Collectors.toList());
		}
		responseData = super.getChildTree(ownerSites, false, "name");
		return new ResponseInfo(responseData);
	}

	/**
	 * 获取站点列表
	 */
	@GetMapping
	@SerializeField(clazz = CmsSite.class, includes = { "id", "name", "domain", "path", "isOpen" })
	public ResponseInfo getChild(@RequestParam(value = "parentId", required = false) Integer parentId) {
		List<CmsSite> sites ;
		if (parentId != null) {
			sites = siteService.findByParentId(parentId);
		} else {
			sites = siteService.findByParentId(null);
		}
		if (!sites.isEmpty()) {
			// 过滤加入回收站，删除
			sites = sites.stream().filter(x -> !x.getIsDelete()).filter(x -> !x.getHasDeleted())
					.collect(Collectors.toList());
		}
		sites = CmsSiteAgent.sortByIdAndChild(sites);
		return new ResponseInfo(sites);
	}

	/**
	 * 基本信息详情
	 */
	@GetMapping(value = "/{id:[0-9]+}")
	@MoreSerializeField({ @SerializeField(clazz = CmsSite.class, includes = { "id", "name", "path", "protocol",
			"isOpen", "iconPath", "description", "domain", "siteLanguage", "seoTitle", "seoKeyword", "domainAlias",
			"iconId", "seoDescription", "parentId", "nodeSiteIds", "alias", "url", "previewUrl",
			"deleteAble", "editAble", "openCloseAble", //权限
			"viewAble", "permAssignAble", "newChildAble", "staticAble" }), })//权限
	@Override
	public ResponseInfo get(@PathVariable Integer id) throws GlobalException {
		/**管理后台默认定是PC访问，获取站点首页url需要*/
		SystemContextUtils.setMobile(false);
		SystemContextUtils.setTablet(false);
		SystemContextUtils.setPc(true);
		return super.get(id);
	}

	/**
	 * 扩展配置详情
	 */
	@GetMapping(value = "/ext")
	@MoreSerializeField({
			@SerializeField(clazz = CmsSite.class, includes = { "id", "cfg", "cmsSiteExt", "uploadPicSuffix",
					"uploadVideoSuffix", "uploadAttachmentSuffix", "uploadAudioSuffix", "uploadDocumentSuffix" }),
			@SerializeField(clazz = CmsSiteExt.class, includes = { "commentFlowId", "newContentResourceId",
					"staticPageFtpId", "staticPageOssId", "uploadFtpId", "uploadOssId", "watermarResourceId" }), })
	public ResponseInfo ext(Integer id) throws GlobalException {
		return super.get(id);
	}

	/**
	 * 默认模板详情
	 */
	@GetMapping(value = "/model")
	@MoreSerializeField({ @SerializeField(clazz = CmsModel.class, includes = { "id", "modelName", "tplType" }), })
	public ResponseInfo model(Integer id) throws GlobalException {
		CmsSiteVo vo = siteService.getModel(id);
		return new ResponseInfo(vo);
	}

	/**
	 * 新增站点
	 */
	@PostMapping()
    @OperatingIntercept
	@EncryptMethod
	public ResponseInfo save(@RequestBody @Valid CmsSite site, BindingResult result, HttpServletRequest request)
			throws GlobalException {
		super.validateBindingResult(result);
		CmsSite newSite = null;
		CmsSite currSite = SystemContextUtils.getSite(request);
		CoreUser currUser = SystemContextUtils.getUser(request);
		if (site.getIconId() != null) {
			site.setIconRes(spaceDataService.findById(site.getIconId()));
		}
		//检查权限
		super.checkSiteDataPerm(site.getParentId(), OpeSiteEnum.NEWCHILD);
		//处理并发
		final ReentrantLock reentrantLock = this.lock;
		reentrantLock.lock();
		try {
			newSite = siteService.save(currSite, currUser, site);
            CmsSite finalNewSite = newSite;
			//lambda的方式
			ThreadPoolService.getInstance().execute(
				()-> {
					// 新增子站，子站将同步父站点的消息模板
					String mobile = null;
					boolean platformSuccess = false;
					try {
						mobile = requestPlatformUtilService.getUserParameter(false);
						platformSuccess = org.apache.commons.lang3.StringUtils.isBlank(mobile);
					} catch (ParseException | IOException e) {
						platformSuccess  = true;
					} catch (GlobalException e) {
						logger.error(e.getMessage());
					}
					List<SynchPlatformTplVo> vos = null;
					SynchChildSiteTplDto dto = new SynchChildSiteTplDto(finalNewSite.getId(), finalNewSite.getName(),site.getParentId());
					if(!platformSuccess) {
						try {
							dto.setMobile(mobile);
							vos = synchTplHttp(dto);
						} catch (GlobalException e) {
							logger.error(e.getMessage());
						}
					}
					try{
						messageTplService.synchronizeChildSiteTpl(vos,dto.getChildIds(),dto.getSiteId(),!CollectionUtils.isEmpty(vos));
					} catch (GlobalException e) {
						logger.error(e.getMessage());
					}
				}
			);

		}finally {
			reentrantLock.unlock();
		}
		return new ResponseInfo(newSite.getId());
	}

    /**
     * 请求header头map
     */
    private static Map<String, String> HEADER_MAP = new HashMap<String, String>();
    /**
     * 获取header头
     *
     * @return Map<String, String>
     * @throws GlobalException 全局异常
     */
    private Map<String, String> getHeader() throws GlobalException,ParseException,IOException {
        HEADER_MAP.put(ContentReviewConstant.SEND_REQUEST_HEADER, requestPlatformUtilService.getUserParameter(true));
        return HEADER_MAP;
    }

    private List<SynchPlatformTplVo> synchTplHttp(SynchChildSiteTplDto dto) throws GlobalException{
        try {
            String responseStr = HttpClientUtil.timeLimitPostJson(WebConstants.DoMain.SYNCH_CHILD_SITE_TPL, dto,this.getHeader(),8000);
            if (JSONObject.isValidObject(responseStr)) {
                JSONObject json = JSONObject.parseObject(responseStr);
                if (WebConstants.RESPONSE_CODE_CORRECT == json.getInteger(WebConstants.RESPONSE_CODE_MARK)) {
                    return json.getJSONArray("data").toJavaList(SynchPlatformTplVo.class);
                }
            }
        } catch (ParseException|IOException e) {
			logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }

	/**
	 * 修改站点基本信息
	 */
	@PutMapping()
    @OperatingIntercept
	@EncryptMethod
	@Override
	public ResponseInfo update(@Valid @RequestBody CmsSite site, BindingResult result)
			throws GlobalException {
		super.validateBindingResult(result);
		// 判断站点路径是否存在
		boolean flags = siteService.existSitePath(site.getId(), site.getPath());
		if (flags) {
			return new ResponseInfo(SiteErrorCodeEnum.SITE_PATH_ERROR.getCode(),
					SiteErrorCodeEnum.SITE_PATH_ERROR.getDefaultMessage());
		}
		if (site.getIconId() != null) {
			site.setIconRes(spaceDataService.findById(site.getIconId()));
		}
		//如果父级站点已删除，则返回错误提示；
		if (site.getParentId() != null) {
			CmsSite parent = siteService.findById(site.getParentId());
			if (parent == null || parent.getIsDelete() || parent.getHasDeleted() != null && parent.getHasDeleted()) {
				return new ResponseInfo(SiteErrorCodeEnum.PARENT_SITE_ALREADY_DELETE.getCode(),
						SiteErrorCodeEnum.PARENT_SITE_ALREADY_DELETE.getDefaultMessage());
			}
		}
		CmsSite site1 = siteService.findById(site.getId());
		//不允许当前组织的子结点作为父结点使用
		if(site1.getChildAllId().contains(site.getParentId())) {
			return new ResponseInfo(SiteErrorCodeEnum.ORG_CHILD_NOT_PARENT_FOR_SELF.getCode(),
					SiteErrorCodeEnum.ORG_CHILD_NOT_PARENT_FOR_SELF.getDefaultMessage());
		}
		//检查权限
		super.checkSiteDataPerm(site.getId(), OpeSiteEnum.EDIT);
		siteService.update(site);
		clearPermCache();
		return new ResponseInfo();
	}

	/**
	 * 修改模板设置
	 */
	@PutMapping("/model")
    @OperatingIntercept
	public ResponseInfo updateModel(@RequestBody @Valid CmsSiteExtDto dto, BindingResult result)
			throws GlobalException {
		super.validateBindingResult(result);
		CmsSite site = service.findById(dto.getId());
		encryptField(dto);
		site.getCfg().putAll(dto.getCfg());
		service.update(site);
		//lambda的方式
		ThreadPoolService.getInstance().execute(
			()-> {
			if (dto.getModelTpls() != null && !dto.getModelTpls().isEmpty()) {
				try {
					// 物理删除站点配置模板
					siteModelTplService.physicalDeleteInBatch(site.getModelTpls());
					List<SiteModelTpl> tpls = new ArrayList<>(10);
					// 重新添加站点配置模板
					for (SiteModelTpl tpl : dto.getModelTpls()) {
						tpl.setSite(site);
						tpl.setModel(cmsModelService.findById(tpl.getModelId()));
						tpls.add(tpl);
					}
					siteModelTplService.saveAll(tpls);
				}catch (GlobalException e){
					logger.error(e.getMessage());
				}
			}
		});
		return new ResponseInfo();
	}

	/**解密**/
	void encryptField(CmsSiteExtDto dto) {
		String mobileHomePageTemplates = dto.getCfg().getOrDefault("mobileHomePageTemplates", "");
		if (org.apache.commons.lang3.StringUtils.isNotBlank(mobileHomePageTemplates) && gradeProtectionService != null) {
			dto.getCfg().put("mobileHomePageTemplates",  gradeProtectionService.decryptStr(mobileHomePageTemplates));
		}
		String pcHomePageTemplates =  dto.getCfg().getOrDefault("pcHomePageTemplates", "");
		if (org.apache.commons.lang3.StringUtils.isNotBlank(pcHomePageTemplates) && gradeProtectionService != null) {
			dto.getCfg().put("pcHomePageTemplates",  gradeProtectionService.decryptStr(pcHomePageTemplates));
		}
		for (SiteModelTpl modelTpl : dto.getModelTpls()) {
			if (gradeProtectionService != null && StringUtils.isNotBlank(modelTpl.getPcTplPath()) ) {
				modelTpl.setPcTplPath(gradeProtectionService.decryptStr(modelTpl.getPcTplPath()));
			}
			if (gradeProtectionService != null && StringUtils.isNotBlank(modelTpl.getMobileTplPath()) ) {
				modelTpl.setMobileTplPath(gradeProtectionService.decryptStr(modelTpl.getMobileTplPath()));
			}
		}
	}

	/**
	 * 删除站点（加入回收站）
	 */
    @PostMapping("/delete")
    @OperatingIntercept
	public ResponseInfo delete(HttpServletRequest request,
			@RequestBody @Valid DeleteDto dels, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		List<CmsSite> toDelSites = new ArrayList<>(10);
		List<Integer> list = Arrays.asList(dels.getIds());
		for (Integer integer : list) {
			CmsSite site = siteService.findById(integer);
			//检查权限
			super.checkSiteDataPerm(integer, OpeSiteEnum.DEL);
			// 顶级站点不能删除
			if (site.getParentId() == null) {
				return new ResponseInfo(SiteErrorCodeEnum.TOP_SITE_CANNOT_DELETE.getCode(),
						SiteErrorCodeEnum.TOP_SITE_CANNOT_DELETE.getDefaultMessage(), false);
			}
			List<CmsSite> childSites = new ArrayList<>();
			/** 子站点有多层级 */
			childSites.addAll(site.getChildAll());
			for (CmsSite cmsSite : childSites) {
				cmsSite.setIsDelete(true);
			}
			toDelSites.addAll(childSites);
		}
		/** 主动清空用户、组织、角色权限缓存 */
		siteService.batchUpdate(toDelSites);
		/**
         *不处理站点删除去删除权限数据，易导致继承自组织导致角色用户权限自动放大
		Integer[]ids=new Integer[list.size()];
		list.toArray(ids);
		ThreadPoolService.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (SiteListener listener : siteListeners) {
						listener.beforeSiteDelete(ids);
					}
				}catch (GlobalException e){
					logger.error(e.getMessage());
				}
			}
		});
         */
		CoreUser currUser = SystemContextUtils.getCoreUser();
		/**清理流程中数据*/
		ThreadPoolService.getInstance().execute(() -> {
			try {
				for(Integer siteId:CmsSite.fetchIds(toDelSites)){
					int page = 1;
					int size = ContentLucene.THREAD_PROCESS_NUM;
					Pageable pageable = PageRequest.of(page, size);
					ContentSearchDto searchDto = new ContentSearchDto(siteId,null, ContentConstant.ORDER_TYPE_ID_DESC,
							Arrays.asList(ContentConstant.STATUS_FLOWABLE,ContentConstant.STATUS_WAIT_PUBLISH), false);
					Page<Content> content = contentGetService.getPages(searchDto, pageable);
					int totalPage = content.getTotalPages();
					for (int i = 0; i < totalPage; i++) {
						final int p = i;
						Pageable onePage = PageRequest.of(p, size);
						Page<Content> contentPage;
						try {
							contentPage = contentGetService.getPages(searchDto, onePage);
							List<Content>  contents = contentPage.getContent();
							if (contents != null && contents.size() > 0) {
								List<Integer> contentIds = Content.fetchIds(contents);
								flowService.doInterruptDataFlow(ContentConstant.WORKFLOW_DATA_TYPE_CONTENT, contentIds,
										currUser);
							}
						} catch (GlobalException e) {
							logger.error(e.getMessage());
						}
					}
				}
			}catch (Exception e){
				logger.error(e.getMessage());
			}
		});
		clearPermCache();
		return new ResponseInfo();
	}

	/**
	 * 修改站点扩展表
	 */
	@PutMapping(value = "/ext")
    @OperatingIntercept
	public ResponseInfo updateExt(@RequestBody @Valid CmsSiteExtDto dto, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		CmsSite site = service.findById(dto.getId());
		CmsSiteExt ext = site.getCmsSiteExt();
		if (ext != null) {
			ext.transfer(dto, ext);
		}
		Integer newContentResourceId = dto.getNewContentResourceId();
		Integer watermarkPicture = dto.getWatermarkResourceId();
		if (newContentResourceId != null) {
			ResourcesSpaceData data = spaceDataService.findById(newContentResourceId);
			dto.getCfg().put("contentNewFlagDefinition", data != null ? newContentResourceId.toString() : "");
			dto.getCfg().put("contentNewFlagDefinitionUrl", data != null ? data.getUrl() : "");
			ext.setNewContentResourceId(newContentResourceId);
		}
		if (watermarkPicture != null) {
			ResourcesSpaceData data = spaceDataService.findById(watermarkPicture);
			dto.getCfg().put("watermarkPicture", data != null ? watermarkPicture.toString() : "");
			dto.getCfg().put("watermarkPictureUrl", data != null ? data.getUrl() : "");
			ext.setWatermarkResource(data);
			ext.setWatermarkResourceId(watermarkPicture);
		}
		Set<String>cfgKeys = dto.getCfg().keySet();
		if (cfgKeys.contains("newContentResourceId")) {
			String s = dto.getCfg().get("newContentResourceId");
			if(StrUtils.isNumeric(s)){
				Integer id = Integer.parseInt(s);
				ResourcesSpaceData data = spaceDataService.findById(id);
				dto.getCfg().put("contentNewFlagDefinition", data != null ? s : "");
				dto.getCfg().put("contentNewFlagDefinitionUrl", data != null ? data.getUrl() : "");
				ext.setNewContentResourceId(id);
			}

		}
		if (cfgKeys != null&&cfgKeys.contains(CmsSiteConfig.UPLOAD_FILE_MEMORY)) {
			String str = dto.getCfg().get(CmsSiteConfig.UPLOAD_FILE_MEMORY);
			if(StrUtils.isNumeric(str)){
				Integer id = Integer.parseInt(str);
				ext.setUploadFtpId(id);
				ext.setUploadFtp(ftpService.findById(id));
			}
		}
		if (cfgKeys != null&&cfgKeys.contains(CmsSiteConfig.STATIC_SERVER_MEMORY)) {
			String str = dto.getCfg().get(CmsSiteConfig.STATIC_SERVER_MEMORY);
			if(StrUtils.isNumeric(str)){
				Integer id = Integer.parseInt(str);
				ext.setStaticPageFtpId(id);
				ext.setStaticPageFtp(ftpService.findById(id));
			}
		}
		if (cfgKeys != null&&cfgKeys.contains(CmsSiteConfig.UPLOAD_FILE_MEMORY)) {
			String str = dto.getCfg().get(CmsSiteConfig.UPLOAD_FILE_MEMORY);
			if(StrUtils.isNumeric(str)){
				Integer id = Integer.parseInt(str);
				ext.setUploadOssId(id);
				ext.setUploadOss(uploadOssService.findById(id));
			}
		}
		if (cfgKeys != null&&cfgKeys.contains(CmsSiteConfig.STATIC_SERVER_MEMORY)) {
			String str = dto.getCfg().get(CmsSiteConfig.STATIC_SERVER_MEMORY);
			if(StrUtils.isNumeric(str)){
				Integer id = Integer.parseInt(str);
				ext.setStaticPageOssId(id);
				ext.setStaticPageOss(uploadOssService.findById(id));
			}
		}
		if (cfgKeys.size() > 0 &&cfgKeys.contains(CmsSiteConfig.SURVEY_CONFIGURATION_ID)) {
			Integer id = null;
			String str = dto.getCfg().get(CmsSiteConfig.SURVEY_CONFIGURATION_ID);
			if(StringUtils.isNotBlank(str) && StrUtils.isNumeric(str)){
				id = Integer.parseInt(str);
			}
			/**
			 * 没有返回结果的异步任务，先返回消息，后台运行生成索引
			 */
			Integer finalId = id;
			//lambda的方式
			ThreadPoolService.getInstance().execute(
					()-> {
						try {
							questionnaireService.updateWorkFlow(finalId, site.getId());
						} catch (GlobalException e) {
							logger.error(e.getMessage());
						}
					}
			);
		}
		site.setCmsSiteExt(ext);
		siteExtService.update(ext);
		site.setCfg(dto.getCfg());
		service.update(site);
		return new ResponseInfo();
	}

	/**
	 * 站点回收站
	 */
	@GetMapping(value = "/recycle")
	@SerializeField(clazz = CmsSite.class, includes = { "id", "name", "updateTime", "updateUser" })
	public ResponseInfo recycle(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String[]> params = new HashMap<>(16);
		params.put("EQ_isDelete_Boolean", new String[] { "true" });
		return new ResponseInfo(siteService.getList(params, null, false));
	}

	/**
	 * 站点回收站删除,此处不检查删除权限，因为在加入回收站的时候就已经判断
	 * 如果此时在检查的话，就会报站点删除没有权限的操作
	 */
	@PostMapping(value = "/recycle/delete")
	public ResponseInfo del(HttpServletRequest request,
			@RequestBody @Valid DeleteDto dels, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		siteService.delete(dels.getIds());
		return new ResponseInfo();
	}

	/**
	 * 还原站点
	 */
	@PostMapping(value = "/restore")
	public ResponseInfo restore(@RequestBody @Valid DeleteDto dels, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		List<Integer> list = Arrays.asList(dels.getIds());
		List<CmsSite> cmssites = new ArrayList<>(10);
		for (Integer integer : list) {
			CmsSite site = siteService.findById(integer);
			// 得到全部父结点数据
			cmssites.addAll(site.getSiteParents(site));
		}
		for (CmsSite cmsSite : cmssites) {
			cmsSite.setIsDelete(false);
		}
		siteService.batchUpdate(cmssites);
		/** 主动清空用户权限缓存 */
		clearPermCache();
		return new ResponseInfo();
	}

	/**
	 * 验证目录
	 *
	 * @param: path
	 *             目录
	 * @param: id
	 *             站点ID
	 */
	@GetMapping(value = "/path/unique")
	public ResponseInfo path(String path, Integer id) {
		boolean flag = siteService.existSitePath(id, path);
		return flag ? new ResponseInfo(false) : new ResponseInfo(true);
	}

	/**
	 * 可管理上级站点+可查看站点
	 *
	 * @param: request
	 *             请求
	 */
	@GetMapping(value = "/childs")
	public ResponseInfo childs(HttpServletRequest request)  {
		List<Integer> list = SystemContextUtils.getUser(request).getViewSiteIds();
		List<CmsSite> sites = siteService.findAllById(list);
		CoreUser user = SystemContextUtils.getCoreUser();
		JSONArray responseData = CmsSiteAgent.convertListToJsonArray(sites,user);
		return new ResponseInfo(responseData);
	}

	/**
	 * 获取模板下拉列表
	 *
	 * @param siteId
	 *            站点ID
	 * @param solution
	 *            模板方案
	 * @return ResponseInfo 响应
	 * @throws GlobalException
	 *             异常
	 * @Title: models
	 */
	@GetMapping(value = "/models")
	public ResponseInfo models(Integer siteId, String solution) throws GlobalException {
		return siteService.models(siteId, solution);
	}

	/**
	 * 获取模板方案列表
	 *
	 * @param siteId
	 *            站点ID
	 * @return ResponseInfo 响应
	 * @throws GlobalException
	 *             异常
	 * @Title: models
	 */
	@GetMapping(value = "/solutions")
	public ResponseInfo solutions(Integer siteId) throws GlobalException {
		CmsSite site = siteService.findById(siteId);
		String[] solutions = resourceService.getSolutions(site.getTplPath());
		return new ResponseInfo(solutions);
	}

	/**
	 * 首页模板列表
	 *
	 * @param siteId
	 *            站点ID
	 * @param solution
	 *            模板方案
	 * @throws GlobalException
	 *             异常
	 */
	@GetMapping("/tpl/list")
	public ResponseInfo getTpl(Integer siteId, String solution) throws GlobalException {
		CmsSite site = siteService.findById(siteId);
		List<String> indexTplList = getTplIndex(site, solution);
		return new ResponseInfo(indexTplList);
	}

	private List<String> getTplIndex(CmsSite site, String solution) throws GlobalException {
		// 得到站点目录
		String path = site.getPath();
		List<String> tplList = null;
		try {
			tplList = tplService.getIndexBySolutionPath(solution, site);
		} catch (IOException e) {
			logger.error("ftp配置错误");
		}
		return TplUtils.tplTrim(tplList, getTplPath(path) + SPT + solution + SPT, null);
	}

	private String getTplPath(String path) {
		return TPL_BASE + File.separator + path;
	}

	/**
	 * 开启站点
	 *
	 * @param dto 批量操作Dto
	 * @throws GlobalException 异常
	 * @Title: enableSite
	 * @return: ResponseInfo 返回
	 */
	@PostMapping("/on")
    @OperatingIntercept
	public ResponseInfo enableSite(HttpServletRequest request, @RequestBody @Valid DeleteDto dto,
			BindingResult result)
			throws GlobalException {
		super.validateBindingResult(result);
		CoreUser user = SystemContextUtils.getUser(request);
		List<CmsSite> sites = service.findAllById(Arrays.asList(dto.getIds()));
		for (CmsSite site : sites) {
			// 检查权限
			//super.checkSiteDataPerm(site.getId(), OpeSiteEnum.OPENCLOSE);
			boolean exportValid = (user.getOpenCloseSiteIds() != null
					&& !user.getOpenCloseSiteIds().contains(site.getId()))
					|| user.getOpenCloseSiteIds() == null;
			if (exportValid) {
				String msg = MessageResolver.getMessage(
						SiteErrorCodeEnum.NO_PERMISSION_OPEN_CLOSE_SITE_ERROR.getCode(),
						SiteErrorCodeEnum.NO_PERMISSION_OPEN_CLOSE_SITE_ERROR
								.getDefaultMessage());
				throw new GlobalException(new SiteExceptionInfo(
						SiteErrorCodeEnum.NO_PERMISSION_OPEN_CLOSE_SITE_ERROR.getCode(),
						msg));
			}
			site.setIsOpen(true);
		}
		service.batchUpdate(sites);
		/** 权限需要更新，站点列表取的权限里的站点数据 */
		clearPermCache();
		return new ResponseInfo();
	}

	/**
	 * 关闭站点
	 *
	 * @param dto 批量操作Dto
	 * @throws GlobalException 异常
	 * @Title: disableSite
	 * @return: ResponseInfo 返回
	 */
	@PostMapping("/off")
    @OperatingIntercept
	public ResponseInfo disableSite(HttpServletRequest request, @RequestBody @Valid DeleteDto dto,
			BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		List<CmsSite> sites = service.findAllById(Arrays.asList(dto.getIds()));
		CoreUser user = SystemContextUtils.getUser(request);
		for (CmsSite site : sites) {
			// 检查权限
			//super.checkSiteDataPerm(site.getId(), OpeSiteEnum.OPENCLOSE);
			boolean exportValid = (user.getOpenCloseSiteIds() != null
					&& !user.getOpenCloseSiteIds().contains(site.getId()))
					|| user.getOpenCloseSiteIds() == null;
			if (exportValid) {
				String msg = MessageResolver.getMessage(
						SiteErrorCodeEnum.NO_PERMISSION_OPEN_CLOSE_SITE_ERROR.getCode(),
						SiteErrorCodeEnum.NO_PERMISSION_OPEN_CLOSE_SITE_ERROR
								.getDefaultMessage());
				throw new GlobalException(new SiteExceptionInfo(
						SiteErrorCodeEnum.NO_PERMISSION_OPEN_CLOSE_SITE_ERROR.getCode(),
						msg));
			}
			site.setIsOpen(false);
		}
		service.batchUpdate(sites);
		/** 权限需要更新，站点列表取的权限里的站点数据 */
		clearPermCache();
		return new ResponseInfo();
	}

	/**
	 * 排序
	 * 
	 * @Title: dragSort
	 * @param sortDto
	 *            排序
	 * @param result
	 *            检验
	 * @return ResponseInfo 响应
	 * @throws GlobalException
	 *             异常
	 */
	@PutMapping(value = "/sort")
	public ResponseInfo dragSort(@RequestBody DragSortDto sortDto, BindingResult result,
			HttpServletRequest request) throws GlobalException {
		validateBindingResult(result);
		// 获取用户可查看站点
		List<CmsSite> siteList = SystemContextUtils.getUser(request).getViewSites();
		if (!siteList.isEmpty()) {
			// 过滤加入回收站，删除
			siteList = siteList.parallelStream().filter(x -> !x.getIsDelete()).filter(x -> !x.getHasDeleted())
					.collect(Collectors.toList());
		}
		siteService.updatePriority(sortDto, siteList);
		/** 权限需要更新，站点列表取的权限里的站点数据 */
		clearPermCache();
		return new ResponseInfo();
	}

	private void clearPermCache() {
		/** 主动清空用户、组织、角色权限缓存 */
		ThreadPoolService.getInstance().execute(()-> {
				userService.clearAllUserCache();
				orgService.clearAllOrgCache();
				roleService.clearAllRoleCache();
			}
		);
	}

	@GetMapping(value = "/getAllTree")
	public ResponseInfo getAllTree(HttpServletRequest request) throws GlobalException {
	    Integer siteId = SystemContextUtils.getSiteId(request);
	    if (siteId == null) {
	        return new ResponseInfo();
        }
	    CmsSite site = siteService.findById(siteId);
	    if (site !=null && !site.getIsDelete()) {
	        List<CmsSite> childAll = site.getChildAllList();
	        JSONArray responseData = super.getChildTree(childAll, false, "name","id");
	        return new ResponseInfo(responseData);
        }
	    return new ResponseInfo();
    }

	@GetMapping(value = "/getAllId")
	public ResponseInfo getAllId(HttpServletRequest request) throws GlobalException {
	    Integer siteId = SystemContextUtils.getSiteId(request);
	    if (siteId == null) {
	        return new ResponseInfo();
        }
	    CmsSite site = siteService.findById(siteId);
	    if (site !=null && !site.getIsDelete()) {
            JSONArray responseData = super.getChildTree(site.getChildAll(), false, "name","id", "childAllId");
            return new ResponseInfo(responseData);
        }
	    return new ResponseInfo();
    }

	/**
	 * 无视权限站点树
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/noauth/tree")
	public ResponseInfo noauth() throws GlobalException {
		// 获取用户可查看站点
		List<CmsSite> siteList = siteService.findAll(true);
		if (!siteList.isEmpty()) {
			// 过滤加入回收站，删除
			siteList = siteList.stream().filter(x -> !x.getIsDelete()).filter(x -> !x.getHasDeleted())
					.collect(Collectors.toList());
		}
		siteList = CmsSiteAgent.sortBySortNumAndChild(siteList);
		JSONArray responseData = super.getChildTree(siteList, false, "name");
		return new ResponseInfo(responseData);
	}

	@Autowired
	private TplResourceService resourceService;
	@Autowired
	private CmsSiteService siteService;
	@Autowired
	private CmsSiteExtService siteExtService;
	@Autowired
	private ResourcesSpaceDataService spaceDataService;
	@Autowired
	private SiteModelTplService siteModelTplService;
	@Autowired
	private CmsModelService cmsModelService;
	@Autowired
	private TplService tplService;
	@Autowired
	private CoreUserService userService;
	@Autowired
	private CoreRoleService roleService;
	@Autowired
	private CmsOrgService orgService;
	@Autowired
	private FtpService ftpService;
	@Autowired
	private UploadOssService uploadOssService;
	@Autowired
	private SysQuestionnaireService questionnaireService;
	@Autowired
	private List<SiteListener> siteListeners;
    @Autowired
    private MessageTplService messageTplService;
    @Autowired
    private HttpRequestPlatformUtilService requestPlatformUtilService;
    @Autowired
	private ContentGetService contentGetService;
    @Autowired
	private FlowService flowService;
	@Autowired(required = false)
	private GradeProtectionService gradeProtectionService;
}
