package com.jeecms.content.service;

import com.jeecms.channel.domain.Channel;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.page.Paginable;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.dto.ContentSearchDto;
import com.jeecms.content.domain.vo.ContentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 内容查询service接口
 *
 * @author: chenming
 * @date: 2019年8月5日 上午10:38:39
 */
public interface ContentGetService {

    /**
     * 内容管理列表
     *
     * @param dto      搜索Dto
     * @param pageable 分页对象
     * @return Page
     * @throws GlobalException 异常
     * @Title: getPage
     */
    Page<ContentVo> getPage(ContentSearchDto dto, Pageable pageable) throws GlobalException;

    /**
     * 内容列表分页
     *
     * @param dto      搜索Dto
     * @param pageable 分页对象
     * @return Page
     * @Title: getPages
     */
    Page<Content> getPages(ContentSearchDto dto, Pageable pageable);

    /**
     * 内容列表分页
     *
     * @param dto       搜索Dto
     * @param paginable 列表对象
     * @return List 集合
     * @Title: getList
     */
    List<Content> getList(ContentSearchDto dto, Paginable paginable);

    /**
     * 查询栏目下内容数
     *
     * @param channel      栏目
     * @param containChild 是否包含子栏目内容
     * @Title: getCountByChannel
     * @return: Integer
     */
    Integer getCountByChannel(Channel channel, boolean containChild);

    /**
     * 搜索到内容的个数
     *
     * @param dto 内容搜索Dto
     * @Title: getCount
     * @return: long
     */
    long getCount(ContentSearchDto dto);

    /**
     * 发布内容数量
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param siteId    站点id
     * @return 发布内容数量
     */
    long getReleaseSum(Date beginTime, Date endTime, Integer siteId);

    /**
     * 投稿数
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param siteId    站点id
     * @return 投稿数
     */
    long getSubmissionSum(Date beginTime, Date endTime, Integer siteId);

    /**
     * 通过内容id集合查询关联的栏目id集合
     *
     * @param contentIds 内容id 集合
     * @return List<Integer>
     */
    List<Integer> getChannelIds(List<Integer> contentIds);
}
