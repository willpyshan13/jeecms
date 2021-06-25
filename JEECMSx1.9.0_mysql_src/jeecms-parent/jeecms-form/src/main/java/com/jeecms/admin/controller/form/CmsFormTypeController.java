/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.form;

import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.exception.error.InteractErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.form.domain.dto.CmsFormTypeDelDto;
import com.jeecms.interact.domain.CmsFormTypeEntity;
import com.jeecms.interact.domain.vo.CmsFormTypeVo;
import com.jeecms.interact.domain.vo.CmsFormTypeVoSumary;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.interact.service.CmsFormTypeService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.ContentMark;
import com.jeecms.util.SystemContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 信箱类型控制器
 * @author: tom
 * @date:
 */
@RequestMapping("/formType")
@RestController
public class CmsFormTypeController extends BaseController<CmsFormTypeEntity, Integer> {

    @Autowired
    private CmsFormTypeService service;
    @Autowired
    private CmsFormService formService;
    private final transient ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        String[] queryParams = {};
        super.setQueryParams(queryParams);
    }


    /**
     * @Title: 列表
     * @param: @param request
     * @param: @param pageable
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    @MoreSerializeField({
            @SerializeField(clazz = CmsFormTypeEntity.class, excludes = {"site"}),
    })
    @GetMapping(value = "/list")
    public ResponseInfo getTypes(HttpServletRequest request) {
        Integer siteId = SystemContextUtils.getSiteId(request);
        Map<String, String[]> params = new HashMap<String, String[]>(1);
        params.put("EQ_siteId_Integer", new String[]{siteId.toString()});
        Paginable paginable = new PaginableRequest(0, 1000);
        paginable.setSort(Sort.by(Sort.Order.asc("id")));
        List<CmsFormTypeEntity> list = service.getList(params, paginable, true);
        Long formTotalCount = 0L;
        CmsFormTypeVoSumary voSumary = new CmsFormTypeVoSumary();
        List<CmsFormTypeVo> forms = new ArrayList<>();
        voSumary.setForms(forms);
        /**未分组表单数量*/
        Long notGroupFormCount = formService.getCount(CmsFormConstant.FORM_SCENE_FORM, siteId, 0, null, null);
        for (CmsFormTypeEntity typeEntity : list) {
            CmsFormTypeVo vo = new CmsFormTypeVo();
            MyBeanUtils.copyProperties(typeEntity, vo);
            Integer formCount = typeEntity.getForms().size();
            vo.setFormCount(formCount);
            formTotalCount += formCount;
            forms.add(vo);
        }
        formTotalCount += notGroupFormCount;
        voSumary.setFormTotalCount(formTotalCount);
        voSumary.setNotGroupFormCount(notGroupFormCount);
        return new ResponseInfo(voSumary);
    }


    /**
     * 获取详情
     *
     * @Title: 获取详情
     * @param: @param id
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    @MoreSerializeField({
            @SerializeField(clazz = CmsFormTypeEntity.class, excludes = {"site"}),
    })
    @GetMapping(value = "/{id:[0-9]+}")
    public ResponseInfo get(@PathVariable("id") Integer id) {
        return new ResponseInfo(service.findById(id));
    }

    /**
     * 校验是否唯一
     *
     * @param name 名称
     * @param id       id
     * @return true 唯一 false 不唯一
     */
    @GetMapping("/unique")
    public ResponseInfo unique(String name, Integer id) {
        return new ResponseInfo(service.checkByName(name, id));
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
    public ResponseInfo save(@RequestBody @Valid CmsFormTypeEntity bean,
                             BindingResult result, HttpServletRequest request) throws GlobalException {
        validateBindingResult(result);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (!service.checkByName(bean.getName(), bean.getId())) {
                throw new GlobalException(new SystemExceptionInfo(InteractErrorCodeEnum.NAME_EXIST_ERROR.getDefaultMessage(),
                        InteractErrorCodeEnum.NAME_EXIST_ERROR.getCode()));
            }
            bean.init();
            CmsSite site = SystemContextUtils.getSite(request);
            bean.setSite(site);
            bean.setSiteId(site.getId());
            /**不允许重名，并发压测下需要加锁*/
            service.save(bean);
        } finally {
            lock.unlock();
        }
        return new ResponseInfo();
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
    public ResponseInfo update(@RequestBody @Valid CmsFormTypeEntity bean,
                               BindingResult result) throws GlobalException {
        validateId(bean.getId());
        validateBindingResult(result);
        if (!service.checkByName(bean.getName(), bean.getId())) {
            throw new GlobalException(new SystemExceptionInfo(InteractErrorCodeEnum.NAME_EXIST_ERROR.getDefaultMessage(),
                    InteractErrorCodeEnum.NAME_EXIST_ERROR.getCode()));
        }
        service.update(bean);
        return new ResponseInfo();
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
    public ResponseInfo delete(@RequestBody @Valid CmsFormTypeDelDto delDto,
                               BindingResult result) throws GlobalException {
        validateBindingResult(result);
        service.delete(delDto.getCascadeDelForm(), delDto.getIds());
        return new ResponseInfo();
    }

}



