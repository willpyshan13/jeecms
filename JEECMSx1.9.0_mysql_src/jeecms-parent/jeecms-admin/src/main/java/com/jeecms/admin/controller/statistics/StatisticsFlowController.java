/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.jeecms.common.annotation.EncryptMethod;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.statistics.domain.StatisticsFlow;
import com.jeecms.statistics.domain.dto.StatisticsFlowDto;
import com.jeecms.statistics.domain.dto.StatisticsFlowRealTimeItemDto;
import com.jeecms.statistics.domain.vo.StatisticsFlowImageVo;
import com.jeecms.statistics.domain.vo.StatisticsFlowListVo;
import com.jeecms.statistics.domain.vo.StatisticsFlowListVos;
import com.jeecms.statistics.domain.vo.StatisticsFlowRealTimeItemVo;
import com.jeecms.statistics.service.StatisticsFlowDtoService;
import com.jeecms.statistics.service.StatisticsFlowService;
import com.jeecms.system.domain.Area;
import com.jeecms.system.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 趋势统计
 *
 * @author: chenming
 * @date: 2019年7月1日 上午9:03:55
 */
@RequestMapping("/statisticsflow")
@RestController
public class StatisticsFlowController extends BaseController<StatisticsFlow, Integer> {

	@Autowired
	private StatisticsFlowService statisticsFlowService;
	@Autowired
	private StatisticsFlowDtoService dtoService;
	@Autowired
	private AreaService areaService;

	@PostConstruct
	public void init() {
		String[] queryParams = {};
		super.setQueryParams(queryParams);
	}

	/**
	 * 实时访客
	 */
	@GetMapping(value = "/realtime")
	public ResponseInfo realTime(@RequestParam Integer siteId) {
		return new ResponseInfo(statisticsFlowService.getRealTimeVo(siteId));
	}

	/**
	 * 实时访客-在线访问人数
	 */
	@GetMapping(value = "/realtime/uvnum")
	public ResponseInfo realTimeUvNum(@RequestParam Integer siteId) {
		return new ResponseInfo(statisticsFlowService.getRealTimeUvNum(siteId));
	}
	
	/**
	 * 实时访客列表
	 */
	@EncryptMethod
	@PostMapping(value = "/realtime/page")
	public ResponseInfo realTimeItem(@RequestBody @Valid StatisticsFlowRealTimeItemDto dto,
				BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		if (dto.getArea() != null) {
			List<Area> list = areaService.findByAreaCode(dto.getArea());
			if (!list.isEmpty()) {
				Area area = list.get(0);
				switch (area.getAreaDictCode()) {
					case Area.AREA_TYPE_PROVINCE:
						dto.setProvince(area.getAreaName());
						break;
					case Area.AREA_TYPE_CITY:
						if (area.getParent() == null) {
							break;
						}
						dto.setProvince(area.getParent().getAreaName());
						dto.setCity(area.getAreaName());
						break;
					default:
						dto.setProvince("县区");
						break;
				}
			}
		}
		List<StatisticsFlowRealTimeItemVo> vos = statisticsFlowService.getRealTimeItemVo(dto);
		Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getSize());
		PageImpl<StatisticsFlowRealTimeItemVo> page = null;
		if (vos != null) {
			Integer size = vos.size();
			Integer num = pageable.getPageSize();
			vos = vos.stream()
					.skip((long)pageable.getPageSize() * (pageable.getPageNumber()))
					.limit(num).collect(Collectors.toList());
			page = new PageImpl<>(vos, pageable, size);
		} else {
			page = new PageImpl<>(
					new ArrayList<>(), pageable, 0);
		}
		return new ResponseInfo(page);
	}

	/**
	 * 图表数据
	 */
	@PostMapping(value = "/image")
	public ResponseInfo getImage(@RequestBody @Valid StatisticsFlowDto dto, BindingResult result)
			throws GlobalException {
		super.validateBindingResult(result);
		StatisticsFlowImageVo vo = statisticsFlowService.getFlow(dto).getImageVo();
		return new ResponseInfo(vo);
	}

	/**
	 * 列表数据
	 */
	@PostMapping(value = "/page")
	public ResponseInfo getList(@RequestBody @Valid StatisticsFlowDto dto,
								BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		if (dto.getArea() != null) {
			List<Area> list = areaService.findByAreaCode(dto.getArea());
			if (!list.isEmpty()) {
				Area area = list.get(0);
				switch (area.getAreaDictCode()) {
					case Area.AREA_TYPE_PROVINCE:
						dto.setProvince(area.getAreaName());
						break;
					case Area.AREA_TYPE_CITY:
						if (area.getParent() == null) {
							break;
						}
						dto.setProvince(area.getParent().getAreaName());
						dto.setCity(area.getAreaName());
						break;
					default:
						dto.setProvince("县区");
						break;
				}
			}
		}
		List<StatisticsFlowListVo> vos = statisticsFlowService.getFlow(dto).getVos();
		Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getSize());
		PageImpl<StatisticsFlowListVo> page = null;
		if (vos != null) {
			Integer size = vos.size();
			Integer num = pageable.getPageSize();
			if (dto.getIsCurrent()) {
				num = 24;
			}
			vos = vos.stream()
					.skip((long)pageable.getPageSize() * (pageable.getPageNumber()))
					.limit(num).collect(Collectors.toList());
			page = new PageImpl<>(vos, pageable, size);
		} else {
			page = new PageImpl<>(new ArrayList<>(), pageable, 0);
		}
		StatisticsFlowListVos vo = dtoService.initFlowListVos(vos);
		vo.setVos(page);
		return new ResponseInfo(vo);
	}

	/**
	 * 列表数据
	 */
	@GetMapping(value = "/overview")
	public ResponseInfo getOverview(@RequestParam Integer siteId) {
		return new ResponseInfo(statisticsFlowService.getOverviewVos(siteId));
	}

	@GetMapping
	public ResponseInfo save() throws GlobalException {
		statisticsFlowService.save();
		return new ResponseInfo();
	}
}
