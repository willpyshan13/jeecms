package com.jeecms.admin.controller.auth;

import com.jeecms.auth.domain.CoreApi;
import com.jeecms.auth.domain.CoreMenu;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.dto.CoreMenuDto;
import com.jeecms.auth.domain.vo.MenuVO;
import com.jeecms.auth.domain.vo.SortMenuVO;
import com.jeecms.auth.service.CoreApiService;
import com.jeecms.auth.service.CoreMenuService;
import com.jeecms.common.annotation.EncryptMethod;
import com.jeecms.common.base.controller.BaseTreeController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.base.domain.SortDto;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 菜单管理
 *
 * @author chenming
 * @date 2019年4月9日 下午3:03:43
 */
@RequestMapping(value = "/menus")
@RestController
public class CoreMenuController extends BaseTreeController<CoreMenu, Integer> {

    @PostConstruct
    public void init() {
        String[] queryParams = {};
        super.setQueryParams(queryParams);
    }

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 查询所有(树形结构)
     *
     * @param request   HttpServletRequest
     * @param paginable Paginable 分页组件
     * @return ResponseInfo
     */
    @GetMapping(value = "/tree")
    @SerializeField(clazz = CoreMenu.class, includes = {"id", "path", "name", "menuName", "hidden", "menuType",
            "children", "sortNum", "isAuth"})
    public ResponseInfo tree(HttpServletRequest request, Paginable paginable) {
        paginable.setSort(Sort.by(Direction.ASC, "sortNum"));
        List<CoreMenu> menuList = menuService.getList(super.getCommonParams(request), paginable, true);
        return new ResponseInfo(super.getTree(menuList, null));
    }

    /**
     * 查询所有（菜单和权限分开存放）
     *
     * @return ResponseInfo
     */
    @GetMapping(value = "/getMenus")
    @MoreSerializeField({
            @SerializeField(clazz = MenuVO.class, includes = {"needChangePassword", "nextNeedCaptcha", "lastLoginTime",
                    "userName", "lastLoginIP", "menus", "perms"}),
            @SerializeField(clazz = SortMenuVO.class, includes = {"id", "path", "name", "menuName", "hidden", "icon",
                    "children", "component", "sortNum", "redirect"})})
    public ResponseInfo getMenus(HttpServletRequest request) throws GlobalException {
        CoreUser user = SystemContextUtils.getUser(request);
        return new ResponseInfo(menuService.getMenu(user));
    }

    /**
     * 保存排序
     */
    @PutMapping(value = "/sort")
    @Override
    public ResponseInfo sort(@RequestBody(required = false) @Valid SortDto sort, BindingResult result)
            throws GlobalException {
        super.checkServerMode();
        return super.sort(sort, result);
    }

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	@Override
	public ResponseInfo delete(@RequestBody @Valid DeleteDto ids, BindingResult result) throws GlobalException {
		super.checkServerMode();
		super.validateBindingResult(result);
        menuService.remove(ids.getIds()[0]);
		return new ResponseInfo();
	}

    /**
     * 详细信息
     */
    @Override
    @GetMapping(value = "/{id:[0-9]+}")
    @MoreSerializeField({
            @SerializeField(clazz = CoreMenu.class, includes = {"id", "icon", "menuName", "name", "menuType", "path",
                    "redirect", "sortNum", "component", "apis", "parentIds", "hidden", "isAuth"}),
            @SerializeField(clazz = CoreApi.class, includes = {"id", "apiName", "apiUrl"})})
    public ResponseInfo get(@PathVariable(name = "id") Integer id) throws GlobalException {
        CoreMenu menu = menuService.get(id);
        // 获取到的是从父级往上开始获取，所以需要反转
        List<Integer> list = new ArrayList<>();
        getParentIds(list, menu.getParentId());
        Collections.reverse(list);
        menu.setParentIds(list.toArray(new Integer[0]));
        return new ResponseInfo(menu);
    }

