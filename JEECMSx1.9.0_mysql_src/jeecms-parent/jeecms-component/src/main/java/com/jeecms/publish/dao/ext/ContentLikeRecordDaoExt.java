/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao.ext;

import com.jeecms.publish.domain.vo.ContentLikeVo;
import com.jeecms.system.domain.vo.MassScoreVo;

import java.util.Date;
import java.util.List;

/**
 * 内容发布记录统计DAO
* @author ljw
* @version 基于x1.4.0
* @date 2020-06-03
*/
public interface ContentLikeRecordDaoExt {

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
