/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.reinsurance.service;

import com.jeecms.common.exception.GlobalException;

/**
 * 分保操作service接口
 * @author: chenming
 * @date: 2020/8/3 15:42
 */
public interface CmsReinsuranceOperatingService {

    /**
     * 关闭分保配置(用于关闭内容密级关闭分保配置)
     * @throws GlobalException  全局异常
     */
    void closeConfig() throws GlobalException;
}
