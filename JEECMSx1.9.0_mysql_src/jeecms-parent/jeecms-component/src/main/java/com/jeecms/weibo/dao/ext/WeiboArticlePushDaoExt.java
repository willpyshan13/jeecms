/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.weibo.dao.ext;

import com.jeecms.weibo.domain.WeiboArticlePush;

import java.util.Date;
import java.util.List;

/**
 * 微博推送DaoExt
* @author ljw
* @version 1.0
* @date 2020-06-22
*/
public interface WeiboArticlePushDaoExt {

    /**
     * 查询推送列表
     * @param start 开始发布时间
     * @param end 结束发布时间
     * @param users 用户集合
     * @param uuids 微博UUId集合
     * @return List
     */
    List<WeiboArticlePush> getList(Date start, Date end, List<Integer> users, List<String> uuids);
}
