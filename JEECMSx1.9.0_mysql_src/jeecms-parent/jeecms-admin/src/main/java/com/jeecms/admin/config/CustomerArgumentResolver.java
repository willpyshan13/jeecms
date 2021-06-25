package com.jeecms.admin.config;

import com.jeecms.common.annotation.EncryptParam;
import com.jeecms.common.web.ApplicationContextProvider;
import com.jeecms.protection.service.GradeProtectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义参数装载,base64解密
 * 
 * @author: tom
 * @date: 2020年07月31日 上午10:27:14
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class CustomerArgumentResolver implements HandlerMethodArgumentResolver {

	/**
	 * 日志对象
	 */
	private Logger logger = LoggerFactory.getLogger(CustomerArgumentResolver.class);
	private GradeProtectionService gradeProtectionService;
	public CustomerArgumentResolver() {
		gradeProtectionService = ApplicationContextProvider.getBean(GradeProtectionService.class);
	}

	/**
	 * 该方法返回true时调用resolveArgument方法执行逻辑 spring家族的架构设计万变不离其宗啊，在之前event &
	 * listener也是用到了同样的方式
	 * 
	 * @param methodParameter
	 *            MethodParameter
	 * @return
	 */
	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.hasParameterAnnotation(EncryptParam.class);
	}

	/**
	 * 装载参数
	 * 
	 * @param methodParameter
	 *            方法参数
	 * @param modelAndViewContainer
	 *            返回视图容器
	 * @param nativeWebRequest
	 *            本次请求对象
	 * @param webDataBinderFactory
	 *            数据绑定工厂
	 * @throws Exception
	 *             Exception
	 */
	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
		String parameterName = methodParameter.getParameterName();
		logger.info("参数名称：{}", parameterName);
		/**
		 * 目标返回对象 如果Model存在该Attribute时从module内获取并设置为返回值
		 * 如果Model不存在该Attribute则从request parameterMap内获取并设置为返回值
		 */
		if (modelAndViewContainer != null) {
			Object target = modelAndViewContainer.containsAttribute(parameterName)
					? modelAndViewContainer.getModel().get(parameterName)
					: createAttribute(parameterName, methodParameter, webDataBinderFactory, nativeWebRequest);
			/**
			 * 返回内容，这里返回的内容才是最终装载到参数的值
			 */
			if(gradeProtectionService!=null){
				if(target instanceof String){
					target = gradeProtectionService.decryptStr((String) target);
					// 存放到model内
					modelAndViewContainer.addAttribute(parameterName, target);
				}else if(target instanceof  String[]){
					String[] objs = (String[]) target;
					String[] array = new String[objs.length];
					for(Integer i=0;i<objs.length;i++){
						array[i]=gradeProtectionService.decryptStr(objs[i]);
					}
					// 存放到model内
					modelAndViewContainer.addAttribute(parameterName, array);
				}else if(target instanceof  List){
					Collection<Object> objs = (Collection<Object>) target;
					List<Object> list = new ArrayList<>(objs.size());
					for (Object o : objs) {
						list.add(gradeProtectionService.decryptStr(String.valueOf(o)));
					}
					// 存放到model内
					modelAndViewContainer.addAttribute(parameterName, list);
				}else if(target instanceof  Set){
					Collection<Object> objs = (Collection<Object>) target;
					Set<Object> set = new HashSet<>(objs.size());
					for (Object o : objs) {
						set.add(gradeProtectionService.decryptStr(String.valueOf(o)));
					}
					modelAndViewContainer.addAttribute(parameterName, set);
				}
			}
			return target;
		}
		return new Object();
	}

	/**
	 * Extension point to create the model attribute if not found in the model.
	 * The default implementation uses the default constructor.
	 *
	 * @param attributeName
	 *            the name of the attribute, never {@code null}
	 * @param parameter
	 *            the method parameter
	 * @param binderFactory
	 *            for creating WebDataBinder instance
	 * @param request
	 *            the current request
	 * @return the created model attribute, never {@code null}
	 */
	protected Object createAttribute(String attributeName, MethodParameter parameter,
									 WebDataBinderFactory binderFactory, NativeWebRequest request) throws Exception {

		String value = getRequestValueForAttribute(attributeName, request);

		if (value != null) {
			Object attribute = createAttributeFromRequestValue(
					value, attributeName, parameter, binderFactory, request);
			if (attribute != null) {
				return attribute;
			}
		}
		return BeanUtils.instantiateClass(parameter.getParameterType());
	}

	/**
	 * Obtain a value from the request that may be used to instantiate the model
	 * attribute through type conversion from String to the target type.
	 * The default implementation looks for the attribute name to match a URI
	 * variable first and then a request parameter.
	 *
	 * @param attributeName
	 *            the model attribute name
	 * @param request
	 *            the current request
	 * @return the request value to try to convert or {@code null}
	 */
	protected String getRequestValueForAttribute(String attributeName, NativeWebRequest request) {
		Map<String, String> variables = getUriTemplateVariables(request);
		if (StringUtils.hasText(variables.get(attributeName))) {
			return variables.get(attributeName);
		} else if (StringUtils.hasText(request.getParameter(attributeName))) {
			return request.getParameter(attributeName);
		} else {
			return null;
		}
	}

	/**
	 * Create a model attribute from a String request value (e.g. URI template
	 * variable, request parameter) using type conversion.
	 * The default implementation converts only if there a registered
	 * {@link org.springframework.core.convert.converter.Converter} that can
	 * perform the conversion.
	 *
	 * @param sourceValue
	 *            the source value to create the model attribute from
	 * @param attributeName
	 *            the name of the attribute, never {@code null}
	 * @param parameter
	 *            the method parameter
	 * @param binderFactory
	 *            for creating WebDataBinder instance
	 * @param request
	 *            the current request
	 * @return the created model attribute, or {@code null}
	 * @throws Exception Exception
	 */
	protected Object createAttributeFromRequestValue(String sourceValue, String attributeName,
													 MethodParameter parameter, WebDataBinderFactory binderFactory,
													 NativeWebRequest request) throws Exception {
		DataBinder binder = binderFactory.createBinder(request, null, attributeName);
		ConversionService conversionService = binder.getConversionService();
		if (conversionService != null) {
			TypeDescriptor source = TypeDescriptor.valueOf(String.class);
			TypeDescriptor target = new TypeDescriptor(parameter);
			if (conversionService.canConvert(source, target)) {
				return binder.convertIfNecessary(sourceValue, parameter.getParameterType(), parameter);
			}
		}
		return null;
	}

	/**
	 * Validate the model attribute if applicable.
	 * The default implementation checks for {@code @javax.validation.Valid}.
	 *
	 * @param binder
	 *            the DataBinder to be used
	 * @param parameter
	 *            the method parameter
	 */
	protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
		Annotation[] annotations = parameter.getParameterAnnotations();
		for (Annotation annot : annotations) {
			if (annot.annotationType().getSimpleName().startsWith("Valid")) {
				Object hints = AnnotationUtils.getValue(annot);
				binder.validate(hints instanceof Object[] ? (Object[]) hints : new Object[] { hints });
			}
		}
	}

	/**
	 * 绑定请求参数
	 * @param binder WebDataBinder
	 * @param nativeWebRequest NativeWebRequest
	 * @throws Exception Exception
	 */
	protected void bindRequestAttributes(WebDataBinder binder, NativeWebRequest nativeWebRequest) throws Exception {
		/**
		 * 获取返回对象实例
		 */
		Object obj = binder.getTarget();
		/**
		 * 获取返回值类型
		 */
		if (obj != null) {
			Class<?> targetType = obj.getClass();
			/**
			 * 转换本地request对象为HttpServletRequest对象
			 */
			HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
			/**
			 * 获取所有attributes
			 */
			if (request != null) {
				Enumeration attributeNames = request.getAttributeNames();
				/**
				 * 遍历设置值
				 */
				while (attributeNames.hasMoreElements()) {
					// 获取attribute name
					String attributeName = String.valueOf(attributeNames.nextElement());
					Field field = null;
					try {
						field = targetType.getDeclaredField(attributeName);
					} catch (NoSuchFieldException e) {
						/**
						 * 如果返回对象类型内不存在字段 则从父类读取
						 */
						try {
							field = targetType.getSuperclass().getDeclaredField(attributeName);
						} catch (NoSuchFieldException e2) {
							continue;
						}
						/**
						 * 如果父类还不存在，则直接跳出循环
						 */
						if (StringUtils.isEmpty(field)) {
							continue;
						}
					}
					/**
					 * 设置字段的值
					 */
					field.setAccessible(true);
					Object attributeObj = request.getAttribute(attributeName);
					if (attributeObj != null) {
						field.set(obj, attributeObj);
					}
				}
				ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
				servletBinder.bind(request);
			}
		}
	}

	protected final Map<String, String> getUriTemplateVariables(NativeWebRequest request) {
		Map<String, String> variables = (Map<String, String>) request
				.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
						RequestAttributes.SCOPE_REQUEST);
		return (variables != null) ? variables : Collections.emptyMap();
	}

}
