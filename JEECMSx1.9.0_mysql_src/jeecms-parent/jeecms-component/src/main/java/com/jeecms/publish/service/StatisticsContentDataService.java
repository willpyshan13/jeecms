/**
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.publish.domain.StatisticsContentData;
import com.jeecms.publish.domain.vo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-16
*/
public interface StatisticsContentDataService extends IBaseService<StatisticsContentData, Integer>{

    /**
     * 列表分页
     * @param type true栏目，false内容
     * @param siteId 站点ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param sortType 排序类型
     * @param sort 排序值
     * @param pageable 分页对象
     * @return page
     */
    Page<DataVo> getPage(Boolean type, Integer siteId, Date startDate,
                         Date endDate, Integer sortType, Boolean sort,
                         Pageable pageable);

    /**
     * 定时统计
     * @Title: collect
     * @param date 时间
     * @throws GlobalException 异常
     */
    void collect(Date date) throws GlobalException;

    /**
     * 内容数据总数
     * @param siteId 站点ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param flag true栏目，false内容
     * @return DataSumVo
     */
    DataSumVo data(Integer siteId, Date startDate,  Date endDate, boolean flag);

    /**
     * 内容浏览记录
     * @param siteId 站点ID
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @throws IllegalAccessException 异常
     * @return DataSumVo
     */
    ContentViewVo view(Integer siteId, Integer contentId, Date startDate, Date endDate) throws IllegalAccessException;

    /**
     * 内容表格数据
     * @param siteId 站点ID
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param sortType 排序类型
     * @param sort true倒序，false顺序
     * @return DataSumVo
     */
    List<ContentTableVo> table(Integer siteId, Integer contentId, Date startDate, Date endDate,
                               Integer sortType, Boolean sort);
}
