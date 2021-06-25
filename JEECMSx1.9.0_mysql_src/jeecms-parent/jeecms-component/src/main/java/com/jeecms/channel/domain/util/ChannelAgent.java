/**   
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.channel.domain.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 栏目增强类
 * 
 * @author: tom
 * @date: 2019年8月29日 下午5:12:04
 */
public class ChannelAgent implements Serializable {
        private static final long serialVersionUID = -4175597114116170423L;
        Channel channel;

        public ChannelAgent(Channel channel) {
                super();
                this.channel = channel;
        }
        
        public ChannelAgent() {
                super();
        }
        
        /**
         * 栏目排序(排序值降序、生成时间降序)
         * 
         * @Title: sort 栏目排序
         * @param channels 栏目集合
         * @return: Collection
         */
        public List<Channel> sort(List<Channel> channels) {
                return channels.stream()
                                .sorted(Comparator.comparing(Channel::getSortNum)
                                                .thenComparing(Comparator.comparing(Channel::getCreateTime)))
                                .collect(Collectors.toList());
        }

        public static JSONArray convertListToJsonArray(List<Channel>childs){
                JSONArray result = new JSONArray();
                /**没有栏目或者用户为空则直接返回空*/
                if (null == childs || childs.size() == 0) {
                        return result;
                }
                JSONArray dataSource = new JSONArray();
                JSONObject jsonObject = null;
                Map<Integer, JSONObject> hashDatas = new HashMap<>(childs.size());
                for (Channel t : childs) {
                        jsonObject = new JSONObject();
                        jsonObject.put("id", t.getId());
                        jsonObject.put("name", t.getName());
                        jsonObject.put("isBottom", t.getIsBottom());
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

        public static JSONArray convertListToJsonArrayWithContentPerm(List<Channel>childs, CoreUser user){
                JSONArray result = new JSONArray();
                /**没有栏目或者用户为空则直接返回空*/
                if (null == childs || childs.size() == 0||user==null) {
                        return result;
                }
                JSONArray dataSource = new JSONArray();
                JSONObject jsonObject = null;
                Map<Integer, JSONObject> hashDatas = new HashMap<>(childs.size());
                List<Integer>editChannelIds = user.getEditContentChannelIds();
                List<Integer>delChannelIds = user.getDelContentChannelIds();
                List<Integer>fileChannelIds = user.getFileContentChannelIds();
                List<Integer>topicChannelIds = user.getTopContentChannelIds();
                List<Integer>viewChannelIds = user.getViewContentChannelIds();
                List<Integer>moveChannelIds = user.getMoveContentChannelIds();
                List<Integer>sortChannelIds = user.getSortContentChannelIds();
                List<Integer>copyChannelIds = user.getCopyContentChannelIds();
                List<Integer>quoteChannelIds = user.getQuoteContentChannelIds();
                List<Integer>typeChannelIds = user.getTypeContentChannelIds();
                List<Integer>createChannelIds = user.getCreateContentChannelIds();
                List<Integer>publishChannelIds = user.getPublishContentChannelIds();
                List<Integer>sitePushChannelIds = user.getSitePushContentChannelIds();
                List<Integer>wechatPushChannelIds = user.getWechatPushContentChannelIds();
                List<Integer>weiboPushChannelIds = user.getWeiboPushContentChannelIds();
                for (Channel t : childs) {
                        jsonObject = new JSONObject();
                        jsonObject.put("id", t.getId());
                        jsonObject.put("isBottom", t.getIsBottom());
                        jsonObject.put("name", t.getName());

                        jsonObject.put("viewContentAble", false);
                        jsonObject.put("editContentAble", false);
                        jsonObject.put("deleteContentAble", false);
                        jsonObject.put("fileContentAble", false);
                        jsonObject.put("topContentAble", false);
                        jsonObject.put("moveContentAble", false);
                        jsonObject.put("sortContentAble", false);
                        jsonObject.put("copyContentAble", false);
                        jsonObject.put("quoteContentAble", false);
                        jsonObject.put("typeContentAble", false);
                        jsonObject.put("createContentAble", false);
                        jsonObject.put("publishContentAble", false);
                        jsonObject.put("sitePushContentAble", false);
                        jsonObject.put("wechatPushContentAble", false);
                        jsonObject.put("weiboPushContentAble", false);

                        if(viewChannelIds.contains(t.getId())){
                                jsonObject.put("viewContentAble", true);
                        }
                        if(editChannelIds.contains(t.getId())){
                                jsonObject.put("editContentAble", true);
                        }
                        if(delChannelIds.contains(t.getId())){
                                jsonObject.put("deleteAble", true);
                        }
                        if(fileChannelIds.contains(t.getId())){
                                jsonObject.put("fileContentAble", true);
                        }
                        if(topicChannelIds.contains(t.getId())){
                                jsonObject.put("topContentAble", true);
                        }
                        if(moveChannelIds.contains(t.getId())){
                                jsonObject.put("moveContentAble", true);
                        }
                        if(sortChannelIds.contains(t.getId())){
                                jsonObject.put("sortContentAble", true);
                        }
                        if(copyChannelIds.contains(t.getId())){
                                jsonObject.put("copyContentAble", true);
                        }
                        if(quoteChannelIds.contains(t.getId())){
                                jsonObject.put("quoteContentAble", true);
                        }
                        if(typeChannelIds.contains(t.getId())){
                                jsonObject.put("typeContentAble", true);
                        }
                        if(createChannelIds.contains(t.getId())){
                                jsonObject.put("createContentAble", true);
                        }
                        if(publishChannelIds.contains(t.getId())){
                                jsonObject.put("publishContentAble", true);
                        }
                        if(sitePushChannelIds.contains(t.getId())){
                                jsonObject.put("sitePushContentAble", true);
                        }
                        if(wechatPushChannelIds.contains(t.getId())){
                                jsonObject.put("wechatPushContentAble", true);
                        }
                        if(weiboPushChannelIds.contains(t.getId())){
                                jsonObject.put("weiboPushContentAble", true);
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
