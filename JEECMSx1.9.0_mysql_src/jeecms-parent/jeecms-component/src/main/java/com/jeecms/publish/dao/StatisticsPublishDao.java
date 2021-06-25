/**
** @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/

package com.jeecms.publish.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.publish.dao.ext.StatisticsPublishDaoExt;
import com.jeecms.publish.domain.StatisticsPublish;


/**
* @author ljw
* @version 1.0
* @date 2020-06-04
*/
public interface StatisticsPublishDao extends IBaseDao<StatisticsPublish, Integer>, StatisticsPublishDaoExt {

}
