/**
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.publish.domain.StatisticsPublish;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-04
*/
public interface StatisticsPublishService extends IBaseService<StatisticsPublish, Integer>{

    /**
     * 获取列表
     * @param siteId 站点ID
     * @param type 类型
     * @param start 开始时间
     * @param end 结束时间
     * @return List
     */
    List<StatisticsPublish> getList(Integer siteId, Integer type, Date start, Date end);

    /**
     * 得到历史最高统计数据
     * @param type 类型
     * @param siteId 站点ID
     * @return StatisticsPublish
     */
    Integer countHigh(Integer type, Integer siteId);

    /**
     * 定时统计
     * @Title: collect
     * @param date 时间
     * @throws GlobalException 异常
     */
    void collect(Date date) throws GlobalException;
}
