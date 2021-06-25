/*
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.auth;

import com.jeecms.admin.controller.BaseAdminController;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.CoreUserExt;
import com.jeecms.auth.domain.dto.CoreUserDto;
import com.jeecms.auth.domain.dto.CoreUserDto.One;
import com.jeecms.auth.domain.dto.CoreUserDto.Three;
import com.jeecms.auth.domain.dto.CoreUserDto.Two;
import com.jeecms.auth.domain.dto.PswDto;
import com.jeecms.auth.dto.CoreSafeManageDto;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.base.domain.ThirdPartyResultDTO;
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.manage.annotation.OperatingIntercept;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.security.PasswdService;
import com.jeecms.manage.service.ReinsuranceService;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.GlobalConfigAttr;
import com.jeecms.system.domain.dto.BeatchDto;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.SystemContextUtils;
import com.jeecms.wechat.domain.AbstractWeChatInfo;
import com.jeecms.wechat.service.AbstractWeChatInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 * 
 * @author ljw
 * @date 2019年4月9日
 * 
 */
@RestController
@RequestMapping("/users")
public class CoreUserController extends BaseAdminController<CoreUser, Integer> {

	@Autowired
	private CoreUserService coreUserService;
	@Autowired
	private GlobalConfigService globalConfigService;
	@Autowired
	private AbstractWeChatInfoService weChatInfoService;
	@Autowired
	private PasswdService passwdService;
	@Autowired
	private CmsOrgService cmsOrgService;
	@Autowired(required = false)
	private ReinsuranceService reinsuranceService;
    @Value("${spring.profiles.active}")
    private String active;

	private final transient ReentrantLock lock = new ReentrantLock();

	/**
	 * 条件查询列表
	 * 
	 * @Title: pageUser
	 * @param key:用户名/邮箱/电话/真名/座机
	 * @return ResponseInfo
	 */
	@GetMapping("/page")
	@MoreSerializeField({
			@SerializeField(clazz = CoreUser.class, includes = { "id", "username", "enabled", "createTime", "itself",
					"createUser", "roleNames", "orgName", "managerAble", "userExt", "notCurrUser", "deleteAble",
					"userSecretName" }),
			@SerializeField(clazz = CoreUserExt.class, includes = { "realname" }),})
	public ResponseInfo pageUser(HttpServletRequest request, Boolean enabled, String key, Integer orgid, Integer roleid,
			Integer userSecretId, Pageable pageable) {
		CoreUser user = SystemContextUtils.getUser(request);
		List<Integer> orgids = new ArrayList<>(1);
		List<Integer> roleids = new ArrayList<>(1);
		if (orgid != null) {
			orgids.add(orgid);
		} else {
			CmsOrg org = user.getOrg();
			orgids = Arrays.asList(org.getChildNodeIds());
		}
		if (roleid != null) {
			roleids.add(roleid);
		}
		return new ResponseInfo(coreUserService.pageUser(enabled, orgids, roleids, key, true,
				CoreUser.AUDIT_USER_STATUS_PASS, null, null, userSecretId, null, pageable));
	}

