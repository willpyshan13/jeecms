package com.jeecms.interact.service.impl;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.interact.dao.CmsFormTypeDao;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormTypeEntity;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.interact.service.CmsFormTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单类型service实现类
 * @author: tom
 * @date: 2020/2/12 13:58
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CmsFormTypeServiceImpl extends BaseServiceImpl<CmsFormTypeEntity, CmsFormTypeDao,Integer> implements CmsFormTypeService {

    @Autowired
    private CmsFormService formService;

    /**默认取5000足以*/
    Paginable paginable = new PaginableRequest(0,5000);

    @Override
    @Transactional(rollbackFor = Exception.class,readOnly = true)
    public boolean checkByName(String name, Integer id) {
        if (StringUtils.isBlank(name)) {
            return true;
        }
        List<CmsFormTypeEntity> lists = dao.findByNameAndHasDeleted(name,false);
        if (lists == null || lists.size() == 0) {
            return true;
        } else {
            if (id == null) {
                return false;
            }
            return lists.get(0).getId().equals(id);
        }
    }

    @Override
    public void delete(boolean cascadeDelForm,Integer[]ids) throws GlobalException {
        for(Integer id:ids){
            List<CmsFormEntity>forms = formService.getList(null,null,id,null,null,paginable);
            if(cascadeDelForm){
                /**删除分组及分组内的所有表单（以及表单内的填写数据）；*/
                    formService.physicalDelete(CmsFormEntity.fetchIds(forms));
            }else{
                /**该分组内的表单变为未分组状态；*/
                for(CmsFormEntity formEntity:forms){
                    formEntity.setTypeId(null);
                    formEntity.setFormType(null);
                    formService.updateAll(formEntity);
                }
            }
        }
        physicalDelete(ids);
    }

}
