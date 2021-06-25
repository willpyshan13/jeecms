/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.util;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.system.domain.MessageTpl;
import com.jeecms.system.domain.MessageTplDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author: chenming
 * @date: 2020/6/11 16:10
 */
public class MessageTplUtil {

    /**
     * 源list集合是否包括目标list集合
     * @param sources   源list
     * @param targets   目标list
     * @return  boolean
     */
    public static boolean isPack(List<Integer> sources, List<Integer> targets) {
        // 如果源list集合或者目标list集合为空，直接判断为false
        if (CollectionUtils.isEmpty(sources) || CollectionUtils.isEmpty(targets)) {
            return false;
        }
        // 如果源list集合大小小于目标list集合大小直接判断为false
//        if (sources.size() < targets.size()) {
//            return false;
//        }
        // 差距
        int gap = Math.abs(targets.size() - sources.size());
        targets.removeAll(sources);
        // 源list集合删除目标list集合，比对之前的差距
        return gap == targets.size();
    }

    /**
     * 校验模板详情信息实体类参数
     *
     * @param tplDetails 模板详情信息实体类
     * @throws GlobalException 全局异常
     */
    public static void checkMessageTplDetailsParameter(MessageTplDetails tplDetails) throws GlobalException {
        // 如果模板详情是站内信或者邮箱，一定要保证标题和内容一定存在，否则抛出异常
        switch (tplDetails.getMesType()) {
            case MessageTplDetails.LETTER:
            case MessageTplDetails.MAIL:
                if (StringUtils.isBlank(tplDetails.getMesTitle())) {
                    throw new GlobalException(SettingErrorCodeEnum.MESTITLE_NOTNULL);
                }
                if (StringUtils.isBlank(tplDetails.getMesContent())) {
                    throw new GlobalException(SettingErrorCodeEnum.MESSAGECONTENT_NOTNULL);
                }
                break;
            // 如果模板详情是手机号一定要保证手机模板一定存在，否则抛出异常
            case MessageTplDetails.PHONE:
                if (tplDetails.getTplId() == null) {
                    throw new GlobalException(SettingErrorCodeEnum.TPLID_NOTNULL);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 校验消息模板关联的详细对象集合的数量：会员验证一定存在2个消息模板详情、用户消息一定存在3个消息模板详情
     * @param tplType           模板类型（1.会员验证 2.用户消息）
     * @param mesTypes          消息模板类型集合
     * @param tplDetailsList    消息模板集合
     * @param siteId            站点id
     */
    public static void checkMessageTplDetailsNum(Integer tplType, Set<Short> mesTypes, List<MessageTplDetails> tplDetailsList, Integer siteId) {
        switch (tplType) {
            case MessageTpl.TPL_TYPE_MEMBER_VALIDATE:
                if (mesTypes.size() < 2) {
                    addMessageTplDetails(tplDetailsList, mesTypes, siteId);
                }
                break;
            case MessageTpl.TPL_TYPE_USER_MESSAGE:
                if (mesTypes.size() < 3) {
                    addMessageTplDetails(tplDetailsList, mesTypes, siteId);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 新增消息模板详情对象
     * @param tplDetailsList    消息模板详情对象集合
     * @param mesTypes          消息模板类型集合
     * @param siteId            站点id
     */
    private static void addMessageTplDetails(List<MessageTplDetails> tplDetailsList, Set<Short> mesTypes, Integer siteId) {
        if (!mesTypes.contains(MessageTplDetails.LETTER)) {
            MessageTplDetails details = new MessageTplDetails(siteId, false, MessageTplDetails.LETTER);
            tplDetailsList.add(details);
        }
        if (!mesTypes.contains(MessageTplDetails.MAIL)) {
            MessageTplDetails details = new MessageTplDetails(siteId, false, MessageTplDetails.MAIL);
            tplDetailsList.add(details);
        }
        if (!mesTypes.contains(MessageTplDetails.PHONE)) {
            MessageTplDetails details = new MessageTplDetails(siteId, false, MessageTplDetails.PHONE);
            tplDetailsList.add(details);
        }
    }



}
