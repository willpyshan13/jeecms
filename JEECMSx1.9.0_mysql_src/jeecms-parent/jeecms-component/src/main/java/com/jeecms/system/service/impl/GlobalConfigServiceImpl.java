/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.service.impl;

import com.jeecms.auth.domain.CoreApi;
import com.jeecms.auth.domain.CoreMenu;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreApiService;
import com.jeecms.auth.service.CoreMenuService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.base.domain.ISysConfig;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.base.service.ISysConfigService;
import com.jeecms.common.constants.TplConstants;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.DomainNotFoundExceptionInfo;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.FileUtils;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.Location;
import com.jeecms.common.web.Location.LocationResult.AdInfo;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.member.domain.MemberScoreDetails;
import com.jeecms.member.service.MemberScoreDetailsService;
import com.jeecms.protection.service.GradeProtectionService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.dao.GlobalConfigDao;
import com.jeecms.system.domain.*;
import com.jeecms.system.domain.dto.GlobalConfigDTO;
import com.jeecms.system.service.*;
import com.jeecms.util.SystemContextUtils;
import com.jeecms.wechat.domain.AbstractWeChatOpen;
import com.jeecms.wechat.service.AbstractWeChatOpenService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.jeecms.system.domain.SysThird.*;

/**
 * 系统配置
 *
 * @author: ztx
 * @date: 2018年7月26日 下午4:21:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@ConditionalOnMissingClass(value="com.jeecms.common.security.SM3CredentialsDigest")
public class GlobalConfigServiceImpl extends BaseServiceImpl<GlobalConfig, GlobalConfigDao, Integer>
                implements GlobalConfigService, ISysConfigService , ApplicationListener<ContextRefreshedEvent> {
        private static final Logger logger = LoggerFactory.getLogger(GlobalConfigServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public GlobalConfig get() throws GlobalException {
        Optional<GlobalConfig> global = super.dao.findById(1);
        if (!global.isPresent()) {
            throw new GlobalException(new DomainNotFoundExceptionInfo());
        }
        return global.get();
    }

    @Override
    public ISysConfig findDefault() throws GlobalException {
        return get();
    }

    @Override
    public void change(String attrName, String attrValue) throws GlobalException {
        GlobalConfig config = get();
        Map<String, String> attrMap = config.getAttrs();
        attrMap.put(attrName, attrValue);
        get().getAttrs().putAll(attrMap);
    }

    @Override
    public GlobalConfig getGlobalConfig() throws GlobalException {
        GlobalConfig config = dao.findByHasDeleted(false);
        if (config != null) {
            GlobalConfig config1 = new GlobalConfig();
            config1.setId(config.getId());
            Map<String, String> attrs = new HashMap<>(config.getAttrs().size());
            if (!config.getAttrs().isEmpty()) {
                for (Map.Entry<String, String> stringStringEntry : config.getAttrs().entrySet()) {
                    if (stringStringEntry.getValue() != null) {
                        String a = stringStringEntry.getValue().trim();
                        attrs.put(stringStringEntry.getKey(), a);
                    } else {
                        attrs.put(stringStringEntry.getKey(), null);
                    }
                }
                config1.setAttrs(attrs);
            }
            return config1;
        } else {
            return new GlobalConfig();
        }
    }

    @Override
    public boolean openElement(String username) throws GlobalException {
        GlobalConfig config = get();
        //开启状态下，首次登录不需要手机验证码
        if (config.getConfigAttr().getElementOpen() && StringUtils.isNotBlank(username)) {
            CoreUser user = coreUserService.findByUsername(username);
            return user != null && StringUtils.isNotBlank(user.getTelephone());
        }
        return config.getConfigAttr().getElementOpen();
    }

    @Override
    @Transactional
    public ResponseInfo saveGlobalConfig(GlobalConfig bean) throws GlobalException {
        GlobalConfig config = get();
        checkSetInfo(bean.getAttrs());
        //忽略系统地址
        bean.getAttrs().remove(GlobalConfigAttr.SYS_URL);
        if (config != null) {
            // 获取之前的数据,将传来的数据替换掉原来的数据
            Map<String, String> oldAttrs = config.getAttrs();
            Map<String, String> currAttrs = new ConcurrentHashMap(bean.getAttrs());
            //解密部署路径以及openoffice安装路径
            encryptField(currAttrs);
            boolean changeDigest = !currAttrs.get(GlobalConfigAttr.CREDENTIAL_DIGEST_SM).equals(oldAttrs.get(GlobalConfigAttr.CREDENTIAL_DIGEST_SM));
            boolean hasChangeAccountLockDay = !oldAttrs.get(GlobalConfigAttr.ACCOUNT_LOCK_AUTO_DAY).equals(currAttrs.get(GlobalConfigAttr.ACCOUNT_LOCK_AUTO_DAY));
             for (Map.Entry<String, String> entry : currAttrs.entrySet()) {
                /** 账户安全设置中 处理方式变更 登录错误次数重新计算 */
                if (GlobalConfigAttr.PROCESSING_MODE.equals(entry.getKey())) {
                    if (!oldAttrs.get(entry.getKey()).equals(currAttrs.get(entry.getKey()))) {
                        userService.resetLoginErrorCount();
                    }
                }
                oldAttrs.put(entry.getKey(), entry.getValue());
                /** 空值且之前老的就没有key则忽略 */
                if (StringUtils.isBlank(entry.getValue()) || !oldAttrs.containsKey(entry.getKey())) {
                    oldAttrs.remove(entry.getKey());
                }
            }

            currAttrs = doChangeDigest(currAttrs,changeDigest);
            bean.init();
            bean.setAttrs(oldAttrs);
            /**设置最后开启账户安全的修改时间,开启则设置，关闭置空，用于长时间未登录锁定账户（以这个时间为基准）*/
            if(GlobalConfigAttr.TRUE_STRING.equals(currAttrs.get(GlobalConfigAttr.SECURITY_OPEN))){
                if(hasChangeAccountLockDay){
                    bean.getAttrs().put(GlobalConfigAttr.LAST_OPEN_ACCOUNT_SECURITY_TIME,MyDateUtils.formatDate(Calendar.getInstance().getTime(),MyDateUtils.COM_Y_M_D_H_M_S_PATTERN));
                }
            }else{
                bean.getAttrs().put(GlobalConfigAttr.LAST_OPEN_ACCOUNT_SECURITY_TIME,"");
            }
            config.getAttrs().putAll(currAttrs);
            //数据为空则给空格
            for (Map.Entry<String, String> stringStringEntry : bean.getAttrs().entrySet()) {
                if (StringUtils.isEmpty(stringStringEntry.getValue())) {
                    String a = stringStringEntry.getValue().concat(" ");
                    bean.getAttrs().put(stringStringEntry.getKey(), a);
                }
            }
            super.updateAll(bean);
        } else {
            super.save(bean);
        }
        return new ResponseInfo();
    }

    /**解密**/
    void encryptField(Map<String, String> currAttrs) {
        String openoffice = currAttrs.getOrDefault(GlobalConfigAttr.OPEN_OFFICE_CATALOG, "");
        if (StringUtils.isNotBlank(openoffice) && gradeProtectionService != null) {
            currAttrs.put(GlobalConfigAttr.OPEN_OFFICE_CATALOG,  gradeProtectionService.decryptStr(openoffice));
        }
        String deploymentPath = currAttrs.getOrDefault(GlobalConfigAttr.DEPLOYMENT_PATH, "");
        if (StringUtils.isNotBlank(deploymentPath) && gradeProtectionService != null) {
            currAttrs.put(GlobalConfigAttr.DEPLOYMENT_PATH,  gradeProtectionService.decryptStr(deploymentPath));
        }
        String sysImgUrl = currAttrs.getOrDefault(GlobalConfigAttr.SYS_IMG_URL, "");
        if (StringUtils.isNotBlank(sysImgUrl) && gradeProtectionService != null) {
            currAttrs.put(GlobalConfigAttr.SYS_IMG_URL,  gradeProtectionService.decryptStr(sysImgUrl));
        }
        String loginSysImgUrl = currAttrs.getOrDefault(GlobalConfigAttr.LOGIN_SYS_IMG_URL, "");
        if (StringUtils.isNotBlank(loginSysImgUrl) && gradeProtectionService != null) {
            currAttrs.put(GlobalConfigAttr.LOGIN_SYS_IMG_URL,  gradeProtectionService.decryptStr(loginSysImgUrl));
        }
    }

    @Override
    public void updateConfigAttr(GlobalConfigAttr attr) throws GlobalException {
        Map<String, String> attrMap = attr.getAttr();
        GlobalConfigAttr configAttr = new GlobalConfigAttr(get().getAttrs());
        /** 强制密码修改周期发生变化调整系统设置调整时间 */
        if (!configAttr.getPassRegularCycle().equals(attr.getPassRegularCycle())) {
            Date now = Calendar.getInstance().getTime();
            attrMap.put(GlobalConfigAttr.PASS_REGULAR_CHANGE_SET_TIME,
                    MyDateUtils.formatDate(now, MyDateUtils.COM_Y_M_D_H_M_S_PATTERN));
        }
        get().getAttrs().putAll(attrMap);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public GlobalConfigDTO filterConfig(HttpServletRequest request, GlobalConfig config) throws GlobalException {
        CmsSite site = SystemContextUtils.getSite(request);
        String areaDistrict = null;
        String areaCity = null;
        String ip = RequestUtils.getRemoteAddr(request);
        Serializable sessionAddress = cacheProvider.getCache(CacheConstants.IP_LOCATION_CACHE, ip);
        Location location = null;
        AdInfo adInfo = null;
        if (sessionAddress == null) {
            String currentIp = RequestUtils.getRemoteAddr(request);
            try {
                location = addressService.getAddressByIP(currentIp);
                if (location != null && location.getResult() != null) {
                    cacheProvider.setCache(CacheConstants.IP_LOCATION_CACHE, ip, location);
                    adInfo = location.getResult().getAdInfo();
                    areaCity = adInfo.getCity();
                    areaDistrict = adInfo.getDistrict();
                }
            } catch (Exception e) {
                logger.error("定位失败: {}", e.getMessage());
            }
        } else {
            location = (Location) sessionAddress;
            if (location.getResult() != null) {
                adInfo = location.getResult().getAdInfo();
                areaCity = adInfo.getCity();
                areaDistrict = adInfo.getDistrict();
            }
        }
        return filterConfig(site, config, areaCity, areaDistrict);
    }

    @Override
    public GlobalConfigDTO filterConfig(CmsSite site, GlobalConfig config, String areaCity, String areaDistrict)
            throws GlobalException {
        GlobalConfigDTO configDto = new GlobalConfigDTO();
        configDto = setBaseInfo(configDto, site, config);
        configDto.setAreaCity(areaCity);
        configDto.setAreaDistrict(areaDistrict);
        configDto.setArea(getArea());
        /** 搜索关键词数组 */
        configDto.setSearchKeyWords(StringUtils.isNotBlank(config.getConfigAttr().getPHKeywords())
                ? config.getConfigAttr().getPHKeywords().split(WebConstants.ARRAY_SPT) : new String[0]);
        configDto.setHotWords(getSearchWords(config));
        /** 版权信息 */
        configDto.setCopyright(config.getConfigAttr().getIcpRecordNumber());
        List<SysThird> list = thirdService.getList();
        for (SysThird sysThird : list) {
            if (WECHAT.equals(sysThird.getCode())) {
                configDto.setWechatOpen(sysThird.getIsEnable());
            } else if (QQ.equals(sysThird.getCode())) {
                configDto.setQqOpen(sysThird.getIsEnable());
            } else if (WEIBO.equals(sysThird.getCode())) {
                configDto.setWeiboOpen(sysThird.getIsEnable());
            }
        }
        return configDto;
    }

    /**
     * 设置基本信息 TODO
     *
     * @param config
     * @Title: setBaseInfo
     * @return: void
     */
    public GlobalConfigDTO setBaseInfo(GlobalConfigDTO configDto, CmsSite site, GlobalConfig config) {
        /** 名称 */
        // configDto.setName(site.getName());
        /** 标题 */
        configDto.setTitle(config.getConfigAttr().getTitle());
        /** 描述 */
        configDto.setDescription(config.getConfigAttr().getDescription());
        /** 关键词 */
        configDto.setKeywords(config.getConfigAttr().getKeywords());
        /** 客服电话 */
        configDto.setCustomerServiceMobile(config.getConfigAttr().getServerPhone());
        /** 客服邮箱 */
        configDto.setCustomerServiceEmail(config.getConfigAttr().getServerEmail());
        return configDto;
    }

    /**
     * 获取搜索热词 TODO
     *
     * @param config
     * @return
     * @Title: getSearchWords
     * @return: String
     */
    public String getSearchWords(GlobalConfig config) {
        /** 搜索热词 */
        String hotWords = "";
        if (StringUtils.isNotBlank(config.getConfigAttr().getHotsKeywords())) {
            // 多组关键词，随机展示其中一个
            String[] str = config.getConfigAttr().getHotsKeywords().split(WebConstants.ARRAY_SPT);
            Random random = new SecureRandom();
            hotWords = str[random.nextInt(str.length - 1 - 0)];
        }
        return hotWords;
    }

    /**
     * 获取区域列表
     *
     * @return
     * @throws GlobalException
     * @Title: getArea
     * @return: ArrayList<Area>
     */
    public ArrayList<Area> getArea() throws GlobalException {
        // 区域列表
        Serializable cacheAreas = cacheProvider.getCache(CacheConstants.SMS, Area.AREA_LIST_ATTRNAME);
        ArrayList<Area> areas = null;
        if (cacheAreas == null) {
            areas = (ArrayList<Area>) areaService.findByDictCode();
            cacheProvider.setCache(CacheConstants.SMS, Area.AREA_LIST_ATTRNAME, areas);
        } else {
            // areas = (ArrayList<Area>) cacheAreas;
        }
        return areas;
    }

    @Override
    public List<String> folderList(String relativePath) throws GlobalException {
        /**
         * 检测是否非法路径
         */
        FileUtils.isValidFilename(TplConstants.TPL_BASE, relativePath);
        relativePath = FileUtils.normalizeFilename(relativePath);
        List<String> resultList = new ArrayList<>();
        try {
            String path = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest().getSession().getServletContext().getRealPath(relativePath);
            File file = new File(path);
            File[] files = file.listFiles();
            for (File it : files) {
                if (it.isDirectory()) {
                    resultList.add(it.getAbsolutePath().replace(path, ""));
                }
            }
            return resultList;
        } catch (Exception e) {
            logger.error("获取文件路径失败: {}", e.getMessage());
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 校验配置信息
     *
     * @param currAttrs
     * @throws GlobalException
     * @Title: checkSetInfo
     * @return: void
     */
    public void checkSetInfo(Map<String, String> currAttrs) throws GlobalException {
        boolean isBaiduPush = false, baiduToken = false, isAutoLogout = false, autoLogoutMinute = false;
        for (Map.Entry<String, String> entry : currAttrs.entrySet()) {
            if (entry.getKey().equals(GlobalConfigAttr.IS_BAIDU_PUSH)) {
                isBaiduPush = entry.getValue().equals(GlobalConfigAttr.TRUE_STRING);
            }
            if (isBaiduPush && entry.getKey().equals(GlobalConfigAttr.BAIDU_PUSH_TOKEN)) {
                baiduToken = StringUtils.isBlank(entry.getValue());
            }
            if (entry.getKey().equals(GlobalConfigAttr.AUTO_LOGOUT)) {
                isAutoLogout = entry.getValue().equals(GlobalConfigAttr.TRUE_STRING);
            }
            if (isAutoLogout && entry.getKey().equals(GlobalConfigAttr.AUTO_LOGOUT_MINUTE)) {
                autoLogoutMinute = StringUtils.isBlank(entry.getValue());
            }
            // 将资源id插入进去
            if (entry.getKey().equals(GlobalConfigAttr.SYSTEM_FLAG_RESOURCE_ID)) {
                if (StringUtils.isNotBlank(entry.getValue())) {
                    ResourcesSpaceData data = sourceService
                            .findById(Integer.valueOf(entry.getValue()));
                    if (data != null) {
                        currAttrs.put(GlobalConfigAttr.SYSTEM_FLAG_RESOURCE_URL, data.getUrl().trim());
                    } else {
                        currAttrs.put(GlobalConfigAttr.SYSTEM_FLAG_RESOURCE_URL, "");
                    }
                } else {
                    currAttrs.put(GlobalConfigAttr.SYSTEM_FLAG_RESOURCE_URL, "");
                }
            }
            // 如果关闭了会员审核
            if (entry.getKey().equals(GlobalConfigAttr.MEMBER_REGISTER_EXAMINE)) {
                // 需要把待审核的会员全部通过
                if (entry.getValue().equals(GlobalConfigAttr.FALSE_STRING)) {
                    // 如果不开启，把待审核的会员改成审核通过
                    List<CoreUser> users = coreUserService.findList(null, null, null, null, false,
                            CoreUser.AUDIT_USER_STATUS_WAIT, null, null, null, null, null);
                    for (CoreUser coreUser : users) {
                        coreUser.setCheckStatus(CoreUser.AUDIT_USER_STATUS_PASS);
                        // 注册积分信息
                        memberScoreDetailsService.addMemberScore(MemberScoreDetails
                                        .REGISTER_SCORE_TYPE,
                                coreUser.getId(), coreUser.getSourceSiteId(), null);
                        // 注册积分信息
                        memberScoreDetailsService.addMemberScore(MemberScoreDetails
                                        .MESSAGE_SCORE_TYPE,
                                coreUser.getId(), coreUser.getSourceSiteId(), null);
                    }
                    coreUserService.batchUpdate(users);
                }
            }

        }
        // 如果开启了百度推送，且未填写信息提示该用户
        if (baiduToken) {
            throw new GlobalException(new SystemExceptionInfo(
                    SettingErrorCodeEnum.BAIDU_PUSH_TOKEN_NOT_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.BAIDU_PUSH_TOKEN_NOT_NULL.getCode()));
        }
        // 如果开启了登录后长时间未操作自动退出账号，且未填写信息，提示该用户
        if (autoLogoutMinute) {
            throw new GlobalException(new SystemExceptionInfo(
                    SettingErrorCodeEnum.AUTO_LOGOUT_MINUTE_NOT_NULL.getDefaultMessage(),
                    SettingErrorCodeEnum.AUTO_LOGOUT_MINUTE_NOT_NULL.getCode()));
        }
    }

    @Override
    public void change(Map<String, String> attrs) throws GlobalException {
        GlobalConfig config = get();
        config.getAttrs().putAll(attrs);
        super.updateAll(config);
    }

    protected  Map<String, String> doChangeDigest( Map<String, String> currAttrs,boolean changeDigest)throws GlobalException{
        return currAttrs;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
            //默认加载区域，第三方配置，微信开放平台表, 菜单表，API表，站点
            ThreadPoolService.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                            List<Area> areas = null;
                            List<SysThird> thirds = thirdService.getList();
                            AbstractWeChatOpen openConfig = abstractWeChatOpenService.findOpenConfig();
                            List<CmsSite> sites = cmsSiteService.findAll(false,true);
                            List<CoreMenu> all = coreMenuService.findAll(true);
                            List<CoreApi> api = coreApiService.findAll(true);
                            SysIptables iptables = iptablesService.getSysIptables();
                            try {
                                    areas = areaService.findByDictCode();
                            } catch (GlobalException e) {
                                    e.printStackTrace();
                            }
                            if (!thirds.isEmpty()) {
                                    logger.debug(thirds.stream().findFirst().get().getAppId());
                            }
                            if (areas != null && !areas.isEmpty()) {
                                    logger.debug(areas.stream().findFirst().get().getAreaName());
                            }
                            if (openConfig != null) {
                                    logger.debug(openConfig.getAppId());
                            }
                            if (!sites.isEmpty()) {
                                    logger.debug(sites.stream().findFirst().get().getName());
                            }
                            if (!all.isEmpty()) {
                                    logger.debug(all.stream().findFirst().get().getName());
                            }
                            if (!api.isEmpty()) {
                                    logger.debug(api.stream().findFirst().get().getApiName());
                            }
                            if(iptables!=null){
                                    logger.debug(iptables.getLimitDomain());
                            }
                    }
            });
        }

        @Autowired
        private AreaService areaService;
        @Autowired
        private CacheProvider cacheProvider;
        @Autowired
        private AddressService addressService;
        @Autowired
        private SysThirdService thirdService;
        @Autowired
        private CoreUserService userService;
        @Autowired
        private ResourcesSpaceDataService sourceService;
        @Autowired
    	private CoreUserService coreUserService;
        @Autowired
        private MemberScoreDetailsService memberScoreDetailsService;
        @Autowired
        private AbstractWeChatOpenService abstractWeChatOpenService;
        @Autowired
        private CmsSiteService cmsSiteService;
        @Autowired
        private CoreMenuService coreMenuService;
        @Autowired
        private CoreApiService coreApiService;
        @Autowired
        private SysIptablesService iptablesService;
        @Autowired
        private CmsDataPermCfgService permCfgService;
        @Lazy
        @Autowired
        private CmsDataPermService permService;
        @Autowired(required = false)
        private GradeProtectionService gradeProtectionService;
}
