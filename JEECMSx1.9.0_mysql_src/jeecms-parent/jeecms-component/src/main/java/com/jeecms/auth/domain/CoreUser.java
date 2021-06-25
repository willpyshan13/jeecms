/**
 * * @date: 2018年3月26日 上午11:10:34
 *
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.auth.domain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.auth.domain.vo.MemberAttrResVo;
import com.jeecms.auth.dto.TokenDetail;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.domain.AbstractDomain;
import com.jeecms.common.base.domain.IBaseUser;
import com.jeecms.common.base.domain.RequestLoginTarget;
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.ChannelExceptionInfo;
import com.jeecms.common.exception.ContentExceptionInfo;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.ChannelErrorCodeEnum;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.exception.error.SiteErrorCodeEnum;
import com.jeecms.common.exception.error.SysOtherErrorCodeEnum;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.PropertiesUtil;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.content.constants.CmsModelConstant;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.Content;
import com.jeecms.content.service.CmsModelService;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.member.domain.MemberAttr;
import com.jeecms.member.domain.MemberGroup;
import com.jeecms.member.domain.MemberLevel;
import com.jeecms.member.domain.SysUserThird;
import com.jeecms.system.domain.*;
import com.jeecms.system.domain.CmsDataPerm.OpeChannelEnum;
import com.jeecms.system.domain.CmsDataPerm.OpeContentEnum;
import com.jeecms.system.domain.CmsDataPerm.OpeSiteEnum;
import com.jeecms.system.domain.dto.CmsSiteAgent;
import com.jeecms.system.exception.SiteExceptionInfo;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.SystemContextUtils;
import com.jeecms.wechat.domain.AbstractWeChatInfo;
import com.jeecms.weibo.domain.WeiboInfo;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户实体类
 *
 * @author: ljw
 *
 */
