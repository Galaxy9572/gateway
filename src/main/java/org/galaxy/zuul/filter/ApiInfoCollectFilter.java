package org.galaxy.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.galaxy.collect.annotations.Filter;
import org.galaxy.collect.annotations.Handler;
import org.galaxy.collect.annotations.Mapping;
import org.galaxy.collect.handlers.BaseHandler;
import org.galaxy.common.config.ApplicationConfig;
import org.galaxy.common.enums.HttpMethod;
import org.galaxy.common.util.ReflectionUtil;
import org.galaxy.common.util.SpringContextUtil;
import org.galaxy.zuul.enums.FilterTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * API数据收集过滤器
 *
 * @author LJY
 */
@Slf4j
@Filter
public class ApiInfoCollectFilter extends ZuulFilter {

    private ApplicationConfig applicationConfig;

    /**
     * Map<Handler的path, Class<BaseHandler>>
     */
    private static Map<String, Class<BaseHandler>> handlerMap = new HashMap<>();

    /**
     * Map<Class<BaseHandler>, Map<HttpMethod:mapping的url, Method>>
     */
    private static Map<Class<BaseHandler>, Map<String, Method>> mappingMap = new HashMap<>();

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public ApiInfoCollectFilter(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @PostConstruct
    public void init() throws ClassNotFoundException {
        initHandlers();
        initMappings();
    }

    /**
     * 在路由之后进行API数据的收集
     *
     * @return filterType
     */
    @Override
    public String filterType() {
        return FilterTypeEnum.POST.getType();
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 是否收集API数据
     *
     * @return 收集则为true，否则为false
     */
    @Override
    public boolean shouldFilter() {
        return applicationConfig.getCollectEnabled();
    }

    @Override
    public Object run() {
        collect(RequestContext.getCurrentContext());
        return null;
    }

    /**
     * 根据请求，反射到对应的收集数据的Handler中进行处理
     *
     * @param ctx RequestContext
     */
    private void collect(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        //根据request中的请求方法找到HttpMethod中的枚举
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String requestURI = request.getRequestURI();

        try {
            Method handleMethod = getHandleMethod(method, requestURI);
            if (handleMethod != null) {
                String apiPattern = handleMethod.getAnnotation(Mapping.class).url();
                Object[] params = ReflectionUtil.injectParameters(handleMethod, apiPattern, request, response);
                Class<?> bean = handleMethod.getDeclaringClass();
                handleMethod.invoke(SpringContextUtil.getBean(bean), params);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Collect Failed, RequestURI: " + requestURI, e);
        }
    }

    /**
     * 获取处理对应API请求的方法
     *
     * @param method     HttpMethod
     * @param requestURI requestURI
     * @return Method
     */
    private Method getHandleMethod(HttpMethod method, String requestURI) {
        Set<Map.Entry<String, Class<BaseHandler>>> handlerMapEntries = handlerMap.entrySet();
        if (CollectionUtils.isEmpty(handlerMapEntries)) {
            return null;
        }
        // 处理请求的方法
        Method handleMethod = null;
        Map<String, Method> methodMap = null;
        for (Map.Entry<String, Class<BaseHandler>> handlerMapEntry : handlerMapEntries) {
            // Handler的path值
            String handlerPath = handlerMapEntry.getKey();
            if (pathMatcher.match(handlerPath, requestURI)) {
                Class<BaseHandler> handlerClass = handlerMap.get(handlerPath);
                methodMap = mappingMap.get(handlerClass);
                break;
            }
        }
        if (CollectionUtils.isEmpty(methodMap)) {
            return null;
        }
        Set<Map.Entry<String, Method>> methodMapEntries = methodMap.entrySet();
        for (Map.Entry<String, Method> methodMapEntry : methodMapEntries) {
            // Http请求方式:API
            String requestMethodApi = methodMapEntry.getKey();
            String[] splitStr = requestMethodApi.split(":");
            String httpMethod = splitStr[0];
            String apiPattern = splitStr[1];
            boolean equals = httpMethod.equals(method.getMethod());
            boolean match = pathMatcher.match(apiPattern, requestURI);
            if (equals && match) {
                handleMethod = methodMap.get(requestMethodApi);
                break;
            }
        }
        return handleMethod;
    }

    /**
     * 初始化带有@Handler注解的类
     *
     * @throws ClassNotFoundException e
     */
    private void initHandlers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Handler.class));
        //handler所在的包
        Set<BeanDefinition> components = provider
                .findCandidateComponents(applicationConfig.getHandlerScanPackage());
        if (CollectionUtils.isEmpty(components)) {
            return;
        }
        for (BeanDefinition component : components) {
            Class<BaseHandler> handlerClass = (Class<BaseHandler>) Class.forName(component.getBeanClassName());
            Handler handler = handlerClass.getAnnotation(Handler.class);
            if (handler == null) {
                continue;
            }
            String path = handler.path();
            handlerMap.put(path, handlerClass);
        }
    }

    /**
     * 初始化带有@Mapping注解的方法
     */
    private void initMappings() {
        Set<Map.Entry<String, Class<BaseHandler>>> entries = handlerMap.entrySet();
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }
        entries.forEach(entry -> {
            Class<BaseHandler> handlerClass = entry.getValue();
            Method[] declaredMethods = handlerClass.getDeclaredMethods();
            if (ArrayUtils.isEmpty(declaredMethods)) {
                return;
            }
            Map<String, Method> methodMap = new HashMap<>(8);
            //循环处理handler中定义的API处理方法
            for (Method method : declaredMethods) {
                Mapping mapping = method.getAnnotation(Mapping.class);
                if (mapping == null) {
                    continue;
                }
                HttpMethod requestMethod = mapping.method();
                String url = mapping.url();
                methodMap.put(requestMethod + ":" + url, method);
            }
            mappingMap.put(handlerClass, methodMap);
        });
    }

}
