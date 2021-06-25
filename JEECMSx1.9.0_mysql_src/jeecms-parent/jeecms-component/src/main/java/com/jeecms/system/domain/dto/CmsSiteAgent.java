/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.util.HibernateProxyUtil;
import com.jeecms.system.domain.CmsSite;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 站点增强类
 *
 * @author: tom
 * @date: 2019年8月30日 下午5:36:34
 */
public class CmsSiteAgent implements Serializable {
    static Logger logger = LoggerFactory.getLogger(CmsSiteAgent.class);
    private static final long serialVersionUID = 8359912924427975500L;
    private final ReentrantLock lock = new ReentrantLock();
    private CmsSite site;

    public CmsSiteAgent(CmsSite site) {
        super();
        this.site = site;
    }

    /**
     * @return the site
     */
    public CmsSite getSite() {
        return site;
    }

    /**
     * @param site
     *                the site to set
     */
    public void setSite(CmsSite site) {
        this.site = site;
    }

    public CmsSiteAgent() {
        super();
    }

    /** 按照站点ID排序 **/
    public static List<CmsSite> sortByIdAndChild(List<CmsSite> sites) {
        if (sites != null && sites.size() > 0) {
            sites = sites.stream().sorted(Comparator.comparing(CmsSite::getId))
                    .collect(Collectors.toList());
            for (CmsSite org : sites) {
                if (org.getChildren() != null && org.getChildren().size() > 0) {
                    org.setChildren(sortByIdAndChild(org.getChildren()));
                }
            }
        }
        return sites;
    }

    /** 按照站点ID排序 **/
    public static List<CmsSite> sortByIdAndChild(Set<CmsSite> sites) {
        List<CmsSite> sortSites = new ArrayList<CmsSite>();
        if (sites != null && sites.size() > 0) {
            sortSites = sites.stream().sorted(Comparator.comparing(CmsSite::getId))
                    .collect(Collectors.toList());
            for (CmsSite site : sites) {
                if (site.getChildren() != null && site.getChildren().size() > 0) {
                    site.setChildren(sortByIdAndChild(site.getChildren()));
                }
            }
        }
        return sortSites;
    }

    /** 得到站点ID集合 **/
    public static List<Integer> sortIds(Set<Integer> ids) {
        List<Integer> idList = new ArrayList<Integer>();
        if (ids != null && ids.size() > 0) {
            idList = ids.stream().sorted().collect(Collectors.toList());
        }
        return idList;
    }

    /** 按照站点排序值，站点创建时间排序 **/
    public static List<CmsSite> sortBySortNumAndChild(List<CmsSite> sites) {
        if (sites != null && sites.size() > 0) {
            sites = sites.stream()
                    .sorted(Comparator.comparing(CmsSite::getSortNum)
                            .reversed().thenComparing(Comparator
                                    .comparing(CmsSite::getCreateTime).reversed()))
                    .collect(Collectors.toList());
            for (CmsSite s : sites) {
                if (s.getChildren() != null && s.getChildren().size() > 0) {
                    List<CmsSite> childrens = s.getChildren();
                    childrens = childrens.stream().sorted(Comparator.comparing(CmsSite::getSortNum)
                            .reversed().thenComparing(Comparator
                                    .comparing(CmsSite::getCreateTime).reversed()))
                            .collect(Collectors.toList());
                    s.setChildren(childrens);
                }
            }
        }
        return sites;
    }

    public static void initSiteChild(CopyOnWriteArraySet<CmsSite> set) {
        for (CmsSite s : set) {
            for (CmsSite site : s.getChilds()) {
                HibernateProxyUtil.loadHibernateProxy(site.getCfg());
                HibernateProxyUtil.loadHibernateProxy(site);
                logger.debug("loadSite Children->" + site.getId());
            }
        }
    }