@Entity
@Table(name = "jc_sys_user")
public class CoreUser extends AbstractDomain<Integer> implements LoginDetail, TokenDetail, IBaseUser, Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(CoreUser.class);

    /**
     * 安全管理员
     */
    public static final int USER_SAFETY_ADMIN = 1;
    /**
     * 审计管理员
     */
    public static final int USER_AUDIT_ADMIN = 2;

    /**
     * 分保：安全员
     */
    public static final int REINSURANCE_SAFETY_ADMIN = 1;
    /**
     * 分保：审计员
     */
    public static final int REINSURANCE_AUDIT_ADMIN = 2;



    private Integer id;
    /** 用户名 **/
    private String username;
    /** 密码 **/
    private String password;
    /** 邮箱 **/
    private String email;
    /** 上次登录的IP **/
    private String lastLoginIp;
    /** 上次登录的时间 **/
    private Date lastLoginTime;
    /** 登录次数 **/
    private Integer loginCount;
    /** 登录错误次数 **/
    private Integer loginErrorCount;
    /** 开始登陆错误时间（登陆成功后会清空） **/
    private Date firstLoginErrorTime;
    /** 是否启用 **/
    private Boolean enabled;
    /** 是否管理员 */
    private Boolean admin;
    /** 密钥 **/
    private String salt;
    /** 上次修改密码的时间 **/
    private Date lastPasswordChange;
    /** 登录限制结束时间 **/
    private Date loginLimitEnd;
    /*** 是否已重置密码 **/
    private Boolean isResetPassword;

    /** 拥有api权限集（未去重） **/
    private String authorityString;
    /** 角色名称 */
    private String roleNames;
    /** 角色集合 **/
    private List<CoreRole> roles;
    /** 角色ID数组 **/
    private Integer[] roleid;
    /** 用户所有数据权限 */
    private Set<CmsDataPerm> dataPerms = new CopyOnWriteArraySet<CmsDataPerm>();
    /** 用户扩展 **/
    private CoreUserExt userExt;
    /** 用户组ID **/
    private Integer groupId;
    /** 用户等级ID **/
    private Integer levelId;
    /** 用户手机号 **/
    private String telephone;
    /** 用户审核状态(1审核通过、2审核不通过 0待审核) **/
    private Short checkStatus;
    /** 用户管理站点 */
    private Set<CmsSite> sites = new HashSet<CmsSite>(10);
    /** 用户菜单 */
    private List<CoreMenu> menus = new ArrayList<CoreMenu>(100);
    /** 用户所属组织id */
    private Integer orgId;
    /** 用户所属组织 */
    private CmsOrg org;
    /** 积分 **/
    private Integer integral;
    /** 来源站点(非管理员，此字段必填) **/
    private Integer sourceSiteId;
    /** 人员密级 **/
    private Integer userSecretId;
    /** 会员组 **/
    private MemberGroup userGroup;
    /** 会员等级 **/
    private MemberLevel userLevel;
    /** 新增权限配置 */
    private List<CmsDataPermCfg> permCfgs;

    private String serverMode;
    /** 人员密级 **/
    private SysUserSecret userSecret;
    /** 来源站点对象 **/
    private CmsSite sourceSite;
    /** 会员自定义属性列表 **/
    private List<MemberAttr> memberAttrs;
    /** 会员第三方配置 **/
    private List<SysUserThird> memberThirds;
    /** 是否来源于第三方登录 **/
    private Boolean third;
    /** 提醒修改密码消息是否已发 **/
    private Boolean passMsgHasSend;
    /** 用户点赞评论 **/
    private List<UserComment> likeComments;
    /** 用户点赞内容 **/
    private List<Content> likeContents;
    /** 关联管理微博账户 **/
    private List<WeiboInfo> weiboInfos = new ArrayList<WeiboInfo>(10);
    /** 关联管理微博账户 **/
    private List<AbstractWeChatInfo> wechatInfos = new ArrayList<AbstractWeChatInfo>(10);
    /******************用户SSO配置***********************/
    /**应用APPID**/
    private String appId;
    /**
     * 三元管理安全管理员
     */
    private Boolean safetyAdmin;
    /**
     * 三元管理审计管理员
     */
    private Boolean auditAdmin;
    /**
     * 分级保护安全员
     */
    private Boolean safetyReinsuranceAdmin;
    /**
     * 分级保护审计员
     */
    private Boolean auditReinsuranceAdmin;

    /** 针对付费打赏新增字段**/

    /**
     * 用户余额（周期结算后需更新）
     */
    //private Long balance;

    /**
     * 累计收益（不受结算周期影响）
     */
    //private Long totalAmount;

    /**
     * 累计提现金额 （单位毫 1分=100毫）
     */
    //private Long withdrawalAmount;

    /**
     * 累计打赏金额（单位毫 1分=100毫）
     */
    //private Long rewardAmount;

    /**
     * 累计内容付费金额（单位毫 1分=100毫）
     */
    //private Long paidAmount;

    /**
     * 待结算金额（单位毫 1分=100毫）
     */
    //private Long waitingSettlementAmount;

    private UserIncome userIncome;


    public void setServerMode(String serverMode) {
        this.serverMode = serverMode;
    }

    @Override
    @Transient
    public Integer getUserId() {
        return getId();
    }

    /**
     * 获取字段map key为字段名，value为字段值{资源类型是文件别名，城市地址是详细拼接}
     * @return
     */
    @Transient
    public Map<String,Object> getAttrMap() {
        List<MemberAttr> attrs = getMemberAttrs();
        Map<String,Object> map = new HashMap<>();
        CmsModelService modelService = ApplicationContextProvider.getBean(CmsModelService.class);
        CmsModel model = modelService.getInfo(null);
        for (MemberAttr a : attrs) {
            /**表单存在字段才显示*/
            if(model.existItem(a.getAttrName())){
                if(a.getResourcesSpaceData()!=null){
                    MemberAttrResVo vo = new MemberAttrResVo();
                    MyBeanUtils.copyProperties(a,vo);
                    /**单资源*/
                    map.put(a.getAttrName(),vo);
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_CITY.equals(a.getAttrType())
                        ||CmsFormConstant.FIELD_ATTR_FILE_PROP_ADDRESS.equals(a.getAttrType())){
                    /**城市地址类型*/
                    map.put(a.getAttrName(),a.getAddress());
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_ITEM.equals(a.getAttrType())){
                    /**单选 下拉 类型等使用option的label 从表单字段的option map 取lable*/
                    String value = a.getAttrValue();
                    String label = model.getItem(a.getAttrName()).getOptionLabel(value);
                    map.put(a.getAttrName(),label);
                }else if(CmsFormConstant.FIELD_ATTR_FILE_PROP_ITEM_MULTI.equals(a.getAttrType())){
                    /**多选、级联 选择等使用option的label 从表单字段的option map 取lable*/
                    List<String> optionLabels = new ArrayList<String>();
                    /**数组字符串*/
                    List<String> vals = JSONArray.parseArray(a.getAttrValue(), String.class);
                    for(String v:vals){
                        String label = model.getItem(a.getAttrName()).getOptionLabel(v);
                        String value = label;
                        optionLabels.add(value);
                    }
                    map.put(a.getAttrName(),optionLabels);
                }else{
                    map.put(a.getAttrName(),a.getAttrValue());
                }
            }
        }
        return map;
    }

    /**
     * 是否不是当前登录用户
     *
     * @Title: getNotCurrUser
     * @return: boolean
     */
    @Transient
    public boolean getNotCurrUser() {
        CoreUser user = SystemContextUtils.getCoreUser();
        if (user != null) {
            if (user.getId().equals(getId())) {
                return false;
            }
        }
        return true;

    }

    /**
     * 获取配置模式
     *
     * @Title: getServerMode
     * @return String
     */
    @Transient
    public String getServerMode() {
        if (StringUtils.isBlank(serverMode)) {
            try {
                String mode = PropertiesUtil.loadSystemProperties()
                        .getProperty("spring.profiles.active");
                setServerMode(mode);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return serverMode;
    }

    @Override
    @Transient
    public RequestLoginTarget getUserSource() {
        return RequestLoginTarget.admin;
    }

    @Transient
    public Integer[] getRoleid() {
        return roleid;
    }

    public void setRoleid(Integer[] roleid) {
        this.roleid = roleid;
    }

    @Transient
    public Boolean getHaveSafeManageMenu() {
        Set<CoreMenu> ownerMenus = getOwnerMenus();
        for (CoreMenu menu:ownerMenus) {
            if ("threeMemberAudit".equals(menu.getName())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean getHaveSafeReinsuranceMenu() {
        Set<CoreMenu> ownerMenus = getOwnerMenus();
        List<String> menuNames = CollectionUtils.isEmpty(ownerMenus) ? new ArrayList<>() : ownerMenus.stream().map(CoreMenu::getName).collect(Collectors.toList());
        if (menuNames.contains("hierarchicalSecret")) {
            if (menuNames.contains("hierarchicalJournal")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean getHaveAuditReinsuranceMenu() {
        Set<CoreMenu> ownerMenus = getOwnerMenus();
        List<String> menuNames = CollectionUtils.isEmpty(ownerMenus) ? new ArrayList<>() : ownerMenus.stream().map(CoreMenu::getName).collect(Collectors.toList());
        if (menuNames.contains("hierarchicalJournal")) {
            return true;
        }
        return false;
    }

    /**
     * 获取用户功能权限
     *
     * @Title: getAuthorityString
     * @return String
     */
    @Transient
    public String getAuthorityString() {
        StringBuilder sb = new StringBuilder();
        Set<CoreMenu> ownerMenus = getOwnerMenus();
        for (CoreMenu menu : ownerMenus) {
            List<CoreApi> apis = menu.getApis();
            for (CoreApi api : apis) {
                /** 权限字符串 类似 GET:/admin/user/list,PUT:/admin/user/ */
                sb = sb.append(CoreApi.getMethod(api.getRequestMethod()) + ":" + api.getApiUrl() + ",");
            }
        }
        if (sb.toString().length() >= 1) {
            this.authorityString = sb.toString().substring(0, sb.length() - 1);
        }
        return authorityString;
    }

    /**
     * 是否需要修改密码 首次登录是否强制要求修改密码 强制要求用户定期修改密码、 重置密码后首次登录是否强制要求修改密码
     *
     * @Title: getNeedUpdatePassword
     * @return: boolean
     */
    @Transient
    public boolean getNeedUpdatePassword() {
        GlobalConfig config = null;
        try {
            config = ApplicationContextProvider.getBean(GlobalConfigService.class).get();
        }catch (GlobalException e){
        }
        boolean needUpdate = false;
        if(config!=null){
            /**未开启直接返回*/
            if (!config.getConfigAttr().getSecurityOpen()) {
                return needUpdate;
            }
            /** 强制要求用户定期修改密码 */
            if (config.getPassRegularChange()) {
                needUpdate = isPassNeedRegularChange(config, getLastPasswordChange());
                if (needUpdate) {
                    return needUpdate;
                }
            }
            /** 首次登录是否强制要求修改密码 */
            if (config.getPassFirstNeedUpdate()) {
                /**未修改密码则需要修改，应对初次登录直接跳出未修改密码场景*/
                if (!getIsResetPassword()) {
                    needUpdate = true;
                }
                /**初次登录标注需要修改*/
                if (getLoginCount() < 1) {
                    needUpdate = true;
                }
                if (needUpdate) {
                    return needUpdate;
                }
            }
            /** 重置密码后首次登录是否强制要求修改密码 ,后台重置密码那必须修改用户的该字段为false 用户主动修改密码后更改为true */
            if (config.getPassResetNeedUpdate()) {
                needUpdate = !getIsResetPassword();
            }
        }
        return needUpdate;
    }

    /**
     * 是否到了密码强制周期修改时间(用户从未修改过密码则不能超过 最后调整设置的时间差)
     *
     * @Title: isPassNeedRegularChange
     * @param config
     *                GlobalConfig
     * @param lastChangeTime
     *                用户最后修改时间
     * @return: boolean
     */
    private boolean isPassNeedRegularChange(GlobalConfig config, Date lastChangeTime) {
        Date now = Calendar.getInstance().getTime();
        if (lastChangeTime == null) {
            return MyDateUtils.getDaysBetweenDate(config.getPassRegularChangeSetTime(), now) >= config
                    .getPassRegularCycle();
        } else {
            return MyDateUtils.getDaysBetweenDate(lastChangeTime, now) >= config.getPassRegularCycle();
        }
    }

    @Transient
    public CopyOnWriteArraySet<CmsSite> getCloneOwnerSites() {
        CopyOnWriteArraySet<CmsSite> cloneSites = new CopyOnWriteArraySet<CmsSite>();
        Iterator<CmsSite> iterator = getOwnerSites().iterator();
        while (iterator.hasNext()) {
            cloneSites.add(iterator.next().clone());
        }
        return cloneSites;
    }

    /**
     * 获取用户站群权限,用户未配置获取 汇总的角色配置权限，角色未配置则获取组织配置的站群权限
     *
     * @Title: getOwnerSites
     * @return Set
     */
    @Transient
    public CopyOnWriteArraySet<CmsSite> getOwnerSites() {
        CopyOnWriteArraySet<CmsSite> ownerSites = new CopyOnWriteArraySet<CmsSite>();
        CacheProvider cache = ApplicationContextProvider.getBean(CacheProvider.class);
        String cacheKey = CmsDataPerm.getUserCacheKey(CmsDataPerm.DATA_TYPE_SITE_OWNER);
        if (StringUtils.isNoneBlank(cacheKey)) {
            Object cacheDataPerms = cache.getCache(cacheKey, getId().toString());
            if (cacheDataPerms != null) {
                CopyOnWriteArraySet<CmsSite> set = (CopyOnWriteArraySet<CmsSite>) cacheDataPerms;
                if (set != null && set.size() > 0) {
                    ownerSites.addAll(set.stream().filter(site -> !site.getIsDelete()).filter(site -> !site.getHasDeleted())
                            .collect(Collectors.toSet()));
                    ownerSites = filterOwnerSites(ownerSites);
                    CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
                    List<CmsSite> sites = siteService.findByIds(CmsSite.fetchIds(ownerSites));
                    return new CopyOnWriteArraySet(sites);
                }
            }
        }
        if (getHasAssignOwnerSite()) {
            ownerSites.addAll(getEffectSites());
        } else {
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (role.getHasAssignOwnerSite()) {
                        ownerSites.addAll(role.getCloneOwnerSites());
                    } else {
                        ownerSites.addAll(role.getOrg().getCloneOwnerSites());
                    }
                }
            } else {
                /** 最后取部门的数据权限 */
                if (getOrg() != null) {
                    ownerSites.addAll(getOrg().getCloneOwnerSites());
                }
            }
        }
        ownerSites = filterOwnerSites(ownerSites);
        if (StringUtils.isNoneBlank(cacheKey) && ownerSites != null && ownerSites.size() > 0) {
            CmsSiteAgent.initSiteChild(ownerSites);
            cache.setCache(cacheKey, getId().toString(), ownerSites);
        }
        return ownerSites;
    }


    /**
     * 过滤站群权限(若用户单独配置权限，则限制在组织最大权限， 若未单独配置且角色不为空则限制在角色最大权限，若未单独配置且角色空则限制在组织最大权限)
     *
     * @Title: filterOwnerSites
     * @param ownerSites
     *                站群权限
     * @return CopyOnWriteArraySet
     */
    private CopyOnWriteArraySet<CmsSite> filterOwnerSites(CopyOnWriteArraySet<CmsSite> ownerSites) {
        CopyOnWriteArraySet<CmsSite> maxOwnerSites = new CopyOnWriteArraySet<CmsSite>();
        boolean filterByRole = false;
        if (!getHasAssignOwnerSite()) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            for (CoreRole role : getRoles()) {
                maxOwnerSites.addAll(role.getOwnerSites());
            }
        } else {
            if (getOrg() != null) {
                maxOwnerSites = getOrg().getOwnerSites();
            }
        }
        /** 最大权限不包含 则移除 */
        for (CmsSite site : ownerSites) {
            if (!CmsSite.fetchIds(maxOwnerSites).contains(site.getId())) {
                ownerSites.remove(site);
            }
        }
        return ownerSites;
    }

    /**
     * 是否单独分配了站群权限
     *
     * @Title: getHasAssignOwnerSite
     * @return boolean
     */
    @Transient
    public boolean getHasAssignOwnerSite() {
        Set<CmsSite> sites = getEffectSites();
        if (sites != null && !sites.isEmpty()) {
            return true;
        }
        if (getHasAssignNewSiteOwner() != null && getHasAssignNewSiteOwner()) {
            return true;
        }
        return false;
    }

    /**
     * 获取用户菜单权限,用户未配置获取 汇总的角色菜单权限，角色未配置则获取组织配置的菜单权限
     *
     * @Title: getOwnerMenus
     * @return Set
     */
    @Transient
    public Set<CoreMenu> getOwnerMenus() {
        CopyOnWriteArraySet<CoreMenu> ownerMenus = new CopyOnWriteArraySet<CoreMenu>();
        CacheProvider cache = ApplicationContextProvider.getBean(CacheProvider.class);
        String cacheKey = CmsDataPerm.getUserCacheKey(CmsDataPerm.DATA_TYPE_MENU);
        if (StringUtils.isNoneBlank(cacheKey)) {
            Object cacheDataPerms = cache.getCache(cacheKey, getId().toString());
            if (cacheDataPerms != null) {
                CopyOnWriteArraySet<CoreMenu> set = (CopyOnWriteArraySet<CoreMenu>) cacheDataPerms;
                if (set != null && set.size() > 0) {
                    return set;
                }
            }
        }
        if (getHasAssignOwnerMenu()) {
            ownerMenus.addAll(getMenus());
        } else {
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (getHasAssignOwnerMenu()) {
                        ownerMenus.addAll(role.getOwnerMenus());
                    } else {
                        ownerMenus.addAll(role.getOrg().getOwnerMenus());
                    }
                }
            } else {
                ownerMenus.addAll(
                        getOrg() != null ? getOrg().getOwnerMenus() : new HashSet<CoreMenu>());
            }
        }
        ownerMenus = filterOwnerMenus(ownerMenus);
        if (StringUtils.isNoneBlank(cacheKey) && ownerMenus != null && ownerMenus.size() > 0) {
            /** 主动拉取下延迟加载的api集合 */
            for (CoreMenu m : ownerMenus) {
                logger.debug(m.getMenuName() + " api->" + m.getApiUrls());
            }
            cache.setCache(cacheKey, getId().toString(), ownerMenus);
        }
        return ownerMenus;
    }

    @Transient
    public CopyOnWriteArraySet<CoreMenu> getCloneOwnerMenus() {
        CopyOnWriteArraySet<CoreMenu> menus = new CopyOnWriteArraySet<CoreMenu>();
        Iterator<CoreMenu> iterator = getOwnerMenus().iterator();
        while (iterator.hasNext()) {
            menus.add(iterator.next().clone());
        }
        return menus;
    }

    /**
     * 过滤 用户权限,用户未配置且角色不为空则过滤不在角色的权限，用户未配置且角色为空 和用户单独配置则过滤不在组织的权限
     *
     * @Title: filterOwnerMenus
     * @param ownerMenus
     *                菜单权限
     * @return CopyOnWriteArraySet
     */
    private CopyOnWriteArraySet<CoreMenu> filterOwnerMenus(CopyOnWriteArraySet<CoreMenu> ownerMenus) {
        HashSet<CoreMenu> maxOwnerMenus = new HashSet<CoreMenu>();
        boolean filterByRole = false;
        if (!getHasAssignOwnerMenu()) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            for (CoreRole role : getRoles()) {
                maxOwnerMenus.addAll(role.getOwnerMenus());
            }
        } else {
            if (getOrg() != null) {
                maxOwnerMenus = getOrg().getOwnerMenus();
            }
        }
        /** 最大权限不包含 则移除 */
        for (CoreMenu menu : ownerMenus) {
            if (!CoreMenu.fetchIds(maxOwnerMenus).contains(menu.getId())) {
                ownerMenus.remove(menu);
            }
        }
        return ownerMenus;
    }

    /**
     * 是否单独分配菜单权限
     *
     * @Title: getHasAssignOwnerMenu
     * @return boolean
     */
    @Transient
    public boolean getHasAssignOwnerMenu() {
        /** 只要配置了权限 */
        if (!getMenuIsOnlyHide()) {
            return true;
        }
        /** 指定了新的权限 */
        if (getHasAssignNewMenuOwner() != null && getHasAssignNewMenuOwner()) {
            return true;
        }
        return false;
    }

    @Transient
    public List<CmsDataPerm> getOwnerDataPermsByType(Short type) {
        List<CmsSite> sites = new ArrayList<CmsSite>();
        List<CmsDataPerm> perms = new ArrayList<>();
        if (CmsDataPerm.DATA_TYPE_SITE.equals(type)) {
            List<CmsDataPerm> sitePerms = new CopyOnWriteArrayList<>();
            sitePerms.addAll(getOwnerDataPermsByType(type, null, null, null));
            sitePerms = CmsDataPerm.sort(sitePerms);
            perms.addAll(sitePerms);
        } else {
            CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
            sites.addAll(siteService.findAll(false, true));
            /**将权限数据排序好后续用作比较权限字符串拼接*/
            List<CmsSite> sortSites = CmsSiteAgent.sortByIdAndChild(sites);
            for (CmsSite s : sortSites) {
                List<CmsDataPerm> sitePerms = new CopyOnWriteArrayList<>();
                sitePerms.addAll(getOwnerDataPermsByType(type, s.getId(), null, null));
                sitePerms = CmsDataPerm.sort(sitePerms);
                perms.addAll(sitePerms);
            }
        }
        return perms;
    }

    /**
     * 获取用户数据权限，用户单独配置了取用户的数据权限，用户未配置但是配置了角色则获取角色的数据权限，否则取组织的数据权限
     *
     * @Title: getOwnerDataPermsByType
     * @param type
     *                数据类型 类型 1站点数据权限 2栏目 3文档
     * @param siteId
     *                站点Id
     * @param operator
     *                操作
     * @param dataId
     *                数据ID
     * @return Set
     */
    @Transient
    public Set<CmsDataPerm> getOwnerDataPermsByType(Short type, Integer siteId, Short operator,
                                                                    Integer dataId) {
        Set<CmsDataPerm> perms = new HashSet<>();
        CacheProvider cache = ApplicationContextProvider.getBean(CacheProvider.class);
        String cacheKey = CmsDataPerm.getUserCacheKey(type);
        //权限是否来自顶层组织
        boolean permFromSuper = false;
        if (StringUtils.isNoneBlank(cacheKey)) {
            Object cacheDataPerms = cache.getCache(cacheKey, getCacheId(type, siteId));
            if (cacheDataPerms != null) {
                /** 站点ID统一后续过滤，缓存中存放权限体的所有站点的数据权限 */
                perms = (Set<CmsDataPerm>) cacheDataPerms;
                perms = CmsDataPerm.streamFilter(perms, siteId, operator, dataId);
                CmsDataPerm.initPermDataChannel(perms,type);
                return perms;
            }
        }
        /** 优先取人员设置的数据权限 */
        if (getHasAssignDataPermsByType(type)) {
            perms = getDataPermsByType(type, siteId, null, null);
        } else {
            /** 其次获取角色的数据权限 */
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (role.getHasAssignDataPermsByType(type)) {
                        perms.addAll(role.getOwnerDataPermsByType(type, siteId, null, null));
                    } else {
                        perms.addAll(role.getOrg().getOwnerDataPermsByType(type, siteId, null,
                                null, true));
                        if (role.getOrg().isTop()) {
                            permFromSuper = true;
                        }
                    }
                }
            } else {
                /** 最后取部门的数据权限 */
                if (getOrg() != null) {
                    perms.addAll(getOrg().getOwnerDataPermsByType(type, siteId, null, null, true));
                    if (getOrg().isTop()) {
                        permFromSuper = true;
                    }
                }
            }
        }
        /**来自顶层组织不用过滤了*/
        if (!permFromSuper) {
            perms = filterOwnerDataPermsByType(perms, type, siteId, null, null);
        }
        if (StringUtils.isNoneBlank(cacheKey)) {
            cache.setCache(cacheKey, getCacheId(type, siteId), (Serializable) perms);
        }
        /** 站点ID统一后续过滤，缓存中存放权限体的所有站点的数据权限，组织、角色、用户需要获取到上级组织或者所属组织的数据权限设定的站点 */
        /**栏目和内容权限已经按站点分开存储*/
        Set<CmsDataPerm> set =  CmsDataPerm.streamFilter(perms, siteId, operator, dataId);
        CmsDataPerm.initPermDataChannel(set,type);
        return set;
    }

    /**
     * 权限是否来自顶层组织，顶层组织拥有所有权限
     * @param type
     * @return
     */
    @Transient
    public boolean getOwnerDataPermsByTypeFromSuper(Short type) {
        if (getHasAssignDataPermsByType(type)) {
            return false;
        } else {
            /** 其次获取角色的数据权限 */
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (!role.getHasAssignDataPermsByType(type)) {
                        if (role.getOrg() != null && role.getOrg().isTop()) {
                            return true;
                        }
                    }
                }
            } else {
                /** 最后取部门的数据权限 */
                if (getOrg() != null && getOrg().isTop()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 过滤 用户权限,用户未配置且角色不为空则过滤不在角色的权限，用户未配置且角色为空 和用户单独配置则过滤不在组织的权限
     *
     * @Title: filterOwnerDataPermsByType
     * @param ownerPerms
     *                数据权限
     * @param type
     *                数据类型
     * @param operator
     *                操作
     * @param dataId
     *                数据ID
     * @return CopyOnWriteArraySet
     */
    private Set<CmsDataPerm> filterOwnerDataPermsByType(Set<CmsDataPerm> ownerPerms,
                                                                        Short type, Integer siteId, Short operator, Integer dataId) {
        if(ownerPerms.isEmpty()){
            return ownerPerms;
        }
        Date lastDataPermModifyTime= ownerPerms.iterator().next().getCreateTime();
        final Set<CmsDataPerm> maxOwnerPerms = new HashSet<>();
        boolean filterByRole = false;
        if (!getHasAssignDataPermsByType(type)) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            /***若是角色修改时间 都在用户权限修改时间之前 则说明角色权限未调整，则直接返回*/
            boolean roleUpdateBeforeUserPermModify = true;
            for (CoreRole role : getRoles()) {
                if(role.getUpdateTime()!=null&&role.getUpdateTime().after(lastDataPermModifyTime)){
                    roleUpdateBeforeUserPermModify = false;
                }
            }
            if(roleUpdateBeforeUserPermModify){
                return ownerPerms;
            }
            for (CoreRole role : getRoles()) {
                maxOwnerPerms.addAll(role.getOwnerDataPermsByType(type, siteId, operator, null));
            }
        } else {
            if (getOrg() != null) {
                /***若是组织修改时间 都在用户权限修改时间之前 则说明组织权限未调整，则直接返回*/
                boolean orgUpdateBeforeUserPermModify = true;
                if(org.getUpdateTime()!=null&&org.getUpdateTime().after(lastDataPermModifyTime)){
                    orgUpdateBeforeUserPermModify = false;
                }
                if(orgUpdateBeforeUserPermModify){
                    return ownerPerms;
                }
                maxOwnerPerms.addAll(getOrg().getOwnerDataPermsByType(type, siteId, operator, null, true));
            }
        }
        /** 最大权限不包含 则移除 */
        if (maxOwnerPerms.size() != ownerPerms.size()) {
            Set<CmsDataPerm> filterOwnerPerms = ownerPerms.stream().filter(item -> item.beContains(maxOwnerPerms)).collect(Collectors.toSet());
            ownerPerms.clear();
            ownerPerms.addAll(filterOwnerPerms);
        }
        return ownerPerms;
    }

    /**
     * 获取所有站点的数据权限
     * @param type 权限类型 类型 1站点数据权限 2栏目 3文档
     * @return
     */
    @Transient
    public CopyOnWriteArraySet<CmsDataPerm> getAllOwnerDataPermsByType(Short type) {
        CopyOnWriteArraySet<CmsDataPerm> set = new CopyOnWriteArraySet<CmsDataPerm>();
        List<CmsSite> sites = new ArrayList<CmsSite>();
        CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
        sites.addAll(siteService.findAll(false, true));
        for (CmsSite site : sites) {
            set.addAll(getOwnerDataPermsByType(type, site.getId(), null, null));
        }
        return set;
    }

    /**
     * 获取用户单独分配的数据权限
     *
     * @Title: getDataPermsByType
     * @param type
     *                数据类型 类型 1站点数据权限 2栏目 3文档
     * @param operator
     *                操作
     * @param siteId
     *                站点Id
     * @param dataId
     *                数据ID
     * @return Set
     */
    @Transient
    public Set<CmsDataPerm> getDataPermsByType(Short type, Integer siteId, Short operator,
                                                               Integer dataId) {
        Set<CmsDataPerm> dataPerms = new HashSet<>();
        try {
            /**此处有脏数据忽略，下面过滤*/
            dataPerms.addAll(getDataPerms());
        }catch (Exception e){
        }
        Set<CmsDataPerm>  perms = new HashSet<>();
        if (type != null && !dataPerms.isEmpty()) {
            Supplier< Stream<CmsDataPerm>> cmsDataPermStream =  () -> dataPerms.stream().filter(p -> p != null).filter(p -> type.equals(p.getDataType()))
                    .filter(p -> (p.getHasDeleted() == null) || (p.getHasDeleted() != null && !p.getHasDeleted()));
            if(siteId!=null) {
                cmsDataPermStream.get().filter(p -> (p.getSiteId() == null)&&p.getSiteId().equals(siteId));
            }
            if(operator!=null) {
                cmsDataPermStream.get().filter(p -> (p.getOperation() == null)&&p.getOperation().equals(operator));
            }
            if(dataId!=null) {
                cmsDataPermStream.get().filter(p -> (p.getDataId() == null)&&p.getDataId().equals(dataId));
            }
            Set<CmsDataPerm> set = cmsDataPermStream.get().collect(Collectors.toSet());
            dataPerms.clear();
            dataPerms.addAll(set);
        }
//        CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
//        ChannelService channelService = ApplicationContextProvider.getBean(ChannelService.class);
//        for (CmsDataPerm d : dataPerms) {
//            if (d.getDataId() != null) {
//                try {
//                    /** 过滤不存在的数据和逻辑删除的数据 */
//                    if (CmsDataPerm.DATA_TYPE_CHANNEL.equals(type)
//                            || CmsDataPerm.DATA_TYPE_CONTENT.equals(type)) {
//                        Channel c = channelService.get(d.getDataId());
//                        if (c == null || c != null && c.getHasDeleted()) {
////                            dataPerms.remove(d);
//                            continue;
//                        }
//                    }
//                    if (CmsDataPerm.DATA_TYPE_SITE.equals(type)
//                            || CmsDataPerm.DATA_TYPE_SITE_OWNER.equals(type)) {
//                        CmsSite c = siteService.get(d.getDataId());
//                        if (c == null || c != null && c.getHasDeleted()) {
////                            dataPerms.remove(d);
//                            continue;
//                        }
//                    }
//                } catch (Exception e) {
////                    dataPerms.remove(d);
//                    continue;
//                }
//            }
//            perms.add(d);
//        }
        return dataPerms;
    }

    /**
     * 获取组织是否单独分配数据权限
     *
     * @Title: getHasAssignDataPermsByType
     * @param type
     *                类型 1站点数据权限 2栏目 3文档
     * @return boolean
     */
    @Transient
    public boolean getHasAssignDataPermsByType(Short type) {
        if (type != null) {
            if (type.equals(CmsDataPerm.DATA_TYPE_SITE) && StringUtils.isNotBlank(getNewSiteOpe())) {
                return true;
            } else if (type.equals(CmsDataPerm.DATA_TYPE_CHANNEL)
                    && StringUtils.isNotBlank(getAllNewChannelOpe())) {
                return true;
            } else if (type.equals(CmsDataPerm.DATA_TYPE_CONTENT)
                    && StringUtils.isNotBlank(getAllNewChannelOpeContent())) {
                return true;
            }
        }
        Set<CmsDataPerm> perms = getDataPermsByType(type, null, null, null);
        if (perms != null && !perms.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 获取权限配置
     *
     * @Title: getPermCfg
     * @return CmsDataPermCfg
     */
    @Transient
    public CmsDataPermCfg getPermCfg() {
        List<CmsDataPermCfg> cfgs = getPermCfgs();
        if (cfgs != null && !cfgs.isEmpty()) {
            return cfgs.get(0);
        }
        return null;
    }

    /**
     * 获取栏目类新增权限配置
     *
     * @Title: getPermCfgForChannel
     * @param siteId
     *                站点ID
     * @return CmsDataPermCfg
     */
    @Transient
    public CmsDataPermCfg getPermCfgForChannel(Integer siteId) {
        if (siteId != null && getPermCfgs() != null) {
            List<CmsDataPermCfg> cfgs = getPermCfgs().stream().filter(cfg -> siteId.equals(cfg.getSiteId()))
                    .collect(Collectors.toList());
            if (cfgs != null && !cfgs.isEmpty()) {
                return cfgs.get(0);
            }
        } else {
            return getPermCfg();
        }
        return null;
    }

    /**
     * 是否有单独分配 新增站点的站群权限
     *
     * @Title: getHasAssignNewSiteOwner
     * @return Boolean
     */
    @Transient
    public Boolean getHasAssignNewSiteOwner() {
        CmsDataPermCfg cfg = getPermCfg();
        if (cfg != null) {
            return cfg.getNewSiteOwner();
        }
        return null;
    }

    /**
     * 是否有新增站点的站群权限
     *
     * @Title: getOwnerNewSiteOwner
     * @return Boolean
     */
    @Transient
    public Boolean getOwnerNewSiteOwner() {
        Boolean newSiteOwner = null;
        if (getHasAssignNewSiteOwner() != null) {
            newSiteOwner = getHasAssignNewSiteOwner();
        } else {
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    Boolean roleNewSiteOwner = role.getOwnerNewSiteOwner();
                    if (roleNewSiteOwner != null && roleNewSiteOwner) {
                        newSiteOwner = true;
                        break;
                    }
                }
            }
        }
        if (newSiteOwner == null&&getOrg()!=null) {
            newSiteOwner = getOrg().getOwnerNewSiteOwner();
        }
        newSiteOwner = filterOwnerNewSiteOwner(newSiteOwner);
        return newSiteOwner;
    }

    /**
     * 过滤 用户权限,用户未配置且角色不为空则过滤不在角色的权限，用户未配置且角色为空 和用户单独配置则过滤不在组织的权限
     *
     * @Title: filterOwnerNewSiteOwner
     * @param ownerNewSiteOwner
     *                新建站点权限
     * @return Boolean
     */
    private Boolean filterOwnerNewSiteOwner(Boolean ownerNewSiteOwner) {
        boolean filterByRole = false;
        if (!getHasAssignOwnerSite()) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            boolean allRoleNotExistNewSiteOwner = true;
            for (CoreRole role : getRoles()) {
                if (role.getOwnerNewSiteOwner() != null && role.getOwnerNewSiteOwner()) {
                    allRoleNotExistNewSiteOwner = false;
                }
            }
            /** 用户所有角色均没有新建站点权限，则过滤为没有新建站点权限 */
            if (allRoleNotExistNewSiteOwner) {
                return false;
            }
        } else {
            if (getOrg() != null) {
                /** 组织若未配置新站点的站群权限，则过滤为也没有权限 */
                Boolean orgOwnerNewSiteOwner = getOrg().getOwnerNewSiteOwner();
                if (orgOwnerNewSiteOwner != null && !orgOwnerNewSiteOwner) {
                    return false;
                }
            }
        }
        return ownerNewSiteOwner;
    }

    /**
     * 是否有单独分配 新增菜单的权限
     *
     * @Title: getHasAssignNewMenuOwner
     * @return Boolean
     */
    @Transient
    public Boolean getHasAssignNewMenuOwner() {
        CmsDataPermCfg cfg = getPermCfg();
        if (cfg != null) {
            return cfg.getNewMenuOwner();
        }
        return null;
    }

    /**
     * 是否有新增菜单的权限
     *
     * @Title: getOwnerNewMenuOwner
     * @return Boolean
     */
    @Transient
    public Boolean getOwnerNewMenuOwner() {
        Boolean newMenuOwner = null;
        if (getHasAssignNewMenuOwner() != null) {
            newMenuOwner = getHasAssignNewMenuOwner();
        } else {
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    Boolean roleNewMenuOwner = role.getOwnerNewMenuOwner();
                    if (roleNewMenuOwner != null && roleNewMenuOwner) {
                        newMenuOwner = true;
                        break;
                    }
                }
            }
        }
        if (newMenuOwner == null) {
            newMenuOwner = getOrg().getOwnerNewMenuOwner();
        }
        newMenuOwner = filterOwnerNewMenuOwner(newMenuOwner);
        return newMenuOwner;
    }

    /**
     * 过滤 用户权限,用户未配置且角色不为空则过滤不在角色的权限，用户未配置且角色为空 和用户单独配置则过滤不在组织的权限
     *
     * @Title: filterOwnerNewMenuOwner
     * @param ownerNewMenuOwner
     *                新建菜单权限
     * @return Boolean
     */
    private Boolean filterOwnerNewMenuOwner(Boolean ownerNewMenuOwner) {
        boolean filterByRole = false;
        if (!getHasAssignOwnerMenu()) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            boolean allRoleNotExistNewMenuOwner = true;
            for (CoreRole role : getRoles()) {
                if (role.getOwnerNewMenuOwner() != null && role.getOwnerNewMenuOwner()) {
                    allRoleNotExistNewMenuOwner = false;
                }
            }
            /** 用户所有角色均没有新菜单权限，则过滤为没有新菜单权限 */
            if (allRoleNotExistNewMenuOwner) {
                return false;
            }
        } else {
            if (getOrg() != null) {
                /** 组织若未配置新菜单权限，则过滤为也没有权限 */
                Boolean orgOwnerNewMenuOwner = getOrg().getOwnerNewMenuOwner();
                if (orgOwnerNewMenuOwner != null && !orgOwnerNewMenuOwner) {
                    return false;
                }
            }
        }
        return ownerNewMenuOwner;
    }

    /**
     * 新增站点的站点类数据权限(,逗号分隔操作 为null或者空串则未单独配置)
     *
     * @Title: getNewSiteOpe
     * @return String
     */
    @Transient
    public String getNewSiteOpe() {
        CmsDataPermCfg cfg = getPermCfg();
        if (cfg != null) {
            return cfg.getNewSiteOpe();
        }
        return null;
    }

    /**
     * 新增站点的站点类数据权限操作选项数组
     *
     * @Title: getNewSiteOperators
     * @return Short[]
     */
    @Transient
    public Short[] getNewSiteOperators() {
        if (StringUtils.isNoneBlank(getNewSiteOpe())) {
            String[] newSiteOperators = getNewSiteOpe().split(",");
            Short[] operators = new Short[newSiteOperators.length];
            for (int i = 0; i < newSiteOperators.length; i++) {
                operators[i] = Short.parseShort(newSiteOperators[i]);
            }
            return operators;
        }
        return new Short[]{};
    }

    /**
     * 用户拥有新增的站点操作选项
     *
     * @Title: getOwnerNewSiteOperators
     * @return Short[]
     */
    @Transient
    public Short[] getOwnerNewSiteOperators() {
        Short[] operators = new Short[]{};
        if (getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_SITE)) {
            operators = getNewSiteOperators();
        } else {
            Set<Short> ownerOperators = new HashSet<Short>();
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (role.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_SITE)) {
                        for (Short s : role.getOwnerNewSiteOperators()) {
                            ownerOperators.add(s);
                        }
                    } else {
                        for (Short s : role.getOrg().getOwnerNewSiteOperators()) {
                            ownerOperators.add(s);
                        }
                    }
                }
            } else {
                for (Short s : getOrg().getOwnerNewSiteOperators()) {
                    ownerOperators.add(s);
                }
            }
            operators = ownerOperators.toArray(operators);
        }
        operators = filterOwnerNewSiteOperators(operators);
        return operators;
    }

    /**
     * 过滤站点权限(若用户单独配置权限，则限制在组织最大权限， 若未单独配置且角色不为空则限制在角色最大权限，若未单独配置且角色空则限制在组织最大权限)
     *
     * @Title: filterOwnerSites
     * @param ownerOps
     *                拥有的站点权限
     * @return Short[]
     */
    private Short[] filterOwnerNewSiteOperators(Short[] ownerOps) {
        Short[] maxOps = new Short[]{};
        boolean filterByRole = false;
        if (!getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_SITE)) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            Set<Short> maxRoleOps = new HashSet<Short>();
            for (CoreRole role : getRoles()) {
                for (Short op : role.getOwnerNewSiteOperators()) {
                    maxRoleOps.add(op);
                }
            }
            maxOps = maxRoleOps.toArray(maxOps);
        } else {
            if (getOrg() != null) {
                maxOps = getOrg().getOwnerNewSiteOperators();
            }
        }
        /** 最大权限不包含 则移除 */
        Set<Short> filterOps = new HashSet<Short>();
        Short[] filterOpArray = new Short[]{};
        for (Short op : ownerOps) {
            if (Arrays.binarySearch(maxOps, op) != -1) {
                filterOps.add(op);
            }
        }
        filterOpArray = filterOps.toArray(filterOpArray);
        return ownerOps;
    }

    /**
     * 新增栏目的栏目类数据权限(,逗号分隔操作 为null或者空串则未单独配置)
     *
     * @Title: getNewChannelOpe
     * @return String
     */
    @Transient
    public String getNewChannelOpe(Integer siteId) {
        CmsDataPermCfg cfg = getPermCfgForChannel(siteId);
        if (cfg != null) {
            return cfg.getNewChannelOpe();
        }
        return null;
    }

    @Transient
    private String getAllNewChannelOpe() {
        List<CmsDataPermCfg> cfgs = getPermCfgs();
        if (cfgs != null) {
            StringBuffer buff = new StringBuffer();
            for (CmsDataPermCfg cfg : cfgs) {
                if (StringUtils.isNoneBlank(cfg.getNewChannelOpe())) {
                    buff.append(cfg.getNewChannelOpe()).append(",");
                }
            }
            if (buff.length() > 0) {
                return buff.toString();
            }
        }
        return null;
    }

    @Transient
    private String getAllNewChannelOpeContent() {
        List<CmsDataPermCfg> cfgs = getPermCfgs();
        if (cfgs != null) {
            StringBuffer buff = new StringBuffer();
            for (CmsDataPermCfg cfg : cfgs) {
                if (StringUtils.isNoneBlank(cfg.getNewChannelOpeContent())) {
                    buff.append(cfg.getNewChannelOpeContent()).append(",");
                }
            }
            if (buff.length() > 0) {
                return buff.toString();
            }
        }
        return null;
    }

    /**
     * 单独分配的 新增栏目的栏目类数据权限操作选项数组
     *
     * @Title: getNewChannelOperators
     * @return Short[]
     */
    @Transient
    public Short[] getNewChannelOperators(Integer siteId) {
        if (StringUtils.isNoneBlank(getNewChannelOpe(siteId))) {
            String[] newOperators = getNewChannelOpe(siteId).split(",");
            Short[] operators = new Short[newOperators.length];
            for (int i = 0; i < newOperators.length; i++) {
                operators[i] = Short.parseShort(newOperators[i]);
            }
            return operators;
        }
        return new Short[]{};
    }

    /**
     * 用户拥有新增的栏目操作选项
     *
     * @Title: getOwnerNewChannelOperators
     * @return Short[]
     */
    @Transient
    public Short[] getOwnerNewChannelOperators(Integer siteId) {
        Short[] operators = new Short[]{};
        if (getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL)) {
            operators = getNewChannelOperators(siteId);
        } else {
            Set<Short> ownerOperators = new HashSet<Short>();
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (role.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL)) {
                        for (Short s : role.getOwnerNewChannelOperators(siteId)) {
                            ownerOperators.add(s);
                        }
                    } else {
                        for (Short s : role.getOrg().getOwnerNewChannelOperators(siteId)) {
                            ownerOperators.add(s);
                        }
                    }
                }
            } else {
                for (Short s : getOrg().getOwnerNewChannelOperators(siteId)) {
                    ownerOperators.add(s);
                }
            }
            operators = ownerOperators.toArray(operators);
        }
        operators = filterOwnerNewChannelOperators(operators, siteId);
        return operators;
    }

    /**
     * 过滤栏目权限(若用户单独配置权限，则限制在组织最大权限， 若未单独配置且角色不为空则限制在角色最大权限，若未单独配置且角色空则限制在组织最大权限)
     *
     * @Title: filterOwnerNewChannelOperators
     * @param ownerOps
     *                拥有的站点权限
     * @return Short[]
     */
    private Short[] filterOwnerNewChannelOperators(Short[] ownerOps, Integer siteId) {
        Short[] maxOps = new Short[]{};
        boolean filterByRole = false;
        if (!getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL)) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            Set<Short> maxRoleOps = new HashSet<Short>();
            for (CoreRole role : getRoles()) {
                for (Short op : role.getOwnerNewChannelOperators(siteId)) {
                    maxRoleOps.add(op);
                }
            }
            maxOps = maxRoleOps.toArray(maxOps);
        } else {
            if (getOrg() != null) {
                maxOps = getOrg().getOwnerNewChannelOperators(siteId);
            }
        }
        /** 最大权限不包含 则移除 */
        Set<Short> filterOps = new HashSet<Short>();
        Short[] filterOpArray = new Short[]{};
        for (Short op : ownerOps) {
            if (Arrays.binarySearch(maxOps, op) != -1) {
                filterOps.add(op);
            }
        }
        filterOpArray = filterOps.toArray(filterOpArray);
        return ownerOps;
    }

    /**
     * 新增栏目的文档类数据权限(,逗号分隔操作 为null或者空串则未单独配置)
     *
     * @Title: getNewChannelOpeContent
     * @return String
     */
    @Transient
    public String getNewChannelOpeContent(Integer siteId) {
        CmsDataPermCfg cfg = getPermCfgForChannel(siteId);
        if (cfg != null) {
            return cfg.getNewChannelOpeContent();
        }
        return null;
    }

    /**
     * 单独配置的 新增栏目的文档类数据权限操作选项数组
     *
     * @Title: getNewChannelContentOperators
     * @return Short[]
     */
    @Transient
    public Short[] getNewChannelContentOperators(Integer siteId) {
        if (StringUtils.isNoneBlank(getNewChannelOpeContent(siteId))) {
            String[] newOperators = getNewChannelOpeContent(siteId).split(",");
            Short[] operators = new Short[newOperators.length];
            for (int i = 0; i < newOperators.length; i++) {
                operators[i] = Short.parseShort(newOperators[i]);
            }
            return operators;
        }
        return new Short[]{};
    }

    /**
     * 用户拥有新增的内容操作选项
     *
     * @Title: getOwnerNewChannelContentOperators
     * @return Short[]
     */
    @Transient
    public Short[] getOwnerNewChannelContentOperators(Integer siteId) {
        Short[] operators = new Short[]{};
        if (getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT)) {
            operators = getNewChannelContentOperators(siteId);
        } else {
            Set<Short> ownerOperators = new HashSet<Short>();
            List<CoreRole> roles = getRoles();
            if (roles != null && !roles.isEmpty()) {
                for (CoreRole role : roles) {
                    if (role.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT)) {
                        for (Short s : role.getOwnerNewChannelContentOperators(siteId)) {
                            ownerOperators.add(s);
                        }
                    } else {
                        for (Short s : role.getOrg()
                                .getOwnerNewChannelContentOperators(siteId)) {
                            ownerOperators.add(s);
                        }
                    }
                }
            } else {
                for (Short s : getOrg().getOwnerNewChannelContentOperators(siteId)) {
                    ownerOperators.add(s);
                }
            }
            operators = ownerOperators.toArray(operators);
        }
        operators = filterOwnerNewChannelContentOperators(operators, siteId);
        return operators;
    }

    /**
     * 过滤文档权限(若用户单独配置权限，则限制在组织最大权限， 若未单独配置且角色不为空则限制在角色最大权限，若未单独配置且角色空则限制在组织最大权限)
     *
     * @Title: filterOwnerNewChannelOperators
     * @param ownerOps
     *                拥有的内容权限
     * @return Short[]
     */
    private Short[] filterOwnerNewChannelContentOperators(Short[] ownerOps, Integer siteId) {
        Short[] maxOps = new Short[]{};
        boolean filterByRole = false;
        if (!getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT)) {
            if (getRoles() != null && !getRoles().isEmpty()) {
                filterByRole = true;
            }
        }
        if (filterByRole) {
            Set<Short> maxRoleOps = new HashSet<Short>();
            for (CoreRole role : getRoles()) {
                for (Short op : role.getOwnerNewChannelContentOperators(siteId)) {
                    maxRoleOps.add(op);
                }
            }
            maxOps = maxRoleOps.toArray(maxOps);
        } else {
            if (getOrg() != null) {
                maxOps = getOrg().getOwnerNewChannelContentOperators(siteId);
            }
        }
        /** 最大权限不包含 则移除 */
        Set<Short> filterOps = new HashSet<Short>();
        Short[] filterOpArray = new Short[]{};
        for (Short op : ownerOps) {
            if (Arrays.binarySearch(maxOps, op) != -1) {
                filterOps.add(op);
            }
        }
        filterOpArray = filterOps.toArray(filterOpArray);
        return ownerOps;
    }

    /**
     * 站点数据权限检查
     *
     * @Title: checkSiteDataPerm
     * @param id
     *                站点id
     * @param operator
     *                站点操作 OpeSiteEnum
     * @throws GlobalException
     *                 GlobalException
     */
    @Transient
    public void checkSiteDataPerm(Integer id, OpeSiteEnum operator) throws GlobalException {
        /** 开发模式下返回true 忽略权限验证 */
        if (ServerModeEnum.dev.toString().equals(getServerMode()) || SystemContextUtils.replay()) {
            return;
        }

//        if (!ServerModeEnum.dev.toString().equals(getServerMode()) || !SystemContextUtils.replay()) {
            CoreUser user = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest());
            if (OpeSiteEnum.DEL.equals(operator)) {
                boolean delValid = (user.getDelSiteIds() != null && !user.getDelSiteIds().contains(id))
                        || user.getDelSiteIds() == null;
                if (delValid) {
                    String msg = MessageResolver.getMessage(
                            SiteErrorCodeEnum.NO_PERMISSION_DEL_SITE_ERROR.getCode(),
                            SiteErrorCodeEnum.NO_PERMISSION_DEL_SITE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new SiteExceptionInfo(
                            SiteErrorCodeEnum.NO_PERMISSION_DEL_SITE_ERROR.getCode(), msg));
                }
            } else if (OpeSiteEnum.EDIT.equals(operator)) {
                boolean editValid = (user.getEditSiteIds() != null
                        && !user.getEditSiteIds().contains(id))
                        || user.getEditSiteIds() == null;
                if (editValid) {
                    String msg = MessageResolver.getMessage(
                            SiteErrorCodeEnum.NO_PERMISSION_MODIFY_SITE_ERROR.getCode(),
                            SiteErrorCodeEnum.NO_PERMISSION_MODIFY_SITE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new SiteExceptionInfo(
                            SiteErrorCodeEnum.NO_PERMISSION_MODIFY_SITE_ERROR.getCode(),
                            msg));
                }
            } else if (OpeSiteEnum.OPENCLOSE.equals(operator)) {
                boolean exportValid = (user.getOpenCloseSiteIds() != null
                        && !user.getOpenCloseSiteIds().contains(id))
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
            } else if (OpeSiteEnum.STATIC.equals(operator)) {
                boolean staticValid = (user.getStaticSiteIds() != null
                        && !user.getStaticSiteIds().contains(id))
                        || user.getStaticSiteIds() == null;
                if (staticValid) {
                    String msg = MessageResolver.getMessage(
                            SiteErrorCodeEnum.NO_PERMISSION_STATIC_SITE_ERROR.getCode(),
                            SiteErrorCodeEnum.NO_PERMISSION_STATIC_SITE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new SiteExceptionInfo(
                            SiteErrorCodeEnum.NO_PERMISSION_STATIC_SITE_ERROR.getCode(),
                            msg));
                }
            } else if (OpeSiteEnum.NEWCHILD.equals(operator)) {
                boolean newChild = (user.getNewChildSiteIds() != null
                        && !user.getNewChildSiteIds().contains(id))
                        || user.getNewChildSiteIds() == null;
                if (newChild) {
                    String msg = MessageResolver.getMessage(
                            SiteErrorCodeEnum.NO_PERMISSION_NEW_CHILD_SITE_ERROR.getCode(),
                            SiteErrorCodeEnum.NO_PERMISSION_NEW_CHILD_SITE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new SiteExceptionInfo(
                            SiteErrorCodeEnum.NO_PERMISSION_NEW_CHILD_SITE_ERROR.getCode(),
                            msg));
                }
            } else if (OpeSiteEnum.PERM_ASSIGN.equals(operator)) {
                boolean assignValid = (user.getPermAssignSiteIds() != null
                        && !user.getPermAssignSiteIds().contains(id))
                        || user.getPermAssignSiteIds() == null;
                if (assignValid) {
                    String msg = MessageResolver.getMessage(
                            SiteErrorCodeEnum.NO_PERMISSION_PERM_ASSIGN_SITE_ERROR
                                    .getCode(),
                            SiteErrorCodeEnum.NO_PERMISSION_PERM_ASSIGN_SITE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new SiteExceptionInfo(
                            SiteErrorCodeEnum.NO_PERMISSION_PERM_ASSIGN_SITE_ERROR
                                    .getCode(),
                            msg));
                }
            } else if (OpeSiteEnum.VIEW.equals(operator)) {
                boolean viewValid = (user.getViewSiteIds() != null
                        && !user.getViewSiteIds().contains(id))
                        || user.getViewSiteIds() == null;
                if (viewValid) {
                    String msg = MessageResolver.getMessage(
                            SiteErrorCodeEnum.NO_PERMISSION_VIEW_SITE_ERROR.getCode(),
                            SiteErrorCodeEnum.NO_PERMISSION_VIEW_SITE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new SiteExceptionInfo(
                            SiteErrorCodeEnum.NO_PERMISSION_VIEW_SITE_ERROR.getCode(),
                            msg));
                }
            }
