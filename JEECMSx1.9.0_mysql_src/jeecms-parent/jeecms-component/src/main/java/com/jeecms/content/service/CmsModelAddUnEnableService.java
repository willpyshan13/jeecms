package com.jeecms.content.service;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.content.domain.CmsModel;

import java.util.List;

/**
 * 模型新增未启用字段
 * @author: chenming
 * @date: 2021/4/24 14:28
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public interface CmsModelAddUnEnableService {

    /**
     * 新增内容赞赏，内容付费模型字段
     * @param models    模型集合
     * @return  List<CmsModel>
     */
    List<CmsModel> addPayReadAndReward(List<CmsModel> models);
}
