/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.auth.dao;

import com.jeecms.auth.dao.ext.UserIncomeDaoExt;
import com.jeecms.auth.domain.UserIncome;
import com.jeecms.common.base.dao.IBaseDao;

/**
 * @author xiaohui
 * @date 2021/3/29 11:05
 */
public interface UserIncomeDao extends IBaseDao<UserIncome, Integer>, UserIncomeDaoExt {
}
