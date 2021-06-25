/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.auth.service;

import com.jeecms.auth.domain.UserIncome;
import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;

/**
 * @author xiaohui
 * @date 2021/3/29 11:09
 */
public interface UserIncomeService extends IBaseService<UserIncome, Integer> {

    /**
     * 收益修改
     * @param userId           用户id
     * @param userAmount       用户余额（周期结算后需更新）
     * @param payAmount        待结算金额（单位毫 1分=100毫
     * @param paidAmount       累计内容付费金额（单位毫 1分=100毫）
     * @param rewardAmount     累计打赏金额（单位毫 1分=100毫）
     * @param totalAmount      累计收益（不受结算周期影响）
     * @param withdrawalAmount 累计提现金额 （单位毫 1分=100毫）
     * @throws GlobalException 全局异常
     */
    void modify(Integer userId, Long userAmount, Long payAmount, Long rewardAmount, Long paidAmount, Long totalAmount, Long withdrawalAmount) throws GlobalException;
}
