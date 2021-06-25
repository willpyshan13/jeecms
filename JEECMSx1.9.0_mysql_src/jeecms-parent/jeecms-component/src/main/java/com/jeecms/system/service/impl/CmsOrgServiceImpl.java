/**
 *  @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import com.jeecms.auth.domain.CoreRole;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.base.domain.DragSortDto;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SysOtherErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.system.dao.CmsOrgDao;
import com.jeecms.system.domain.CmsDataPerm;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.dto.BeatchDto;
import com.jeecms.system.exception.SiteExceptionInfo;
import com.jeecms.system.service.CmsDataPermService;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.util.SystemContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织service实现
 * 
 * @author: tom
 * @date: 2018年11月5日 下午2:03:50
 */
@Service
@Transactional(rollbackFor = { Exception.class })
public class CmsOrgServiceImpl extends BaseServiceImpl<CmsOrg, CmsOrgDao, Integer>
                implements CmsOrgService, ApplicationListener<ContextRefreshedEvent> {

        @Override
        @Transactional(rollbackFor = { Exception.class },readOnly = true)
        public CmsOrg findDefault() {
                return findById(1);
        }

        @Override
        @Transactional(rollbackFor = { Exception.class },readOnly = true)
        public List<CmsOrg> findTopList(Boolean disabled) {
                return super.dao.findList(null, disabled, null, new PaginableRequest(0, Integer.MAX_VALUE));
        }

        @Override
        @Transactional(rollbackFor = { Exception.class },readOnly = true)
        public List<CmsOrg> findListByParentId(Integer parentId, Boolean disabled) {
                return super.dao.findList(parentId, disabled, null, new PaginableRequest(0, Integer.MAX_VALUE));
        }

        @Override
        @Transactional(rollbackFor = { Exception.class },readOnly = true)
        public List<CmsOrg> findList(Integer parentId, Boolean disabled) {
                List<CmsOrg> topList = findTopList(disabled);
                List<CmsOrg> channels = CmsOrg.getListForSelect(topList, null, disabled);
                return channels;
        }

        @Override
        public CmsOrg save(CmsOrg bean) throws GlobalException {
                bean = super.save(bean);
                /** 权限分配需要用到对象parent */
                if (bean.getParentId() != null) {
                        bean.setParent(findById(bean.getParentId()));
                }
                bean.validManagerGetAble();
                return bean;
        }

        @Override
        public CmsOrg update(CmsOrg bean) throws GlobalException {
                bean = super.update(bean);
                /** 需要清空组织权限缓存数据，后续操作从新拉取 */
                bean.clearPermCache();
                return bean;
        }

        @Override
        public CmsOrg updateDirect(CmsOrg bean) throws GlobalException {
                bean = super.update(bean);
                return bean;
        }

        @Override
        public CmsOrg delete(CmsOrg bean) throws GlobalException {
                return super.delete(bean);
        }

        @Override
        public CmsOrg delete(Integer id) throws GlobalException {
                return super.delete(id);
        }

        @Override
        public List<CmsOrg> delete(Integer[] ids) throws GlobalException {
                return super.delete(ids);
        }

        @Override
        public CmsOrg physicalDelete(Integer id) throws GlobalException {
                findById(id).validManagerAble();
                return super.physicalDelete(id);
        }

        @Override
        public List<CmsOrg> physicalDelete(Integer[] ids) throws GlobalException {
                for (Integer id : ids) {
                        findById(id).validManagerAble();
                }
                return super.physicalDelete(ids);
        }

        @Override
        @Transactional(rollbackFor = { Exception.class },readOnly = true)
        public List<CmsOrg> cmsList(Boolean isVirtual, String key, List<Integer> ids) {
                List<CmsOrg> orgs = dao.findListByIDS(ids, isVirtual, key, new PaginableRequest(0, Integer.MAX_VALUE));
                return orgs;
        }

        @Override
        public void clearAllOrgCache() {
                List<CmsOrg> orgs = cmsList(null, null, null);
                for (CmsOrg org : orgs) {
                        org.clearPermCache();
                }
        }

        @Override
        @Transactional(rollbackFor = { Exception.class },readOnly = true)
        public Boolean validName(String orgname, Integer id) {
                CmsOrg org = dao.findByNameAndHasDeleted(orgname, false);
                if (id == null) {
                        if (org != null) {
                                return false;
                        }
                        return true;
                }
                CmsOrg orgs = super.findById(id);
                if (org != null && !orgname.equals(orgs.getName())) {
                        return false;
                }
                return true;
        }

        @Override
        public ResponseInfo deleteOrg(BeatchDto dto, List<Integer> currUserOrgIds) throws GlobalException {
                List<Integer> toDelOrgIds = new ArrayList<Integer>();
                List<CmsOrg> orgs = super.findAllById(dto.getIds());
                List<CmsOrg> toDelOrgs = new ArrayList<CmsOrg>();
                if (currUserOrgIds == null || currUserOrgIds.isEmpty()) {
                        String msg = MessageResolver.getMessage(SysOtherErrorCodeEnum.NO_ORG_PERMISSION_ERROR.getCode(),
                                        SysOtherErrorCodeEnum.NO_ORG_PERMISSION_ERROR.getDefaultMessage());
                        throw new GlobalException(new SiteExceptionInfo(
                                        SysOtherErrorCodeEnum.NO_ORG_PERMISSION_ERROR.getCode(), msg));
                }
                for (CmsOrg org : orgs) {
                        if (!SystemContextUtils.replay()) {
                            if (!currUserOrgIds.contains(org.getId())) {
                                String msg = MessageResolver.getMessage(
                                        SysOtherErrorCodeEnum.NO_ORG_PERMISSION_ERROR.getCode(),
                                        SysOtherErrorCodeEnum.NO_ORG_PERMISSION_ERROR.getDefaultMessage());
                                throw new GlobalException(new SiteExceptionInfo(
                                        SysOtherErrorCodeEnum.NO_ORG_PERMISSION_ERROR.getCode(), msg));
                            }
                        }
                        toDelOrgIds.addAll(org.getChildOrgIds());
                        toDelOrgs.addAll(org.getChildNodeList());
                }
                super.delete(toDelOrgs);
                //super.flush();
                // 删除组织所有角色
                List<CoreRole> toDelRoles = new ArrayList<CoreRole>();
                for (CmsOrg org2 : toDelOrgs) {
                        List<CoreRole>roles =new ArrayList<>();
                        roles.addAll(org2.getRoles());
                        toDelRoles.addAll(roles);
                        org2.getRoles().clear();
                        super.update(org2);
                }
                ThreadPoolService.getInstance().execute(()->{
                        List<CoreUser> users = coreUserService.findList(null, toDelOrgIds, null, null, true,
                                CoreUser.AUDIT_USER_STATUS_PASS, null, null, null, null, null);
                        try {
                                coreUserService.delete(users);
                                cmsDataPermService.delete(toDelOrgs,toDelRoles,users);
                        } catch (GlobalException e) {
                                e.printStackTrace();
                        }
                });
                return new ResponseInfo();
        }

        @Override
        public ResponseInfo sort(DragSortDto sortDto, List<CmsOrg> orgs) throws GlobalException {
                Integer parentId = super.findById(sortDto.getFromId()).getParentId();
                if (parentId != null) {
                        orgs = orgs.stream().filter(site -> parentId.equals(site.getParentId()))
                                .sorted(Comparator.comparing(CmsOrg::getSortNum).reversed()
                                        .thenComparing(Comparator.comparing(CmsOrg::getCreateTime).reversed()))
                                .collect(Collectors.toList());
                } else {
                        orgs = orgs.stream().filter(site -> site.getParentId() == null)
                                .sorted(Comparator.comparing(CmsOrg::getSortNum).reversed()
                                        .thenComparing(Comparator.comparing(CmsOrg::getCreateTime).reversed()))
                                .collect(Collectors.toList());
                }
                // 最大排序值
                int i = orgs.size() * 2;
                // 需要移动的对象的排序值
                int newSort = 0;
                // 需要移动的对象在list集合中的位置
                int index = 0;
                for (int j = 0; j < orgs.size(); j++) {
                        CmsOrg org = orgs.get(j);
                        org.setSortNum(i);
                        if (sortDto.getToId() != null && org.getId().equals(sortDto.getToId())) {
                                newSort = org.getSortNum() - 1;
                        }
                        if (sortDto.getNextId() != null && org.getId().equals(sortDto.getNextId())) {
                                newSort = org.getSortNum() + 1;
                        }
                        // 取出需要移动的值在list集合中的位置
                        if (org.getId().equals(sortDto.getFromId())) {
                                index = j;
                        }
                        i -= 2;
                }
                orgs.get(index).setSortNum(newSort);
                super.batchUpdate(orgs);
                return new ResponseInfo();
        }

        /**
         * 系统启动初始化组织权限
         * 
         * @Title: initAllOrgCache
         * @return: void
         */
        public void initAllOrgCache() {
                /**暂时等待一分钟*/
                try {
                        Thread.sleep(60000);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
                List<CmsOrg> orgs = findAll(true);
                /**初始化组织的各类权限，角色和用户都需要过滤操作，加快读取速度*/
                for (CmsOrg org : orgs) {
                        if (!org.getIsTop()) {
                                /***
                                 * 间隔执行以免数据库被占用太多
                                 */
                                try {
                                        Thread.sleep(500);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                                org.getOwnerSites();
                                org.getOwnerMenus();
                                for (CmsSite site : siteService.findAll(false, true)) {
                                        org.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_SITE, site.getId(), null, null, true);
                                        org.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL, site.getId(), null, null, true);
                                        org.getOwnerDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT, site.getId(), null, null, true);
                                }
                        }
                }
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
                /**
                 * 用户反馈组织多，加载组织列表慢，异步系统启动加载到缓存中
                 */
                ThreadPoolService.getInstance().execute(()->{
                        initAllOrgCache();
                });
        }

        @Autowired
        private CoreUserService coreUserService;
        @Autowired
        private CmsSiteService siteService;
        @Lazy
        @Autowired
        private CmsDataPermService cmsDataPermService;

}