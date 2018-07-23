package org.galaxy.common.util;

import org.apache.commons.lang.ArrayUtils;
import org.galaxy.collect.annotations.PathValue;
import org.galaxy.collect.annotations.QueryValue;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * 反射工具类
 *
 * @author LJY
 */
public class ReflectionUtil {

    private static UrlPathHelper urlPathHelper = new UrlPathHelper();

    private static PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 向方法的参数注入值
     *
     * @param method     将要被注入参数值的方法
     * @param mappingUrl @Mapping注解中url的值
     * @param request    HttpServletRequest
     * @param response   HttpServletResponse
     * @return 注入完值的参数数组
     */
    public static Object[] injectParameters(Method method, String mappingUrl,
                                            HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];
        // Query Params <paramName,value>
        Map<String, String[]> queryParams = request.getParameterMap();
        Map<String, String> map = pathMatcher.extractUriTemplateVariables(mappingUrl, requestURI);
        // Path Params <paramName,value>
        Map<String, String> pathVariables = urlPathHelper.decodePathVariables(request, map);

        if (!ArrayUtils.isEmpty(parameters)) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                // 向方法中带有@PathValue的参数中注入参数值
                PathValue pathValue = parameter.getAnnotation(PathValue.class);
                if (pathValue != null) {
                    params[i] = pathVariables.get(pathValue.value());
                    continue;
                }
                // 向方法中带有@QueryValue的参数中注入参数值
                QueryValue queryValue = parameter.getAnnotation(QueryValue.class);
                if (queryValue != null) {
                    String[] value = queryParams.get(queryValue.value());
                    if (!ArrayUtils.isEmpty(value)) {
                        params[i] = value[0];
                    }
                    continue;
                }
                // 注入HttpServletRequest
                if (parameter.getType() == HttpServletRequest.class) {
                    params[i] = request;
                    continue;
                }
                // 注入HttpServletResponse
                if (parameter.getType() == HttpServletResponse.class) {
                    params[i] = response;
                }
            }
        }
        return params;
    }

}
