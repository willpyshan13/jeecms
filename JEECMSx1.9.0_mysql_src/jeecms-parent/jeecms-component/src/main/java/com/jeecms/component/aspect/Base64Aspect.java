package com.jeecms.component.aspect;

import com.jeecms.common.annotation.EncryptField;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.protection.service.GradeProtectionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description:处理添加@EncryptMethod注解的方法拦截业务，会判断是否需要bas64反转 普通形参注解 @EncryptParam  实体字段注解 @EncryptField
 * 普通形参反转实现在 CustomerArgumentResolver  字段注解反转实现在本切面 doAround
 * @author: tom
 * @date: 2018年7月26日 下午1:42:35
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Base64Aspect {
    Logger log = LoggerFactory.getLogger(Base64Aspect.class);

    @Pointcut("@annotation(com.jeecms.common.annotation.EncryptMethod)")
    public void annotationPointCut() {
    }

    @Around("annotationPointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object requestObj = joinPoint.getArgs()[0];
            if(!(requestObj instanceof BindingResult)){
                handleDecrypt(requestObj);
            }
        }catch (Throwable throwable) {
            log.error("SecureFieldAop处理出现异常{}", throwable);
        }
        return joinPoint.proceed();
    }

    /**
     * 处理解密
     *
     * @param responseObj
     */
    private Object handleDecrypt(Object responseObj) throws IllegalAccessException {
        if (Objects.isNull(responseObj)) {
            return null;
        }

        /***
         * 字段注解
         */
        Field[] fields = responseObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean hasSecureField = field.isAnnotationPresent(EncryptField.class);
            init();
            if (hasSecureField && gradeProtectionService != null) {
                field.setAccessible(true);
                Object obj = field.get(responseObj);
                String encryptValue;
                if (obj instanceof List) {
                    List<Object> objs = (List<Object>) obj;
                    List<Object> list = new ArrayList<>(objs.size());
                    for (Object o : objs) {
                        list.add(gradeProtectionService.decryptStr(String.valueOf(o)));
                    }
                    field.set(responseObj, list);
                } else if (obj instanceof String[]) {
                    String[] objs = (String[]) obj;
                    String[] strings = new String[objs.length];
                    for (int i = 0 ; i<objs.length; i++) {
                        strings[i] = gradeProtectionService.decryptStr(String.valueOf(objs[i]));
                    }
                    field.set(responseObj, strings);
                } else {
                    encryptValue = (String) obj;
                    String plaintextValue = gradeProtectionService.decryptStr(encryptValue);
                    field.set(responseObj, plaintextValue);
                }
            }
        }
        return responseObj;
    }

    GradeProtectionService gradeProtectionService;

    public void init() {
        gradeProtectionService = ApplicationContextProvider.getBean(GradeProtectionService.class);
    }

}
