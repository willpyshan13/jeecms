package com.jeecms.component.aspect; /**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.SystemExceptionEnum;
import com.jeecms.common.manage.annotation.OperatingIntercept;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.manage.service.OperatingInterceptService;
import com.jeecms.util.SystemContextUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


/**
 * 检测操作校验切面(三员管理需要拦截诸多请求)
 * @author: chenming
 * @date: 2020/3/16 17:05   
 */
@Component
@Aspect
public class OperatingCheckAspect {
    Logger logger = LoggerFactory.getLogger(OperatingCheckAspect.class);

    @Autowired(required = false)
    private OperatingInterceptService service;

    @Before(value = "@annotation(operatingIntercept)")
    public void operatingIntercept(OperatingIntercept operatingIntercept) throws Throwable {

    }
}