    /**
     * 分页查询三元管理用户
     *
     * @Title: pageUser
     * @param: key:用户名/真名
     * @return: ResponseInfo
     */
    @PostMapping("/safe/manage/page")
    @MoreSerializeField({
            @SerializeField(clazz = CoreUser.class, includes = {"id", "username", "enabled", "roleNames", "orgName", "managerAble", "userExt"}),
            @SerializeField(clazz = CoreUserExt.class, includes = {"realname"}),})
    public ResponseInfo pageSafeManageUser(HttpServletRequest request, @RequestBody CoreSafeManageDto dto, Pageable pageable) {
        pageable = PageRequest.of(dto.getPage() - 1, dto.getSize());
        CoreUser user = SystemContextUtils.getUser(request);
        List<Integer> orgids = new ArrayList<>();
        List<Integer> roleids = new ArrayList<>();
        if (dto.getOrgid() != null) {
            orgids.add(dto.getOrgid());
        } else {
            CmsOrg org = user.getOrg();
            orgids = Arrays.asList(org.getChildNodeIds());
        }
        if (dto.getRoleid() != null) {
            roleids.add(dto.getRoleid());
        }
        return new ResponseInfo(coreUserService.pageSafeManageUser(orgids, roleids, dto.getKey(), dto.getNotIds(), pageable));
    }
    /**
     * 条件查询列表
     *
     * @Title: pageUser
     * @param: key:用户名/真名
     * @return: ResponseInfo
     */
    @PostMapping("/reinsurance/page")
    @MoreSerializeField({
            @SerializeField(clazz = CoreUser.class, includes = {"id", "username", "enabled", "roleNames", "orgName", "managerAble", "userExt"}),
            @SerializeField(clazz = CoreUserExt.class, includes = {"realname"}),})
    public ResponseInfo pageReinsuranceAdmin(HttpServletRequest request, @RequestBody CoreSafeManageDto dto, Pageable pageable) {
        pageable = PageRequest.of(dto.getPage() - 1, dto.getSize());
        CoreUser user = SystemContextUtils.getUser(request);
        List<Integer> orgids = new ArrayList<>();
        List<Integer> roleids = new ArrayList<>();
        if (dto.getOrgid() != null) {
            orgids.add(dto.getOrgid());
        } else {
            CmsOrg org = user.getOrg();
            orgids = Arrays.asList(org.getChildNodeIds());
        }
        if (dto.getRoleid() != null) {
            roleids.add(dto.getRoleid());
        }
        return new ResponseInfo(coreUserService.pageSafeManageUser(orgids, roleids, dto.getKey(), dto.getNotIds(), pageable));
    }
	/**
	 * 管理员列表
	 * 
	 * @Title: pageUser
	 * @param: key:用户名/邮箱/电话/真名/座机
	 * @return: ResponseInfo
	 */
	@GetMapping("/list")
	@MoreSerializeField({ @SerializeField(clazz = CoreUser.class, includes = { "id", "username", "managerAble",
			"notCurrUser", "deleteAble" }), })
	public ResponseInfo list() {
		Map<String, String[]> map = new HashMap<>(3);
		map.put("EQ_admin_Boolean", new String[] { "true" });
		return new ResponseInfo(service.getList(map, null, true));
	}

