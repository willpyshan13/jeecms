/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.auth.dao.impl;

import com.jeecms.auth.dao.ext.UserIncomeDaoExt;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author xiaohui
 * @date 2021/3/29 11:07
 */
public class UserIncomeDaoImpl implements UserIncomeDaoExt {

    private EntityManager em;

    @PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }
}
