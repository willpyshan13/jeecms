/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.common.web.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.stereotype.Component;


/**
 * 智能表单浏览量缓存
 */
@Component
@Qualifier(value = CacheConstants.SMART_FORM_VIEW_CACHE)
public class SmartFormViewCache extends EhCacheFactoryBean {
}
