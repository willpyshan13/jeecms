package com.jeecms.admin.controller.auth;

import com.jeecms.auth.domain.CoreApi;
import com.jeecms.auth.service.CoreApiService;
import com.jeecms.common.annotation.EncryptMethod;
import com.jeecms.common.annotation.EncryptParam;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.base.domain.SortDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Api接口管理controller层
 * @author: chenming
 * @date: 2019年4月9日 下午3:39:54
 */
@RequestMapping(value = "/apis")
@RestController
public class CoreApiController extends BaseController<CoreApi, Integer> {

	@PostConstruct
	public void init() {
		String[] queryParams = { "apiName_LIKE", "apiUrl_LIKE" };
		super.setQueryParams(queryParams);
	}

	/**
	 * api列表含分页
	 */
	@EncryptMethod
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	@MoreSerializeField({ @SerializeField(clazz = CoreApi.class, includes = { "apiName", "apiUrl", "id", "perms",
			"requestMethod", "sortNum", "useScene" }) })
	public ResponseInfo page(HttpServletRequest request,
			@PageableDefault(sort = "id", direction = Direction.DESC) 
		Pageable pageable,@EncryptParam  String apiUrl,String apiName) throws GlobalException {
		ResponseInfo responseInfo = new ResponseInfo(service.getPage(apiUrl,apiName, pageable, false));
		return responseInfo;
	}

	/**
	 * api详情
	 */
	@RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
	@MoreSerializeField({ @SerializeField(clazz = CoreApi.class, includes = { "apiName", "apiUrl", "id", "perms",
			"requestMethod", "sortNum", "useScene" }) })
	@Override
	public ResponseInfo get(@PathVariable(name = "id") Integer id) throws GlobalException {
		return super.get(id);
	}

	/**
	 * api添加
	 */
	@EncryptMethod
	@RequestMapping(method = RequestMethod.POST)
	@Override
	public ResponseInfo save(@RequestBody @Valid CoreApi bean, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		Boolean urlResult = this.ckUrl(bean.getApiUrl(), bean.getRequestMethod(), null);
		if (!urlResult) {
			return new ResponseInfo(SettingErrorCodeEnum.API_URL_ALREADY_EXIST.getCode(),
					SettingErrorCodeEnum.API_URL_ALREADY_EXIST.getDefaultMessage());
		}
		service.save(bean);
		return new ResponseInfo();
	}

	/**
	 * api修改
	 */
	@EncryptMethod
	@RequestMapping(method = RequestMethod.PUT)
	@Override
	public ResponseInfo update(@RequestBody @Valid CoreApi bean, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		Boolean urlResult = this.ckUrl(bean.getApiUrl(), bean.getRequestMethod(), bean.getId());
		if (!urlResult) {
			return new ResponseInfo(SettingErrorCodeEnum.API_URL_ALREADY_EXIST.getCode(),
					SettingErrorCodeEnum.API_URL_ALREADY_EXIST.getDefaultMessage());
		}
		service.update(bean);
		return new ResponseInfo();
	}

	/**
	 * 检查api地址是否唯一
	 */
	@EncryptMethod
	@RequestMapping(value = "/path/unique", method = RequestMethod.GET)
	public ResponseInfo checkRouting(@EncryptParam @RequestParam String url, @RequestParam Short requestMethod,
			@RequestParam(name = "id", required = false) Integer id) throws GlobalException {
		Boolean result = this.ckUrl(url, requestMethod, id);
		return new ResponseInfo(result);
	}

	/**
	 * 保存排序
	 */
	@RequestMapping(value = "/sort", method = RequestMethod.PUT)
	public ResponseInfo sort(HttpServletRequest request, @RequestBody(required = false) @Valid SortDto sort,
			BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		service.sort(sort);
		return new ResponseInfo();
	}

	/**
	 * api删除
	 */
	@Override
	@PostMapping("/delete")
	public ResponseInfo delete(@RequestBody @Valid DeleteDto dels, BindingResult result) throws GlobalException {
		super.validateBindingResult(result);
		return super.deleteBeatch(dels.getIds());
	}

	/**
	 * 检查url是否唯一
	 */
	private Boolean ckUrl(String url, Short requestMethod, Integer apiId) throws GlobalException {
		CoreApi coreApi = service.findByUrl(url, requestMethod);
		if (coreApi == null) {
			return true;
		}
		if (apiId == null) {
			return false;
		} else {
			return coreApi.getId().equals(apiId);
		}
	}

	@Autowired
	private CoreApiService service;
}