    /**
     * 保存
     */
    @EncryptMethod
    @PostMapping()
    public ResponseInfo saveChild(@RequestBody @Valid CoreMenuDto menuDto, BindingResult result)
            throws GlobalException {
        super.checkServerMode();
        super.validateBindingResult(result);
        lock.lock();
        try {
            String name = menuDto.getName();
            if (!this.ckRouting(name, null)) {
                return new ResponseInfo(SettingErrorCodeEnum.ROUTING_ALREADY_EXIST.getCode(),
                        SettingErrorCodeEnum.ROUTING_ALREADY_EXIST.getDefaultMessage());
            }
            String path = menuDto.getPath();
            if (!this.ckPath(path, null)) {
                return new ResponseInfo(SettingErrorCodeEnum.ROUTING_ADDRESS_ALREADY_EXIST.getCode(),
                        SettingErrorCodeEnum.ROUTING_ADDRESS_ALREADY_EXIST.getDefaultMessage());
            }
            menuService.save(menuDto);
        } finally {
            lock.unlock();
        }
        return new ResponseInfo();
    }

    /**
     * 更新
     */
    @EncryptMethod
    @PutMapping()
    public ResponseInfo update(@RequestBody @Valid CoreMenuDto menuDto, BindingResult result) throws GlobalException {
        super.checkServerMode();
        super.validateBindingResult(result);
        CoreMenu menuInfo = menuService.findById(menuDto.getId());
        List<CoreApi> apiList = new ArrayList<>();
        if (menuInfo.getApis() != null) {
            menuInfo.getApis().clear();
        }
        Integer[] apiIds = menuDto.getApiIds();
        if (apiIds != null && apiIds.length != 0) {
            apiList = apiService.findAllById(Arrays.asList(apiIds));
        }
        menuInfo.setApis(apiList);
        menuInfo.setParentId(menuDto.getParentId());
        menuInfo.setMenuName(menuDto.getMenuName());
        menuInfo.setComponent(menuDto.getComponent());
        menuInfo.setPath(menuDto.getPath());
        menuInfo.setName(menuDto.getName());
        menuInfo.setIcon(menuDto.getIcon());
        menuInfo.setRedirect(menuDto.getRedirect());
        menuInfo.setSortNum(menuDto.getSortNum());
        menuInfo.setMenuType(menuDto.getMenuType());
        menuInfo.setHidden(menuDto.getHidden());
        menuInfo.setIsAuth(menuDto.getIsAuth());
        lock.lock();
        try {
            if (StringUtils.isNotBlank(menuDto.getName()) && !this.ckRouting(menuDto.getName(), menuDto.getId())) {
                    return new ResponseInfo(SettingErrorCodeEnum.ROUTING_ALREADY_EXIST.getCode(),
                            SettingErrorCodeEnum.ROUTING_ALREADY_EXIST.getDefaultMessage());
            }
            if (StringUtils.isNotBlank(menuDto.getPath()) && !this.ckPath(menuDto.getPath(), menuDto.getId())) {
                return new ResponseInfo(SettingErrorCodeEnum.ROUTING_ADDRESS_ALREADY_EXIST.getCode(),
                        SettingErrorCodeEnum.ROUTING_ADDRESS_ALREADY_EXIST.getDefaultMessage());
            }
            menuService.update(menuInfo);
        } finally {
            lock.unlock();
        }
        return new ResponseInfo();
    }

	/**
	 * 检查菜单地址是否唯一
	 *
	 * @param id
	 *            菜单id
	 * @param routing
	 *            路由标识
	 * @return ResponseInfo
	 */
    @EncryptMethod
	@RequestMapping(value = "/routes/unique", method = RequestMethod.GET)
	public ResponseInfo checkPath(@RequestParam(name = "id", required = false) Integer id, @RequestParam String routing) {
		Boolean result = this.ckPath(routing, id);
		return new ResponseInfo(result);
	}

