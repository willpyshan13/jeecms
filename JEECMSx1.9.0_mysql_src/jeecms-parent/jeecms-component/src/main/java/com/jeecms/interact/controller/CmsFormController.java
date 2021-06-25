/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.interact.controller;

import com.alibaba.fastjson.JSON;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.InteractErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.domain.CmsFormTypeEntity;
import com.jeecms.interact.domain.dto.CmsFormFastDto;
import com.jeecms.interact.domain.dto.CmsFormFastEditDto;
import com.jeecms.interact.domain.dto.CmsFormPublishDto;
import com.jeecms.interact.domain.dto.CmsFormSimpleFastDto;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.interact.service.CmsFormTypeService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 表单类型控制器基类
 * @author: tom
 * @date: 2020/1/6 11:36
 */
public class CmsFormController extends BaseController<CmsFormEntity, Integer> {

    @Autowired
    private CmsFormService service;
    @Autowired
    private CmsFormTypeService formTypeService;
    @Autowired
    private ResourcesSpaceDataService resourcesSpaceDataService;

    private final transient ReentrantLock lock = new ReentrantLock();

    /**1领导信箱 2智能表单*/
    private Short formScene;

    public Short getFormScene() {
        return formScene;
    }

    public void setFormScene(Short formScene) {
        this.formScene = formScene;
    }

    @PostConstruct
    public void init() {
        String[] queryParams = {};
        super.setQueryParams(queryParams);
    }

    @MoreSerializeField({
            @SerializeField(clazz = CmsFormEntity.class, includes = {"id", "title", "description", "createTime", "createUser",
                    "formType", "joinCount", "status", "viewStatus"}),
            @SerializeField(clazz = CmsFormTypeEntity.class, includes = {"id", "name"}),
    })
    public ResponseInfo getPage(String name, Integer siteId, Integer typeId, Short status,
                                @PageableDefault(sort = {"id"},
                                        direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CmsFormEntity> page = service.getPage(getFormScene(), siteId, typeId, status, name, pageable);
        return new ResponseInfo(page);
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
            @SerializeField(clazz = CmsFormEntity.class, excludes = {"bgImgId", "headImgId", "bgImg", "headImg", "site"}),
            @SerializeField(clazz = CmsFormItemEntity.class, excludes = {"form"}),
            @SerializeField(clazz = ResourcesSpaceData.class, includes = {"id", "url"}),
            @SerializeField(clazz = CmsFormTypeEntity.class, includes = {"id", "name"}),
    })
    public ResponseInfo get(@PathVariable("id") Integer id) {
        return new ResponseInfo(service.findById(id));
    }

    /**
     * 校验是否唯一
     *
     * @param title 名称
     * @param id       id
     * @param siteId 站点id
     * @return true 唯一 false 不唯一
     */
    public ResponseInfo unique(String title, Integer id, Integer siteId) {
        return new ResponseInfo(service.checkByName(getFormScene(), title, id, siteId));
    }

