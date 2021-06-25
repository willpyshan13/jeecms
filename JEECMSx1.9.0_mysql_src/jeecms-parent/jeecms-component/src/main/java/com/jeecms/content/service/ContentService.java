/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentExt;
import com.jeecms.content.domain.dto.*;
import com.jeecms.content.domain.vo.ContentButtonVo;
import com.jeecms.content.domain.vo.ContentFindVo;
import com.jeecms.content.domain.vo.ResetSecretVo;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.CmsSiteConfig;
import com.jeecms.system.domain.GlobalConfig;
import com.jeecms.system.domain.dto.BeatchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 内容主体service接口
 *
 * @author: chenming
 * @date: 2019年5月6日 下午2:35:08
 */
public interface ContentService extends IBaseService<Content, Integer> {

    /**
     * @param dto  批量操作Dto
     * @param user 请求用户user
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    ResponseInfo changeStatus(BeatchDto dto, CoreUser user, Boolean checkComplete) throws GlobalException;

    /**
     * 发布内容（无权限限制）
     *
     * @param ids 内容ID集合
     * @throws GlobalException GlobalException
     * @return: void
     */
    void publish(List<Integer> ids) throws GlobalException;

    /**
     * 内容类型操作
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo 返回对象
     * @throws GlobalException 异常
     */
    ResponseInfo operation(OperationDto dto) throws GlobalException;

    /**
     * 置顶操作
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo 返回对象
     * @throws Exception 异常
     */
    ResponseInfo top(OperationDto dto) throws Exception;

    /**
     * 取消置顶操作
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo 返回对象
     * @throws GlobalException 异常
     */
    ResponseInfo notop(OperationDto dto) throws GlobalException;

    /**
     * 移动操作
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo 返回对象
     * @throws GlobalException 异常
     */
    ResponseInfo move(OperationDto dto) throws GlobalException;

    /**
     * 排序操作
     *
     * @param dto 批量操作Dto
     * @return ResponseInfo 返回对象
     * @throws GlobalException 异常
     */
    ResponseInfo sort(OperationDto dto) throws GlobalException;

    /**
     * 删除操作(加入回收站)
     *
     * @param dto 批量操作Dto
     * @throws GlobalException 异常
     */
    void rubbish(OperationDto dto) throws GlobalException;

    /**
     * 引用操作
     *
     * @param dto 批量操作Dto
     * @throws GlobalException 异常
     */
    void quote(OperationDto dto) throws GlobalException;

    /**
     * 取消引用操作
     *
     * @param dto 批量操作Dto
     * @throws GlobalException 异常
     */
    void noquote(OperationDto dto) throws GlobalException;

    /**
     * 新增内容
     *
     * @param dto 新增内容扩展dto
     * @return Content
     * @throws GlobalException 全局异常
     */
    Content save(ContentSaveDto dto, CmsSite site) throws GlobalException;

    /**
     * 提交内容
     *
     * @param dto        ContentUpdateDto
     * @param request    HttpServletRequest
     * @param contribute 是否是一个新增操作
     * @param content    内容对象
     * @throws GlobalException GlobalException
     */
    void submit(ContentUpdateDto dto, HttpServletRequest request, boolean contribute, Content content) throws GlobalException;

    /**
     * 查询单个内容
     *
     * @param id           内容id
     * @param globalConfig 全局配置
     * @throws GlobalException 全局异常
     * @return: ContentFindVo
     */
    ContentFindVo findContent(Integer id, GlobalConfig globalConfig) throws GlobalException;

    /**
     * 修改单个内容
     *
     * @param dto     接受修改dto
     * @param request 前台传入request请求
     * @return Content
     * @throws GlobalException 异常
     */
    Content update(ContentUpdateDto dto, HttpServletRequest request) throws GlobalException;

    /**
     * 初始化组装update的dto
     *
     * @param content 内容对象
     * @return: SpliceCheckUpdateDto
     */
    SpliceCheckUpdateDto initSpliceCheckUpdateDto(Content content);

    /**
     * 校验修改的内容，并返回数据
     *
     * @param oldUpdateDto 老的校验dto
     * @param newUpdateDto 新的校验dto
     * @param globalConfig 全局配置
     * @param bean         内容对象
     * @param userId       用户id
     * @throws GlobalException 全局异常
     */
    void checkUpdate(SpliceCheckUpdateDto oldUpdateDto, SpliceCheckUpdateDto newUpdateDto,
                     GlobalConfig globalConfig, Content bean, Integer userId) throws GlobalException;

    /**
     * copy内容
     *
     * @param dto        copy接收dto
     * @param rquest     前台出传入request请求
     * @param siteConfig 站点配置config
     * @throws GlobalException 异常
     */
    void copy(ContentCopyDto dto, HttpServletRequest rquest, CmsSiteConfig siteConfig) throws GlobalException;

