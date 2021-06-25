package com.jeecms.common.configuration;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 * <p>
 * SpringMvcConfigure
 * @author: tom 静态html支持post请求
 * @date: 2021/3/23 10:57
 */

/**
 * SpringMvcConfigure 
 * @author: tom 静态html支持post请求
 * @date: 2021/3/23 10:57   
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SpringMvcConfigure implements WebMvcConfigurer {

    private int order = Ordered.LOWEST_PRECEDENCE - 2;

    /** 自定义HandlerMapping实例 */
    @Bean("staticPageHandlerMapping")
    public SimpleUrlHandlerMapping staticPageHandlerMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        Map<String, Object> urlMap = new LinkedHashMap<>();
        urlMap.put("/p*/**", staticHandler());
        urlMap.put("/m*/**", staticHandler());
        simpleUrlHandlerMapping.setUrlMap(urlMap);
        // 重要:设置顺序优先级优于默认handleMapping,否则不生效(默认order=Ordered.LOWEST_PRECEDENCE - 1)
        simpleUrlHandlerMapping.setOrder(order);
        // DispatcherServlet的initHandlerMappings方法会自动扫描容器中的所有HandlerMapping类型实例
        return simpleUrlHandlerMapping;
    }

    /** 加入Bean注解借助spring初始化一些关键属性例,如:afterPropertiesSet()方法 */
    @Bean("staticHandler")
    public ResourceHttpRequestHandler staticHandler() {
        ResourceHttpRequestHandler myHandler = new ResourceHttpRequestHandler();
        // 此处添加POST方式(默认只支持GET、HEAD)
        myHandler.setSupportedMethods(WebContentGenerator.METHOD_GET, WebContentGenerator.METHOD_HEAD, WebContentGenerator.METHOD_POST);
        myHandler.setLocationValues(Arrays.asList("/"));
        return myHandler;
    }
}