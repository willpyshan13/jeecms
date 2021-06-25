/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.protection.service;

import com.jeecms.common.exception.GlobalException;

import java.util.List;

/**
 * 等级保护service
 * @author: chenming
 * @date: 2020/7/30 15:20
 */
public interface GradeProtectionService {
    /**
     * 校验是否开启等级保护
     * @return true->开启，false->关闭
     * @throws GlobalException  全局异常
     */
    boolean checkGradeProtection();

    /**
     * 解密字符串集合
     * @param requestStrList    请求字符串集合
     * @return  List<String>
     */
    List<String> decryptStrList(List<String> requestStrList);

    /**
     * 解密字符串
     * @param requestStr    请求字符串
     * @return  String
     */
    String decryptStr(String requestStr);
}
