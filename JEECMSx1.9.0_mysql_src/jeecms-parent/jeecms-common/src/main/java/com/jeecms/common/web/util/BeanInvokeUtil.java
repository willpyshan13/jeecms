package com.jeecms.common.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 调用springbean的私有方法，采用CGLIB
 */
public class BeanInvokeUtil {
    //子类
    public class InvokeParams {

        // 方法名称（私有）
        private String methodName;

        // 参数列表类型数组
        private Class<?>[] paramTypes;
        // 调用的对象
        private Object object;

        // 参数列表数组（如果不为null，需要和paramTypes对应）
        private Object[] args;

        public InvokeParams() {
            super();
        }

        public InvokeParams(Object object, String methodName, Class<?>[] paramTypes, Object[] args) {
            this.methodName = methodName;
            this.paramTypes = paramTypes;
            this.object = object;
            this.args = args;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(Class<?>[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }
    }

    public static Object invokePrivateMethod(InvokeParams invokeParams) throws InvocationTargetException, IllegalAccessException {
        // 参数检查
        checkParams(invokeParams);
        // 调用
        return doInvoke(invokeParams);
    }

    private static Object doInvoke(InvokeParams invokeParams) throws InvocationTargetException, IllegalAccessException {
        Object object = invokeParams.getObject();
        String methodName = invokeParams.getMethodName();
        Class<?>[] paramTypes = invokeParams.getParamTypes();
        Object[] args = invokeParams.getArgs();

        Method method;
        if (paramTypes == null) {
            method = BeanUtils.findDeclaredMethod(object.getClass(), methodName);
        } else {
            method = BeanUtils.findDeclaredMethod(object.getClass(), methodName, paramTypes);
        }
        method.setAccessible(true);
        if (args == null) {
            return method.invoke(object);
        }
        return method.invoke(object, args);

    }

    private static void checkParams(InvokeParams invokeParams) {
        Object object = invokeParams.getObject();
        if (object == null) {
            throw new IllegalArgumentException("object can not be null");
        }
        String methodName = invokeParams.getMethodName();
        if (StringUtils.isEmpty(methodName)) {
            throw new IllegalArgumentException("methodName can not be empty");
        }

        // 参数类型数组和参数数组要对应
        Class<?>[] paramTypes = invokeParams.getParamTypes();
        Object[] args = invokeParams.getArgs();

        boolean illegal = true;
        if (paramTypes == null && args != null) {
            illegal = false;
        }
        if (args == null && paramTypes != null) {
            illegal = false;
        }
        if (paramTypes != null && args != null && paramTypes.length != args.length) {
            illegal = false;
        }
        if (!illegal) {
            throw new IllegalArgumentException("paramTypes length != args length");
        }
    }

}
