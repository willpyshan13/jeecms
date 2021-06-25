/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import com.jeecms.common.base.domain.DragSortDto;
import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.domain.CmsModel;
import com.jeecms.content.domain.dto.CmsModelDto;

/**
 * 模型service层
 *
 * @version 1.0
 * @author: wulongwei
 * @date: 2019年4月17日 下午3:15:33
 */
public interface CmsModelService extends IBaseService<CmsModel, Integer> {
    /**
     * 获得模型列表
     *
     * @param containDisabled 是否禁用
     * @param siteId          站点ID
     * @return 模型列表
     * @Title: getList 获得模型列表
     */
    List<CmsModel> getList(boolean containDisabled, Integer siteId);

    /**
     * 更新列表拖动排序
     *
     * @param sortDto 请求dto
     * @throws GlobalException 全局异常
     */
    void updatePriority(DragSortDto sortDto) throws GlobalException;

    /**
     * 分页查询模型集合
     *
     * @param tplType   模型类型（1-栏目模型 2-内容模型 3会员模型）
     * @param isGlobal  是否全局(1全局 0站点模型)
     * @param isDisable 是否开启
     * @param modelName 模型名称
     * @param siteId    站点id
     * @param pageable  分页对象
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    ResponseInfo getModelPage(Short tplType, Short isGlobal, Boolean isDisable, String modelName, Integer siteId,
                              Pageable pageable) throws GlobalException;

    /**
     * 保存本站模型
     *
     * @param model 模型对象
     * @throws GlobalException
     * @Title: saveThisSiteModel
     * @return: ResponseInfo
     */
    ResponseInfo saveThisSiteModel(CmsModel model) throws GlobalException;

    /**
     * 保存全站模型
     *
     * @param model 模型对象
     * @throws GlobalException 全局异常
     * @Title: saveWholeSiteModel
     * @return: ResponseInfo
     */
    ResponseInfo saveWholeSiteModel(CmsModel model) throws GlobalException;

    /**
     * 修改模型
     *
     * @param model 模型对象
     * @throws GlobalException 全局异常
     * @return: ResponseInfo
     */
    ResponseInfo updateModel(CmsModel model) throws GlobalException;

    /**
     * 是否启用
     *
     * @param model 请求dto
     * @throws GlobalException 全局异常
     * @Title: isEnable
     * @return: ResponseInfo
     */
    ResponseInfo isEnable(CmsModelDto model) throws GlobalException;

    /**
     * 校验modelName是否存在
     *
     * @param id        模型id
     * @param tplType   模型类型（1-栏目模型 2-内容模型 3会员模型）
     * @param modelName 模型名称
     * @param siteId    站点id
     * @param isGlobal  是否全局(1全局 0站点模型)
     * @return ResponseInfo
     * @throws GlobalException 全局异常
     */
    ResponseInfo checkModelName(Integer id, Short tplType, String modelName, Integer siteId, Short isGlobal) throws GlobalException;

    /**
     * 根据条件查询出未禁用的模型列表
     *
     * @param tplType 模型类型（1-栏目模型 2-内容模型  3-会员模型）
     * @param siteId  站点Id
     * @throws GlobalException 全局异常
     * @Title: findList
     * @return: List
     */
    List<CmsModel> findList(Short tplType, Integer siteId) throws GlobalException;

    /**
     * 获取模型详情，组合前端数据
     *
     * @param id 为null时，默认获取会员模型
     * @return CmsModel
     */
    CmsModel getInfo(Integer id);

    /**
     * 根据条件查询模型集合
     *
     * @param tplType  模型类型（1-栏目模型 2-内容模型 3会员模型）
     * @param isEnable 是否启动
     * @param siteId   站点id
     * @return List<CmsModel>
     * @throws GlobalException 全局异常
     */
    List<CmsModel> getModelList(Short tplType, Boolean isEnable, Integer siteId) throws GlobalException;

    /**
     * 获取栏目及内容模型字段，默认过滤模式为 SHOW_CHANNEL_AND_CONTENT
     *
     * @param id 模型id
     * @return CmsModel
     * @throws GlobalException 全局异常
     */
    CmsModel getChannelOrContentModel(Integer id) throws GlobalException;

    /**
     * 获取会员模型字段，主要应用至前端注册页面，默认过滤模式为FilterModel.SHOW_MEMBER_REGISTOR( “应用到会员注册” 字段)
     *
     * @throws GlobalException 全局异常
     */
    CmsModel getFrontMemberModel() throws GlobalException;

    /**
     * 查询未删除的内容模型
     *
     * @return List<CmsModel>
     */
    List<CmsModel> findModel(Short type);
}
