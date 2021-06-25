package com.jeecms.common.manage.annotation;

import java.lang.annotation.*;

/**
 * 操作阻止注解
 *
 * @author: chenming
 * @date: 2019年5月6日 下午2:35:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface OperatingIntercept {

}
