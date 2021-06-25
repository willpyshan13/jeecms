/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.system.domain.MessageTpl;

import java.lang.String;
import java.util.List;

/**
 * 模板信息 dao接口
 * @author: wulongwei
 * @version 1.0
 * @date:   2019年4月26日 下午12:00:13
 */
public interface MessageTplDao extends IBaseDao<MessageTpl, Integer> {

    /**
     * 通过模板唯一标识查询是否有未删除的模板实体类
     * @param mesCode       模板code模板唯一标识
     * @param hasDeleted    逻辑删除标识
     * @return  模版信息实体类
     */
    List<MessageTpl> findByMesCodeAndSiteIdAndHasDeleted(String mesCode,Integer siteId,Boolean hasDeleted);

    /**
     * 通过站点id查询是否有未删除的模板实体集合
     * @param siteId        站点id
     * @param hasDeleted    逻辑删除标识
     * @return List<MessageTpl>
     */
    List<MessageTpl> findBySiteIdInAndHasDeleted(List<Integer> siteIds,Boolean hasDeleted);
}