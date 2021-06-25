/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.publish.dao.ext.ContentPublishRecordDaoExt;
import com.jeecms.publish.domain.ContentPublishRecord;

/**
 * 内容发布统计DAO
* @author ljw
* @version 基于x1.4.0
* @date 2020-06-03
*/
public interface ContentPublishRecordDao extends IBaseDao<ContentPublishRecord, Integer>, ContentPublishRecordDaoExt {

    /**
     * 根据内容ID查询发布记录
     * @param contentId 内容ID
     * @return ContentPublishRecord
     */
    ContentPublishRecord findByContentId(Integer contentId);
}
