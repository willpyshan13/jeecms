package com.jeecms.auth.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.jeecms.auth.dao.CoreRoleDao;
import com.jeecms.auth.domain.CoreMenu;
import com.jeecms.auth.domain.CoreRole;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.vo.CoreRoleVo;
import com.jeecms.auth.service.CoreRoleService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.ServerModeEnum;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.dto.BeatchDto;
import com.jeecms.system.service.CmsDataPermService;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.util.SystemContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 角色管理service实现类
 * 
 * @author: tom
 * @date: 2019年4月17日 下午7:49:10
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CoreRoleServiceImpl extends BaseServiceImpl<CoreRole, CoreRoleDao, Integer>
                implements CoreRoleService,ApplicationListener<ContextRefreshedEvent> {
        static Logger logger = LoggerFactory.getLogger(CoreRoleServiceImpl.class);
        @Autowired
        private CmsOrgService cmsOrgService;
        @Lazy
        @Autowired
        private CmsDataPermService cmsDataPermService;
        @Autowired
        private CmsSiteService siteService;

        @Override
        public CoreRole saveRole(CoreRole bean) throws GlobalException {
                Integer orgid = bean.getOrgid();
                if (orgid != null) {
                        CmsOrg org = cmsOrgService.findById(orgid);
                        bean.setOrg(org);
                }
                bean = super.save(bean);
                bean.validManagerAble();
                return bean;
        }

        @Override
	public CoreRole updateRole(CoreRole bean) throws GlobalException {
		Integer orgid = bean.getOrgid();
        /** 需要清空权限缓存数据，后续操作从新拉取 */
        bean.clearPermCache();
		bean = super.update(bean);
        if (orgid != null) {
                CmsOrg org = cmsOrgService.findById(orgid);
                bean.setOrgid(orgid);
                bean.setOrg(org);
        }
		return bean;
	}

        @Override
        public ResponseInfo deleteBatch(BeatchDto dto, Integer orgId) throws GlobalException {
                Integer[] ids = null;
                List<Integer> roleList = new ArrayList<Integer>(10);
                // 得到该组织以及子组织的ID集合
                CmsOrg org = cmsOrgService.findById(orgId);
                List<Integer> old = org.getChildOrgIds();
                // 得到所有的角色
                List<CoreRole> coreRoles = dao.listRole(old, null);
                // 得到所有角色ID集合
                for (CoreRole coreRole : coreRoles) {
                        roleList.add(coreRole.getId());
                }
                // 判断是否存在不可操作数据,如果存在，直接不允许操作
                if(!SystemContextUtils.replay()) {
                    for (Integer id : dto.getIds()) {
                        if (!roleList.contains(id)) {
                            return new ResponseInfo(UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getCode(),
                                    UserErrorCodeEnum.ALREADY_DATA_NOT_OPERATION.getDefaultMessage());
                        }
                    }
                }
                ids = dto.getIds().toArray(new Integer[0]);
                List<CoreRole> roles = super.findAllById(Arrays.asList(ids));
                // 判断是否可管理
                for (CoreRole role : roles) {
                     role.validManagerAble();
                        for (CoreUser user : role.getUsers()) {
                                user.getRoles().remove(role);
                        }
                     role.setUsers(Collections.emptyList());
                }
                super.delete(roles);
                // 删除角色权限,无需删除用户
                cmsDataPermService.delete(null,roles,null);
                return new ResponseInfo();
        }

        @Override
        public ResponseInfo pageRole(List<Integer> orgids, String roleName, Pageable pageable) {
                return new ResponseInfo(dao.pageRole(orgids, roleName, pageable));
        }

        @Override
        public List<CoreRole> listRole(List<Integer> orgids, String roleName) {
                return dao.listRole(orgids, roleName);
        }

        @Override
        public List<CoreRoleVo> listRoleVo(List<Integer> orgids, String roleName) {
                List<CoreRole> roles = listRole(orgids,roleName);
                List<CoreRoleVo> roleVos= new ArrayList<>();
                CoreUser user = SystemContextUtils.getCoreUser();
                for(CoreRole role:roles){
                        CoreRoleVo vo = convertRoleToVo(user,role);
                        roleVos.add(vo);
                }
                return roleVos;
        }


        @Override
        public void clearAllRoleCache() {
                List<CoreRole> roles = listRole(null, null);
                for (CoreRole role : roles) {
                        role.clearPermCache();
                }
        }

        /**
         * 系统启动初始化角色权限
         * 
         * @Title: initAllRoleCache
         * @return: void
         */
        public void initAllRoleCache() {
                List<CoreRole> roles = listRole(null, null);
                for (CoreRole role : roles) {
                        logger.info(role.getId().toString());
                }
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
//                ThreadPoolService.getInstance().execute(() -> initAllRoleCache());
        }

        @Override
        public  Page<CoreRoleVo> pageRoleForJson(List<Integer> orgids, String roleName, Pageable pageable) {
                Page<CoreRole>rolePage = dao.pageRole(orgids, roleName, pageable);
                List<CoreRoleVo> roleVos= new ArrayList<>();
                CoreUser user = SystemContextUtils.getCoreUser();
                for(CoreRole role:rolePage.getContent()){
                        CoreRoleVo vo = convertRoleToVo(user,role);
                        roleVos.add(vo);
                }
                Page<CoreRoleVo> roleVoPage = new PageImpl<>(roleVos, pageable, rolePage.getTotalElements());
                return roleVoPage;
        }

        private CoreRoleVo convertRoleToVo(CoreUser user,CoreRole role){
                CoreRoleVo vo = new CoreRoleVo();
                vo.setId(role.getId());
                vo.setCreateTime(role.getCreateTime());
                vo.setCreateUser(role.getCreateUser());
                vo.setOrgName(role.getOrgName());
                vo.setNotCurrUserRole(role.getNotCurrUserRole());
                vo.setRoleName(role.getRoleName());
                List<Integer> ownerSiteIds = CmsSite.fetchIds(user.getOwnerSites());
                List<Integer>userOwnerMenuIds= CoreMenu.fetchIds(user.getOwnerMenus());
                List<Integer> userOrgIds = user.getChildOrgIds();
                if (user != null ) {
                        /** 开发模式下返回true 忽略权限验证 */
                        if (ServerModeEnum.dev.toString().equals(serverMode)) {
                                vo.setDeleteAble(true);
                        }else{
                                vo.setDeleteAble(true);
                                /** 不可编辑自己所属角色的权限 */
                                if(user.getRoleIds().contains(role.getId())){
                                        vo.setDeleteAble(false);
                                }
                                if(!user.getAdmin()){
                                        vo.setDeleteAble(false);
                                }
                                /** 检查站群是否全包含 */
                                List<Integer> roleOwnerSiteIds = CmsSite.fetchIds(role.getOwnerSites());
                                if (!ownerSiteIds.containsAll(roleOwnerSiteIds)) {
                                        vo.setDeleteAble(false);
                                }
                                /** 检查菜单是否全包含 */
                                if (!userOwnerMenuIds.containsAll(CoreMenu.fetchIds(role.getOwnerMenus()))) {
                                        vo.setDeleteAble(false);
                                }
                                /** 检查用户组织层级是否全包含 */
                                if (!userOrgIds.contains(role.getOrgid())) {
                                        vo.setDeleteAble(false);
                                }
                        }
                }else{
                        vo.setDeleteAble(false);
                }
                vo.setManagerAble(vo.isDeleteAble());
                return vo;
        }

        @Value("${spring.profiles.active}")
        private String serverMode;
}
