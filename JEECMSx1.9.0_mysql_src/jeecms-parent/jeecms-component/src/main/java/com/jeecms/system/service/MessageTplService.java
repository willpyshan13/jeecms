/*
 *  * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.system.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.system.domain.MessageTpl;
import com.jeecms.system.domain.MessageTplDetails;
import com.jeecms.system.domain.dto.MessageTplDetailsStatusDto;
import com.jeecms.system.domain.dto.SynchChildSiteTplDto;
import com.jeecms.system.domain.vo.SynchPlatformTplVo;

import java.util.List;

/**
 * 模版信息service接口
 *
 * @version 1.0
 * @author: wulongwei
 * @date: 2019年4月26日 下午1:40:50
 */
public interface MessageTplService extends IBaseService<MessageTpl, Integer> {

    /**
     * 新增消息模板
     *
     * @param messageTpl 模板信息对象
     * @throws GlobalException 全局异常
     */
    void saveMessageTpl(MessageTpl messageTpl, Integer siteId) throws GlobalException;

    /**
     * 修改消息模板
     *
     * @param messageTpl 消息模板对象
     * @throws GlobalException 全局异常
     */
    void updateMessageTpl(MessageTpl messageTpl) throws GlobalException;

    /**
     * 校验模板code是否唯一
     *
     * @param mesCode 模板唯一标识
     * @return Boolean
     */
    Boolean checkMesCode(String mesCode,Integer siteId);

    /**
     * 获取单个消息模板(邮件、短信、站内信)详情对象
     *
     * @param mesTplId 消息模板id
     * @param mesType  消息类型(消息类型 1、站内信 2、邮件 3、手机)
     * @return MessageTplDetails
     */
    MessageTplDetails getMessageTplDetails(Integer mesTplId, Short mesType);

    /**
     * 修改单个修改模板详情(邮件、短信、站内信)的状态
     *
     * @param dto 修改模板详情的状态dto
     * @throws GlobalException 全局异常
     */
    void updateMessageTplDetailsStatus(MessageTplDetailsStatusDto dto) throws GlobalException;

    /**
     * 通过模板唯一标识查询模板对象
     *
     * @param mesCode 模板唯一标识
     * @return MessageTpl
     */
    MessageTpl findByMesCode(String mesCode,Integer siteId);

    /**
     * 同步子站模板
     *
     * @param vos 云平台同步子站模板返回VO
     * @throws GlobalException 全局异常
     */
    void synchronizeChildSiteTpl(List<SynchPlatformTplVo> vos, List<Integer> childSiteIds, Integer siteId, Boolean platformSuccess) throws GlobalException;

    /**
     * 校验子站点(校验子站点id是否在站点id下方)
     *
     * @param sourceList 来源站点id(子站点id)
     * @param siteId     父站点(当前站点id)
     * @throws GlobalException 全局异常
     */
    void checkChildSite(List<Integer> sourceList, Integer siteId) throws GlobalException;
}
