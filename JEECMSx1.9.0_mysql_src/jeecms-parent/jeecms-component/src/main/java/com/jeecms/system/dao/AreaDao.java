package com.jeecms.system.dao;

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.system.dao.ext.AreaDaoExt;
import com.jeecms.system.domain.Area;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * 区域设置dao接口
 * @author: chenming
 * @date:   2019年4月13日 下午2:47:37
 */
public interface AreaDao extends IBaseDao<Area, Integer>, AreaDaoExt {

	/**
	 * 通过areaCode作为查询条件(用于验证areaCode是否唯一)
	 * @Title: findByAreaCode
	 * @param areaCode		区域唯一code标识
	 * @return: List<Area>
	 */
	@Query("select bean from Area bean where 1 = 1 and bean.areaCode=?1 and bean.hasDeleted = false")
	@QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
	List<Area> findByAreaCode(String areaCode);

	int countByAreaCodeAndHasDeleted(String areaCode, boolean hasDeled);

	/**
	 * 通过区域字典值(省、市、区)查询列表
	 * @Title: findList  
	 * @param areaDictCode	区域唯一code标识
	 * @return: List<Area>
	 */
	@Query("select bean from Area bean where 1 = 1 and bean.areaDictCode=?1")
	@QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
	List<Area> findList(String areaDictCode);

    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
	List<Area> findByHasDeletedOrderBySortNumAsc(Boolean hasDeleted);
}
