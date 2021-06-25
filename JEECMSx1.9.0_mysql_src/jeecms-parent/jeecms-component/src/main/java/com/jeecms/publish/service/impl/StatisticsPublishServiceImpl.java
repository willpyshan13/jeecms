/**
*@Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service.impl;

import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.interact.domain.UserComment;
import com.jeecms.interact.service.UserCommentService;
import com.jeecms.publish.dao.StatisticsPublishDao;
import com.jeecms.publish.domain.ContentPublishRecord;
import com.jeecms.publish.domain.StatisticsPublish;
import com.jeecms.publish.service.ContentPublishRecordService;
import com.jeecms.publish.service.StatisticsPublishService;
import com.jeecms.wechat.domain.WechatSend;
import com.jeecms.wechat.service.WechatSendService;
import com.jeecms.weibo.domain.WeiboArticlePush;
import com.jeecms.weibo.service.WeiboArticlePushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
* @author ljw
* @version 1.0
* @date 2020-06-04
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class StatisticsPublishServiceImpl extends BaseServiceImpl<StatisticsPublish, StatisticsPublishDao, Integer>
        implements StatisticsPublishService {

    @Autowired
    private ContentPublishRecordService contentPublishRecordService;
    @Autowired
    private WechatSendService wechatSendService;
    @Autowired
    private WeiboArticlePushService weiboArticlePushService;
    @Autowired
    private UserCommentService userCommentService;
    @Autowired
    private ChannelService channelService;

    @Override
    public List<StatisticsPublish> getList(Integer siteId, Integer type, Date start, Date end) {
        return dao.getList(siteId, type, start, end);
    }

    @Override
    public Integer countHigh(Integer type, Integer siteId) {
        return dao.countHigh(type, siteId);
    }

    @Override
    public void collect(Date date) throws GlobalException {
        List<StatisticsPublish> list = new ArrayList<>(4);
        Map<String, String[]> params = new HashMap<>(2);
        String start = MyDateUtils.formatDate(MyDateUtils.getStartDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        String end = MyDateUtils.formatDate(MyDateUtils.getFinallyDate(date),
                MyDateUtils.COM_Y_M_D_H_M_S_PATTERN);
        params.put("GTE_publishTime_Timestamp", new String[]{start});
        params.put("LTE_publishTime_Timestamp", new String[]{end});
        //统计昨日内容发布情况
        List<ContentPublishRecord> records = contentPublishRecordService.getList(params, null, true);
        if (!records.isEmpty()) {
            Map<Integer, List<ContentPublishRecord>> map = records.stream()
                    .collect(Collectors.groupingBy(ContentPublishRecord::getSiteId));
            for (Map.Entry<Integer, List<ContentPublishRecord>> integerListEntry : map.entrySet()) {
                StatisticsPublish publish = new StatisticsPublish();
                publish.setSiteId(integerListEntry.getKey());
                publish.setNumbers(integerListEntry.getValue().size());
                publish.setTypes(STATISTICS_TYPE_CONTENT);
                publish.setStatisticsDay(date);
                list.add(publish);
            }
        }
        Map<String, String[]> maps = new HashMap<>(2);
        maps.put("GTE_createTime_Timestamp", new String[]{start});
        maps.put("LTE_createTime_Timestamp", new String[]{end});
        //微信文章发布
        List<WechatSend> wechat = wechatSendService.getList(maps, null, true);
        if (!wechat.isEmpty()) {
            Map<Integer, List<WechatSend>> map = wechat.stream()
                    .collect(Collectors.groupingBy(WechatSend::getSiteId));
            for (Map.Entry<Integer, List<WechatSend>> integerListEntry : map.entrySet()) {
                StatisticsPublish publish = new StatisticsPublish();
                publish.setSiteId(integerListEntry.getKey());
                publish.setNumbers(integerListEntry.getValue().size());
                publish.setTypes(STATISTICS_TYPE_WECHAT);
                publish.setStatisticsDay(date);
                list.add(publish);
            }
        }
        //微博发布
        List<WeiboArticlePush> weibo =  weiboArticlePushService.getList(maps, null, true);
        if (!weibo.isEmpty()) {
            Map<Integer, List<WeiboArticlePush>> map = weibo.stream()
                    .collect(Collectors.groupingBy(WeiboArticlePush::getSiteId));
            for (Map.Entry<Integer, List<WeiboArticlePush>> integerListEntry : map.entrySet()) {
                StatisticsPublish publish = new StatisticsPublish();
                publish.setSiteId(integerListEntry.getKey());
                publish.setNumbers(integerListEntry.getValue().size());
                publish.setTypes(STATISTICS_TYPE_WEIBO);
                publish.setStatisticsDay(date);
                list.add(publish);
            }
        }
        //评论数
        List<UserComment> comment = userCommentService.getList(maps, null, true);
        if (!comment.isEmpty()) {
            Map<Integer, List<UserComment>> map = comment.stream()
                    .collect(Collectors.groupingBy(UserComment::getSiteId));
            for (Map.Entry<Integer, List<UserComment>> integerListEntry : map.entrySet()) {
                StatisticsPublish publish = new StatisticsPublish();
                publish.setSiteId(integerListEntry.getKey());
                publish.setNumbers(integerListEntry.getValue().size());
                publish.setTypes(STATISTICS_TYPE_COMMENT);
                publish.setStatisticsDay(date);
                list.add(publish);
            }
        }
        //新建栏目
        List<Channel> channel = channelService.getList(maps, null, true);
        if (!channel.isEmpty()) {
            Map<Integer, List<Channel>> map = channel.stream()
                    .collect(Collectors.groupingBy(Channel::getSiteId));
            for (Map.Entry<Integer, List<Channel>> integerListEntry : map.entrySet()) {
                StatisticsPublish publish = new StatisticsPublish();
                publish.setSiteId(integerListEntry.getKey());
                publish.setNumbers(integerListEntry.getValue().size());
                publish.setTypes(STATISTICS_TYPE_CHANNEL);
                publish.setStatisticsDay(date);
                list.add(publish);
            }
        }
        super.saveAll(list);
    }
}