	/**
	 * 管理员添加
	 * 
	 * @Title: saveUser
	 * @return: ResponseInfo
	 */
	@PostMapping
	@MoreSerializeField({ @SerializeField(clazz = CoreUser.class, includes = { "id" }) })
    @OperatingIntercept
	public ResponseInfo saveUser(@RequestBody @Validated(One.class) CoreUserDto coreUser, BindingResult result)
			throws Exception {
		super.validateBindingResult(result);
		ResponseInfo info = null;
		//处理并发
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			coreUser.setIsAdmin(true);
			GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
			// 查询人员是否开启密级，不开启无视前台传值
			if (!attr.getOpenContentSecurity()) {
				coreUser.setUserSecretId(null);
			}
			//是否开启双因子，开启双因子手机号必传
			if (attr.getElementOpen() && StringUtils.isBlank(coreUser.getUsePhone())) {
				return new ResponseInfo("-2", "手机号不能为空", false);
			}
			//如果父级站点已删除，则返回错误提示；
			CmsOrg org = cmsOrgService.findById(coreUser.getOrgid());
			if (org == null) {
				return new ResponseInfo(UserErrorCodeEnum.PARENT_ORG_NOT_EXIST.getCode(),
						UserErrorCodeEnum.PARENT_ORG_NOT_EXIST.getDefaultMessage());
			}
			//开启分保，管理员创建用户后交由安全员设置用户密级，只有安全员设置好密级后才能启用用户
			if (reinsuranceService != null && reinsuranceService.open()) {
				coreUser.setEnabled(false);
			}
			info = coreUserService.saveUser(coreUser);
		}finally {
			lock.unlock();
		}
		return info;
	}

	/**
	 * 更新用户
	 * 
	 * @Title: update
	 * @param: @param
	 *             models
	 * @param: @param
	 *             result
	 * @param: @return
	 * @return: ResponseInfo
	 */
	@PutMapping()
    @OperatingIntercept
	public ResponseInfo updateUser(@RequestBody @Validated(Two.class) CoreUserDto coreUser, BindingResult result)
			throws Exception {
		super.validateBindingResult(result);
		GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
		// 查询人员是否开启密级，不开启无视前台传值
		if (!attr.getOpenContentSecurity()) {
			coreUser.setUserSecretId(null);
		}
		//是否开启双因子，开启双因子手机号必传
		if (attr.getElementOpen() && StringUtils.isBlank(coreUser.getUsePhone())) {
			return new ResponseInfo("-2", "手机号不能为空", false);
		}
		//如果父级站点已删除，则返回错误提示；
		CmsOrg org = cmsOrgService.findById(coreUser.getOrgid());
		if (org == null) {
			return new ResponseInfo(UserErrorCodeEnum.PARENT_ORG_NOT_EXIST.getCode(),
					UserErrorCodeEnum.PARENT_ORG_NOT_EXIST.getDefaultMessage());
		}
		//开启分保，管理员修改用户后未设置密级就不能启用
		if (reinsuranceService != null && reinsuranceService.open()) {
			if (coreUser.getUserSecretId() == null) {
				coreUser.setEnabled(false);
			}
		}
		return coreUserService.updateUser(coreUser);
	}

	/**
	 * 获取单个用户
	 * 
	 * @Title: get
	 * @param: id
	 * @return: ResponseInfo
	 */
	@Override
	@GetMapping("/{id:[0-9]+}")
	@MoreSerializeField({
			@SerializeField(clazz = CoreUser.class, includes = { "id", "username", "orgId", "email", "enabled",
					"telephone", "createTime", "createUser", "org", "userExt", "roleNames", "orgName", "userSecretId",
					"roleIds", "managerAble", "notCurrUser", "deleteAble" }),
			@SerializeField(clazz = CmsOrg.class, includes = { "id", "nodeIds" }),
			@SerializeField(clazz = CoreUserExt.class, includes = { "realname", "linephone" }) })
	public ResponseInfo get(@NotNull @PathVariable(value = "id") Integer id) throws GlobalException {
		return super.get(id);
	}

	/**
	 * 删除管理员,根据可操作对象，判断是否可以操作 如果存在用户权限，删除用户则删除用户权限
	 * 
	 * @Title: delete
	 * @param: @param
	 *             ids
	 * @param: @return
	 * @param: @throws
	 *             GlobalException
	 * @return: ResponseInfo
	 */
    @PostMapping("/delete")
    @OperatingIntercept
	public ResponseInfo delete(HttpServletRequest request, @RequestBody @Valid BeatchDto dto, BindingResult result)
			throws GlobalException {
		super.validateBindingResult(result);
		CoreUser user = SystemContextUtils.getUser(request);
		return coreUserService.deleteUser(dto, user.getOrgId());
	}

	/**
	 * 后台管理用户重置密码
	 * 
	 * @Title: psw
	 * @param coreUser 传输DTO
	 * @throws GlobalException 异常
	 * @return: ResponseInfo 返回
	 */
	@PostMapping("/psw")
    @OperatingIntercept
	public ResponseInfo psw(HttpServletRequest request, @RequestBody CoreUserDto coreUser) throws GlobalException {
		CoreUser currUser = SystemContextUtils.getCoreUser();
		// 当前登录用户不能重置自己的密码
		if (currUser.getId().equals(coreUser.getId())) {
			return new ResponseInfo(UserErrorCodeEnum.PASSWORD_ERROR_RESET_MYSELF.getCode(),
					UserErrorCodeEnum.PASSWORD_ERROR_RESET_MYSELF.getDefaultMessage());
		}
		return coreUserService.psw(coreUser, true);
	}

	/**
	 * 自己修改密码
	 * 
	 * @Title: adminpsw
	 * @param coreUser
	 *            传输DTO
	 * @throws GlobalException
	 *             异常
	 * @return: ResponseInfo 返回
	 */
	@PostMapping("/adminpsw")
	public ResponseInfo adminpsw(HttpServletRequest request,
			@RequestBody @Validated(value = Three.class) CoreUserDto coreUser) throws GlobalException {
		CoreUser user = SystemContextUtils.getCoreUser();
		coreUser.setId(user.getId());
        /**演示站test密码不允许修改*/
        if (ServerModeEnum.demo.toString().equals(active) && "test".equals(user.getUsername())) {
            return new ResponseInfo(UserErrorCodeEnum.ACCOUNT_CREDENTIAL_ERROR.getCode(),
                    UserErrorCodeEnum.ACCOUNT_CREDENTIAL_ERROR.getDefaultMessage(), false);
        }
		// 原密码不能为空
		if (StringUtils.isBlank(coreUser.getOldpsw())) {
			// 密码为空
			return new ResponseInfo(UserErrorCodeEnum.PASSWORD_FORMAT_IS_INCORRECT.getCode(),
					UserErrorCodeEnum.PASSWORD_FORMAT_IS_INCORRECT.getDefaultMessage(), false);
		}
		// 解密旧密码
		String oldpStr = passwdService.decrypt(coreUser.getOldpsw());
		// 判断输入的旧密码是否匹配
		if (!passwdService.isPasswordValid(oldpStr, user.getSalt(), user.getPassword())) {
			return new ResponseInfo(UserErrorCodeEnum.ACCOUNT_CREDENTIAL_ERROR.getCode(),
					UserErrorCodeEnum.ACCOUNT_CREDENTIAL_ERROR.getDefaultMessage(), false);
		}
		return coreUserService.psw(coreUser, false);
	}

	/**
	 * 启用用户
	 * 
	 * @Title: enableCoreUser
	 * @param dto
	 *            批量操作Dto
	 * @throws GlobalException
	 *             异常
	 * @return: ResponseInfo 返回
	 */
	@PostMapping("/on")
	public ResponseInfo enableCoreUser(HttpServletRequest request, HttpServletResponse response,
			@RequestBody @Valid BeatchDto dto, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		return coreUserService.enableUser(dto);
	}

	/**
	 * 禁用用户
	 * 
	 * @Title: disableCoreUser
	 * @param dto
	 *            批量操作Dto
	 * @throws GlobalException
	 *             异常
	 * @return: ResponseInfo 返回
	 */
	@PostMapping("/off")
	public ResponseInfo disableCoreUser(HttpServletRequest request, HttpServletResponse response,
			@RequestBody @Valid BeatchDto dto, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		return coreUserService.disableUser(dto);
	}

	/**
	 * 验证用户名
	 * 
	 * @param: validName
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/username/unique")
	public ResponseInfo username(@NotNull String username) {
		Boolean flag = coreUserService.validName(username);
		return new ResponseInfo(flag);
	}

	/**
	 * 验证邮箱
	 * 
	 * @param: validMail
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/mail/unique")
	public ResponseInfo mail(String mail, Integer id) {
		Boolean flag = coreUserService.validMail(mail, id);
		return new ResponseInfo(flag);
	}

	/**
	 * 验证手机
	 * 
	 * @param: validMail
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/phone/unique")
	public ResponseInfo validMail(String phone, Integer id) {
		Boolean flag = coreUserService.validPhone(phone, id);
		return new ResponseInfo(flag);
	}

	/**
	 * 验证密码
	 * 
	 * @Title: validPsd
	 * @return: ResponseInfo
	 */
	@PostMapping(value = "/pwd/unique")
	public ThirdPartyResultDTO validPsd(@RequestBody PswDto dto) throws GlobalException {
		String psw = dto.getPsw();
		String username = dto.getUsername();
		if (StringUtils.isNotBlank(psw)) {
			return coreUserService.validPwd(psw, username);
		}
		return new ThirdPartyResultDTO(true);
	}

	/**
	 * 第三方管理员分页列表
	 */
	@GetMapping("/third/manager/page")
	@MoreSerializeField({
			@SerializeField(clazz = CoreUser.class, includes = { "id", "userExt", "username", "org", "roleNames" }),
			@SerializeField(clazz = CoreUserExt.class, includes = { "realname" }),
			@SerializeField(clazz = CmsOrg.class, includes = { "name" }), })
	public ResponseInfo thirdManagerPage(@RequestParam(value = "orgId", required = false) Integer orgId,
			@RequestParam(value = "roleid", required = false) Integer roleid,
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "appId") String appId, @RequestParam(value = "isWechat") boolean isWechat,
			Pageable pageable) {
		Page<CoreUser> coreUserPage = null;
		// 如果传入的是微信公众号列表
		if (isWechat) {
			AbstractWeChatInfo info = weChatInfoService.findAppId(appId);
			List<CoreUser> coreUsers = info.getUsers();
			List<Integer> ids = null;
            /**查询可设置的管理员列表（特排除已设置的管理员），所以查询条件是not in 已选择的id*/
			if (coreUsers != null && !coreUsers.isEmpty()) {
				ids = coreUsers.stream().map(CoreUser::getId).collect(Collectors.toList());
			}
			coreUserPage = coreUserService.pageThirdManager(orgId, roleid, username, pageable, ids);
		}
		return new ResponseInfo(coreUserPage);
	}

}
