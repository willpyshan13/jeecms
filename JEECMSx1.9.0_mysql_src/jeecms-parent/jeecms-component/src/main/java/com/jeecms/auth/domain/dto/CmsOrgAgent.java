/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */  
 package com.jeecms.auth.domain.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreMenu;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.service.CmsSiteService;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**   
 * 组织增强类
 * @author: tom
 * @date:   2019年8月30日 下午5:36:34     
 */
public class CmsOrgAgent implements Serializable{

        private static final long serialVersionUID = 8359912924427975500L;
        
        private CmsOrg org;

        private CmsOrgAgent(Builder builder) {
                setOrg(builder.org);
        }

        public CmsOrg getOrg() {
                return org;
        }

        public void setOrg(CmsOrg org) {
                this.org = org;
        }

        public CmsOrgAgent(CmsOrg org) {
                super();
                this.org = org;
        }

        public CmsOrgAgent() {
                super();
        }

        public static List<CmsOrg> sortListBySortAndChild(List<CmsOrg> orgs) {
                sortBySortAndChild(orgs);
                return orgs;
        }

        public static List<CmsOrg> sortBySortAndChild(List<CmsOrg> orgs) {
                if (orgs != null && orgs.size() > 0) {
                        orgs = orgs.stream().sorted(Comparator.comparing(CmsOrg::getId).reversed()
                                        ).collect(Collectors.toList());
                        for (CmsOrg org : orgs) {
                                if (org.getChildren() != null && org.getChildren().size() > 0) {
                                        org.setChildren(sortBySortAndChild(org.getChildren()));
                                }
                        }
                }
                return orgs;
        }

        public static void agentOwnerSites(CmsOrg org) {
                CmsSiteService siteService = ApplicationContextProvider.getBean(CmsSiteService.class);
                siteService.findByIds(CmsSite.fetchIds(org.getOwnerSites()));
        }

        public static JSONArray convertListToJsonArray(List<CmsOrg>childs, CoreUser  user){
                JSONArray result = new JSONArray();
                /**没有栏目或者用户为空则直接返回空*/
                if (null == childs || childs.size() == 0||user==null) {
                        return result;
                }
                JSONArray dataSource = new JSONArray();
                JSONObject jsonObject = null;
                Map<Integer, JSONObject> hashDatas = new HashMap<>(childs.size());

                List<Integer>uerOwnnerSiteIds = CmsSite.fetchIds(user.getOwnerSites());
                List<Integer>uerOwnnerMenuIds = CoreMenu.fetchIds(user.getOwnerMenus());
                for (CmsOrg t : childs) {
                        jsonObject = new JSONObject();
                        jsonObject.put("id", t.getId());
                        jsonObject.put("createTime", t.getCreateTime());
                        jsonObject.put("createUser", t.getCreateUser());
                        jsonObject.put("isVirtual", t.getIsVirtual());
                        jsonObject.put("name", t.getName());
                        jsonObject.put("notCurrUserOrg", t.getNotCurrUserOrg());
                        jsonObject.put("orgLeader", t.getOrgLeader());
                        jsonObject.put("orgNum", t.getOrgNum());
                        jsonObject.put("createTimes", t.getCreateTimes());
                        jsonObject.put("phone", t.getPhone());
                        jsonObject.put("managerAble", true);
                        jsonObject.put("deleteAble", true);
                        /** 检查站群是否全包含 */
                        if (!uerOwnnerSiteIds.containsAll(CmsSite.fetchIds(t.getOwnerSites()))) {
                                jsonObject.put("managerAble",false);
                                jsonObject.put("deleteAble", false);
                        }
                        /** 检查菜单是否全包含 */
                        if (!uerOwnnerMenuIds.containsAll(CoreMenu.fetchIds(t.getOwnerMenus()))) {
                                jsonObject.put("managerAble",false);
                                jsonObject.put("deleteAble", false);
                        }
                        /***
                         * 组织是否可删除 顶层组织不可删除，用户所属组织不可删
                         */
                        if (t.isTop()) {
                                jsonObject.put("deleteAble", false);
                        }
                        if (user != null && t.getId().equals(user.getOrgId())) {
                                jsonObject.put("deleteAble", false);
                        }
                        jsonObject.put("parentId", t.getParentId());
                        // 没有子节点则过滤childs
//                        long count = childs.stream().filter(
//                                c -> null != c.getParentId() && ((Integer) t.getId()).intValue()
//                                        == c.getParentId().intValue()).count();
//                        if (count > 0) {
////                                jsonObject.put("children", new ArrayList<>());
////                        }
                        long count = t.getChild().size();
                        if (count > 0) {
                                jsonObject.put("children", new ArrayList<>());
                        }
                        dataSource.add(jsonObject);
                        hashDatas.put((Integer) jsonObject.get("id"), jsonObject);
                }
                childs.clear();

                // 遍历菜单集合
                for (int i = 0; i < dataSource.size(); i++) {
                        // 当前节点
                        JSONObject json = (JSONObject) dataSource.get(i);
                        // 当前的父节点
                        JSONObject parent = hashDatas.get(json.get("parentId"));
                        if (parent != null) {
                                // 表示当前节点为子节点
                                ((List<JSONObject>) parent.get("children")).add(json);
                        } else {
                                // parentId为null和获取匹配parentId的节点(生成某节点的子节点树时需要用到)
                                result.add(json);
                        }

                }
                return result;
        }


        public static final class Builder {
                private CmsOrg org;

                public Builder() {
                }

                public Builder org(CmsOrg val) {
                        org = val;
                        return this;
                }

                public CmsOrgAgent build() {
                        return new CmsOrgAgent(this);
                }
        }


}
