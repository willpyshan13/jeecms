/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.content;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.admin.controller.BaseAdminController;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.dto.ContentSearchDto;
import com.jeecms.content.domain.dto.OperationDto;
import com.jeecms.content.domain.dto.WechatPushDto;
import com.jeecms.content.domain.dto.WechatViewDto;
import com.jeecms.content.domain.vo.ContentVo;
import com.jeecms.content.service.ContentGetService;
import com.jeecms.content.service.ContentService;
import com.jeecms.system.domain.CmsDataPerm;
import com.jeecms.system.domain.ContentType;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.domain.dto.BeatchDto;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 内容扩展控制器
 *
 * @author: ljw
 * @date: 2019年5月16日 上午8:53:24
 */
@RequestMapping("/contentext")
@RestController
public class ContentExtController extends BaseAdminController<Content, Integer> {

    @Autowired
    private ContentService contentService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentGetService contentGetService;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 初始化
     *
     * @Title: init
     * @Description: 完成
     */
    @PostConstruct
    public void init() {
        String[] queryParams = {
                // 开始时间
                "[startTime,updateTime]_GTE_Timestamp",
                // 结束时间
                "[endTime,updateTime]_LTE_Timestamp",
                // 栏目ID
                "channelId_EQ_Integer",
                // 标题
                "title_LIKE"};
        super.setQueryParams(queryParams);
    }

