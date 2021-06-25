/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao.ext;


import com.jeecms.publish.domain.StatisticsPublishDetails;
import com.jeecms.publish.domain.vo.PublishPageVo;

import java.util.Date;
import java.util.List;

/**
 * 内容发布记录详情统计DAO
* @author ljw
* @version 基于x1.4.0
* @date 2020-06-03
*/
public interface StatisticsPublishDetailsDaoExt {

    /**
     * 数据表格接口
     * @param siteId 站点ID
     * @param publishType 发布类型
     * @param sort true倒序，false 正序
     * @param key 关键字
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return Page
     */
    List<PublishPageVo> publishData(Integer siteId, Integer publishType, Boolean sort, String key,
                                    Date startDate, Date endDate);

    /**
     * 得到集合
     * @param type 类型
     * @param siteId 站点ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return Long
     */
    List<StatisticsPublishDetails> getList(Integer type, Integer siteId, Date startDate, Date endDate);
}
