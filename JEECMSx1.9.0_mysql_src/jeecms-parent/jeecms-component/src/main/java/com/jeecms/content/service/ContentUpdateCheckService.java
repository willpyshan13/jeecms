/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.content.domain.dto.ContentUpdateDto;

/**
 * 内容扩展的service接口
 * @author: chenming
 * @date: 2020/3/31 17:15
 */
public interface ContentUpdateCheckService {

    /**
     * 内容修改校验
     * @param dto       前台传入的dto
     * @throws GlobalException  全局异常
     */
    void checkUpdate(ContentUpdateDto dto) throws GlobalException;
}
