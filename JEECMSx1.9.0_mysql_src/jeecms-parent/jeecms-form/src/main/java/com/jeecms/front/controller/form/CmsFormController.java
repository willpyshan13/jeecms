/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller.form;

import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.InteractErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.form.domain.vo.CmsFormSubmitVo;
import com.jeecms.form.service.CmsFormDataService;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.domain.CmsFormTypeEntity;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 表单控制器
 * @author: tom
 * @date: 2020/2/15 11:45
 */
@RestController
@RequestMapping("/smartForm")
public class CmsFormController extends BaseController<CmsFormEntity,Integer> {

	@Autowired
	private CmsFormService service;
	@Autowired
	private CmsFormDataService formDataService;

	private final transient ReentrantLock lock = new ReentrantLock();

	@PostConstruct
	public void init() {
		String[] queryParams = {};
		super.setQueryParams(queryParams);
	}


	/**
	 * 获取表单详情
	 * @param id     id
	 * @param request HttpServletRequest
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/{id:[0-9]+}")
	@MoreSerializeField({
			@SerializeField(clazz = CmsFormEntity.class, excludes = {"bgImgId", "headImgId","bgImg","headImg","site"}),
			@SerializeField(clazz = CmsFormItemEntity.class, excludes = {"form"}),
			@SerializeField(clazz = ResourcesSpaceData.class, includes = {"id", "url","sysRes"}),
			@SerializeField(clazz = CmsFormTypeEntity.class, includes = {"id", "name"}),
	})
	public ResponseInfo getForm(@PathVariable("id") Integer id, HttpServletRequest request) throws GlobalException {
		CmsFormEntity formEntity = service.findById(id);
		/**未设置表单**/
		if(formEntity==null){
			throw new GlobalException(new SystemExceptionInfo(
					InteractErrorCodeEnum.FORM_NOT_EXIST.getDefaultMessage(),
					InteractErrorCodeEnum.FORM_NOT_EXIST.getCode()));
		}
		return new ResponseInfo(formEntity);
	}


	/**
	 * 查看表单
	 * @param request
	 * @param response
	 * @return
	 * @throws GlobalException
	 */
	@GetMapping(value = "/view/{id}")
	public ResponseInfo  pageView(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
		Integer views = service.getViewAndRefreshCache(id);
		return new ResponseInfo(views);
	}

	/**
	 * 提交表单
	 * @param formId 表单id
	 * @param request
	 * @param response
	 * @return
	 * @throws GlobalException
	 */
	@PostMapping
	public ResponseInfo  submitForm(Integer formId, HttpServletRequest request, HttpServletResponse response) throws GlobalException {
		CmsFormSubmitVo vo = formDataService.submitForm(formId,request,response);
		return new ResponseInfo(vo);
	}

}
