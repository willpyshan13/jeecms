/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.dao.ext;

/**
 * 模型字段dao扩展接口
 * @author: chenming
 * @date: 2020/3/27 10:55
 */
public interface CmsModelItemDaoExt {

    /**
     * 通过模型和字段类型查询出排序值最高的字段
     * @param modelId   模型id
     * @param dataType  字段类型
     * @return  String
     */
    String getModelItemByDataType(Integer modelId,String dataType);

}
