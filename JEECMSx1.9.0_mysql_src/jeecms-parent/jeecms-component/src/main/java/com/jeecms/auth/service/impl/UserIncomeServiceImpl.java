/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.auth.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jeecms.auth.dao.UserIncomeDao;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.UserIncome;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.auth.service.UserIncomeService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.exception.SystemExceptionInfo;
import com.jeecms.common.util.LockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;

/**
 * @author xiaohui
 * @date 2021/3/29 11:10
 */
@Service
@Transactional(rollbackFor = Exception.class)
@ConditionalOnMissingClass(value = "com.jeecms.component.redis.util.RedisUtil")
public class UserIncomeServiceImpl extends BaseServiceImpl<UserIncome, UserIncomeDao, Integer> implements UserIncomeService {

    private final static Logger logger = LoggerFactory.getLogger(UserIncomeServiceImpl.class);

    @Autowired
    private CoreUserService coreUserService;

    @Autowired
    private PlatformTransactionManager jpaTransactionManager;

    @Override
    public void modify(Integer userId, Long userAmount, Long payAmount, Long rewardAmount, Long paidAmount, Long totalAmount, Long withdrawalAmount) throws GlobalException {
        modifyLock(userId, userAmount, payAmount, rewardAmount, paidAmount, totalAmount, withdrawalAmount);
    }

    /**
     * 收益修改
     *
     * @param userId           用户id
     * @param userAmount       用户余额（周期结算后需更新）
     * @param payAmount        待结算金额（单位毫 1分=100毫
     * @param paidAmount       累计内容付费金额（单位毫 1分=100毫）
     * @param rewardAmount     累计打赏金额（单位毫 1分=100毫）
     * @param totalAmount      累计收益（不受结算周期影响）
     * @param withdrawalAmount 累计提现金额 （单位毫 1分=100毫）
     * @throws GlobalException 全局异常
     */
    protected void modifyLock(Integer userId, Long userAmount, Long payAmount, Long rewardAmount, Long paidAmount, Long totalAmount, Long withdrawalAmount) throws GlobalException {
        LockUtil.lock("income" + userId);
        try {
            DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transaction = jpaTransactionManager.getTransaction(defaultTransactionDefinition);
            logger.debug("");
            logger.debug("");
            logger.debug("");
            logger.debug(Thread.currentThread().getName());
            logger.debug("用户id: " + userId);
            logger.debug("用户余额: " + userAmount);
            logger.debug("待结算金额: " + payAmount);
            logger.debug("累计内容付费金额: " + rewardAmount);
            logger.debug("累计打赏金额: " + paidAmount);
            logger.debug("累计收益: " + totalAmount);
            logger.debug("累计提现金额: " + withdrawalAmount);
            logger.debug("==============================================================================================================");
            logger.debug("进入时间：" + DateUtil.format(LocalDateTime.now(), "HH:mm:ss.SSS"));
            logger.debug("修改前");
            logger.debug(StrUtil.format("UserId:[{}]", userId));

            try {
                CoreUser user = coreUserService.findById(userId);
                UserIncome income = null;
                if (user.getUserIncome() != null) {
                    income = findById(user.getUserIncome().getId());
                    logger.debug(StrUtil.format("  余　　额:[{}] 消费:[{}]", income.getBalance(), userAmount));
                }
                if (income == null) {
                    UserIncome userIncome = new UserIncome(null, userAmount, totalAmount, withdrawalAmount, rewardAmount, paidAmount, payAmount, user);
                    save(userIncome);
                } else {
                    if (userAmount != null) {
                        long balance = (income.getBalance() == null ? 0 : income.getBalance()) + userAmount;
                        if (balance < 0) {
                            throw new RuntimeException("余额不足");
                        }
                        income.setBalance(balance);
                    }
                    if (payAmount != null) {
                        long amount = (income.getWaitingSettlementAmount() == null ? 0 : income.getWaitingSettlementAmount()) + payAmount;
                        logger.debug(StrUtil.format("  待结算计算前余额：[{}]", income.getWaitingSettlementAmount()));
                        logger.debug(StrUtil.format("  待结算计算后余额：[{}]", amount));
                        income.setWaitingSettlementAmount(amount > 0 ? amount : 0);
                    }
                    if (rewardAmount != null) {
                        income.setRewardAmount(rewardAmount + (income.getRewardAmount() == null ? 0 : income.getRewardAmount()));
                    }
                    if (paidAmount != null) {
                        income.setPaidAmount(paidAmount + (income.getPaidAmount() == null ? 0 : income.getPaidAmount()));
                    }
                    if (totalAmount != null) {
                        income.setTotalAmount(totalAmount + (income.getTotalAmount() == null ? 0 : income.getTotalAmount()));
                    }
                    if (withdrawalAmount != null) {
                        income.setWithdrawalAmount(withdrawalAmount + (income.getWithdrawalAmount() == null ? 0 : income.getWithdrawalAmount()));
                    }

                    logger.debug(StrUtil.format("  结算后余额:[{}] ", income.getBalance()));
                    update(income);
                    super.flush();
                }

                logger.debug("入库后");
                logger.debug(StrUtil.format("  余　　额:[{}]", get(userId).getBalance()));
                logger.debug("结束时间：" + DateUtil.format(LocalDateTime.now(), "HH:mm:ss.SSS"));
                logger.debug("==============================================================================================================");
                logger.debug("");
                logger.debug("");
                logger.debug("");

                jpaTransactionManager.commit(transaction);
            } catch (Exception e) {
                logger.error("收益修改失败，{}", e.getMessage());
                jpaTransactionManager.rollback(transaction);
                throw new GlobalException(new SystemExceptionInfo(SystemExceptionEnum.UNKNOWN_ERROR.getDefaultMessage(), SystemExceptionEnum.UNKNOWN_ERROR.getCode()));
            }
        } finally {
            LockUtil.unlock("income" + userId);
        }
    }

}
