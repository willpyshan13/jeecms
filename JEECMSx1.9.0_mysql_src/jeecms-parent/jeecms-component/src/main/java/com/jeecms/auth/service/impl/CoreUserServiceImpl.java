/**
 * * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.auth.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.constants.AuthConstant.LoginFailProcessMode;
import com.jeecms.auth.dao.CoreUserDao;
import com.jeecms.auth.domain.*;
import com.jeecms.auth.domain.dto.CoreMenuDto.RoutingConstant;
import com.jeecms.auth.domain.dto.CoreUserDto;
import com.jeecms.auth.domain.dto.UserManagerDto;
import com.jeecms.auth.domain.vo.*;
import com.jeecms.auth.service.CoreMenuService;
import com.jeecms.auth.service.CoreRoleService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.base.domain.ThirdPartyResultDTO;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.RPCErrorCodeEnum;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.exception.error.SysOtherErrorCodeEnum;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.security.Digests;
import com.jeecms.common.security.PasswdService;
import com.jeecms.common.util.CollectionUtil;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.common.wechat.Const;
import com.jeecms.common.wechat.util.client.HttpUtil;
import com.jeecms.constants.MessageTplCodeConstants;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.CmsModelItem;
import com.jeecms.content.service.CmsModelService;
import com.jeecms.member.domain.MemberAttr;
import com.jeecms.member.domain.MemberGroup;
import com.jeecms.member.domain.MemberLevel;
import com.jeecms.member.domain.MemberScoreDetails;
import com.jeecms.member.domain.dto.MemberPwdDto;
import com.jeecms.member.domain.dto.MemberRegisterDto;
import com.jeecms.member.domain.dto.MobileMemberDto;
import com.jeecms.member.domain.dto.PcMemberDto;
import com.jeecms.member.domain.vo.MemberInfoVo;
import com.jeecms.member.service.MemberAttrService;
import com.jeecms.member.service.MemberGroupService;
import com.jeecms.member.service.MemberLevelService;
import com.jeecms.member.service.MemberScoreDetailsService;
import com.jeecms.message.MqConstants;
import com.jeecms.message.MqSendMessageService;
import com.jeecms.message.dto.CommonMqConstants;
import com.jeecms.message.dto.CommonMqConstants.MessageSceneEnum;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.domain.dto.ResourcesSpaceShareDto;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.sso.dto.request.SyncDeleteUser;
import com.jeecms.sso.dto.request.SyncDeleteUserDto;
import com.jeecms.sso.dto.request.SyncRequestUser;
import com.jeecms.sso.dto.response.SyncResponseVo;
import com.jeecms.sso.service.CmsSsoService;
import com.jeecms.system.domain.*;
import com.jeecms.system.domain.dto.BeatchDto;
import com.jeecms.system.exception.SiteExceptionInfo;
import com.jeecms.system.job.factory.JobFactory;
import com.jeecms.system.service.*;
import com.jeecms.universal.service.SendSmsUtilService;
import com.jeecms.util.SystemContextUtils;
import com.jeecms.wechat.domain.AbstractWeChatInfo;
import com.jeecms.wechat.domain.AbstractWeChatOpen;
import com.jeecms.wechat.service.AbstractWeChatInfoService;
import com.jeecms.wechat.service.AbstractWeChatOpenService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jeecms.common.exception.error.UserErrorCodeEnum.*;
import static com.jeecms.system.domain.dto.ValidateCodeConstants.*;

/**
 * 用户service层
 *
 * @author: tom
 * @date: 2019年3月5日 下午4:48:37
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CoreUserServiceImpl extends BaseServiceImpl<CoreUser, CoreUserDao, Integer>
        implements CoreUserService, ApplicationListener
        <ContextRefreshedEvent> {
    private Logger logger = LoggerFactory.getLogger(CoreUserServiceImpl.class);

    @Autowired
    private PasswdService passwdService;
    @Autowired
    private CmsOrgService cmsOrgService;
    @Autowired
    private CoreRoleService coreRoleService;
    @Autowired
    private AbstractWeChatOpenService weChatOpenService;
    @Autowired
    private CoreMenuService menuService;
    @Autowired
    private AbstractWeChatInfoService weChatInfoService;
    @Autowired
    private MemberLevelService levelService;
    @Autowired
    private MemberGroupService groupService;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private CmsSiteService cmsSiteService;
    @Lazy
    @Autowired
    private CmsDataPermService cmsDataPermService;
    @Autowired
    private SysJobService jobService;
    @Autowired
    private CmsModelService cmsModelService;
    @Autowired
    private MemberAttrService memberAttrService;
    @Autowired
    private SendSmsUtilService sendSmsUtilService;
    @Autowired
    private CacheProvider cacheProvider;
    @Autowired
    private MqSendMessageService mqSendMessageService;
    @Autowired
    private MessageTplService messageTplService;
    @Autowired
    private MemberScoreDetailsService memberScoreDetailsService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;
    @Autowired
    private SysUserSecretService sysUserSecretService;
    @Value("${sso.save.update.url}")
    private String saveUpdateUrl;
    @Value("${sso.delete.url}")
    private String deleteUrl;
    @Autowired
    private CmsSsoService cmsSsoService;
    @Autowired
    private CmsSiteService siteService;

    @Override
    public ResponseInfo saveUser(CoreUserDto userDto) throws Exception {
        if (userDto.getIsAdmin() && userDto.getOrgid() == null) {
            return new ResponseInfo(UserErrorCodeEnum.ORG_CANNOT_EMPTY.getCode(),
                    UserErrorCodeEnum.ORG_CANNOT_EMPTY.getDefaultMessage());
        }
        /** 验证用户名，邮箱，电话 **/
        Boolean validName = validName(userDto.getUsername());
        if (!validName) {
            return new ResponseInfo(UserErrorCodeEnum.USERNAME_ALREADY_EXIST.getCode(),
                    UserErrorCodeEnum.USERNAME_ALREADY_EXIST.getDefaultMessage());
        }
        if (StringUtils.isNotBlank(userDto.getEmail())) {
            Boolean validMail = validMail(userDto.getEmail(), null);
            if (!validMail) {
                return new ResponseInfo(UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getDefaultMessage());
            }
        }
        if (StringUtils.isNotBlank(userDto.getUsePhone())) {
            Boolean validPhone = validPhone(userDto.getUsePhone(), null);
            if (!validPhone) {
                return new ResponseInfo(UserErrorCodeEnum.PHONE_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.PHONE_ALREADY_EXIST.getDefaultMessage());
            }
        }
        // 验证密码格式
        Boolean flag = validPassword(userDto.getPsw(), userDto.getUsername());
        if (!flag) {
            return new ResponseInfo(UserErrorCodeEnum.PASSWORD_FORMAT_IS_INCORRECT.getCode(),
                    UserErrorCodeEnum.PASSWORD_FORMAT_IS_INCORRECT.getDefaultMessage());
        }
        CoreUserExt ext = new CoreUserExt();
        ext.setRealname(userDto.getRealname());
        ext.setLinephone(userDto.getTelephone());
        ext.setGender(CoreUserExt.GENDER_SECRET);
        Integer orgid = userDto.getOrgid();
        List<CoreRole> roles = coreRoleService.findAllById(userDto.getRoleid());
        CmsOrg org = cmsOrgService.findById(orgid);
        // 密码加密
        byte[] salt = Digests.generateSaltFix();
        CoreUser user = new CoreUser();
        user.setSalt(Digests.getSaltStr(salt));
        // 解密密码
        String password = passwdService.decrypt(userDto.getPsw());
        user.setPassword(passwdService.entryptPassword(salt, password));
        /**false表示未重置密码，在开启账户安全且开启首次登录需要修改密码根据此判断*/
        user.setIsResetPassword(false);
        user.setRoles(roles);
        user.setOrgId(orgid);
        user.setOrg(org);
        // 默认登录次数为0
        user.setLoginCount(0);
        // 默认登录失败次数为0
        user.setLoginErrorCount(0);
        user.addExt(ext);
        user.setUsername(userDto.getUsername());
        user.setTelephone(userDto.getUsePhone());
        user.setUserSecretId(userDto.getUserSecretId());
        // 是否启用
        user.setEnabled(userDto.getEnabled());
        // 默认为管理员
        user.setAdmin(true);
        // 默认通过
        user.setCheckStatus(CoreUser.AUDIT_USER_STATUS_PASS);
        user.setEmail(userDto.getEmail());
        user.setPassMsgHasSend(false);
        user.setIntegral(0);
        user = super.save(user);
        user.validManagerAble();
        //开启单点登录需要同步该用户至SSO
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        if (attr.getSsoLoginAppOpen()) {
            syncSaveOrUpdate(user, attr.getSsoLoginAppId(), attr.getSsoLoginAppSecret());
        }
        return new ResponseInfo(user);
    }

    /**
     * 同步用户至SSO
     **/
    private void syncSaveOrUpdate(CoreUser user, String appId, String appSecret) {
        if (!cmsSsoService.isConnection(saveUpdateUrl.replace(" ", ""))) {
            return;
        }
        SyncRequestUser dto = new SyncRequestUser(user);
        dto.setOperateName(SystemContextUtils.getCurrentUsername());
        dto.setAppId(appId);
        dto.setAppSecret(appSecret);
        try {
            HttpUtil.postJsonBeanForJSON(saveUpdateUrl.replace(" ", ""),
                    null, JSONObject.toJSONString(dto),
                    SyncResponseVo.class);
        } catch (Exception e) {
            logger.debug("新增同步失败" + e.getMessage());
        }
    }

    /**
     * 带权限校验的修改用户资料
     */
    @Override
    public ResponseInfo updateUser(CoreUserDto bean) throws Exception {
        CoreUser user = super.get(bean.getId());
        CoreUser currUser = SystemContextUtils.getCoreUser();
        boolean changeOrgOrRole = !currUser.getOrgId().equals(bean.getOrgid())
                || !CollectionUtil.booleanEq(bean.getRoleid(), currUser.getRoleIds());
        /** 不可变更自己的组织和角色 */
        if (!SystemContextUtils.replay()) {
            if (user.getId().equals(currUser.getId()) && changeOrgOrRole) {
                String msg = MessageResolver.getMessage(
                        SysOtherErrorCodeEnum.USER_CHANGE_ORG_ROLE_ERROR.getCode(),
                        SysOtherErrorCodeEnum.USER_CHANGE_ORG_ROLE_ERROR.getDefaultMessage());
                throw new GlobalException(new SiteExceptionInfo(
                        SysOtherErrorCodeEnum.USER_CHANGE_ORG_ROLE_ERROR.getCode(), msg));
            }
        }
        user.setRoles(null);
        super.update(user);
        if (StringUtils.isNotBlank(bean.getEmail())) {
            Boolean boolean1 = validMail(bean.getEmail(), bean.getId());
            if (!boolean1) {
                return new ResponseInfo(UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getDefaultMessage());
            }
        }
        List<CoreRole> roles = coreRoleService.findAllById(bean.getRoleid());
        CmsOrg org = cmsOrgService.findById(bean.getOrgid());
        user.setRoles(roles);
        user.setOrgId(bean.getOrgid());
        user.setOrg(org);
        CoreUserExt ext = user.getUserExt();
        ext.setRealname(bean.getRealname());
        ext.setLinephone(bean.getTelephone());
        user.setUserExt(ext);
        user.setTelephone(bean.getUsePhone());
        user.setEmail(bean.getEmail());
        user.setEnabled(bean.getEnabled());
        if (bean.getUserSecretId() != null) {
            user.setUserSecretId(bean.getUserSecretId());
            user.setUserSecret(sysUserSecretService.findById(bean.getUserSecretId()));
        }
        user = super.update(user);
        /** 更改用户组织或角色需要检查分配之后权限 */
        if (changeOrgOrRole) {
            user.validManagerAble();
        }
        /** 需要清空权限缓存数据，后续操作从新拉取 */
        user.clearPermCache();
        /**开启单点登录需要同步修改该用户至SSO**/
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        //判断是否本用户
        if (attr.getSsoLoginAppOpen() && user.getItself()) {
            syncSaveOrUpdate(user, attr.getSsoLoginAppId(), attr.getSsoLoginAppSecret());
        } else if (!user.getItself()) {
            throw new GlobalException(RPCErrorCodeEnum.USER_CANNOT_OPERATE);
        }
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo enableUser(BeatchDto dto) throws GlobalException {
        //判断是否开启单点登录
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        List<CoreUser> userlist = super.findAllById(dto.getIds());
        for (CoreUser user : userlist) {
            user.validManagerAble();
            user.setEnabled(true);
            ableOrNot(user);
            super.update(user);
            if (attr.getSsoLoginAppOpen()) {
                syncSaveOrUpdate(user, attr.getSsoLoginAppId(), attr.getSsoLoginAppSecret());
            }
        }
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo disableUser(BeatchDto dto) throws GlobalException {
        //判断是否开启单点登录
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        List<CoreUser> userlist = super.findAllById(dto.getIds());
        for (CoreUser user : userlist) {
            user.validManagerAble();
            user.setEnabled(false);
            ableOrNot(user);
            super.update(user);
            if (attr.getSsoLoginAppOpen()) {
                syncSaveOrUpdate(user, attr.getSsoLoginAppId(), attr.getSsoLoginAppSecret());
            }
        }
        return new ResponseInfo();
    }

    /**
     * 判断是否可以操作
     **/
    void ableOrNot(CoreUser user) throws GlobalException {
        if (user != null && StringUtils.isNotBlank(user.getAppId())) {
            throw new GlobalException(RPCErrorCodeEnum.USER_CANNOT_OPERATE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoreUser findByUsername(String username) {
        CoreUser user = dao.findByUsernameAndHasDeleted(username, false);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public CoreUser findByUsernameAndAuth(String username) {
        CoreUser user = findByUsername(username);
        return user;
    }

    @Override
    public void userLogin(String username, String ip, boolean loginSuccess, LoginFailProcessMode processMode)
            throws GlobalException {
        CoreUser user = findByUsername(username);
        GlobalConfig config = globalConfigService.get();
        Date now = Calendar.getInstance().getTime();
        if (user != null) {
            if (processMode != null) {
                if (LoginFailProcessMode.forbidden.equals(processMode)) {
                    user.setEnabled(false);
                    user.setLoginLimitEnd(config.getLoginLimitTime());
                    /** 定时任务解禁 */
                    try {
                        jobService.addJob(JobFactory.createUserReleaseLock(user.getId(),
                                config.getLoginLimitTime()));
                    } catch (Exception e) {
                        logger.error(" add job error-->" + e.getMessage());
                    }
                    loginSuccess = false;
                } else if (LoginFailProcessMode.lock.equals(processMode)) {
                    user.setEnabled(false);
                    loginSuccess = false;
                }
            }
            if (loginSuccess) {
                user.setLoginErrorCount(0);
                user.setLastLoginTime(now);
                user.setLoginCount(user.getLoginCount() + 1);
                user.setLastLoginIp(ip);
                SystemContextUtils.setCoreUser(user);
            } else {
                /** 用户最早的登录错误时间比获取到的系统错误周期最早时间之前，则是从新计算周期了 ,从新设置周期内最早登录错误时间和登录错误次数重置 */
                if (user.getFirstLoginErrorTime() == null) {
                    user.setFirstLoginErrorTime(now);
                } else if (user.getFirstLoginErrorTime().before(config.getLoginErrorUnitBeginTime())) {
                    user.setFirstLoginErrorTime(now);
                    user.setLoginErrorCount(0);
                }
                user.setLoginErrorCount(user.getLoginErrorCount() + 1);
            }
            update(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Boolean validName(String validName) {
        CoreUser user = dao.findByUsernameAndHasDeleted(validName, false);
        if (user == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Boolean validMail(String validMail, Integer id) {
        CoreUser user = dao.findByEmailAndHasDeleted(validMail, false);
        if (id == null) {
            if (user != null) {
                return false;
            }
            return true;
        }
        CoreUser coreUser = super.findById(id);
        if (user != null && !validMail.equals(coreUser.getEmail())) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Boolean validPhone(String validPhone, Integer id) {
        CoreUser user = dao.findByTelephoneAndHasDeleted(validPhone, false);
        if (id == null) {
            if (user != null) {
                return false;
            }
            return true;
        }
        CoreUser coreUser = super.findById(id);
        if (user != null && !validPhone.equals(coreUser.getTelephone())) {
            return false;
        }
        return true;
    }

    @Override
    public Page<CoreUser> pageUser(Boolean enabled, List<Integer> orgids, List<Integer> roleids, String key,
                                   Boolean isAdmin, Short checkStatus, Integer groupId, Integer levelId, Integer userSecretId,
                                   List<Integer> sourceSiteId, Pageable pageable) {
        Page<CoreUser> page = dao.pageUser(enabled, orgids, roleids, key, isAdmin, checkStatus, groupId,
                levelId, userSecretId, sourceSiteId, pageable);
        return page;
    }

    @Override
    public List<CoreUser> findList(Boolean enabled, List<Integer> orgids, List<Integer> roleids, String key,
                                   Boolean isAdmin, Short checkStatus, Integer groupId, Integer levelId, Integer secretId,
                                   List<Integer> sourceSiteIds, Paginable paginable) {
        return dao.findList(enabled, orgids, roleids, key, isAdmin, checkStatus, groupId, levelId, secretId,
                sourceSiteIds, paginable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Map<String, Object> routingTree(String username, HttpServletRequest request) throws GlobalException {
        if (StringUtils.isBlank(username)) {
            username = SystemContextUtils.getCurrentUsername();
        }
        if (StringUtils.isBlank(username) || WebConstants.ANONYMOUSUSER.equals(username)) {
            return null;
        }
        CoreUser user = this.findByUsername(username);
        if (user == null) {
            return null;
        }
        /** 管理员重新登录后需要重新拉取权限数据到缓存中 */
        user.clearPermCache();
        if (user.getRoles() != null) {
            for (CoreRole role : user.getRoles()) {
                role.clearPermCache();
            }
        }
        user.getOrg().clearPermCache();
        List<SortMenuVO> menuResult = new ArrayList<>();
        // 数据交互集合
        Set<CoreApiVo> routingResult = new HashSet<CoreApiVo>();
        SortMenuVO menuVO = null;
        CoreUserAgent userAgent = new CoreUserAgent(user);
        menuResult = userAgent.getUserMenuVos(CoreMenu.MENUTYPE_MENU);
        routingResult = userAgent.getUserApis();
        AbstractWeChatOpen weChatOpen = weChatOpenService.findOpenConfig();
        List<AbstractWeChatInfo> weChatInfos = weChatInfoService.findWeChatInfo(Const.WeCahtType.PUBLIC_ACCOUNT,
                user.getSourceSiteId());
        CoreMenu weChatMenu = null;
        // 如果jc_wechat_open有数据并且未授权
        if (weChatOpen != null && weChatInfos == null) {
            weChatMenu = menuService.findByRouting(RoutingConstant.PlatFormWeChatPublic.getRouting());
            if (weChatMenu != null) {
                menuVO = new SortMenuVO(weChatMenu);
                menuResult.remove(menuVO);
            }
        }
        // 判断jc_wechat_open是否有数据,如果没有过滤微信菜单
        if (weChatOpen == null) {
            weChatMenu = menuService.findByRouting(RoutingConstant.PlatFormWeChat.getRouting());
            if (weChatMenu != null) {
                menuVO = new SortMenuVO(weChatMenu);
                menuResult.remove(menuVO);
            }
        }
        // routings存放所有数据 menus只存放菜单类型数据
        // 结果集交互集合
        Map<String, Object> result = new TreeMap<>();
        return interaction(result, user, routingResult, menuResult);
    }

    /**
     * 结果集交互集合
     *
     * @param result        结果
     * @param user          用户
     * @param routingResult 交互结果
     * @param menuResult    菜单结果
     * @return Map
     * @Title: interaction
     */
    public Map<String, Object> interaction(Map<String, Object> result, CoreUser user, Set<CoreApiVo> routingResult,
                                           List<SortMenuVO> menuResult) {
        result.put(VoConstants.CONSTANT_USER_NAME, user.getUsername());
        result.put(VoConstants.CONSTANT_LASTLOGIN_IP, user.getLastLoginIp());
        result.put(VoConstants.CONSTANT_LASTLOGIN_TIME,
                MyDateUtils.formatDate(user.getLastLoginTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN));
        result.put(VoConstants.CONSTANT_MENU_ROUTINGS_NAME, routingResult);
        result.put(VoConstants.CONSTANT_MENU_MENUS_NAME, menuResult);
        return result;
    }

    @Override
    public ResponseInfo removeUser(UserManagerDto dto) throws GlobalException {
        List<CoreUser> users = new ArrayList<CoreUser>(10);
        // 如果是角色
        if (UserManagerDto.ROLE_TYPE.equals(dto.getType())) {
            List<CoreUser> list = super.findAllById(dto.getUserId());
            CoreRole role = coreRoleService.findById(dto.getId());
            for (CoreUser coreUser : list) {
                List<CoreRole> roles = coreUser.getRoles();
                CopyOnWriteArrayList<CoreRole> coreRoles = new CopyOnWriteArrayList<CoreRole>(roles);
                if (!roles.isEmpty()) {
                    coreRoles.remove(role);
                }
                coreUser.setRoles(coreRoles);
                users.add(coreUser);
            }
            super.batchUpdateAll(users);
            super.flush();
        } else {
            // 如果是组织
            users = super.findAllById(dto.getUserId());
            CmsOrg org = cmsOrgService.findById(dto.getId());
            for (CoreUser coreUser : users) {
                coreUser.validManagerAble();
                coreUser.setOrgId(dto.getId());
                coreUser.setOrg(org);
            }
            super.batchUpdate(users);
        }
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo addUser(UserManagerDto dto) throws GlobalException {
        List<CoreUser> users = new ArrayList<>(10);
        List<CoreRole> roles;
        // 如果是角色
        if (UserManagerDto.ROLE_TYPE.equals(dto.getType())) {
            List<CoreUser> list = super.findAllById(dto.getUserId());
            CoreRole role = coreRoleService.findById(dto.getId());
            if (role == null || role.getHasDeleted()) {
                return new ResponseInfo(UserErrorCodeEnum.ROLE_NOT_EXIST.getCode(),
                        UserErrorCodeEnum.ROLE_NOT_EXIST.getDefaultMessage());
            }
            for (CoreUser coreUser : list) {
                roles = coreUser.getRoles();
                //判断当前用户是否存在相同的角色，不存在则相加
                if (roles.isEmpty() || !roles.contains(role)) {
                    roles.add(role);
                    coreUser.setRoles(roles);
                }
            }
            super.batchUpdate(list);
            super.flush();
        } else if (UserManagerDto.ORG_TYPE.equals(dto.getType())) {
            // 如果是组织
            CmsOrg org = cmsOrgService.findById(dto.getId());
            if (org == null || org.getHasDeleted()) {
                return new ResponseInfo(UserErrorCodeEnum.ORG_NOT_EXIST.getCode(),
                        UserErrorCodeEnum.ORG_NOT_EXIST.getDefaultMessage());
            }
            List<CoreUser> list = super.findAllById(dto.getUserId());
            for (CoreUser coreUser : list) {
                coreUser.setOrgId(dto.getId());
                users.add(coreUser);
            }
            super.batchUpdate(users);
        } else {
            // 如果是会员组
            MemberGroup group = groupService.findById(dto.getId());
            if (group == null || group.getHasDeleted()) {
                return new ResponseInfo(UserErrorCodeEnum.MEMBER_GROUP_NOT_EXIST.getCode(),
                        UserErrorCodeEnum.MEMBER_GROUP_NOT_EXIST.getDefaultMessage());
            }
            List<CoreUser> list = super.findAllById(dto.getUserId());
            for (CoreUser coreUser : list) {
                coreUser.setGroupId(dto.getId());
                users.add(coreUser);
            }
            super.batchUpdate(users);
        }
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo auditON(Integer[] ids) throws GlobalException {
        Map<String, String[]> params = new HashMap<>(3);
        params.put("EQ_isDefault_Boolean", new String[]{"true"});
        // 得到默认会员组
        List<MemberGroup> groups = groupService.getList(params, null, true);
        List<CoreUser> list = new ArrayList<>(10);
        List<CoreUser> users = super.findAllById(Arrays.asList(ids));
        for (CoreUser user : users) {
            if (CoreUser.AUDIT_USER_STATUS_PASS.equals(user.getCheckStatus())) {
                continue;
            }
            user.setCheckStatus(CoreUser.AUDIT_USER_STATUS_PASS);
            if (!groups.isEmpty()) {
                // 审核通过，需要将会员归为默认会员组
                user.setGroupId(groups.get(0).getId());
                user.setUserGroup(groups.get(0));
            }
            //注册积分信息
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.REGISTER_SCORE_TYPE,
                    user.getId(), user.getSourceSiteId(), null);
            // 完善积分信息
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.MESSAGE_SCORE_TYPE, user.getId(),
                    user.getSourceSiteId(), null);
            list.add(user);
        }
        //super.batchUpdate(list);
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        //发送消息
        ThreadPoolService.getInstance().execute(() -> {
            for (CoreUser coreUser : list) {
                try {
                    sendMemmberMessage(coreUser, MessageTplCodeConstants.MEMBER_AUDIT_PASS_TPL,
                            CoreUser.AUDIT_USER_STATUS_PASS, siteId);
                } catch (GlobalException e) {
                    logger.error("审核成功发送提醒失败");
                }
            }
        });
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo auditOFF(BeatchDto dto) throws GlobalException {
        List<CoreUser> list = new ArrayList<>(10);
        List<CoreUser> users = super.findAllById(dto.getIds());
        for (CoreUser user : users) {
            user.setCheckStatus(CoreUser.AUDIT_USER_STATUS_NOPASS);
            user.getUserExt().setRemark(dto.getReason());
            list.add(user);
        }
        //super.batchUpdate(list);
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        // 发送消息，以及原因
        ThreadPoolService.getInstance().execute(() -> {
            for (CoreUser coreUser : list) {
                try {
                    sendMemmberMessage(coreUser, MessageTplCodeConstants.MEMBER_AUDIT_UNPASS_TPL,
                            CoreUser.AUDIT_USER_STATUS_NOPASS, siteId);
                } catch (GlobalException e) {
                    logger.error("审核失败发送提醒失败");
                }
            }
        });
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo saveMember(JSONObject json, CoreUser user) throws GlobalException {
        /** 验证用户名和邮箱 **/
        Boolean validName = validName(user.getUsername());
        if (validName != null && !validName) {
            return new ResponseInfo(UserErrorCodeEnum.USERNAME_ALREADY_EXIST.getCode(),
                    UserErrorCodeEnum.USERNAME_ALREADY_EXIST.getDefaultMessage());
        }
        if (StringUtils.isNotBlank(user.getEmail())) {
            Boolean validMail = validMail(user.getEmail(), null);
            if (validMail != null && !validMail) {
                return new ResponseInfo(UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getDefaultMessage());
            }
        }
        if (StringUtils.isNotBlank(user.getTelephone())) {
            Boolean validPhone = validPhone(user.getTelephone(), null);
            if (validPhone != null && !validPhone) {
                return new ResponseInfo(UserErrorCodeEnum.PHONE_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.PHONE_ALREADY_EXIST.getDefaultMessage());
            }
        }

        user = super.save(user);
        super.flush();
        // 是否存在自定义字段
        if (json != null) {
            workAttr(json, user.getId());
        }
        return new ResponseInfo();
    }

    /**
     * 处理自定义属性
     *
     * @param object 自定义对象
     * @Title: workAttr
     */
    protected void workAttr(JSONObject object, Integer memberId) throws GlobalException {
        // 直接删除之前的自定义属性值
        memberAttrService.deleteAttrs(memberId);
        Set<CmsModelItem> modelItemList = cmsModelService.getInfo(null).getItems();
        // 将模型字段表的默认值取出
        Map<String, String> newModelItemMap = modelItemList.stream().filter(a -> a.getIsCustom().equals(true))
                .collect(HashMap::new, (m, v) -> m.put(v.getField(), v.getDefValue()), HashMap::putAll);
        // 处理前台传值的json
        List<MemberAttr> attrs = memberAttrService.jsonToMemberAttrs(object, memberId, modelItemList);
        initMemberAttrs(attrs, newModelItemMap);
        // 如果自定义属性不为空
        if (!attrs.isEmpty()) {
            memberAttrService.saveAll(attrs);
            memberAttrService.flush();
        }
    }

    /**
     * 初始化自定义字段
     *
     * @param attrs 会员自定义属性
     * @return List
     * @throws GlobalException 异常
     * @Title: initMemberAttrs
     */
    protected void initMemberAttrs(List<MemberAttr> attrs, Map<String, String> newModelItemMap)
            throws GlobalException {
        for (MemberAttr memberAttr : attrs) {
            String defValue = newModelItemMap.get(memberAttr.getAttrName());
            // 这步的操作就是将自定义字段前台没有传值的情况下，
            // 查询模型字段表，取默认值给当前；
            if (memberAttr.getAttrValue() == null && defValue != null) {
                memberAttr.setAttrValue(defValue);
            }
        }
    }

    @Override
    public void updateMember(CoreUser bean, JSONObject json) throws GlobalException {
        // 验证邮箱
        if (StringUtils.isNotBlank(bean.getEmail())) {
            Boolean validMail = validMail(bean.getEmail(), bean.getId());
            if (!validMail) {
                throw new GlobalException((new SystemExceptionInfo(UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.EMAIL_ALREADY_EXIST.getDefaultMessage())));
            }
        }
        // 验证电话
        if (StringUtils.isNotBlank(bean.getTelephone())) {
            Boolean validPhone = validPhone(bean.getTelephone(), bean.getId());
            if (!validPhone) {
                throw new GlobalException((new SystemExceptionInfo(UserErrorCodeEnum.PHONE_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.PHONE_ALREADY_EXIST.getDefaultMessage())));
            }
        }

        super.update(bean);
        super.flush();
        // 是否存在自定义字段
        if (json != null) {
            workAttr(json, bean.getId());
        }
    }

    /**
     * 共享设置修改默认共享人
     */
    @Override
    public CoreUser updateMember(Integer userId, ResourcesSpaceShareDto dto) throws GlobalException {
        CoreUser user = super.get(userId);
        CoreUserExt ext = user.getUserExt();
        ext.setShareUserId(StringUtils.join(dto.getUserIds(), ","));
        ext.setShareOrgId(StringUtils.join(dto.getOrgIds(), ","));
        ext.setShareRoleId(StringUtils.join(dto.getRoleIds(), ","));
        user.setUserExt(ext);
        return user;
    }

    @Override
    public ResponseInfo updatePCMember(MemberRegisterDto bean) throws Exception {
        CoreUser user = super.findById(bean.getId());
        // 验证电话
        if (StringUtils.isNotBlank(bean.getTelephone())) {
            Boolean validPhone = validPhone(bean.getTelephone(), bean.getId());
            if (!validPhone) {
                return new ResponseInfo(UserErrorCodeEnum.PHONE_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.PHONE_ALREADY_EXIST.getDefaultMessage());
            }
        }
        CoreUserExt ext = user.getUserExt();
        ext.setRealname(bean.getRealname());
        user.setUserExt(ext);
        user.setTelephone(bean.getTelephone());
        super.update(user);
        super.flush();
        // 是否存在自定义字段
        if (bean.getJson() != null) {
            // 保留之前的单图上传内容
            List<MemberAttr> list = user.getMemberAttrs().stream()
                    .filter(x -> x.getAttrType().equals(CmsModelConstant.SINGLE_CHART_UPLOAD))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                //判断是否修改头像
                for (MemberAttr memberAttr : list) {
                    String image = bean.getJson().getString(memberAttr.getAttrName());
                    if (StringUtils.isNoneBlank(image) && !image.equals(memberAttr.getAttrValue())) {
                        bean.getJson().put(memberAttr.getAttrName(), image);
                    } else {
                        bean.getJson().put(memberAttr.getAttrName(), memberAttr.getAttrValue());
                    }
                }
            }
            workAttr(bean.getJson(), user.getId());
        }
        // 如果是会员才加积分
        if (!user.getAdmin()) {
            // 注册积分信息
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.MESSAGE_SCORE_TYPE, user.getId(),
                    user.getSourceSiteId(), null);

        }
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo updatePStr(MemberPwdDto memberInfoDto, Integer userId) throws GlobalException {
        CoreUser user = findById(userId);
        /**演示站禁止test修改密码*/
        if ("test".equals(user.getUsername()) && ServerModeEnum.demo.toString().equals(serverModel)) {
            return new ResponseInfo(UserErrorCodeEnum.MOVE_TO_BACKGROUND_UPDATE.getCode(),
                    UserErrorCodeEnum.MOVE_TO_BACKGROUND_UPDATE.getDefaultMessage(), false);
        }
        if (StringUtils.isBlank(memberInfoDto.getNewPStr())) {
            // 密码为空
            return new ResponseInfo(UserErrorCodeEnum.PASSWORD_FORMAT_IS_INCORRECT.getCode(),
                    UserErrorCodeEnum.PASSWORD_FORMAT_IS_INCORRECT.getDefaultMessage(), false);
        } else if (!memberInfoDto.getNewPStr().equals(memberInfoDto.getAgainPStr())) {
            // 两次密码输入不一致
            return new ResponseInfo(UserErrorCodeEnum.PASSWORD_INCONSISTENT_ERROR.getCode(),
                    UserErrorCodeEnum.PASSWORD_INCONSISTENT_ERROR.getDefaultMessage(), false);
        }
        // 判断是否第三方登录，如果是则不需要原密码
        if (user.getThird() != null && user.getThird()) {
            // 密码解密
            String pStr = passwdService.decrypt(memberInfoDto.getNewPStr());
            // 密码加密
            byte[] salt = Digests.generateSaltFix();
            user.setSalt(Digests.getSaltStr(salt));
            user.setPassword(passwdService.entryptPassword(salt, pStr));
            super.update(user);
            return new ResponseInfo();
        }
        // 解密旧密码
        String oldpStr = passwdService.decrypt(memberInfoDto.getOldPStr());
        // 判断输入的旧密码是否匹配
        if (!passwdService.isPasswordValid(oldpStr, user.getSalt(), user.getPassword())) {
            return new ResponseInfo(UserErrorCodeEnum.ACCOUNT_CREDENTIAL_ERROR.getCode(),
                    UserErrorCodeEnum.ACCOUNT_CREDENTIAL_ERROR.getDefaultMessage(), false);
        }
        // 密码解密
        String pStr = passwdService.decrypt(memberInfoDto.getNewPStr());
        // 密码加密
        byte[] salt = Digests.generateSaltFix();
        user.setSalt(Digests.getSaltStr(salt));
        user.setPassword(passwdService.entryptPassword(salt, pStr));
        user.setIsResetPassword(true);
        super.update(user);
        return new ResponseInfo();
    }

    @Override
    public void changeUserStatus() throws GlobalException {
        Map<String, String[]> params = new HashMap<String, String[]>(10);
        params.put("EQ_checkStatus_Integer", new String[]{CoreUser.AUDIT_USER_STATUS_WAIT.toString()});
        List<CoreUser> user = super.getList(params, null, false);
        List<CoreUser> members = new ArrayList<CoreUser>(10);
        for (CoreUser coreUser : user) {
            coreUser.setCheckStatus(CoreUser.AUDIT_USER_STATUS_PASS);
            members.add(coreUser);
        }
        super.batchUpdate(members);
    }

    @Override
    public void resetLoginErrorCount() throws GlobalException {
        Map<String, String[]> params = new HashMap<String, String[]>(10);
        /** 登录错误次数大于0的重置为0 */
        params.put("GT_loginErrorCount_Integer", new String[]{"0"});
        List<CoreUser> user = super.getList(params, null, false);
        List<CoreUser> users = new ArrayList<CoreUser>(10);
        for (CoreUser coreUser : user) {
            coreUser.setLoginErrorCount(0);
            coreUser.setFirstLoginErrorTime(null);
            users.add(coreUser);
        }
        super.batchUpdateAll(users);
    }

    protected Boolean validPassword(String pwd, String username) throws GlobalException {
        // 解密密码
        String password = passwdService.decrypt(pwd);
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        // 判断是否开启账户安全，关闭则不检验
        if (attr.getSecurityOpen()) {
            // 验证密码最小长度
            Integer min = Integer.valueOf(attr.getPassMin());
            // 验证密码最大长度
            Integer max = Integer.valueOf(attr.getPassMax());
            if (password.length() < min || password.length() > max) {
                return false;
            }
            // 验证密码正则表达式
            switch (Integer.parseInt(attr.getPassCompositionRule())) {
                case GlobalConfig.NONE_TYPE:
                    break;
                case GlobalConfig.LETTER_NUMBER_TYPE:
                    return Pattern.matches(GlobalConfig.REGEX, password);
                case GlobalConfig.LETTER_UPPER_LOWER_NUMBER_TYPE:
                    return Pattern.matches(GlobalConfig.REGEXS, password);
                case GlobalConfig.LETTER_NUMBER_STRING_TYPE:
                    return Pattern.matches(GlobalConfig.REGEXMORE, password);
                case GlobalConfig.LETTER_UPPER_LOWER_NUMBER_STRING_TYPE:
                    return Pattern.matches(GlobalConfig.REGEXMORES, password);
                default:
                    break;
            }
            // 验证是否允许密码跟用户名相同
            if (attr.getPassIsEqually()) {
                return !password.equals(username);
            }
        }
        return true;
    }

    @Override
    public ThirdPartyResultDTO validPwd(String pwd, String username) throws GlobalException {
        // 解密密码
        String password = passwdService.decrypt(pwd);
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        // 判断是否开启账户安全，关闭则不检验
        StringBuilder builder = new StringBuilder();
        if (attr.getSecurityOpen()) {
            // 验证密码最小长度
            Integer min = Integer.valueOf(attr.getPassMin());
            // 验证密码最大长度
            Integer max = Integer.valueOf(attr.getPassMax());
            if (password.length() < min || password.length() > max) {
                builder.append("密码长度需在").append(min).append("-").append(max).append("之间");
                return new ThirdPartyResultDTO(200, builder.toString(), false);
            }
            // 验证密码正则表达式
            switch (Integer.parseInt(attr.getPassCompositionRule())) {
                case GlobalConfig.NONE_TYPE:
                    break;
                case GlobalConfig.LETTER_NUMBER_TYPE:
                    return Pattern.matches(GlobalConfig.REGEX, password) ? new ThirdPartyResultDTO(true)
                            : new ThirdPartyResultDTO(200, builder.append("必须包含字母和数字").toString(),
                            false);
                case GlobalConfig.LETTER_UPPER_LOWER_NUMBER_TYPE:
                    return Pattern.matches(GlobalConfig.REGEXS, password) ? new ThirdPartyResultDTO(true)
                            : new ThirdPartyResultDTO(200,
                            builder.append("必须包含大写字母、小写字母、数字").toString(), false);
                case GlobalConfig.LETTER_NUMBER_STRING_TYPE:
                    return Pattern.matches(GlobalConfig.REGEXMORE, password) ? new ThirdPartyResultDTO(true)
                            : new ThirdPartyResultDTO(200,
                            builder.append("必须包含字母、数字、特殊字符").toString(), false);
                case GlobalConfig.LETTER_UPPER_LOWER_NUMBER_STRING_TYPE:
                    return Pattern.matches(GlobalConfig.REGEXMORES, password)
                            ? new ThirdPartyResultDTO(true)
                            : new ThirdPartyResultDTO(200,
                            builder.append("必须包含大写字母、小写字母、数字、特殊字符").toString(),
                            false);
                default:
                    break;
            }
            // 验证是否允许密码跟用户名相同
            if (attr.getPassIsEqually()) {
                return password.equals(username) ? new ThirdPartyResultDTO(200,
                        builder.append("密码跟用户名不能相同").toString(), false)
                        : new ThirdPartyResultDTO(true);
            }
        }
        return new ThirdPartyResultDTO(true);
    }

    @Override
    public ResponseInfo updateScore(Integer siteId, Map<String, String> map) throws GlobalException {
        CmsSite site = cmsSiteService.findById(siteId);
        Map<String, String> cfg = site.getCfg();
        cfg.putAll(map);
        cmsSiteService.update(site);
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo deleteUser(BeatchDto dto, Integer orgId) throws GlobalException {
        List<Integer> userList = getUsers(orgId);
        // 判断是否存在不可操作数据
        for (Integer id : dto.getIds()) {
            CoreUser user = super.findById(id);
            /** 判断是否开启单点登录，删除需要同步 **/
            GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
            if (attr.getSsoLoginAppOpen()) {
                if (!user.getItself()) {
                    throw new GlobalException(RPCErrorCodeEnum.USER_CANNOT_OPERATE);
                } else {
                    if (!userList.contains(id)) {
                        return new ResponseInfo(UserErrorCodeEnum
                                .ALREADY_DATA_NOT_OPERATION.getCode(),
                                UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION
                                        .getDefaultMessage());
                    }
                }
            } else {
                /**非三员回放请求*/
                if (!SystemContextUtils.replay()) {
                    /**传递的用户id不在用户所属组织下属用户范围内 则不可删除*/
                    if (!userList.contains(id)) {
                        return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                                UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION
                                        .getDefaultMessage());
                    }
                }
            }
        }
        List<CoreUser> users = super.findAllById(dto.getIds());
        for (CoreUser user : users) {
            user.validManagerAble();
        }
        // 删除用户
        super.delete(users);
        // 删除用户权限
        cmsDataPermService.delete(null, null, users);
        /** 判断是否开启单点登录，删除需要同步 **/
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        if (attr.getSsoLoginAppOpen()) {
            syncDelete(users, attr.getSsoLoginAppId(), attr.getSsoLoginAppSecret());
        }
        return new ResponseInfo();
    }

    /**
     * 删除同步SSO
     **/
    private void syncDelete(List<CoreUser> user, String appId, String appSecret) throws GlobalException {
        if (!cmsSsoService.isConnection(deleteUrl.replace(" ", ""))) {
            return;
        }
        List<SyncDeleteUser> userlist = new ArrayList<SyncDeleteUser>();
        for (CoreUser coreUser : user) {
            SyncDeleteUser requestUser = new SyncDeleteUser(coreUser);
            userlist.add(requestUser);
        }
        SyncDeleteUserDto dto = new SyncDeleteUserDto();
        dto.setOperateName(SystemContextUtils.getCurrentUsername());
        dto.setAppId(appId);
        dto.setAppSecret(appSecret);
        dto.setUsers(userlist);
        try {
            HttpUtil.postJsonBeanForJSON(deleteUrl.replace(" ", ""), null,
                    JSONObject.toJSONString(dto), SyncResponseVo.class);
        } catch (Exception ignore) {
            logger.debug("删除同步失败");
        }
    }

    private List<Integer> getUsers(Integer orgid) {
        List<Integer> userList = new ArrayList<Integer>(10);
        CmsOrg org = cmsOrgService.findById(orgid);
        // 获取当前组织以及下级组织所有用户
        List<CoreUser> users = findList(null, org.getChildOrgIds(), null, null, true,
                CoreUser.AUDIT_USER_STATUS_PASS, null, null, null, null, null);
        for (CoreUser coreUser : users) {
            userList.add(coreUser.getId());
        }
        return userList;
    }

    @Override
    public Page<CoreUser> pageThirdManager(Integer orgId, Integer roleid, String username, Pageable pageable,
                                           List<Integer> notIds) {
        return dao.pageWechat(orgId, roleid, username, pageable, notIds);
    }

    @Override
    public List<CoreUser> listThirdManager(List<Integer> ids, Integer orgId, Integer roleid, String username) {
        return dao.listWechat(ids, orgId, roleid, username);
    }

    @Override
    public long getNewUserSum(Date beginTime, Date endTime, Integer siteId) {
        return dao.getUserSum(beginTime, endTime, siteId, null);
    }

    @Override
    public long getUserSum(Date beginTime, Date endTime, Integer siteId) {
        return dao.getUserSum(beginTime, endTime, siteId, CoreUser.AUDIT_USER_STATUS_WAIT);
    }

    @Override
    public ResponseInfo psw(CoreUserDto coreUser, boolean isReset) throws GlobalException {
        CoreUser user = findById(coreUser.getId());
        // 密码解密
        String pStr = passwdService.decrypt(coreUser.getPsw());
        // 密码加密
        byte[] salt = Digests.generateSaltFix();
        String saltStr = Digests.getSaltStr(salt);
        String newPaswd = passwdService.entryptPassword(salt, pStr);
        user.setSalt(saltStr);
        user.setPassword(newPaswd);
        /** 是否重置密码，如果是重置密码记录修改时间，用于修改密码后token自动过期验证，踢出用户，需要用户重新登录 */
        if (isReset) {
            /**标注需要主动调整密码*/
            user.setIsResetPassword(false);
        } else {
            user.setIsResetPassword(true);
        }
        user.setLastPasswordChange(Calendar.getInstance().getTime());
        super.update(user);
        /**开启单点登录需要修改该用户至SSO**/
        GlobalConfigAttr attr = globalConfigService.get().getConfigAttr();
        if (attr.getSsoLoginAppOpen() && user.getItself()) {
            syncSaveOrUpdate(user, attr.getSsoLoginAppId(), attr.getSsoLoginAppSecret());
        }
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo moveUser(UserManagerDto dto) throws GlobalException {
        List<CoreUser> arrayList = new ArrayList<CoreUser>(10);
        List<CoreUser> list = super.findAllById(dto.getUserId());
        for (CoreUser coreUser : list) {
            coreUser.setGroupId(dto.getId());
            arrayList.add(coreUser);
        }
        super.batchUpdate(arrayList);
        return new ResponseInfo();
    }

    @Override
    public MemberInfoVo getMemberInfo(Integer memberId) throws GlobalException {
        MemberInfoVo vo = new MemberInfoVo();
        CoreUser member = super.findById(memberId);
        CmsModel model = cmsModelService.getInfo(null);
        JSONObject renderingField = model.getEnableJson();
        // 设置渲染字段
        vo.setRenderingField(renderingField);
        JSONObject defaultJson = initDefaultModelItems(member, false);
        // 自定义字段
        Set<CmsModelItem> modelItems = model.getItems();
        Set<CmsModelItem> customModelItems = modelItems.stream().filter(CmsModelItem::getIsCustom)
                .collect(Collectors.toSet());
        JSONObject customJson = initCustomModelItems(customModelItems, member.getMemberAttrs());
        // 创建一个新的JSON，将默认字段和自定义字段拼接出的JSON组装成一个JSON
        JSONObject dataFieldJson = new JSONObject();
        dataFieldJson.putAll(defaultJson);
        dataFieldJson.putAll(customJson);
        vo.setDataField(dataFieldJson);
        vo.setId(member.getId());
        vo.setSiteName(member.getSourceSite().getName());
        vo.setRegisterTime(MyDateUtils.formatDate(member.getCreateTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN));
        return vo;
    }

    /**
     * 会员模型字段赋值
     */
    @Override
    public JSONObject initDefaultModelItems(CoreUser member, Boolean type) {
        JSONObject json = new JSONObject();
        if (type) {
            MemberGroup group = member.getUserGroup();
            MemberLevel level = member.getUserLevel();
            json.put(CmsModelConstant.FIELD_MEMBER_USERGROUP, group == null ? "" : group.getGroupName());
            json.put(CmsModelConstant.FIELD_MEMBER_USERLEVEL, level == null ? "" : level.getLevelName());
            json.put("sourceSiteName", member.getSourceSite().getName());
            json.put("createTime", MyDateUtils.formatDate(member.getCreateTime()));
            json.put("enabled", member.getEnabled());
            // 判断是否有扩展字段
            if (member.getMemberAttrs() != null && !member.getMemberAttrs().isEmpty()) {
                for (MemberAttr attr : member.getMemberAttrs()) {
                    json.put(attr.getAttrName(), attr.getAttrValue());
                }
            }
        } else {
            json.put(CmsModelConstant.FIELD_MEMBER_USERGROUP, member.getGroupId());
            json.put(CmsModelConstant.FIELD_MEMBER_USERLEVEL, member.getLevelId());
        }
        CoreUserExt ext = member.getUserExt();
        json.put(CmsModelConstant.FIELD_MEMBER_ID, member.getId());
        json.put(CmsModelConstant.FIELD_MEMBER_EMAIL, StringUtils
                .isNotBlank(member.getEmail()) ? member.getEmail() : "");
        json.put(CmsModelConstant.FIELD_MEMBER_TELEPHONE, StringUtils
                .isNotBlank(member.getTelephone()) ? member.getTelephone() : "");
        //针对前端字段类型会变，直接给双值
        json.put(CmsModelConstant.FIELD_MEMBER_MOBILE, StringUtils
                .isNotBlank(member.getTelephone()) ? member.getTelephone() : "");
        json.put(CmsModelConstant.FIELD_MEMBER_INTEGRAL, member.getIntegral());
        json.put(CmsModelConstant.FIELD_MEMBER_REALNAME, StringUtils
                .isNotBlank(ext.getRealname()) ? ext.getRealname() : "");
        json.put(CmsModelConstant.FIELD_MEMBER_CHECKSTATUS, member.getCheckStatusName(member.getCheckStatus()));
        json.put(CmsModelConstant.FIELD_MEMBER_USERNAME, member.getUsername());
        json.put(CmsModelConstant.FIELD_MEMBER_STATUS, member.getEnabled());
        //x1.9.0新增性别以及头像
        if (!member.getAdmin()) {
            json.put(CmsModelConstant.FIELD_MEMBER_USER_IMG,
                    Optional.ofNullable(member.getUserExt().getResourcesSpaceData())
                            .map(o -> {
                                if (!o.getUrl().contains("://")) {
                                    return member.getSourceSite().getUrlBuffer(true).toString() + o.getUrl();
                                } else {
                                    return o.getUrl();
                                }
                            }).orElse(""));
            json.put(CmsModelConstant.FIELD_MEMBER_USER_IMG_ID,
                    Optional.ofNullable(member.getUserExt().getResourcesSpaceData())
                            .map(ResourcesSpaceData::getId).orElse(null));
            json.put(CmsModelConstant.FIELD_MEMBER_GENDER,
                    member.getUserExt().getGender());
        }
        return json;
    }

    /**
     * 初始化自定义字段
     */
    private JSONObject initCustomModelItems(Set<CmsModelItem> items, List<MemberAttr> memberAttrs) {
        JSONObject json = new JSONObject();
        if (memberAttrs.isEmpty()) {
            for (CmsModelItem cmsModelItem : items) {
                String field = cmsModelItem.getField();
                String dataType = cmsModelItem.getDataType();
                /**
                 * 特殊处理字段：单图上传、所在地、城市, 多选框,性别,默认值从模型字段取
                 */
                switch (dataType) {
                    case CmsModelConstant.SINGLE_CHART_UPLOAD:
                        json.put(field, cmsModelItem.getDefValue());
                        continue;
                    case CmsModelConstant.ADDRESS:
                        JSONObject address = JSONObject.parseObject(cmsModelItem.getDefValue());
                        JSONObject addressJson = new JSONObject();
                        addressJson.put(MemberAttr.PROVINCE_CODE_NAME,
                                address.getString(MemberAttr.PROVINCE_CODE_NAME));
                        addressJson.put(MemberAttr.CITY_CODE_NAME,
                                address.getString(MemberAttr.CITY_CODE_NAME));
                        addressJson.put(MemberAttr.AREA_CODE_NAME,
                                address.getString(MemberAttr.AREA_CODE_NAME));
                        addressJson.put(MemberAttr.ADDRESS_NAME,
                                address.getString(MemberAttr.ADDRESS_NAME));
                        json.put(field, addressJson);
                        continue;
                    case CmsModelConstant.CITY:
                        JSONObject city = JSONObject.parseObject(cmsModelItem.getDefValue());
                        JSONObject cityJson = new JSONObject();
                        cityJson.put(MemberAttr.PROVINCE_CODE_NAME,
                                city.getString(MemberAttr.PROVINCE_CODE_NAME));
                        cityJson.put(MemberAttr.CITY_CODE_NAME,
                                city.getString(MemberAttr.CITY_CODE_NAME));
                        json.put(field, cityJson);
                        continue;
                    case CmsModelConstant.MANY_CHOOSE:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    case CmsModelConstant.DROP_DOWN:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    case CmsModelConstant.SINGLE_CHOOSE:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    case CmsModelConstant.SEX:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    default:
                        break;
                }
                String value = cmsModelItem.getDefValue();
                if (StringUtils.isNoneBlank(value)) {
                    json.put(field, value);
                }
            }
            return json;
        }
        Map<String, MemberAttr> attrMap = memberAttrs.stream()
                .collect(Collectors.toMap(MemberAttr::getAttrName, MemberAttr -> MemberAttr));
        for (CmsModelItem cmsModelItem : items) {
            String field = cmsModelItem.getField();
            MemberAttr attr = attrMap.get(field);
            /**
             * 特殊处理字段：单图上传、所在地、城市, 多选框
             */
            if (attr != null) {
                switch (attr.getAttrType()) {
                    case CmsModelConstant.SINGLE_CHART_UPLOAD:
                        if (attr.getResourcesSpaceData() != null) {
                            json.put(field, attr.getResourcesSpaceData());
                        } else {
                            json.put(field, "");
                        }
                        continue;
                    case CmsModelConstant.ADDRESS:
                        JSONObject addressJson = new JSONObject();
                        addressJson.put(MemberAttr.PROVINCE_CODE_NAME, attr.getProvinceCode());
                        addressJson.put(MemberAttr.CITY_CODE_NAME, attr.getCityCode());
                        addressJson.put(MemberAttr.AREA_CODE_NAME, attr.getAreaCode());
                        addressJson.put(MemberAttr.ADDRESS_NAME, attr.getAttrValue());
                        json.put(field, addressJson);
                        continue;
                    case CmsModelConstant.CITY:
                        JSONObject cityJson = new JSONObject();
                        cityJson.put(MemberAttr.PROVINCE_CODE_NAME, attr.getProvinceCode());
                        cityJson.put(MemberAttr.CITY_CODE_NAME, attr.getCityCode());
                        json.put(field, cityJson);
                        continue;
                    case CmsModelConstant.MANY_CHOOSE:
                        json.put(field, JSONObject.parseObject(attr.getAttrValue()));
                        continue;
                    case CmsModelConstant.DROP_DOWN:
                        json.put(field, JSONObject.parseObject(attr.getAttrValue()));
                        continue;
                    case CmsModelConstant.SINGLE_CHOOSE:
                        json.put(field, JSONObject.parseObject(attr.getAttrValue()));
                        continue;
                    case CmsModelConstant.SEX:
                        json.put(field, JSONObject.parseObject(attr.getAttrValue()));
                        continue;
                    default:
                        break;
                }
                String value = attr.getAttrValue();
                if (StringUtils.isNoneBlank(value)) {
                    json.put(field, value);
                }
            } else {
                // 如果自定义attr表里没有数据，给默认值字符串
                switch (cmsModelItem.getDataType()) {
                    case CmsModelConstant.SINGLE_CHART_UPLOAD:
                        json.put(field, cmsModelItem.getDefValue());
                        continue;
                    case CmsModelConstant.ADDRESS:
                        JSONObject address = JSONObject.parseObject(cmsModelItem.getDefValue());
                        JSONObject addressJson = new JSONObject();
                        addressJson.put(MemberAttr.PROVINCE_CODE_NAME,
                                address.getString(MemberAttr.PROVINCE_CODE_NAME));
                        addressJson.put(MemberAttr.CITY_CODE_NAME,
                                address.getString(MemberAttr.CITY_CODE_NAME));
                        addressJson.put(MemberAttr.AREA_CODE_NAME,
                                address.getString(MemberAttr.AREA_CODE_NAME));
                        addressJson.put(MemberAttr.ADDRESS_NAME,
                                address.getString(MemberAttr.ADDRESS_NAME));
                        json.put(field, addressJson);
                        continue;
                    case CmsModelConstant.CITY:
                        JSONObject city = JSONObject.parseObject(cmsModelItem.getDefValue());
                        JSONObject cityJson = new JSONObject();
                        cityJson.put(MemberAttr.PROVINCE_CODE_NAME,
                                city.getString(MemberAttr.PROVINCE_CODE_NAME));
                        cityJson.put(MemberAttr.CITY_CODE_NAME,
                                city.getString(MemberAttr.CITY_CODE_NAME));
                        json.put(field, cityJson);
                        continue;
                    case CmsModelConstant.MANY_CHOOSE:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    case CmsModelConstant.DROP_DOWN:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    case CmsModelConstant.SINGLE_CHOOSE:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    case CmsModelConstant.SEX:
                        json.put(field, JSONObject.parseObject(cmsModelItem.getDefValue()));
                        continue;
                    default:
                        break;
                }
                String value = cmsModelItem.getDefValue();
                if (StringUtils.isNoneBlank(value)) {
                    json.put(field, value);
                }
            }
        }
        return json;
    }


    @Override
    public ResponseInfo rectrieve(PcMemberDto dto) throws GlobalException {
        String sessionKey = null;
        // 如果是邮箱找回
        if (dto.getType().equals(PcMemberDto.TYPE_EMAIL)) {
            sessionKey = WebConstants.KCAPTCHA_PREFIX + CODE_SECOND_LEVEL_IDENTITY_RETRIEVE_PSTR
                    + dto.getEmail();

        } else {
            sessionKey = WebConstants.KCAPTCHA_PREFIX + CODE_SECOND_LEVEL_IDENTITY_RETRIEVE_PSTR
                    + dto.getPhone();
        }
        int status = sendSmsUtilService.validateCode(sessionKey, dto.getValidateCode());
        if (STATUS_PASS > status) {
            return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                    VALIDATE_CODE_UNTHROUGH.getDefaultMessage());
        }
        CoreUser member = super.findById(dto.getId());
        // 生成'盐'值字节数组
        byte[] salt = Digests.generateSaltFix();
        // 将字节'盐'值进行转码-->存放到会员信息中
        member.setSalt(Digests.getSaltStr(salt));
        // 将用户密码进行N次加密后再进行'盐'值加密-->生成的加密后的哈希字节数组进行转码-->存放到会员信息中
        member.setPassword(passwdService.entryptPassword(salt, passwdService.decrypt(dto.getpStr())));
        member.setLastPasswordChange(new Date());
        super.update(member);
        super.flush();
        // 成功后从服务端将验证码手动删除
        cacheProvider.clearCache(CacheConstants.SMS, sessionKey);
        return new ResponseInfo();
    }

    @Override
    public CoreUser findByPhone(String phone) throws GlobalException {
        return dao.findByTelephoneAndHasDeleted(phone, false);
    }

    @Override
    public CoreUser findByEmailOrUsername(String key) {
        return dao.findByEmailAndHasDeletedOrUsernameAndHasDeleted(key, false, key, false);
    }

    @Override
    public ResponseInfo savePCMember(MemberRegisterDto bean) throws GlobalException {
        // 注册类型1.无需验证码2.邮箱验证码3.手机验证码4.邮箱，手机验证码
        if (bean.getType().equals(MemberRegisterDto.TYPE_EMAIL)) {
            if (!validMail(bean.getEmail(), null)) {
                return new ResponseInfo(EMAIL_ALREADY_EXIST.getCode(),
                        EMAIL_ALREADY_EXIST.getDefaultMessage());
            }
            // 根据邮箱注册
            String sessionKey = WebConstants.KCAPTCHA_PREFIX + CODE_SECOND_LEVEL_IDENTITY_REGISTER
                    + bean.getEmail();
            int status = sendSmsUtilService.validateCode(sessionKey, bean.getEmailCode());
            if (STATUS_PASS != status) {
                return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                        VALIDATE_CODE_UNTHROUGH.getDefaultMessage());
            }
        } else if (bean.getType().equals(MemberRegisterDto.TYPE_PHONE)) {
            if (!validPhone(bean.getTelephone(), null)) {
                return new ResponseInfo(PHONE_ALREADY_EXIST.getCode(),
                        PHONE_ALREADY_EXIST.getDefaultMessage());
            }
            // 根据手机号注册
            String sessionKey = WebConstants.KCAPTCHA_PREFIX + CODE_SECOND_LEVEL_IDENTITY_REGISTER
                    + bean.getTelephone();
            int status = sendSmsUtilService.validateCode(sessionKey, bean.getMobileCode());
            if (STATUS_PASS != status) {
                return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                        VALIDATE_CODE_UNTHROUGH.getDefaultMessage());
            }
        } else if (bean.getType().equals(MemberRegisterDto.TYPE_ALL)) {
            if (!validMail(bean.getEmail(), null)) {
                return new ResponseInfo(EMAIL_ALREADY_EXIST.getCode(),
                        EMAIL_ALREADY_EXIST.getDefaultMessage());
            }
            if (!validPhone(bean.getTelephone(), null)) {
                return new ResponseInfo(PHONE_ALREADY_EXIST.getCode(),
                        PHONE_ALREADY_EXIST.getDefaultMessage());
            }
            // 根据邮箱注册
            String sessionKeyEmail = WebConstants.KCAPTCHA_PREFIX + CODE_SECOND_LEVEL_IDENTITY_REGISTER
                    + bean.getEmail();
            int status1 = sendSmsUtilService.validateCode(sessionKeyEmail, bean.getEmailCode());
            if (STATUS_PASS != status1) {
                return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                        VALIDATE_CODE_UNTHROUGH.getDefaultMessage());
            }
            // 根据手机号注册
            String sessionKey = WebConstants.KCAPTCHA_PREFIX + CODE_SECOND_LEVEL_IDENTITY_REGISTER
                    + bean.getTelephone();
            int status = sendSmsUtilService.validateCode(sessionKey, bean.getMobileCode());
            if (STATUS_PASS != status) {
                return new ResponseInfo(VALIDATE_CODE_UNTHROUGH.getCode(),
                        VALIDATE_CODE_UNTHROUGH.getDefaultMessage());
            }
        }
        saveMemberCommon(bean);
        return new ResponseInfo();
    }

    /**
     * PC新增会员
     */
    private void saveMemberCommon(MemberRegisterDto bean) throws GlobalException {
        CoreUserExt ext = new CoreUserExt();
        ext.setRealname(bean.getRealname());
        //x1.9.0新增性别
        ext.setGender(bean.getGender());
        // 密码加密
        byte[] salt = Digests.generateSaltFix();
        CoreUser user = new CoreUser();
        // 手机号
        user.setTelephone(bean.getTelephone());
        user.setSalt(Digests.getSaltStr(salt));
        // 解密密码
        String password = passwdService.decrypt(bean.getPassword());
        user.setPassword(passwdService.entryptPassword(salt, password));
        user.setIsResetPassword(true);
        // 默认登录次数为0
        user.setLoginCount(0);
        // 默认登录失败次数为0
        user.setLoginErrorCount(0);
        user.addExt(ext);
        user.setUsername(bean.getUsername());
        user.setTelephone(bean.getTelephone());
        user.setPassMsgHasSend(false);
        // 是否启用
        user.setEnabled(true);
        // 设置为会员
        user.setAdmin(false);
        // 判断会员审核是否通过,需要验证系统设置
        // 会员注册是否需要审核
        Boolean flag = globalConfigService.get().getConfigAttr().getMemberRegisterExamine();
        if (flag) {
            user.setCheckStatus(CoreUser.AUDIT_USER_STATUS_WAIT);
        } else {
            user.setCheckStatus(CoreUser.AUDIT_USER_STATUS_PASS);
        }
        // 设置来源站点
        user.setSourceSiteId(bean.getSiteId());
        user.setSourceSite(cmsSiteService.findById(bean.getSiteId()));
        user.setEmail(bean.getEmail());
        user.setIntegral(0);
        user = super.save(user);
        super.flush();
        // 是否存在自定义字段
        if (bean.getJson() != null) {
            workAttr(bean.getJson(), user.getId());
        }
        // 需要发送注册成功信息
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        if (!flag) {
            sendMemmberMessage(user, MessageTplCodeConstants
                    .MEMBER_AUDIT_PASS_TPL, CoreUser.AUDIT_USER_STATUS_PASS, siteId);
            //注册积分信息
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.REGISTER_SCORE_TYPE,
                    user.getId(), bean.getSiteId(), null);
            // 完善信息积分
            memberScoreDetailsService.addMemberScore(MemberScoreDetails.MESSAGE_SCORE_TYPE, user.getId(),
                    bean.getSiteId(), null);
        }
    }

    @Override
    public MemberInfoVo getPCMemberInfo(Integer memberId) throws GlobalException {
        CmsModel model = cmsModelService.getInfo(null);
        Set<CmsModelItem> items = model.getItems();
        //排序
        List<CmsModelItem> itemList = items.stream().sorted(Comparator.comparing(CmsModelItem::getSortNum))
                .collect(Collectors.toList());
        JSONObject renderingField = new JSONObject();
        JSONArray array = new JSONArray();
        CopyOnWriteArrayList<CmsModelItem> arrayList = new CopyOnWriteArrayList<>(itemList);
        // 过滤密码，过滤用户名,会员组，会员等级，积分，状态
        List<String> list = Arrays.asList(CmsModelConstant.FIELD_MEMBER_PDW,
                CmsModelConstant.FIELD_MEMBER_USERNAME,
                CmsModelConstant.FIELD_MEMBER_USERGROUP, CmsModelConstant.FIELD_MEMBER_USERLEVEL,
                CmsModelConstant.FIELD_MEMBER_INTEGRAL, CmsModelConstant.FIELD_MEMBER_STATUS);
        for (CmsModelItem object : arrayList) {
            // 过滤密码，过滤用户名,会员组，会员等级，积分，状态
            if (list.contains(object.getField())) {
                arrayList.remove(object);
                continue;
            }
            array.add(object.getContentObj());
        }
        renderingField.put("formListBase", array);
        MemberInfoVo vo = new MemberInfoVo();
        // 设置渲染字段
        vo.setRenderingField(renderingField);
        CoreUser member = super.findById(memberId);
        //设置会员模型字段赋值
        JSONObject defaultJson = initDefaultModelItems(member, false);
        // 过滤status
        defaultJson.remove(CmsModelConstant.FIELD_MEMBER_STATUS);
        // 自定义字段
        Set<CmsModelItem> modelItems = model.getItems();
        Set<CmsModelItem> customModelItems = modelItems.stream().filter(modelItem -> modelItem.getIsCustom())
                .collect(Collectors.toSet());
        JSONObject customJson = initCustomModelItems(customModelItems, member.getMemberAttrs());
        // 创建一个新的JSON，将默认字段和自定义字段拼接出的JSON组装成一个JSON
        JSONObject dataFieldJson = new JSONObject();
        dataFieldJson.putAll(defaultJson);
        dataFieldJson.putAll(customJson);
        vo.setDataField(dataFieldJson);
        vo.setId(member.getId());
        if (member.getSourceSite() != null) {
            vo.setSiteName(member.getSourceSite().getName());
        }
        vo.setRegisterTime(MyDateUtils.formatDate(member.getCreateTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN));
        vo.setThird(member.getThird());
        vo.setAdmin(member.getAdmin());
        vo.setLevelIcon(member.getUserLevel() != null
                && member.getUserLevel().getLogoResource() != null
                && member.getSourceSite() != null
                //应前端要求，给绝对路径地址
                ? member.getSourceSite().getUrlWhole()
                + member.getUserLevel().getLogoResource().getUrl().substring(1) : "");
        return vo;
    }

    @Override
    public void clearAllUserCache() {
        List<CoreUser> users = findList(true, null, null, null, true, CoreUser.AUDIT_USER_STATUS_PASS, null,
                null, null, null, new PaginableRequest(0, Integer.MAX_VALUE));
        for (CoreUser user : users) {
            user.clearPermCache();
        }
    }

    /**
     * 系统启动初始化用户权限
     *
     * @Title: initAllUserCache
     * @return: void
     */
    public void initAllUserCache() {
        List<CoreUser> users = findList(true, null, null, null, true, CoreUser.AUDIT_USER_STATUS_PASS, null,
                null, null, null, new PaginableRequest(0, Integer.MAX_VALUE));
        for (CoreUser user : users) {
            user.getOwnerSites();
            user.getOwnerMenus();
            for (CmsSite site : siteService.findAll(false, true)) {
                user.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_SITE, site.getId(), null, null);
                user.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL, site.getId(), null, null);
                user.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT, site.getId(), null, null);
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
//                ThreadPoolService.getInstance().execute(() -> initAllUserCache());
    }


    /**
     * 发送会员消息
     *
     * @param member  用户
     * @param msgCode 消息code
     * @param type    消息发送类型，1审核通过2审核不通过
     * @Title: sendMemmberMessage
     */
    public void sendMemmberMessage(CoreUser member, String msgCode, Short type, Integer sId) throws GlobalException {
        MessageTpl tpl = messageTplService.findByMesCode(msgCode, sId);
        // 未配置消息模板
        if (tpl == null) {
            throw new GlobalException(SettingErrorCodeEnum.MESSAGE_TPL_UNCONFIGURED);
        }
        // 判断是否开启，只发送开启状态的
        List<MessageTplDetails> list = tpl.getDetails().stream()
                .filter(messageTplDetail -> messageTplDetail.getIsOpen()).collect(Collectors.toList());

        for (MessageTplDetails messageTplDetails : list) {
            JSONObject data = new JSONObject();
            Map<String, String> result = new LinkedHashMap<String, String>();
            Integer siteId = messageTplDetails.getMessageTpl().getSiteId();
            // 判断是否审核通过
            if (type.equals(CoreUser.AUDIT_USER_STATUS_NOPASS)) {
                result.put("name", member.getUsername());
                result.put("reason", member.getUserExt().getRemark());
            }
            // 发送邮件
            if (messageTplDetails.getMesType().equals(MessageTplDetails.MESTYPE_MAIL)) {
                if (StringUtils.isNotBlank(member.getEmail())) {
                    // 发送消息
                    data.put(CommonMqConstants.EXT_DATA_KEY_EMAIL, result);
                    mqSendMessageService.sendMemberMsg(MqConstants.ASSIGN_MEMBER, null, null, null,
                            Arrays.asList(MqConstants.RECEIVE_TYPE_MEMBER), msgCode,
                            MessageSceneEnum.USER_MESSAGE,
                            messageTplDetails.getMesTitle(),
                            messageTplDetails.getMesContent(),
                            null,
                            Arrays.asList(member.getEmail()), data,
                            MqConstants.SEND_EMAIL, siteId, null);
                }
            } else if (messageTplDetails.getMesType().equals(MessageTplDetails.MESTYPE_PHONE)) {
                if (StringUtils.isNotBlank(member.getTelephone())) {
                    // 发送短信
                    data.put(CommonMqConstants.EXT_DATA_KEY_SMS, result);
                    mqSendMessageService.sendMemberMsg(MqConstants.ASSIGN_MEMBER, null, null, null,
                            Arrays.asList(MqConstants.RECEIVE_TYPE_MEMBER), msgCode,
                            MessageSceneEnum.USER_MESSAGE,
                            messageTplDetails.getMesTitle(),
                            messageTplDetails.getMesContent(),
                            Arrays.asList(member.getTelephone()), null,
                            data, MqConstants.SEND_SMS, siteId, null);
                }
            }
        }
    }

    @Override
    public ResponseInfo updateMobileSysMember(MobileMemberDto dto) throws Exception {
        CoreUser user = super.findById(dto.getId());
        // 验证电话
        if (StringUtils.isNotBlank(dto.getTelephone())) {
            Boolean validPhone = validPhone(dto.getTelephone(), dto.getId());
            if (!validPhone) {
                return new ResponseInfo(UserErrorCodeEnum.PHONE_ALREADY_EXIST.getCode(),
                        UserErrorCodeEnum.PHONE_ALREADY_EXIST.getDefaultMessage());
            }
        }
        CoreUserExt ext = user.getUserExt();
        ext.setRealname(dto.getRealname());
        user.setUserExt(ext);
        user.setTelephone(dto.getTelephone());
        super.update(user);
        super.flush();
        return new ResponseInfo();
    }

    @Override
    public ResponseInfo updateMobileCustomMember(MobileMemberDto dto) throws Exception {
        MemberAttr attr = new MemberAttr();
        CoreUser user = super.findById(dto.getId());
        // 判断前台传值是否在扩展里面，是就更新，不是就新增
        List<MemberAttr> attrs = user.getMemberAttrs();
        List<String> list = attrs.stream().map(MemberAttr::getAttrName).collect(Collectors.toList());
        if (list.contains(dto.getAttrName())) {
            attr = attrs.stream().filter(x -> x.getAttrName().equals(dto.getAttrName())).findFirst().get();
            attr.setAttrValue(dto.getAttrValue());
            attr.setAttrName(dto.getAttrName());
            attr.setAttrType(dto.getAttrType());
            setValue(attr, dto);
            memberAttrService.update(attr);
        } else {
            attr.setMemberId(dto.getId());
            attr.setAttrValue(dto.getAttrValue());
            attr.setAttrName(dto.getAttrName());
            attr.setAttrType(dto.getAttrType());
            setValue(attr, dto);
            memberAttrService.save(attr);
        }
        return new ResponseInfo();
    }

    @Override
    public Integer getSafeOrAuditUser(List<Integer> userIds, Integer status) {
        Long count = dao.getSafeOrAuditUser(userIds, status);
        return Integer.valueOf(count + "");
    }

    @Override
    public List<CoreUser> getSafeOrAuditUser(Integer status, Boolean enabled) {
        return dao.getSafeOrAuditUser(status, enabled);
    }

    @Override
    public List<CoreUser> findByIds(List<Integer> userIds) {
        return dao.findByIdInAndHasDeleted(userIds, false);
    }

    @Override
    public List<CoreUser> findAdminByIds(List<Integer> userIds) {
        return dao.findByIdInAndAdminAndEnabledAndHasDeleted(userIds, true, true, false);
    }

    @Override
    public Page<CoreUser> pageSafeManageUser(List<Integer> orgids, List<Integer> roleids, String key, List<Integer> notIds, Pageable pageable) {
        return dao.pageSafeManageUser(orgids, roleids, key, notIds, pageable);
    }

    protected void setValue(MemberAttr attr, MobileMemberDto dto) {
        switch (dto.getAttrType()) {
            case CmsModelConstant.SINGLE_CHART_UPLOAD:
                attr.setAttrValue(dto.getAttrValue());
                attr.setResId(Integer.valueOf(dto.getAttrValue()));
                ResourcesSpaceData space = resourcesSpaceDataService
                        .findById(Integer.valueOf(dto.getAttrValue()));
                attr.setResourcesSpaceData(space);
                break;
            case CmsModelConstant.ADDRESS:
                JSONObject address = JSONObject.parseObject(dto.getAttrValue());
                attr.setProvinceCode(address.getString(MemberAttr.PROVINCE_CODE_NAME));
                attr.setCityCode(address.getString(MemberAttr.CITY_CODE_NAME));
                attr.setAreaCode(address.getString(MemberAttr.AREA_CODE_NAME));
                attr.setAttrValue(address.getString(MemberAttr.ADDRESS_NAME));
                break;
            case CmsModelConstant.CITY:
                JSONObject city = JSONObject.parseObject(dto.getAttrValue());
                attr.setProvinceCode(city.getString(MemberAttr.PROVINCE_CODE_NAME));
                attr.setCityCode(city.getString(MemberAttr.CITY_CODE_NAME));
                attr.setAttrValue("");
                break;
            default:
                break;
        }
    }

    @Override
    public void updatePasswdBatch(String passwd, String saltStr) {
        dao.updatePasswdBatch(passwd, saltStr);
    }

    @Override
    public List<Integer> getSafeOrAuditUserIds(Integer status) {
        return dao.getSafeOrAuditUserIds(status);
    }

    @Override
    public Page<CoreUser> pageReinsurance(boolean set, Boolean enabled, List<Integer> orgIds, List<Integer> roleIds,
                                          String key, Integer userSecretId, Pageable pageable) {
        return dao.pageReinsurance(set, enabled, orgIds, roleIds, key, userSecretId, pageable);
    }

    @Override
    public List<CoreUser> getReinsuranceSafeOrAuditUser(Integer status, Boolean enabled) {
        return dao.getReinsuranceSafeOrAuditUser(status, enabled);
    }

    @Override
    public UserStatisticVO getUserStatisticVO() {
        return dao.getUserStatisticVO();
    }

    @Override
    public Page<CoreUser> getUserStatisticPage(String userName, Integer orderType, Pageable pageable) {
        return dao.getUserStatisticPage(userName, orderType, pageable);
    }

    @Override
    public List<CoreUser> getUserStatisticList(String userName, Integer orderType, Boolean deleteFlag) {
        return dao.getUserStatisticList(userName, orderType, deleteFlag);
    }

    /**
     * 付费统计-用户排名Top10
     *
     * @param sortType
     * @return
     */
    @Override
    public List<CoreUser> getUserTopTen(int sortType) {

        return dao.getUserTopTen(sortType);
    }

    @Value("${spring.profiles.active}")
    private String serverModel;
}