	/**
	 * 内容列表分页
	 * 
	 * @Title: page
	 * @param request  请求
	 * @param dto      传输对象
	 * @return ResponseInfo 返回对象
	 * @throws GlobalException 异常
	 */
	@PostMapping(value = "/page")
	@MoreSerializeField({ @SerializeField(clazz = ContentVo.class, includes = { "cmsContent", "operations", 
			"types", "status", "quote", "quoteChannelName", "createType", "currentNodeName", "channelId", "viewData" }),
			@SerializeField(clazz = Content.class, includes = { "id", "status", "title", "model", "top", 
					"topStartTime", "topEndTime", "createUser", "createTime", "contentTypes",
				"channelId", "copyName", "releaseTime", "channelName", "quoteChannel", "copyName", 
				"previewUrl", "urlWhole", "quoteChannelIds" }),
			@SerializeField(clazz = ContentType.class, includes = { "id", "typeName" }),
			@SerializeField(clazz = CmsModel.class, includes = { "id", "modelName" }),
			})
	public ResponseInfo page(HttpServletRequest request, @RequestBody ContentSearchDto dto)
			throws GlobalException {
		Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getSize());
		// 默认存入用户ID
		dto.setUserId(SystemContextUtils.getUserId(request));
		// 存入站点ID
		dto.setSiteId(SystemContextUtils.getSiteId(request));
		dto.setRecycle(false);
		GlobalConfig config = SystemContextUtils.getGlobalConfig(request);
		CoreUser user = SystemContextUtils.getUser(request);
		/** 系统开启了密级则附加内容密级查询条件 */
		if (config != null && config.getConfigAttr().getOpenContentSecurity()) {
			boolean flag = user.getUserSecret() != null
					&& !user.getUserSecret().getContentSecretIds().isEmpty();
			//前台筛选条件不为空，为空则取用户可查看的密级
			if (dto.getContentSecretIds() != null && dto.getContentSecretIds().length > 0) {
				dto.setSecret(true);
                //如果人员密级不为空
				if (flag) {
					//判断查询的密级是否包含其中
					List<Integer> result = Arrays.asList(dto.getContentSecretIds());
					Set<Integer> contentSecretIdSet = user.getUserSecret().getContentSecretIds();
					//判断用户内容密级是否包含前台传的密级
					if (!contentSecretIdSet.containsAll(result)) {
						//用户没有内容密级直接返回
						return new ResponseInfo();
					}
				} else {
					//用户没有内容密级直接返回
					return new ResponseInfo();
				}
			} else {
                /**用户查询没有密级参数，但是系统开启了密级，则dao setContentSecretIds 使用了密级查询结果 setSecret false 表示内容没有密级要求的内容数据 也查询出来*/
				dto.setSecret(false);
				if (flag) {
					Set<Integer> contentSecretIdSet = user.getUserSecret().getContentSecretIds();
					Integer[] ids = new Integer[contentSecretIdSet.size()];
					contentSecretIdSet.toArray(ids);
					dto.setContentSecretIds(ids);
				} else {
					dto.setContentSecretIds(new Integer[]{0});
				}
			}
		} else {
            /**不考虑密级查询*/
			dto.setSecret(false);
			dto.setContentSecretIds(null);
		}
		List<Integer> channelIdSet = new ArrayList<>();
		/** 设置用户有权限的栏目集合，列表查询的是有查看权限的栏目 ,有查询当前站点所有的栏目权限则不用传递当前所有权限栏目集合*/
		boolean viewCurrSiteAllChannel = user.getViewCurrSiteAllChannel();
		if(!viewCurrSiteAllChannel){
			channelIdSet = user.getViewContentChannelIds();
			if (channelIdSet.isEmpty()) {
				//文档类权限可查看栏目为空，直接返回
				return new ResponseInfo();
			}
		}
		Integer[] channelIds = null;
		if (dto.getChannelIds() != null && dto.getChannelIds().length > 0) {
			/** 检查栏目权限是否存在，只保留权限内的栏目 */
			List<Integer> queryChannelIdList = new ArrayList<>();
			for (Integer channelId : dto.getChannelIds()) {
				//判断是否有子栏目，有的话需要加进去
				List<Integer> channels = channelService.findById(channelId).getChildAllIds();
				queryChannelIdList.addAll(channels);
			}
			//求交集
			if(!viewCurrSiteAllChannel){
				queryChannelIdList.retainAll(channelIdSet);
			}
			int sum = queryChannelIdList.size();
			channelIds = new Integer[sum];
			channelIds = queryChannelIdList.toArray(channelIds);
			dto.setChannelIds(channelIds);
		} else {
			int sum = channelIdSet.size();
			channelIds = new Integer[sum];
			channelIds = channelIdSet.toArray(channelIds);
			dto.setChannelIds(channelIds);
		}
		if (CollectionUtils.isEmpty(dto.getStatus())) {
			dto.setStatus(ContentConstant.DEF_QUERY_STATUS);
		}
		Page<ContentVo> vos = contentGetService.getPage(dto, pageable);
		return new ResponseInfo(vos);
	}

	/**
	 * 获取到内容审核列表
	 * @Title: checkPage  
	 * @param request		request请求
	 * @param title			内容标题
	 * @param channelId		该栏目id
	 * @param pageable		分页对象
	 * @throws GlobalException   全局异常   
	 * @return: ResponseInfo
	 */
	@GetMapping(value = "/chack/page")
	@MoreSerializeField({ 
			@SerializeField(clazz = ContentVo.class, includes = { "cmsContent", "channelId" }),
			@SerializeField(clazz = Content.class, includes = { "id", "status", "title", "model", "createUser",
					"createTime", "channelId", "releaseTime", "channelName", "previewUrl", "urlWhole" }),
			@SerializeField(clazz = CmsModel.class, includes = { "id", "modelName" }) })
	public ResponseInfo checkPage(HttpServletRequest request, @RequestParam(required = false) String title,
			@RequestParam(required = false) Integer channelId, @RequestParam(required = false) Integer status,
			Pageable pageable) throws GlobalException {
		List<Integer> successStatus = ContentConstant.AUDIT_QUERY_STATUS;
		if (status != null&&!successStatus.contains(status)) {
			return new ResponseInfo();
		}
		ContentSearchDto dto = new ContentSearchDto(successStatus, ContentConstant.KEY_TYPE_TITLE, title, null,
				SystemContextUtils.getSiteId(request));
		dto.setRecycle(false);
		if (status != null) {
			dto.setStatus(Arrays.asList(status));
		}
		dto.setOrderType(ContentConstant.ORDER_TYPE_UPDATETIME_DESC);
		GlobalConfig config = SystemContextUtils.getGlobalConfig(request);
		CoreUser user = SystemContextUtils.getUser(request);
		/** 系统开启了密级则附加内容密级查询条件 */
		if (config != null && config.getConfigAttr().getOpenContentSecurity()) {
			boolean flag = user.getUserSecret() != null && !user.getUserSecret().getContentSecretIds().isEmpty();
			// 取用户可查看的密级
			if (flag) {
				Set<Integer> contentSecretIdSet = user.getUserSecret().getContentSecretIds();
				Integer[] ids = new Integer[contentSecretIdSet.size()];
				contentSecretIdSet.toArray(ids);
				dto.setContentSecretIds(ids);
			}
		}
		/** 设置用户有权限的栏目集合，列表查询的是有查看权限的栏目 */
		List<Integer> channelIdSet = new ArrayList<>();
		/** 设置用户有权限的栏目集合，列表查询的是有查看权限的栏目 ,有查询当前站点所有的栏目权限则不用传递当前所有权限栏目集合*/
		boolean viewCurrSiteAllChannel = user.getViewCurrSiteAllChannel();
		if(!viewCurrSiteAllChannel){
			channelIdSet = user.getViewContentChannelIds();
			if (channelIdSet.isEmpty()) {
				//文档类权限可查看栏目为空，直接返回
				return new ResponseInfo();
			}
		}
		Integer[] channelIds = null;
		if (channelId != null) {
			/** 检查栏目权限是否存在，只保留权限内的栏目 */
			List<Integer> queryChannelIdList = new ArrayList<>();
			Channel channel = channelService.findById(channelId);
			if (channel == null) {
				channelIds = new Integer[] {channelId};
			} else {
				List<Integer> channels = channel.getChildAllIds();
				queryChannelIdList.addAll(channels);
				// 求交集
				if(!viewCurrSiteAllChannel){
					queryChannelIdList.retainAll(channelIdSet);
				}
				int sum = queryChannelIdList.size();
				channelIds = new Integer[sum];
				channelIds = queryChannelIdList.toArray(channelIds);
			}
			dto.setChannelIds(channelIds);
		} else {
			int sum = channelIdSet.size();
			channelIds = new Integer[sum];
			channelIds = channelIdSet.toArray(channelIds);
			dto.setChannelIds(channelIds);
		}
		Page<ContentVo> vos = contentGetService.getPage(dto, pageable);
		return new ResponseInfo(vos);
	}
	
	@GetMapping(value = "/chack/amount")
	public ResponseInfo reviewContentNum(HttpServletRequest request, @RequestParam(required = false) Integer channelId) {
		CoreUser user = SystemContextUtils.getUser(request);
		GlobalConfig config = SystemContextUtils.getGlobalConfig(request);
		/** 系统开启了密级则附加内容密级查询条件 */
		if (config != null && config.getConfigAttr().getOpenContentSecurity()) {
			boolean flag = user.getUserSecret() != null && !user.getUserSecret().getContentSecretIds().isEmpty();
			// 取用户可查看的密级
			if (flag) {
				Set<Integer> contentSecretIdSet = user.getUserSecret().getContentSecretIds();
				Integer[] ids = new Integer[contentSecretIdSet.size()];
				contentSecretIdSet.toArray(ids);
			}
		}
		/** 设置用户有权限的栏目集合，列表查询的是有查看权限的栏目 */
		Integer[] channelIds = null;
		List<Integer> channelIdSet = new ArrayList<>();
		/** 设置用户有权限的栏目集合，列表查询的是有查看权限的栏目 ,有查询当前站点所有的栏目权限则不用传递当前所有权限栏目集合*/
		boolean viewCurrSiteAllChannel = user.getViewCurrSiteAllChannel();
		if(!viewCurrSiteAllChannel){
			channelIdSet = user.getViewContentChannelIds();
			if (channelIdSet.isEmpty()) {
				//文档类权限可查看栏目为空，直接返回
				return new ResponseInfo(0L);
			}
		}
		if (channelId != null) {
			/** 检查栏目权限是否存在，只保留权限内的栏目 */
			List<Integer> queryChannelIdList = new ArrayList<>();
			// 判断是否有子栏目，有的话需要加进去
			List<Integer> channels = channelService.findById(channelId).getChildAllIds();
			queryChannelIdList.addAll(channels);
			// 求交集
			if(!viewCurrSiteAllChannel){
				queryChannelIdList.retainAll(channelIdSet);
			}
			int sum = queryChannelIdList.size();
			channelIds = new Integer[sum];
			channelIds = queryChannelIdList.toArray(channelIds);
		} else {
			int sum = channelIdSet.size();
			channelIds = new Integer[sum];
			channelIds = channelIdSet.toArray(channelIds);
		}
		ContentSearchDto dto = new ContentSearchDto(SystemContextUtils.getSiteId(request), channelIds,
				Arrays.asList(ContentConstant.STATUS_SMART_AUDIT), null, null, false);
		return new ResponseInfo(contentGetService.getCount(dto));
	}
	
	/**
	 * 改变内容状态
	 * 
	 * @Title: changeStatus
	 * @Description:
	 * @param dto     传输对象
	 * @param result  结果
	 * @param request 请求
	 * @return ResponseInfo 返回对象
	 * @throws GlobalException 异常
	 */
	@PostMapping(value = "/status")
	public ResponseInfo changeStatus(@RequestBody BeatchDto dto, BindingResult result, HttpServletRequest request)
			throws GlobalException {
		super.validateBindingResult(result);
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setSiteId(siteId);
		dto.setCheckPerm(true);
		return contentService.changeStatus(dto,null,false);
	}

    /**
     * 内容操作
     *
     * @param dto     传输
     * @param result  检查
     * @param request 请求
     * @throws GlobalException 异常
     * @Title: changeStatus
     * @Description:
     */
    @PostMapping(value = "/operation")
    public ResponseInfo operation(@RequestBody OperationDto dto, BindingResult result, HttpServletRequest request)
            throws GlobalException {
        super.validateBindingResult(result);
        return contentService.operation(dto);
    }

    /**
     * 置顶操作
     *
     * @param dto     传输对象
     * @param result  检查
     * @param request 请求
     * @return ResponseInfo 结果
     * @throws GlobalException 异常
     * @Title: top
     * @Description: 完成
     */
    @PostMapping(value = "/top")
    public ResponseInfo top(@RequestBody OperationDto dto, BindingResult result, HttpServletRequest request)
            throws Exception {
        super.validateBindingResult(result);
        return contentService.top(dto);
    }

    /**
     * 取消置顶操作
     *
     * @param dto     传输对象
     * @param result  检查
     * @param request 请求
     * @return ResponseInfo 结果
     * @throws GlobalException 异常
     * @Title: notop
     * @Description: 完成
     */
    @PostMapping(value = "/notop")
    public ResponseInfo notop(@RequestBody OperationDto dto, BindingResult result, HttpServletRequest request)
            throws GlobalException {
        super.validateBindingResult(result);
        return contentService.notop(dto);
    }

    /**
     * 移动操作
     *
     * @param dto     传输对象
     * @param result  检查结果
     * @param request 请求
     * @throws GlobalException 异常
     * @Title: move
     * @Description:
     */
    @PostMapping(value = "/move")
    public ResponseInfo move(@RequestBody OperationDto dto, BindingResult result, HttpServletRequest request)
            throws GlobalException {
        super.validateBindingResult(result);
        return contentService.move(dto);
    }


    /**
     * 排序操作
     *
     * @param dto     传输对象
     * @param result  检查对象
     * @param request 请求
     * @return ResponseInfo 返回的对象
     * @throws GlobalException 异常
     * @Title: sort
     * @Description:
     */
    @PostMapping(value = "/sort")
    public ResponseInfo sort(@RequestBody OperationDto dto, BindingResult result, HttpServletRequest request)
            throws GlobalException {
        super.validateBindingResult(result);
        // 查询得到内容列表
        List<Content> contents = contentService.findAllById(dto.getIds());
        if (!contentService.validType(CmsDataPerm.OPE_CONTENT_ORDER, contents, null)) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    contentService.sort(dto);
                } catch (GlobalException e) {
                    e.printStackTrace();
                }
            }
        });
        return new ResponseInfo();
    }

    /**
     * 删除操作(加入回收站)
     *
     * @param dto     传输对象
     * @param result  检查对象
     * @param request 请求
     * @return
     * @throws GlobalException 异常
     * @Title: rubbish
     * @Description:
     */
    @PostMapping(value = "/rubbish")
    public ResponseInfo rubbish(@RequestBody OperationDto dto, BindingResult result, HttpServletRequest request)
            throws GlobalException {
        super.validateBindingResult(result);
        // 查询得到内容列表
        List<Content> contents = contentService.findAllByIdForCache(dto.getIds());
        // 判断操作类型
        if (!contentService.validType(CmsDataPerm.OPE_CONTENT_DEL, contents, null)) {
            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(), false);
        }
        contentService.rubbish(dto);
        return new ResponseInfo();
    }

    /**
     * 排序列表
     *
     * @param request 请求
     * @return ResponseInfo 返回对象
     * @throws GlobalException 异常
     * @Title: order
     */
    @GetMapping(value = "/orders")
    public ResponseInfo orders(HttpServletRequest request) throws GlobalException {
        Map<String, String> orderMap = ContentConstant.order();
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, String> ent : orderMap.entrySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", ent.getKey());
            jsonObject.put("value", ent.getValue());
            jsonArray.add(jsonObject);
        }
        return new ResponseInfo(jsonArray);
    }

    /**
     * 回收站
     *
     * @param request  请求
     * @param pageable 分页
     * @return ResponseInfo 返回实体
     * @throws GlobalException 异常
     * @Title: recycle
     */
    @GetMapping(value = "/recycle")
    @MoreSerializeField({@SerializeField(clazz = Content.class, includes = {"id", "title", "updateUser",
            "updateTime", "status"}),})
    public ResponseInfo recycle(HttpServletRequest request,
                                @PageableDefault(sort = "updateTime", direction = Direction.DESC) Pageable pageable)
            throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        Map<String, String[]> params = super.getCommonParams(request);
        // 选择回收站
        params.put("EQ_recycle_Boolean", new String[]{"true"});
        // 站点ID
        params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        return new ResponseInfo(contentService.getPage(params, pageable, false));
    }

	/**
	 * 删除内容
	 * 
	 * @Title: delete
	 * @param request 请求
	 * @param dto     传输对象
	 * @param result  结果
	 * @throws GlobalException 异常
	 * @return ResponseInfo 返回实体
	 */
    @PostMapping("/delete")
	public ResponseInfo delete(HttpServletRequest request, @RequestBody DeleteDto dto, BindingResult result)
			throws GlobalException {
		validateBindingResult(result);
		contentService.deleteContent(new ArrayList<>(Arrays.asList(dto.getIds())));
		return new ResponseInfo();
	}

    /**
     * 还原内容
     *
     * @param request 请求
     * @param dto     传输对象
     * @param result  检查对象
     * @return ResponseInfo 返回的实体
     * @throws GlobalException 异常
     * @Title: delete
     */
    @PutMapping()
    public ResponseInfo restore(HttpServletRequest request, @RequestBody DeleteDto dto, BindingResult result)
            throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(request);
        List<Integer> ids = Arrays.asList(dto.getIds());
        List<Integer> channelIds = new ArrayList<>();
        contentService.restore(ids, siteId, channelIds);
        return new ResponseInfo();
    }

    /**
     * 归档列表分页
     *
     * @param request  请求
     * @param dto      传输对象
     * @param pageable 分页
     * @return ResponseInfo 返回实体
     * @throws GlobalException 异常
     * @Title: pigeonhole
     */
    @PostMapping(value = "/pigeonhole")
    @MoreSerializeField({
            @SerializeField(clazz = Content.class, includes = {"id", "status", "title", "model", "top",
                    "topStartTime", "topEndTime", "createUser", "createTime", "contentTypes",
                    "updateTime", "copyName",
                    "updateUser", "channelId", "copyName", "releaseTime", "channelName", "quoteChannel"}),
            @SerializeField(clazz = ContentType.class, includes = {"id", "typeName"}),
            @SerializeField(clazz = CmsModel.class, includes = {"id", "modelName"}),
    })
    public ResponseInfo pigeonhole(HttpServletRequest request, @RequestBody ContentSearchDto dto, Pageable pageable) {
        //过滤数组, 前台传值所有会传0，需要过滤
        if (dto.getChannelIds() != null && dto.getChannelIds().length > 0) {
            Integer[] channels = Arrays.stream(dto.getChannelIds()).filter(x -> x != 0).toArray(Integer[]::new);
            dto.setChannelIds(channels);
        }
        // 默认存入用户ID
        dto.setUserId(SystemContextUtils.getUserId(request));
        // 默认内容状态为归档
        dto.setStatus(Arrays.asList(ContentConstant.STATUS_PIGEONHOLE));
        dto.setSiteId(SystemContextUtils.getSiteId(request));
        dto.setRecycle(false);
        return new ResponseInfo(contentGetService.getPages(dto, pageable));
    }

    /**
     * 出档
     *
     * @param request 请求
     * @param dto     传输对象
     * @param result  检查对象
     * @return ResponseInfo 返回的实体
     * @throws GlobalException 异常
     * @Title: page
     */
    @PostMapping(value = "/file")
    public ResponseInfo file(HttpServletRequest request, @RequestBody DeleteDto dto, BindingResult result)
            throws GlobalException {
        List<Content> contents = new ArrayList<>(10);
        List<Integer> ids = Arrays.asList(dto.getIds());
        List<Content> list = contentService.findAllById(ids);
        // 修改状态的同时，也需要修改contentChannel里面的状态值
        for (Content content : list) {
            // 出档设置为初稿状态
            content.setStatus(ContentConstant.STATUS_FIRST_DRAFT);
            contents.add(content);
        }
        contentService.batchUpdate(contents);
        return new ResponseInfo();
    }

    /**
     * 引用
     *
     * @param request 请求
     * @param dto     传输对象
     * @param result  检查对象
     * @return ResponseInfo 返回的实体
     * @throws GlobalException 异常
     * @Title: quote
     */
    @PostMapping(value = "/quote")
    public ResponseInfo quote(HttpServletRequest request, @RequestBody OperationDto dto, BindingResult result)
            throws GlobalException {
        // 查询得到内容列表
        List<Content> contents = contentService.findAllById(dto.getIds());
        // 判断权限
        for (Content content : contents) {
            List<Short> oprations = SystemContextUtils.getUser(request)
                    .getContentOperatorByChannelId(content.getChannelId());
            if (!oprations.contains(CmsDataPerm.OPE_CONTENT_QUOTE)) {
                throw new GlobalException(new SystemExceptionInfo(
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage(),
                        UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode()));
            }
        }
        // 查询得到内容列表
        List<Integer> channelids = contentGetService.getChannelIds(dto.getIds());
        // 得到引用栏目ID集合
        List<Integer> cids = dto.getChannelIds();
        // 检测是否引用内容原有的栏目
        for (Integer channelId : channelids) {
            if (cids.contains(channelId)) {
                throw new GlobalException(new SystemExceptionInfo(
                        ContentErrorCodeEnum.NO_QUOTE_CHANNEL_MYSELF_ERROR.getDefaultMessage(),
                        ContentErrorCodeEnum.NO_QUOTE_CHANNEL_MYSELF_ERROR.getCode()));
            }
        }

        final ReentrantLock reentrantLock = this.lock;
        reentrantLock.lock();
        try {
            contentService.quote(dto);
        } finally {
            reentrantLock.unlock();
        }
        return new ResponseInfo();
    }

	/**
	 * 取消引用
	 * 
	 * @Title: noquote
	 * @param request 请求
	 * @param dto     传输对象
	 * @param result  检查对象
	 * @throws GlobalException 异常
	 * @return ResponseInfo 返回的实体
	 */
	@PostMapping(value = "/noquote")
	public ResponseInfo noquote(HttpServletRequest request, @RequestBody OperationDto dto, BindingResult result)
			throws GlobalException {
		contentService.noquote(dto);
		return new ResponseInfo();
	}
	
	/**
	 * 微信推送预览
	 * 
	 * @Title: preview
	 * @param  dto  传输对象
	 * @throws GlobalException 异常
	 * @return ResponseInfo 返回的实体
	 */
	@PostMapping(value = "/preview")
	public ResponseInfo preview(@RequestBody WechatViewDto dto)
			throws GlobalException {
		return contentService.preview(dto);
	}
	
	/**
	 * 微信推送
	 * 
	 * @Title: preview
	 * @param  dto  传输对象
	 * @throws Exception 异常
	 * @return ResponseInfo 返回的实体
	 */
    @PostMapping(value = "/push")
    public ResponseInfo push(HttpServletRequest request, @RequestBody WechatPushDto dto)
            throws Exception {
        Integer userId = SystemContextUtils.getUserId(request);
        Integer siteId = SystemContextUtils.getSiteId(request);
        dto.setUserId(userId);
        dto.setSiteId(siteId);
        return contentService.push(dto);
    }
	
	/**
	 * 文档导入
	 * 
	 * @Title: preview
	 * @param  file  文件
	 * @param  type  类型
	 * @throws Exception 异常
	 * @return ResponseInfo 返回的实体
	 */
	@PostMapping(value = "/docImport")
	public ResponseInfo docImport(@RequestParam(value = "uploadFile", required = false) MultipartFile file,
			Integer type) throws Exception {
		if (type == null) {
			type = ContentConstant.IMPORT_TYPE_1;
		}
		//得到文件后缀
		String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase(Locale.ENGLISH);
		//判断后缀是否符合
		if (!ext.equalsIgnoreCase("doc") && !ext.equalsIgnoreCase("docx")) {
			return new ResponseInfo(ContentErrorCodeEnum.IMPORT_FORMAT_NOT_SUPPORTED.getCode(),
					ContentErrorCodeEnum.IMPORT_FORMAT_NOT_SUPPORTED.getDefaultMessage(), false);
		}
		String html = contentService.docImport(file, type);
		return new ResponseInfo(html);
	}
	
	/**
	 * 内容清空
	 * 
	 * @Title: clearAll
	 * @throws Exception 异常
	 * @return ResponseInfo 返回的实体
	 */
	@GetMapping(value = "/clearAll")
	public ResponseInfo clearAll(HttpServletRequest request) throws GlobalException {
		Integer siteId = SystemContextUtils.getSiteId(request);
		//得到全部回收站数据
		HashMap<String, String[]> params = new HashMap<>(1);
		params.put("EQ_recycle_Boolean", new String[] { "true" });
		params.put("EQ_siteId_Integer", new String[] { siteId.toString() });
		List<Content> list = contentService.getList(params, null, true);
		if (!list.isEmpty()) {
			List<Integer> ids = list.stream().map(Content::getId).collect(Collectors.toList());
			//删除内容
			contentService.deleteContent(ids);
		}
		List<Channel> channelList = channelService.getList(params, null, true);
		if (!channelList.isEmpty()) {
			List<Integer> ids = channelList.stream().map(Channel::getId).collect(Collectors.toList());
			channelService.delete(ids.toArray(new Integer[ids.size()]), true);
		}
		return new ResponseInfo();
	}
}