//        }
    }

    /**
     * 清除权限缓存
     *
     * @Title: clearPermCache
     * @return: void
     */
    public void clearPermCache() {
        CacheProvider cache = ApplicationContextProvider.getBean(CacheProvider.class);
        CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
        cache.clearCache(CacheConstants.USER_OWNER_MENU_CACHE, getId().toString());
        cache.clearCache(CacheConstants.USER_OWNER_SITE_CACHE, getId().toString());
        cache.clearCache(CacheConstants.USER_OWNER_SITE_DATA_CACHE, getId().toString());
        for (CmsSite site : siteService.findAll(false, true)) {
            cache.clearCache(CacheConstants.USER_OWNER_CHANNEL_CACHE,
                    getCacheId(CmsDataPerm.DATA_TYPE_CHANNEL, site.getId()));
            cache.clearCache(CacheConstants.USER_OWNER_CONTENT_CACHE,
                    getCacheId(CmsDataPerm.DATA_TYPE_CONTENT, site.getId()));
        }
    }

    /**
     * 是否可删除 true不禁用 false禁用 当前用户 返回false 其次 当前登录用户权限>=用户权限 返回true
     *
     * @Title: getDeleteAble
     * @return: boolean
     */
    @Transient
    public boolean getDeleteAble() {
        if(SystemContextUtils.replay()) {
            return true;
        }
        CoreUser user = SystemContextUtils.getCoreUser();
        if (user != null && user.getId().equals(getId())) {
            return false;
        }
        return getManagerAble();
    }

    /**
     * 是否可管理
     *
     * @Title: getManagerAble 登录用户权限是否大于等于组织权限：当前登录用户5大类目权限范围内是否都包含用户对应的权限
     * @return boolean
     */
    @Transient
    public boolean getManagerAble() {
        /** 开发模式下和回访请求下返回true 忽略权限验证 */
        if (ServerModeEnum.dev.toString().equals(getServerMode()) || SystemContextUtils.replay()) {
            return true;
        }
        CoreUser user = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest());
        if (user == null) {
            return false;
        }
        /** 用户自己资料修改可直接返回true */
        if (getId().equals(user.getId())) {
            return true;
        }
        /** 检查站群是否全包含 */
        if (!CmsSite.fetchIds(user.getOwnerSites()).containsAll(CmsSite.fetchIds(getOwnerSites()))) {
            return false;
        }
        /** 检查菜单是否全包含 */
        if (!CoreMenu.fetchIds(user.getOwnerMenus()).containsAll(CoreMenu.fetchIds(getOwnerMenus()))) {
            return false;
        }
        /** 检查数据权限是否全包含 */
