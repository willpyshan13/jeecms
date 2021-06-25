/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.URLUtil;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.web.Location;
import com.jeecms.common.web.Location.LocationResult.AdInfo;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.session.SessionProvider;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.content.domain.Content;
import com.jeecms.content.service.ContentService;
import com.jeecms.publish.constants.PublishConstant;
import com.jeecms.publish.domain.vo.ContentFlowVo;
import com.jeecms.statistics.domain.dto.StatisticsFlowDto;
import com.jeecms.statistics.domain.dto.StatisticsFlowRealTimeItemDto;
import com.jeecms.statistics.domain.vo.SiteFlow;
import com.jeecms.system.dao.SysAccessRecordDao;
import com.jeecms.system.domain.Area;
import com.jeecms.system.domain.CmsSiteConfig;
import com.jeecms.system.domain.SysAccessRecord;
import com.jeecms.system.domain.vo.MassScoreVo;
import com.jeecms.system.service.AddressService;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.system.service.SysAccessRecordService;
import com.jeecms.util.SystemContextUtils;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

import static com.jeecms.common.constants.WebConstants.*;
import static com.jeecms.common.util.MyDateUtils.COM_YMDHMS_PATTERN;

/**
 * 浏览记录Service
 *
 * @author ljw
 * @version 1.0
 * @date 2019-06-22
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysAccessRecordServiceImpl extends BaseServiceImpl<SysAccessRecord, SysAccessRecordDao, Integer>
        implements SysAccessRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysAccessRecordServiceImpl.class);

    @Autowired
    private SessionProvider sessionProvider;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CmsSiteService siteService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentService contentService;
    @Resource(name = CacheConstants.ACCESSRECORD_CACHE)
    private Ehcache cache;
    @Resource(name = CacheConstants.SITE_FLOW_CACHE)
    private Ehcache flowCache;
    @Resource(name = CacheConstants.LAST_ACCESSRECORD_CACHE)
    private Ehcache lastAccessCache;
    @Resource(name = CacheConstants.UV_ACCESSRECORD_CACHE)
    private Ehcache uvAccessCache;
    @Resource(name = CacheConstants.IP_ACCESSRECORD_CACHE)
    private Ehcache ipAccessCache;
    private static LongAdder instance;

    /**
     * 间隔时间 10分钟
     */
    private final int interval = 60 * 1000;
    /**
     * 最后刷新时间,线程对volatile变量的修改会立刻被其他线程所感知，即不会出现数据脏读的现象，从而保证数据的“可见性”。
     */
    private volatile Long refreshTime = System.currentTimeMillis();


    /**
     * 使用单例模式维护这个累加器，累加器本身就是线程安全，可不加synchronized
     *
     * @return
     * @Title: getInstance
     */
    public static LongAdder getInstance() {
        if (instance == null) {
            instance = new LongAdder();
        }
        return instance;
    }

    @Override
    public void recordInfo(HttpServletRequest request) throws GlobalException {
        //调用加1
        getInstance().increment();
        SysAccessRecord record = new SysAccessRecord();
        // 得到会话ID
        String sessionId = RequestUtils.getRequestedSessionId(request);
        record.setSessionId(sessionId);
        // 判断是否登录访问
        CoreUser user = SystemContextUtils.getUser(request);
        String cookieValue = null;
        if (null != user) {
            record.setIsLogin(true);
            record.setLoginUserId(user.getId());
            record.setLoginUserName(user.getUsername());
            //判断新老客户，先判断UserId是否存在，然后在判断cookie
            Long sum = countUserId(user.getId());
            //判断是否新老客户
            if (sum > 1) {
                record.setNewVisitor(false);
            } else {
                record.setNewVisitor(true);
            }
        } else {
            record.setIsLogin(false);
            //遍历cookie,取cookie的值
            if (request.getCookies() != null && request.getCookies().length > 0) {
                for (Cookie cookie : request.getCookies()) {
                    if (WebConstants.IDENTITY_COOKIE.equals(cookie.getName())) {
                        cookieValue = cookie.getValue();
                        record.setCookieId(cookieValue);
                        Long sum = countCookie(cookieValue);
                        //判断是否新老客户
                        if (sum > 1) {
                            record.setNewVisitor(false);
                        } else {
                            record.setNewVisitor(true);
                        }
                    } else {
                        record.setNewVisitor(true);
                    }
                }
            }
        }
        //给是否新客户赋值
        if (null == record.getNewVisitor()) {
            record.setNewVisitor(true);
        }
        Integer siteId = SystemContextUtils.getSiteId(request);
        record.setSiteId(siteId);
        //访客IP
        record.setAccessIp(RequestUtils.getRemoteAddr(request));
        //访问网站(点击链接才可获取)
        String accessUrl = request.getHeader("Referer");
        if (StringUtils.isNotBlank(accessUrl)) {
            if (accessUrl.length() >= 251) {
                accessUrl = accessUrl.substring(0, 251);
            }
        } else {
            accessUrl = "localhost";
        }
        record.setAccessUrl(accessUrl);
        //来源网站
        String sourceUrl = request.getHeader("Referer");
        String sourceUrlSub = "localhost";
        if (StringUtils.isNotBlank(sourceUrl)) {
            URL url = URLUtil.url(sourceUrl);
            if (url != null) {
                sourceUrlSub = url.getProtocol() + "://" +   url.getHost();
            }
        }
        record.setEngineName(engineName(sourceUrl));
        //来源域名
        record.setSourceDomain(sourceUrlSub);
        //来源网站
        record.setSourceUrl(sourceUrlSub);
        //来源网站类型
        if (StringUtils.isNotBlank(sourceUrl) && sourceUrl.equals(RequestUtils.getServerUrl(request))) {
            record.setSorceUrlType(SysAccessRecord.RESOURCE_SELF);
        } else {
            record.setSorceUrlType(sorceUrlType(sourceUrl));
        }
        //设备
        record = device(request, record);
        //访问来源客户端类型
        Short acessTypeWx = SystemContextUtils.isWxH5() ? SysAccessRecord.ACCESS_TYPE_WECHAT_H5 : null;
        Short acessTypePc = SystemContextUtils.isPc() ? SysAccessRecord.ACCESS_TYPE_PC : acessTypeWx;
        Short accessType = SystemContextUtils.isMobile() || SystemContextUtils.isTablet()
                ? SysAccessRecord.ACCESS_TYPE_MOBILE_H5
                : acessTypePc;
        record.setAccessSourceClient(accessType);
        //访问浏览器类型
        browser(request, record);
        //城市，国家，省份
        area(request, record);
        //异步存取
        if (Integer.MAX_VALUE <= getInstance().intValue()) {
            instance.reset();
        }
        //统计内容以及栏目首页，基于x1.4的内容统计扩展
        pageType(accessUrl, record);
        // 把访问记录放缓存里
        cache.put(new Element(instance.intValue(), record));
        lastAccessCache.put(new Element(siteId, record));
        if (null != user) {
            uvAccessCache.put(new Element(sessionId + "_" + siteId, record));
        } else {
            uvAccessCache.put(new Element(cookieValue + "_" + siteId, record));
        }
        ipAccessCache.put(new Element(RequestUtils.getRemoteAddr(request) + "_" + siteId, record));
        refreshToDB();
    }

    public static void main(String[] args) {
        String sourceUrl = "http://demo.jeecms.com/gj/2573.jhtml";
        String sourceUrlSub = "localhost";
        if (StringUtils.isNotBlank(sourceUrl)) {
            URL url = URLUtil.url(sourceUrl);
            if (StringUtils.isNotBlank(url.getHost())) {
                sourceUrlSub = url.getProtocol() + "://" + url.getHost();
            }
        }
        System.out.println(sourceUrlSub);
        String url = "/phtoo.html";
        String url2 = "/p1/1111.html";
        System.out.println(ReUtil.isMatch(REG_STATIC_PC_PATH, url));
        System.out.println(ReUtil.isMatch(REG_STATIC_PC_PATH, url2));
        url2 = url2.substring(url2.indexOf("/", 1));
        System.out.println(url2);
    }

    @Override
    public List<SysAccessRecord> getSource(Date beginTime, Date endTime,
                                           Integer siteId,
                                           Boolean newVisitor,
                                           Short accessSourceClient,
                                           Integer sourceType) {
        return dao.getSource(beginTime, endTime, siteId, newVisitor, accessSourceClient, sourceType);
    }

    @Override
    public List<SysAccessRecord> getAccessPage(Date beginTime, Date endTime,
                                               Integer siteId,
                                               Boolean newVisitor,
                                               Integer sorceUrlType) {
        return dao.getAccessPage(beginTime, endTime, siteId, newVisitor, sorceUrlType);
    }

    /**
     * 将缓存中的访问记录保存到数据库
     *
     * @throws GlobalException 异常
     * @Title: refreshToDB
     */
    protected void refreshToDB() throws GlobalException {
        long time = System.currentTimeMillis();
        if (time > refreshTime + interval) {
            //直接加重锁
            synchronized (refreshTime) {
                saveToDB(cache);
                // 清除缓存
                cache.removeAll();
                uvAccessCache.removeAll();
                ipAccessCache.removeAll();
                freshSiteAttrCacheToDB(flowCache);
                flowCache.removeAll();
                LOGGER.info("刷新时间：{}",
                        MyDateUtils.formatDate(new Date(refreshTime), COM_YMDHMS_PATTERN));
                refreshTime = time;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void saveToDB(Ehcache cache) throws GlobalException {
        List<Integer> keys = cache.getKeys();
        int keySize = keys.size();
        if (keySize <= 0) {
            return;
        }
        List<SysAccessRecord> accessRecords = new ArrayList<>(keySize);
        Element element;
        for (Integer it : keys) {
            element = cache.get(it);
            if (element != null) {
                accessRecords.add((SysAccessRecord) element.getObjectValue());
            }
        }
        accessRecords = super.saveAll(accessRecords);
        super.flush();
    }

    /**
     * 缓存入库
     *
     * @param cache 缓存
     */
    @Override
    public void freshSiteAttrCacheToDB(Ehcache cache) {
        @SuppressWarnings("unchecked")
        List<String> list = cache.getKeys();
        for (String key : list) {
            String[] str = key.split("_");
            Element siteFlowCache = cache.get(key);
            SiteFlow siteFlow = (SiteFlow) siteFlowCache.getObjectValue();
            Map<String, String> attr = new HashMap<String, String>(6);
            //	CmsSite site=CmsThreadVariable.getSite();
            //总
            attr.put(CmsSiteConfig.PV_TOTAL, siteFlow.getPvTotal().toString());
            attr.put(CmsSiteConfig.UV_TOTAL, siteFlow.getUvTotal().toString());
            attr.put(CmsSiteConfig.IP_TOTAL, siteFlow.getIpTotal().toString());
            //今日
            attr.put(CmsSiteConfig.TODAY_PV, siteFlow.getTodayPv().toString());
            attr.put(CmsSiteConfig.TODAY_UV, siteFlow.getTodayUv().toString());
            attr.put(CmsSiteConfig.TODAY_IP, siteFlow.getTodayIp().toString());
            Integer siteId = Integer.parseInt(str[1]);
            siteService.updateAttr(siteId, attr);
        }
    }

    /**
     * 访客设备系统
     *
     * @param request 请求
     * @param record  设备对象
     * @Title: device
     */
    protected SysAccessRecord device(HttpServletRequest request, SysAccessRecord record) {
        // 分析浏览器UserAgent,得到设备信息
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.isNotBlank(userAgent)) {
            record.setAccessDevice(SysAccessRecord.OTHERS);
            record.setDeviceType(SysAccessRecord.MOBIE);
        }
        //Windows 10,8,7版本
        if (userAgent.contains("Windows NT 10.0")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_WINDOWS_10);
            record.setDeviceType(SysAccessRecord.COMPUTER);
        } else if (userAgent.contains("Windows NT 6.2")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_WINDOWS_8);
            record.setDeviceType(SysAccessRecord.COMPUTER);
        } else if (userAgent.contains("Windows NT 6.1")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_WINDOWS_7);
            record.setDeviceType(SysAccessRecord.COMPUTER);
        } else if (userAgent.contains("iPhone OS 12")) {
            //苹果12,11,10
            record.setAccessDevice(SysAccessRecord.DEVICE_IPHONE_12);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else if (userAgent.contains("iPhone OS 11")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_IPHONE_11);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else if (userAgent.contains("iPhone OS 10")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_IPHONE_10);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else if (userAgent.contains("Android 8")) {
            //安卓8,7,6
            record.setAccessDevice(SysAccessRecord.DEVICE_ANDROID_8);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else if (userAgent.contains("Android 7")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_ANDROID_7);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else if (userAgent.contains("Android 6")) {
            record.setAccessDevice(SysAccessRecord.DEVICE_ANDROID_6);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else if (userAgent.contains(SysAccessRecord.DEVICE_MAC)) {
            record.setAccessDevice(SysAccessRecord.DEVICE_MAC);
            record.setDeviceType(SysAccessRecord.MOBIE);
        } else {
            record.setAccessDevice(SysAccessRecord.OTHERS);
            record.setDeviceType(SysAccessRecord.MOBIE);
        }
        return record;
    }

    /**
     * 浏览器
     *
     * @param request 请求
     * @param record  设备对象
     * @Title: browser
     */
    protected SysAccessRecord browser(HttpServletRequest request, SysAccessRecord record) {
        // 分析浏览器UserAgent,得到设备信息
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.contains(SysAccessRecord.BROWSER_CHROME)) {
            record.setAccessBrowser(SysAccessRecord.BROWSER_CHROME);
        } else if (userAgent.contains(SysAccessRecord.BROWSER_FIREFOX)) {
            record.setAccessBrowser(SysAccessRecord.BROWSER_FIREFOX);
        } else if (userAgent.contains(SysAccessRecord.BROWSER_EDGE)) {
            record.setAccessBrowser(SysAccessRecord.BROWSER_EDGE);
        } else if (userAgent.contains(SysAccessRecord.BROWSER_SAFARI) && userAgent.contains("version")) {
            record.setAccessBrowser(SysAccessRecord.BROWSER_SAFARI);
        } else {
            record.setAccessBrowser(SysAccessRecord.OTHERS);
        }
        return record;
    }

    /**
     * 判断URL来源
     *
     * @param url 来源url
     * @Title: sorceUrlType
     */
    protected Integer sorceUrlType(String url) {
        if (Web.get().contains(url)) {
            return SysAccessRecord.RESOURCE_SEARCHER;
        } else {
            return SysAccessRecord.RESOURCE_EXT;
        }
    }

    /**
     * 搜索引擎名称
     **/
    protected String engineName(String url) {
        if (!StringUtils.isNotBlank(url)) {
            return "其他";
        }
        if (url.equalsIgnoreCase(Web.BAIDU.getUrl())) {
            return "百度搜索";
        } else if (url.equalsIgnoreCase(Web.SO.getUrl())) {
            return "360搜索";
        } else if (url.equalsIgnoreCase(Web.SOGOU.getUrl())) {
            return "搜狗搜索";
        } else if (url.equalsIgnoreCase(Web.CHINASO.getUrl())) {
            return "中国搜索";
        } else if (url.equalsIgnoreCase(Web.BING.getUrl())) {
            return "微软搜索";
        } else if (url.equalsIgnoreCase(Web.YAHOO.getUrl())) {
            return "雅虎搜索";
        } else if (url.equalsIgnoreCase(Web.GOOGLE.getUrl())) {
            return "谷歌搜索";
        } else {
            return "其他";
        }
    }

    public enum Web {
        BAIDU("https://www.baidu.com"),
        SO("https://www.so.com"),
        SOGOU("https://www.sogou.com"),
        BING("https://cn.bing.com"),
        YAHOO("https://search.yahoo.com"),
        GOOGLE("https://www.google.com"),
        CHINASO("http://www.chinaso.com"),
        ;

        Web(String url) {
            this.url = url;
        }

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public static List<String> get() {
            return Arrays.asList(BAIDU.getUrl(), SO.getUrl(), SOGOU.getUrl(),
                    BING.getUrl(), YAHOO.getUrl(), GOOGLE.getUrl(), CHINASO.getUrl());
        }
    }

    /**
     * 国家，省会，城市，
     *
     * @param request 请求
     * @param record  设备对象
     * @Title: area
     */
    protected SysAccessRecord area(HttpServletRequest request, SysAccessRecord record) {
        //国家
        String country = null;
        //省份
        String province = null;
        //市区
        String city = null;
        Serializable sessionAddress = sessionProvider.getAttribute(request, Area.CURRENT_ADDRESS_ATTRNAME);
        Location location = null;
        AdInfo adInfo = null;
        // 定位
        if (sessionAddress == null) {
            String currentIp = RequestUtils.getRemoteAddr(request);
            try {
                location = addressService.getAddressByIP(currentIp);
                request.getSession().setAttribute(Area.CURRENT_ADDRESS_ATTRNAME, location);
                if (location != null && location.getResult() != null) {
                    adInfo = location.getResult().getAdInfo();
                    city = adInfo.getCity();
                    country = adInfo.getNation();
                    province = adInfo.getProvince();
                }
            } catch (Exception e) {
                LOGGER.error("定位失败: {}", e.getMessage());
            }
        } else {
            location = (Location) sessionAddress;
            if (location.getResult() != null) {
                adInfo = location.getResult().getAdInfo();
                city = adInfo.getCity();
                country = adInfo.getNation();
                province = adInfo.getProvince();
            }
        }
        //应前端需要，将省份，城市中的XX省，XX市去除
        if (StringUtils.isNotBlank(province) && province.contains("省")) {
            province = province.replace("省", "");
        }
        //去除直辖市
        if (StringUtils.isNotBlank(province) && province.contains("市")) {
            province = province.replace("市", "");
        }
        //去除自治区
        if (StringUtils.isNotBlank(province) && province.contains("自治区")) {
            province = province.replace("自治区", "");
            //特殊处理内蒙古
            if (province.contains("内蒙古")) {
                province = province.substring(0, 3);
            } else {
                province = province.substring(0, 2);
            }
        }
        if (StringUtils.isNotBlank(city) && city.contains("市")) {
            city = city.replace("市", "");
        }
        record.setAccessCountry(StringUtils.isNotBlank(country) ? country : "其他");
        record.setAccessProvince(StringUtils.isNotBlank(province) ? province : "其他");
        record.setAccessCity(StringUtils.isNotBlank(city) ? city : "其他");
        //不是中国，改为其他
        if (!"中国".equals(country)) {
            record.setAccessCountry("其他");
            record.setAccessProvince("其他");
            record.setAccessCity("其他");
        }
        return record;
    }

    /**
     * 访问页面类型
     **/
    protected void pageType(String accessUrl, SysAccessRecord record) {
        if (StringUtils.isNotBlank(accessUrl)) {
            String path = URLUtil.getPath(accessUrl);
            //是否开启内容，开启内网之后需要去除/c的路径符
            Boolean flag = siteService.findById(record.getSiteId()).getGlobalConfig().getIsIntranet();
            if (flag) {
                path = path.replace("/c", "");
            }
            if (StringUtils.isNotBlank(path) && path.contains("/")) {
                /**截取静态页前缀 类似 /p1 /m1 */
                /**判断是否是 /p1/news.html /m1/233.html 类似这样的静态页 */
                if (ReUtil.isMatch(REG_STATIC_PC_PATH, path) || ReUtil.isMatch(REG_STATIC_MOBILE_PATH, path)) {
                    path = path.substring(path.indexOf("/", 1));
                }
                String[] split = path.split("/");
                //根据leng为2为栏目，3位内容，0为首页
                if (split.length == 0) {
                    record.setPageType(PublishConstant.PAGE_TYPE_INDEX);
                } else if (split.length == 2 && getString(split[1]) != null) {
                    if (INDEX_HTML.equals(path)) {
                        record.setPageType(PublishConstant.PAGE_TYPE_INDEX);
                    } else {
                        record.setPageType(PublishConstant.PAGE_TYPE_CHANNEL);
                        //根据栏目路径查询
                        Channel channel = channelService.findByPath(getString(split[1]), record.getSiteId());
                        if (channel != null) {
                            record.setChannelId(channel.getId());
                        } else {
                            record.setPageType(PublishConstant.PAGE_TYPE_OTHER);
                        }
                    }
                } else if (split.length == 4 || split.length == 3) {
                    /**
                     * 静态页访问规则地址 /p1/gj/20191203/2573.html 和动态页访问规则地址 /gj/2573.jhtml
                     **/
                    record.setPageType(PublishConstant.PAGE_TYPE_CONTENT);
                    String p = getString(split[split.length - 1]);
                    if (p != null) {
                        Content content = contentService.findById(Integer.valueOf(p));
                        if (content != null) {
                            record.setChannelId(content.getChannelId());
                            record.setContentId(Integer.valueOf(p));
                        } else {
                            record.setPageType(PublishConstant.PAGE_TYPE_OTHER);
                        }
                    } else {
                        record.setPageType(PublishConstant.PAGE_TYPE_OTHER);
                    }
                } else {
                    if (INDEX_HTML.equals(path)) {
                        record.setPageType(PublishConstant.PAGE_TYPE_INDEX);
                    } else {
                        record.setPageType(PublishConstant.PAGE_TYPE_OTHER);
                    }
                }
            }
        } else {
            record.setPageType(PublishConstant.PAGE_TYPE_OTHER);
        }
    }

    /**
     * 分割
     **/
    private String getString(String key) {
        if (key.contains(".")) {
            return key.substring(0, key.indexOf("."));
        } else {
            return null;
        }
    }

    @Override
    public Long countCookie(String cookie) {
        return dao.countByCookieId(cookie);
    }

    @Override
    public Long countUserId(Integer userId) {
        return dao.countByLoginUserId(userId);
    }

    @Override
    public long countIp(Date date, Integer siteId) {
        return dao.countIp(date, siteId);
    }

    @Override
    public List<SysAccessRecord> haveList(Date start, Date end, Integer siteId,
                                          Integer sourceType, String province, Boolean visitor) {
        return dao.getList(start, end, siteId, sourceType, province, visitor);
    }

    @Override
    public List<SysAccessRecord> findByDate(Date startTime, Date endTime, Integer siteId, Integer contentId) {
        return dao.findByDate(startTime, endTime, siteId, contentId);
    }

    @Override
    public long getContentByDate(Date date, Integer siteId) {
        return dao.getContentByDate(date, siteId);
    }

    @Override
    public List<SysAccessRecord> getRealTimeItem(StatisticsFlowRealTimeItemDto dto, Date startTime, Date endTime) {
        return dao.getRealTimeItem(startTime, endTime, dto);
    }

    @Override
    public List<SysAccessRecord> getFlow(StatisticsFlowDto dto) {
        return dao.getFlow(dto);
    }

    @Override
    public SysAccessRecord findBySessionId(String sessionId, Integer siteId, Date date) {
        return dao.findBySession(sessionId, siteId, date);
    }

    @Override
    public SysAccessRecord findByIp(String ip, Integer siteId, Date date) {
        return dao.findByIp(ip, siteId, date);
    }

    @Override
    public List<ContentFlowVo> flowContribution(Integer siteId, Date startDate, Date endDate) {
        return dao.flowContribution(siteId, startDate, endDate);
    }

    @Override
    public List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate) {
        return dao.massCount(contentId, startDate, endDate);
    }

    @Override
    public List<SysAccessRecord> typeList(Integer type, Date startDate, Date endDate) {
        return dao.typeList(type, startDate, endDate);
    }

    @Override
    public long getPeopleViews(Integer contentId) {
        return dao.getPeopleViews(contentId);
    }

    @Override
    public void deleteByDaysAgo(Date time) {
        dao.deleteAllByCreateTimeBefore(time);
    }


}