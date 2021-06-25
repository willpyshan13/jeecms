/**
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.publish.domain.ContentLikeRecord;
import com.jeecms.publish.domain.vo.ContentLikeVo;
import com.jeecms.system.domain.vo.MassScoreVo;

import java.util.Date;
import java.util.List;

/**
* @author ljw
* @version 1.0
* @date 2020-06-17
*/
public interface ContentLikeRecordService extends IBaseService<ContentLikeRecord, Integer>{


    /**
     * 根据用户Id以及内容Id删除
     * @param userId 用户ID
     * @param contentId 内容ID
     */
    void deleteByUserIdAndContentId(Integer userId, Integer contentId);

    /**
     * 根据用户Id以及内容Id删除
     * @param cookie cookie
     * @param contentId 内容ID
     */
    void deleteByCookieAndContentId(String cookie, Integer contentId);

    /**
     * 计数统计
     * @param type true栏目，false内容
     * @param start 开始时间
     * @param end 结束时间
     * @return List
     */
    List<ContentLikeVo> count(boolean type, Date start, Date end);

    /**
     * 流量计算
     * @param contentId 内容ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return SysAccessRecord
     */
    List<MassScoreVo> massCount(List<Integer> contentId, Date startDate, Date endDate);
}
