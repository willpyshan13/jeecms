/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.dao.ext;

import com.jeecms.common.page.Paginable;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.dto.ContentSearchDto;
import com.jeecms.content.domain.vo.ContentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 内容扩展查询dao接口
 *
 * @author: tom
 * @date: 2019年5月11日 下午1:47:17
 */
public interface ContentDaoExt {

    /**
     * 内容管理列表分页
     *
     * @param dto      搜索Dto
     * @param pageable 分页对象
     * @return Page 返回的内容分页
     */
    Page<Content> getPage(ContentSearchDto dto, Pageable pageable);

    /**
     * 内容管理列表分页
     *
     * @param dto      搜索Dto
     * @param pageable 分页对象
     * @return Page 返回的内容分页
     */
    Page<ContentVo> getPages(ContentSearchDto dto, Pageable pageable);

    /**
     * 内容管理列表
     *
     * @param dto       搜索Dto
     * @param paginable 列表对象
     * @return List 返回的内容
     */
    List<Content> getList(ContentSearchDto dto, Paginable paginable);

    /**
     * 根据搜索条件得到内容数
     *
     * @param dto 搜索对象
     * @return long
     */
    long getCount(ContentSearchDto dto);

    /**
     * 获取内容数
     *
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @param siteId     站点id
     * @param status     内容状态(1:草稿; 2-初稿 3:流转中; 4:待发布; 5:已发布; 6:退回; 7:下线 8-归档 )
     * @param createType 创建方式（1:直接创建 2:投稿 3:站群推送 4:站群采集 5:复制 6:链接型引用 7:镜像型引用）
     * @return 内容发布数
     */
    long getSum(Date beginTime, Date endTime, Integer siteId, Integer status, Integer createType);

    /**
     * 获取内容数
     *
     * @param siteId  站点id
     * @param recycle true 回收站 false 不是回收站
     * @return 数量
     */
    long getSum(Integer siteId, Boolean recycle);

    /**
     * 根据排序值排序移动
     *
     * @param sortNum   排序值
     * @param move      平移值
     * @param direction true在后半段false为前半段
     * @param sort      true排序值之前，false排序值之后
     */
    void updateSortNum(Integer sortNum, Integer move, boolean direction, boolean sort);

    /**
     * 查找内容
     *
     * @param siteId 站点id
     * @param pcTpl pc模板标识
     * @param mobileTpl 手机模板标识
     * @return List
     */
    long countByTpl(Integer siteId, String pcTpl, String mobileTpl);

    /**
     * 内容管理列表
     * 得到缓存数据
     *
     * @param ids 内容IDs
     * @return List 返回的内容
     */
    List<Content> getListForCache(List<Integer> ids);

    /**
     * 通过内容模型id集合分页查询内容集合
     *
     * @param pageable 分页对象
     * @param modelIds 内容模型id集合
     * @return Page<Content>
     */
    Page<Content> getPage(Pageable pageable, List<Integer> modelIds);

    /**
     * 通过密级id和模型id分页查询内容
     *
     * @param pageable  分页对象集合
     * @param secretIds 密级id集合
     * @param moduleId  模型id
     * @return Page<Content>
     */
    Page<Content> getPageBySecretId(Pageable pageable, List<Integer> secretIds, Integer moduleId);

    /**
     * 通过密级id集合和模型id查询内容数量
     *
     * @param secretIds 密级id集合
     * @param moduleId  模型id
     * @return long
     */
    long getCountBySecretId(List<Integer> secretIds, Integer moduleId);

    /**
     * 通过站点id查询加入回收站的内容id集合
     *
     * @param siteId 站点id
     * @return List<Integer>
     */
    List<Integer> getRecycleIds(Integer siteId);

    /**
     * 通过栏目id查询其底下的所有的内容标题集合
     *
     * @param channelId 栏目id
     * @return List<String>
     */
    List<String> findContentTitles(Integer channelId);

    /**
     * 通过内容id集合查询未被加入回收站未被删除的对应的栏目id集合
     *
     * @param ids 内容id集合
     * @return List<Integer>
     */
    List<Integer> getChannelIds(List<Integer> ids);

    /**
     * 根据栏目id和内容id和创建类型集合查询引用内容id
     *
     * @param channelId    栏目id
     * @param contentId    内容id(源内容id)
     * @param createStatua 创建状态
     * @return long
     */
    long getCount(Integer channelId, Integer contentId, List<Integer> createStatua);

    /**
     * 收益统计
     * @param sortType
     * @return
     */
    Page<Content> getProfitCount(int sortType, Pageable pageable);

    /**
     * 付费统计-内容top10
     * @param sortType
     * @return
     */
    List<Content> getContentTopTen(int sortType);

}
