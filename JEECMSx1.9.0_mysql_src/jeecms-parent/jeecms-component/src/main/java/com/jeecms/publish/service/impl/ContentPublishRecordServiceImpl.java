package com.jeecms.publish.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.vo.ContentPerfVo;
import com.jeecms.content.service.ContentService;
import com.jeecms.interact.service.UserCommentService;
import com.jeecms.publish.dao.ContentPublishRecordDao;
import com.jeecms.publish.domain.ContentPublishRecord;
import com.jeecms.publish.domain.StatisticsPublish;
import com.jeecms.publish.domain.StatisticsPublishDetails;
import com.jeecms.publish.domain.vo.*;
import com.jeecms.publish.service.ContentPublishRecordService;
import com.jeecms.publish.service.StatisticsPublishDetailsService;
import com.jeecms.publish.service.StatisticsPublishService;
import com.jeecms.statistics.domain.vo.SiteGeneralBaseVo;
import com.jeecms.statistics.domain.vo.SiteGeneralVo;
import com.jeecms.system.service.SysAccessRecordService;
import com.jeecms.util.SystemContextUtils;
import com.jeecms.wechat.service.WechatSendService;
import com.jeecms.weibo.service.WeiboArticlePushService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
 * service实现类
 *
 * @author: ljw
 * @date: 2020年6月4日 下午2:10:36
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentPublishRecordServiceImpl extends BaseServiceImpl<ContentPublishRecord, ContentPublishRecordDao, Integer>
        implements ContentPublishRecordService {

    @Autowired
    private ContentService contentService;
    @Autowired
    private StatisticsPublishService statisticsPublishService;
    @Autowired
    private WechatSendService wechatSendService;
    @Autowired
    private WeiboArticlePushService weiboArticlePushService;
    @Autowired
    private UserCommentService userCommentService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private StatisticsPublishDetailsService statisticsPublishDetailsService;
    @Autowired
    private SysAccessRecordService sysAccessRecordService;

    @Override
    public List<ContentPublishRecord> getList(Integer siteId, Integer orgId, Integer userId, Integer channelId,
                                              Date start, Date end) {
        return dao.getList(siteId, orgId, userId, channelId, start, end);
    }

    @Override
    public Long count(Integer siteId, Integer orgId, Integer userId, Integer channelId, Date start, Date end) {
        return dao.count(siteId, orgId, userId, channelId, start, end);
    }

    @Override
    public void savePublish(Content content) throws GlobalException {
        //判断是否存在内容发布记录存在则不新增
        ContentPublishRecord record = dao.findByContentId(content.getId());
        if (record != null) {
            record.setPublishTime(content.getReleaseTime());
            super.update(record);
        } else {
            CoreUser user = SystemContextUtils.getCoreUser();
            if (user != null) {
                ContentPublishRecord contentPublishRecord = new ContentPublishRecord(content);
                contentPublishRecord.setPublishTime(content.getReleaseTime());
                //得到当前登录人
                contentPublishRecord.setUserId(user.getId());
                contentPublishRecord.setOrgId(user.getOrgId());
                super.save(contentPublishRecord);
            }
        }
    }

    @Override
    public SiteGeneralVo siteInfo(Integer siteId) throws GlobalException {
        Date date = new Date();
        //得到昨日统计全部的数据
        List<StatisticsPublish> publishList = statisticsPublishService.getList(siteId, null, MyDateUtils.getSpecficDateStart(date, -1),
                MyDateUtils.getSpecficDateEnd(date, -1));
        SiteGeneralVo vo = new SiteGeneralVo();
        vo.setContentPublish(content(publishList, date, siteId));
        vo.setWechatPublish(wechat(publishList, date, siteId));
        vo.setWeiboPublish(weibo(publishList, date, siteId));
        vo.setAddComment(comment(publishList, date, siteId));
        vo.setAddChannel(channel(publishList, date, siteId));
        return vo;
    }

    @Override
    public FlowContributionVo views(Integer siteId) throws GlobalException {
        FlowContributionVo vo = new FlowContributionVo();
        Date date = new Date();
        int channelSum  = 0;
        int contentSum  = 0;
        List<ContentFlowVo> contentFlowVos = sysAccessRecordService.flowContribution(siteId, date, date);
        if (!contentFlowVos.isEmpty()) {
            //今日总浏览量
            vo.setSum(contentFlowVos.size());
            //首页
            long index = contentFlowVos.stream().filter(x -> PAGE_TYPE_INDEX.equals(x.getPageType())).count();
            vo.setIndex((int) index);
            //栏目页
            List<ContentFlowVo> channel = contentFlowVos.stream().filter(x -> PAGE_TYPE_CHANNEL.equals(x.getPageType()))
                    .collect(Collectors.toList());
            if (!channel.isEmpty()) {
                List<PublishVo> list = new ArrayList<>(16);
                //聚合栏目
                Map<Integer, List<ContentFlowVo>> map = channel.stream()
                        .collect(Collectors.groupingBy(ContentFlowVo::getChannelId));
                for (Integer domain : map.keySet()) {
                    Channel path = channelService.findById(domain);
                    if (path != null) {
                        PublishVo vo1 = new PublishVo();
                        vo1.setName(path.getName());
                        vo1.setValue(map.get(domain).size());
                        list.add(vo1);
                    }
                }
                //总数
                channelSum =  list.stream().mapToInt(PublishVo::getValue).sum();
                vo.setChannel(channelSum);
                vo.setChannelList(addVo(list, channelSum));
            }
            //内容页
            List<ContentFlowVo> content = contentFlowVos.stream().filter(x -> PAGE_TYPE_CONTENT.equals(x.getPageType()))
                    .collect(Collectors.toList());
            if (!content.isEmpty()) {
                List<PublishVo> list = new ArrayList<>(16);
                //聚合栏目
                Map<Integer, List<ContentFlowVo>> map = content.stream()
                        .collect(Collectors.groupingBy(ContentFlowVo::getContentId));
                for (Integer id : map.keySet()) {
                    Content content1 = contentService.findById(id);
                    if (content1 != null) {
                        PublishVo vo1 = new PublishVo();
                        vo1.setName(content1.getTitle());
                        vo1.setValue(map.get(id).size());
                        list.add(vo1);
                    }
                }
                //总数
                contentSum =  list.stream().mapToInt(PublishVo::getValue).sum();
                vo.setContent(contentSum);
                vo.setContentList(addVo(list, contentSum));
            }
            //其他页
            vo.setOther((int) (contentFlowVos.size() - index - channelSum - contentSum));
        }
        return vo;
    }

    /**
     * 内容发布
     **/
    private SiteGeneralBaseVo content(List<StatisticsPublish> publishList, Date date, Integer siteId) {
        //内容发布数
        SiteGeneralBaseVo content = new SiteGeneralBaseVo();
        //今日发布数
        content.setToday(count(siteId, null, null, null, MyDateUtils.getStartDate(date), date).intValue());
        //昨日发布数
        content.setYesterday(0);
        if (!publishList.isEmpty()) {
            Optional<StatisticsPublish> obj = publishList.stream().filter(x -> x.getTypes()
                    .equals(STATISTICS_TYPE_CONTENT)).findFirst();
            obj.ifPresent(a -> content.setYesterday(a.getNumbers() != null ? a.getNumbers() : 0));
        }
        //历史最高
        content.setHigh(statisticsPublishService.countHigh(STATISTICS_TYPE_CONTENT, siteId));
        //累计
        content.setSum(publish(siteId,STATISTICS_TYPE_CONTENT,null,null).getSum());
        return content;
    }

    /**
     * 微信发布
     **/
    private SiteGeneralBaseVo wechat(List<StatisticsPublish> publishList, Date date, Integer siteId) {
        //微信发布数
        SiteGeneralBaseVo wechat = new SiteGeneralBaseVo();
        //今日微信发布数
        Map<String, String[]> params = new HashMap<>(2);
        String start = MyDateUtils.formatDate(MyDateUtils.getStartDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        String end = MyDateUtils.formatDate(MyDateUtils.getFinallyDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        params.put("GTE_createTime_Timestamp", new String[]{start});
        params.put("LTE_createTime_Timestamp", new String[]{end});
        params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        wechat.setToday((int) wechatSendService.count(params));
        wechat.setYesterday(0);
        //昨日发布数
        if (!publishList.isEmpty()) {
            Optional<StatisticsPublish> obj = publishList.stream().filter(x -> x.getTypes()
                    .equals(STATISTICS_TYPE_WECHAT)).findFirst();
            obj.ifPresent(a ->
                    wechat.setYesterday(a.getNumbers() != null ?a.getNumbers():0));
        }
        //历史最高
        wechat.setHigh(statisticsPublishService.countHigh(STATISTICS_TYPE_WECHAT, siteId));
        //累计
        Map<String, String[]> sumMap = new HashMap<>(1);
        sumMap.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        wechat.setSum((int) wechatSendService.count(sumMap));
        return wechat;
    }

    /**
     * 微博发布
     **/
    private SiteGeneralBaseVo weibo(List<StatisticsPublish> publishList, Date date, Integer siteId) {
        //微博发布数
        SiteGeneralBaseVo weibo = new SiteGeneralBaseVo();
        //今日微博发布数
        Map<String, String[]> params = new HashMap<>(2);
        String start = MyDateUtils.formatDate(MyDateUtils.getStartDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        String end = MyDateUtils.formatDate(MyDateUtils.getFinallyDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        params.put("GTE_createTime_Timestamp", new String[]{start});
        params.put("LTE_createTime_Timestamp", new String[]{end});
        params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        weibo.setToday((int) weiboArticlePushService.count(params));
        //昨日发布数
        weibo.setYesterday(0);
        if (!publishList.isEmpty()) {
            Optional<StatisticsPublish> obj = publishList.stream().filter(x -> x.getTypes()
                    .equals(STATISTICS_TYPE_WEIBO)).findFirst();
            obj.ifPresent(a ->
                    weibo.setYesterday(a.getNumbers() != null ? a.getNumbers():0));
        }
        //历史最高
        weibo.setHigh(statisticsPublishService.countHigh(STATISTICS_TYPE_WEIBO, siteId));
        //累计
        Map<String, String[]> sumMap = new HashMap<>(1);
        sumMap.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        weibo.setSum((int) weiboArticlePushService.count(sumMap));
        return weibo;
    }

    /**
     * 新增评论
     **/
    private SiteGeneralBaseVo comment(List<StatisticsPublish> publishList, Date date, Integer siteId) {
        //新增评论数
        SiteGeneralBaseVo comment = new SiteGeneralBaseVo();
        //今日新增评论数
        comment.setToday((int) userCommentService.getCount(MyDateUtils.getStartDate(date), date, siteId, null));
        //昨日新增评论
        comment.setYesterday(0);
        if (!publishList.isEmpty()) {
            Optional<StatisticsPublish> obj = publishList.stream().filter(x -> x.getTypes()
                    .equals(STATISTICS_TYPE_COMMENT)).findFirst();
            obj.ifPresent(a ->
                    comment.setYesterday(a.getNumbers() != null ? a.getNumbers():0));
        }
        //历史最高
        comment.setHigh(statisticsPublishService.countHigh(STATISTICS_TYPE_COMMENT,siteId));
        //累计
        comment.setSum((int) userCommentService.getCount(null, null, siteId, null));
        return comment;
    }

    /**
     * 新建栏目数
     **/
    private SiteGeneralBaseVo channel(List<StatisticsPublish> publishList, Date date, Integer siteId) {
        //新建栏目数
        SiteGeneralBaseVo channel = new SiteGeneralBaseVo();
        //今日新建栏目数
        Map<String, String[]> params = new HashMap<>(2);
        String start = MyDateUtils.formatDate(MyDateUtils.getStartDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        String end = MyDateUtils.formatDate(MyDateUtils.getFinallyDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        params.put("GTE_createTime_Timestamp", new String[]{start});
        params.put("LTE_createTime_Timestamp", new String[]{end});
        params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        channel.setToday((int) channelService.count(params));
        //昨日新建栏目数
        channel.setYesterday(0);
        if (!publishList.isEmpty()) {
            Optional<StatisticsPublish> obj = publishList.stream().filter(x -> x.getTypes()
                    .equals(STATISTICS_TYPE_CHANNEL)).findFirst();
            obj.ifPresent(a ->
                    channel.setYesterday(a.getNumbers() != null ? a.getNumbers():0));
        }
        //历史最高
        channel.setHigh(statisticsPublishService.countHigh(STATISTICS_TYPE_CHANNEL,siteId));
        //累计
        Map<String, String[]> hashMap = new HashMap<>(1);
        hashMap.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        channel.setSum((int) channelService.count(hashMap));
        return channel;
    }

    @Override
    public PublishSumVo publish(Integer siteId, Integer publishType,
                                Date startDate, Date endDate) {
        PublishSumVo todayVo = new PublishSumVo(0);
        PublishSumVo object = new PublishSumVo(0);
        Date today = new Date();
        //如果开始时间不为空
        //如果是今天，实时查询
        List<ContentPublishRecord> records = getList(siteId, null, null, null,
                today, today);
        todayVo.setSum(records.size());
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            //发布栏目类型
            if (publishType.equals(PUBLISH_CHANNEL_TYPE)) {
                List<PublishVo> list = nowPublishChannel(records);
                todayVo.setPublishs(addVo(list, records.size()));
            } else if (publishType.equals(PUBLISH_USER_TYPE)) {
                List<PublishVo> list = nowPublishUser(records);
                todayVo.setPublishs(addVo(list, records.size()));
            } else if (publishType.equals(PUBLISH_ORG_TYPE)) {
                List<PublishVo> list = nowPublishOrg(records);
                todayVo.setPublishs(addVo(list, records.size()));
            } else if (publishType.equals(PUBLISH_SITE_TYPE)) {
                List<PublishVo> list = nowPublishSite(records);
                todayVo.setPublishs(addVo(list, records.size()));
            }
            return todayVo;
        } else {
            //否则查询累计表
            List<StatisticsPublishDetails> publishDetails = statisticsPublishDetailsService.getList(publishType,
                    siteId, startDate, endDate);
            if (publishType.equals(PUBLISH_CHANNEL_TYPE)) {
                if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                    todayVo.setPublishs(nowPublishChannel(records));
                }
                object = publishChannel(object, publishDetails);
            } else if (publishType.equals(PUBLISH_USER_TYPE)) {
                if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                    todayVo.setPublishs(nowPublishUser(records));
                }
                object = publishUser(object, publishDetails);
            } else if (publishType.equals(PUBLISH_ORG_TYPE)) {
                if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                    todayVo.setPublishs(nowPublishOrg(records));
                }
                object = publishOrg(object, publishDetails);
            } else if (publishType.equals(PUBLISH_SITE_TYPE)) {
                if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                    todayVo.setPublishs(nowPublishSite(records));
                }
                object = publishSite(object, publishDetails);
            }
        }
        if (endDate != null && !DateUtil.isSameDay(endDate, today)) {
            return object;
        }
        //加上今天的数据
        return addToday(todayVo, object);
    }

    /**加上今天的数据**/
    private PublishSumVo addToday(PublishSumVo todayVo,  PublishSumVo sumVo) {
        List<PublishVo> list = new ArrayList<>(16);
        List<PublishVo> lists = new ArrayList<>(16);
        PublishSumVo sum = new PublishSumVo(0);
        sum.setSum(todayVo.getSum() + sumVo.getSum());
        if (todayVo.getPublishs() != null) {
            list.addAll(todayVo.getPublishs());
        }
        if (sumVo.getPublishs() != null) {
            list.addAll(sumVo.getPublishs());
        }
        Map<String, List<PublishVo>> collect = list.stream().collect(Collectors.groupingBy(PublishVo::getName));
        for (Map.Entry<String, List<PublishVo>> entry : collect.entrySet()) {
            PublishVo vo = new PublishVo();
            vo.setName(entry.getKey());
            vo.setValue(entry.getValue().stream().mapToInt(PublishVo::getValue).sum());
            lists.add(vo);
        }
        sum.setPublishs(addVo(lists, lists.stream().mapToInt(PublishVo::getValue).sum()));
        return sum;
    }

    /**
     * 饼状与柱状图
     * 实时内容发布按栏目
     **/
    protected List<PublishVo> nowPublishChannel(List<ContentPublishRecord> records) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!records.isEmpty()) {
            //分组
            Map<Integer, List<ContentPublishRecord>> map = records.stream()
                    .collect(Collectors.groupingBy(ContentPublishRecord::getChannelId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<ContentPublishRecord>> entry : map.entrySet()) {
                List<ContentPublishRecord> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getChannel().getName());
                vo.setValue(value.size());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed())
                    .collect(Collectors.toList());
        }
        return list;
    }

    /**栏目，查询统计表**/
    protected PublishSumVo publishChannel(PublishSumVo object,
                                             List<StatisticsPublishDetails> details) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!details.isEmpty()) {
            long sum = details.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum();
            //分组
            Map<Integer, List<StatisticsPublishDetails>> map = details.stream()
                    .collect(Collectors.groupingBy(StatisticsPublishDetails::getChannelId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<StatisticsPublishDetails>> entry : map.entrySet()) {
                List<StatisticsPublishDetails> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getChannel().getName());
                vo.setValue((int) value.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
            object.setSum((int) sum);
            object.setPublishs(list);
        }
        return object;
    }

    /**
     * 饼状与柱状图
     * 实时内容发布按用户
     **/
    protected List<PublishVo> nowPublishUser(List<ContentPublishRecord> records) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!records.isEmpty()) {
            //分组
            Map<Integer, List<ContentPublishRecord>> map = records.stream()
                    .collect(Collectors.groupingBy(ContentPublishRecord::getUserId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<ContentPublishRecord>> entry : map.entrySet()) {
                List<ContentPublishRecord> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getUser().getUsername());
                vo.setValue(value.size());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
        }
        return list;
    }

    /**用户，查询统计表**/
    protected PublishSumVo publishUser(PublishSumVo object,
                                     List<StatisticsPublishDetails> details) {
        List<PublishVo> list = new ArrayList<>(16);

        if (!details.isEmpty()) {
            //过滤为空的数据
            long sum = details.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum();
            //分组
            Map<Integer, List<StatisticsPublishDetails>> map = details.stream()
                    .filter(x -> x.getUserId() != null)
                    .collect(Collectors.groupingBy(StatisticsPublishDetails::getUserId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<StatisticsPublishDetails>> entry : map.entrySet()) {
                List<StatisticsPublishDetails> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getUser().getUsername());
                vo.setValue((int) value.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
            object.setSum((int) sum);
            object.setPublishs(list);
        }
        return object;
    }

    /**
     * 饼状与柱状图
     * 实时内容发布按组织
     **/
    protected List<PublishVo> nowPublishOrg(List<ContentPublishRecord> records) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!records.isEmpty()) {
            //分组
            Map<Integer, List<ContentPublishRecord>> map = records.stream()
                    .collect(Collectors.groupingBy(ContentPublishRecord::getOrgId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<ContentPublishRecord>> entry : map.entrySet()) {
                List<ContentPublishRecord> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getOrg().getName());
                vo.setValue(value.size());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
        }
        return list;
    }

    /**用户，查询统计表**/
    protected PublishSumVo publishOrg(PublishSumVo object, List<StatisticsPublishDetails> details) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!details.isEmpty()) {
            long sum = details.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum();
            //分组
            Map<Integer, List<StatisticsPublishDetails>> map = details.stream()
                    .filter(x -> x.getOrgId() != null)
                    .collect(Collectors.groupingBy(StatisticsPublishDetails::getOrgId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<StatisticsPublishDetails>> entry : map.entrySet()) {
                List<StatisticsPublishDetails> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getOrg().getName());
                vo.setValue((int) value.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
            object.setSum((int) sum);
            object.setPublishs(list);
        }
        return object;
    }

    /**
     * 饼状与柱状图
     * 实时内容发布按站点
     **/
    protected List<PublishVo> nowPublishSite(List<ContentPublishRecord> records) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!records.isEmpty()) {
            //分组
            Map<Integer, List<ContentPublishRecord>> map = records.stream()
                    .collect(Collectors.groupingBy(ContentPublishRecord::getSiteId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<ContentPublishRecord>> entry : map.entrySet()) {
                List<ContentPublishRecord> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getSite().getName());
                vo.setValue(value.size());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
        }
        return list;
    }

    /**用户，查询统计表**/
    protected PublishSumVo publishSite(PublishSumVo object,
                                     List<StatisticsPublishDetails> details) {
        List<PublishVo> list = new ArrayList<>(16);
        if (!details.isEmpty()) {
            long sum = details.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum();
            //分组
            Map<Integer, List<StatisticsPublishDetails>> map = details.stream()
                    .collect(Collectors.groupingBy(StatisticsPublishDetails::getSiteId,
                            Collectors.toList()));
            //组合
            for (Map.Entry<Integer, List<StatisticsPublishDetails>> entry : map.entrySet()) {
                List<StatisticsPublishDetails> value = entry.getValue();
                PublishVo vo = new PublishVo();
                vo.setName(value.get(0).getSite().getName());
                vo.setValue((int) value.stream().mapToLong(StatisticsPublishDetails::getNumbers).sum());
                list.add(vo);
            }
            //排序
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
            object.setSum((int) sum);
            object.setPublishs(list);
        }
        return object;
    }

    /**新增其他**/
    protected List<PublishVo> addVo(List<PublishVo> list, Integer all) {
        int size = 10;
        //如果分组大于10，其他的放到其他栏目
        if (list.size() > size) {
            list = list.stream().sorted(Comparator.comparing(PublishVo::getValue).reversed()).collect(Collectors.toList());
            int sum = list.stream().limit(size).mapToInt(PublishVo::getValue).sum();
            //排序
            list = list.stream().limit(size).sorted(Comparator.comparing(PublishVo::getValue).reversed())
                    .collect(Collectors.toList());
            //其他栏目数据
            PublishVo vo = new PublishVo();
            vo.setName("其他");
            vo.setValue(all - sum);
            list.add(vo);
        }
        return list;
    }

    @Override
    public JSONObject publishData(Integer siteId, Integer publishType, Boolean sort, String key,
                                  Date startDate, Date endDate, Pageable pageable) {
        JSONObject obj = new JSONObject();
        Date today = new Date();
        //如果开始时间不为空
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            //如果是今天，实时查询
            List<PublishPageVo> list = dao.publishDataList(siteId, publishType, sort, key, startDate, today);
            Page<PublishPageVo> publishPageVos = dao.publishData(siteId, publishType, sort, key, startDate, endDate, pageable);
            obj.put("sum", list.stream().mapToLong(PublishPageVo::getValue).sum());
            for (PublishPageVo vo : publishPageVos.getContent()) {
                vo.setRealName(StringUtils.isNotBlank(vo.getRealName()) ? vo.getRealName() : "--");
            }
            obj.put("page", publishPageVos);
        } else {
            List<PublishPageVo> todayList = new ArrayList<>(16);
            if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                //得到今日数据
                todayList = dao.publishDataList(siteId, publishType, sort, key, today, today);
            }
            //否则查询累计表 + 今天的数据
            //得到累计表
            List<PublishPageVo> list = statisticsPublishDetailsService.publishData(siteId,
                    publishType, sort, key, startDate, endDate);
            todayList.addAll(list);
            //得到总数
            Long sums = todayList.stream().mapToLong(PublishPageVo::getValue).sum();
            //聚合
            Map<String, List<PublishPageVo>> collect = todayList.stream().collect(Collectors.groupingBy(PublishPageVo::getName));
            List<PublishPageVo> pageVoList = new ArrayList<>(16);
            for (Map.Entry<String, List<PublishPageVo>> entry : collect.entrySet()) {
                List<PublishPageVo> value = entry.getValue();
                PublishPageVo vo = new PublishPageVo();
                vo.setName(entry.getKey());
                String realName = entry.getValue().get(0).getRealName();
                vo.setRealName(StringUtils.isNotBlank(realName) ? realName : "--");
                vo.setValue(value.stream().mapToLong(PublishPageVo::getValue).sum());
                pageVoList.add(vo);
            }
            int sum = pageVoList.size();
            //排序
            if(sort != null && !sort) {
                pageVoList = pageVoList.stream().sorted(Comparator.comparing(PublishPageVo::getValue))
                        .collect(Collectors.toList());
            } else {
                pageVoList = pageVoList.stream().sorted(Comparator.comparing(PublishPageVo::getValue).reversed())
                        .collect(Collectors.toList());
            }
            pageVoList = pageVoList.stream()
                    .skip(pageable.getPageSize() * (pageable.getPageNumber()))
                    .limit(pageable.getPageSize()).collect(Collectors.toList());
            PageImpl<PublishPageVo> publishPageVos = new PageImpl<>(pageVoList, pageable, sum);
            obj.put("sum", sums);
            obj.put("page", publishPageVos);
        }
        return obj;
    }

    @Override
    public Workbook exportData(Integer publishType, Boolean sort, String keyString, Date startDate, Date endDate) {
        String title = "表格数据";
        String sheetName = "表格信息";
        Date today = new Date();
        List<PublishPageVo> pageVoList = new ArrayList<>(16);
        List<PublishPageVo> sumList = new ArrayList<>(16);
        //如果开始时间不为空
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            //如果是今天，实时查询
            sumList = dao.publishDataList(null, publishType, sort, keyString, today, today);
        } else {
            if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                //得到今日数据
                pageVoList = dao.publishDataList(null, publishType, sort, keyString, today, today);
            }
            //得到累计表
            List<PublishPageVo> listSum = statisticsPublishDetailsService.publishData(null, publishType, sort, keyString, startDate, endDate);
            pageVoList.addAll(listSum);
            //聚合
            Map<String, List<PublishPageVo>> collect = pageVoList.stream().collect(Collectors.groupingBy(PublishPageVo::getName));
            for (Map.Entry<String, List<PublishPageVo>> entry : collect.entrySet()) {
                List<PublishPageVo> value = entry.getValue();
                PublishPageVo vo = new PublishPageVo();
                vo.setName(entry.getKey());
                String realName = entry.getValue().get(0).getRealName();
                vo.setRealName(StringUtils.isNotBlank(realName) ? realName : "--");
                vo.setValue(value.stream().mapToLong(PublishPageVo::getValue).sum());
                sumList.add(vo);
            }
        }
        //用户特殊处理
        if (publishType.equals(PUBLISH_USER_TYPE)) {
            List<ExcelUserVo> mapList = new ArrayList<>(16);
            for (PublishPageVo publishPageVo : sumList) {
                ExcelUserVo vo = new ExcelUserVo();
                vo.setName(publishPageVo.getName());
                vo.setRealName(StringUtils.isNotBlank(publishPageVo.getRealName()) ? publishPageVo.getRealName() : "--");
                vo.setValue(publishPageVo.getValue());
                mapList.add(vo);
            }
            mapList = mapList.stream().sorted(Comparator.comparing(ExcelUserVo::getValue).reversed()).collect(Collectors.toList());
            return ExcelExportUtil.exportExcel(new ExportParams(title, sheetName), ExcelUserVo.class, mapList);
        }
        sumList = sumList.stream().sorted(Comparator.comparing(PublishPageVo::getValue).reversed()).collect(Collectors.toList());
        return ExcelExportUtil.exportExcel(new ExportParams(title, sheetName), PublishPageVo.class, sumList);
    }

    @Override
    public List<ContentPerfVo> getList(Date start, Date end, List<Integer> channels, List<Integer> users, List<Integer> orgs, List<Integer> sites) {
        return dao.getList(start, end, channels, users, orgs, sites);
    }

}
