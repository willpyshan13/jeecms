/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.channel.dao.ext;

import com.jeecms.channel.domain.Channel;
import com.jeecms.common.page.Paginable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

/**
 * 栏目扩展查询
 *
 * @author: tom
 * @date: 2019年3月20日 上午8:41:45
 */
public interface ChannelDaoExt {

    /**
     * 查詢list
     *
     * @Title: findList
     * @param siteId
     *                站点ID
     * @param modelId
     *                模型ID
     * @param parentId
     *                父栏目ID（为空或者0则查询顶层栏目 ）
     * @param display
     *                是否前台显示
     * @param staticChannel
     *                是否开启静态化
     * @param paginable
     *                条数
     * @param path 栏目路径
     * @param recycle 是否回收
     * @return List
     */
    List<Channel> findList(Integer siteId, Integer modelId, Integer parentId, Boolean display,
                           Boolean staticChannel, Paginable paginable, String path, Boolean recycle);

    /**
     * 查询page
     *
     * @Title: findPage
     * @param siteId
     *                站点ID
     * @param modelId
     *                模型ID
     * @param parentId
     *                父栏目ID（为空或者0则查询顶层栏目 ）
     * @param display
     *                是否前台显示
     * @param staticChannel
     *                是否开启静态化
     * @param pageable
     *                分页组件
     * @param path 栏目路径
     * @param recycle 是否回收
     * @return List
     */
    Page<Channel> findPage(Integer siteId, Integer modelId, Integer parentId, Boolean display,
                           Boolean staticChannel, Pageable pageable, String path, Boolean recycle);

    /**
     * 根据siteId与栏目名称或者栏目路径，进行查询检索
     *
     * @Title: checkNameAndPath
     * @param name
     *                栏目名称
     * @param path
     *                栏目路径
     * @param siteId
     *                站点Id
     * @return: List
     */
    List<String> checkNameAndPath(boolean name, boolean path, Integer siteId);


    /**
     * 根据栏目路径查询对象
     * @Title: findByPath
     * @param path 栏目路径
     * @param siteId 站点ID
     * @param recycle 是否回收站
     * @return: Channel
     */
    Channel findByPath(String path, Integer siteId, Boolean recycle);

    /**
     * 根据自定义字段名称和自定义字段值 父栏目id查询（含所有孙节点）
     * @param lft 父节点lft
     * @param rgt 父节点rgt
     * @param attrName 字段名称
     * @param attrVal 字段值
     * @return
     */
    List<Channel> findByAttr(Integer lft,Integer rgt, String attrName, String attrVal);

    /**
     * 根据栏目路径查询对象
     * @Title: findByPath
     * @param paths 栏目路径
     * @param siteId 站点ID
     * @param recycle 是否回收站
     * @return: Channel
     */
    List<Channel> findByPath(String[] paths, Integer siteId, Boolean recycle);
    
    Integer findBySortNum(Integer siteId,Integer parentId);

    List<Channel> findByIds(Collection<Integer> ids);

    /**
     * 获取站点和是否在回收站的数量
     * @param siteId 站点id
     * @param recycle true 是 false 不是
     * @return 数量
     */
    long getSum(Integer siteId, Boolean recycle);

    /**
     * 通过模型id集合分页查询栏目分页
     * @param pageable  分页对象
     * @param modelIds  栏目模型id集合
     * @return  Page<Channel>
     */
    Page<Channel> getPage(Pageable pageable, List<Integer> modelIds);
    List<Integer> findByParentIds(List<Integer> ids);

    List<Integer> getRecycleIds(Integer siteId);

    List<Channel> findList(Integer siteId,Boolean recycle);
}
