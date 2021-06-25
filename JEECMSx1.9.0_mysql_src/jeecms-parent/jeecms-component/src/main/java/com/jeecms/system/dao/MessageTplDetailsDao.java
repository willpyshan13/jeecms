/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.system.dao.ext.MessageTplDetailsExt;
import com.jeecms.system.domain.MessageTplDetails;

import java.util.List;

/**
 * 
 * 详细模版信息 dao接口
 * @author: wulongwei
 * @version 1.0
 * @date:   2019年4月26日 下午1:39:20
 */
public interface MessageTplDetailsDao extends IBaseDao<MessageTplDetails, Integer>, MessageTplDetailsExt {

    /**
     * 通过消息模板id和消息类型查询模板详情对象
     * @param mesTplId  消息模板id
     * @param mesType   消息类型(消息类型 1、站内信 2、邮件 3、手机)
     * @param hasDeleted 逻辑删除标识
     * @return MessageTplDetails
     */
    List<MessageTplDetails> findByMesTplIdAndMesTypeAndHasDeleted(Integer mesTplId, Short mesType,Boolean hasDeleted);

    /**
     * 通过消息模板id查询模板详情对象集合
     * @param mesTplId      消息模板id
     * @param hasDeleted    逻辑删除标识
     * @return  List<MessageTplDetails>
     */
    List<MessageTplDetails> findByMesTplIdAndHasDeleted(Integer mesTplId, Boolean hasDeleted);

    /**
     * 通过站点id集合查询未删除的模板详情对象集合
     * @param siteIds       站点id集合
     * @param hasDeleted    逻辑删除标识
     * @return List<MessageTplDetails>
     */
    List<MessageTplDetails> findBySiteIdInAndHasDeleted(List<Integer> siteIds,Boolean hasDeleted);

    /**
     * 通过站点id、手机模板id查询未删除的消息模板详情对象集合
     * @param siteId    站点id
     * @param tplId     手机模板id
     * @param hasDeleted    逻辑删除标识
     * @return List<MessageTplDetails>
     */
    List<MessageTplDetails> findBySiteIdAndTplIdAndHasDeleted(Integer siteId,String tplId,Boolean hasDeleted);
}