package com.jeecms.auth.service;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.auth.constants.AuthConstant.LoginFailProcessMode;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.dto.CoreUserDto;
import com.jeecms.auth.domain.dto.UserManagerDto;
import com.jeecms.auth.domain.vo.UserStatisticVO;
import com.jeecms.common.base.domain.ThirdPartyResultDTO;
import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.member.domain.dto.MemberPwdDto;
import com.jeecms.member.domain.dto.MemberRegisterDto;
import com.jeecms.member.domain.dto.MobileMemberDto;
import com.jeecms.member.domain.dto.PcMemberDto;
import com.jeecms.member.domain.vo.MemberInfoVo;
import com.jeecms.resource.domain.dto.ResourcesSpaceShareDto;
import com.jeecms.system.domain.dto.BeatchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CoreUser service接口
 *
 * @author: tom
 * @date: 2018年1月24日 上午10:10:01
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public interface CoreUserService extends IBaseService<CoreUser, Integer> {

    /**
     * 管理员列表分页
     *
     * @param enabled      是否启用
     * @param orgids       组织ID集合
     * @param roleids      角色ID集合
     * @param key          用户名/邮箱/电话/真名/座机
     * @param isAdmin      是否管理员
     * @param checkStatus  用户审核状态(1审核通过、2审核不通过 0待审核)
     * @param groupId      用户组ID
     * @param levelId      等级ID
     * @param userSecretId 密级ID
     * @param sourceSiteId 来源站点IDs
     * @param pageable     分页对象
     * @Title pageUser
     * @return: Page
     */
    Page<CoreUser> pageUser(Boolean enabled, List<Integer> orgids,
                            List<Integer> roleids, String key, Boolean isAdmin, Short checkStatus, Integer groupId,
                            Integer levelId, Integer userSecretId, List<Integer> sourceSiteId, Pageable pageable);

    /**
     * 管理员列表
     *
     * @param enabled       是否启用
     * @param orgids        组织ID集合
     * @param roleids       角色ID集合
     * @param key           用户名/邮箱/电话/真名/座机
     * @param isAdmin       是否管理员
     * @param checkStatus   用户审核状态(1审核通过、2审核不通过 0待审核)
     * @param groupId       用户组ID
     * @param levelId       等级ID
     * @param secretId      用户密级ID
     * @param sourceSiteIds 来源站点IDs
     * @param paginable     列表对象
     * @Title findList
     * @return: List
     */
    List<CoreUser> findList(Boolean enabled, List<Integer> orgids,
                            List<Integer> roleids, String key, Boolean isAdmin,
                            Short checkStatus, Integer groupId, Integer levelId,
                            Integer secretId, List<Integer> sourceSiteIds, Paginable paginable);

    /**
     * 根据用户名查找
     *
     * @param username 用户名
     * @Title: findByUsername
     * @return: CoreUser
     */
    CoreUser findByUsername(String username);

    /**
     * 查找用户并授权（查询用户所拥有的权限并设置用户权限标识字段）
     *
     * @param username 用户名
     * @Title: findByUsernameAndAuth
     * @return: CoreUser
     */
    CoreUser findByUsernameAndAuth(String username);

    /**
     * 保存管理员
     *
     * @param bean 用户
     * @return 用户信息
     * @throws Exception 全局异常
     */
    ResponseInfo saveUser(CoreUserDto bean) throws Exception;

    /**
     * 修改管理员
     *
     * @param bean 用户对象
     * @return ResponseInfo
     * @throws Exception 全局异常
     */
    ResponseInfo updateUser(CoreUserDto bean) throws Exception;

    /**
     * 启用用户
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    ResponseInfo enableUser(BeatchDto dto) throws GlobalException;

    /**
     * 禁用用户
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    ResponseInfo disableUser(BeatchDto dto) throws GlobalException;

    /**
     * 用户登录
     *
     * @param username     用户名
     * @param ip           ip
     * @param loginSuccess 是否登录成功
     * @param processMode  登录错误处理方式
     * @throws GlobalException GlobalException
     * @Title: userLogin
     */
    void userLogin(String username, String ip, boolean loginSuccess,
                   LoginFailProcessMode processMode) throws GlobalException;

    /**
     * 验证用户名是否重复
     *
     * @param validName 用户名
     * @return
     */
    Boolean validName(String validName);

    /**
     * 验证邮箱是否重复
     *
     * @param validMail 验证的邮箱
     * @param id        用户ID
     * @return
     */
    Boolean validMail(String validMail, Integer id);

    /**
     * 验证手机是否重复
     *
     * @param validPhone 验证的手机号
     * @param id         用户ID
     * @return
     */
    Boolean validPhone(String validPhone, Integer id);

    /**
     * 获取当前登录用户下的路由权限数据集
     *
     * @param username 用户名
     * @return Map 响应
     * @throws GlobalException 异常
     */
    Map<String, Object> routingTree(String username, HttpServletRequest request) throws GlobalException;

    /**
     * 移除成员
     *
     * @param dto 用户管理Dto
     * @throws GlobalException 全局异常
     * @Title: removeUser
     * @return: ResponseInfo
     */
    ResponseInfo removeUser(UserManagerDto dto) throws GlobalException;

    /**
     * 移动成员
     *
     * @param dto 用户管理Dto
     * @throws GlobalException 全局异常
     * @Title: removeUser
     * @return: ResponseInfo
     */
    ResponseInfo moveUser(UserManagerDto dto) throws GlobalException;

    /**
     * 添加成员
     *
     * @param dto 用户管理Dto
     * @throws GlobalException 全局异常
     * @Title: addUser
     * @return: ResponseInfo
     */
    ResponseInfo addUser(UserManagerDto dto) throws GlobalException;

    /**
     * 审核通过
     *
     * @param ids 用户ids
     * @throws GlobalException 全局异常
     * @Title: auditON
     * @return: ResponseInfo
     */
    ResponseInfo auditON(Integer[] ids) throws GlobalException;

    /**
     * 审核不通过
     *
     * @param dto 传输
     * @throws GlobalException 全局异常
     * @Title: auditON
     * @return: ResponseInfo
     */
    ResponseInfo auditOFF(BeatchDto dto) throws GlobalException;

    /**
     * 后台保存会员
     *
     * @param json 自定义字段
     * @return 用户信息
     * @throws GlobalException 全局异常
     * @return: ResponseInfo
     */
    ResponseInfo saveMember(JSONObject json, CoreUser user) throws GlobalException;

    /**
     * 修改会员
     *
     * @param bean 用户对象
     * @return ResponseInfo ResponseInfo
     * @throws GlobalException 全局异常
     */
    void updateMember(CoreUser bean, JSONObject json) throws GlobalException;

    /**
     * 共享设置修改默认共享人
     *
     * @param userId 用户id
     * @param dto    共享dto
     * @return 用户
     * @throws GlobalException 异常
     */
    CoreUser updateMember(Integer userId, ResourcesSpaceShareDto dto) throws GlobalException;

    /**
     * 会员中心修改会员
     *
     * @param dto 会员基本信息Dto
     * @return ResponseInfo 响应
     * @throws Exception 全局异常
     */
    ResponseInfo updatePCMember(MemberRegisterDto dto) throws Exception;

    /**
     * 修改用户密码
     *
     * @param memberInfoDto 会员修改密码DtoDto
     * @param userId        用户ID
     * @throws GlobalException 全局异常
     */
    ResponseInfo updatePStr(MemberPwdDto memberInfoDto, Integer userId) throws GlobalException;

    /**
     * 会员审核关闭时，需要将待审核会员自动通过，需调此方法
     *
     * @throws GlobalException 全局异常
     * @Title: changeUserStatus
     * @return: void
     */
    void changeUserStatus() throws GlobalException;


    /**
     * 重置登录错误次数，账户安全设置更改的时候需要重新计算用户登录错误次数
     *
     * @throws GlobalException 全局异常
     * @Title: resetLoginErrorCount
     * @return: void
     */
    void resetLoginErrorCount() throws GlobalException;

    /**
     * 验证密码
     *
     * @param pwd      密码
     * @param username 用户名
     * @throws GlobalException 全局异常
     * @Title: validPwd
     * @return: Boolean
     */
    ThirdPartyResultDTO validPwd(String pwd, String username) throws GlobalException;

    /**
     * 修改积分配置
     *
     * @param siteId 站点ID
     * @param map    积分配置   @see com.jeecms.system.domain.CmsSiteConfig
     * @throws GlobalException 异常
     * @Title: updateScore
     */
    ResponseInfo updateScore(Integer siteId, Map<String, String> map) throws GlobalException;

    /**
     * 批量删除用户
     *
     * @param dto   批量操作Dto
     * @param orgId 组织ID
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    ResponseInfo deleteUser(BeatchDto dto, Integer orgId) throws GlobalException;

    /**
     * 后台修改用户密码
     *
     * @param coreUser 用户DTO
     * @param isReset  是否管理员重置密码，重置密码会记录最后重置密码时间，用于token验证主动过期，踢出用户
     * @return ResponseInfo 响应
     * @throws GlobalException 异常
     * @Title: psw
     */
    ResponseInfo psw(CoreUserDto coreUser, boolean isReset) throws GlobalException;

    /**
     * 分页查询管理员列表(微信、微博)
     *
     * @param orgId    组织id
     * @param roleid   角色id
     * @param username 用户名、真实姓名
     * @param pageable 分页信息
     * @param notIds   用户集合(不包含的用户)
     * @Title: pageWechat
     * @return: Page
     */
    Page<CoreUser> pageThirdManager(Integer orgId, Integer roleid, String username, Pageable pageable,
                                    List<Integer> notIds);

    /**
     * 查询管理员列表(微信、微博)
     *
     * @param ids      用户集合(包含的用户)
     * @param orgId    组织id
     * @param roleid   角色id
     * @param username 用户名、真实姓名
     * @Title: listThirdManager
     * @return: List
     */
    List<CoreUser> listThirdManager(List<Integer> ids, Integer orgId, Integer roleid, String username);

    /**
     * 新增会员数
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param siteId    站点id
     * @return 新增会员数
     */
    long getNewUserSum(Date beginTime, Date endTime, Integer siteId);

    /**
     * 获取会员数
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param siteId    站点id
     * @return 会员数
     */
    long getUserSum(Date beginTime, Date endTime, Integer siteId);

    /**
     * 获取后台会员详情
     *
     * @param memberId 会员ID
     * @return MemberInfoVo
     * @throws GlobalException 异常
     * @Title: getMemberInfo
     */
    MemberInfoVo getMemberInfo(Integer memberId) throws GlobalException;

    /**
     * 会员模型字段赋值
     *
     * @param member 会员对象
     * @param type   true 用于待审核，false用于详情
     * @return JSONObject
     * @Title: initDefaultModelItems
     */
    JSONObject initDefaultModelItems(CoreUser member, Boolean type);

    /**
     * 根据电话号码查找未删除会员
     *
     * @param phone 电话号码
     * @return CoreUser 用户
     * @throws GlobalException 异常
     * @Title: findByPhone
     */
    CoreUser findByPhone(String phone) throws GlobalException;

    /**
     * 根据邮箱/用户名查找未删除会员
     *
     * @param key 邮箱或用户名
     * @return CoreUser 用户
     * @Title: findByEmailOrUsername
     */
    CoreUser findByEmailOrUsername(String key);

    /**
     * 找回密码
     *
     * @param dto 传输
     * @return ResponseInfo 响应
     * @throws GlobalException 异常
     * @Title: rectrieve
     */
    ResponseInfo rectrieve(PcMemberDto dto) throws GlobalException;

    /**
     * PC注册会员
     *
     * @param bean 用户
     * @return 用户信息
     * @throws GlobalException 全局异常
     * @return: ResponseInfo
     */
    ResponseInfo savePCMember(MemberRegisterDto bean) throws GlobalException;


    /**
     * 获取PC会员详情
     *
     * @param memberId 会员ID
     * @return MemberInfoVo
     * @throws GlobalException 异常
     * @Title: getMemberInfo
     */
    MemberInfoVo getPCMemberInfo(Integer memberId) throws GlobalException;

    /**
     * 清空所有管理员用户权限
     *
     * @Title: clearAllUserCache
     * @return: void
     */
    void clearAllUserCache();

    /**
     * 手机端会员中心修改会员系统信息
     *
     * @param dto 会员基本信息Dto
     * @return ResponseInfo 响应
     * @throws Exception 全局异常
     */
    ResponseInfo updateMobileSysMember(MobileMemberDto dto) throws Exception;

    /**
     * 手机端会员中心修改会员自定义信息
     *
     * @param dto 会员基本信息Dto
     * @return ResponseInfo 响应
     * @throws Exception 全局异常
     */
    ResponseInfo updateMobileCustomMember(MobileMemberDto dto) throws Exception;

    /**
     * 查询安全管理员或审计管理员数量
     *
     * @param userIds 用户id集合
     * @param status  true->用户开启，false->用户关闭
     * @return Integer
     */
    Integer getSafeOrAuditUser(List<Integer> userIds, Integer status);

    /**
     * 查询安全管理员或审计管理员
     *
     * @param status  1-安全管理员、2-审计管理员
     * @param enabled true->用户开启，false->用户关闭
     * @return
     */
    List<CoreUser> getSafeOrAuditUser(Integer status, Boolean enabled);

    /**
     * 通过id集合查询用户对象集合
     *
     * @param userIds 用户id集合
     * @return List<CoreUser>
     */
    List<CoreUser> findByIds(List<Integer> userIds);

    /**
     * 通过id集合查询开启的未删除管理员对象集合
     *
     * @param userIds 用户id集合
     * @return List<CoreUser>
     */
    List<CoreUser> findAdminByIds(List<Integer> userIds);

    /**
     * 三元管理员列表分页
     *
     * @param orgids   组织ID集合
     * @param roleids  角色ID集合
     * @param key      用户名/邮箱/电话/真名/座机
     * @param pageable 分页对象
     * @return Page<CoreUser>
     */
    Page<CoreUser> pageSafeManageUser(List<Integer> orgids,
                                      List<Integer> roleids, String key, List<Integer> notIds, Pageable pageable);

    /**
     * 修改密码
     *
     * @param passwd  加密后密码
     * @param saltStr 混淆字符串
     */
    void updatePasswdBatch(String passwd, String saltStr);

    /**
     * 通过状态查询安全管理员或审计管理员id集合
     *
     * @param status 1-安全管理员，2-审计管理员
     * @return List<Integer>
     */
    List<Integer> getSafeOrAuditUserIds(Integer status);

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
     * @param enabled true->用户开启，false->用户关闭
     * @return
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

    /**
     * 用户账户统计列表（按余额）
     *
     * @param userName
     * @param sortType
     * @return
     */
    List<CoreUser> getUserStatisticList(String userName, Integer sortType, Boolean deleteFlag);

    /**
     * 付费统计-用户排名Top10
     * @param sortType
     * @return
     */
    List<CoreUser> getUserTopTen(int sortType);

}
