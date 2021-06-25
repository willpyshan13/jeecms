/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.content.domain.ContentLuceneError;

/**
 * 内容索引异常service接口
 *
 * @author: tom
 * @date: 2019年5月27日 上午11:18:59
 */
public interface ContentLuceneErrorService extends IBaseService<ContentLuceneError, Integer> {

    /**
     * 新增索引异常
     * @param contentId 内容id
     * @param op        操作
     */
    void saveError(Integer contentId, Short op);

    /**
     * 判断内容id和操作是索引是否存在异常
     * @param contentId 内容id
     * @param op        操作
     * @return boolean
     */
    boolean existError(Integer contentId, Short op);
}
