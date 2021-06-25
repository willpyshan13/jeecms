/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.system.dao.ext.LinkDaoExt;
import com.jeecms.system.domain.Link;

/**
 * 友情链接 dao接口
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-06-11
 */
public interface LinkDao extends IBaseDao<Link, Integer>, LinkDaoExt {

        /**
         * 查询指定站点和指定友情链接类型的友情链接
         *
         * @param siteId     站点id
         * @param linkTypeId 友情链接类型id
         * @return list
         */
        int countBySiteIdAndLinkTypeIdAndHasDeleted(Integer siteId, Integer linkTypeId, Boolean hasDeleted);
}
