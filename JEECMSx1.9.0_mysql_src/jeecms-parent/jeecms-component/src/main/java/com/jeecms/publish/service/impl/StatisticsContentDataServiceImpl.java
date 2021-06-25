/**
*@Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.MathUtil;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentExt;
import com.jeecms.content.service.ContentService;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.interact.service.UserCommentService;
import com.jeecms.publish.dao.StatisticsContentDataDao;
import com.jeecms.publish.domain.StatisticsContentData;
import com.jeecms.publish.domain.vo.*;
import com.jeecms.publish.service.ContentLikeRecordService;
import com.jeecms.publish.service.StatisticsContentDataService;
import com.jeecms.system.domain.SysAccessRecord;
import com.jeecms.system.domain.vo.MassScoreVo;
import com.jeecms.system.service.SysAccessRecordService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
* @author ljw
* @version 1.0
* @date 2020-06-16
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class StatisticsContentDataServiceImpl extends BaseServiceImpl<StatisticsContentData, StatisticsContentDataDao, Integer>
        implements StatisticsContentDataService {

    @Autowired
    private SysAccessRecordService sysAccessRecordService;
    @Autowired
    private ContentLikeRecordService contentLikeRecordService;
    @Autowired
    private UserCommentService userCommentService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private ContentService contentService;

    @Override
    public Page<DataVo> getPage(Boolean type, Integer siteId, Date startDate,
                                Date endDate, Integer sortType, Boolean sort, Pageable pageable) {
        //得到今日访问数据
        Date today = new Date();
        //如果开始时间不为空
        List<DataVo> dataList = new ArrayList<>(16);
        List<SysAccessRecord> records = sysAccessRecordService.findByDate(MyDateUtils.getStartDate(today),
                today, siteId, null);
        //得到内容数据
        records = records.stream()
                .filter(x -> PAGE_TYPE_CONTENT.equals(x.getPageType()))
                .collect(Collectors.toList());
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            //如果是今天，实时查询
            if (type) {
                dataList = channelTime(records, today);
            } else {
                dataList = contentTime(records, today);
            }
        } else {
            //否则查询累计表
            if (type) {
                //查询栏目
                List<DataVo> pageList = dao.getPage(STATISTICS_TYPE_1, siteId,
                        startDate != null ? MyDateUtils.getStartDate(startDate) : null,
                        endDate != null ? MyDateUtils.getFinallyDate(endDate) : null);
                if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                    //需要加上今日数据
                    dataList = channelTime(records, today);
                }
                dataList = addToday(true, dataList, pageList);
            } else {
                //查询内容
                List<DataVo> pageList = dao.getPage(STATISTICS_TYPE_2, siteId,
                        startDate != null ? MyDateUtils.getStartDate(startDate) : null,
                        endDate != null ? MyDateUtils.getFinallyDate(endDate) : null);
                if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                    //需要加上今日数据
                    dataList = contentTime(records, today);
                }
                dataList = addToday(false, dataList, pageList);
                List<Integer> collect = dataList.stream().map(DataVo::getId).collect(Collectors.toList());
                List<Content> allById = contentService.findAllById(collect);
                Map<Integer, String> collect1 = allById.stream().collect(
                        Collectors.toMap(Content::getId, Content::getPreviewUrl));
                for (DataVo dataVo : dataList) {
                    dataVo.setUrl(collect1.getOrDefault(dataVo.getId(), ""));
                }
            }
        }
        //排序
        dataList = orderType(dataList, sortType, sort);
        int sum = dataList.size();
        dataList = dataList.stream()
                .skip(pageable.getPageSize() * (pageable.getPageNumber()))
                .limit(pageable.getPageSize()).collect(Collectors.toList());
        return new PageImpl<>(dataList, pageable, sum);
    }

    /**
     * 累计数据加上今日的数据
     **/
    protected List<DataVo> addToday(boolean type, List<DataVo> today, List<DataVo> day) {
        List<DataVo> sum = new ArrayList<>(16);
        today.addAll(day);
        Map<Integer, List<DataVo>> collect;
        //栏目
        if (type) {
            collect = today.stream()
                    .collect(Collectors.groupingBy(DataVo::getChannelId));
        } else {
            //内容
            collect = today.stream()
                    .collect(Collectors.groupingBy(DataVo::getContentId));
        }
        for (Map.Entry<Integer, List<DataVo>> entry : collect.entrySet()) {
            DataVo vo = new DataVo();
            vo.setId(entry.getKey());
            vo.setPublishTime(entry.getValue().get(0).getPublishTime());
            vo.setName(entry.getValue().get(0).getName());
            vo.setReadCount(entry.getValue().stream().mapToInt(DataVo::getReadCount).sum());
            vo.setPeopleCount(entry.getValue().stream().mapToInt(DataVo::getPeopleCount).sum());
            vo.setLikeCount(entry.getValue().stream().mapToInt(DataVo::getLikeCount).sum());
            vo.setCommentCount(entry.getValue().stream().mapToInt(DataVo::getCommentCount).sum());
            sum.add(vo);
        }
        return sum;
    }

    /**排序**/
    private List<DataVo> orderType(List<DataVo> today, Integer type, Boolean sort) {
        switch (type) {
            case SORT_TYPE_0:
                if (!sort) {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getPublishTime).reversed())
                            .collect(Collectors.toList());
                    return today;
                } else {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getPublishTime))
                            .collect(Collectors.toList());
                    return today;
                }
            case SORT_TYPE_1:
                if (!sort) {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getReadCount).reversed())
                            .collect(Collectors.toList());
                    return today;
                } else {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getReadCount))
                            .collect(Collectors.toList());
                    return today;
                }
            case SORT_TYPE_2:
                if (!sort) {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getPeopleCount).reversed())
                            .collect(Collectors.toList());
                    return today;
                } else {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getPeopleCount))
                            .collect(Collectors.toList());
                    return today;
                }
            case SORT_TYPE_3:
                if (!sort) {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getLikeCount).reversed())
                            .collect(Collectors.toList());
                    return today;
                } else {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getLikeCount))
                            .collect(Collectors.toList());
                    return today;
                }
            case SORT_TYPE_4:
                if (!sort) {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getCommentCount).reversed())
                            .collect(Collectors.toList());
                    return today;
                } else {
                    today = today.stream().sorted(Comparator.comparing(DataVo::getCommentCount))
                            .collect(Collectors.toList());
                    return today;
                }
            default:
                today = today.stream().sorted(Comparator.comparing(DataVo::getReadCount).reversed())
                        .collect(Collectors.toList());
                return today;
        }
    }

    @Override
    public void collect(Date date) throws GlobalException {
        //得到昨日访问数据,只筛选内容页
        List<SysAccessRecord> records = sysAccessRecordService.typeList(PAGE_TYPE_CONTENT, date, date);
        if (!records.isEmpty()) {
            channel(records, date);
            content(records, date);
            //统计内容的浏览记录
            browse(records, date);
            //统计内容的浏览记录访问设备
            device(records, date);
            //统计内容的浏览记录访问地域
            province(records, date);
        }
    }

    /**统计类型-栏目**/
    private void channel(List<SysAccessRecord> records, Date date) throws GlobalException {
        List<StatisticsContentData> channelList = new ArrayList<>(16);
        //按照站点分组
        Map<Integer, List<SysAccessRecord>> collect = records.stream()
                .collect(Collectors.groupingBy(SysAccessRecord::getSiteId));
        //遍历
        for (Map.Entry<Integer, List<SysAccessRecord>> entry : collect.entrySet()) {
            //根据栏目ID分组
            Map<Integer, List<SysAccessRecord>> channel = entry.getValue().stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getChannelId));
            for (Map.Entry<Integer, List<SysAccessRecord>> entry1 : channel.entrySet()) {
                StatisticsContentData data = new StatisticsContentData();
                List<SysAccessRecord> people = entry1.getValue();
                data.setSiteId(entry.getKey());
                data.setChannelId(entry1.getKey());
                data.setReadCount(people.size());
                //阅读人数
                //访客数（UV）：当前站点下，
                //有user_id，以user_id分组，统计user_id数量，记为：C1；
                //无user_id，以cookie分组，统计cookie数量，记为：C2；
                //访客数（UV）= C1+C2。
                Integer c1 = people.stream().filter(x -> x.getLoginUserId() != null)
                        .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                Integer c2 = people.stream()
                        .filter(x -> x.getLoginUserId() == null)
                        .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                        .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                data.setPeopleCount(c1 + c2);
                data.setType(STATISTICS_TYPE_1);
                data.setStatisticsDay(date);
                channelList.add(data);
            }
        }
        //查询点赞数以及评论数
        //查询全部点赞数
        List<ContentLikeVo> like = contentLikeRecordService.count(true, MyDateUtils.getStartDate(date),
                MyDateUtils.getFinallyDate(date));
        Map<Integer, Long> likeMap = new HashMap<>(10);
        if (!like.isEmpty()) {
            likeMap = like.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        //查询全部评论数
        Map<Integer, Long> commentMap = new HashMap<>(10);
        List<ContentLikeVo> comment =  userCommentService.count(true, MyDateUtils.getStartDate(date),
                MyDateUtils.getFinallyDate(date));
        if (!comment.isEmpty()) {
            commentMap = comment.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        for (StatisticsContentData data : channelList) {
            //查询点赞
            data.setLikeCount(likeMap.get(data.getChannelId()) != null
                    ? Math.toIntExact(likeMap.get(data.getChannelId())) : 0);
            //查询评论
            data.setCommentCount(commentMap.get(data.getChannelId()) != null
                    ? Math.toIntExact(commentMap.get(data.getChannelId())) : 0);
        }
        super.saveAll(channelList);
        super.flush();
    }

    /**统计类型-内容**/
    private void content(List<SysAccessRecord> records, Date date) throws GlobalException {
        List<StatisticsContentData> contentList = new ArrayList<>(16);
        //按照站点分组
        Map<Integer, List<SysAccessRecord>> collect = records.stream()
                .collect(Collectors.groupingBy(SysAccessRecord::getSiteId));
        //遍历
        for (Map.Entry<Integer, List<SysAccessRecord>> entry : collect.entrySet()) {
            //根据内容ID分组
            Map<Integer, List<SysAccessRecord>> content = entry.getValue().stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getContentId));
            for (Map.Entry<Integer, List<SysAccessRecord>> entry1 : content.entrySet()) {
                StatisticsContentData data = new StatisticsContentData();
                List<SysAccessRecord> people = entry1.getValue();
                data.setSiteId(entry.getKey());
                data.setContentId(entry1.getKey());
                data.setReadCount(people.size());
                //阅读人数
                //访客数（UV）：当前站点下，
                //有user_id，以user_id分组，统计user_id数量，记为：C1；
                //无user_id，以cookie分组，统计cookie数量，记为：C2；
                //访客数（UV）= C1+C2。
                Integer c1 = people.stream().filter(x -> x.getLoginUserId() != null)
                        .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                Integer c2 = people.stream()
                        .filter(x -> x.getLoginUserId() == null)
                        .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                        .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                data.setPeopleCount(c1 + c2);
                data.setType(STATISTICS_TYPE_2);
                data.setStatisticsDay(date);
                contentList.add(data);
            }
        }
        //查询点赞数以及评论数
        //查询全部点赞数
        List<ContentLikeVo> like = contentLikeRecordService.count(false, MyDateUtils.getStartDate(date),
                MyDateUtils.getFinallyDate(date));
        Map<Integer, Long> likeMap = new HashMap<>(10);
        if (!like.isEmpty()) {
            likeMap = like.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        //查询全部评论数
        Map<Integer, Long> commentMap = new HashMap<>(10);
        List<ContentLikeVo> comment =  userCommentService.count(false, MyDateUtils.getStartDate(date),
                MyDateUtils.getFinallyDate(date));
        if (!comment.isEmpty()) {
            commentMap = comment.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        for (StatisticsContentData data : contentList) {
            //查询点赞
            data.setLikeCount(likeMap.get(data.getContentId()) != null
                    ? Math.toIntExact(likeMap.get(data.getContentId())) : 0);
            //查询评论
            data.setCommentCount(commentMap.get(data.getContentId()) != null
                    ? Math.toIntExact(commentMap.get(data.getContentId())) : 0);
        }
        super.saveAll(contentList);
        super.flush();
        //刷新内容的相关数据，包含总数，日，月，周数据；
        flushContent(contentList,date);
    }

    /**刷新内容的相关数据，包含总数，日，月，周数据；**/
    void flushContent(List<StatisticsContentData> contentList, Date date) throws GlobalException {
        if (!contentList.isEmpty()) {
            //集合转Map
            Map<Integer, StatisticsContentData> collect = contentList.stream()
                    .collect(Collectors.toMap(StatisticsContentData::getContentId, a -> a, (k1, k2) -> k1));
            //查询内容数据
            List<Content> contents = contentService.findAllById(collect.keySet());
            for (Content content : contents) {
                //得到总数
                DataSumVo sum = dao.countContent(content.getId(), null,null);
                content.setViews(sum != null ? content.getViews() + sum.getReadCount() : content.getViews());
                content.setComments(sum != null ? content.getComments() + sum.getCommentCount() : content.getComments());
                content.setUps(sum != null ? content.getUps() + sum.getLikeCount() : content.getUps());
                //得到月访问数
                DataSumVo month = dao.countContent(content.getId(), DateUtil.beginOfMonth(date),date);
                ContentExt ext = content.getContentExt();
                ext.setViewsMonth(month != null ? ext.getViewsMonth() + month.getReadCount() : ext.getViewsMonth());
                ext.setCommentsMonth(month != null ? ext.getCommentsMonth() + month.getCommentCount(): ext.getCommentsMonth());
                ext.setUpsMonth(month != null ? ext.getUpsMonth() + month.getLikeCount(): ext.getUpsMonth());
                //得到周访问数
                DataSumVo week = dao.countContent(content.getId(), DateUtil.beginOfWeek(date),date);
                ext.setViewsWeek(week != null ? ext.getViewsWeek() + week.getReadCount() : ext.getViewsWeek());
                ext.setCommentsWeek(week != null ? ext.getCommentsWeek() + week.getCommentCount() : ext.getCommentsWeek());
                ext.setUpsWeek(week != null ? ext.getUpsWeek() + week.getLikeCount() : ext.getUpsWeek());
                //得到日访问数
                StatisticsContentData statisticsContentData = collect.get(content.getId());
                ext.setViewsDay(statisticsContentData != null ? statisticsContentData.getReadCount() : 0);
                ext.setCommentsDay(statisticsContentData != null ? statisticsContentData.getCommentCount() : 0);
                ext.setUpsDay(statisticsContentData != null ? statisticsContentData.getLikeCount() : 0);
                content.setContentExt(ext);
            }
            contentService.batchUpdate(contents);
        }
    }

    /**统计类型-栏目**/
    private List<DataVo> channelTime(List<SysAccessRecord> records, Date date) {
        List<DataVo> dataList = new ArrayList<>(16);
            //根据栏目ID分组
            Map<Integer, List<SysAccessRecord>> channel = records.stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getChannelId));
            for (Map.Entry<Integer, List<SysAccessRecord>> entry1 : channel.entrySet()) {
                DataVo data = new DataVo();
                List<SysAccessRecord> people = entry1.getValue();
                data.setName(channelService.findById(entry1.getKey()).getName());
                data.setChannelId(entry1.getKey());
                data.setReadCount(people.size());
                //阅读人数
                //访客数（UV）：当前站点下，
                //有user_id，以user_id分组，统计user_id数量，记为：C1；
                //无user_id，以cookie分组，统计cookie数量，记为：C2；
                //访客数（UV）= C1+C2。
                Integer c1 = people.stream().filter(x -> x.getLoginUserId() != null)
                        .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                Integer c2 = people.stream()
                        .filter(x -> x.getLoginUserId() == null)
                        .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                        .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                data.setPeopleCount(c1 + c2);
                dataList.add(data);
            }
        //查询点赞数以及评论数
        //查询全部点赞数
        List<ContentLikeVo> like = contentLikeRecordService.count(true, MyDateUtils.getStartDate(date),
                date);
        Map<Integer, Long> likeMap = new HashMap<>(10);
        if (!like.isEmpty()) {
            likeMap = like.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        //查询全部评论数
        Map<Integer, Long> commentMap = new HashMap<>(10);
        List<ContentLikeVo> comment =  userCommentService.count(true, MyDateUtils.getStartDate(date),
                date);
        if (!comment.isEmpty()) {
            commentMap = comment.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        for (DataVo data : dataList) {
            //查询点赞
            data.setLikeCount(likeMap.get(data.getChannelId()) != null
                    ? Math.toIntExact(likeMap.get(data.getChannelId())) : 0);
            //查询评论
            data.setCommentCount(commentMap.get(data.getChannelId()) != null
                    ? Math.toIntExact(commentMap.get(data.getChannelId())) : 0);
        }
        return dataList;
    }

    /**统计类型-内容**/
    private List<DataVo> contentTime(List<SysAccessRecord> records, Date date) {
        List<DataVo> dataList = new ArrayList<>(16);
            //根据内容ID分组
            Map<Integer, List<SysAccessRecord>> content = records.stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getContentId));
            for (Map.Entry<Integer, List<SysAccessRecord>> entry1 : content.entrySet()) {
                DataVo data = new DataVo();
                List<SysAccessRecord> people = entry1.getValue();
                Content content1 = contentService.findById(entry1.getKey());
                data.setName(content1.getTitle());
                data.setId(entry1.getKey());
                data.setContentId(entry1.getKey());
                data.setPublishTime(content1.getReleaseTime());
                data.setUrl(content1.getPreviewUrl());
                data.setReadCount(people.size());
                //阅读人数
                //访客数（UV）：当前站点下，
                //有user_id，以user_id分组，统计user_id数量，记为：C1；
                //无user_id，以cookie分组，统计cookie数量，记为：C2；
                //访客数（UV）= C1+C2。
                Integer c1 = people.stream().filter(x -> x.getLoginUserId() != null)
                        .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                Integer c2 = people.stream()
                        .filter(x -> x.getLoginUserId() == null)
                        .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                        .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                data.setPeopleCount(c1 + c2);
                dataList.add(data);
            }
        //查询点赞数以及评论数
        //查询全部点赞数
        List<ContentLikeVo> like = contentLikeRecordService.count(false, MyDateUtils.getStartDate(date),
                date);
        Map<Integer, Long> likeMap = new HashMap<>(10);
        if (!like.isEmpty()) {
            likeMap = like.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        //查询全部评论数
        Map<Integer, Long> commentMap = new HashMap<>(10);
        List<ContentLikeVo> comment =  userCommentService.count(false, MyDateUtils.getStartDate(date),
                date);
        if (!comment.isEmpty()) {
            commentMap = comment.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        for (DataVo data : dataList) {
            //查询点赞
            data.setLikeCount(likeMap.get(data.getContentId()) != null
                    ? Math.toIntExact(likeMap.get(data.getContentId())) : 0);
            //查询评论
            data.setCommentCount(commentMap.get(data.getContentId()) != null
                    ? Math.toIntExact(commentMap.get(data.getContentId())) : 0);
        }
        return dataList;
    }

    @Override
    public DataSumVo data(Integer siteId, Date startDate, Date endDate, boolean flag) {
        Date today = new Date();
        DataSumVo todayVo = new DataSumVo(0, 0, 0, 0);
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            return todayVo(today, siteId, todayVo, flag);
        } else {
            DataSumVo todayVos = new DataSumVo(0, 0, 0, 0);
            if (endDate == null || DateUtil.isSameDay(endDate,today)) {
                todayVos = todayVo(today, siteId, todayVo, flag);
            }
            DataSumVo vo = dao.count(siteId, startDate, endDate, flag);
            //加上今日数据
            vo.setCommentCount(todayVos.getCommentCount() + ObjectUtil.defaultIfNull(vo.getCommentCount(), 0));
            vo.setReadCount(todayVos.getReadCount() + ObjectUtil.defaultIfNull(vo.getReadCount(), 0));
            vo.setPeopleCount(todayVos.getPeopleCount() + ObjectUtil.defaultIfNull(vo.getPeopleCount(), 0));
            vo.setLikeCount(todayVos.getLikeCount() + ObjectUtil.defaultIfNull(vo.getLikeCount(), 0));
            return vo;
        }
    }

    /**今日数据**/
    private DataSumVo todayVo(Date today, Integer siteId, DataSumVo todayVo, boolean flag) {
        LongAdder adder = new LongAdder();
        List<SysAccessRecord> records = sysAccessRecordService.findByDate(MyDateUtils.getStartDate(today),
                today, siteId, null);
        //得到内容数据
        records = records.stream()
                .filter(x -> PAGE_TYPE_CONTENT.equals(x.getPageType()))
                .collect(Collectors.toList());
        if (!records.isEmpty()) {
            if (flag) {
                //根据栏目ID分组
                Map<Integer, List<SysAccessRecord>> channel = records.stream()
                        .collect(Collectors.groupingBy(SysAccessRecord::getChannelId));
                for (Map.Entry<Integer, List<SysAccessRecord>> entry1 : channel.entrySet()) {
                    //阅读人数
                    //访客数（UV）：当前站点下，
                    //有user_id，以user_id分组，统计user_id数量，记为：C1；
                    //无user_id，以cookie分组，统计cookie数量，记为：C2；
                    //访客数（UV）= C1+C2。
                    Integer c1 = entry1.getValue().stream().filter(x -> x.getLoginUserId() != null)
                            .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                    Integer c2 = entry1.getValue().stream()
                            .filter(x -> x.getLoginUserId() == null)
                            .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                            .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                    adder.add(c1 + c2);
                }
                //查询全部点赞数
                List<ContentLikeVo> like = contentLikeRecordService.count(true, MyDateUtils.getStartDate(today),
                        today);
                long sumLike = 0L;
                if (!like.isEmpty()) {
                    sumLike = like.stream().mapToLong(ContentLikeVo::getValue).sum();
                }
                //查询全部评论数
                List<ContentLikeVo> comment = userCommentService.count(true, MyDateUtils.getStartDate(today),
                        today);
                long sumComment = 0L;
                if (!comment.isEmpty()) {
                    sumComment = comment.stream().mapToLong(ContentLikeVo::getValue).sum();
                }
                todayVo.setLikeCount((int) sumLike);
                todayVo.setCommentCount((int) sumComment);
            } else {
                //根据内容ID分组
                Map<Integer, List<SysAccessRecord>> content = records.stream()
                        .collect(Collectors.groupingBy(SysAccessRecord::getContentId));
                for (Map.Entry<Integer, List<SysAccessRecord>> entry1 : content.entrySet()) {
                    //阅读人数
                    //访客数（UV）：当前站点下，
                    //有user_id，以user_id分组，统计user_id数量，记为：C1；
                    //无user_id，以cookie分组，统计cookie数量，记为：C2；
                    //访客数（UV）= C1+C2。
                    Integer c1 = entry1.getValue().stream().filter(x -> x.getLoginUserId() != null)
                            .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                    Integer c2 = entry1.getValue().stream()
                            .filter(x -> x.getLoginUserId() == null)
                            .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                            .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                    adder.add(c1 + c2);
                }
            }
            todayVo.setReadCount(records.size());
            todayVo.setPeopleCount(adder.intValue());
            //查询全部点赞数
            List<ContentLikeVo> like = contentLikeRecordService.count(false, MyDateUtils.getStartDate(today),
                    today);
            long sumLike = 0L;
            if (!like.isEmpty()) {
                sumLike = like.stream().mapToLong(ContentLikeVo::getValue).sum();
            }
            //查询全部评论数
            List<ContentLikeVo> comment = userCommentService.count(false, MyDateUtils.getStartDate(today),
                    today);
            long sumComment = 0L;
            if (!comment.isEmpty()) {
                sumComment = comment.stream().mapToLong(ContentLikeVo::getValue).sum();
            }
            todayVo.setLikeCount((int) sumLike);
            todayVo.setCommentCount((int) sumComment);
        }
        return todayVo;
    }

    /**统计类型-浏览内容**/
    private void browse(List<SysAccessRecord> records, Date date) throws GlobalException {
        List<StatisticsContentData> browseList = new ArrayList<>(16);
        //按照站点分组
        Map<Integer, List<SysAccessRecord>> collect = records.stream()
                .collect(Collectors.groupingBy(SysAccessRecord::getSiteId));
        for (Map.Entry<Integer, List<SysAccessRecord>> integerListEntry : collect.entrySet()) {
            //按照内容ID分组
            Map<Integer, List<SysAccessRecord>> collect1 = integerListEntry.getValue().stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getContentId));
            //遍历
            for (Map.Entry<Integer, List<SysAccessRecord>> entry : collect1.entrySet()) {
                StatisticsContentData data = new StatisticsContentData();
                List<SysAccessRecord> recordList = entry.getValue();
                data.setSiteId(integerListEntry.getKey());
                data.setContentId(entry.getKey());
                data.setReadCount(recordList.size());
                //阅读人数
                //访客数（UV）：当前站点下，
                //有user_id，以user_id分组，统计user_id数量，记为：C1；
                //无user_id，以cookie分组，统计cookie数量，记为：C2；
                //访客数（UV）= C1+C2。
                Integer c1 = recordList.stream().filter(x -> x.getLoginUserId() != null)
                        .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                Integer c2 = recordList.stream()
                        .filter(x -> x.getLoginUserId() == null)
                        .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                        .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                data.setPeopleCount(c1 + c2);
                data.setType(STATISTICS_TYPE_3);
                data.setStatisticsDay(date);
                browseList.add(data);
            }
        }
        //查询点赞数以及评论数
        //查询全部点赞数
        List<ContentLikeVo> like = contentLikeRecordService.count(false, MyDateUtils.getStartDate(date),
                MyDateUtils.getFinallyDate(date));
        Map<Integer, Long> likeMap = new HashMap<>(10);
        if (!like.isEmpty()) {
            likeMap = like.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        //查询全部评论数
        Map<Integer, Long> commentMap = new HashMap<>(10);
        List<ContentLikeVo> comment =  userCommentService.count(false, MyDateUtils.getStartDate(date),
                MyDateUtils.getFinallyDate(date));
        if (!comment.isEmpty()) {
            commentMap = comment.stream().collect(
                    Collectors.toMap(ContentLikeVo::getKey, ContentLikeVo::getValue));
        }
        for (StatisticsContentData data : browseList) {
            //查询点赞
            data.setLikeCount(likeMap.get(data.getContentId()) != null
                    ? Math.toIntExact(likeMap.get(data.getContentId())) : 0);
            //查询评论
            data.setCommentCount(commentMap.get(data.getContentId()) != null
                    ? Math.toIntExact(commentMap.get(data.getContentId())) : 0);
        }
        super.saveAll(browseList);
        super.flush();
    }

    /**统计设备**/
    private void device(List<SysAccessRecord> records, Date date) throws GlobalException {
        List<StatisticsContentData> deviceList = new ArrayList<>(16);
        //按照站点分组
        Map<Integer, List<SysAccessRecord>> collect = records.stream()
                .collect(Collectors.groupingBy(SysAccessRecord::getSiteId));
        for (Map.Entry<Integer, List<SysAccessRecord>> integerListEntry : collect.entrySet()) {
            //按照内容ID分组
            Map<Integer, List<SysAccessRecord>> collect1 = integerListEntry.getValue().stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getContentId));
            //遍历
            for (Map.Entry<Integer, List<SysAccessRecord>> entry : collect1.entrySet()) {
                StatisticsContentData data = new StatisticsContentData();
                List<SysAccessRecord> recordList = entry.getValue();
                data.setSiteId(integerListEntry.getKey());
                data.setContentId(entry.getKey());
                //得到移动数据
                long count = recordList.stream().filter(x -> x.getAccessSourceClient() > 1).count();
                if (count != 0) {
                    StatisticsContentData data1 = new StatisticsContentData();
                    //阅读量
                    data1.setReadCount((int) count);
                    data1.setDevice(DEVICE_TYPE_2);
                    data1.setType(STATISTICS_TYPE_4);
                    data1.setStatisticsDay(date);
                    deviceList.add(data1);
                }
                data.setDevice(DEVICE_TYPE_1);
                //阅读量
                data.setReadCount((int) (recordList.size() - count));
                data.setType(STATISTICS_TYPE_4);
                data.setStatisticsDay(date);
                deviceList.add(data);
            }
        }
        super.saveAll(deviceList);
        super.flush();
    }

    /**统计地域**/
    private void province(List<SysAccessRecord> records, Date date) throws GlobalException {
        List<StatisticsContentData> proList = new ArrayList<>(16);
        //按照站点分组
        Map<Integer, List<SysAccessRecord>> collect = records.stream()
                .collect(Collectors.groupingBy(SysAccessRecord::getSiteId));
        for (Map.Entry<Integer, List<SysAccessRecord>> integerListEntry : collect.entrySet()) {
            //按照内容ID分组
            Map<Integer, List<SysAccessRecord>> collect1 = integerListEntry.getValue().stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getContentId));
            //遍历
            for (Map.Entry<Integer, List<SysAccessRecord>> entry : collect1.entrySet()) {
                List<SysAccessRecord> recordList = entry.getValue();
                //省份分组
                Map<String, List<SysAccessRecord>> collect2 = recordList.stream()
                        .collect(Collectors.groupingBy(SysAccessRecord::getAccessProvince));
                for (Map.Entry<String, List<SysAccessRecord>> stringListEntry : collect2.entrySet()) {
                    StatisticsContentData data = new StatisticsContentData();
                    List<SysAccessRecord> province = stringListEntry.getValue();
                    data.setSiteId(integerListEntry.getKey());
                    data.setContentId(entry.getKey());
                    data.setProvince(stringListEntry.getKey());
                    //阅读量
                    data.setReadCount(province.size());
                    Integer c1 = province.stream().filter(x -> x.getLoginUserId() != null)
                            .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                    Integer c2 = province.stream()
                            .filter(x -> x.getLoginUserId() == null)
                            .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                            .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                    //阅读人数
                    data.setPeopleCount(c1 + c2);
                    data.setType(STATISTICS_TYPE_5);
                    data.setStatisticsDay(date);
                    //查询全部评论数
                    List<UserComment> list = userCommentService.list(Collections.singletonList(entry.getKey()),
                            MyDateUtils.getStartDate(date),
                            MyDateUtils.getFinallyDate(date), stringListEntry.getKey());
                    data.setCommentCount(list != null ? list.size() :0);
                    proList.add(data);
                }
            }
        }
        super.saveAll(proList);
        super.flush();
    }

    @Override
    public ContentViewVo view(Integer siteId, Integer contentId, Date startDate, Date endDate)
            throws IllegalAccessException {
        Date today = new Date();
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            return todayVo(today, siteId, contentId);
        } else {
            ContentViewVo todayVo = new ContentViewVo(0,0,0,0,
                    0,0,0,0);
            //得到累计数据
            if(endDate == null || DateUtil.isSameDay(endDate, today)) {
                //得到今日数据
                todayVo = todayVo(today, siteId, contentId);
            }
            ContentViewVo vo = new ContentViewVo();
            //浏览记录
            List<StatisticsContentData> list1 = dao.getList(STATISTICS_TYPE_3, null, contentId, startDate, endDate);
            vo.setReadCount(list1.stream().mapToInt(StatisticsContentData::getReadCount).sum()
                    + todayVo.getReadCount());
            vo.setPeopleCount(list1.stream().mapToInt(StatisticsContentData::getPeopleCount).sum()
                    + todayVo.getPeopleCount());
            vo.setLikeCount(list1.stream().mapToInt(StatisticsContentData::getLikeCount).sum()
                    + todayVo.getLikeCount());
            vo.setCommentCount(list1.stream().mapToInt(StatisticsContentData::getCommentCount).sum()
                    + todayVo.getCommentCount());
            //计算机
            List<StatisticsContentData> list2 = dao.getList(STATISTICS_TYPE_4, DEVICE_TYPE_1, contentId, startDate, endDate);
            int pc = list2.stream().mapToInt(StatisticsContentData::getReadCount).sum();
            vo.setPc(pc + todayVo.getPc());
            int mobile = vo.getReadCount() - vo.getPc();
            vo.setMobile(mobile);
            BigDecimal div = MathUtil.div(new BigDecimal(mobile), new BigDecimal(vo.getReadCount()),
                    MathUtil.SCALE_LEN_COMMON);
            vo.setMobilePrecent(MathUtil.mul(BigDecimal.valueOf(100), div).intValue());
            if(!div.equals(BigDecimal.ZERO)) {
                BigDecimal sub = MathUtil.sub(BigDecimal.valueOf(1), div);
                vo.setPcPrecent(MathUtil.mul(BigDecimal.valueOf(100), sub).intValue());
            }
            //地图数据
            List<StatisticsContentData> list3 = dao.getList(STATISTICS_TYPE_5, null, contentId, startDate, endDate);
            List<JSONObject> map = new ArrayList<>(16);
            if (!list3.isEmpty()) {
                Map<String, List<StatisticsContentData>> collect = list3.stream()
                        .collect(Collectors.groupingBy(StatisticsContentData::getProvince));
                for (Map.Entry<String, List<StatisticsContentData>> stringListEntry : collect.entrySet()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", stringListEntry.getKey());
                    jsonObject.put("value", stringListEntry.getValue().stream()
                            .mapToInt(StatisticsContentData::getReadCount).sum());
                    map.add(jsonObject);
                }
            }
            if(todayVo.getMap() != null) {
                map.addAll(todayVo.getMap());
            }
            vo.setMap(map);
            return vo;
        }
    }

    /**今日数据**/
    private ContentViewVo todayVo(Date today, Integer siteId, Integer contentId) throws IllegalAccessException {
        ContentViewVo vo = new ContentViewVo(0,0,0,0,
                0,0,0,0);
        //如果是今天，实时查询
        List<SysAccessRecord> records = sysAccessRecordService.findByDate(MyDateUtils.getStartDate(today),
                today, siteId, contentId);
        if (!records.isEmpty()) {
            //阅读量
            vo.setReadCount(records.size());
            //阅读人数
            Integer c1 = records.stream().filter(x -> x.getLoginUserId() != null)
                    .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
            Integer c2 = records.stream()
                    .filter(x -> x.getLoginUserId() == null)
                    .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                    .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
            vo.setPeopleCount(c1 + c2);
            //评论数
            List<UserComment> list = userCommentService.list(Collections.singletonList(contentId), MyDateUtils.getStartDate(today),
                    today, null);
            vo.setCommentCount(list.size());
            //点赞数
            List<MassScoreVo> like = contentLikeRecordService.massCount(Collections.singletonList(contentId), MyDateUtils.getStartDate(today),
                    today);
            if (!like.isEmpty()) {
                vo.setLikeCount(like.get(0).getCounts().intValue());
            } else {
                vo.setLikeCount(0);
            }
            //得到移动数据
            long mobile = records.stream().filter(x -> x.getAccessSourceClient() > 1).count();
            long pc = records.size() - mobile;
            vo.setMobile((int) mobile);
            vo.setPc((int) pc);
            BigDecimal div = MathUtil.div(new BigDecimal(mobile), new BigDecimal(records.size()),
                    MathUtil.SCALE_LEN_COMMON);
            vo.setMobilePrecent(MathUtil.mul(BigDecimal.valueOf(100), div).intValue());
            if(!div.equals(BigDecimal.ZERO)) {
                BigDecimal sub = MathUtil.sub(BigDecimal.valueOf(1), div);
                vo.setPcPrecent(MathUtil.mul(BigDecimal.valueOf(100), sub).intValue());
            }
            Map<String, List<SysAccessRecord>> collect = records.stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getAccessProvince));
            List<JSONObject> map = new ArrayList<>(16);
            for (Map.Entry<String, List<SysAccessRecord>> stringListEntry : collect.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", stringListEntry.getKey());
                jsonObject.put("value", stringListEntry.getValue().size());
                map.add(jsonObject);
            }
            vo.setMap(map);
        }
        return vo;
    }


    @Override
    public List<ContentTableVo> table(Integer siteId, Integer contentId, Date startDate,
                                      Date endDate, Integer sortType, Boolean sort) {
        Date today = new Date();
        List<ContentTableVo> vos = new ArrayList<>(16);
        if (startDate != null && DateUtil.isSameDay(startDate, today)) {
            vos = todayTableVo(today, siteId, contentId);
        } else {
            List<ContentTableVo> todayVo = new ArrayList<>(16);
            if (endDate == null || DateUtil.isSameDay(endDate, today)) {
                //得到今日数据
                todayVo = todayTableVo(today, siteId, contentId);
            }
            List<StatisticsContentData> list = dao.getList(STATISTICS_TYPE_5, null,
                    contentId, startDate, endDate);
            //省份分组
            Map<String, List<StatisticsContentData>> collect = list.stream()
                    .collect(Collectors.groupingBy(StatisticsContentData::getProvince));
            for (Map.Entry<String, List<StatisticsContentData>> stringListEntry : collect.entrySet()) {
                ContentTableVo vo = new ContentTableVo();
                vo.setProvince(stringListEntry.getKey());
                //阅读量
                Integer read = stringListEntry.getValue().stream()
                        .mapToInt(StatisticsContentData::getReadCount).sum();
                vo.setReadCount(read);
                //阅读人数
                Integer people = stringListEntry.getValue().stream()
                        .mapToInt(StatisticsContentData::getPeopleCount).sum();
                vo.setPeopleCount(people);
                //评论数
                Integer comment = stringListEntry.getValue().stream()
                        .mapToInt(StatisticsContentData::getCommentCount).sum();
                vo.setCommentCount(comment);
                vos.add(vo);
            }
            //累计加今日数据
            vos.addAll(todayVo);
            //分组
            Map<String, List<ContentTableVo>> collect1 = vos.stream()
                    .collect(Collectors.groupingBy(ContentTableVo::getProvince));
            //清除集合
            vos.clear();
            for (Map.Entry<String, List<ContentTableVo>> stringListEntry : collect1.entrySet()) {
                ContentTableVo vo = new ContentTableVo();
                vo.setProvince(stringListEntry.getKey());
                //阅读量
                Integer read = stringListEntry.getValue().stream()
                        .mapToInt(ContentTableVo::getReadCount).sum();
                vo.setReadCount(read);
                //阅读人数
                Integer people = stringListEntry.getValue().stream()
                        .mapToInt(ContentTableVo::getPeopleCount).sum();
                vo.setPeopleCount(people);
                //评论数
                Integer comment = stringListEntry.getValue().stream()
                        .mapToInt(ContentTableVo::getCommentCount).sum();
                vo.setCommentCount(comment);
                vos.add(vo);
            }
        }
        //排序
        return sort(vos, sortType, sort) ;
    }

    /**
     * 排序
     **/
    private List<ContentTableVo> sort(List<ContentTableVo> vos, Integer sortType, Boolean sort) {
        if (sortType == SORT_TYPE_1) {
            if (sort) {
                vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getReadCount).reversed()).collect(Collectors.toList());
            } else {
                vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getReadCount)).collect(Collectors.toList());
            }
        } else if (sortType == SORT_TYPE_2) {
            if (sort) {
                vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getPeopleCount).reversed()).collect(Collectors.toList());
            } else {
                vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getPeopleCount)).collect(Collectors.toList());
            }
        } else if (sortType == SORT_TYPE_4) {
            if (sort) {
                vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getCommentCount).reversed()).collect(Collectors.toList());
            } else {
                vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getCommentCount)).collect(Collectors.toList());
            }
        } else {
            vos = vos.stream().sorted(Comparator.comparing(ContentTableVo::getReadCount).reversed()).collect(Collectors.toList());
        }
        return vos;
    }

    /**今日表格数据**/
    private List<ContentTableVo> todayTableVo(Date today, Integer siteId, Integer contentId) {
        List<ContentTableVo> vos = new ArrayList<>(16);
        //如果是今天，实时查询
        List<SysAccessRecord> records = sysAccessRecordService.findByDate(MyDateUtils.getStartDate(today),
                today, siteId, contentId);
        if (!records.isEmpty()) {
            //省份分组
            Map<String, List<SysAccessRecord>> collect = records.stream()
                    .collect(Collectors.groupingBy(SysAccessRecord::getAccessProvince));
            for (Map.Entry<String, List<SysAccessRecord>> stringListEntry : collect.entrySet()) {
                ContentTableVo vo = new ContentTableVo();
                vo.setProvince(stringListEntry.getKey());
                //阅读量
                vo.setReadCount(records.size());
                //阅读人数
                Integer c1 = records.stream().filter(x -> x.getLoginUserId() != null)
                        .collect(Collectors.groupingBy(SysAccessRecord::getLoginUserId)).size();
                Integer c2 = records.stream()
                        .filter(x -> x.getLoginUserId() == null)
                        .filter(x -> StringUtils.isNotBlank(x.getCookieId()))
                        .collect(Collectors.groupingBy(SysAccessRecord::getCookieId)).size();
                vo.setPeopleCount(c1 + c2);
                //处理评论量
                List<UserComment> list = userCommentService.list(Collections.singletonList(contentId), MyDateUtils.getStartDate(today),
                        today, stringListEntry.getKey());
                vo.setCommentCount(list != null ? list.size():0);
                vos.add(vo);
            }
        }
        return vos;
    }
}