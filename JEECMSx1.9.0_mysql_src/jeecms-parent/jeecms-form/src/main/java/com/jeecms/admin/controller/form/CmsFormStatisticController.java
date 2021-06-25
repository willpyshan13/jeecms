/**
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.form;

import com.alibaba.fastjson.JSONArray;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.form.domain.CmsFormDataEntity;
import com.jeecms.form.domain.vo.CmsFormDataAttrStatisticVo;
import com.jeecms.form.domain.vo.CmsFormDataDeviceVo;
import com.jeecms.form.domain.vo.CmsFormDataProviceVo;
import com.jeecms.form.domain.vo.CmsFormDataTimeVo;
import com.jeecms.form.service.CmsFormDataService;
import com.jeecms.interact.controller.CmsFormController;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.domain.dto.CmsFormFastDto;
import com.jeecms.interact.domain.dto.CmsFormFastEditDto;
import com.jeecms.interact.domain.dto.CmsFormPublishDto;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.util.SystemContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * 表单统计控制器
 * @author: tom
 * @date: 2020/2/24 11:36
 */
@RequestMapping("/smartForm/statistic")
@RestController
public class CmsFormStatisticController extends BaseController<CmsFormDataEntity,Integer> {

	/**
	 * 查询表单统计数据根据字段 单选、多选、图片单选、图片多选、下拉、级联、城市、性别时，使用饼图展示；
	 * 其他组件只显示字段
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/groupByField/{id:[0-9]+}")
	public ResponseInfo groupByField(@PathVariable("id") Integer id) {
		List<CmsFormDataAttrStatisticVo> vos = formDataService.getAttrGroupByField(id);
		return new ResponseInfo(vos);
	}

	/**
	 * 查询表单统计数据根据字段 单选、多选、图片单选、图片多选、下拉、级联、城市、性别时，使用饼图展示；
	 * 其他组件只显示字段
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/device/{id:[0-9]+}")
	public ResponseInfo staticByDevice(@PathVariable("id") Integer id) {
		long pcCount = formDataService.getCount(id,null,null,null,true,null,null,null,null,null,null,null);
		long mobileCount = formDataService.getCount(id,null,null,null,false,null,null,null,null,null,null,null);
		CmsFormDataDeviceVo vo = new CmsFormDataDeviceVo();
		vo.setMobileCount(mobileCount);
		vo.setPcCount(pcCount);
		return new ResponseInfo(vo);
	}

	/***
	 * 统计-省份
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/province/{id:[0-9]+}")
	public ResponseInfo staticByProvice(@PathVariable("id") Integer id) {
		List<CmsFormDataProviceVo> vos = formDataService.staticByProvince(id);
		return new ResponseInfo(vos);
	}

    /**
     * 趋势统计
     * @param id 表单id
     * @param pc  是否pc
     * @param province 省份编号
     * @param city 城市编号
     * @param showType 1按时显示 2按天显示 3 按周显示 4按月统计
     * @param beginTime 查询开始时间
     * @param endTime 查询结束时间
     * @param request
     * @return
     */
	@GetMapping("/trend/{id:[0-9]+}")
	public ResponseInfo trend(@PathVariable("id") Integer id, Boolean pc, String province, String city,
							  Integer showType, Date beginTime, Date endTime, HttpServletRequest request) {
		showType = showType == null ? CmsFormConstant.GROUP_HOUR : showType;
		List<CmsFormDataTimeVo>vos = formDataService.staticCountGroupByTime(id, pc,province,city,showType, beginTime, endTime);
		return new ResponseInfo(vos);
	}

	@Autowired
	private CmsFormDataService formDataService;
	@Autowired
	private CmsFormService formService;

}



