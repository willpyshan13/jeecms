/*
 *  * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.system.domain.MessageTplDetails;

import java.util.List;

/**
 * 模版详细信息service接口
 *
 * @version 1.0
 * @author: wulongwei
 * @date: 2019年4月26日 下午1:41:09
 */
public interface MessageTplDetailsService extends IBaseService<MessageTplDetails, Integer> {


    /**
     * 获取消息模板详情
     *
     * @param mesCode       消息模板标识
     * @param detailMesType 消息类型(消息类型 1、站内信 2、邮件 3、手机)
     * @return MessageTplDetails
     */
    MessageTplDetails findByCodeAndType(String mesCode, Short detailMesType,Integer siteId) throws GlobalException;

    /**
     * 根据消息模板id和消息类型查询消息模板对象
     *
     * @param mesTplId 消息模板id
     * @param mesType  消息类型(消息类型 1、站内信 2、邮件 3、手机)
     * @return MessageTplDetails
     */
    MessageTplDetails findByMesTplIdAndMesType(Integer mesTplId, Short mesType);

    /**
     * 根据消息模板id查询消息模板详细对象集合
     *
     * @param mesTplId 消息模板id
     * @return List<MessageTplDetails>
     */
    List<MessageTplDetails> findByMesTplId(Integer mesTplId);

    /**
     * 通过站点id集合查询消息模板详情对象集合
     *
     * @param siteIds 站点id集合
     * @return List<MessageTplDetails>
     */
    List<MessageTplDetails> findBySiteIdIn(List<Integer> siteIds);

    /**
     * 通过短信模板id和站点id查询该站点下是否使用了该短信模板
     *
     * @param tplId  短信模板id
     * @param siteId 站点id
     * @return Boolean true->拥有、false->没有拥有
     */
    Boolean haveTplId(String tplId, Integer siteId);

}
