/**
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.wechat.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.wechat.bean.ValidateToken;
import com.jeecms.common.wechat.bean.request.mp.userstatistics.UserStatistics;
import com.jeecms.wechat.domain.WechatNewsAnalyze;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-22
*/
public interface WechatNewsAnalyzeService extends IBaseService<WechatNewsAnalyze, Integer>{

    /**
     * 将微信查询出的数据保存到数据库(昨天的数据 定时保存)
     *
     * @Title: saveNewsStatistics
     * @param statistics    查询对象
     * @param validateToken token验证
     * @throws GlobalException 全局异常
     */
    void saveNewsStatistics(UserStatistics statistics, ValidateToken validateToken) throws GlobalException;

    /**
     * 查询发送列表
     * @param start 开始发布时间
     * @param end 结束发布时间
     * @param appids 微信公众号AppId集合
     * @return List
     */
    List<WechatNewsAnalyze> getList(Date start, Date end, List<String> appids);
}
