package com.jeecms.interact.service;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.page.Paginable;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.dto.CmsFormFastEditDto;
import com.jeecms.interact.domain.dto.CmsFormPublishDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 表单service接口
 * @author: tom
 * @date:
 */
public interface CmsFormService   extends IBaseService<CmsFormEntity, Integer> {

    boolean checkByName(Short scene, String name, Integer id, Integer siteId);

    CmsFormEntity updateFields(CmsFormEntity dto) throws GlobalException;

    CmsFormEntity getVo(Integer id);

    void publish(CmsFormPublishDto dto) throws GlobalException;

    Page<CmsFormEntity> getPage(Short scene, Integer siteId, Integer typeId, Short status, String name, Pageable pageable);


    List<CmsFormEntity> getList(Short scene, Integer siteId, Integer typeId, Short status, String name, Paginable paginable);

    /**
     * 查询数量
     * @param scene 场景 应用场景-1领导信箱 应用场景-2智能表单
     * @param siteId 站点id
     * @param typeId 类型id 0是未分组的表单
     * @param status 状态
     * @param name 名称
     * @return
     */
    Long getCount(Short scene, Integer siteId, Integer typeId, Short status, String name);

    CmsFormEntity copy(CmsFormFastEditDto dto) throws GlobalException;


    Integer getViewAndRefreshCache(Integer id);

}
