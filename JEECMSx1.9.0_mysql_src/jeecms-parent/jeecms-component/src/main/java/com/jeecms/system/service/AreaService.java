package com.jeecms.system.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.base.service.IBaseService;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.system.domain.Area;

import java.util.List;
import java.util.Map;

/**
 * Area设置service接口
 * @author: chenming
 * @date:   2019年4月13日 下午2:44:44
 */
public interface AreaService extends IBaseService<Area, Integer> {

	/**
	 * 根据区域编号查询区域信息
	 * @Title: findByAreaCode  
	 * @param areaCode		区域编号
	 * @throws GlobalException   异常   
	 * @return: List<Area>
	 */
	List<Area> findByAreaCode(String areaCode) throws GlobalException;

    /**
     * 判断区域code是否可用
     * @param areaCode  区域code
     * @return  true->不可用，false->可用
     */
	boolean isExist(String areaCode);

	/**
	 * 根据父级ID查询区域信息
	 * @Title: findByParentId  
	 * @param parentId	父级ID
	 * @throws GlobalException	异常      
	 * @return: List<Area>
	 */
	List<Area> findByParentId(Integer parentId) throws GlobalException;

	/**
	 * 删除节点及下所有子节点信息（物理删除）
	 * @Title: remove  
	 * @param id	标识
	 * @throws GlobalException  异常    
	 * @return: List<Area>
	 */
	List<Area> remove(Integer id) throws GlobalException;

	/**
	 * 查询所有省市区列表
	 * @Title: findAllList  
	 * @throws GlobalException   异常   
	 * @return: Map<String,Object>
	 */
	Map<String,Object> findAllList() throws GlobalException;

	/**
	 * 获取所有省列表
	 * @Title: findByDictCode  
	 * @throws GlobalException  异常    
	 * @return: List<Area>
	 */
	List<Area> findByDictCode() throws GlobalException;
	
	/**
	 * 查询所有的省市区列表转换成String
	 * @Title: findAllToString  
	 * @throws GlobalException  异常    
	 * @return: String
	 */
	JSONObject findAllToString() throws GlobalException;

    /**
     * 查询区域集合(通过排序值排序)
     * @return  List<Area>
     */
	List<Area> findAllOrdeSortNum();

    /**
     * 获取区域层次结构(从缓存中)
     * @return  JSONArray
     */
	JSONArray getAreaTree();

}
