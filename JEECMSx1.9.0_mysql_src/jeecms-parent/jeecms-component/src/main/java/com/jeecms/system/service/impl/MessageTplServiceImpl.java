/*
 *  * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.jeecms.common.exception.error.SiteErrorCodeEnum;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.dto.MessageTplDetailsStatusDto;
import com.jeecms.system.domain.vo.SynchPlatformTplVo;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.util.MessageTplUtil;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.system.dao.MessageTplDao;
import com.jeecms.system.domain.MessageTpl;
import com.jeecms.system.domain.MessageTplDetails;
import com.jeecms.system.service.MessageTplDetailsService;
import com.jeecms.system.service.MessageTplService;
import org.springframework.util.CollectionUtils;

/**
 * 消息模板service层实现
 *
 * @version 1.0
 * @author: wulongwei
 * @date: 2019年4月26日 上午9:55:44
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MessageTplServiceImpl extends BaseServiceImpl<MessageTpl, MessageTplDao, Integer>
        implements MessageTplService {

    @Override
    public void saveMessageTpl(MessageTpl messageTpl, Integer siteId) throws GlobalException {
        List<MessageTplDetails> tplDetailsList = messageTpl.getDetails();
        // 获取到消息类型集合(去除重复的)
        Set<Short> mesTypes = tplDetailsList.stream().map(MessageTplDetails::getMesType).collect(Collectors.toSet());
        MessageTplUtil.checkMessageTplDetailsNum(messageTpl.getTplType(), mesTypes, tplDetailsList, siteId);
        MessageTpl bean = super.save(messageTpl);
        for (MessageTplDetails tplDetails : tplDetailsList) {
            tplDetails.setMesTplId(bean.getId());
            tplDetails.setSiteId(siteId);
            // 如果消息模板开启那么应当进行校验参数
            if (tplDetails.getIsOpen() != null && tplDetails.getIsOpen()) {
                MessageTplUtil.checkMessageTplDetailsParameter(tplDetails);
                // 没有开启那么无论传入什么直接置空
            } else {
                tplDetails.setTplId(null);
                tplDetails.setMesTitle(null);
                tplDetails.setMesContent(null);
                tplDetails.setExtendedField(null);
                tplDetails.setTplName(null);
            }
            tplDetails.setMessageTpl(bean);
        }
        List<MessageTplDetails> messageTplDetailsList = messageTplDetailsService.saveAll(tplDetailsList);
        bean.setDetails(messageTplDetailsList);
    }


    @Override
    public void updateMessageTpl(MessageTpl messageTpl) throws GlobalException {
        Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
        messageTpl.setSiteId(siteId);
        MessageTpl bean = super.updateAll(messageTpl);
        for (MessageTplDetails tplDetil : messageTpl.getDetails()) {
            // 无论传入的站点id是啥直接置为当前站点
            tplDetil.setSiteId(siteId);
            // 如果开启校验参数
            if (tplDetil.getIsOpen()) {
                MessageTplUtil.checkMessageTplDetailsParameter(tplDetil);
            }
            tplDetil.setMessageTpl(bean);
        }
        List<MessageTplDetails> tplDetailsList = Lists.newArrayList(messageTplDetailsService.batchUpdateAll(messageTpl.getDetails()));
        bean.getDetails().clear();
        bean.setDetails(tplDetailsList);
    }

    @Override
    public Boolean checkMesCode(String mesCode,Integer siteId) {
        MessageTpl messageTpl = this.findByMesCode(mesCode, siteId);
        if (messageTpl != null) {
            return true;
        }
        return false;
    }

    @Override
    public MessageTplDetails getMessageTplDetails(Integer mesTplId, Short mesType) {
        return messageTplDetailsService.findByMesTplIdAndMesType(mesTplId, mesType);
    }

    @Override
    public void updateMessageTplDetailsStatus(MessageTplDetailsStatusDto dto) throws GlobalException {
        MessageTpl messageTpl = super.findById(dto.getId());
        // 消息模板id传入为空直接抛出异常
        if (messageTpl == null) {
            throw new GlobalException(SettingErrorCodeEnum.MESSAGE_TPL_ID_INCOMING_ERROR);
        }
        // 传入的消息模板详细对象集合
        MessageTplDetails incomingTplDetails = dto.getTplDetails();
        // 通过消息模板id和消息模板类型查询出消息模板详情对象
        MessageTplDetails tplDetails = messageTplDetailsService.findByMesTplIdAndMesType(dto.getId(), dto.getMesType());
        // 如果消息模板为空那么说名新增修改的部分逻辑有问题，直接新增一个，弥补这个问题
        if (tplDetails == null) {
            Integer siteId = SystemContextUtils.getSiteId(RequestUtils.getHttpServletRequest());
            tplDetails = new MessageTplDetails(siteId, dto.getId(), dto.getOpen(), dto.getMesType(), incomingTplDetails.getTplId(), incomingTplDetails.getMesTitle(), incomingTplDetails.getMesContent(), incomingTplDetails.getExtendedField(), incomingTplDetails.getTplName());
            tplDetails = messageTplDetailsService.save(tplDetails);
            super.flush();
            messageTpl.getDetails().add(tplDetails);
        } else {
            // 如果该消息类型开启直接从参数中取值
            if (dto.getOpen()) {
                tplDetails.setIsOpen(true);
                tplDetails.setTplId(incomingTplDetails.getTplId());
                tplDetails.setTplName(incomingTplDetails.getTplName());
                tplDetails.setMesTitle(incomingTplDetails.getMesTitle());
                tplDetails.setMesContent(incomingTplDetails.getMesContent());
                tplDetails.setExtendedField(incomingTplDetails.getExtendedField());
                MessageTplUtil.checkMessageTplDetailsParameter(tplDetails);
                // 如果关闭，直接关闭，不改动任何参数
            } else {
                tplDetails.setIsOpen(false);
            }
            messageTplDetailsService.updateAll(tplDetails);
        }
    }

    @Override
    public MessageTpl findByMesCode(String mesCode,Integer siteId) {
        List<MessageTpl> tpls = dao.findByMesCodeAndSiteIdAndHasDeleted(mesCode, siteId,false);
        if (CollectionUtils.isEmpty(tpls)) {
            return null;
        }
        return tpls.get(0);
    }

    @Override
    public void synchronizeChildSiteTpl(List<SynchPlatformTplVo> vos, List<Integer> childSiteIds, Integer siteId, Boolean platformSuccess) throws GlobalException {
        // 如果传入的是子站id集合不为空，直接删除该子站底下的消息模板
        if (!CollectionUtils.isEmpty(childSiteIds)) {
            this.deleteTpl(childSiteIds);
        }
        // 查询出父站点的所有消息模板集合
        List<MessageTpl> messageTpls = dao.findBySiteIdInAndHasDeleted(Collections.singletonList(siteId), false);
        // 如果消息模板集合不为空(为空就不要继续处理了啊)
        if (!CollectionUtils.isEmpty(messageTpls)) {
            Map<Integer, Map<String, String>> siteMap = new HashMap<>();
            if (platformSuccess) {
                // 通过站点分组，然后分组中再将(来源手机模板id->返回的消息模板id)转换成map
                siteMap = vos.stream().collect(Collectors.groupingBy(SynchPlatformTplVo::getSiteId, Collectors.toMap(SynchPlatformTplVo::getSourceTplId, SynchPlatformTplVo::getTargetTplId)));
            }
            for (Integer childSiteId : childSiteIds) {
                // 遍历子站然后新增处理消息模板
                this.saveMessageTpl(childSiteId, messageTpls, siteMap.get(childSiteId), platformSuccess);
            }
        }
    }

    /**
     * 通过站点id删除该站点集合下的消息模板
     *
     * @param childSiteIds 子站点id集合
     * @throws GlobalException 全局异常
     */
    private void deleteTpl(List<Integer> childSiteIds) throws GlobalException {
        List<MessageTpl> messageTpls = dao.findBySiteIdInAndHasDeleted(childSiteIds, false);
        if (!CollectionUtils.isEmpty(messageTpls)) {
            super.delete(messageTpls);
        }
        List<MessageTplDetails> tplDetailsList = messageTplDetailsService.findBySiteIdIn(childSiteIds);
        if (!CollectionUtils.isEmpty(tplDetailsList)) {
            messageTplDetailsService.delete(tplDetailsList);
        }
    }

    /**
     * 同步消息模板-增加消息模板
     *
     * @param childSiteId     子站点id
     * @param messageTpls     消息模板集合
     * @param phoneTplMap     手机模板Map(key->原来的手机模板id，value->同步后的模板id)
     * @param platformSuccess 云平台是否同步成功
     * @throws GlobalException 全局异常
     */
    private void saveMessageTpl(Integer childSiteId, List<MessageTpl> messageTpls, Map<String, String> phoneTplMap, boolean platformSuccess) throws GlobalException {
        for (MessageTpl messageTpl : messageTpls) {
            // 新增一个消息模板对象
            MessageTpl tpl = new MessageTpl(childSiteId, messageTpl.getMesTitle(), messageTpl.getMesCode(), messageTpl.getRemark(), messageTpl.getTplType());
            tpl = super.save(tpl);
            super.flush();
            // 查询出需要同步的消息模板对象关联的消息模板详情对象集合(老的消息模板详情对q象集合)
            List<MessageTplDetails> messageTplDetailsList = messageTplDetailsService.findByMesTplId(messageTpl.getId());
            List<MessageTplDetails> newTplDetailsList = new ArrayList<>(messageTplDetailsList.size());
            // 同步老的消息模板详情对象集合
            for (MessageTplDetails tplDetails : messageTplDetailsList) {
                MessageTplDetails details = null;
                // 如果是手机号需要特殊处理：1.云平台同步成功且返回的map中有原来的手机模板id，将父消息模板重置到子站点消息模板
                //                        2. 云平台同步失败，或者返回的手机模板id不存在，将其它参数置空，然后设置为false
                if (MessageTplDetails.PHONE == tplDetails.getMesType()) {
                    details = new MessageTplDetails(childSiteId,tpl.getId(), false, tplDetails.getMesType());
                    if (platformSuccess) {
                        if (StringUtils.isNotBlank(phoneTplMap.get(tplDetails.getTplId()))) {
                            details.setIsOpen(tplDetails.getIsOpen());
                            details.setTplId(phoneTplMap.get(tplDetails.getTplId()));
                            details.setTplName(tplDetails.getTplName());
                        }
                    }
                } else {
                    details = new MessageTplDetails(childSiteId, tpl.getId(), tplDetails.getIsOpen(), tplDetails.getMesType(),
                            tplDetails.getTplId(), tplDetails.getMesTitle(), tplDetails.getMesContent(), tplDetails.getExtendedField(),
                            tplDetails.getTplName());
                }
                newTplDetailsList.add(details);
            }
            newTplDetailsList = messageTplDetailsService.saveAll(newTplDetailsList);
            tpl.setDetails(newTplDetailsList);
        }
    }

    @Override
    public void checkChildSite(List<Integer> sourceList, Integer siteId) throws GlobalException {
        CmsSite cmsSite = cmsSiteService.findById(siteId);
        // 取出该站点下方所有子站点id集合
        List<Integer> childIdList = cmsSite.getChildIdList();
        // 子站id集合是否包含传入的来源站点id(子站点id)集合，如果不是抛出异常
        if (!MessageTplUtil.isPack(sourceList, childIdList)) {
            throw new GlobalException(SiteErrorCodeEnum.CHILD_SITE_IS_NOT_UNDER_THE_CURRENT_SITE);
        }
    }

    @Autowired
    private MessageTplDetailsService messageTplDetailsService;
    @Autowired
    private CmsSiteService cmsSiteService;
}
