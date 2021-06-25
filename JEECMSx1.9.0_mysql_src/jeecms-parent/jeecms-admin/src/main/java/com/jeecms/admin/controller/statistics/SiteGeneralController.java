/**
 *  * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.statistics;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.util.ResponseUtils;
import com.jeecms.publish.domain.vo.DataVo;
import com.jeecms.publish.service.ContentPublishRecordService;
import com.jeecms.publish.service.StatisticsContentDataService;
import com.jeecms.publish.service.StatisticsPublishDetailsService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.jeecms.publish.constants.PublishConstant.*;

/**
 * 网站统计概况扩展控制器
 * @author: ljw
 * @version: 基于x1.4.0
 * @date 2020-06-03
 */
@RequestMapping("/sitegeneral")
@RestController
public class SiteGeneralController {

	@Autowired
	private ContentPublishRecordService contentPublishRecordService;
	@Autowired
	private StatisticsContentDataService statisticsContentDataService;

	/**
	 * 网站信息
	 * @Title: info
	 * @return ResponseInfo 响应
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/info")
	public ResponseInfo info(HttpServletRequest request) throws GlobalException {
		Integer siteId;
		String siteString = request.getParameter("siteId");
		if (StringUtils.isNotBlank(siteString)) {
			siteId = Integer.valueOf(siteString);
		} else {
			siteId = SystemContextUtils.getSiteId(request);
		}
		return new ResponseInfo(contentPublishRecordService.siteInfo(siteId));
	}

	/**
	 * 流量贡献值
	 * @Title: info
	 * @return ResponseInfo 响应
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/views")
	public ResponseInfo views(HttpServletRequest request) throws GlobalException {
		Integer siteId;
		String siteString = request.getParameter("siteId");
		if (StringUtils.isNotBlank(siteString)) {
			siteId = Integer.valueOf(siteString);
		} else {
			siteId = SystemContextUtils.getSiteId(request);
		}
		return new ResponseInfo(contentPublishRecordService.views(siteId));
	}

	/**
	 * 内容发布统计-栏目数据(饼状图/柱状图)
	 * @param siteId 站点ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/channel")
	public ResponseInfo channel(Integer siteId, Date startDate, Date endDate) {
		return new ResponseInfo(contentPublishRecordService.publish(siteId, PUBLISH_CHANNEL_TYPE,
				startDate, endDate ));
	}

	/**
	 * 内容发布统计-数据表格列表分页
	 * @param siteId 站点ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/channel/data")
	public ResponseInfo channelData(Integer siteId, Date startDate,
									Date endDate, Boolean sort,
									Pageable pageable) {
		return new ResponseInfo(contentPublishRecordService.publishData(siteId, PUBLISH_CHANNEL_TYPE, sort,
				null, startDate, endDate, pageable));
	}

	/**
	 * 内容发布统计-用户数据(饼状图/柱状图)
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/user")
	public ResponseInfo user( Date startDate, Date endDate) {
		return new ResponseInfo(contentPublishRecordService.publish(null, PUBLISH_USER_TYPE,
				startDate, endDate ));
	}

	/**
	 * 内容发布统计-用户数据表格列表分页
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @param key 关键字
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/user/data")
	public ResponseInfo userData(Date startDate,
									Date endDate, Boolean sort, String key,
									Pageable pageable) {
		return new ResponseInfo(contentPublishRecordService.publishData(null, PUBLISH_USER_TYPE, sort,
				key, startDate, endDate, pageable));
	}

	/**
	 * 内容发布统计-组织数据(饼状图/柱状图)
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/org")
	public ResponseInfo org(Date startDate, Date endDate) {
		return new ResponseInfo(contentPublishRecordService.publish(null, PUBLISH_ORG_TYPE,
				startDate, endDate ));
	}

	/**
	 * 内容发布统计-数据表格列表分页
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @param key 关键字
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/org/data")
	public ResponseInfo orgData(Date startDate,
								 Date endDate, Boolean sort, String key,
								 Pageable pageable) {
		return new ResponseInfo(contentPublishRecordService.publishData(null, PUBLISH_ORG_TYPE, sort,
				key, startDate, endDate, pageable));
	}

	/**
	 * 内容发布统计-站点数据(饼状图/柱状图)
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/site")
	public ResponseInfo site(Date startDate, Date endDate) {
		return new ResponseInfo(contentPublishRecordService.publish(null, PUBLISH_SITE_TYPE,
				startDate, endDate ));
	}

	/**
	 * 内容发布统计-数据表格列表分页
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @param key 关键字
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/site/data")
	public ResponseInfo siteData(Date startDate,
								Date endDate, Boolean sort, String key,
								Pageable pageable) {
		return new ResponseInfo(contentPublishRecordService.publishData(null, PUBLISH_SITE_TYPE, sort,
				key, startDate, endDate, pageable));
	}

	/**
	 * 内容数据统计-按栏目列表分页
	 * @param siteId 站点ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sortType 排序类型
	 * @param sort true倒序，false 正序
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/statistics/channel")
	@MoreSerializeField({@SerializeField(clazz = DataVo.class,
			includes = {"name", "readCount", "peopleCount", "likeCount", "commentCount"})})
	public ResponseInfo statisticsChannel(Integer siteId, Date startDate,
								 Date endDate, Integer sortType, Boolean sort,
								 Pageable pageable) {
		//排序类型
		if (sortType == null) {
			sortType = SORT_TYPE_1;
		}
		//排序
		if (sort == null) {
			sort = false;
		}
		return new ResponseInfo(statisticsContentDataService.getPage(true,
				siteId, startDate, endDate, sortType, sort, pageable));
	}

	/**
	 * 内容数据统计-按单篇内容列表分页
	 * @param siteId 站点ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sortType 排序类型
	 * @param sort true倒序，false 正序
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/statistics/content")
	@MoreSerializeField({@SerializeField(clazz = DataVo.class,
			includes = {"name", "readCount", "peopleCount", "likeCount", "commentCount",
			"publishTime", "url"})})
	public ResponseInfo statisticsContent(Integer siteId, Date startDate,
										  Date endDate, Integer sortType, Boolean sort,
										  Pageable pageable) {
		//排序类型
		if (sortType == null) {
			sortType = SORT_TYPE_1;
		}
		//排序
		if (sort == null) {
			sort = false;
		}
		return new ResponseInfo(statisticsContentDataService.getPage(false,
				siteId, startDate, endDate, sortType, sort, pageable));
	}

	/**
	 * 内容数据统计-总数
	 * @param siteId 站点ID
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param flag true栏目，false内容
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/statistics/data")
	public ResponseInfo dataChannel(Integer siteId, Date startDate,  Date endDate, boolean flag) {
		return new ResponseInfo(statisticsContentDataService.data(siteId, startDate, endDate, flag));
	}

	/**
	 * 导出用户数据表格
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @param key 关键字
	 */
	@GetMapping(value = "/excel/user")
	public void userExcel(HttpServletRequest request, HttpServletResponse response,
						  Date startDate, Date endDate, Boolean sort, String key) throws IOException {
		Workbook workbook = contentPublishRecordService.exportData(PUBLISH_USER_TYPE, sort,
				key, startDate, endDate);
		ResponseUtils.exportExcel(request, response, "用户数据导出数据", workbook);
	}

	/**
	 * 导出组织数据表格
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @param key 关键字
	 */
	@GetMapping(value = "/excel/org")
	public void orgExcel(HttpServletRequest request, HttpServletResponse response,
						  Date startDate, Date endDate, Boolean sort, String key) throws IOException {
		Workbook workbook = contentPublishRecordService.exportData(PUBLISH_ORG_TYPE, sort,
				key, startDate, endDate);
		ResponseUtils.exportExcel(request, response, "组织数据导出数据", workbook);
	}

	/**
	 * 导出站点数据表格
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param sort true倒序，false 正序
	 * @param key 关键字
	 */
	@GetMapping(value = "/excel/site")
	public void siteExcel(HttpServletRequest request, HttpServletResponse response,
						  Date startDate, Date endDate, Boolean sort, String key) throws IOException {
		Workbook workbook = contentPublishRecordService.exportData(PUBLISH_SITE_TYPE, sort,
				key, startDate, endDate);
		ResponseUtils.exportExcel(request, response, "站点数据导出数据", workbook);
	}
	@Autowired
	private StatisticsPublishDetailsService statisticsPublishDetailsService;

}