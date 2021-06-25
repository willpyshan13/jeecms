package com.jeecms.system.service.impl;

import com.jeecms.auth.domain.CoreMenu;
import com.jeecms.auth.domain.CoreRole;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.dto.CmsOrgAgent;
import com.jeecms.auth.domain.dto.CoreRoleAgent;
import com.jeecms.auth.service.CoreMenuService;
import com.jeecms.auth.service.CoreRoleService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.util.MyBeanUtils;
import com.jeecms.component.listener.ChannelListener;
import com.jeecms.component.listener.MenuListener;
import com.jeecms.component.listener.SiteListener;
import com.jeecms.system.dao.CmsDataPermDao;
import com.jeecms.system.domain.CmsDataPerm;
import com.jeecms.system.domain.CmsDataPermCfg;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.dto.*;
import com.jeecms.system.domain.dto.CmsDatePermDto.*;
import com.jeecms.system.domain.vo.CmsDataPermVo;
import com.jeecms.system.service.CmsDataPermCfgService;
import com.jeecms.system.service.CmsDataPermService;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 数据权限service实现类
 *
 * @author: tom
 * @date: 2018年11月5日 下午2:04:49
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class CmsDataPermServiceImpl extends BaseServiceImpl<CmsDataPerm, CmsDataPermDao, Integer>
        implements CmsDataPermService, SiteListener, ChannelListener, MenuListener {

    Paginable maxPaginable = new PaginableRequest(0, Integer.MAX_VALUE);

    /***
     * 修改文档类权限慢，所以此处用了异步执行
     * @param dto
     *                CmsDatePermDto
     * @throws GlobalException
     */
    @Override
    @Async("asyncServiceExecutor")
    public void updateByDto(CmsDatePermDto dto) throws GlobalException {
        Short dataType = dto.getDataType();
        CmsDataPermCfg cfg = null;
        List<CmsDataPerm> toRemoveDataPerms = new ArrayList<CmsDataPerm>();
        CmsOrg org = null;
        CoreRole role = null;
        CoreUser user = null;
        Integer siteId = null;
        if (dto != null && dto.getMoreDataIds() != null) {
            siteId = dto.getMoreDataIds().getSiteId();
        }
        Set<CmsDataPerm> dataPerms = new HashSet<>();
        if (dto.getOrgId() != null) {
            org = orgService.findById(dto.getOrgId());
            cfg = org.getPermCfg();
            /** 先删除对应数据权限 */
            toRemoveDataPerms = findList(org.getId(), null, null, siteId, dto.getDataType(), null, null,
                    maxPaginable);
            dataPerms = org.getDataPerms();
        } else if (dto.getRoleId() != null) {
            role = roleService.findById(dto.getRoleId());
            cfg = role.getPermCfg();
            toRemoveDataPerms = findList(null, dto.getRoleId(), null, siteId, dto.getDataType(), null, null,
                    maxPaginable);
            dataPerms = role.getDataPerms();
        } else if (dto.getUserId() != null) {
            user = userService.findById(dto.getUserId());
            cfg = user.getPermCfg();
            toRemoveDataPerms = findList(null, null, dto.getUserId(), siteId, dto.getDataType(), null, null,
                    maxPaginable);
            dataPerms = user.getDataPerms();
        }
        physicalDeleteInBatch(toRemoveDataPerms);
        dataPerms.removeAll(toRemoveDataPerms);
        /**
         * 站群权限
         */
        if (CmsDataPerm.DATA_TYPE_SITE_OWNER.equals(dataType)) {
            List<SiteMap> rows = dto.getSiteDatas();
            Set<CmsSite> sites = new HashSet<CmsSite>();
            for (SiteMap row : rows) {
                if (row.getSiteId() != null) {
                    if (row.getSiteId() != 0) {
                        if (row.getSelected() != null && row.getSelected()) {
                            CmsSite s = siteService.findById(row.getSiteId());
                            sites.add(s);
                        }
                    } else {
                        /** 新增站点配置 */
                        if (cfg == null) {
                            if (org != null) {
                                cfg = permCfgService.saveByOrg(org, null);
                            } else if (role != null) {
                                cfg = permCfgService.saveByRole(role, null);
                            } else if (user != null) {
                                cfg = permCfgService.saveByUser(user, null);
                            }
                        }
                        if (row.getSelected() != null && row.getSelected()) {
                            cfg.setNewSiteOwner(true);
                        } else {
                            if (dto.getSiteDataOneSelect()) {
                                cfg.setNewSiteOwner(false);
                            } else {
                                cfg.setNewSiteOwner(null);
                            }
                        }
                        permCfgService.updateAll(cfg);
                    }
                }
            }
            if (dto.getOrgId() != null) {
                org.getSites().clear();
                org.setSites(sites);
                orgService.update(org);
            } else if (dto.getRoleId() != null) {
                role.getSites().clear();
                role.setSites(sites);
                roleService.update(role);
            } else if (dto.getUserId() != null) {
                user.getSites().clear();
                user.setSites(sites);
                userService.update(user);
            }
        } else if (CmsDataPerm.DATA_TYPE_SITE.equals(dataType)) {
            /**
             * 站点数据权限
             */
            List<SiteRow> rows = dto.getDataIds();
            for (SiteRow row : rows) {
                if (row.getSiteId() != null) {
                    if (row.getSiteId() != 0) {
                        for (MiniDataUnit rowOpe : row.getRowDatas()) {
                            CmsDataPerm data = new CmsDataPerm();
                            data = saveOneDataPerm(data, dto, dataType, row.getSiteId(),
                                    rowOpe.getOperation(), row.getSiteId(),
                                    rowOpe.getSelected());
                            dataPerms.add(data);
                        }
                    } else {
                        /** 新增站点配置 */
                        StringBuffer newSiteOpe = new StringBuffer();
                        for (MiniDataUnit rowOpe : row.getRowDatas()) {
                                if (rowOpe.getSelected() != null
                                        && rowOpe.getSelected()) {
                                    newSiteOpe.append(rowOpe.getOperation())
                                            .append(",");
                                }
                        }
                        if (cfg == null) {
                            if (org != null) {
                                cfg = permCfgService.saveByOrg(org, null);
                            } else if (role != null) {
                                cfg = permCfgService.saveByRole(role, null);
                            } else if (user != null) {
                                cfg = permCfgService.saveByUser(user, null);
                            }
                        }
                        cfg.setNewSiteOpe(newSiteOpe.toString());
                        permCfgService.updateAll(cfg);
                    }
                }
            }
        } else if (CmsDataPerm.DATA_TYPE_MENU.equals(dataType)) {
            List<MenuMap> rows = dto.getMenus();
            /**
             * 不参与权限分配的子菜单默认全部给与，一级菜单若是不参与分配 则全部给与
             */
            List<CoreMenu> topAutoAuthMenus = menuService.findByParams(0).stream()
                    .filter(menu -> !menu.getIsAuth()).collect(Collectors.toList());
            List<CoreMenu> autoAuthMenus = new ArrayList<CoreMenu>();
            /** 用户所选择的菜单处理不参与权限分配的子菜单 */
            List<CoreMenu> toAddAutoAuthMenus = new ArrayList<CoreMenu>();
            toAddAutoAuthMenus.addAll(topAutoAuthMenus);
            for (MenuMap row : rows) {
                if (row.getMenuId() != 0) {
                    if (row.getSelected() != null && row.getSelected()) {
                        CoreMenu menu = menuService.findById(row.getMenuId());
                        toAddAutoAuthMenus.add(menu);
                    }
                }
            }
            autoAuthMenus.addAll(CoreMenu.getAllChildAndSort(toAddAutoAuthMenus, true));
            Set<MenuMap> menuMap = new HashSet<MenuMap>();
            menuMap.addAll(rows);
            for (CoreMenu menu : autoAuthMenus) {
                menuMap.add(new MenuMap(menu.getId(), true));
            }
            List<CoreMenu> menus = new ArrayList<CoreMenu>();
            for (MenuMap row : menuMap) {
                if (row.getMenuId() != null) {
                    if (row.getMenuId() != 0) {
                        if (row.getSelected() != null && row.getSelected()) {
                            menus.add(menuService.findById(row.getMenuId()));
                        }
                    } else {
                        /** 新增菜单配置 */
                        if (cfg == null) {
                            if (org != null) {
                                cfg = permCfgService.saveByOrg(org, null);
                            } else if (role != null) {
                                cfg = permCfgService.saveByRole(role, null);
                            } else if (user != null) {
                                cfg = permCfgService.saveByUser(user, null);
                            }
                        }
                        if (row.getSelected() != null && row.getSelected()) {
                            cfg.setNewMenuOwner(true);
                        } else {
                            if (dto.getMenuOneSelect()) {
                                cfg.setNewMenuOwner(false);
                            } else {
                                cfg.setNewMenuOwner(null);
                            }
                        }
                        permCfgService.updateAll(cfg);
                    }
                }
            }
            if (dto.getOrgId() != null) {
                org.getMenus().clear();
                org.setMenus(menus);
                /**未单独分配菜单权限则把menus清空，否则menus带了默认菜单权限，则无法使用继承的关系*/
                if(!org.getHasAssignOwnerMenu()){
                    org.getMenus().clear();
                }
                orgService.update(org);
            } else if (dto.getRoleId() != null) {
                role.getMenus().clear();
                role.setMenus(menus);
                /**未单独分配菜单权限则把menus清空，否则menus带了默认菜单权限，则无法使用继承的关系*/
                if(!role.getHasAssignOwnerMenu()){
                    role.getMenus().clear();
                }
                roleService.update(role);
            } else if (dto.getUserId() != null) {
                user.getMenus().clear();
                user.setMenus(menus);
                /**未单独分配菜单权限则把menus清空，否则menus带了默认菜单权限，则无法使用继承的关系*/
                if(!user.getHasAssignOwnerMenu()){
                    user.getMenus().clear();
                }
                userService.update(user);
            }
        } else {
            /**
             * 栏目、文档类数据权限
             */
            ChannelRow row = dto.getMoreDataIds();
            /** 栏目的权限的新配置 多个站点是分开的 */
            if (dto.getOrgId() != null) {
                cfg = org.getPermCfgForChannel(row.getSiteId());
            } else if (dto.getRoleId() != null) {
                cfg = role.getPermCfgForChannel(row.getSiteId());
            } else if (dto.getUserId() != null) {
                cfg = user.getPermCfgForChannel(row.getSiteId());
            }
            /** 新增栏目配置 */
            StringBuffer newSiteOpe = new StringBuffer();
            CmsSite site = siteService.findById(row.getSiteId());
            /** 循环栏目 */
            for (ChannelUnit oneSite : row.getUnits()) {
                if (oneSite.getKeyId() != 0) {
                    /** 具体操作权限 */
                    for (MiniDataUnit rowOpe : oneSite.getRowDatas()) {
//                        CmsDataPerm data = findOne(dto.getOrgId(), dto.getRoleId(),
//                                dto.getUserId(), row.getSiteId(), dataType,
//                                rowOpe.getOperation(), oneSite.getKeyId());
                        CmsDataPerm data = new CmsDataPerm();
                        data = saveOneDataPerm(data, dto, dataType, row.getSiteId(),
                                rowOpe.getOperation(), oneSite.getKeyId(),
                                rowOpe.getSelected());
                        dataPerms.add(data);
                    }
                } else {
                    /** 0为新增栏目配置 */
                    for (MiniDataUnit rowOpe : oneSite.getRowDatas()) {
                        if (rowOpe.getSelected() != null && rowOpe.getSelected()) {
                            newSiteOpe.append(rowOpe.getOperation()).append(",");
                        }
                    }
                }
            }
            if(cfg==null){
                if (org != null) {
                    cfg = permCfgService.saveByOrg(org, site);
                } else if (role != null) {
                    cfg = permCfgService.saveByRole(role, site);
                } else if (user != null) {
                    cfg =  permCfgService.saveByUser(user, site);
                }
            }
            if (cfg != null) {
                if (CmsDataPerm.DATA_TYPE_CHANNEL.equals(dataType)) {
                    cfg.setNewChannelOpe(newSiteOpe.toString());
                } else if (CmsDataPerm.DATA_TYPE_CONTENT.equals(dataType)) {
                    cfg.setNewChannelOpeContent(newSiteOpe.toString());
                }
                permCfgService.updateAll(cfg);
            }
        }
//        if (dto.getOrgId() != null) {
//            /**主动维护集合缓存*/
//            org.setDataPerms(dataPerms);
//            /** 从新分配权限，需要清空权限缓存 */
//            org.clearPermCache();
//        } else if (dto.getRoleId() != null) {
//            /**主动维护集合缓存*/
//            role.setDataPerms(dataPerms);
//            /** 从新分配权限，需要清空权限缓存 */
//            role.clearPermCache();
//        } else if (dto.getUserId() != null) {
//            /**主动维护集合缓存*/
//            user.setDataPerms(dataPerms);
//            /** 从新分配权限，需要清空权限缓存 */
//            user.clearPermCache();
//        }
        ThreadPoolService.getInstance().execute(() -> {
            userService.clearAllUserCache();
            orgService.clearAllOrgCache();
            roleService.clearAllRoleCache();
        });
    }

    @Override
    public void updateDataPermByOrg(OrgPermDto dto) throws GlobalException {
        if (dto.getDataId() != null) {
            HashSet<OrgPermUnitDto> units = dto.getPerms();
            Set<Integer> orgIds = new HashSet<Integer>();
            Set<Integer> roleIds = new HashSet<Integer>();
            Map<Integer, Set<MiniDataUnit>> rolePermMap = new HashMap<Integer, Set<MiniDataUnit>>(16);
            Map<Integer, Set<MiniDataUnit>> orgPermMap = new HashMap<Integer, Set<MiniDataUnit>>(16);
            for (OrgPermUnitDto unit : units) {
                if (unit.getOrgId() != null && unit.getOrgOps() != null) {
                    orgIds.add(unit.getOrgId());
                    orgPermMap.put(unit.getOrgId(), unit.getOrgOps());
                } else if (unit.getRoleId() != null && unit.getRoleOps() != null) {
                    roleIds.add(unit.getRoleId());
                    rolePermMap.put(unit.getRoleId(), unit.getRoleOps());
                }
            }
            CmsSite site;
            if (CmsDataPerm.DATA_TYPE_SITE.equals(dto.getDataType())) {
                site = siteService.findById(dto.getDataId());
            } else {
                Channel channel = channelService.findById(dto.getDataId());
                site = channel.getSite();
            }
            Set<CmsDataPerm> toRemovePerms = new HashSet<>();
            Set<CmsDataPerm> toAddPerms = new HashSet<>();
            CoreUser currUser = SystemContextUtils.getCoreUser();
            for (Integer orgId : orgIds) {
                CmsOrg org = orgService.findById(orgId);
                /** 非顶层组织和非当前登录用户所属组织才可变更权限 */
                if (!org.isTop() && !currUser.getOrgId().equals(orgId)) {
                    Set<CmsDataPerm> orgOwnerByChannelPerms = org
                            .getOwnerDataPermsByType(dto.getDataType(), site.getId(),
                                    null, dto.getDataId(), false);
                    Set<Short> orgOwnerByChannelOpes = CmsDataPerm
                            .getOperators(orgOwnerByChannelPerms);
                    Set<Short> toRemoveOpes = new HashSet<Short>();
                    Set<Short> toAddOpes = new HashSet<Short>();
                    boolean needUpdateOrgPerm = false;
                    /** 前端需要传递所有操作选项 */
                    if (orgPermMap != null && orgPermMap.size() > 0) {
                        for (MiniDataUnit mu : orgPermMap.get(orgId)) {
                            /** 未选中的则需要移除 */
                            if (mu.getSelected() != null && !mu.getSelected()) {
                                /** 未选中的被现有权限包含，则需要添加到待删除中 */
                                if (orgOwnerByChannelOpes.contains(mu.getOperation())) {
                                    toRemoveOpes.add(mu.getOperation());
                                    needUpdateOrgPerm = true;
                                }
                            } else {
                                /** 选中的没有被现有权限包含，则需要添加到待添加中 */
                                if (!orgOwnerByChannelOpes.contains(mu.getOperation())) {
                                    toAddOpes.add(mu.getOperation());
                                    needUpdateOrgPerm = true;
                                }
                            }
                        }
                    }
                    /** 则检查是否需要 待更新 */
                    if (needUpdateOrgPerm) {
                        /** 判断是否单独分配了权限,单独分配了则直接从单独分配的权限去除或者新增，否则需要从继承的权限中去除后新增 */
                        Set<CmsDataPerm> orgOriginPerms =new CopyOnWriteArraySet<>();
                        if (org.getHasAssignDataPermsByType(dto.getDataType())) {
                            orgOriginPerms.addAll(org.getDataPermsByType(dto.getDataType(),
                                    site.getId(), null, dto.getDataId()));
                            for (CmsDataPerm p : orgOriginPerms) {
                                if (toRemoveOpes.contains(p.getOperation())) {
                                    toRemovePerms.add(p);
                                    orgOriginPerms.remove(p);
                                }
                            }
                            for (Short op : toAddOpes) {
                                CmsDataPerm dp = new CmsDataPerm(dto.getDataId(),
                                        dto.getDataType(), op, orgId,
                                        site.getId(), site, org);
                                if (CmsDataPerm.DATA_TYPE_SITE.equals(dto.getDataType())) {
                                    dp.setSite(siteService.findById(dto.getDataId()));
                                } else {
                                    dp.setDataChannel(channelService.findById(dto.getDataId()));
                                }
                                toAddPerms.add(dp);
                                orgOriginPerms.add(dp);
                            }
                            org.setDataPerms(orgOriginPerms);
                        } else {
                            Set<CmsDataPerm> perms = new HashSet<>();
                            for(CmsDataPerm p:org.getAllOwnerDataPermsByType(dto.getDataType())){
                                if (toRemoveOpes.contains(p.getOperation())&&dto.getDataId().equals(p.getDataId())) {
                                    continue;
                                }
                                CmsDataPerm d = new CmsDataPerm();
                                MyBeanUtils.copyProperties(p,d);
                                d.setSite(p.getSite());
                                d.setSiteId(p.getSiteId());
                                d.setOrgId(orgId);
                                d.setOrg(org);
                                if(p.getDataId()!=null){
                                    if (CmsDataPerm.DATA_TYPE_CONTENT.equals(dto.getDataType()) || CmsDataPerm.DATA_TYPE_CHANNEL.equals(dto.getDataType())) {
                                        p.setDataChannel(channelService.findById(p.getDataId()));
                                    }
                                }
                                perms.add(d);
                            }
                            org.setDataPerms(perms);
                            toAddPerms.addAll(perms);
                            updatePermCfg(site,dto.getDataType(),org,null,null);
                        }
                        /** 从新分配权限，需要清空权限缓存 */
                        org.clearPermCache();
                    }
                }
            }
            for (Integer roleId : roleIds) {
                /** 非当前登录用户所在角色 */
                if (!currUser.getRoleIds().contains(roleId)) {
                    CoreRole role = roleService.findById(roleId);
                    Set<CmsDataPerm> roleOwnerByChannelPerms = role
                            .getOwnerDataPermsByType(dto.getDataType(), site.getId(),
                                    null, dto.getDataId());
                    Set<Short> roleOwnerByChannelOpes = CmsDataPerm
                            .getOperators(roleOwnerByChannelPerms);
                    Set<Short> toRemoveOpes = new HashSet<Short>();
                    Set<Short> toAddOpes = new HashSet<Short>();
                    boolean needUpdateRolePerm = false;
                    /** 前端需要传递所有操作选项 */
                    if (rolePermMap != null && rolePermMap.size() > 0) {
                        for (MiniDataUnit mu : rolePermMap.get(roleId)) {
                            /** 未选中的则需要移除 */
                            if (mu.getSelected() != null && !mu.getSelected()) {
                                /** 未选中的被现有权限包含，则需要添加到待删除中 */
                                if (roleOwnerByChannelOpes.contains(mu.getOperation())) {
                                    toRemoveOpes.add(mu.getOperation());
                                    needUpdateRolePerm = true;
                                }
                            } else {
                                /** 选中的没有被现有权限包含，则需要添加到待添加中 */
                                if (!roleOwnerByChannelOpes.contains(mu.getOperation())) {
                                    toAddOpes.add(mu.getOperation());
                                    needUpdateRolePerm = true;
                                }
                            }
                        }
                    }
                    /** 则检查是否需要 待更新 */
                    if (needUpdateRolePerm) {
                        /** 判断是否单独分配了权限,单独分配了则直接从单独分配的权限去除或者新增，否则需要从继承的权限中去除不需要的后添加 */
                        Set<CmsDataPerm> roleOriginPerms = new CopyOnWriteArraySet<>();
                        if (role.getHasAssignDataPermsByType(dto.getDataType())) {
                            roleOriginPerms.addAll(role.getDataPermsByType(dto.getDataType(),
                                    site.getId(), null, dto.getDataId()));
                            for (CmsDataPerm p : roleOriginPerms) {
                                if (toRemoveOpes.contains(p.getOperation())) {
                                    toRemovePerms.add(p);
                                    roleOriginPerms.remove(p);
                                }
                            }
                            for (Short op : toAddOpes) {
                                CmsDataPerm dp = new CmsDataPerm(dto.getDataId(),
                                        dto.getDataType(), op, roleId,
                                        site.getId(), site, role);
                                if (CmsDataPerm.DATA_TYPE_SITE.equals(dto.getDataType())) {
                                    dp.setSite(siteService.findById(dto.getDataId()));
                                } else {
                                    dp.setDataChannel(channelService.findById(dto.getDataId()));
                                }
                                toAddPerms.add(dp);
                                roleOriginPerms.add(dp);
                            }
                            role.setDataPerms(roleOriginPerms);
                        } else {
                            /***否则需要从继承的权限中去除不需要的后添加 */
                            Set<CmsDataPerm> perms = new HashSet<>();
                            for(CmsDataPerm p:role.getAllOwnerDataPermsByType(dto.getDataType())){
                                if (toRemoveOpes.contains(p.getOperation())&&dto.getDataId().equals(p.getDataId())) {
                                    continue;
                                }
                                CmsDataPerm d = new CmsDataPerm();
                                MyBeanUtils.copyProperties(p,d);
                                d.setSite(p.getSite());
                                d.setSiteId(p.getSiteId());
                                d.setRoleId(roleId);
                                d.setRole(role);
                                if(p.getDataId()!=null){
                                    if (CmsDataPerm.DATA_TYPE_CONTENT.equals(dto.getDataType()) || CmsDataPerm.DATA_TYPE_CHANNEL.equals(dto.getDataType())) {
                                        p.setDataChannel(channelService.findById(p.getDataId()));
                                    }
                                }
                                perms.add(d);
                            }
                            role.setDataPerms(perms);
                            toAddPerms.addAll(perms);
                            updatePermCfg(site,dto.getDataType(),null,role,null);
                        }
                        /** 从新分配权限，需要清空权限缓存 */
                        role.clearPermCache();
                    }
                }
            }
            saveAll(toAddPerms);
            physicalDeleteInBatch(toRemovePerms);
        }
    }

    @Override
    public void updateDataPermByUser(UserPermDto dto) throws GlobalException {
        if (dto.getDataId() != null) {
            HashSet<UserPermUnitDto> units = dto.getPerms();
            Set<Integer> userIds = new HashSet<Integer>();
            Map<Integer, Set<MiniDataUnit>> userPermMap = new HashMap<Integer, Set<MiniDataUnit>>(16);
            for (UserPermUnitDto unit : units) {
                userIds.add(unit.getUserId());
                userPermMap.put(unit.getUserId(), unit.getOps());
            }
            CmsSite site;
            if (CmsDataPerm.DATA_TYPE_SITE.equals(dto.getDataType())) {
                site = siteService.findById(dto.getDataId());
            } else {
                Channel channel = channelService.findById(dto.getDataId());
                site = channel.getSite();
            }
            Set<CmsDataPerm> toRemovePerms = new HashSet<>();
            Set<CmsDataPerm> toAddPerms = new HashSet<>();
            Integer currUserId = SystemContextUtils.getCoreUser().getId();
            /** 不可变更当前登录用户的权限 */
            List<CoreUser> users = new ArrayList<>();
            Map<Integer,Set<Short>>toRemoveOpesMap=new HashMap<>();
            for (Integer userId : userIds) {
                if (!userId.equals(currUserId)) {
                    CoreUser user = userService.findById(userId);
                    users.add(user);
                    Set<CmsDataPerm> ownerByChannelPerms = user
                            .getOwnerDataPermsByType(dto.getDataType(), site.getId(),
                                    null, dto.getDataId());
                    Set<Short> ownerByChannelOpes = CmsDataPerm
                            .getOperators(ownerByChannelPerms);
                    Set<Short> toRemoveOpes = new HashSet<>();
                    Set<Short> toAddOpes = new HashSet<Short>();
                    boolean needUpdateRolePerm = false;
                    /** 前端需要传递所有操作选项 */
                    if (userPermMap != null && userPermMap.size() > 0) {
                        for (MiniDataUnit mu : userPermMap.get(userId)) {
                            /** 未选中的则需要移除 */
                            if (mu.getSelected() != null && !mu.getSelected()) {
                                /** 未选中的被现有权限包含，则需要添加到待删除中 */
                                if (ownerByChannelOpes.contains(mu.getOperation())) {
                                    toRemoveOpes.add(mu.getOperation());
                                    needUpdateRolePerm = true;
                                }
                            } else {
                                /** 选中的没有被现有权限包含，则需要添加到待添加中 */
                                if (!ownerByChannelOpes.contains(mu.getOperation())) {
                                    toAddOpes.add(mu.getOperation());
                                    needUpdateRolePerm = true;
                                }
                            }
                        }
                    }
                    /** 则检查是否需要 待更新 */
                    if (needUpdateRolePerm) {
                        /** 判断是否单独分配了权限,单独分配了则直接从单独分配的权限去除或者新增，否则需要从继承的权限中去除不需要的后添加 */
                       Set<CmsDataPerm> originPerms;
                        if (user.getHasAssignDataPermsByType(dto.getDataType())) {
                            originPerms = user.getDataPermsByType(dto.getDataType(),
                                    site.getId(), null, dto.getDataId());
                            for (CmsDataPerm p : originPerms) {
                                if (toRemoveOpes.contains(p.getOperation())) {
                                    toRemovePerms.add(p);
                                }
                            }
                            for (Short op : toAddOpes) {
                                CmsDataPerm dp = new CmsDataPerm(dto.getDataId(),
                                        dto.getDataType(), op, userId,
                                        site.getId(), site, user);
                                if (CmsDataPerm.DATA_TYPE_SITE.equals(dto.getDataType())) {
                                    dp.setSite(siteService.findById(dto.getDataId()));
                                } else {
                                    dp.setDataChannel(channelService.findById(dto.getDataId()));
                                }
                                toAddPerms.add(dp);
                            }
                        } else {
                            /***否则需要从继承的权限中去除不需要的后添加 */
                            originPerms = user.getAllOwnerDataPermsByType(dto.getDataType());
                            Set<CmsDataPerm> perms = new HashSet<>();
                            for(CmsDataPerm p:originPerms){
                                if (toRemoveOpes.contains(p.getOperation())&&dto.getDataId().equals(p.getDataId())) {
                                    continue;
                                }
                                CmsDataPerm d = new CmsDataPerm();
                                MyBeanUtils.copyProperties(p,d);
                                d.setSite(p.getSite());
                                d.setSiteId(p.getSiteId());
                                d.setUserId(userId);
                                d.setUser(user);
                                if(p.getDataId()!=null){
                                    if (CmsDataPerm.DATA_TYPE_CONTENT.equals(dto.getDataType()) || CmsDataPerm.DATA_TYPE_CHANNEL.equals(dto.getDataType())) {
                                        p.setDataChannel(channelService.findById(p.getDataId()));
                                    }
                                }
                                perms.add(d);
                            }
                            user.setDataPerms(perms);
                            toAddPerms.addAll(perms);
                            updatePermCfg(site,dto.getDataType(),null,null,user);
                        }
                    }
                }
            }
            toAddPerms = toAddPerms.stream().filter(p->p.getDataId()!=null).collect(Collectors.toSet());
            saveAll(toAddPerms);
            physicalDeleteInBatch(toRemovePerms);
            users.stream().forEach(new Consumer<CoreUser>() {
                @Override
                public void accept(CoreUser coreUser) {
                    /**强制刷新集合缓存*/
                    coreUser.getDataPerms().clear();
                    /** 从新分配权限，需要清空权限缓存 */
                    coreUser.clearPermCache();
                }
            });
        }
    }

    @Override
    public void updateSiteOwner(OwnerSitePermDto dto) throws GlobalException {
        Integer siteId = dto.getSiteId();
        List<OwnerSitePermUnitDto> perms = dto.getPerms();
        CmsSite site = siteService.findById(siteId);
        Set<Integer> toAddOwnerSiteOrgIds = new HashSet<Integer>();
        Set<Integer> toRemoveOwnerSiteOrgIds = new HashSet<Integer>();
        HashSet<CmsOrg> toAddOwnerSiteOrgs = new HashSet<CmsOrg>();
        HashSet<CmsOrg> toRemoveOwnerSiteOrgs = new HashSet<CmsOrg>();
        Set<Integer> toAddOwnerSiteRoleIds = new HashSet<Integer>();
        Set<Integer> toRemoveOwnerSiteRoleIds = new HashSet<Integer>();
        Set<Integer> toAddOwnerSiteUserIds = new HashSet<Integer>();
        Set<Integer> toRemoveOwnerSiteUserIds = new HashSet<Integer>();
        CoreUser currUser = SystemContextUtils.getCoreUser();
        for (OwnerSitePermUnitDto p : perms) {
            if (p.getOrgId() != null) {
                if (p.getSelected()) {
                    toAddOwnerSiteOrgIds.add(p.getOrgId());
                } else {
                    toRemoveOwnerSiteOrgIds.add(p.getOrgId());
                }
            }
            if (p.getRoleId() != null) {
                if (p.getSelected()) {
                    toAddOwnerSiteRoleIds.add(p.getRoleId());
                } else {
                    toRemoveOwnerSiteRoleIds.add(p.getRoleId());
                }
            }
            if (p.getUserId() != null) {
                if (p.getSelected()) {
                    toAddOwnerSiteUserIds.add(p.getUserId());
                } else {
                    toRemoveOwnerSiteUserIds.add(p.getUserId());
                }
            }

        }
        toAddOwnerSiteOrgs.addAll(orgService.findAllById(toAddOwnerSiteOrgIds));
        toRemoveOwnerSiteOrgs.addAll(orgService.findAllById(toRemoveOwnerSiteOrgIds));
        /** 判断是否单独分配了权限,单独分配了则直接从单独分配的权限去除或者新增，否则需要从继承的权限中去除或者新增 */
        HashSet<CmsSite> orgOriginPerms;
        for (CmsOrg org : toRemoveOwnerSiteOrgs) {
            /** 非顶层组织且非当前用户所在组织 */
            if (!org.isTop() && !org.getId().equals(currUser.getOrgId())) {
                if (org.getHasAssignOwnerSite()) {
                    org.getSites().remove(site);
                } else {
                    orgOriginPerms = new HashSet<>();
                    orgOriginPerms.addAll(org.getCloneOwnerSites());
                    orgOriginPerms.remove(site);
                    org.setSites(orgOriginPerms);
                }
                CmsDataPermCfg  cfg = org.getPermCfg();
                if (cfg == null) {
                    cfg = permCfgService.saveByOrg(org, null);
                }
                /**删除站点权限,没有分配新增站点权限,导致继承权限自上级,当站群权限为空则处理新站点权限数据为空*/
                if (org.getSites().isEmpty()&&(org.getHasAssignNewSiteOwner()==null
                        ||!org.getHasAssignNewSiteOwner())) {
                    cfg.setNewSiteOwner(true);
                }
                permCfgService.updateAll(cfg);
                /** 从新分配权限，需要清空权限缓存 */
                org.clearPermCache();
            }
        }
        for (CmsOrg org : toAddOwnerSiteOrgs) {
            /** 非顶层组织且非当前用户所在组织 */
            if (!org.isTop() && !org.getId().equals(currUser.getOrgId())) {
                if (org.getHasAssignOwnerSite()) {
                    org.getSites().add(site);
                } else {
                    /** 非顶层组织才可变更 */
                    orgOriginPerms = new HashSet<>();
                    orgOriginPerms.addAll(org.getCloneOwnerSites());
                    orgOriginPerms.add(site);
                    org.setSites(orgOriginPerms);
                }
                /** 从新分配权限，需要清空权限缓存 */
                org.clearPermCache();
            }
        }
        /** 处理角色权限 */
        HashSet<CoreRole> toAddOwnerSiteRoles = new HashSet<CoreRole>();
        HashSet<CoreRole> toRemoveOwnerSiteRoles = new HashSet<CoreRole>();
        toAddOwnerSiteRoles.addAll(roleService.findAllById(toAddOwnerSiteRoleIds));
        toRemoveOwnerSiteRoles.addAll(roleService.findAllById(toRemoveOwnerSiteRoleIds));
        /** 判断是否单独分配了权限,单独分配了则直接从单独分配的权限去除或者新增，否则需要从继承的权限中去除或者新增 */
        for (CoreRole role : toRemoveOwnerSiteRoles) {
            if (!currUser.getRoleIds().contains(role.getId())) {
                if (role.getHasAssignOwnerSite()) {
                    role.getSites().remove(site);
                } else {
                    orgOriginPerms = new HashSet<>();
                    orgOriginPerms.addAll(role.getCloneOwnerSites());
                    orgOriginPerms.remove(site);
                    role.setSites(orgOriginPerms);
                }
                /**删除站点权限,可能导致继承权限自上级,当站群权限为空则处理新站点权限数据为空*/
                CmsDataPermCfg  cfg = role.getPermCfg();
                if (cfg == null) {
                    cfg = permCfgService.saveByRole(role, null);
                }
                if (role.getSites().isEmpty()&&(role.getHasAssignNewSiteOwner()==null
                        ||!role.getHasAssignNewSiteOwner())) {
                    cfg.setNewSiteOwner(true);
                }
                permCfgService.updateAll(cfg);
                /** 从新分配权限，需要清空权限缓存 */
                role.clearPermCache();
            }
        }
        for (CoreRole role : toAddOwnerSiteRoles) {
            if (!currUser.getRoleIds().contains(role.getId())) {
                if (role.getHasAssignOwnerSite()) {
                    role.getSites().add(site);
                } else {
                    orgOriginPerms = new HashSet<>();
                    orgOriginPerms.addAll(role.getCloneOwnerSites());
                    orgOriginPerms.add(site);
                    role.setSites(orgOriginPerms);
                }
                /** 从新分配权限，需要清空权限缓存 */
                role.clearPermCache();
            }
        }
        /** 处理用户权限 */
        HashSet<CoreUser> toAddOwnerSiteUsers = new HashSet<CoreUser>();
        HashSet<CoreUser> toRemoveOwnerSiteUsers = new HashSet<CoreUser>();
        toAddOwnerSiteUsers.addAll(userService.findAllById(toAddOwnerSiteUserIds));
        toRemoveOwnerSiteUsers.addAll(userService.findAllById(toRemoveOwnerSiteUserIds));
        /** 需要从继承的权限中去除或者新增 */
        Set<CmsSite> userOwnerSites;
        for (CoreUser user : toAddOwnerSiteUsers) {
            if (!user.getId().equals(currUser.getId())) {
                userOwnerSites = new HashSet<>();
                userOwnerSites.addAll(user.getCloneOwnerSites());
                userOwnerSites.add(site);
                user.getSites().clear();
                user.setSites(userOwnerSites);
                /** 从新分配权限，需要清空权限缓存 */
                user.clearPermCache();
            }
        }

        for (CoreUser user : toRemoveOwnerSiteUsers) {
            if (!user.getId().equals(currUser.getId())) {
                userOwnerSites = new HashSet<>();
                userOwnerSites.addAll(user.getCloneOwnerSites());
                userOwnerSites.remove(site);
                user.getSites().clear();
                user.setSites(userOwnerSites);
                CmsDataPermCfg  cfg = user.getPermCfg();
                if (cfg == null) {
                        cfg = permCfgService.saveByUser(user, null);
                }
                if (user.getSites().isEmpty()&&(user.getHasAssignNewSiteOwner()==null
                        ||!user.getHasAssignNewSiteOwner())) {
                    cfg.setNewSiteOwner(true);
                }
                permCfgService.updateAll(cfg);
                /** 从新分配权限，需要清空权限缓存 */
                user.clearPermCache();
            }
        }
    }

    @Override
    public List<CmsDataPerm> findList(Integer orgId, Integer roleId, Integer userId, Integer siteId, Short dataType,
                                      Short operation, Integer dataId, Paginable paginable) {
        List<CmsDataPerm> list = dao.findList(orgId, roleId, userId, siteId, dataType, operation, dataId,
                paginable);
        return list;
    }

    @Override
    public long getCount(Integer orgId, Integer roleId, Integer userId, Integer siteId,
                         Short dataType, Short operation, Integer dataId) {
        return dao.getCount(orgId, roleId, userId, siteId, dataType, operation, dataId);
    }


    @Override
    public CmsDataPerm findOne(Integer orgId, Integer roleId, Integer userId, Integer siteId, Short dataType,
                               Short operation, Integer dataId) {
        List<CmsDataPerm> list = findList(orgId, roleId, userId, siteId, dataType, operation, dataId,
                new PaginableRequest(0, 1));
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 从继承制改成手动赋予栏目权限，此时调用，需要新增继承权限除去去除的部分，还需要新增站点类和栏目类新增权限配置
     * @param site
     * @param dataType
     * @param org
     * @param role
     * @param user
     * @throws GlobalException
     */
    private void updatePermCfg(CmsSite site,Short dataType,CmsOrg org,CoreRole role,CoreUser user) throws GlobalException {
        List<CmsDataPermCfg> cfgs = new ArrayList<>();
        List<CmsSite>sites = siteService.findAll(false,true);
        if(user!=null){
            if(CmsDataPerm.DATA_TYPE_CONTENT.equals(dataType)
                    ||CmsDataPerm.DATA_TYPE_CHANNEL.equals(dataType)){
                for(CmsSite s:sites){
                    CmsDataPermCfg c = user.getPermCfgForChannel(s.getId());
                    if(c==null){
                        c = permCfgService.saveByUser(user,s);
                    }
                    cfgs.add(c);
                }
                user.setPermCfgs(cfgs);
            }
            if(CmsDataPerm.DATA_TYPE_SITE.equals(dataType)){
                CmsDataPermCfg cfg = user.getPermCfg();
                if(cfg==null){
                    cfg = permCfgService.saveByUser(user,site);
                }
                cfg.setNewSiteOpe(CmsDataPerm.getOpeSites());
                permCfgService.updateAll(cfg);
            }
        }
        if(org!=null){
            if(CmsDataPerm.DATA_TYPE_CONTENT.equals(dataType)
                    ||CmsDataPerm.DATA_TYPE_CHANNEL.equals(dataType)){
                for(CmsSite s:sites){
                    CmsDataPermCfg c = org.getPermCfgForChannel(s.getId());
                    if(c==null){
                        c = permCfgService.saveByOrg(org,s);
                    }
                    cfgs.add(c);
                }
                org.setPermCfgs(cfgs);
            }
            if(CmsDataPerm.DATA_TYPE_SITE.equals(dataType)){
                CmsDataPermCfg cfg = org.getPermCfg();
                if(cfg==null){
                    cfg = permCfgService.saveByOrg(org,site);
                }
                cfg.setNewSiteOpe(CmsDataPerm.getOpeSites());
                permCfgService.updateAll(cfg);
            }
        }
        if(role!=null){
            if(CmsDataPerm.DATA_TYPE_CONTENT.equals(dataType)
                    ||CmsDataPerm.DATA_TYPE_CHANNEL.equals(dataType)){
                for(CmsSite s:sites){
                    CmsDataPermCfg c = role.getPermCfgForChannel(s.getId());
                    if(c==null){
                        c = permCfgService.saveByRole(role,s);
                    }
                    cfgs.add(c);
                }
                role.setPermCfgs(cfgs);
            }
            if(CmsDataPerm.DATA_TYPE_SITE.equals(dataType)){
                CmsDataPermCfg cfg = role.getPermCfg();
                if(cfg==null){
                    cfg = permCfgService.saveByRole(role,site);
                }
                cfg.setNewSiteOpe(CmsDataPerm.getOpeSites());
                permCfgService.updateAll(cfg);
            }
        }
        for(CmsDataPermCfg c:cfgs){
            /**重新手动分配了权限，新栏目权限给全部*/
            if (CmsDataPerm.DATA_TYPE_CONTENT.equals(dataType)) {
                c.setNewChannelOpeContent(CmsDataPerm.getOpeContentChannels());
            }
            if (CmsDataPerm.DATA_TYPE_CHANNEL.equals(dataType)) {
                c.setNewChannelOpe(CmsDataPerm.getOpeChannels());
            }
            if (CmsDataPerm.DATA_TYPE_SITE.equals(dataType)) {
                c.setNewSiteOpe(CmsDataPerm.getOpeSites());
            }
            permCfgService.updateAll(c);
        }
    }

    private CmsDataPerm saveOneDataPerm(CmsDataPerm data, CmsDatePermDto dto, Short dataType, Integer siteId,
                                        Short operate, Integer dataId, Boolean selected) throws GlobalException {
//        if (data == null) {
//            if (selected != null && selected) {
//                data = getDataPerm(dto, dataType, siteId, operate, dataId);
//                super.save(data);
//            }
//        } else {
//            super.update(data);
//        }
        if (selected != null && selected) {
            data = getDataPerm(dto, dataType, siteId, operate, dataId);
            super.save(data);
        }
        return data;
    }

    private CmsDataPerm getDataPerm(CmsDatePermDto dto, Short dataType, Integer siteId, Short operate,
                                    Integer dataId) throws GlobalException {
        CmsDataPerm data = new CmsDataPerm();
        data.setDataType(dataType);
        /**角色和用户的权限编辑会带上组织id，需要屏蔽掉，只有组织的编辑才设置*/
        if (dto.getOrgId() != null && (dto.getRoleId() == null && dto.getUserId() == null)) {
            data.setOrgId(dto.getOrgId());
        }
        data.setRoleId(dto.getRoleId());
        data.setUserId(dto.getUserId());
        data.setSiteId(siteId);
        data.setDataId(dataId);
        data.setOperation(operate);
        /**角色和用户的权限编辑会带上组织id，需要屏蔽掉，只有组织的编辑才设置*/
        if (dto.getOrgId() != null && (dto.getRoleId() == null && dto.getUserId() == null)) {
            data.setOrg(orgService.findById(dto.getOrgId()));
        }
        if (dto.getRoleId() != null) {
            data.setRole(roleService.findById(dto.getRoleId()));
        }
        if (dto.getUserId() != null) {
            data.setUser(userService.findById(dto.getUserId()));
        }
        if (siteId != null) {
            data.setSite(siteService.findById(siteId));
        }
        return data;
    }

    /**
     * 分配新栏目的权限
     *
     * @throws GlobalException GlobalException
     * @Title: afterChannelSave
     * @return: void
     */
    @Override
    public void afterChannelSave(Channel c) throws GlobalException {
        List<CoreUser> users = userService.findList(true, null, null, null, true, CoreUser.AUDIT_USER_STATUS_PASS, null, null, null, null,
                maxPaginable);
        for (CoreUser user : users) {
            updateUserChannel(user, c);
        }
        List<CoreRole> roles = roleService.findAll(true);
        for (CoreRole role : roles) {
            updateRoleChannel(role, c);
        }
        List<CmsOrg> orgs = orgService.findAll(true);
        for (CmsOrg org : orgs) {
            updateOrgChannel(org, c);
        }
        /** 主动刷新权限缓存 */
        userService.clearAllUserCache();
        orgService.clearAllOrgCache();
        roleService.clearAllRoleCache();
    }

    @Override
    public void beforeChannelDelete(Integer[] ids) {
        for (Integer id : ids) {
            deleteByChannelId(id);
        }
    }

    @Override
    @Async("asyncServiceExecutor")
    public void afterChannelRecycle(List<Channel> channels) throws GlobalException {
        /** 主动刷新权限缓存 */
        userService.clearAllUserCache();
        orgService.clearAllOrgCache();
        roleService.clearAllRoleCache();
    }

    @Override
    public void afterChannelChange(Channel c) throws GlobalException {
        /** 主动刷新权限缓存 */
        userService.clearAllUserCache();
        orgService.clearAllOrgCache();
        roleService.clearAllRoleCache();
    }

    /**
     * 新建菜单调用更新用户、角色、组织的菜单权限
     *
     * @param menu CoreMenu
     * @throws GlobalException GlobalException
     * @Title: afterMenuSave
     */
    @Override
    public void afterMenuSave(CoreMenu menu) throws GlobalException {
        List<CoreUser> users = userService.findList(true, null, null, null, true, null, null, null, null, null,
                maxPaginable);
        /** 只更新用户栏目数据会导致菜单不会保存入库 */
        for (CoreUser user : users) {
            /** 用户是否有新增菜单权限 */
            if (user.getHasAssignOwnerMenu() && user.getOwnerNewMenuOwner()) {
                user.getMenus().add(menu);
            }
        }
        List<CoreRole> roles = roleService.findAll(true);
        for (CoreRole role : roles) {
            if (role.getHasAssignOwnerMenu() && role.getOwnerNewMenuOwner()) {
                role.getMenus().add(menu);
            }
        }
        List<CmsOrg> orgs = orgService.findAll(true);
        for (CmsOrg org : orgs) {
            if (org.getHasAssignOwnerMenu() && org.getOwnerNewMenuOwner()) {
                org.getMenus().add(menu);
            }
        }
        /** 主动刷新权限缓存 */
        userService.clearAllUserCache();
        orgService.clearAllOrgCache();
        roleService.clearAllRoleCache();
    }

    /**
     * 分配新站点权限给用户、角色、组织
     *
     * @param site 站点
     * @throws GlobalException GlobalException
     * @Title: afterSiteSave
     * @return: void
     */
    @Override
    @Async("asyncServiceExecutor")
    public void afterSiteSave(CmsSite site) throws GlobalException {
        List<CoreUser> users = userService.findList(true, null, null, null, true,
                CoreUser.AUDIT_USER_STATUS_PASS, null, null, null, null, maxPaginable);
        for (CoreUser user : users) {
            updateUserSite(user, site);
        }
        List<CoreRole> roles = roleService.findAll(true);
        for (CoreRole role : roles) {
            updateRoleSite(role, site);
        }
        List<CmsOrg> orgs = orgService.findAll(true);
        for (CmsOrg org : orgs) {
            updateOrgSite(org, site);
        }
        /** 主动刷新权限缓存 */
        userService.clearAllUserCache();
        orgService.clearAllOrgCache();
        roleService.clearAllRoleCache();
    }

    @Override
    public void beforeSiteDelete(Integer[] ids) {
        for (Integer id : ids) {
            deleteBySiteId(id);
        }
    }

    public void updateUserSite(CoreUser user, CmsSite site) throws GlobalException {
        /** 用户是否有新增站点站群权限 */
        if (user.getHasAssignOwnerSite() && user.getOwnerNewSiteOwner()) {
            /** 给用户分配该新建站点的站群权限 */
            Set<CmsSite> sites = user.getOwnerSites();
            sites.add(site);
            user.setSites(sites);
            userService.update(user);
        }
        if (user.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_SITE)) {
            if (StringUtils.isNoneBlank(user.getNewSiteOpe())) {
                List<CmsDataPerm> initPerms = CmsDataPermVo
                        .initUserDataPermsForSite(user.getOwnerNewSiteOperators(), user, site);
                initPerms = saveAll(initPerms);
                user.getDataPerms().addAll(initPerms);
                userService.update(user);
            }
        }
    }

    private void updateRoleSite(CoreRole role, CmsSite site) throws GlobalException {
        if (role.getHasAssignOwnerSite() && role.getOwnerNewSiteOwner()) {
            Set<CmsSite> sites = role.getOwnerSites();
            CoreRoleAgent.agentOwnerSites(role);
            sites.add(site);
            role.setSites(sites);
            roleService.flush();
        }
        if (role.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_SITE)) {
            if (StringUtils.isNoneBlank(role.getNewSiteOpe())) {
                List<CmsDataPerm> initPerms = CmsDataPermVo
                        .initRoleDataPermsForSite(role.getOwnerNewSiteOperators(), role, site);
                initPerms = saveAll(initPerms);
                role.getDataPerms().addAll(initPerms);
                roleService.flush();
            }
        }
    }

    private void updateOrgSite(CmsOrg org, CmsSite site) throws GlobalException {
        /** 是否单独分配了站群权限，且有新建站点的站群权限 */
        if (org.getHasAssignOwnerSite() && org.getOwnerNewSiteOwner()) {
            Set<CmsSite> sites = org.getOwnerSites();
            CmsOrgAgent.agentOwnerSites(org);
            sites.add(site);
            org.setSites(sites);
            orgService.flush();
        }
        /** 是否单独分配了站点数据权限，且有新建站点的站点操作数据权限 */
        if (org.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_SITE)) {
            if (StringUtils.isNoneBlank(org.getNewSiteOpe())) {
                List<CmsDataPerm> initPerms = CmsDataPermVo
                        .initOrgDataPermsForSite(org.getOwnerNewSiteOperators(), org, site);
                initPerms = saveAll(initPerms);
                org.getDataPerms().addAll(initPerms);
                orgService.flush();
            }
        }
    }

    private void updateUserChannel(CoreUser user, Channel channel) throws GlobalException {
        /** 栏目类数据权限 */
        /** 是否单独分配了栏目类权限，且有新增栏目类权限 */
        if (user.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL)) {
            if (StringUtils.isNoneBlank(user.getNewChannelOpe(channel.getSiteId()))) {
                List<CmsDataPerm> initPerms = CmsDataPermVo.initUserDataPermsForChannelByChannel(
                        user.getOwnerNewChannelOperators(channel.getSiteId()), user, channel);
                initPerms = saveAll(initPerms);
                user.getDataPerms().addAll(initPerms);
                userService.flush();
            }
        }

        /** 文档类数据权限 */
        /** 是否单独分配了文档类权限，且有新增文档类权限 */
        if (user.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT)) {
            if (StringUtils.isNoneBlank(user.getNewChannelOpeContent(channel.getSiteId()))) {
                List<CmsDataPerm> initPerms = CmsDataPermVo.initUserDataPermsForContentByChannel(
                        user.getOwnerNewChannelContentOperators(channel.getSiteId()), user,
                        channel);
                initPerms = saveAll(initPerms);
                user.getDataPerms().addAll(initPerms);
                userService.flush();
            }
        }
    }

    private void updateRoleChannel(CoreRole role, Channel channel) throws GlobalException {
        /** 栏目类数据权限 */
        if (role.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL)) {
            if (StringUtils.isNoneBlank(role.getNewChannelOpe(channel.getSiteId()))) {
                List<CmsDataPerm> initPerms = CmsDataPermVo.initRoleDataPermsForChannelByChannel(
                        role.getOwnerNewChannelOperators(channel.getSiteId()), role, channel);
                initPerms = saveAll(initPerms);
                role.getDataPerms().addAll(initPerms);
                roleService.flush();
            }
        }

        /** 文档类数据权限 */
        if (role.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT)) {
            if (StringUtils.isNoneBlank(role.getNewChannelOpeContent(channel.getSiteId()))) {
                List<CmsDataPerm> initPerms = CmsDataPermVo.initRoleDataPermsForContentByChannel(
                        role.getOwnerNewChannelContentOperators(channel.getSiteId()), role,
                        channel);
                initPerms = saveAll(initPerms);
                role.getDataPerms().addAll(initPerms);
                roleService.flush();
            }
        }
    }

    private void updateOrgChannel(CmsOrg org, Channel channel) throws GlobalException {
        /** 栏目类数据权限 */
        if (org.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CHANNEL)) {
            if (StringUtils.isNoneBlank(org.getNewChannelOpe(channel.getSiteId()))) {
                List<CmsDataPerm> initPerms = CmsDataPermVo.initOrgDataPermsForChannelByChannel(
                        org.getOwnerNewChannelOperators(channel.getSiteId()), org, channel);
                initPerms = saveAll(initPerms);
                org.getDataPerms().addAll(initPerms);
                orgService.flush();
            }
        }
        /** 文档类数据权限 */
        if (org.getHasAssignDataPermsByType(CmsDataPerm.DATA_TYPE_CONTENT)) {
            if (StringUtils.isNoneBlank(org.getNewChannelOpeContent(channel.getSiteId()))) {
                List<CmsDataPerm> initPerms = CmsDataPermVo.initOrgDataPermsForContentByChannel(
                        org.getOwnerNewChannelContentOperators(channel.getSiteId()), org,
                        channel);
                initPerms = saveAll(initPerms);
                org.getDataPerms().addAll(initPerms);
                orgService.flush();
            }
        }
    }

    public void deleteBySiteId(Integer siteId) {
        dao.deleteBySiteId(siteId);
        permCfgService.deleteBySiteId(siteId);
    }

    public void deleteByChannelId(Integer channelId) {
        dao.deleteByChannelId(channelId);
    }

    @Override
    public void delete(List<CmsOrg> orgs, List<CoreRole> roles, List<CoreUser> users) throws GlobalException {
        List<CmsDataPerm> data = new ArrayList<CmsDataPerm>(10);
        if(orgs!=null){
            for (int i = 0; i < orgs.size(); i++) {
                CmsOrg org = orgs.get(i);
                List<CmsDataPerm> list = findList(org.getId(), null, null, null, null, null,
                        null, null);
                data.addAll(list);
                permCfgService.deleteByOrgId(org.getId());
            }
        }
        if(users!=null){
            for (int i = 0; i < users.size(); i++) {
                CoreUser user = users.get(i);
                List<CmsDataPerm> list = findList(null, null, user.getId(), null, null, null,
                        null, null);
                data.addAll(list);
                permCfgService.deleteByUserId(user.getId());
            }
        }
        if(roles!=null){
            for (int i = 0; i < roles.size(); i++) {
                CoreRole role = roles.get(i);
                List<CmsDataPerm> list = findList(null, role.getId(), null, null, null, null,
                        null, null);
                data.addAll(list);
                permCfgService.deleteByRoleId(role.getId());
            }
        }
        physicalDeleteInBatch(data);
    }

    @Autowired
    private CmsOrgService orgService;
    @Autowired
    private CoreRoleService roleService;
    @Autowired
    private CoreUserService userService;
    @Autowired
    private CmsSiteService siteService;
    @Autowired
    private CoreMenuService menuService;
    @Autowired
    private CmsDataPermCfgService permCfgService;
    @Lazy
    @Autowired
    private ChannelService channelService;

}
