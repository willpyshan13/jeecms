package com.jeecms.system.dao.ext;

import com.jeecms.publish.domain.vo.ContentFlowVo;
import com.jeecms.statistics.domain.dto.StatisticsFlowDto;
import com.jeecms.statistics.domain.dto.StatisticsFlowRealTimeItemDto;
import com.jeecms.system.domain.SysAccessRecord;
import com.jeecms.system.domain.vo.MassScoreVo;

import java.util.Date;
import java.util.List;

/**
 * 访问记录dao扩展接口
 *
 * @author: chenming
 * @date: 2019年6月26日 上午8:57:38
 */
public interface SysAccessRecordDaoExt {

	/**
	 * 获取PV
	 *
	 * @param startTime 开始时间
	 * @param endTime   结束时间
	 * @return
	 * @Title: getFlowPv
	 */
	List<SysAccessRecord> getFlowPv(Date startTime, Date endTime);

	/**
	 * 统计来源
	 *
	 * @param startTime 开始时间
	 * @param endTime   结束时间
	 * @return List
	 */
	List<SysAccessRecord> getSource(Date startTime, Date endTime, Integer siteId, Boolean newVisitor,
									Short accessSourceClient, Integer sourceType);

	/**
	 * 受访分析
	 *
	 * @param startTime 开始时间
	 * @param endTime   结束时间
	 * @return List
	 */
	List<SysAccessRecord> getAccessPage(Date startTime, Date endTime, Integer siteId, Boolean newVisitor,
										Integer sorceUrlType);

	/**
	 * 获取记录
	 *
	 * @param start      开始时间
	 * @param end        结束时间
	 * @param siteId     站点ID
	 * @param sourceType 来源类型
	 * @param province   省份
	 * @param visitor    是否新客户
	 * @return
	 * @Title: getList
	 */
	List<SysAccessRecord> getList(Date start, Date end, Integer siteId,
								  Integer sourceType, String province, Boolean visitor);

	/**
	 * 根据时间+站点查询出list集合
	 *
	 * @param startTime 起始时间
	 * @param endTime   截止时间
	 * @param siteId    站点
	 * @param contentId 内容ID
	 * @Title: findByDate
	 * @return List
	 */
	List<SysAccessRecord> findByDate(Date startTime, Date endTime, Integer siteId, Integer contentId);

	/**
	 * 获取某天的访问记录数
	 *
	 * @param date   时间
	 * @param siteId 站点id
	 * @return long
	 */
	long getContentByDate(Date date, Integer siteId);

	/**
	 * 获取某天的访问ip数
	 *
	 * @param date   时间
	 * @param siteId 站点id
	 * @return long
	 */
	long countIp(Date date, Integer siteId);

	/**
	 * 根据条件查询出实时数据列表
	 *
	 * @param dto       条件dto
	 * @param startTime 起始时间
	 * @param endTime   截止时间
	 * @Title: getRealTimeItem
	 * @return: List
	 */
	List<SysAccessRecord> getRealTimeItem(Date startTime, Date endTime, StatisticsFlowRealTimeItemDto dto);

	/**
	 * 流量趋势分析
	 * @param dto 传输
	 * @return List
	 */
	List<SysAccessRecord> getFlow(StatisticsFlowDto dto);

	/**
	 * 根据sessionId或cookies和站点获取访问记录
	 *
	 * @param sessionId ip
	 * @param siteId    站点id
	 * @param date   时间
	 * @return SysAccessRecord
	 */
	SysAccessRecord findBySession(String sessionId, Integer siteId, Date date);

	/**
	 * 根据ip和站点获取访问记录
	 *
	 * @param ip     ip
	 * @param siteId 站点id
	 * @param date   时间
	 * @return SysAccessRecord
	 */
	SysAccessRecord findByIp(String ip, Integer siteId, Date date);

	/**
	 * 流量贡献值
	 * @param siteId 站点id
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return SysAccessRecord
	 */
	List<ContentFlowVo> flowContribution(Integer siteId, Date startDate, Date endDate);

	/**
	 * 流量计算
	 * @param contentId 内容ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return SysAccessRecord
	 */
	List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate);

	/**
	 * 得到数据
	 * @param type 页面类型
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return SysAccessRecord
	 */
	List<SysAccessRecord> typeList(Integer type, Date startDate, Date endDate);

	/**
	 * 获取访问人数
	 * @param contentId 内容ID
	 * @return long
	 */
	long getPeopleViews(Integer contentId);
}
