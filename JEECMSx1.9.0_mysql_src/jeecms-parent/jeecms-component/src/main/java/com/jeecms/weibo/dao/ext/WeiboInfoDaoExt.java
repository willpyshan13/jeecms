/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.weibo.dao.ext;

import com.jeecms.weibo.domain.WeiboInfo;

import java.util.List;

/**
 * 微博DaoExt
* @author ljw
* @version 1.0
* @date 2020-06-22
*/
public interface WeiboInfoDaoExt {

    /**
     * 根据站点ID和微博UID查询
     * @Title: getList
     * @param siteId 站点ID
     * @param uid 微博账户UID
     * @return List
     */
    List<WeiboInfo> getList(Integer siteId, List<String> uid);
}
