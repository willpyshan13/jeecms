package com.jeecms.common.annotation;

import java.lang.annotation.*;

/**
 * 异步开始评分注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface SyncStartScore {
    String value() default "";
}
