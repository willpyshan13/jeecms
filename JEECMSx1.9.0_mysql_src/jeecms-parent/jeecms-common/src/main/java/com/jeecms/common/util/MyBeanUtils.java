package com.jeecms.common.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

import com.jeecms.common.base.domain.AbstractIdDomain;

/**
 * BeanUtils copyProperties 过滤null
 * 
 * @author: tom
 * @date: 2018年4月11日 下午3:25:30
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class MyBeanUtils extends org.springframework.beans.BeanUtils {

	/**
	 * 拷贝一个对象至另一个对象，（，为空属性不拷贝，子对象无法拷贝）
	 * 
	 * @Title: copyProperties
	 * @Description:
	 * @param: @param
	 *             source
	 * @param: @param
	 *             target
	 * @param: @throws
	 *             BeansException
	 * @return: void
	 */
	public static void copyProperties(Object source, Object target)  {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");
		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<PropertyDescriptor> targetPdList = new ArrayList<>();
		for(PropertyDescriptor targetPd:targetPds){
			boolean needAdd = targetPd.getWriteMethod() != null
					&&getPropertyDescriptor(source.getClass(), targetPd.getName())!=null
					&&getPropertyDescriptor(source.getClass(), targetPd.getName()).getReadMethod()!=null;
			if (needAdd) {
				targetPdList.add(targetPd);
			}
		}
		for (PropertyDescriptor targetPd : targetPdList) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				try {
					Method readMethod = sourcePd.getReadMethod();
					if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
						readMethod.setAccessible(true);
					}
					Object value = readMethod.invoke(source);
					// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
					boolean valIdBase = value instanceof AbstractIdDomain;
					/**不复制基类的子对象，可能有些实体类用了级联操作 更新的时候 原本对象有值，这里又复制导致错误*/
					if (value != null && !valIdBase) {
						Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, value);
					}
				} catch (Exception ex) {
					throw new FatalBeanException("" + "Could not copy properties from source to target", ex);
				}
		}
	}

	/**
	 * 拷贝一个对象至另一个对象，（，为空属性不拷贝，子对象无法拷贝）
	 * 
	 * @Title: copyProperties
	 * @Description:
	 * @param: @param
	 *             source
	 * @param: @param
	 *             target
	 * @param: @throws
	 *             BeansException
	 * @return: void
	 */
	public static void copyPropertiesContentNull(Object source, Object target) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");
		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<PropertyDescriptor> targetPdList = new ArrayList<>();
		for(PropertyDescriptor targetPd:targetPds){
			if (targetPd.getWriteMethod() != null) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					targetPdList.add(targetPd);
				}
			}
		}
		for (PropertyDescriptor targetPd : targetPdList) {
			PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
			try {
				Method readMethod = sourcePd.getReadMethod();
				if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
					readMethod.setAccessible(true);
				}
				Object value = readMethod.invoke(source);
				// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
				Method writeMethod = targetPd.getWriteMethod();
				if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
					writeMethod.setAccessible(true);
				}
				writeMethod.invoke(target, value);
			} catch (Exception ex) {
				throw new FatalBeanException("" + "Could not copy properties from source to target", ex);
			}
		}
	}


}