    /**
     * 添加
     *
     * @Title: 添加
     * @param: @param result
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    public ResponseInfo save(@RequestBody @Valid CmsFormFastDto dto,
                             BindingResult result, HttpServletRequest request) throws GlobalException {
        validateBindingResult(result);
        CmsFormEntity entity = new CmsFormEntity();
        final ReentrantLock lock = this.lock;
        lock.lock();
        /**不允许重名，并发压测下需要加锁*/
        try {
            MyBeanUtils.copyProperties(dto, entity);
            if (!service.checkByName(getFormScene(), entity.getTitle(), null, dto.getSiteId())) {
                return new ResponseInfo(InteractErrorCodeEnum.NAME_EXIST_ERROR.getCode(),
                        InteractErrorCodeEnum.NAME_EXIST_ERROR.getDefaultMessage());
            }
            entity.init();
            entity.setFormScene(getFormScene());
            if (dto.getTypeId() != null && !dto.getTypeId().equals(0)) {
                entity.setTypeId(dto.getTypeId());
                entity.setFormType(formTypeService.findById(dto.getTypeId()));
            }
            CmsSite site = SystemContextUtils.getSite(request);
            entity.setSite(site);
            entity.setSiteId(site.getId());
            entity = service.save(entity);
            if (CmsFormConstant.FORM_SCENE_LETTER.equals(getFormScene())) {
                /**领导信箱的表单需要初始化标题和内容的JSON字段信息 否则如果客户未保存则前台展示有问题*/
                entity.setFields(JSON.parseArray(CmsFormConstant.LETTER_FORM_INIT_FIELD_JSON));
                service.updateFields(entity);
            }
        } finally {
            lock.unlock();
        }
        return new ResponseInfo(entity.getId());
    }

    /**
     * 复制
     *
     * @Title: 复制
     * @param: @param result
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    public ResponseInfo copy(@RequestBody @Valid CmsFormFastEditDto dto,
                             BindingResult result) throws GlobalException {
        validateBindingResult(result);
        if (!service.checkByName(getFormScene(), dto.getTitle(), null, dto.getSiteId())) {
            return new ResponseInfo(InteractErrorCodeEnum.NAME_EXIST_ERROR.getCode(),
                    InteractErrorCodeEnum.NAME_EXIST_ERROR.getDefaultMessage());
        }
        CmsFormEntity formEntity = service.copy(dto);
        return new ResponseInfo(formEntity.getId());
    }


    /**
     * 修改
     *
     * @Title: 修改
     * @param: @param result
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    public ResponseInfo update(@RequestBody @Valid CmsFormFastEditDto dto,
                               BindingResult result) throws GlobalException {
        validateId(dto.getId());
        validateBindingResult(result);
        if (dto.getId() != null) {
            CmsFormEntity entity = service.findById(dto.getId());
            if (StringUtils.isNotBlank(dto.getTitle())) {
                /**表单设置 忽略标题和描述*/
                MyBeanUtils.copyProperties(dto, entity, "status","typeId", "formType", "shareLogo", "coverPic");
            } else {
                /**快速编辑*/
                MyBeanUtils.copyProperties(dto, entity, "status","typeId", "formType", "shareLogo", "coverPic", "title", "description");
            }
            if (!service.checkByName(getFormScene(), dto.getTitle(), dto.getId(), dto.getSiteId())) {
                return new ResponseInfo(InteractErrorCodeEnum.NAME_EXIST_ERROR.getCode(),
                        InteractErrorCodeEnum.NAME_EXIST_ERROR.getDefaultMessage());
            }
            entity.setFormScene(getFormScene());
            if (dto.getSubmitLimitLogin() != null) {
                if (dto.getCoverPicId() != null) {
                    entity.setCoverPic(resourcesSpaceDataService.findById(dto.getCoverPicId()));
                    entity.setCoverPicId(dto.getCoverPicId());
                } else {
                    entity.setCoverPicId(null);
                    entity.setCoverPic(null);
                }
                if (dto.getShareLogoId() != null) {
                    entity.setShareLogo(resourcesSpaceDataService.findById(dto.getShareLogoId()));
                    entity.setShareLogoId(dto.getShareLogoId());
                } else {
                    entity.setShareLogo(null);
                    entity.setShareLogoId(null);
                }
                if (dto.getOnlyWechat() != null) {
                    entity.setIsOnlyWechat(dto.getOnlyWechat());
                }
                if (entity.getSubmitLimitLogin() == null) {
                    entity.setSubmitLimitLogin(false);
                }
                if (entity.getIsCaptcha() == null) {
                    entity.setIsCaptcha(false);
                }
                service.updateAll(entity);
            } else {
                /**可能存在脏数据*/
                if (entity.getIsOnlyWechat() != null) {
                    entity.setIsOnlyWechat(false);
                }
                if (entity.getSubmitLimitLogin() == null) {
                    entity.setSubmitLimitLogin(false);
                }
                if (entity.getIsCaptcha() == null) {
                    entity.setIsCaptcha(false);
                }
                /**修改分组可以修改为未分组*/
                if (dto.getTypeId() != null) {
                    entity.setTypeId(dto.getTypeId());
                    if (!dto.getTypeId().equals(0)) {
                        entity.setFormType(formTypeService.findById(dto.getTypeId()));
                    } else {
                        entity.setFormType(null);
                        entity.setTypeId(null);
                    }
                    service.updateAll(entity);
                } else {
                    service.update(entity);
                }
            }
        }
        return new ResponseInfo(dto.getId());
    }

    /**
     * 修改
     *
     * @Title: 修改
     * @param: @param result
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    public ResponseInfo simpleUpdate(@RequestBody @Valid CmsFormSimpleFastDto dto,
                                     BindingResult result) throws GlobalException {
        validateId(dto.getId());
        validateBindingResult(result);
        Integer originTypeId = 0;
        if (dto.getId() != null) {
            CmsFormEntity entity = service.findById(dto.getId());
            if (entity.getFormType() != null) {
                originTypeId = entity.getFormType().getId();
            }
            if (StringUtils.isNotBlank(dto.getTitle())) {
                /**表单设置 忽略标题和描述*/
                MyBeanUtils.copyProperties(dto, entity, "status","typeId", "formType", "shareLogo", "coverPic");
            } else {
                /**快速编辑*/
                MyBeanUtils.copyProperties(dto, entity, "status","typeId", "formType", "shareLogo", "coverPic", "title", "description");
            }
            if (!service.checkByName(getFormScene(), dto.getTitle(), dto.getId(), dto.getSiteId())) {
                return new ResponseInfo(InteractErrorCodeEnum.NAME_EXIST_ERROR.getCode(),
                        InteractErrorCodeEnum.NAME_EXIST_ERROR.getDefaultMessage());
            }
            /**修改分组可以修改为未分组*/
            if (dto.getTypeId() != null) {
                entity.setTypeId(dto.getTypeId());
                if (!dto.getTypeId().equals(0)) {
                    entity.setFormType(formTypeService.findById(dto.getTypeId()));
                } else {
                    entity.setFormType(null);
                    entity.setTypeId(null);
                }
                service.updateAll(entity);
            } else {
                if (originTypeId != 0) {
                    entity.setFormType(formTypeService.findById(originTypeId));
                    entity.setTypeId(originTypeId);
                }
            }
            service.update(entity);
        }
        return new ResponseInfo(dto.getId());
    }

    public ResponseInfo publish(@RequestBody @Valid CmsFormPublishDto dto,
                                BindingResult result) throws GlobalException {
        validateBindingResult(result);
        service.publish(dto);
        return new ResponseInfo();
    }

    /**
     * 修改字段
     * @Title:
     * @param: @param result
     * @param: @throws GlobalException
     * @return: ResponseInfo
     */
    public ResponseInfo updateFields(@RequestBody CmsFormEntity dto,
                                     BindingResult result) throws GlobalException {
        validateBindingResult(result);
        service.updateFields(dto);
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
    public ResponseInfo delete(@RequestBody @Valid DeleteDto dels,
                               BindingResult result) throws GlobalException {
        validateBindingResult(result);
        service.delete(dels.getIds());
        return new ResponseInfo();
    }

}



