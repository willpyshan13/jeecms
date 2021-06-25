/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.member.controller;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.CoreUserExt;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.annotation.EncryptMethod;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.HibernateProxyUtil;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.*;
import com.jeecms.content.domain.dto.ContentContributeDto;
import com.jeecms.content.domain.dto.ContentContributeDto.DeleteContribute;
import com.jeecms.content.domain.dto.ContentContributeDto.SaveContribute;
import com.jeecms.content.domain.dto.ContentContributeDto.UpdateContribute;
import com.jeecms.content.domain.vo.ContentContributeVo;
import com.jeecms.content.domain.vo.ContentFrontVo;
import com.jeecms.content.service.CmsModelService;
import com.jeecms.content.service.ContentFrontService;
import com.jeecms.content.service.ContentService;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.interact.service.UserCommentService;
import com.jeecms.member.domain.MemberGroup;
import com.jeecms.member.domain.dto.MemberEmailDto;
import com.jeecms.member.domain.dto.MemberPwdDto;
import com.jeecms.member.domain.dto.MemberRegisterDto;
import com.jeecms.member.domain.dto.MobileMemberDto;
import com.jeecms.member.domain.vo.front.MemberLikeVo;
import com.jeecms.member.domain.vo.front.MemberMoblieLikeVo;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.SysUserSecret;
import com.jeecms.universal.service.SendSmsUtilService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.jeecms.common.exception.error.UserErrorCodeEnum.VALIDATE_CODE_UNTHROUGH;
import static com.jeecms.system.domain.dto.ValidateCodeConstants.*;

/**
 * PC会员管理controller
 *
 * @author ljw
 * @version 1.0
 * @date 2019/7/18
 */
@RestController
@RequestMapping("/memberinfo")
@Validated
public class MemberController extends BaseController<CoreUser, Integer> {

    @Autowired
    private CoreUserService coreUserService;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private SendSmsUtilService sendSmsUtilService;
    @Autowired
    private CacheProvider cacheProvider;
    @Autowired
    private ContentFrontService contentFrontService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private UserCommentService userCommentService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;

    /**
     * 获取用户的个人信息
     *
     * @param request request
     * @return return
     * @throws GlobalException 异常
     */
    @GetMapping()
    @MoreSerializeField({
            @SerializeField(clazz = ResourcesSpaceData.class, includes = {"id", "url"}),})
    public ResponseInfo getInfo(HttpServletRequest request) throws GlobalException {
        Integer userId = SystemContextUtils.getUserId(request);
        return new ResponseInfo(coreUserService.getPCMemberInfo(userId));
    }

    /**
     * 修改用户个人基本信息
     *
     * @param dto     会员基本信息Dto
     * @param request request
     * @return ResponseInfo
     */
    @PutMapping()
    public ResponseInfo update(@RequestBody MemberRegisterDto dto,
                               HttpServletRequest request) throws Exception {
        Integer userId = SystemContextUtils.getUserId(request);
        Integer siteId = SystemContextUtils.getSiteId(request);
        dto.setId(userId);
        dto.setSiteId(siteId);
        return coreUserService.updatePCMember(dto);
    }

    /**
     * 修改用户密码
     *
     * @param memberInfoDto 会员修改密码Dto
     * @param request       request
     * @return return
     */
    @PostMapping("/pstr")
    public ResponseInfo updatePStr(@RequestBody @Valid MemberPwdDto memberInfoDto, HttpServletRequest request, BindingResult result)
            throws GlobalException {
        super.validateBindingResult(result);
        Integer userId = SystemContextUtils.getUserId(request);
        return coreUserService.updatePStr(memberInfoDto, userId);
    }