    /**
     * 校验内容标题，true->该内容存在，false->内容不存在
     *  @param title     内容标题
     * @param channelId 栏目id
     * @param siteId
     */
    boolean checkTitle(String title, Integer channelId, Integer siteId);

    /**
     * 回复版本
     *
     * @param versionId 版本id
     * @param contentId 内容id
     * @throws GlobalException 全局异常
     */
    void recoveryVersion(Integer versionId, Integer contentId) throws GlobalException;

    /**
     * 校验权限
     *
     * @param opration 权限标识符
     * @param contents 内容集合
     * @param content  内容对象
     * @return: Boolean
     */
    Boolean validType(Short opration, List<Content> contents, Content content);

    /**
     * 通过栏目id校验权限
     *
     * @param opration 权限标识
     * @param channId  栏目id
     * @return: Boolean
     */
    Boolean validType(Short opration, Integer channId);

    /**
     * 通过栏目id数组查询该栏目id数组下的内容集合
     *
     * @param channelIds 栏目数组
     * @return: List
     */
    List<Content> findByChannels(Integer[] channelIds);

    /**
     * 通过栏目id数组查询该栏目id数组下的内容集合
     *
     * @param channelIds 栏目id集合
     * @param recycle    回收站状态
     * @return long
     */
    long countByChannelIdInAndRecycle(Integer[] channelIds, boolean recycle);

    /**
     * 推送到站群
     *
     * @param dto          接受dto
     * @param request      request请求
     * @param globalConfig 全局配置
     * @param site         站点id
     * @throws GlobalException 全局异常
     */
    void pushSites(ContentPushSitesDto dto, HttpServletRequest request, GlobalConfig globalConfig, CmsSite site) throws GlobalException;


    /**
     * 微信预览
     *
     * @param dto 传输DTO
     * @throws GlobalException 异常
     */
    ResponseInfo preview(WechatViewDto dto) throws GlobalException;

    /**
     * 推送到微信
     *
     * @param dto 传输DTO
     * @throws Exception 异常
     */
    ResponseInfo push(WechatPushDto dto) throws Exception;

    /**
     * 内容导入
     *
     * @param file 文件
     * @param type 类型
     * @throws Exception 异常
     */
    String docImport(MultipartFile file, Integer type) throws Exception;

    /**
     * 还原内容
     *
     * @param contentIds 内容ID数组
     * @param siteId     站点ID
     * @param channelIds 栏目id集合
     * @throws GlobalException 异常
     */
    void restore(List<Integer> contentIds, Integer siteId, List<Integer> channelIds) throws GlobalException;

    /**
     * 删除内容
     *
     * @param contentIds 内容ID数组
     * @throws GlobalException 异常
     */
    void deleteContent(List<Integer> contentIds) throws GlobalException;

    /**
     * 新增修改之后初始化内容content关联的对象
     *
     * @param content 需要初始化的内容
     * @throws GlobalException 全局异常
     */
    void initContentObject(Content content) throws GlobalException;

    /**
     * 新增修改之后初始化内容扩展contentExt关联的对象
     *
     * @param contentExt 内容扩展对象
     * @throws GlobalException 全局异常
     */
    void initContentExtObject(ContentExt contentExt) throws GlobalException;

    /**
     * 查询强制通过按钮
     *
     * @param channelId 栏目ID
     * @return Boolean
     */
    Boolean getForceReleaseButton(Integer channelId);

    /**
     * 新增、修改查询按钮
     *
     * @param status    状态
     * @param id        内容ID
     * @param channelId 栏目
     * @return ContentButtonVo
     */
    ContentButtonVo findByContentButton(Integer status, Integer id, Integer channelId, Boolean quote);


    /**
     * 查询操最大的排序值对象
     *
     * @return Content
     */
    Content findFirstByOrderByIdDesc();

    /**
     * 得到最大排序值
     *
     * @return Integer
     */
    Integer findMaxSortNum();

    /**
     * 查找内容
     *
     * @param siteId 站点id
     * @return List
     */
    long countByTpl(Integer siteId, String pcTpl, String mobileTpl);

    /**
     * 内容重置密级
     *
     * @param dto    重置密级dto
     * @param user   当前操作用户user
     * @param siteId 当前站点id
     * @return ResetSecretVo
     */
    ResetSecretVo resetSecret(ResetSecretDto dto, CoreUser user, Integer siteId);

    /**
     * 带缓存的查询
     *
     * @param ids 内容IDS
     * @return List
     */
    List<Content> findAllByIdForCache(List<Integer> ids);

    /**
     * 收益统计
     * @param sortType
     * @return
     */
    Page<Content> getProfitCount(int sortType, Pageable pageable);

    /**
     * 付费统计 内容top10
     * @param sortType
     * @return
     */
    List<Content> getContentTopTen(int sortType);
}
