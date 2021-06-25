/**
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.auth.dao;

import com.jeecms.auth.dao.ext.CoreUserDaoExt;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.dao.IBaseDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * 管理员DAO层
 * @author: tom
 * @date: 2018年3月1日 下午8:50:32
 */
public interface CoreUserDao extends IBaseDao<CoreUser, Integer>, CoreUserDaoExt {

    /**
     * 依据用户名查询单个对象，dao实现不需要实现该方法
     *
     * @param  username 用户名
     * @param  flag 删除标识
     * @return CoreUser
     * @see com.jeecms.auth.domain.CoreUser
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    CoreUser findByUsernameAndHasDeleted(String username, boolean flag);

    /**
     * 根据邮箱查询单个对象
     *
     * @param  email 邮箱
     * @param  flag 删除标识
     * @return CoreUser
     * @see com.jeecms.auth.domain.CoreUser
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    CoreUser findByEmailAndHasDeleted(String email, boolean flag);

    /**
     * 根据手机号查询单个对象
     *
     * @param usePhone 手机号
     * @param  flag 删除标识
     * @return CoreUser
     * @see com.jeecms.auth.domain.CoreUser
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    CoreUser findByTelephoneAndHasDeleted(String usePhone, boolean flag);

    /**
     * 根据邮箱和用户名查询单个对象
     *
     * @param  email 邮箱
     * @param  username 用户名
     * @param  flag 删除标识
     * @return CoreUser
     * @see com.jeecms.auth.domain.CoreUser
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    CoreUser findByEmailAndHasDeletedOrUsernameAndHasDeleted(String email, boolean flag, String username, boolean flag1);

    /**
     * 通过id集合查询未被删除的用户
     * @param ids   用户id集合
     * @param hasDeleted    是否逻辑删除
     * @return List<CoreUser>
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    List<CoreUser> findByIdInAndHasDeleted(List<Integer> ids, Boolean hasDeleted);

    /**
     * 通过id集合查询未被删除开启的管理员
     * @param ids   用户id集合
     * @param admin 是否管理员
     * @param enable 是否开启
     * @param hasDeleted    是否逻辑删除
     * @return List<CoreUser>
     */
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    List<CoreUser> findByIdInAndAdminAndEnabledAndHasDeleted(List<Integer> ids, Boolean admin, Boolean enable, Boolean hasDeleted);

    /**
     * 批量修改密码
     * @param passwd 加密后密码
     */
    @Modifying
    @Query(value = "update  CoreUser user set user.password=?1,user.salt=?2 where user.hasDeleted=false ")
    void updatePasswdBatch(String passwd, String saltStr);
}
