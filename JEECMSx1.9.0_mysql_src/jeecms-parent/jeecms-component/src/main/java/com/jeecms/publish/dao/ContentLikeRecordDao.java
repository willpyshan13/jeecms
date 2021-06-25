/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.publish.dao.ext.ContentLikeRecordDaoExt;
import com.jeecms.publish.domain.ContentLikeRecord;


/**
* @author ljw
* @version 1.0
* @date 2020-06-17
*/
public interface ContentLikeRecordDao extends IBaseDao<ContentLikeRecord, Integer>, ContentLikeRecordDaoExt {

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
}
