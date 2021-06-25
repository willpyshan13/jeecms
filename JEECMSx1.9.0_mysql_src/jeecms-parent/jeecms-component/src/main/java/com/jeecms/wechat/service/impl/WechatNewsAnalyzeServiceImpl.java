/**
*@Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.wechat.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.wechat.annotations.ValidWeChatToken;
import com.jeecms.common.wechat.api.mp.NewsAnalyzeApiService;
import com.jeecms.common.wechat.bean.ValidateToken;
import com.jeecms.common.wechat.bean.request.mp.userstatistics.UserStatistics;
import com.jeecms.common.wechat.bean.response.mp.summary.NewsSummaryResponse;
import com.jeecms.wechat.dao.WechatNewsAnalyzeDao;
import com.jeecms.wechat.domain.WechatNewsAnalyze;
import com.jeecms.wechat.service.WechatNewsAnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-22
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class WechatNewsAnalyzeServiceImpl extends BaseServiceImpl<WechatNewsAnalyze,
        WechatNewsAnalyzeDao, Integer>  implements WechatNewsAnalyzeService {

    @Autowired
    private NewsAnalyzeApiService newsAnalyzeApiService;

    @Override
    @ValidWeChatToken()
    public void saveNewsStatistics(UserStatistics statistics, ValidateToken validateToken)
            throws GlobalException {
        List<WechatNewsAnalyze> list = new ArrayList<>(16);
        // 查询出获取用户增减数据
        NewsSummaryResponse articlesummary = newsAnalyzeApiService.getArticlesummary(statistics, validateToken);
        if (articlesummary != null && !articlesummary.getList().isEmpty()) {
            for (NewsSummaryResponse.NewsSummaryResult newsSummaryResult : articlesummary.getList()) {
                WechatNewsAnalyze wechatNewsAnalyze = new WechatNewsAnalyze();
                wechatNewsAnalyze.setAppId(validateToken.getAppId());
                wechatNewsAnalyze.setMsgId(newsSummaryResult.getMsgid());
                wechatNewsAnalyze.setTitle(newsSummaryResult.getTitle());
                wechatNewsAnalyze.setIntPageReadCount(newsSummaryResult.getIntPageReadCount());
                wechatNewsAnalyze.setIntPageReadUser(newsSummaryResult.getIntPageReadUser());
                wechatNewsAnalyze.setOriPageReadCount(newsSummaryResult.getOriPageReadCount());
                wechatNewsAnalyze.setOriPageReadUser(newsSummaryResult.getOriPageReadUser());
                wechatNewsAnalyze.setShareCount(newsSummaryResult.getShareCount());
                wechatNewsAnalyze.setShareUser(newsSummaryResult.getShareUser());
                wechatNewsAnalyze.setAddToFavCount(newsSummaryResult.getAddToFavCount());
                wechatNewsAnalyze.setAddToFavUser(newsSummaryResult.getAddToFavUser());
                wechatNewsAnalyze.setRefDate(MyDateUtils.parseDate(newsSummaryResult.getRefDate()));
                list.add(wechatNewsAnalyze);
            }
            super.saveAll(list);
        }
    }

    @Override
    public List<WechatNewsAnalyze> getList(Date start, Date end, List<String> appids) {
        return dao.getList(start, end, appids);
    }
}