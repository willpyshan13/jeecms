package com.jeecms.publish.service;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.vo.ContentPerfVo;
import com.jeecms.publish.domain.ContentPublishRecord;
import com.jeecms.publish.domain.vo.FlowContributionVo;
import com.jeecms.publish.domain.vo.PublishSumVo;
import com.jeecms.statistics.domain.vo.SiteGeneralVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 网站信息内容发布service接口
 * @author: ljw
 * @date:   2020年6月3日 下午2:10:02
 */
public interface ContentPublishRecordService extends IBaseService<ContentPublishRecord, Integer> {


    /**
     * 获取列表
     * @param siteId 站点ID
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param channelId 栏目ID
     * @param start 开始时间
     * @param end 结束时间
     * @return List
     */
    List<ContentPublishRecord> getList(Integer siteId, Integer orgId, Integer userId, Integer channelId,
                                       Date start, Date end);

    /**
     * 获取列表
     * @param siteId 站点ID
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param channelId 栏目ID
     * @param start 开始时间
     * @param end 结束时间
     * @return List
     */
    Long count(Integer siteId, Integer orgId, Integer userId, Integer channelId,
                                    Date start, Date end);

    /**
     * 保存内容发布记录
     * @param content 内容
     * @throws GlobalException 异常
     */
    void savePublish(Content content) throws GlobalException;

    /**
     * 网站信息
     * @return SiteGeneralVo
     * @throws GlobalException 异常
     */
    SiteGeneralVo siteInfo(Integer siteId) throws GlobalException;

    /**
     * 流量贡献值
     * @return JSONObject
     * @param siteId 站点ID
     * @throws GlobalException 异常
     */
    FlowContributionVo views(Integer siteId) throws GlobalException;

    /**
     * 发布统计接口
     * @param siteId 站点ID
     * @param publishType 发布类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return JSONObject
     */
    PublishSumVo publish(Integer siteId, Integer publishType, Date startDate, Date endDate);

    /**
     * 数据表格接口
     * @param siteId 站点ID
     * @param publishType 发布类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param sort true倒序，false 正序
     * @param key 关键字
     * @param pageable 分页对象
     * @return JSONObject
     */
    JSONObject publishData(Integer siteId, Integer publishType, Boolean sort, String key,
                           Date startDate, Date endDate, Pageable pageable);

    /**
     * 数据表格接口
     * @param publishType 发布类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param sort true倒序，false 正序
     * @param key 关键字
     * @return Page
     */
    Workbook exportData(Integer publishType, Boolean sort, String key,
                         Date startDate, Date endDate);

    /**
	 * 查询发布内容列表
	 * @param start 开始发布时间
	 * @param end 结束发布时间
	 * @param channels 栏目集合
	 * @param users 用户集合
	 * @param orgs 组织集合
	 * @param sites 站点集合
	 * @return List
	 */
	List<ContentPerfVo> getList(Date start, Date end, List<Integer> channels,
                                List<Integer> users, List<Integer> orgs, List<Integer> sites);
}
