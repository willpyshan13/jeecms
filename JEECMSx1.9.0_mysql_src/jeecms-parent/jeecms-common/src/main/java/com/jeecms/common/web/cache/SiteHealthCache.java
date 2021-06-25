package com.jeecms.common.web.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.stereotype.Component;

/**
 * 站点检测缓存数据cache
 * @author: tom
 * @date: 2020年3月6日 上午9:43:25
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Component
@Qualifier(value = CacheConstants.SITE_HEALTH_CACHE)
public class SiteHealthCache extends EhCacheFactoryBean {

}
