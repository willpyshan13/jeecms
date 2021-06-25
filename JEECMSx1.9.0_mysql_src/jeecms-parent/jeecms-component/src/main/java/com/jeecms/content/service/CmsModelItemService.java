/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.service;

import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.domain.CmsModelItem;
import com.jeecms.content.domain.vo.ModelItemDto;

import java.util.List;
import java.util.Map;

/**
 * 模型字段service层
 * 
 * @author: wulongwei
 * @version 1.0
 * @date: 2019年4月17日 下午3:15:13
 */
public interface CmsModelItemService extends IBaseService<CmsModelItem, Integer> {

	/**
	 * 保存模型字段
	 * @Title: saveCmsModelItem
	 * @param  modelItemDto 前台传入dto
	 * @throws GlobalException	全局异常
	 * @return: ResponseInfo
	 */
	ResponseInfo saveCmsModelItem(ModelItemDto modelItemDto) throws GlobalException;

	/**
	 * 根据模型Id查询模型字段List集合
	 * 
	 * @Title: findList
	 * @param modelId 模型Id
	 * @throws GlobalException 全局异常
	 * @return: List
	 */
	List<CmsModelItem> findByModelId(Integer modelId) throws GlobalException;

	/**
	 * 根据模型Id，数据类型查询模型字段List集合
	 * @Title: findByModelIdAndDataType  
	 * @param modelId	模型id
	 * @param type	数据类型
	 * @throws GlobalException    全局异常  
	 * @return: List
	 */
	List<CmsModelItem> findByModelIdAndDataType(Integer modelId, String type) throws GlobalException;

	/**
	 * 根据模型id和字段名称查询数据类型
	 * @Title: getDataType  
	 * @param modelId		模型id
	 * @param fields		字段名称集合
	 * @throws GlobalException  全局异常    
	 * @return: String
	 */
	Map<String,String> getDataType(Integer modelId,List<String> fields) throws GlobalException;

    /**
     * 通过模型和字段类型查询出排序值最高的字段
     * @param modelId   模型id
     * @return  模型字段对象
     */
	String getModelItemByDataType(Integer modelId,String dataType);

}
