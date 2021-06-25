/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
import com.jeecms.auth.domain.CoreApi;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreApiService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.configuration.ThreadPoolConfiguration;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.LoggerUtils;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.SnowFlake;
import com.jeecms.common.util.UserAgentUtils;
import com.jeecms.common.web.cache.CacheConstants;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.system.constants.LogConstants;
import com.jeecms.system.constants.LogConstants.StatisticsType;
import com.jeecms.system.dao.SysLogDao;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.StatisticsLogResult;
import com.jeecms.system.domain.SysLog;
import com.jeecms.system.domain.dto.SearchUserLogDto;
import com.jeecms.system.domain.vo.SysLogSelectVo;
import com.jeecms.system.service.SysLogService;
import com.jeecms.util.SystemContextUtils;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.toJSONString;
import static com.jeecms.common.constants.WebConstants.ADMIN_PREFIX;
import static com.jeecms.system.constants.LogConstants.LOG_OPERATE_DOWNLOAD;
import static com.jeecms.system.constants.LogConstants.LOG_OPERATE_EXPORT;
import static com.jeecms.system.constants.LogConstants.LOG_OPERATE_IMPORT;
import static com.jeecms.system.constants.LogConstants.LOG_OPERATE_LOGIN;
import static com.jeecms.system.constants.LogConstants.LOG_OPERATE_LOGOUT;
import static com.jeecms.system.constants.LogConstants.LOG_OPERATE_UPLOAD;

