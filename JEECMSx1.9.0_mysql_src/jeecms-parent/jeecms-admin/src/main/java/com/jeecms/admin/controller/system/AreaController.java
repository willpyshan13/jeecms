package com.jeecms.admin.controller.system;

import com.alibaba.fastjson.JSONArray;
import com.jeecms.common.base.controller.BaseTreeController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.base.domain.SortDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.local.ThreadPoolService;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.web.cache.CacheProvider;
import com.jeecms.system.domain.Area;
import com.jeecms.system.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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


    @Autowired
    private CacheProvider cacheProvider;
    /**
     * 保存排序
     */
    @RequestMapping(value = "/sort", method = RequestMethod.PUT)
    @Override
    public ResponseInfo sort(@RequestBody(required = false) @Valid SortDto sort, BindingResult result)
            throws GlobalException {
        super.sort(sort, result);
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshAreaTree();
                } catch (GlobalException e) {
                    e.printStackTrace();
                }
            }
        });
        return new ResponseInfo();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @Override
    public ResponseInfo delete(@RequestBody @Valid DeleteDto ids, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        areaService.remove(ids.getIds()[0]);
        ThreadPoolService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshAreaTree();
                } catch (GlobalException e) {
                    e.printStackTrace();
                }
            }
        });
        return new ResponseInfo(true);
    }

    /**
     * 获取详细信息
     */
    @RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
    @MoreSerializeField({ @SerializeField(clazz = Area.class, includes = { "id", "areaCode", "areaName", "remark",
            "sortNum", "areaDictCode", "nodeIds" })})
    public ResponseInfo getArea(@PathVariable(name = "id") Integer id) throws Exception {
        return super.get(id);
    }

    final transient ReentrantLock lock = new ReentrantLock();

    /**
     * 保存区域信息 检查当前信息编号是否存在
     */
    @RequestMapping(method = RequestMethod.POST)
    @Override
    public ResponseInfo save(@RequestBody @Valid Area area, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        if (area.getParentId() != null) {
            area.setParent(areaService.findById(area.getParentId()));
        }
        lock.lock();
        try {
            if (areaService.isExist(area.getAreaCode())) {
                return new ResponseInfo(SettingErrorCodeEnum.AREACODE_EXIST.getCode(),
                        SettingErrorCodeEnum.AREACODE_EXIST.getDefaultMessage());
            } else {
                areaService.save(area);
            }
        } finally {
            lock.unlock();
        }
        ThreadPoolService.getInstance().execute(() -> {
            try {
                refreshAreaTree();
            } catch (GlobalException e) {
                e.printStackTrace();
            }
        });
        return new ResponseInfo(true);
    }

    /**
     * 更新
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseInfo update(HttpServletRequest request, @RequestBody @Valid Area area, BindingResult result)
            throws GlobalException {
        super.validateBindingResult(result);
        String code = service.get(area.getId()).getAreaCode();
        if (area.getAreaCode().equals(code) || areaService.findByAreaCode(area.getAreaCode()).size() == 0) {
            Area newArea = areaService.updateAll(area);
            Integer parentId = newArea.getParentId();
            if (parentId != null && parentId != 0) {
                newArea.setParent(areaService.findById(parentId));
            }
            ThreadPoolService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        refreshAreaTree();
                    } catch (GlobalException e) {
                        e.printStackTrace();
                    }
                }
            });
            return new ResponseInfo(true);
        }
        return new ResponseInfo(SettingErrorCodeEnum.AREACODE_EXIST.getCode(),
                SettingErrorCodeEnum.AREACODE_EXIST.getDefaultMessage());
    }

    /**
     * 验证AreaCode是否唯一
     */
    @RequestMapping(value = "/code/unique", method = RequestMethod.GET)
    public ResponseInfo getAreaCode(@RequestParam String areaCode,
                                    @RequestParam(name = "id", required = false) Integer id) throws GlobalException {
        if (id != null) {
            Area area = service.get(id);
            if (!area.getAreaCode().equals(areaCode) && areaService.findByAreaCode(areaCode).size() > 0) {
                return new ResponseInfo(false);
            }
        } else {
            if (areaService.findByAreaCode(areaCode).size() > 0) {
                return new ResponseInfo(false);
            }
        }
        return new ResponseInfo(true);
    }

    /**
     * 刷新缓存
     */
    @RequestMapping(value = "/cache", method = RequestMethod.PUT)
    public ResponseInfo updateCache() throws GlobalException {
        areaService.findAllList();
        return new ResponseInfo(true);
    }

    private JSONArray refreshAreaTree() throws GlobalException {
        List<Area> areaList = areaService.findAllOrdeSortNum();
        JSONArray array = super.getChildTree(areaList, false, "areaName", "areaCode", "areaDictCode");
        cacheProvider.setCache(Area.AREA_CACHE_KEY, Area.AREA_TREE_LIST, array);
        return array;
    }

    @Autowired
    private AreaService areaService;
}
