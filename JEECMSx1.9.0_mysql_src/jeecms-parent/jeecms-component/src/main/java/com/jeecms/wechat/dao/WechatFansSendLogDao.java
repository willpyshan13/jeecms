/**
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.wechat.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.wechat.dao.ext.WechatFansSendLogDaoExt;
import com.jeecms.wechat.domain.WechatFansSendLog;

import java.util.List;

/**
 * 粉丝推送Dao
 * @author ljw
 * @version 1.0
 * @date 2018-08-09
 */
public interface WechatFansSendLogDao extends IBaseDao<WechatFansSendLog, Integer>, WechatFansSendLogDaoExt {

    /**
     * 根据留言获取留言数
     * @Title: findByOpenid
     * @param openid 粉丝OPENID
     * @param flag 标识
     * @return List
     */
    List<WechatFansSendLog> findByOpenIdAndHasDeleted(String openid, boolean flag);
}
