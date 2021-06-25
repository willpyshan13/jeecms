/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao.ext;


import com.jeecms.publish.domain.StatisticsPublish;

import java.util.Date;
import java.util.List;

/**
 * 内容发布记录统计DAO
* @author ljw
* @version 基于x1.4.0
* @date 2020-06-03
*/
public interface StatisticsPublishDaoExt {

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
     * @return StatisticsPublish
     */
    Integer countHigh(Integer type, Integer siteId);
}
