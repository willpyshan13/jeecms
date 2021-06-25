/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao.ext;

import com.jeecms.publish.domain.StatisticsContentData;
import com.jeecms.publish.domain.vo.DataSumVo;
import com.jeecms.publish.domain.vo.DataVo;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-16
*/
public interface StatisticsContentDataDaoExt {

    /**
     * 数据表格接口
     * @param type 统计类型
     * @param siteId 站点ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return Page
     */
    List<DataVo> getPage(Integer type, Integer siteId,
                         Date startDate, Date endDate);

    /**
     * 统计
     * @param siteId 站点ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param flag true栏目，false内容
     * @return DataSumVo
     */
    DataSumVo count(Integer siteId, Date startDate,  Date endDate, boolean flag);

    /**
     * 得到集合
     * @param type 类型
     * @param device 设备类型
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return DataSumVo
     */
    List<StatisticsContentData> getList(Integer type, Integer device, Integer contentId, Date startDate, Date endDate);

    /**
     * 内容统计
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return DataSumVo
     */
    DataSumVo countContent(Integer contentId, Date startDate, Date endDate);
}
