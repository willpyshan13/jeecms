/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service;

import com.jeecms.content.domain.ContentAttrRes;

import java.util.List;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;

/**
 * 内容自定义属性-多资源表Service
 *
 * @author ljw
 * @version 1.0
 * @date 2019-05-15
 */
public interface ContentAttrResService extends IBaseService<ContentAttrRes, Integer> {

    /**
     * 通过自内容自定义属性集合删除该集合对应的多资源对象集合
     *
     * @param ids 内容自定义属性Id集合
     * @throws GlobalException 全局异常
     * @Title: deleteByContentAttr
     * @return: void
     */
    void deleteByContentAttrs(List<Integer> ids) throws GlobalException;

    /**
     * 根据资源ID查询密级ID
     *
     * @param resId 资源ID
     * @throws GlobalException 异常
     * @Title: getSecretByRes
     */
    List<Integer> getSecretByRes(Integer resId) throws GlobalException;

    /**
     * 重置密级
     *
     * @param secretIds 密级id
     * @throws GlobalException 全局异常
     */
    void resetAnnexSecret(List<Integer> secretIds) throws GlobalException;

    /**
     * 通过附件密级id集合查询出对应的多资源对象
     *
     * @param secretIds 附件密级id集合
     * @return long
     */
    long getCountBySecretId(List<Integer> secretIds);

    /**
     * 通过资源id和内容自定义属性id查询对象
     *
     * @param resId  资源id
     * @param attrId 内容自定义属性id
     * @return ContentAttrRes
     * @throws GlobalException 全局异常
     */
    ContentAttrRes getSecretByResWithAttrId(Integer resId, Integer attrId) throws GlobalException;
}