//                if (!CmsDataPerm.containsAll(user.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_SITE),
//                                getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_SITE))) {
//                        return false;
//                }
//                if (!CmsDataPerm.containsAll(
//                                user.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL),
//                                getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL))) {
//                        return false;
//                }
//                if (!CmsDataPerm.containsAll(
//                                user.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT),
//                                getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT))) {
//                        return false;
//                }
        /** 检查用户组织层级是否全包含 */
        if (!user.getChildOrgIds().contains(getOrgId())) {
            return false;
        }
        return true;
    }

    /**
     * 验证是否可管理
     *
     * @Title: validManagerAble
     * @throws GlobalException
     *                 GlobalException
     */
    @Transient
    public void validManagerAble() throws GlobalException {
        if (!getManagerAble()) {
            String msg = MessageResolver.getMessage(
                    SysOtherErrorCodeEnum.NO_USER_PERMISSION_ERROR.getCode(),
                    SysOtherErrorCodeEnum.NO_USER_PERMISSION_ERROR.getDefaultMessage());
            throw new GlobalException(new SiteExceptionInfo(
                    SysOtherErrorCodeEnum.NO_USER_PERMISSION_ERROR.getCode(), msg));
        }
    }

    /**
     * 验证是否可以分配权限（不能分配自己的权限和比自己权限大的）
     * @Title: validAssignAble
     * @throws GlobalException
     * @return: void
     */
    @Transient
    public void validAssignAble() throws GlobalException {
        if (!getDeleteAble()) {
            String msg = MessageResolver.getMessage(
                    SysOtherErrorCodeEnum.NO_USER_PERMISSION_ERROR.getCode(),
                    SysOtherErrorCodeEnum.NO_USER_PERMISSION_ERROR.getDefaultMessage());
            throw new GlobalException(new SiteExceptionInfo(
                    SysOtherErrorCodeEnum.NO_USER_PERMISSION_ERROR.getCode(), msg));
        }
    }

    /**
     * 从内容中检测出该操作是否是回放操作
     * @return boolean
     */
    @Transient
    public boolean replay() {
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            String replay = request.getHeader(WebConstants.REPLAY_OPERATING);
            if (StringUtils.isNotBlank(replay)) {
                return Boolean.parseBoolean(replay);
            }
        }
        return false;
    }

    /**
     * 检查栏目数据权限
     *
     * @Title: checkChannelDataPerm
     * @param id
     *                栏目id
     * @param operator
     *                栏目操作OpeChannelEnum
     * @throws GlobalException
     *                 GlobalException
     */
    @Transient
    public void checkChannelDataPerm(Integer id, OpeChannelEnum operator) throws GlobalException {
        /** 开发模式下返回true 忽略权限验证 */
        if (!ServerModeEnum.dev.toString().equals(getServerMode())) {
            CoreUser user = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest());
            boolean replay = false;
            if (this.replay()) {
                replay = user.getSafetyAdmin() != null ? user.getSafetyAdmin() : false;
            }
            if (!replay) {
                if (OpeChannelEnum.DEL.equals(operator)) {
                    boolean delValid = (user.getDelChannelIds() != null
                            && !user.getDelChannelIds().contains(id))
                            || user.getDelChannelIds() == null;
                    if (delValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_DEL_CHANNEL_ERROR.getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_DEL_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_DEL_CHANNEL_ERROR
                                        .getCode()));
                    }
                } else if (OpeChannelEnum.EDIT.equals(operator)) {
                    boolean editValid = (user.getEditChannelIds() != null
                            && !user.getEditChannelIds().contains(id))
                            || user.getEditChannelIds() == null;
                    if (editValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_MODIFY_CHANNEL_ERROR
                                        .getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_MODIFY_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_MODIFY_CHANNEL_ERROR
                                        .getCode()));
                    }
                } else if (OpeChannelEnum.CREATE.equals(operator)) {
                    boolean createValid = (user.getCreateChannelIds() != null
                            && !user.getCreateChannelIds().contains(id))
                            || user.getCreateChannelIds() == null;
                    if (createValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_CREATE_CHANNEL_ERROR
                                        .getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_CREATE_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_CREATE_CHANNEL_ERROR
                                        .getCode()));
                    }
                } else if (OpeChannelEnum.MERGE.equals(operator)) {
                    boolean mergeValid = (user.getMergeChannelIds() != null
                            && !user.getMergeChannelIds().contains(id))
                            || user.getMergeChannelIds() == null;
                    if (mergeValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_MERGE_CHANNEL_ERROR
                                        .getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_MERGE_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_MERGE_CHANNEL_ERROR
                                        .getCode()));
                    }
                } else if (OpeChannelEnum.STATIC.equals(operator)) {
                    boolean moveValid = (user.getStaticChannelIds() != null
                            && !user.getStaticChannelIds().contains(id))
                            || user.getStaticChannelIds() == null;
                    if (moveValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_STATIC_CHANNEL_ERROR
                                        .getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_STATIC_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_STATIC_CHANNEL_ERROR
                                        .getCode()));
                    }
                } else if (OpeChannelEnum.PERM_ASSIGN.equals(operator)) {
                    boolean permAssignValid = (user.getPermAssignChannelIds() != null
                            && !user.getPermAssignChannelIds().contains(id))
                            || user.getPermAssignChannelIds() == null;
                    if (permAssignValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_PERM_ASSIGN_CHANNEL_ERROR
                                        .getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_PERM_ASSIGN_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_PERM_ASSIGN_CHANNEL_ERROR
                                        .getCode()));
                    }
                } else if (OpeChannelEnum.VIEW.equals(operator)) {
                    boolean publishValid = (user.getViewChannelIds() != null
                            && !user.getViewChannelIds().contains(id))
                            || user.getViewChannelIds() == null;
                    if (publishValid) {
                        String msg = MessageResolver.getMessage(
                                ChannelErrorCodeEnum.NO_PERMISSION_VIEW_CHANNEL_ERROR.getCode(),
                                ChannelErrorCodeEnum.NO_PERMISSION_VIEW_CHANNEL_ERROR
                                        .getDefaultMessage());
                        throw new GlobalException(new ChannelExceptionInfo(msg,
                                ChannelErrorCodeEnum.NO_PERMISSION_VIEW_CHANNEL_ERROR
                                        .getCode()));
                    }
                }
            }
        }
    }


    /**
     * 检查内容数据权限
     *
     * @Title: checkContentDataPerm
     * @param channelId
     *                栏目channelId
     * @param operator
     *                内容操作 OpeContentEnum
     * @throws GlobalException
     *                 GlobalException
     */
    @Transient
    public void checkContentDataPerm(Integer channelId, OpeContentEnum operator) throws GlobalException {
        /** 开发模式下返回true 忽略权限验证 */
        if (!ServerModeEnum.dev.toString().equals(getServerMode())) {
            CoreUser user = SystemContextUtils.getUser(RequestUtils.getHttpServletRequest());
            if (OpeContentEnum.DEL.equals(operator)) {
                boolean delValid = (user.getDelContentChannelIds() != null
                        && !user.getDelContentChannelIds().contains(channelId))
                        || user.getDelContentChannelIds() == null;
                if (delValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_DEL_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_DEL_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_DEL_ERROR.getCode()));
                }
            } else if (OpeContentEnum.EDIT.equals(operator)) {
                boolean modifyValid = (user.getEditContentChannelIds() != null
                        && !user.getEditContentChannelIds().contains(channelId))
                        || user.getEditContentChannelIds() == null;
                if (modifyValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_MODIFY_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_MODIFY_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(
                            msg,
                            ContentErrorCodeEnum.NO_PERMISSION_MODIFY_ERROR.getCode()
                    ));
                }
            } else if (OpeContentEnum.CREATE.equals(operator)) {
                boolean createValid = (user.getCreateContentChannelIds() != null
                        && !user.getCreateContentChannelIds().contains(channelId))
                        || user.getCreateContentChannelIds() == null;
                if (createValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_CREATE_CONTENT_ERROR
                                    .getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_CREATE_CONTENT_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(
                            msg, ContentErrorCodeEnum.NO_PERMISSION_CREATE_CONTENT_ERROR
                            .getCode()));
                }
            } else if (OpeContentEnum.VIEW.equals(operator)) {
                boolean annotationValid = (user.getViewContentChannelIds() != null
                        && !user.getViewContentChannelIds().contains(channelId))
                        || user.getViewContentChannelIds() == null;
                if (annotationValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_VIEW_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_VIEW_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_VIEW_ERROR.getCode()));
                }
            } else if (OpeContentEnum.PUBLISH.equals(operator)) {
                boolean publishValid = (user.getPublishContentChannelIds() != null
                        && !user.getPublishContentChannelIds().contains(channelId))
                        || user.getPublishContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_PUBLISH_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_PUBLISH_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(
                            msg, ContentErrorCodeEnum.NO_PERMISSION_PUBLISH_ERROR.getCode()
                    ));
                }
            } else if (OpeContentEnum.TOP.equals(operator)) {
                boolean publishValid = (user.getTopContentChannelIds() != null
                        && !user.getTopContentChannelIds().contains(channelId))
                        || user.getTopContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_TOP_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_TOP_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_TOP_ERROR.getCode()));
                }
            } else if (OpeContentEnum.MOVE.equals(operator)) {
                boolean publishValid = (user.getMoveContentChannelIds() != null
                        && !user.getTopContentChannelIds().contains(channelId))
                        || user.getTopContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_MOVE_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_MOVE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_MOVE_ERROR.getCode()));
                }
            } else if (OpeContentEnum.ORDER.equals(operator)) {
                boolean publishValid = (user.getSortContentChannelIds() != null
                        && !user.getSortContentChannelIds().contains(channelId))
                        || user.getSortContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_SORT_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_SORT_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_SORT_ERROR.getCode()));
                }
            } else if (OpeContentEnum.COPY.equals(operator)) {
                boolean publishValid = (user.getCopyContentChannelIds() != null
                        && !user.getCopyContentChannelIds().contains(channelId))
                        || user.getCopyContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_COPY_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_COPY_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_COPY_ERROR.getCode()));
                }
            } else if (OpeContentEnum.QUOTE.equals(operator)) {
                boolean publishValid = (user.getQuoteContentChannelIds() != null
                        && !user.getQuoteContentChannelIds().contains(channelId))
                        || user.getQuoteContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_QUOTE_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_QUOTE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_QUOTE_ERROR.getCode()));
                }
            } else if (OpeContentEnum.TYPE.equals(operator)) {
                boolean publishValid = (user.getTypeContentChannelIds() != null
                        && !user.getTypeContentChannelIds().contains(channelId))
                        || user.getTypeContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_TYPE_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_TYPE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_TYPE_ERROR.getCode()));
                }
            } else if (OpeContentEnum.FILE.equals(operator)) {
                boolean publishValid = (user.getFileContentChannelIds() != null
                        && !user.getFileContentChannelIds().contains(channelId))
                        || user.getFileContentChannelIds() == null;
                if (publishValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_TYPE_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_TYPE_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_TYPE_ERROR.getCode()));
                }
            } else if (OpeContentEnum.SITE_PUSH.equals(operator)) {
                boolean pushValid = (user.getSitePushContentChannelIds() != null
                        && !user.getSitePushContentChannelIds().contains(channelId))
                        || user.getSitePushContentChannelIds() == null;
                if (pushValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_SITE_PUSH_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_SITE_PUSH_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(msg,
                            ContentErrorCodeEnum.NO_PERMISSION_SITE_PUSH_ERROR.getCode()
                    ));
                }
            } else if (OpeContentEnum.WECHAT_PUSH.equals(operator)) {
                boolean wechatPushValid = (user.getWechatPushContentChannelIds() != null
                        && !user.getWechatPushContentChannelIds().contains(channelId))
                        || user.getWechatPushContentChannelIds() == null;
                if (wechatPushValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_WECHAT_PUSH_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_WECHAT_PUSH_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(
                            msg,
                            ContentErrorCodeEnum.NO_PERMISSION_WECHAT_PUSH_ERROR.getCode()
                    ));
                }
            } else if (OpeContentEnum.WEIBO_PUSH.equals(operator)) {
                boolean weiboPushValid = (user.getWeiboPushContentChannelIds() != null
                        && !user.getWeiboPushContentChannelIds().contains(channelId))
                        || user.getWeiboPushContentChannelIds() == null;
                if (weiboPushValid) {
                    String msg = MessageResolver.getMessage(
                            ContentErrorCodeEnum.NO_PERMISSION_WEIBO_PUSH_ERROR.getCode(),
                            ContentErrorCodeEnum.NO_PERMISSION_WEIBO_PUSH_ERROR
                                    .getDefaultMessage());
                    throw new GlobalException(new ContentExceptionInfo(
                            msg,
                            ContentErrorCodeEnum.NO_PERMISSION_WEIBO_PUSH_ERROR.getCode()));
                }
            }
        }
    }

    /**
     * 可管理站点ID
     *
     * @Title: getOwnerSiteIds
     * @return List
     */
    @Override
    @Transient
    public List<Integer> getOwnerSiteIds() {
        if (getAdmin() != null && getAdmin()) {
            return CmsSite.fetchIds(getOwnerSites());
        }
        return new ArrayList<>();
    }

    /**
     * 获取用户的角色ID
     *
     * @Title: getRoleIds
     * @return List
     */
    @Transient
    public List<Integer> getRoleIds() {
        return CoreRole.fetchIds(getRoles());
    }

    /**
     * 可修改站点ID
     *
     * @Title: getEditSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getEditSiteIds() {
        return CmsSite.fetchIds(getEditSites());
    }

    /**
     * 可修改站点数据
     *
     * @Title: getEditSites
     * @return List
     */
    @Transient
    public List<CmsSite> getEditSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_EDIT);
    }

    /**
     * 可静态化站点ID
     *
     * @Title: getStaticSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getStaticSiteIds() {
        return CmsSite.fetchIds(getStaticSites());
    }

    /**
     * 可静态化站点数据
     *
     * @Title: getStaticSites
     * @return List
     */
    @Transient
    public List<CmsSite> getStaticSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_STATIC);
    }

    /**
     * 可删除站点数据
     *
     * @Title: getDelSites
     * @return List
     */
    @Transient
    public List<CmsSite> getDelSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_DEL);
    }

    /**
     * 可删除站点id
     *
     * @Title: getDelSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getDelSiteIds() {
        return CmsSite.fetchIds(getDelSites());
    }

    /**
     * 可新建子站点数据
     *
     * @Title: getNewChildSites
     * @return List
     */
    @Transient
    public List<CmsSite> getNewChildSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_NEW_CHILD);
    }

    /**
     * 可新建子站点id
     *
     * @Title: getNewChildSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getNewChildSiteIds() {
        return CmsSite.fetchIds(getNewChildSites());
    }

    /**
     * 可开启关闭站点数据
     *
     * @Title: getOpenCloseSites
     * @return List
     */
    @Transient
    public List<CmsSite> getOpenCloseSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_OPEN_CLOSE);
    }

    /**
     * 可导出站点id
     *
     * @Title: getOpenCloseSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getOpenCloseSiteIds() {
        return CmsSite.fetchIds(getOpenCloseSites());
    }

    /**
     * 可权限分配站点数据
     *
     * @Title: getPermAssignSites
     * @return List
     */
    @Transient
    public List<CmsSite> getPermAssignSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_PERM_ASSIGN);
    }

    /**
     * 可权限分配站点id
     *
     * @Title: getPermAssignSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getPermAssignSiteIds() {
        return CmsSite.fetchIds(getPermAssignSites());
    }

    /**
     * 可查看站点数据
     *
     * @Title: getViewSites
     * @return List
     */
    @Transient
    public List<CmsSite> getViewSites() {
        return getSitesByOperator(CmsDataPerm.OPE_SITE_VIEW);
    }

    /**
     * 可发布站点id
     *
     * @Title: getViewSiteIds
     * @return List
     */
    @Transient
    public List<Integer> getViewSiteIds() {
        return CmsSite.fetchIds(getViewSites());
    }

    private List<CmsSite> getSitesByOperator(Short operator) {
        /**来自顶层组织权限，则直接返回站点 */
        if (getOwnerDataPermsByTypeFromSuper(CmsDataPerm.DATA_TYPE_SITE)) {
            return ApplicationContextProvider.getBean(CmsSiteService.class).findAll(false, true);
        }
        Set<CmsDataPerm> allDataPerms = getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_SITE,
                null, operator, null);
        Set<CmsSite> sites = new HashSet<>();
        CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
        for (CmsDataPerm p : allDataPerms) {
            if (p.getSite() != null && !p.getSite().getHasDeleted()) {
                sites.add(siteService.findById(p.getSiteId()));
            }
        }
        List<CmsSite> siteList = new ArrayList<>(sites.size());
        siteList.addAll(sites);
        return siteList;
    }

    /**
     * 可查看栏目数据
     *
     * @Title: getViewChannels
     * @return List
     */
    @Transient
    public List<Channel> getViewChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        List<Channel> channels = getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_VIEW);
        if (channels.size() > 0) {
            channels = channels.stream().filter(c -> (c != null && c.getRecycle() != null && !c.getRecycle())).collect(Collectors.toList());
        }
        ChannelService channelService = ApplicationContextProvider.getBean(ChannelService.class);
        channels = channelService.findByIds(Channel.fetchIds(channels));
        return channels;
    }

    /**
     * 可查看非回收站的栏目数据(栏目数据权限的栏目)
     *
     * @Title: getViewNoCycleChannels
     * @return List
     */
    @Transient
    public List<Channel> getViewNoCycleChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        List<Channel> channels = getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_VIEW);
        if (channels.size() > 0) {
            channels = channels.stream().filter(c -> (c != null && null != c.getRecycle() && !c.getRecycle())).collect(Collectors.toList());
        }
        ChannelService channelService = ApplicationContextProvider.getBean(ChannelService.class);
        channels = channelService.findByIds(Channel.fetchIds(channels));
        return channels;
    }

    /**
     * 可查看非回收站的栏目数据 (内容数据权限的栏目)
     *
     * @Title: getViewNoCycleChannelsForContent
     * @return: List<Channel>
     */
    @Transient
    public List<Channel> getViewNoCycleChannelsForContent() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        List<Channel> channels = getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_VIEW);
        List<Channel> channelList = new ArrayList<>();
        if (channels.size() > 0) {
            return channels.stream().filter(c -> (c != null && c.getRecycle() != null && !c.getRecycle())).collect(Collectors.toList());
        }
        return channelList;
    }

    /**
     * 可查看栏目id
     *
     * @Title: getViewChannelIds
     * @return List
     */
    @Transient
    public List<Integer> getViewChannelIds() {
        return Channel.fetchIds(getViewChannels());
    }

    /**
     * 可创建子栏目的栏目数据
     *
     * @Title: getCreateChannels
     * @return List
     */
    @Transient
    public List<Channel> getCreateChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_CREATE);
    }

    /**
     * 可创建子栏目的栏目id
     *
     * @Title: getCreateChannelIds
     * @return List
     */
    @Transient
    public List<Integer> getCreateChannelIds() {
        return Channel.fetchIds(getCreateChannels());
    }

    /**
     * 可删除的栏目数据
     *
     * @Title: getDelChannels
     * @return List
     */
    @Transient
    public List<Channel> getDelChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_DEL);
    }

    @Transient
    public List<Integer> getDelChannelIds() {
        return Channel.fetchIds(getDelChannels());
    }

    /**
     * 可修改的栏目数据
     *
     * @Title: getEditChannels
     * @return List
     */
    @Transient
    public List<Channel> getEditChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_EDIT);
    }

    @Transient
    public List<Integer> getEditChannelIds() {
        return Channel.fetchIds(getEditChannels());
    }

    /**
     * 可合并的栏目数据
     *
     * @Title: getMergeChannels
     * @return List
     */
    @Transient
    public List<Channel> getMergeChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_MERGE);
    }

    @Transient
    public List<Integer> getMergeChannelIds() {
        return Channel.fetchIds(getMergeChannels());
    }

    /**
     * 可静态化的栏目数据
     *
     * @Title: getStaticChannels
     * @return List
     */
    @Transient
    public List<Channel> getStaticChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_STATIC);
    }

    @Transient
    public List<Integer> getStaticChannelIds() {
        return Channel.fetchIds(getStaticChannels());
    }

    /**
     * 可权限分配的栏目数据
     *
     * @Title: getPermAssignChannels
     * @return List
     */
    @Transient
    public List<Channel> getPermAssignChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getChannelsByOperator(siteId, CmsDataPerm.OPE_CHANNEL_PERM_ASSIGN);
    }

    @Transient
    public List<Integer> getPermAssignChannelIds() {
        return Channel.fetchIds(getPermAssignChannels());
    }

    /**
     * 可查看栏目数据
     *
     * @Title: getViewContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getViewContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_VIEW);
    }

    /**
     * 可查看栏目id
     *
     * @Title: getViewContentChannelIds
     * @return List
     */
    @Transient
    public List<Integer> getViewContentChannelIds() {
        return Channel.fetchIds(getViewContentChannels());
    }

    /**
     * 是否可查看站点下所有栏目
     * @return
     */
    @Transient
    public Boolean getViewCurrSiteAllChannel() {
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            CmsSite site = SystemContextUtils.getSite(request);
            return  getViewContentChannels().size()==site.getNoReCycleChannels().size();
        }
        return false;
    }

    /**
     * 可发布内容的栏目数据
     *
     * @Title: getPublishContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getPublishContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_PUBLISH);
    }

    @Transient
    public List<Integer> getPublishContentChannelIds() {
        return Channel.fetchIds(getPublishContentChannels());
    }

    /**
     * 可置顶内容的栏目数据
     *
     * @Title: getTopContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getTopContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_TOP);
    }

    @Transient
    public List<Integer> getTopContentChannelIds() {
        return Channel.fetchIds(getTopContentChannels());
    }

    /**
     * 可移动内容的栏目数据
     *
     * @Title: getMoveContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getMoveContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_MOVE);
    }

    @Transient
    public List<Integer> getMoveContentChannelIds() {
        return Channel.fetchIds(getMoveContentChannels());
    }

    /**
     * 可排序内容的栏目数据
     *
     * @Title: getSortContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getSortContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_ORDER);
    }

    @Transient
    public List<Integer> getSortContentChannelIds() {
        return Channel.fetchIds(getSortContentChannels());
    }

    /**
     * 可复制内容的栏目数据
     *
     * @Title: getCopyContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getCopyContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_COPY);
    }

    @Transient
    public List<Integer> getCopyContentChannelIds() {
        return Channel.fetchIds(getCopyContentChannels());
    }

    /**
     * 可引用内容的栏目数据
     *
     * @Title: getQuoteContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getQuoteContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_QUOTE);
    }

    @Transient
    public List<Integer> getQuoteContentChannelIds() {
        return Channel.fetchIds(getQuoteContentChannels());
    }

    /**
     * 可操作内容类型内容的栏目数据
     *
     * @Title: getTypeContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getTypeContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_TYPE);
    }

    @Transient
    public List<Integer> getTypeContentChannelIds() {
        return Channel.fetchIds(getTypeContentChannels());
    }

    /**
     * 可操作归档内容的栏目数据
     *
     * @Title: getFileContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getFileContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_FILE);
    }

    @Transient
    public List<Integer> getFileContentChannelIds() {
        return Channel.fetchIds(getFileContentChannels());
    }

    /**
     * 可新建的内容 栏目数据
     *
     * @Title: getCreateContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getCreateContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_CREATE);
    }

    @Transient
    public List<Integer> getCreateContentChannelIds() {
        return Channel.fetchIds(getCreateContentChannels());
    }

    /**
     * 可删除的内容 栏目数据
     *
     * @Title: getDelContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getDelContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_DEL);
    }

    @Transient
    public List<Integer> getDelContentChannelIds() {
        return Channel.fetchIds(getDelContentChannels());
    }

    /**
     * 可修改的的内容 栏目数据
     *
     * @Title: getEditContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getEditContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_EDIT);
    }

    @Transient
    public List<Integer> getEditContentChannelIds() {
        return Channel.fetchIds(getEditContentChannels());
    }

    /**
     * 可站群推送的内容 栏目数据
     *
     * @Title: getSitePushContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getSitePushContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_SITE_PUSH);
    }

    @Transient
    public List<Integer> getSitePushContentChannelIds() {
        return Channel.fetchIds(getSitePushContentChannels());
    }

    /**
     * 可微信群推送的内容 栏目数据
     *
     * @Title: getWechatPushContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getWechatPushContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_WECHAT_PUSH);
    }

    @Transient
    public List<Integer> getWechatPushContentChannelIds() {
        return Channel.fetchIds(getWechatPushContentChannels());
    }

    /**
     * 可微博群推送的内容 栏目数据
     *
     * @Title: getWeiboPushContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getWeiboPushContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_WEIBO_PUSH);
    }

    @Transient
    public List<Integer> getWeiboPushContentChannelIds() {
        return Channel.fetchIds(getWeiboPushContentChannels());
    }

    /**
     * 可浏览记录内容 栏目数据
     *
     * @Title: getBrowsingContentChannels
     * @return List
     */
    @Transient
    public List<Channel> getBrowsingContentChannels() {
        Integer siteId = null;
        HttpServletRequest request = RequestUtils.getHttpServletRequest();
        if (request != null) {
            siteId = SystemContextUtils.getSiteId(request);
        }
        return getContentChannelsByOperator(siteId, CmsDataPerm.OPE_CONTENT_BROWSING_HISTORY);
    }

    @Transient
    public List<Integer> getBrowsingContentChannelIds() {
        return Channel.fetchIds(getBrowsingContentChannels());
    }

    @Transient
    public List<Channel> getChannelsByOperator(Integer siteId, Short operator) {
        /**来自顶层组织权限，则直接返回栏目树*/
//                if(getOwnerDataPermsByTypeFromSuper(CmsDataPerm.DATA_TYPE_CHANNEL)){
//                        CmsSite site = ApplicationContextProvider.getBean(CmsSiteService.class).findById(siteId);
//                        return site.getChannels();
//                }
        Set<CmsDataPerm> channelDataPerms = getOwnerDataPermsByType(
                CmsDataPerm.DATA_TYPE_CHANNEL, siteId, operator, null);
        Set<Channel> channels = new HashSet<Channel>();
        for (CmsDataPerm p : channelDataPerms) {
            if (p.getDataChannel() != null && !p.getDataChannel().getHasDeleted()) {
                channels.add(p.getDataChannel());
            }
        }
        // 存在集合里面为null的数据，需要过滤
        if (!channels.isEmpty()) {
            channels = channels.stream().filter(x -> x != null).collect(Collectors.toSet());
        }
        List<Channel> channelList = new ArrayList<>(channels);
        return channelList;
    }

    @Transient
    public List<Channel> getContentChannelsByOperator(Integer siteId, Short operator) {
        /**来自顶层组织权限，则直接返回栏目树*/
//                if(getOwnerDataPermsByTypeFromSuper(CmsDataPerm.DATA_TYPE_CONTENT)){
//                        CmsSite site = ApplicationContextProvider.getBean(CmsSiteService.class).findById(siteId);
//                        return site.getChannels();
//                }
        Set<CmsDataPerm> channelDataPerms = getOwnerDataPermsByType(
                CmsDataPerm.DATA_TYPE_CONTENT, siteId, operator, null);
        Set<Integer> channelIds = new HashSet<>();
        for (CmsDataPerm p : channelDataPerms) {
            if (p.getDataChannel() != null && !p.getDataChannel().getHasDeleted()) {
                channelIds.add(p.getDataId());
            }
        }
        // 存在集合里面为null的数据，需要过滤
        if (!channelIds.isEmpty()) {
            channelIds = channelIds.stream().filter(x -> x != null).collect(Collectors.toSet());
        }
        ChannelService channelService = ApplicationContextProvider.getBean(ChannelService.class);
        List<Channel> channelList = channelService.findByIds(channelIds);
        return channelList;
    }

    /**
     * 获取内容 可操作的选项
     *
     * @Title: getContentOperatorByChannelId
     * @param channelId
     *                栏目Id
     * @return: List 支持的操作
     */
    @Transient
    public List<Short> getContentOperatorByChannelId(Integer channelId) {
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        /**目标栏目id可能不是当前所属站点，以目标栏目站点的权限为准*/
        if (channelId != null) {
            Channel c = ApplicationContextProvider.getBean(ChannelService.class).findById(channelId);
            if (c != null) {
                siteId = c.getSiteId();
            }
        }
        Set<CmsDataPerm> channelDataPerms = getOwnerDataPermsByType(
                CmsDataPerm.DATA_TYPE_CONTENT, siteId, null, channelId);
        Set<Short> opes = new HashSet<Short>();
        for (CmsDataPerm p : channelDataPerms) {
            opes.add(p.getOperation());
        }
        return new ArrayList<Short>(opes);
    }

    /**
     * 获取栏目 可操作的选项
     *
     * @Title: getChannelOperatorByChannelId
     * @param channelId
     *                栏目Id
     * @return: List 支持的操作
     */
    @Transient
    public List<Short> getChannelOperatorByChannelId(Integer channelId) {
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        /**目标栏目id可能不是当前所属站点，以目标栏目站点的权限为准*/
        if (channelId != null) {
            Channel c = ApplicationContextProvider.getBean(ChannelService.class).findById(channelId);
            if (c != null) {
                siteId = c.getSiteId();
            }
        }
        Set<CmsDataPerm> channelDataPerms = getOwnerDataPermsByType(
                CmsDataPerm.DATA_TYPE_CONTENT, siteId, null, channelId);
        Set<Short> opes = new HashSet<>();
        for (CmsDataPerm p : channelDataPerms) {
            opes.add(p.getOperation());
        }
        return new ArrayList<>(opes);
    }

    /**
     * 获取用户组织以及下级组织(组织权限小于当前登录用户的权限的)
     *
     * @Title: getAssignOrgs
     * @return List
     */
    @Transient
    public List<CmsOrg> getAssignOrgs() {
        List<CmsOrg> orgs = getChildOrgs();
        return orgs.stream().filter(org -> org.getManagerAble()).collect(Collectors.toList());
    }

    /**
     * 获取用户组织以及下级组织(组织权限小于当前登录用户的权限的)，可包含顶层组织以及用户所属组织
     *
     * @Title: getAssignOrgsContainTop
     * @param permIgnore
     *                是否忽略比较权限大小 （列表查询不比较，新建和修改比较）
     * @return List
     */
    @Transient
    public List<CmsOrg> getAssignOrgsContainTop(boolean permIgnore) {
        List<CmsOrg> orgs = getChildOrgs();
        if (!permIgnore) {
            orgs = orgs.stream().filter(org -> org.getManagerAbleContainTop()).collect(Collectors.toList());
        }
        return orgs;
    }

    @Transient
    private List<CmsOrg> getChildOrgs() {
        return getOrg().getChildNodeList();
    }

    /**
     * 获取用户所属组织 以及下级组织ID
     *
     * @Title: getChildOrgIds
     * @return List
     */
    @Transient
    public List<Integer> getChildOrgIds() {
        List<Integer> orgIds = new ArrayList<Integer>();
        List<CmsOrg> orgs = getChildOrgs();
        for (CmsOrg org : orgs) {
            orgIds.add(org.getId());
        }
        return orgIds;
    }

    /**
     * 获取用户组织以及下级组织Id(组织权限小于当前登录用户的权限的)
     *
     * @Title: getAssignOrgIds
     * @return List
     */
    @Transient
    public List<Integer> getAssignOrgIds() {
        List<Integer> orgIds = new ArrayList<Integer>();
        List<CmsOrg> orgs = getAssignOrgs();
        for (CmsOrg org : orgs) {
            orgIds.add(org.getId());
        }
        return orgIds;
    }

    /**
     * 获取用户组织以及下级组织所属角色(角色权限小于当前登录用户的权限的)
     *
     * @Title: getAssignRoles
     * @return List
     */
    @Transient
    public List<CoreRole> getAssignRoles() {
        List<CoreRole> roles = new ArrayList<CoreRole>();
        for (CmsOrg org : getAssignOrgs()) {
            List<CoreRole> orgRoles = org.getRoles();
            for (CoreRole role : orgRoles) {
                if (role.getManagerAble()) {
                    roles.add(role);
                }
            }
        }
        return roles;
    }

    /**
     * 获取用户组织以及下级组织所属角色(忽视权限大小)
     *
     * @Title: getChildRoles
     * @return List
     */
    @Transient
    public List<CoreRole> getChildRoles() {
        List<CoreRole> roles = new ArrayList<CoreRole>();
        for (CmsOrg org : getChildOrgs()) {
            roles.addAll(org.getRoles());
        }
        return roles;
    }

    /**
     * 获取用户组织以及下级组织所属角色Id(角色权限小于当前登录用户的权限的)
     *
     * @Title: getAssignRoleIds
     * @return List
     */
    @Transient
    public List<Integer> getAssignRoleIds() {
        List<Integer> orgIds = new ArrayList<Integer>();
        List<CoreRole> roles = getAssignRoles();
        for (CoreRole role : roles) {
            orgIds.add(role.getId());
        }
        return orgIds;
    }

    /**
     * 过滤回收站和已删除的站点
     *
     * @Title: getEffectSites
     * @return: Set
     */
    @Transient
    @JSONField(serialize = false)
    public Set<CmsSite> getEffectSites() {
        return getSites().stream().filter(site -> !site.getIsDelete()).filter(site -> !site.getHasDeleted())
                .collect(Collectors.toSet());
    }

    /**
     * 获取是否需要处理登录错误超过限制
     *
     * @Title: getNeedProcessLoginError
     * @param config
     *                全局配置对象
     * @return: boolean true 需要处理
     */
    @Transient
    @JSONField(serialize = false)
    public boolean getNeedProcessLoginError(GlobalConfig config) {
        /**未开启不用处理*/
        if (!config.getConfigAttr().getSecurityOpen()) {
            return false;
        }
        /** 全局配置不需要处理 */
        if (config.getLoginErrorCount() == 0) {
            return false;
        }
        /** 用户登录次数暂时未超过限制次数 */
        if (getLoginErrorCount() >= (config.getLoginErrorCount() - 1)) {
            return true;
        }
        return false;
    }

    /**
     * 获取真实姓名
     *
     * @Title: getRealname
     * @return
     */
    @Transient
    public String getRealname() {
        if (getUserExt() != null) {
            return getUserExt().getRealname();
        }
        return null;
    }

    /**
     * 获取用户任务组名(含角色和组织名)
     *
     * @Title: getCandidateGroupNames
     * @return: List
     */
    @Transient
    @JSONField(serialize = false)
    public List<String> getCandidateGroupNames() {
        List<String> groups = new ArrayList<String>();
        groups.add(getOrg().getCandidateGroupName());
        for (CoreRole role : getRoles()) {
            groups.add(role.getCandidateGroupName());
        }
        return groups;
    }

    /** 得到IDs **/
    @Transient
    @JSONField(serialize = false)
    public static List<Integer> fetchIds(Collection<CoreUser> users) {
        if (users == null) {
            return null;
        }
        List<Integer> ids = new ArrayList<Integer>();
        for (CoreUser r : users) {
            ids.add(r.getId());
        }
        return ids;
    }

    /**
     * 是否全部都是隐藏权限的菜单
     *
     * @Title: getMenuIsOnlyHide
     * @return: 没有菜单权限则返回true 以及全部都是隐藏权限的菜单也返回true
     */
    @Transient
    public boolean getMenuIsOnlyHide() {
        if(getMenus()==null){
            return true;
        }
        if(getMenus().isEmpty()){
            return true;
        }
        return getMenus().stream().filter(menu -> menu.getIsAuth()).collect(Collectors.toList()).isEmpty();
    }

    /**
     * 获取可分配权限的菜单
     *
     * @Title: getAuthMenus
     * @return: List
     */
    @Transient
    public List<CoreMenu> getAuthMenus() {
        if (getMenus() != null) {
            return getMenus().stream().filter(menu -> menu.getIsAuth()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 前端 工作流模块读取名称
     *
     * @Title: getLabel
     * @return: String
     */
    @Transient
    public String getLabel() {
        return getUsername();
    }

    /**
     * 前端 工作流模块读取id值
     *
     * @Title: getValue
     * @return: Integer
     */
    @Transient
    public Integer getValue() {
        return getId();
    }

    @Override
    @Transient
    public boolean getChecked() {
        if (AUDIT_USER_STATUS_PASS.equals(getCheckStatus())) {
            return true;
        }
        return false;
    }

    public CoreUser() {
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "jc_tr_user_role", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    public List<CoreRole> getRoles() {
        return roles;
    }

    public void setRoles(List<CoreRole> roles) {
        this.roles = roles;
    }

    @Where(clause = " deleted_flag=false ")
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "user")
    public Set<CmsDataPerm> getDataPerms() {
        return dataPerms;
    }

    public void setDataPerms(Set<CmsDataPerm> dataPerms) {
        this.dataPerms = dataPerms;
    }

    @Override
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @TableGenerator(name = "jc_sys_user", pkColumnValue = "jc_sys_user", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_sys_user")
    public Integer getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Length(max = 60)
    @Email
    @Column(name = "user_email", length = 60)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "last_login_ip", nullable = false, length = 50)
    public String getLastLoginIp() {
        return this.lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login_time", nullable = false)
    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Column(name = "login_count", nullable = false)
    public Integer getLoginCount() {
        return this.loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    @Override
    @Column(name = "login_error_count", nullable = false)
    public Integer getLoginErrorCount() {
        return loginErrorCount;
    }

    public void setLoginErrorCount(Integer loginErrorCount) {
        this.loginErrorCount = loginErrorCount;
    }

    @Column(name = "first_login_error_time")
    public Date getFirstLoginErrorTime() {
        return firstLoginErrorTime;
    }

    public void setFirstLoginErrorTime(Date firstLoginErrorTime) {
        this.firstLoginErrorTime = firstLoginErrorTime;
    }

    @Override
    @Column(name = "password", nullable = false, length = 150)
    @Length(min = 0, max = 150)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    @Column(name = "obfuscation_code", length = 32)
    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    @Column(name = "username", nullable = false, length = 150)
    @NotNull
    @Length(min = 0, max = 150)
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    @Column(name = "is_enabled", nullable = false)
    @NotNull
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @Column(name = "is_admin", nullable = false)
    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    @Column(name = "last_password_change", nullable = false)
    public Date getLastPasswordChange() {
        return lastPasswordChange;
    }

    @Column(name = "login_limit_end", nullable = false)
    public Date getLoginLimitEnd() {
        return loginLimitEnd;
    }

    @Column(name = "is_reset_password", nullable = true)
    public Boolean getIsResetPassword() {
        return isResetPassword;
    }

    public void setIsResetPassword(Boolean isResetPassword) {
        this.isResetPassword = isResetPassword;
    }

    public void setLoginLimitEnd(Date loginLimitEnd) {
        this.loginLimitEnd = loginLimitEnd;
    }

    public void setLastPasswordChange(Date lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    public void setAuthorityString(String authorityString) {
        this.authorityString = authorityString;
    }

    /*@Column(name = "balance")
    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Column(name = "total_amount")
    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Column(name = "withdrawal_amount")
    public Long getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Long withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    @Column(name = "reward_amount")
    public Long getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Long rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    @Column(name = "paid_amount")
    public Long getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Long paidAmount) {
        this.paidAmount = paidAmount;
    }

    @Column(name = "waiting_settlement_amount")
    public Long getWaitingSettlementAmount(){
        return waitingSettlementAmount;
    }

    public void setWaitingSettlementAmount(Long waitingSettlementAmount){
        this.waitingSettlementAmount=waitingSettlementAmount;
    }*/


    /**
     * 获取角色名称
     *
     * @Title: getRoleNames
     * @return String
     */
    @Transient
    public String getRoleNames() {
        List<CoreRole> roles = this.getRoles();
        StringBuilder sb = new StringBuilder();
        if (roles != null && !roles.isEmpty()) {
            for (CoreRole role : roles) {
                sb = sb.append(role.getRoleName() + ",");
            }
            this.roleNames = sb.toString().substring(0, sb.length() - 1);
        }
        return roleNames;
    }

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE})
    public CoreUserExt getUserExt() {
        return userExt;
    }

    public void setUserExt(CoreUserExt userExt) {
        this.userExt = userExt;
    }

    @Column(name = "group_id", nullable = true, length = 11)
    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Column(name = "level_id", nullable = true, length = 11)
    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    @Override
    @Column(name = "use_phone", nullable = true)
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Column(name = "check_status", nullable = false, length = 6)
    public Short getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Short checkStatus) {
        this.checkStatus = checkStatus;
    }

    /**
     * 添加扩展用户信息
     *
     * @Title: addExt
     * @param ext
     *                CoreUserExt
     * @return CoreUser
     */
    public CoreUser addExt(CoreUserExt ext) {
        ext.setUser(this);
        this.setUserExt(ext);
        return this;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_user_site", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "site_id"))
    public Set<CmsSite> getSites() {
        return sites;
    }

    public void setSites(Set<CmsSite> sites) {
        this.sites = sites;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_user_menu", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "menu_id"))
    public List<CoreMenu> getMenus() {
        return menus;
    }

    public void setMenus(List<CoreMenu> menus) {
        this.menus = menus;
    }

    @Column(name = "org_id", nullable = true, length = 11)
    public Integer getOrgId() {
        return orgId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    public CmsOrg getOrg() {
        return org;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public void setOrg(CmsOrg org) {
        this.org = org;
    }

    @Column(name = "integral", nullable = true, length = 6)
    public Integer getIntegral() {
        return integral;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public MemberGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(MemberGroup userGroup) {
        this.userGroup = userGroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public MemberLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(MemberLevel userLevel) {
        this.userLevel = userLevel;
    }

    @Where(clause = " deleted_flag=false ")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public List<CmsDataPermCfg> getPermCfgs() {
        return permCfgs;
    }

    public void setPermCfgs(List<CmsDataPermCfg> permCfgs) {
        this.permCfgs = permCfgs;
    }

    @Column(name = "source_site_id", nullable = true, length = 6)
    public Integer getSourceSiteId() {
        return sourceSiteId;
    }

    public void setSourceSiteId(Integer sourceSiteId) {
        this.sourceSiteId = sourceSiteId;
    }

    @Column(name = "user_secret_id", length = 11)
    public Integer getUserSecretId() {
        return userSecretId;
    }

    public void setUserSecretId(Integer userSecretId) {
        this.userSecretId = userSecretId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_secret_id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    public SysUserSecret getUserSecret() {
        return userSecret;
    }

    public void setUserSecret(SysUserSecret userSecret) {
        this.userSecret = userSecret;
    }

    /**
     * 重写 hashCode
     *
     * @Title: hashCode
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((admin == null) ? 0 : admin.hashCode());
        result = prime * result + ((checkStatus == null) ? 0 : checkStatus.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((integral == null) ? 0 : integral.hashCode());
        result = prime * result + ((lastLoginIp == null) ? 0 : lastLoginIp.hashCode());
        result = prime * result + ((lastLoginTime == null) ? 0 : lastLoginTime.hashCode());
        result = prime * result + ((lastPasswordChange == null) ? 0 : lastPasswordChange.hashCode());
        result = prime * result + ((levelId == null) ? 0 : levelId.hashCode());
        result = prime * result + ((loginCount == null) ? 0 : loginCount.hashCode());
        result = prime * result + ((loginErrorCount == null) ? 0 : loginErrorCount.hashCode());
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + Arrays.hashCode(roleid);
        result = prime * result + ((salt == null) ? 0 : salt.hashCode());
        result = prime * result + ((sourceSiteId == null) ? 0 : sourceSiteId.hashCode());
        result = prime * result + ((telephone == null) ? 0 : telephone.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /**
     * 重写 equals
     *
     * @Title: equals
     * @param obj
     *                Object
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CoreUser other = (CoreUser) obj;
        if (admin == null) {
            if (other.admin != null) {
                return false;
            }
        } else if (!admin.equals(other.admin)) {
            return false;
        }
        if (checkStatus == null) {
            if (other.checkStatus != null) {
                return false;
            }
        } else if (!checkStatus.equals(other.checkStatus)) {
            return false;
        }
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        if (enabled == null) {
            if (other.enabled != null) {
                return false;
            }
        } else if (!enabled.equals(other.enabled)) {
            return false;
        }
        if (groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!groupId.equals(other.groupId)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (integral == null) {
            if (other.integral != null) {
                return false;
            }
        } else if (!integral.equals(other.integral)) {
            return false;
        }
        if (lastLoginIp == null) {
            if (other.lastLoginIp != null) {
                return false;
            }
        } else if (!lastLoginIp.equals(other.lastLoginIp)) {
            return false;
        }
        if (lastLoginTime == null) {
            if (other.lastLoginTime != null) {
                return false;
            }
        } else if (!lastLoginTime.equals(other.lastLoginTime)) {
            return false;
        }
        if (lastPasswordChange == null) {
            if (other.lastPasswordChange != null) {
                return false;
            }
        } else if (!lastPasswordChange.equals(other.lastPasswordChange)) {
            return false;
        }
        if (levelId == null) {
            if (other.levelId != null) {
                return false;
            }
        } else if (!levelId.equals(other.levelId)) {
            return false;
        }
        if (loginCount == null) {
            if (other.loginCount != null) {
                return false;
            }
        } else if (!loginCount.equals(other.loginCount)) {
            return false;
        }
        if (loginErrorCount == null) {
            if (other.loginErrorCount != null) {
                return false;
            }
        } else if (!loginErrorCount.equals(other.loginErrorCount)) {
            return false;
        }
        if (orgId == null) {
            if (other.orgId != null) {
                return false;
            }
        } else if (!orgId.equals(other.orgId)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (!Arrays.equals(roleid, other.roleid)) {
            return false;
        }
        if (salt == null) {
            if (other.salt != null) {
                return false;
            }
        } else if (!salt.equals(other.salt)) {
            return false;
        }
        if (sourceSiteId == null) {
            if (other.sourceSiteId != null) {
                return false;
            }
        } else if (!sourceSiteId.equals(other.sourceSiteId)) {
            return false;
        }
        if (telephone == null) {
            if (other.telephone != null) {
                return false;
            }
        } else if (!telephone.equals(other.telephone)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    @Transient
    public String getOrgName() {
        if (org != null) {
            return org.getName();
        }
        return "";
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_site_id", insertable = false, updatable = false)
    public CmsSite getSourceSite() {
        return sourceSite;
    }

    public void setSourceSite(CmsSite sourceSite) {
        this.sourceSite = sourceSite;
    }

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    public List<MemberAttr> getMemberAttrs() {
        return memberAttrs;
    }

    public void setMemberAttrs(List<MemberAttr> memberAttrs) {
        this.memberAttrs = memberAttrs;
    }

    @OneToMany(mappedBy = "member")
    @Where(clause = " deleted_flag=false")
    public List<SysUserThird> getMemberThirds() {
        return memberThirds;
    }

    public void setMemberThirds(List<SysUserThird> memberThirds) {
        this.memberThirds = memberThirds;
    }

    @Column(name = "is_third")
    public Boolean getThird() {
        return third;
    }

    public void setThird(Boolean third) {
        this.third = third;
    }

    @Column(name = "pass_msg_has_send")
    public Boolean getPassMsgHasSend() {
        return passMsgHasSend;
    }

    public void setPassMsgHasSend(Boolean passMsgHasSend) {
        this.passMsgHasSend = passMsgHasSend;
    }

    @Column(name = "safety_admin", nullable = true, length = 1)
    public Boolean getSafetyAdmin () {
        if (safetyAdmin == null) {
            return false;
        }
        return safetyAdmin;
    }

    public void setSafetyAdmin (Boolean safetyAdmin) {
        this.safetyAdmin = safetyAdmin;
    }

    @Column(name = "audit_admin", nullable = true, length = 1)
    public Boolean getAuditAdmin () {
        if (auditAdmin == null) {
            return false;
        }
        return auditAdmin;
    }

    public void setAuditAdmin (Boolean auditAdmin) {
        this.auditAdmin = auditAdmin;
    }

    @Column(name = "safety_reinsurance_admin", nullable = true, length = 1)
    public Boolean getSafetyReinsuranceAdmin() {
        if (safetyReinsuranceAdmin == null) {
            return false;
        }
        return safetyReinsuranceAdmin;
    }

    public void setSafetyReinsuranceAdmin(Boolean safetyReinsuranceAdmin) {
        this.safetyReinsuranceAdmin = safetyReinsuranceAdmin;
    }

    @Column(name = "audit_reinsurance_admin", nullable = true, length = 1)
    public Boolean getAuditReinsuranceAdmin() {
        if (auditReinsuranceAdmin == null) {
            return false;
        }
        return auditReinsuranceAdmin;
    }

    public void setAuditReinsuranceAdmin(Boolean auditReinsuranceAdmin) {
        this.auditReinsuranceAdmin = auditReinsuranceAdmin;
    }

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_user_comment_like", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "comment_id"))
    public List<UserComment> getLikeComments() {
        return likeComments;
    }

    public void setLikeComments(List<UserComment> likeComments) {
        this.likeComments = likeComments;
    }

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_user_content_like", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "content_id"))
    public List<Content> getLikeContents() {
        return likeContents;
    }

    public void setLikeContents(List<Content> likeContents) {
        this.likeContents = likeContents;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_weibo_user", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "weibo_info_id"))
    public List<WeiboInfo> getWeiboInfos() {
        return weiboInfos;
    }

    public void setWeiboInfos(List<WeiboInfo> weiboInfos) {
        this.weiboInfos = weiboInfos;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "jc_tr_wechat_user", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "wehcat_info_id"))
    public List<AbstractWeChatInfo> getWechatInfos() {
        return wechatInfos;
    }

    public void setWechatInfos(List<AbstractWeChatInfo> wechatInfos) {
        this.wechatInfos = wechatInfos;
    }

    /** 应前台需要，用于展示待审核会员列表 **/
    @Transient
    public String getCheckStatusName(Short status) {
        if (status.equals(AUDIT_USER_STATUS_WAIT)) {
            return "待审核";
        } else if (status.equals(AUDIT_USER_STATUS_PASS)) {
            return "审核通过";
        } else {
            return "审核不通过";
        }
    }

    @Column(name = "app_id")
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    public UserIncome getUserIncome() {
        return userIncome;
    }

    public void setUserIncome(UserIncome userIncome) {
        this.userIncome = userIncome;
    }

    /**
     * 得到用户头像
     * @Title: getHeadImage
     * @return
     */
    @Transient
    public String getHeadImage() {
        String image = "";
        if (getMemberAttrs() != null && !getMemberAttrs().isEmpty()) {
            Optional<MemberAttr> attr = getMemberAttrs().stream()
                    .filter(x -> x.getAttrType().equals(CmsModelConstant.SINGLE_CHART_UPLOAD))
                    .findFirst();
            if (attr.isPresent()) {
                image = attr.get().getResourcesSpaceData() != null
                        ? attr.get().getResourcesSpaceData().getUrl() : "";
            }
        }
        return image;
    }

    /**是否本系统用户，true是，false不是**/
    @Transient
    public Boolean getItself() {
        if (StringUtils.isNotBlank(getAppId())) {
            return false;
        }
        return true;
    }

    @Transient
    public String getCacheId(Short dateType, Integer siteId) {
        if (CmsDataPerm.DATA_TYPE_CONTENT.equals(dateType) || CmsDataPerm.DATA_TYPE_CHANNEL.equals(dateType)) {
            return getId() + "-" + siteId;
        } else {
            return getId().toString();
        }
    }

    @Transient
    public String getUserSecretName() {
        String name = "";
        if (getUserSecret() != null) {
            name = getUserSecret().getName();
        }
        return name;
    }

	/** 待审核 **/
	public static Short AUDIT_USER_STATUS_WAIT = 0;
	/** 审核通过 **/
	public static Short AUDIT_USER_STATUS_PASS = 1;
	/** 审核不通过 **/
	public static Short AUDIT_USER_STATUS_NOPASS = 2;

}