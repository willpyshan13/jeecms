package com.jeecms.publish.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.publish.domain.StatisticsPublishDetails;
import com.jeecms.publish.domain.vo.PublishPageVo;

import java.util.Date;
import java.util.List;

/**
 * 网站信息内容发布service接口
 * @author: ljw
 * @date:   2020年6月3日 下午2:10:02
 */
public interface StatisticsPublishDetailsService extends IBaseService<StatisticsPublishDetails, Integer> {

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
    List<StatisticsPublishDetails> getList(Integer type, Integer siteId,  Date startDate, Date endDate);

    /**
     * 定时统计
     * @Title: collect
     * @param date 时间
     * @throws GlobalException 异常
     */
    void collect(Date date) throws GlobalException;
}
