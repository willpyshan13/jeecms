package com.jeecms.front.controller;

import com.alibaba.fastjson.JSONArray;
import com.jeecms.common.base.controller.BaseTreeController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.system.domain.Area;
import com.jeecms.system.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 区域设置
 *
 * @author: chenming
 * @version: 1.0
 * @date 2018-06-19
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@RequestMapping(value = "/area")
@RestController
public class AreaController extends BaseTreeController<Area, Integer> {

    @PostConstruct
    public void init() {
        String[] queryParams = { "[name,areaName]_LIKE_String", "[code,areaCode]_EQ_String" };
        super.setQueryParams(queryParams);
    }

    /**
     * 获得某节点后的子集集合
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @MoreSerializeField({
            @SerializeField(clazz = Area.class, includes = { "id", "areaCode", "areaName", "parentId",
                    "remark", "sortNum", "isChild", "areaDictCode", "nodeIds", "createTime" })})
    public ResponseInfo list(@RequestParam(value = "parentId", required = false) Integer parentId)
            throws GlobalException {
        List<Area> areaList = areaService.findByParentId(parentId);
        return new ResponseInfo(areaList);
    }

    /**
     * 树形查询
     */
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public ResponseInfo findAll(HttpServletRequest request, Paginable paginable) throws GlobalException {
        JSONArray array = areaService.getAreaTree();
        if (array == null) {
            array = this.refreshAreaTree();
        }
        return new ResponseInfo(array);
    }

    /**
     * 获取详细信息
     */
    @RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
    @MoreSerializeField({ @SerializeField(clazz = Area.class, includes = { "id", "areaCode", "areaName", "remark",
            "sortNum", "areaDictCode", "nodeIds" }),})
    public ResponseInfo getArea(@PathVariable(name = "id") Integer id) throws Exception {
        return super.get(id);
    }

    final transient ReentrantLock lock = new ReentrantLock();


    private JSONArray refreshAreaTree() throws GlobalException {
        List<Area> areaList = areaService.findAllOrdeSortNum();
        JSONArray array = super.getChildTree(areaList, false, "areaName", "areaCode", "areaDictCode");
        cacheProvider.setCache(Area.AREA_CACHE_KEY, Area.AREA_TREE_LIST, array);
        return array;
    }

    @Autowired
    private AreaService areaService;

    @Autowired
    private CacheProvider cacheProvider;
}
