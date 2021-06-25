package com.jeecms.auth.domain.vo;


/**
 * @author pss
 * @version 1.0
 * @date 2021/3/15 9:32
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class UserStatisticVO {
    /**
     * 用户总余额
     **/
    private Long balance;
    /**
     * 用户总提现金额
     **/
    private Long withdrawalAmount;
    /**
     * 用户累计总收益
     **/
    private Long userTotalAmount;
    /**
     * 平台累计总收益
     **/
    private Long platformTotalAmount;

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Long withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public Long getUserTotalAmount() {
        return userTotalAmount;
    }

    public void setUserTotalAmount(Long userTotalAmount) {
        this.userTotalAmount = userTotalAmount;
    }

    public Long getPlatformTotalAmount() {
        return platformTotalAmount;
    }

    public void setPlatformTotalAmount(Long platformTotalAmount) {
        this.platformTotalAmount = platformTotalAmount;
    }

    @Override
    public String toString() {
        return "UserStatisticVO{" +
                "balance='" + balance + '\'' +
                ", withdrawalAmount='" + withdrawalAmount + '\'' +
                ", userTotalAmount='" + userTotalAmount + '\'' +
                ", platformTotalAmount='" + platformTotalAmount + '\'' +
                '}';
    }

}
