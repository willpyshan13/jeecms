package com.jeecms.wechat.dao.ext;

import com.jeecms.wechat.domain.AbstractWeChatInfo;

import java.util.List;

/**
 * @Description:AbstractWeChatInfoDaoExt
 * @author: qqwang
 * @date: 2018年4月16日 上午11:05:40
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */
public interface AbstractWeChatInfoDaoExt {

    /**
     * 查询公众号/小程序集合
     *
     * @param userId 用户id
     * @param siteId 站点id
     * @param type   授权方类型 1-公众号 2-小程序
     * @return 授权方类型 1-公众号 2-小程序
     */
    List<AbstractWeChatInfo> getList(Integer userId, Integer siteId, Short type);

    /**
     * 查询公众号集合
     * @param siteId 站点id
     * @param appids appid集合
     * @return List
     */
    List<AbstractWeChatInfo> getListForWechat(Integer siteId,List<String> appids);
}