    /**
     * 检查菜单标识是否唯一
     *
     * @param id   菜单id
     * @param name 路由标识
     * @return ResponseInfo
     */
    @GetMapping(value = "/name/unique")
    public ResponseInfo checkRouting(@RequestParam(name = "id", required = false) Integer id, @RequestParam String name)
            throws GlobalException {
        boolean result = this.ckRouting(name, id);
        return new ResponseInfo(result);
    }

    /**
     * 显示
     */
    @PutMapping(value = "/display")
    public ResponseInfo enable(@RequestBody @Valid DeleteDto bean, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        menuService.show(bean.getIds()[0]);
        return new ResponseInfo();
    }

    /**
     * 隐藏
     */
    @PutMapping(value = "/hidden")
    public ResponseInfo disable(@RequestBody @Valid DeleteDto bean, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        menuService.hide(bean.getIds()[0]);
        return new ResponseInfo();
    }

    /**
     * 开启参与权限分配
     */
    @PutMapping(value = "/openAuth")
    public ResponseInfo openAuth(@RequestBody @Valid DeleteDto bean, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        // 所有子菜单中级联选中
        Integer[] ids = bean.getIds();
        List<Integer> toOpenIds = new ArrayList<>();
        for (Integer id : ids) {
            toOpenIds.add(id);
            CoreMenu menu = menuService.findById(id);
            toOpenIds.addAll(CoreMenu.fetchIds(menu.getChildren()));
        }
        menuService.openAuth(toOpenIds);
        // 可能组织会需要调整权限，这里主动清空组织权限缓存
        orgService.clearAllOrgCache();
        return new ResponseInfo();
    }

    /**
     * 关闭参与权限分配
     */
    @PutMapping(value = "/closeAuth")
    public ResponseInfo closeAuth(@RequestBody @Valid DeleteDto bean, BindingResult result) throws GlobalException {
        super.validateBindingResult(result);
        /* 所有子菜单中级联选中 */
        Integer[] ids = bean.getIds();
        List<Integer> toCloseIds = new ArrayList<>();
        for (Integer id : ids) {
            toCloseIds.add(id);
            CoreMenu menu = menuService.findById(id);
            toCloseIds.addAll(CoreMenu.fetchIds(menu.getChildren()));
        }
        menuService.closeAuth(toCloseIds);
        /* 可能组织会需要调整权限，这里主动清空组织权限缓存 */
        orgService.clearAllOrgCache();
        return new ResponseInfo();
    }

    /**
     * 判断菜单标识是否唯一
     *
     * @param menuId  菜单id
     * @param routing 路由标识
     * @return Boolean true 唯一 false 不唯一
     */
    private boolean ckRouting(String routing, Integer menuId) throws GlobalException {
        if (StringUtils.isBlank(routing)) {
            return true;
        }
        CoreMenu routingMenu = menuService.findByRouting(routing);
        if (routingMenu == null) {
            return true;
        }
        if (menuId == null) {
            return false;
        } else {
            return routingMenu.getId().equals(menuId);
        }
    }

    /**
     * 判断路由地址是否唯一
     *
     * @param menuId 菜单id
     * @param path   路由地址
     * @return Boolean true 唯一 false 不唯一
     */
    private boolean ckPath(String path, Integer menuId) {
        if (StringUtils.isBlank(path)) {
            return true;
        }
        CoreMenu routingMenu = menuService.findByPathAndHasDeleted(path);
        if (routingMenu == null) {
            return true;
        }
        if (menuId == null) {
            return false;
        } else {
            return routingMenu.getId().equals(menuId);
        }
    }

    /**
     * 获取菜单的所有上级菜单id
     *
     * @param list     保存获取到的list
     * @param parentId 父级id
     */
    private void getParentIds(List<Integer> list, Integer parentId) {
        if (parentId != null) {
            list.add(parentId);
            CoreMenu menu = menuService.get(parentId);
            getParentIds(list, menu.getParentId());
        }
    }

    @Autowired
    private CoreMenuService menuService;
    @Autowired
    private CmsOrgService orgService;
    @Autowired
    private CoreApiService apiService;
}
