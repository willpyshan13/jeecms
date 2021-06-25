/**
*@Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.weibo.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.weibo.dao.StatisticsWeiboFansDao;
import com.jeecms.weibo.domain.StatisticsWeiboFans;
import com.jeecms.weibo.service.StatisticsWeiboFansService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-23
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class StatisticsWeiboFansServiceImpl extends BaseServiceImpl<StatisticsWeiboFans, StatisticsWeiboFansDao, Integer>
        implements StatisticsWeiboFansService {


    @Override
    public List<StatisticsWeiboFans> getList(Date startDate, Date endDate, List<String> uids) {
        return dao.getList(startDate, endDate, uids);
    }
}