    public static List<String> getBaseDomains(List<CmsSite>sites) {
        List<String> baseDomains = new ArrayList<>();
        if (sites != null && sites.size() > 0) {
            return sites.stream().map(s -> s.getBaseDomain()).collect(Collectors.toList());
        }
        return baseDomains;
    }

    /**
     * 验证refer是否有效，有效的则true，否则false
     * @param sites 所有站点
     * @param refer 当前访问refer
     * @return
     */
    public  static  boolean validRefer(List<CmsSite>sites,String refer,List<String> referHeaderExcludes){
        List<String> baseDomains = getBaseDomains(sites);
        if(StringUtils.isBlank(refer)){
            return true;
        }
        for(String ex:referHeaderExcludes){
            if(refer.startsWith(ex)){
                return true;
            }
        }
//        HttpServletRequest request = RequestUtils.getHttpServletRequest();
//        if(request.getRequestURL().toString().contains(request.getServerName())){
//            return true;
//        }
        /**refer只要满足是站点中一个域名的根域名即可，认为是来自安全的域名范围内*/
        if(StringUtils.isNotBlank(refer)){
            for(String base:baseDomains){
                if(refer.contains(base)){
                    return true;
                }
            }
        }
        if(refer.contains("jeecms.com")){
            return true;
        }
        if(refer.contains("localhost") || refer.contains("127.0.0.1")){
            return true;
        }
        return  false;
    }

    public static JSONArray convertListToJsonArray(List<CmsSite>childs, CoreUser user){
        JSONArray result = new JSONArray();
        /**没有栏目或者用户为空则直接返回空*/
        if (null == childs || childs.size() == 0) {
            return result;
        }
        JSONArray dataSource = new JSONArray();
        JSONObject jsonObject = null;
        Map<Integer, JSONObject> hashDatas = new HashMap<>(childs.size());
        List<Integer>newChildSiteIds = user.getNewChildSiteIds();
        for (CmsSite t : childs) {
            jsonObject = new JSONObject();
            jsonObject.put("id", t.getId());
            jsonObject.put("name", t.getName());
            jsonObject.put("newChildAble", false);
            jsonObject.put("parentId", t.getParentId());
            if (newChildSiteIds.contains(t.getId())) {
                jsonObject.put("newChildAble", true);
            }
            // 没有子节点则过滤childs
            long count = childs.stream().filter(
                    c -> null != c.getParentId() && ((Integer) t.getId()).intValue()
                            == c.getParentId().intValue()).count();
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

    public static JSONArray convertForTree(List<CmsSite>childs, CoreUser user){
        JSONArray result = new JSONArray();
        if (null == childs || childs.size() == 0) {
            return result;
        }
        JSONArray dataSource = new JSONArray();
        JSONObject jsonObject = null;
        Map<Integer, JSONObject> hashDatas = new HashMap<>(childs.size());
        List<Integer>newChildSiteIds = user.getNewChildSiteIds();
        List<Integer>deleteSiteIds = user.getDelSiteIds();
        for (CmsSite t : childs) {
            jsonObject = new JSONObject();
            jsonObject.put("id", t.getId());
            jsonObject.put("name", t.getName());
            jsonObject.put("domain", t.getDomain());
            jsonObject.put("path", t.getPath());
            jsonObject.put("isOpen", t.getIsOpen());
            jsonObject.put("domain", t.getDomain());
            jsonObject.put("previewUrl", t.getPreviewUrl());
            jsonObject.put("deleteAble", false);
            jsonObject.put("newChildAble", false);
            if (newChildSiteIds.contains(t.getId())) {
                jsonObject.put("newChildAble", true);
            }
            if (deleteSiteIds.contains(t.getId())) {
                jsonObject.put("deleteAble", true);
            }
            jsonObject.put("parentId", t.getParentId());
            // 没有子节点则过滤childs
            long count = childs.stream().filter(
                    c -> null != c.getParentId() && ((Integer) t.getId()).intValue()
                            == c.getParentId().intValue()).count();
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
}
