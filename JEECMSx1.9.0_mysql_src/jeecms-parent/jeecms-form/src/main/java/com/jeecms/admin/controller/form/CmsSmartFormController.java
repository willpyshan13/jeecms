/**
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.form;

import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.form.service.CmsFormDataService;
import com.jeecms.interact.controller.CmsFormController;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.domain.CmsFormTypeEntity;
import com.jeecms.interact.domain.dto.CmsFormFastDto;
import com.jeecms.interact.domain.dto.CmsFormFastEditDto;
import com.jeecms.interact.domain.dto.CmsFormPublishDto;
import com.jeecms.interact.domain.dto.CmsFormSimpleFastDto;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * 表单类型控制器
 * @author: tom
 * @date: 2020/1/6 11:36
 */
@RequestMapping("/smartForm")
@RestController
public class CmsSmartFormController extends CmsFormController {

	private final transient ReentrantLock lock = new ReentrantLock();

	@PostConstruct
	public void initScene() {
		super.setFormScene(CmsFormConstant.FORM_SCENE_FORM);
	}

	/**
	 * 列表
	 * @param name
	 * @param typeId 分组id 0未分组  null全部
	 * @param status 状态 0未发布 1进行中 2已结束
	 * @param pageable
	 * @return
	 */
	@GetMapping(value = "/getPage")
	@MoreSerializeField({
			@SerializeField(clazz = CmsFormEntity.class, includes = {"id", "title","description","createTime","createUser",
			"formType","joinCount","status","viewStatus","beginTime","endTime","url","previewUrl"}),
			@SerializeField(clazz = CmsFormTypeEntity.class, includes = {"id", "name"}),
	})
	public ResponseInfo getPage(String name, Integer typeId, Short status, Pageable pageable) {
		Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
		return super.getPage(name, siteId, typeId, status,pageable);
	}

	/**
	 * 获取详情
	 *
	 * @Title: 获取详情
	 * @param: @param id
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@GetMapping(value = "/{id:[0-9]+}")
	@MoreSerializeField({
			@SerializeField(clazz = CmsFormEntity.class, excludes = {"bgImgId", "headImgId","site"}),
			@SerializeField(clazz = CmsFormItemEntity.class, excludes = {"form"}),
			@SerializeField(clazz = ResourcesSpaceData.class, includes = {"id", "url","sysRes"}),
			@SerializeField(clazz = CmsFormTypeEntity.class, includes = {"id", "name"}),
	})
	public ResponseInfo get(@PathVariable("id") Integer id) {
		return super.get(id);
	}

	/**
	 * 校验是否唯一
	 *
	 * @param title 名称
	 * @param id       id
	 * @return true 唯一 false 不唯一
	 */
	@GetMapping("/unique")
	public ResponseInfo unique(String title, Integer id,HttpServletRequest request) {
		Integer siteId = SystemContextUtils.getSiteId(request);
		return super.unique(title,id, siteId);
	}

	/**
	 * 添加
	 *
	 * @Title: 添加
	 * @param: @param result
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@PostMapping
	public ResponseInfo save(@RequestBody @Valid CmsFormFastDto dto,
							 BindingResult result, HttpServletRequest request) throws GlobalException {
		/**智能表单的表单数据区分站点管理*/
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setSiteId(siteId);
		return super.save(dto,result,request);
	}


	/**
	 * 修改
	 *
	 * @Title: 修改
	 * @param: @param result
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@PutMapping
	public ResponseInfo update(@RequestBody @Valid CmsFormFastEditDto dto,HttpServletRequest request,
									 BindingResult result) throws GlobalException {
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setSiteId(siteId);
		return super.update(dto,result);
	}

	/**
	 * 简易修改（名称描述、分组）
	 *
	 * @Title: 修改
	 * @param: @param result
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@PutMapping("/simpleUpdate")
	public ResponseInfo simpleUpdate(@RequestBody @Valid CmsFormSimpleFastDto dto, HttpServletRequest request,
									 BindingResult result) throws GlobalException {
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setSiteId(siteId);
		return super.simpleUpdate(dto,result);
	}

	@PostMapping(value = "/copy")
	public ResponseInfo copy(@RequestBody @Valid CmsFormFastEditDto dto,HttpServletRequest request,
							   BindingResult result) throws GlobalException {
		Integer siteId = SystemContextUtils.getSiteId(request);
		dto.setSiteId(siteId);
		return super.copy(dto,result);
	}

	@PutMapping(value = "/publish")
	public ResponseInfo publish(@RequestBody @Valid CmsFormPublishDto dto,
							   BindingResult result) throws GlobalException {
		return super.publish(dto,result);
	}

	/**
	 * 修改字段
	 * @Title:
	 * @param: @param result
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@PostMapping(value = "/updateFields")
	public ResponseInfo updateFields(@RequestBody CmsFormEntity dto,
									 BindingResult result) throws GlobalException {
		return super.updateFields(dto,result);
	}

	/**
	 * 删除
	 *
	 * @Title: 删除
	 * @param: @param ids
	 * @param: @return
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
    @PostMapping("/delete")
	public ResponseInfo delete(@RequestBody @Valid DeleteDto dels,
								   BindingResult result) throws GlobalException {
		ResponseInfo info = new ResponseInfo();
		final ReentrantLock lock = this.lock;
		lock.lock();
		/**智能表单的表单删除可以物理删除*/
		try {
			info= super.physicalDelete(dels.getIds());
		}finally {
			lock.unlock();
		}
		return info;
	}


	/**
	 * 清空结果
	 *
	 * @Title: 删除
	 * @param: @param ids
	 * @param: @return
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@PostMapping(value = "/clearData/delete/{id:[0-9]+}")
	public ResponseInfo clearData(@PathVariable("id") Integer id) throws GlobalException {
		validateId(id);
		formDataService.deleteAllByFormId(id);
		return new ResponseInfo();
	}

	@Autowired
	private CmsFormDataService formDataService;

}



