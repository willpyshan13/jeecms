/*
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.common.web.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.stereotype.Component;

/**
 * 评论点赞缓存
 *
 * @author xiaohui
 * @version 1.2
 * @date 2020/3/27 16:14
 */
@Component
@Qualifier(value = CacheConstants.UP_CACHE)
public class UpCache extends EhCacheFactoryBean {
}
