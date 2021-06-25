/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.dao;

import java.util.List;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.content.domain.ContentAttrRes;

/**
 * 内容自定义属性-多资源表dao接口
 *
 * @author ljw
 * @version 1.0
 * @date 2019-05-15
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public interface ContentAttrResDao extends IBaseDao<ContentAttrRes, Integer> {

    /**
     * 通过内容自定义id集合查询出多资源集合对象
     *
     * @param ids 内容自定义id集合
     * @Title: findByResIdIn
     * @return: List
     */
    List<ContentAttrRes> findByResIdIn(List<Integer> ids);

    /**
     * 通过资源id和内容自定义属性id查询对象
     *
     * @param ids    资源id集合
     * @param attrId 内容自定义属性id
     * @return List<ContentAttrRes>
     */
    List<ContentAttrRes> findByResIdInAndContentAttrIdEquals(List<Integer> ids, Integer attrId);

    /**
     * 通过附件密级id集合查询
     *
     * @param secretIds 附加密级id集合
     * @return List<ContentAttrRes>
     */
    List<ContentAttrRes> findBySecretIdIn(List<Integer> secretIds);

    /**
     * 通过附件密级id集合查询数量
     *
     * @param secretIds 附件密级id集合
     * @return long
     */
    long countBySecretIdIn(List<Integer> secretIds);
}
