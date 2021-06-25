/*
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.auth.dao.ext;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.vo.UserStatisticVO;
import com.jeecms.common.page.Paginable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 管理员DAO扩展层
 *
 * @author: tom
 * @date: 2018年3月1日 下午8:48:23
 */
public interface CoreUserDaoExt {

    /**
     * {@docRoot} 管理员列表分页
     *
     * @param enabled      是否启用
     * @param orgids       组织ID集合
     * @param roleids      角色ID集合
     * @param key          关键字
     * @param isAdmin      是否管理员
     * @param checkStatus  审核状态
     * @param groupId      会员组ID
     * @param levelId      用户等级ID
     * @param userSecretId 人员密级ID
     * @param sourceSiteId 来源站点ID
     * @param pageable     分页对象
     * @return ResponseInfo
     * @Title pageUser
     * @see com.jeecms.auth.domain.CoreUser
     */
    Page<CoreUser> pageUser(Boolean enabled, List<Integer> orgids, List<Integer> roleids,
                            String key, Boolean isAdmin, Short checkStatus, Integer groupId, Integer levelId,
                            Integer userSecretId, List<Integer> sourceSiteId,
                            Pageable pageable);

    /**
     * {@docRoot} 管理员列表
     *
     * @param enabled       是否启用
     * @param orgids        组织ID集合
     * @param roleids       角色ID集合
     * @param key           关键字
     * @param isAdmin       是否管理员
     * @param checkStatus   审核状态
     * @param groupId       会员组ID
     * @param levelId       用户等级ID
     * @param secretId      用户密级ID
     * @param sourceSiteIds 站点来源IDs
     * @param paginable     取条数对象
     * @return List
     * @Title findList
     * @see com.jeecms.auth.domain.CoreUser
     */
    List<CoreUser> findList(Boolean enabled, List<Integer> orgids, List<Integer> roleids,
                            String key, Boolean isAdmin, Short checkStatus, Integer groupId, Integer levelId,
                            Integer secretId, List<Integer> sourceSiteIds,
                            Paginable paginable);

    /**
     * 微信端查询管理员列表分页
     *
     * @param orgId    组织id
     * @param roleid   角色id
     * @param username 用户名或真实姓名
     * @param pageable 分页对象
     * @param notIds   用户集合(不包含的用户)
     * @Title: pageWechat
     * @return: Page
     */
    Page<CoreUser> pageWechat(Integer orgId, Integer roleid, String username,
                              Pageable pageable, List<Integer> notIds);

    /**
     * 微信端查询管理员列表分页
     *
     * @param ids      用户id集合
     * @param orgId    组织id
     * @param roleid   角色id
     * @param username 用户名或真实姓名
     * @Title: pageWechat
     * @return: Page
     */
    List<CoreUser> listWechat(List<Integer> ids, Integer orgId, Integer roleid, String username);

    /**
     * 获取会员数
     *
     * @param beginTime   开始时间
     * @param endTime     结束时间
     * @param checkStatus 用户审核状态(1审核通过、2审核不通过 0待审核)
     * @param siteId      站点id
     * @return 会员数
     */
    long getUserSum(Date beginTime, Date endTime, Integer siteId, Short checkStatus);

    /**
     * 查询安全管理或审计管理员数量
     *
     * @param ids    用户id集合
     * @param status 1-安全管理员、2-审计管理员
     * @return long
     */
    long getSafeOrAuditUser(List<Integer> ids, Integer status);

    /**
     * 查询安全管理员或审计管理员
     *
     * @param status  1-安全管理员、2-审计管理员
     * @param enabled true->开启，false->关闭
     * @return List<CoreUser>
     */
    List<CoreUser> getSafeOrAuditUser(Integer status, Boolean enabled);

    /**
     * 通过状态查询安全管理员或审计管理员id集合
     *
     * @param status 1-安全管理员，2-审计管理员
     * @return List<Integer>
     */
    List<Integer> getSafeOrAuditUserIds(Integer status);

    /**
     * 分页查询三元管理用户
     *
     * @param orgids   组织+该组织子集id集合
     * @param roleids  角色+该角色子集id集合
     * @param key      用户名/真实姓名
     * @param notIds   不包含的id集合
     * @param pageable 分页对象
     * @return Page<CoreUser>
     */
    Page<CoreUser> pageSafeManageUser(List<Integer> orgids, List<Integer> roleids, String key, List<Integer> notIds, Pageable pageable);

    /**
     * 用户密级设置列表
     *
     * @param set          true已设置，false未设置
     * @param enabled      true启用，false禁用
     * @param orgIds       组织IDs
     * @param roleIds      角色IDs
     * @param key          用户名/邮箱/电话/真名/座机
     * @param userSecretId 用户密级ID
     * @param pageable     分页对象
     * @return Page<CoreUser>
     */
    Page<CoreUser> pageReinsurance(boolean set, Boolean enabled, List<Integer> orgIds,
                                   List<Integer> roleIds, String key, Integer userSecretId, Pageable pageable);

    /**
     * 查询安全管理员或审计管理员
     *
     * @param status  1-安全管理员、2-审计管理员
     * @param enabled true->开启，false->关闭
     * @return List<CoreUser>
     */
    List<CoreUser> getReinsuranceSafeOrAuditUser(Integer status, Boolean enabled);

    /**
     * 查询用户账户统计
     *
     * @return
     */
    UserStatisticVO getUserStatisticVO();

    /**
     * 用户账户统计分页（按余额）
     *
     * @param userName
     * @param orderType
     * @param pageable
     * @return
     */
    Page<CoreUser> getUserStatisticPage(String userName, Integer orderType, Pageable pageable);

    List<CoreUser> getUserStatisticList(String userName, Integer orderType, Boolean deleteFlag);

    /**
     * 付费统计-用户top10
     *
     * @param sortType
     * @return
     */
    List<CoreUser> getUserTopTen(int sortType);

}