/**
 * 日志Service实现类
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-05-29 16:55:15
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysLogServiceImpl extends BaseServiceImpl<SysLog, SysLogDao, Integer> implements SysLogService {
    private static final Logger logger = LoggerFactory.getLogger(SysLogServiceImpl.class);

    @Autowired
    private CoreApiService apiService;
    @Resource(name = CacheConstants.SYS_LOG_CACHE)
    private Ehcache cache;

    @Override
    public Page<SysLog> getPage(SearchUserLogDto dto, Set<String> name, Pageable pageable) {
        return dao.getPage(dto, name, pageable);
    }

    @Override
    public List<SysLog> getList(SearchUserLogDto dto, Set<String> name) {
        return dao.getList(dto, name);
    }

    @Override
    public JSONObject statistics(Date beginTime, Date endTime, int statisticsType, int timeCategory, Boolean isToday) {
        LogConstants.LogGroup logGroup = LogConstants.LogGroup.values()[timeCategory];
        if (isToday == null) {
            Date now = Calendar.getInstance().getTime();
            if (LogConstants.LogGroup.HOUR.equals(logGroup)) {
                beginTime = beginTime == null ? MyDateUtils.getStartDate(now) : beginTime;
                endTime = endTime == null ? MyDateUtils.getFinallyDate(now) : endTime;
            } else if (LogConstants.LogGroup.DAY.equals(logGroup)) {
                beginTime = beginTime == null ? MyDateUtils.getSpecficMonthStart(now, 0) : beginTime;
                endTime = endTime == null ? MyDateUtils.getSpecficMonthEnd(now, 0) : endTime;
            } else if (LogConstants.LogGroup.MONTH.equals(logGroup)) {
                beginTime = beginTime == null ? MyDateUtils.getSpecficYearStart(now, 0) : beginTime;
                endTime = endTime == null ? MyDateUtils.getSpecficYearEnd(now, 0) : endTime;
            }
        }
        List<SysLogSelectVo> logs = dao.getList(beginTime, endTime);
        StatisticsType type = StatisticsType.values()[statisticsType];
        Map<String, List<SysLogSelectVo>> map = null;
        List<String> list = null;
        JSONObject jo = new JSONObject();
        switch (type) {
            case OPERATIONRESULT:
                map = logs.parallelStream().filter(o -> o.getRequestResult() != null)
                        .collect(Collectors.groupingBy(o -> LogConstants.status(o.getRequestResult())));
                list = Arrays.asList("成功", "失败");
                break;
            case EVENTTYPE:
                map = logs.parallelStream().filter(o -> o.getEventType() != null)
                        .collect(Collectors.groupingBy(o -> LogConstants.eventType(o.getEventType())));
                list = Arrays.asList("系统事件", "业务事件");
                break;
            case OPERATIONTYPE:
                map = logs.parallelStream().filter(o -> o.getOperateType() != null)
                        .collect(Collectors.groupingBy(o -> LogConstants.operate(o.getOperateType())));
                list = Arrays.asList("查询", "新增", "修改", "删除", "导出", "导入", "上传", "下载", "登录", "退出");
                break;
            case USERNAME:
                map = logs.parallelStream().filter(o -> o.getUsername() != null)
                        .collect(Collectors.groupingBy(SysLogSelectVo::getUsername));
                list = new ArrayList<>(map.keySet());
                JSONArray jsonArray = new JSONArray();
                for (String s : list) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", s);
                    jsonObject.put("日志条数", map.get(s).size());
                    jsonArray.add(jsonObject);
                }
                jo.put("data", jsonArray);
                return jo;
            default:
                break;
        }
        Set<String> set = map.keySet();
        List<String> timeList;
        if (LogConstants.LogGroup.YEAR.equals(logGroup)) {
            Map<String, List<SysLogSelectVo>> timeMap = logs.parallelStream().filter(o -> o.getCreateTime() != null)
                    .collect(Collectors.groupingBy(o -> MyDateUtils.formatDate(o.getCreateTime(), MyDateUtils.COM_Y_PATTERN)));
            timeList = new ArrayList<String>(timeMap.keySet());
        } else {
            timeList = LogConstants.getTimeList(logGroup, beginTime, endTime);
        }
        JSONArray array = new JSONArray();
        for (String s : list) {
            JSONObject object = new JSONObject();
            object.put("name", s);
            if (set.contains(s)) {
                List<SysLogSelectVo> sysLogs = map.get(s);
                Map<String, List<SysLogSelectVo>> collect = getMap(sysLogs, logGroup);
                Set<String> timeSet = collect.keySet();
                for (String time : timeList) {
                    if (timeSet.contains(time)) {
                        object.put(time, collect.get(time).size());
                    } else {
                        object.put(time, 0);
                    }
                }
            } else {
                for (String time : timeList) {
                    object.put(time, 0);
                }
            }
            array.add(object);
        }
        jo.put("data", array);
        jo.put("fields", timeList.toArray());
        return jo;
    }

    /**
     * 根据时间
     *
     * @param sysLogs  数据
     * @param logGroup 小时，天， 月， 年
     * @return
     */
    private Map<String, List<SysLogSelectVo>> getMap(List<SysLogSelectVo> sysLogs, LogConstants.LogGroup logGroup) {
        String pattern;
        switch (logGroup) {
            case HOUR:
                pattern = MyDateUtils.COM_H_PATTERN;
                break;
            case DAY:
                pattern = MyDateUtils.COM_Y_M_D_PATTERN;
                break;
            case MONTH:
                pattern = MyDateUtils.COM_Y_M_PATTERN;
                break;
            case YEAR:
                pattern = MyDateUtils.COM_Y_PATTERN;
                break;
            default:
                pattern = MyDateUtils.COM_H_PATTERN;
                break;
        }
        return sysLogs.parallelStream().filter(o -> o.getCreateTime() != null)
                .sorted(Comparator.comparing(SysLogSelectVo::getCreateTime))
                .collect(Collectors.groupingBy(o -> MyDateUtils.formatDate(o.getCreateTime(), pattern)));
    }

    @SuppressWarnings({"unused", "rawtypes"})
    @Override
    public JSONArray groupByCreateDate(LogConstants.LogGroup type, Integer requestResult,
                                       Integer eventType, Integer operateType) throws GlobalException {
        List list = dao.groupByCreateDate(type, requestResult, eventType, operateType);
        JSONArray jsonArray = new JSONArray();
        for (Object obj : list) {
            String json = toJSONString(obj).replaceAll("\"", "");
            String[] arr = json.substring(1, json.length() - 1).split(",");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(arr[0], arr[1]);
            jsonArray.add(jsonObject);
            StatisticsLogResult logResult = new StatisticsLogResult();
        }
        return jsonArray;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List hourlyStatistics(StatisticsType type) {
        return dao.hourlyStatistics(type);
    }

    @Override
    public void saveLog(HttpServletRequest request) {
        String urlPath = RequestUtils.getRequestUrl(request);
        //调用线程池中的线程进行异步处理
        SysLog log = new SysLog();
        // 设置客户端ip
        log.setClientIp(LoggerUtils.getCliectIp(request));
        // 设置请求方法
        log.setMethod(request.getMethod());
        // 设置请求参数内容json字符串
        String paramData = toJSONString(request.getParameterMap(),
                SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue);
        log.setParamData(paramData);
        // 设置sessionId
        String sessionId = request.getRequestedSessionId();
        log.setSessionId(sessionId);
        // 请求耗时
        Object sendTime = request.getAttribute(WebConstants.LOGGER_SEND_TIME);
        if (sendTime != null) {
            long time = Long.parseLong(sendTime.toString());
            long currentTime = System.currentTimeMillis();
            log.setTimeConsuming(Integer.valueOf((currentTime - time) + ""));
            //接口返回时间
            log.setReturmTime(currentTime + "");
        }
        //浏览器
        log.setBrowser(UserAgentUtils.getBrowserInfo(request));
        //操作系统
        log.setOs(UserAgentUtils.getClientOS(request));
        //用户代理
        log.setUserAgent(UserAgentUtils.getBrowerUserAgent(request));
        // 设置请求地址
        log.setUri(urlPath);
        String methods = request.getMethod();
        CoreUser user = SystemContextUtils.getCoreUser();
        CmsSite site = SystemContextUtils.getSite(request);
        asyncLog(methods, log, null, user, site);
    }

    public static final Map<String, Short> METHODS = ImmutableMap.of("GET", (short) 1, "POST", (short) 2, "DELETE", (short) 4,
            "UPDATE", (short) 3, "PUT", (short) 3);

    /**
     * 备注：@Async 标识加入线程池，asyncServiceExecutor为{@link ThreadPoolConfiguration#asyncServiceExecutor()}
     */
    @Override
    @Async("asyncServiceExecutor")
    public void asyncLog(String method, SysLog log, ResponseInfo info, CoreUser coreUser, CmsSite site) {
        try {
            //option请求不记录
            if (WebConstants.OPTIONS.equalsIgnoreCase(method)) {
                return;
            }
            if (StringUtils.isBlank(method)) {
                return;
            }
            Short methodValue = CoreApi.METHODS.get(method.toUpperCase());
            if (methodValue == null) {
                return;
            }
            log.setUri(autoDealUrl(log.getUri()));
            CoreApi api = apiService.findByUrl(log.getUri(), methodValue);
            //日志分类
            log.setLogCategory(getLogCategoryByUri(log.getUri()));
            //日志类别
            log.setLogType(SysLog.LOG_TYPE_INFO);
            //事件类型
            log.setEventType(getLogEventTypeByCode(log.getUri()));
            //日志级别
            log.setLogLevel(SysLog.LOG_LEVEL_LOW);
            //操作类型
            if (api != null) {
                if ((ADMIN_PREFIX + "/logs/alarm/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/smartFormData/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/questionnaireAnswer/subject/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/questionnaireAnswer/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/collectContents/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/sensitiveWords/download").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/template/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/logs/export").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/sitegeneral/excel/user").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/sitegeneral/excel/org").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/sitegeneral/excel/site").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/pfscore/excel").equals(api.getApiUrl())) {
                    log.setOperateType(LOG_OPERATE_EXPORT);
                } else if ((ADMIN_PREFIX + "/contentext/docImport").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/sensitiveWords/import").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/template/import").equals(api.getApiUrl())) {
                    log.setOperateType(LOG_OPERATE_IMPORT);
                } else if ((ADMIN_PREFIX + "/wechatMaterial/upload/img").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/resource/upload").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/template/upload").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/upload/o_upload").equals(api.getApiUrl())) {
                    log.setOperateType(LOG_OPERATE_UPLOAD);
                } else if ((ADMIN_PREFIX + "/questionnaire/qrcode").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/databaseBackup/download").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/statistics/download").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/wechatMaterial/dowload/material").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/wechatMaterial/dowload/video").equals(api.getApiUrl()) ||
                        (ADMIN_PREFIX + "/resourceSpaceDatas/download").equals(api.getApiUrl())) {
                    log.setOperateType(LOG_OPERATE_DOWNLOAD);
                } else if ((ADMIN_PREFIX + "/login").equals(api.getApiUrl())) {
                    log.setOperateType(LOG_OPERATE_LOGIN);
                } else if ((ADMIN_PREFIX + "/logout").equals(api.getApiUrl())) {
                    log.setOperateType(LOG_OPERATE_LOGOUT);
                } else {
                    log.setOperateType(Integer.valueOf(METHODS.get(method.toUpperCase())));
                }
            } else {
                log.setOperateType(Integer.valueOf(METHODS.get(method.toUpperCase())));
            }
            //事件子类型
            log.setSubEventType(api != null ? api.getApiName() : "");
            //操作用户名
            if (coreUser != null) {
                log.setUsername(coreUser.getUsername());
            }
            //响应数据,暂时不记录，存在json转换出现循环依赖问题
