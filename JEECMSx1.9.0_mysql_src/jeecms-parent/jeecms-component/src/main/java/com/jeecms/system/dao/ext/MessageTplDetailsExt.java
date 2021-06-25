package com.jeecms.system.dao.ext;

import com.jeecms.common.exception.GlobalException;
import com.jeecms.system.domain.MessageTplDetails;

/**
 * 详细模版信息扩展 dao接口
 * @author: wlw
 * @date:
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。 JpaRepository Repository
 */
public interface MessageTplDetailsExt {

    /**
     * 获取消息模板详情
     * @param mesCode           消息模板标识
     * @param detailMesType     消息类型(消息类型 1、站内信 2、邮件 3、手机)
     * @param hasDeleted        逻辑删除标识
     * @return MessageTplDetails
     */
	MessageTplDetails findByMesCodeAndType(String mesCode, Short detailMesType,Integer siteId, boolean hasDeleted);

}
