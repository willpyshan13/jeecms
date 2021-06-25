/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao.ext;

import com.jeecms.content.domain.vo.ContentPerfVo;
import com.jeecms.publish.domain.ContentPublishRecord;
import com.jeecms.publish.domain.vo.PublishPageVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 内容发布记录统计DAO
* @author ljw
* @version 基于x1.4.0
* @date 2020-06-03
*/
public interface ContentPublishRecordDaoExt {

    /**
     * 获取列表
     * @param siteId 站点ID
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param channelId 栏目ID
     * @param start 开始时间
     * @param end 结束时间
     * @return List
     */
    List<ContentPublishRecord> getList(Integer siteId, Integer orgId, Integer userId, Integer channelId,
                                       Date start, Date end);

    /**
     * 获取列表
     * @param siteId 站点ID
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param channelId 栏目ID
     * @param start 开始时间
     * @param end 结束时间
     * @return List
     */
    Long count(Integer siteId, Integer orgId, Integer userId, Integer channelId,
               Date start, Date end);

    /**
     * 数据表格接口-列表分页
     * @param siteId 站点ID
     * @param publishType 发布类型
     * @param sort true倒序，false 正序
     * @param key 关键字
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页对象
     * @return Page
     */
    Page<PublishPageVo> publishData(Integer siteId, Integer publishType, Boolean sort, String key,
                                           Date startDate, Date endDate, Pageable pageable);

    /**
     * 数据表格接口
     * @param siteId 站点ID
     * @param publishType 发布类型
     * @param sort true倒序，false 正序
     * @param key 关键字
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return List
     */
    List<PublishPageVo> publishDataList(Integer siteId, Integer publishType, Boolean sort, String key,
                                    Date startDate, Date endDate);

    /**
	 * 查询内容列表
	 * @param start 开始发布时间
	 * @param end 结束发布时间
	 * @param channels 栏目集合
	 * @param users 用户集合
	 * @param orgs 组织集合
	 * @param sites 站点集合
	 * @return List
	 */
	List<ContentPerfVo> getList(Date start, Date end, List<Integer> channels,
                                List<Integer> users, List<Integer> orgs, List<Integer> sites);
}
