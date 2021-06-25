/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.weibo.dao.ext;

import com.jeecms.weibo.domain.StatisticsWeiboFans;

import java.util.Date;
import java.util.List;

/**
 * 微博推送DaoExt
* @author ljw
* @version 1.0
* @date 2020-06-22
*/
public interface StatisticsWeiboFansDaoExt {

    /**
     * 粉丝统计数据列表分页
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param uids 微博UID集合
     * @return List
     */
    List<StatisticsWeiboFans> getList(Date startDate, Date endDate, List<String> uids);
}