//	            log.setReturnData(JSONObject.toJSONString(info));
            // 设置返回错误码
            if (info != null) {
                Integer code = info.getCode();
                log.setHttpStatusCode(code + "");
                //操作结果
                log.setRequestResult(code != null && SystemExceptionEnum.SUCCESSFUL.getCode().equals(String.valueOf(code)) ? 1 : 2);
            } else {
                log.setHttpStatusCode("200");
                log.setRequestResult(1);
            }

            if (coreUser != null) {
                log.setCreateUser(coreUser.getUsername());
            } else {
                log.setCreateUser(WebConstants.ANONYMOUSUSER);
            }
            SnowFlake snowFlake = new SnowFlake(SnowFlake.LONG_STR_CODE);
            /**先存入缓存中，后续批量入库*/
            cache.put(new Element(Long.toString(snowFlake.nextId()).toString(), log));
            refreshToDB();
            //检测当前操作是否存在越权的高危行为，存在需要再次存储数据并发送邮件或短信警告
            dealWarnLog(log, api, coreUser, site);
        } catch (GlobalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("日志记录异常:{}", e.getMessage());
        }
    }

    @Override
    public void deleteByDaysAgo(Date time) {
        dao.deleteAllByCreateTimeBefore(time);
    }


    private void refreshToDB() throws GlobalException {
        long time = System.currentTimeMillis();
        if (time > refreshTime + interval) {
            refreshTime = time;
            int accessCount = freshSysLogCacheToDB(cache);
            // 清除缓存
            cache.removeAll();
            logger.info("refresh cache log to DB: {}", accessCount);
        }
    }

    private int freshSysLogCacheToDB(Ehcache cache) throws GlobalException {
        int count = 0;
        List<String> list = cache.getKeys();
        List<SysLog> logs = new ArrayList<>();
        for (String key : list) {
            Element element = cache.get(key);
            if (element == null) {
                return count;
            }
            SysLog log = (SysLog) element.getObjectValue();
            if (log != null) {
                logs.add(log);
                count++;
            }
        }
        super.saveAll(logs);
        return count;
    }


    /**
     * 根据请求路径获取日志分类
     *
     * @param urlPath
     * @return
     */
    private Integer getLogCategoryByUri(String urlPath) {
        //审计日志请求接口前缀 /admin/log 、/admin/logs
        String audit_log_prefix = ADMIN_PREFIX + "/log/";
        String audit_logs_prefix = ADMIN_PREFIX + "/logs/";
        String audit_logConfigs_prefix = ADMIN_PREFIX + "/logConfigs";

        //业务日志请求接口前缀 /admin/channel 、/admin/content
        String bus_channel_log_prefix = ADMIN_PREFIX + "/channel";
        String bus_content_log_prefix = ADMIN_PREFIX + "/content";

        if (urlPath.startsWith(audit_log_prefix)
                || urlPath.startsWith(audit_logs_prefix)
                || urlPath.startsWith(audit_logConfigs_prefix)) {
            return SysLog.LOG_CATEGORY_AUDIT;
        } else if (urlPath.startsWith(bus_channel_log_prefix)
                || urlPath.startsWith(bus_content_log_prefix)) {
            return SysLog.LOG_CATEGORY_BUS;
        }
        return SysLog.LOG_CATEGORY_SYSTEM;
    }

    /**
     * 根据请求路径获取日志事件类型
     *
     * @param urlPath
     * @return
     */
    private Integer getLogEventTypeByCode(String urlPath) {
        //业务日志请求接口前缀 /admin/channel 、/admin/content
        String bus_channel_log_prefix = ADMIN_PREFIX + "/channel";
        String bus_content_log_prefix = ADMIN_PREFIX + "/content";
        if (urlPath.startsWith(bus_channel_log_prefix) || urlPath.startsWith(bus_content_log_prefix)) {
            return SysLog.LOG_EVENT_TYPE_BUS;
        }
        return SysLog.LOG_EVENT_TYPE_SYSTEM;
    }


    /**
     * 处理/users/123 格式的url转换为 /users/{id:[0-9]+}格式, 不满足原样返回
     *
     * @param url
     * @return
     */
    private String autoDealUrl(String url) {
        /* 处理/users/{id:[0-9]+}形式接口 */
        int lastSplitSymbol = url.lastIndexOf("/");
        String lastUrl = url.substring(lastSplitSymbol);
        // 判断接口地址 最后一个/ 后是否为全部数字
        String numRegular = "^\\d+$";
        if (lastUrl.matches(numRegular)) {
            return url.substring(0, lastSplitSymbol) + "/{id:[0-9]+}";
        }
        return url;
    }

    /**
     * 触发越权行为记录
     * 触发后系统执行动作送短信或日志（暂时不考虑实现）
     *
     * @param warnLog
     * @param api
     * @param user
     * @param site
     * @throws GlobalException
     */
    private void dealWarnLog(SysLog warnLog, CoreApi api, CoreUser user, CmsSite site) throws GlobalException {
        if (warnLog == null) {
            return;
        }
        //当前请求触发越权行为
        if (SystemExceptionEnum.HAS_NOT_PERMISSION.getCode().equals(warnLog.getHttpStatusCode())) {
            SysLog log = new SysLog();
            MyBeanUtils.copyPropertiesContentNull(warnLog, log);
            log.setId(null);
            log.setLogCategory(SysLog.LOG_CATEGORY_WARN);
            log.setLogLevel(SysLog.LOG_LEVEL_HEIGHT);
            log.setLogType(SysLog.LOG_TYPE_WARN);
            String str = "用户【%s】所属组织【%s】于【%s】尝试请求站点【%s】资源：%s，发生了越权访问!";
            String orgName = user != null ? user.getOrgName() : "'";
            String siteName = site != null ? site.getName() : "'";
            String apiName = api != null ? api.getApiName() : "";
            String timeStr = MyDateUtils.formatDate(warnLog.getCreateTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
            log.setRemark(String.format(str, warnLog.getUsername(), orgName, timeStr, siteName, apiName));
            super.save(log);
        }
    }

    // 间隔时间
    private int interval = 1 * 60 * 1000 * 1; //  10分钟
    // 最后刷新时间
    private long refreshTime = System.currentTimeMillis();
}