/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.content.dao.ext.ContentDaoExt;
import com.jeecms.content.domain.Content;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * 内容主体dao接口
 *
 * @author: chenming
 * @date: 2019年5月6日 下午2:32:49
 */
public interface ContentDao extends IBaseDao<Content, Integer>, ContentDaoExt {

    /**
     * 根据栏目id数组查询内容集合
     *
     * @param channelIds 栏目id集合
     * @param hasDeleted 是否逻辑删除
     * @return List<Content>
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    List<Content> findByChannelIdInAndHasDeleted(Integer[] channelIds, Boolean hasDeleted);

    /**
     * 通过内容id、是否加入回收站标识查询内容
     *
     * @param contentId  内容id
     * @param recycle    是否加入回收站
     * @param hasDeleted 是否逻辑删除
     * @return Content
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Content findByIdAndRecycleAndHasDeleted(Integer contentId, Boolean recycle, Boolean hasDeleted);

    /**
     * 得到排序值最大的内容
     *
     * @return Content
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Content findFirstByOrderBySortNumDesc();

    /**
     * 按栏目和内容id查询
     *
     * @param channelId 栏目id
     * @param types     创建类型集合
     * @return List<Content>
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    List<Content> findByChannelIdAndOriContentIdAndCreateTypeIn(Integer channelId, Integer contentId, List<Integer> types);

    /**
     * 通过栏目id集合和是否加入回收站查询数量
     *
     * @param chanenlIds 栏目id集合
     * @param recycle    是否加入回收站
     * @param hasDeleted 是否逻辑删除
     * @return long
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    long countByChannelIdInAndRecycleAndHasDeleted(Integer[] chanenlIds, Boolean recycle, Boolean hasDeleted);

    /**
     * @param originContentId 原内容ID
     * @param recycle         是否进入回收站
     * @param hasDeleted      是否删除
     * @return
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    List<Content> findByOriContentIdAndRecycleAndHasDeleted(Integer originContentId, Boolean recycle, Boolean hasDeleted);

}
