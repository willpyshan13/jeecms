/**
*@Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.publish.dao.StatisticsPublishDetailsDao;
import com.jeecms.publish.domain.ContentPublishRecord;
import com.jeecms.publish.domain.StatisticsPublishDetails;
import com.jeecms.publish.domain.vo.PublishPageVo;
import com.jeecms.publish.service.ContentPublishRecordService;
import com.jeecms.publish.service.StatisticsPublishDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
* @author ljw
* @version 1.0
* @date 2020-06-04
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class StatisticsPublishDetailsServiceImpl extends BaseServiceImpl<StatisticsPublishDetails, StatisticsPublishDetailsDao, Integer>
        implements StatisticsPublishDetailsService {

    @Autowired
    private ContentPublishRecordService contentPublishRecordService;

    @Override
    public List<PublishPageVo> publishData(Integer siteId, Integer publishType, Boolean sort, String key,
                                           Date startDate, Date endDate) {
        return dao.publishData(siteId, publishType, sort, key, startDate, endDate);
    }

    @Override
    public List<StatisticsPublishDetails> getList(Integer type, Integer siteId, Date startDate, Date endDate) {
        return dao.getList(type, siteId, startDate, endDate);
    }

    @Override
    public void collect(Date date) throws GlobalException {
        List<StatisticsPublishDetails> detailsList = new ArrayList<>(16);
        //得到数据
        List<ContentPublishRecord> list = contentPublishRecordService.getList(null, null, null, null,
                MyDateUtils.getStartDate(date), MyDateUtils.getFinallyDate(date));
        if (!list.isEmpty()) {
            //按站点
            Map<Integer, List<ContentPublishRecord>> site = list.stream()
                    .collect(Collectors.groupingBy(ContentPublishRecord::getSiteId));
            for (Map.Entry<Integer, List<ContentPublishRecord>> entry : site.entrySet()) {
                List<ContentPublishRecord> value = entry.getValue();
                StatisticsPublishDetails details = new StatisticsPublishDetails();
                details.setSiteId(entry.getKey());
                details.setTypes(PUBLISH_SITE_TYPE);
                details.setNumbers((long) value.size());
                details.setStatisticsDay(date);
                detailsList.add(details);
                //按站点 + 按栏目
                Map<Integer, List<ContentPublishRecord>> channel = value.stream()
                        .collect(Collectors.groupingBy(ContentPublishRecord::getChannelId));
                for (Map.Entry<Integer, List<ContentPublishRecord>> channelEntry : channel.entrySet()) {
                    StatisticsPublishDetails detail = new StatisticsPublishDetails();
                    detail.setSiteId(entry.getKey());
                    detail.setChannelId(channelEntry.getKey());
                    detail.setTypes(PUBLISH_CHANNEL_TYPE);
                    detail.setNumbers((long) channelEntry.getValue().size());
                    detail.setStatisticsDay(date);
                    detailsList.add(detail);
                }
                //按用户
                Map<Integer, List<ContentPublishRecord>> user = value.stream()
                        .collect(Collectors.groupingBy(ContentPublishRecord::getUserId));
                for (Map.Entry<Integer, List<ContentPublishRecord>> entry1 : user.entrySet()) {
                    StatisticsPublishDetails detailsUser = new StatisticsPublishDetails();
                    detailsUser.setSiteId(entry.getKey());
                    detailsUser.setUserId(entry1.getKey());
                    detailsUser.setTypes(PUBLISH_USER_TYPE);
                    detailsUser.setNumbers((long) entry1.getValue().size());
                    detailsUser.setStatisticsDay(date);
                    detailsList.add(detailsUser);
                }
                //按组织
                Map<Integer, List<ContentPublishRecord>> org = value.stream()
                        .collect(Collectors.groupingBy(ContentPublishRecord::getOrgId));
                for (Map.Entry<Integer, List<ContentPublishRecord>> entry2 : org.entrySet()) {
                    StatisticsPublishDetails detailsOrg = new StatisticsPublishDetails();
                    detailsOrg.setSiteId(entry.getKey());
                    detailsOrg.setOrgId(entry2.getKey());
                    detailsOrg.setTypes(PUBLISH_ORG_TYPE);
                    detailsOrg.setNumbers((long) entry2.getValue().size());
                    detailsOrg.setStatisticsDay(date);
                    detailsList.add(detailsOrg);
                }
            }
        }
        super.saveAll(detailsList);
    }
}