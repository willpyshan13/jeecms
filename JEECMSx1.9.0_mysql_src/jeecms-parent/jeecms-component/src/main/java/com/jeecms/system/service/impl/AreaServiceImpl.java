package com.jeecms.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.system.dao.AreaDao;
import com.jeecms.system.domain.Area;
import com.jeecms.system.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Area设置service实现类
 * 
 * @author: chenming
 * @date: 2019年4月13日 下午2:46:25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AreaServiceImpl extends BaseServiceImpl<Area, AreaDao, Integer> implements AreaService {

	@Autowired
	private CacheProvider cacheProvider;

	/**
	 * 根据父id找到子节点
	 */
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	@Override
	public List<Area> findByParentId(Integer parentId) throws GlobalException {
		if (parentId != null && parentId == 0) {
			parentId = null;
		}
		List<Area> areaList = dao.findByParams(parentId, false);
		return areaList;
	}

	/**
	 * 以areaCode为条件进行查询
	 */
	@Override
	public List<Area> findByAreaCode(String areaCode) throws GlobalException {
		return dao.findByAreaCode(areaCode);
	}

	@Override
	public boolean isExist(String areaCode) {
		return dao.countByAreaCodeAndHasDeleted(areaCode, false) > 0;
	}

	/**
	 * 删除
	 */
	@Override
	public List<Area> remove(Integer id) throws GlobalException {
		Area area = super.findById(id);
		List<Integer> removeIdList = new ArrayList<>();
		if (!area.getIsChild()) {
			Map<String, String[]> params = new HashMap<>(16);
			params.put("GTE_lft_Integer", new String[] { String.valueOf(area.getLft()) });
			params.put("LTE_rgt_Integer", new String[] { String.valueOf(area.getRgt()) });
			List<Area> sonList = super.getList(params, null, false);
			for (Area area2 : sonList) {
				removeIdList.add(area2.getId());
			}
		} else {
			removeIdList.add(area.getId());
		}
		Integer[] removeIdsArray = removeIdList.toArray(new Integer[0]);
		return super.physicalDelete(removeIdsArray);
	}

	/**
	 * 查询所有省市区列表
	 */
	@Override
	public Map<String, Object> findAllList() throws GlobalException {
		Map<String, Object> returnData = new HashMap<>(16);
		Map<String, String> provinceMap = new HashMap<>(16);
		Map<String, String> cityMap = new HashMap<>(16);
		Map<String, String> countyMap = new HashMap<>(16);
		List<Area> provinceList = dao.findList(String.valueOf(1));
		for (Area area : provinceList) {
			provinceMap.put(area.getAreaCode(), area.getAreaName());
		}
		returnData.put("province_list", provinceMap);
		List<Area> cityList = dao.findList(String.valueOf(2));
		for (Area area : cityList) {
			cityMap.put(area.getAreaCode(), area.getAreaName());
		}
		returnData.put("city_list", cityMap);
		List<Area> countyList = dao.findList(String.valueOf(3));
		for (Area area : countyList) {
			countyMap.put(area.getAreaCode(), area.getAreaName());
		}
		returnData.put("county_list", countyMap);

		JSONObject json = new JSONObject(returnData);
		cacheProvider.setCache(Area.AREA_CACHE_KEY, Area.AREA_LIST_TOSTRING_NAME, json);

		return returnData;
	}

	@Override
	public List<Area> findByDictCode() throws GlobalException {
		return dao.findList(String.valueOf(1));
	}

	@Override
	public JSONObject findAllToString() throws GlobalException {
		if (cacheProvider.exist(Area.AREA_CACHE_KEY, Area.AREA_LIST_TOSTRING_NAME)) {
			String str = cacheProvider.getCache(Area.AREA_CACHE_KEY, Area.AREA_LIST_TOSTRING_NAME)
						.toString();
			return JSONObject.parseObject(str);
		} else {
			Map<String, Object> returnData = this.findAllList();
			JSONObject json = new JSONObject(returnData);
			return json;
		}
	}

    @Override
    public List<Area> findAllOrdeSortNum() {
        return dao.findByHasDeletedOrderBySortNumAsc(false);
    }

    @Override
    public JSONArray getAreaTree() {
        if (cacheProvider.exist(Area.AREA_CACHE_KEY, Area.AREA_TREE_LIST)) {
            String str = cacheProvider.getCache(Area.AREA_CACHE_KEY, Area.AREA_TREE_LIST)
                    .toString();
            return JSONArray.parseArray(str);
        }
        return null;
    }

}
