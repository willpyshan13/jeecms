/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.auth.domain;

import com.jeecms.common.base.domain.AbstractDomain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author xiaohui
 * @date 2021/3/29 9:14
 */
@Entity
@Table(name = "jc_sys_user_income")
public class UserIncome extends AbstractDomain<Integer> implements Serializable {

    private Integer id;

    /**
     * 用户余额（周期结算后需更新）
     */
    private Long balance;

    /**
     * 累计收益（不受结算周期影响）
     */
    private Long totalAmount;

    /**
     * 累计提现金额 （单位毫 1分=100毫）
     */
    private Long withdrawalAmount;

    /**
     * 累计打赏金额（单位毫 1分=100毫）
     */
    private Long rewardAmount;

    /**
     * 累计内容付费金额（单位毫 1分=100毫）
     */
    private Long paidAmount;

    /**
     * 待结算金额（单位毫 1分=100毫）
     */
    private Long waitingSettlementAmount;

    /**
     * 用户
     **/
    private CoreUser user;

    public UserIncome() {
    }

    public UserIncome(CoreUser user) {
        this.balance = 0L;
        this.totalAmount = 0L;
        this.withdrawalAmount = 0L;
        this.rewardAmount = 0L;
        this.paidAmount = 0L;
        this.waitingSettlementAmount = 0L;
        this.user = user;
    }

    public UserIncome(Integer id, Long balance, Long totalAmount, Long withdrawalAmount, Long rewardAmount, Long paidAmount, Long waitingSettlementAmount, CoreUser user) {
        this.id = id;
        this.balance = balance == null ? 0 : balance;
        this.totalAmount = totalAmount == null ? 0 : totalAmount;
        this.withdrawalAmount = withdrawalAmount == null ? 0 : withdrawalAmount;
        this.rewardAmount = rewardAmount == null ? 0 : rewardAmount;
        this.paidAmount = paidAmount == null ? 0 : paidAmount;
        this.waitingSettlementAmount = waitingSettlementAmount == null ? 0 : waitingSettlementAmount;
        this.user = user;
    }

    @Override
    @Id
    @TableGenerator(name = "jc_sys_user_income", pkColumnValue = "jc_sys_user_income", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_sys_user_income")
    @Column(name = "id", nullable = false, length = 11)
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "balance")
    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Column(name = "total_amount")
    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Column(name = "withdrawal_amount")
    public Long getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Long withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    @Column(name = "reward_amount")
    public Long getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Long rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    @Column(name = "paid_amount")
    public Long getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Long paidAmount) {
        this.paidAmount = paidAmount;
    }

    @Column(name = "waiting_settlement_amount")
    public Long getWaitingSettlementAmount() {
        return waitingSettlementAmount;
    }

    public void setWaitingSettlementAmount(Long waitingSettlementAmount) {
        this.waitingSettlementAmount = waitingSettlementAmount;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    public CoreUser getUser() {
        return user;
    }

    public void setUser(CoreUser user) {
        this.user = user;
    }


    @Override
    public String toString() {
        return "UserIncome{" +
                "createTime=" + createTime +
                ", createUser='" + createUser + '\'' +
                ", updateTime=" + updateTime +
                ", updateUser='" + updateUser + '\'' +
                ", id=" + id +
                ", balance=" + balance +
                ", totalAmount=" + totalAmount +
                ", withdrawalAmount=" + withdrawalAmount +
                ", rewardAmount=" + rewardAmount +
                ", paidAmount=" + paidAmount +
                ", waitingSettlementAmount=" + waitingSettlementAmount +
                ", user=" + user +
                '}';
    }
}
