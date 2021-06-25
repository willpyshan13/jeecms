package com.jeecms.interact.service.impl;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.interact.dao.CmsFormItemDao;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.service.CmsFormItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 信件类型service实现类
 * @author: tom
 * @date: 2020/1/4 13:58   
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CmsFormItemServiceImpl extends BaseServiceImpl<CmsFormItemEntity, CmsFormItemDao,Integer> implements CmsFormItemService {
    @Override
    public void deleteByFormId(Integer formId) {
        dao.deleteByFormId(formId);
    }
}