    /**
     * 修改用户注册邮箱
     *
     * @param dto     会员修改邮箱Dto
     * @param request request
     * @return ResponseInfo
     * @throws GlobalException 异常
     */
    @PostMapping("/email")
    public ResponseInfo updateEmail(@RequestBody MemberEmailDto dto, HttpServletRequest request)
            throws GlobalException {
        Integer userId = SystemContextUtils.getUserId(request);
        CoreUser bean = service.findById(userId);
        //判断是否需要验证
        CmsModel model = cmsModelService.getFrontMemberModel();
        CmsModelItem item = model.getItem(CmsModelConstant.FIELD_MEMBER_EMAIL);
        JSONObject obj = item.getContentObj();
        JSONObject valueObje = obj.getJSONObject(CmsModelItem.VALUE);
        //判断是否需要验证
        Boolean flag = valueObje.getBoolean("isSmsVerification");
        if (flag) {
            // 验证新邮箱
            if (StringUtils.isNotBlank(dto.getNewEmail())) {
                String sessionKey = WebConstants.KCAPTCHA_PREFIX
                        + CODE_SECOND_LEVEL_IDENTITY_VALIDATE_USER_INFOR + bean.getEmail();
                int status = sendSmsUtilService.validateCodeSpec(sessionKey, dto.getCode(), true);
                if (STATUS_PASS != status) {
                    return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                            VALIDATE_CODE_UNTHROUGH.getDefaultMessage(), false);
                }
                String sessionKeyOld = WebConstants.KCAPTCHA_PREFIX
                        + CODE_SECOND_LEVEL_IDENTITY_NEW_VALIDATE_USER_INFOR + dto.getNewEmail();
                int status1 = sendSmsUtilService.validateCodeSpec(sessionKeyOld, dto.getNewEmailCode(), true);
                if (STATUS_PASS != status1) {
                    return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                            VALIDATE_CODE_UNTHROUGH.getDefaultMessage(), false);
                }
                // 修改成功后从服务端将验证码手动删除
                cacheProvider.clearCache(CacheConstants.SMS, sessionKey);
                // 注册成功后从服务端将验证码手动删除
                cacheProvider.clearCache(CacheConstants.SMS, sessionKeyOld);
                Boolean boolean1 = coreUserService.validMail(dto.getNewEmail(), userId);
                if (!boolean1) {
                    return new ResponseInfo(UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getCode(),
                            UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getDefaultMessage(), false);
                }
                bean.setEmail(dto.getNewEmail());
            }
        } else {
            if (StringUtils.isNotBlank(dto.getNewEmail())) {
                bean.setEmail(dto.getNewEmail());
            }
        }
        service.update(bean);
        return new ResponseInfo();
    }

    /**
     * 是否可以投稿（前台用户-投稿按钮-是否显示）
     *
     * @param request request请求对象
     * @Title: isContribute
     * @return: ResponseInfo
     */
    @GetMapping("/isContribute")
    public ResponseInfo isContribute(HttpServletRequest request) {
        boolean isContribute = true;
        CoreUser coreUser = SystemContextUtils.getCoreUser();
        // 获取到的对象为空则不允许投稿
        if (coreUser == null) {
            return new ResponseInfo(false);
        }
        if (!coreUser.getAdmin()) {
            MemberGroup memberGroup = coreUser.getUserGroup();
            Integer siteId = SystemContextUtils.getSiteId(request);
            // 如果获取到的对象不为空，则判断该对象是否有栏目组，没有的话不允许投稿，有的话判断该用户组是否有一个或多个栏目投稿的权限
            if (memberGroup != null) {
                if (!memberGroup.getIsAllChannelContribute()) {
                    List<Channel> contributeChannels = memberGroup.getContributeChannels();
                    if (contributeChannels != null && contributeChannels.size() > 0) {
                        contributeChannels = contributeChannels.stream()
                                .filter(
                                        channel -> channel.getSiteId().equals(siteId)
                                                &&
                                                channel.getRecycle().equals(false)).collect(Collectors.toList());
                        if (contributeChannels == null || contributeChannels.size() == 0) {
                            isContribute = false;
                        }
                    } else {
                        isContribute = false;
                    }
                }
            } else {
                isContribute = false;
            }
        }
        return new ResponseInfo(isContribute);
    }

	/**
	 * 用户投稿
	 *
	 * @param dto 投稿接受dto
	 * @throws GlobalException 全局异常
	 * @Title: contribute
	 * @return: ResponseInfo
	 */
	@PostMapping("/contribute")
    @EncryptMethod
	public ResponseInfo contribute(@RequestBody @Validated(SaveContribute.class) ContentContributeDto dto) throws GlobalException {
		contentFrontService.contribute(dto);
		return new ResponseInfo(true);
	}

    /**
     * 获取用户投稿信息
     *
     * @param status    状态：1. 待审核，2. 暂存，3. 已发布
     * @param title     内容标题
     * @param startDate 起始时间
     * @param endDate   截止时间
     * @param pageable  分页对象
     * @param request   request请求对象
     * @Title: getContribute
     * @return: ResponseInfo
     */
    @GetMapping("/contribute/page")
    @MoreSerializeField({
            @SerializeField(clazz = Content.class, includes = {"id", "title", "channel", "createTime", "status", "url"}),
            @SerializeField(clazz = Channel.class, includes = {"name"})
    })
    public ResponseInfo getContributePage(@RequestParam(required = false) Integer status, @RequestParam(required = false) String title,
                                          @RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate, Pageable pageable, HttpServletRequest request) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        CoreUser user = SystemContextUtils.getUser(request);
        if (user == null) {
            return new ResponseInfo();
        }
        if (startDate != null) {
            startDate = MyDateUtils.getStartDate(startDate);
        }
        if (endDate != null) {
            endDate = MyDateUtils.getFinallyDate(endDate);
        }
        List<Integer> contentSecretIds = null;
        SysUserSecret userSecret = user.getUserSecret();
        if (userSecret != null) {
            contentSecretIds = new ArrayList<>(userSecret.getContentSecretIds());
        }
        Page<Content> contentPage = contentFrontService.getPage(siteId, user.getId(), status, title, startDate,
                endDate, contentSecretIds, pageable);
        return new ResponseInfo(contentPage);
    }


    /**
     * 手机端分页查询投稿列表
     *
     * @param status   状态：1.待审核、2.已发布
     * @param pageable 分页详情
     * @param request
     * @throws GlobalException
     * @Title: getContributePage
     * @return: ResponseInfo
     */
    @GetMapping("/mobile/contribute/page")
    @MoreSerializeField({
            @SerializeField(clazz = ContentAttr.class, includes = {"resourcesSpaceData", "contentAttrRes"}),
            @SerializeField(clazz = ContentAttrRes.class, includes = {"resourcesSpaceData"}),
            @SerializeField(clazz = ResourcesSpaceData.class, includes = {"resourceType", "dimensions", "url",
                    "resourceDate", "suffix"})
    })
    public ResponseInfo getContributePage(@Range(min = 1, max = 2, message = "类型只有1或者2") @RequestParam Integer status,
                                          Pageable pageable, HttpServletRequest request) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        CoreUser user = SystemContextUtils.getUser(request);
        if (user == null) {
            return new ResponseInfo();
        }
        List<Integer> contentSecretIds = null;
        SysUserSecret userSecret = user.getUserSecret();
        if (userSecret != null) {
            contentSecretIds = new ArrayList<>(userSecret.getContentSecretIds());
        }
        Page<ContentFrontVo> contentPage = contentFrontService.getMobilePage(siteId, user.getId(), status, contentSecretIds, pageable);
        return new ResponseInfo(contentPage);
    }


    /**
     * 查询单个投稿内容
     *
     * @param contentId 投稿内容id值
     * @param request   request对象
     * @throws GlobalException 全局异常
     * @Title: getContribute
     * @return: ResponseInfo
     */
    @GetMapping("/contribute/{contentId:[0-9]+}")
    @MoreSerializeField({
            @SerializeField(clazz = Channel.class, includes = {"id", "name"}),
            @SerializeField(clazz = ContentContributeVo.class, excludes = {"payread", "payPrice", "trialReading"}),
    })
    public ResponseInfo getContribute(@PathVariable(name = "contentId") Integer contentId, HttpServletRequest request) {
        Integer userId = SystemContextUtils.getUserId(request);
        if (userId == null) {
            return new ResponseInfo();
        }
        return new ResponseInfo(contentFrontService.splicContributeVo(contentId, userId));
    }

    final transient ReentrantLock lock = new ReentrantLock();
	/**
	 * 用户投稿修改
	 *
	 * @param dto 投稿修改接收dto
	 * @throws GlobalException 全局异常
	 * @Title: contribute
	 * @return: ResponseInfo
	 */
	@PutMapping("/contribute")
    @EncryptMethod
	public ResponseInfo updateContribute(@RequestBody @Validated(UpdateContribute.class) ContentContributeDto dto, HttpServletRequest request) throws GlobalException {
		Channel channel = channelService.findById(dto.getChannnelId());
		if (channel == null) {
			return new ResponseInfo();
		}
        Content content = null;
        lock.lock();
        try {
            content = contentService.findById(dto.getContentId());
            if (content == null || content.getRecycle()) {
                return new ResponseInfo();
            }
            if (!content.getStatus().equals(ContentConstant.STATUS_TEMPORARY_STORAGE)) {
                return new ResponseInfo(
                        ContentErrorCodeEnum.CONTENT_NOT_IS_TEMPORARY_STORAGE.getCode(),
                        ContentErrorCodeEnum.CONTENT_NOT_IS_TEMPORARY_STORAGE.getDefaultMessage());
            }
        } finally {
            lock.unlock();
        }
        Content finalContent = content;
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                HibernateProxyUtil.loadHibernateProxy(finalContent.getContentExt());
                try {
                    contentFrontService.updateContribute(dto, channel, finalContent, request, finalContent.getContentExt());
                } catch (GlobalException e) {
                    e.printStackTrace();
                }
            }
        });
        return new ResponseInfo(true);
    }

	/**
	 * 删除暂存的投稿数据
	 *
	 * @param dto 删除dto
	 * @throws GlobalException 全局异常
	 * @Title: deleteContribute
	 * @return: ResponseInfo
	 */
	@PostMapping("/contribute/delete")
	public ResponseInfo deleteContribute(@RequestBody @Validated(DeleteContribute.class) ContentContributeDto dto, HttpServletRequest request) throws GlobalException {
        lock.lock();
        try {
            Content content = contentService.findById(dto.getContentId());
            if (content == null || !content.getStatus().equals(ContentConstant.STATUS_TEMPORARY_STORAGE)) {
                return new ResponseInfo();
            }
            Integer userId = SystemContextUtils.getUserId(request);
            if (userId != null && !content.getRecycle() && userId.equals(content.getUserId())) {
                contentService.delete(content);
            }
        } finally {
            lock.unlock();
        }
        return new ResponseInfo();
    }

    /**
     * 我的点赞分页
     *
     * @param request  请求
     * @param pageable 分页
     * @throws GlobalException 异常
     * @Title: like
     */
    @GetMapping("/like")
    public ResponseInfo like(HttpServletRequest request, Pageable pageable)
            throws GlobalException {
        //得到VOs
        List<MemberLikeVo> vos = new ArrayList<MemberLikeVo>(10);
        CoreUser coreUser = SystemContextUtils.getCoreUser();
        List<UserComment> userComments = coreUser.getLikeComments();
        List<Content> usContents = coreUser.getLikeContents();
        if (!userComments.isEmpty()) {
            for (UserComment comment : userComments) {
                MemberLikeVo vo = new MemberLikeVo();
                vo.setType(MemberLikeVo.TYPE_1);
                vo.setComment(comment.getCommentText());
                vo.setCommentId(comment.getId());
                vo.setTitle(comment.getContent().getTitle());
                vo.setContentUrl(comment.getContent().getUrlWhole());
                vos.add(vo);
            }
        }
        if (!usContents.isEmpty()) {
            for (Content content : usContents) {
                MemberLikeVo vo = new MemberLikeVo();
                vo.setType(MemberLikeVo.TYPE_2);
                vo.setContentId(content.getId());
                vo.setTitle(content.getTitle());
                vo.setContentUrl(content.getUrlWhole());
                vos.add(vo);
            }
        }
        PageImpl<MemberLikeVo> page = null;
        if (!vos.isEmpty()) {
            vos = vos.stream()
                    .skip(pageable.getPageSize() * (pageable.getPageNumber()))
                    .limit(pageable.getPageSize()).collect(Collectors.toList());
            page = new PageImpl<MemberLikeVo>(vos, pageable, vos.size());
        } else {
            page = new PageImpl<MemberLikeVo>(vos, pageable, vos.size());
        }
        return new ResponseInfo(page);
    }


	/**
	 * 取消点赞
	 *
	 * @param id   标识
	 * @param type 1.取消点赞评论 2.取消点赞内容
	 * @throws GlobalException 异常
	 * @Title: quitlike
	 */
	@GetMapping("/quitlike")
	public ResponseInfo quitlike(Integer id, Integer type) throws GlobalException {
		CoreUser coreUser = SystemContextUtils.getCoreUser();
		if (MemberLikeVo.TYPE_1.equals(type)) {
			List<UserComment> userComments = coreUser.getLikeComments();
			//直接过滤ID相同的对象
			userComments = userComments.stream().filter(x -> !x.getId().equals(id)).collect(Collectors.toList());
			coreUser.setLikeComments(userComments);
			UserComment comment = userCommentService.findById(id);
			comment.setUpCount(comment.getUpCount() - 1);
			userCommentService.update(comment);
		} else {
			List<Content> usContents = coreUser.getLikeContents();
			usContents = usContents.stream().filter(x -> !x.getId().equals(id)).collect(Collectors.toList());
			coreUser.setLikeContents(usContents);
			contentFrontService.saveOrUpdateNum(id, null, ContentConstant.CONTENT_NUM_TYPE_UPS, true);
		}
		coreUserService.update(coreUser);
		return new ResponseInfo();
	}
	
	/**
	 * 修改用户个人系统信息
	 *
	 * @param dto    会员基本信息Dto
	 * @param request request
	 * @return ResponseInfo
	 */
	@PutMapping("/system")
	public ResponseInfo updateSystem(@RequestBody  MobileMemberDto dto,
			HttpServletRequest request) throws Exception {
		Integer userId = SystemContextUtils.getUserId(request);
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setId(userId);
		dto.setSiteId(siteId);
		return coreUserService.updateMobileSysMember(dto);
	}
	
	/**
	 * 修改用户个人自定义信息
	 *
	 * @param dto    会员基本信息Dto
	 * @param request request
	 * @return ResponseInfo
	 */
	@PutMapping("/custom")
	public ResponseInfo updateCustom(@RequestBody  MobileMemberDto dto,
			HttpServletRequest request) throws Exception {
		Integer userId = SystemContextUtils.getUserId(request);
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setId(userId);
		dto.setSiteId(siteId);
		return coreUserService.updateMobileCustomMember(dto);
	}
	
	/**
	 * 手机端我的点赞分页
	 *
	 * @param request  请求
	 * @param pageable 分页
	 * @throws GlobalException 异常
	 * @Title: like
	 */
	@GetMapping("/moblielike")
	@MoreSerializeField({
		@SerializeField(clazz = ContentFrontVo.class, excludes = {"titleIsBold","titleColor", 
		}),
		@SerializeField(clazz = ContentAttr.class, includes = {"resourcesSpaceData","contentAttrRes"}),
        @SerializeField(clazz = ContentAttrRes.class, includes = {"resourcesSpaceData"}),
		@SerializeField(clazz = ResourcesSpaceData.class, includes = {"resourceType","dimensions","url",
				"resourceDate","suffix"})
	})
	public ResponseInfo moblielike(HttpServletRequest request, Pageable pageable)
			throws GlobalException {
		//得到VOs
		List<MemberMoblieLikeVo> vos = new ArrayList<MemberMoblieLikeVo>(10);
		List<UserComment> replyComments = new ArrayList<UserComment>(16);
		CoreUser coreUser = SystemContextUtils.getCoreUser();
		//我点赞评论
		List<UserComment> userComments = coreUser.getLikeComments();
		if (!userComments.isEmpty()) {
			List<Integer> list = userComments.stream().map(UserComment::getId).collect(Collectors.toList());
			//得到评论的评论数
			replyComments = userCommentService.getListByReplyCommentId(list);
		}
		//点赞内容
		List<Content> usContents = coreUser.getLikeContents();
		if (!userComments.isEmpty()) {
			for (UserComment comment : userComments) {
				MemberMoblieLikeVo vo = new MemberMoblieLikeVo();
				//回复ID为空，则为评论，反之为回复
				if (comment.getReplyCommentId() != null) {
					vo.setCommentType(MemberMoblieLikeVo.COMMENT_TYPE_2);
					vo.setReplyUsername(comment.getReplyComment().getUser() != null 
							? comment.getReplyComment().getUser().getUsername()
									: "");
				} else {
					vo.setCommentType(MemberMoblieLikeVo.COMMENT_TYPE_1);
				}
				vo.setUps(comment.getUpCount() == null ? 0 : comment.getUpCount());
				vo.setUsername(comment.getUser() != null 
						? comment.getUser().getUsername()
						: "");
				vo.setHeadImage(comment.getUser() != null 
						? comment.getUser().getHeadImage()
								: "");
				vo.setTime(comment.getDistanceTime());
				vo.setType(MemberMoblieLikeVo.TYPE_1);
				vo.setComment(comment.getCommentText());
				vo.setCommentId(comment.getId());
				if (replyComments.isEmpty()) {
					vo.setCommentSum(0);
				} else {
					Long sumLong = replyComments.stream().filter(x ->x.getReplyCommentId()
							.equals(comment.getId())).count();
					vo.setCommentSum(sumLong.intValue());
				}
				ContentFrontVo moblie = contentFrontService.initMobileVo(new ContentFrontVo(), 
						comment.getContent());
				vo.setMobileContent(moblie);
				vos.add(vo);
			}
		}
		if (!usContents.isEmpty()) {
			for (Content content : usContents) {
				MemberMoblieLikeVo vo = new MemberMoblieLikeVo();
				vo.setType(MemberMoblieLikeVo.TYPE_2);
				vo.setCommentSum(content.getComments() != null ? content.getComments() : 0);
				ContentFrontVo moblie = contentFrontService.initMobileVo(new ContentFrontVo(), content);
				vo.setMobileContent(moblie);
				vos.add(vo);
			}
		}
		PageImpl<MemberMoblieLikeVo> page = null;
		if (!vos.isEmpty()) {
			//排序
			vos = vos.stream().sorted(Comparator.comparing(MemberMoblieLikeVo::getCommentSum))
					.collect(Collectors.toList());
			vos = vos.stream()
					.skip(pageable.getPageSize() * (pageable.getPageNumber()))
					.limit(pageable.getPageSize()).collect(Collectors.toList());
			page = new PageImpl<MemberMoblieLikeVo>(vos, pageable, vos.size());
		} else {
			page = new PageImpl<MemberMoblieLikeVo>(vos, pageable, vos.size());
		}
		return new ResponseInfo(page);
	}

    /**
     * 修改用户头像
     *
     * @param pic     图片ID
     * @param request request
     * @return ResponseInfo
     * @since x1.9.0
     */
    @GetMapping("/pic")
    public ResponseInfo updatePic(HttpServletRequest request, @NotNull @RequestParam(value = "pic") Integer pic)
            throws GlobalException {
        Integer userId = SystemContextUtils.getUserId(request);
        if (userId != null) {
            CoreUser user = coreUserService.findById(userId);
            CoreUserExt ext = user.getUserExt();
            //会员定制新增字段
            ext.setUserImgId(pic);
            ext.setResourcesSpaceData(resourcesSpaceDataService.findById(pic));
            user.setUserExt(ext);
            coreUserService.update(user);
        }
        return new ResponseInfo();
    }

}
