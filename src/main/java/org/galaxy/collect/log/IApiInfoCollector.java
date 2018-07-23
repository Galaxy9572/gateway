package org.galaxy.collect.log;

import org.galaxy.common.domain.ApiInfo;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * 收集API数据的接口
 *
 * @author LJY
 */
public interface IApiInfoCollector {

    /**
     * 收集数据
     *
     * @param request    HttpServletRequest
     * @param parameters Map<String,String>
     */
    void collect(HttpServletRequest request, Map<String, String> parameters);

    /**
     * 收集数据
     *
     * @param request HttpServletRequest
     */
    default void collect(HttpServletRequest request) {
        collect(request, null);
    }

    /**
     * 格式化API数据
     *
     * @param apiInfo ApiInfo
     * @return 格式化后的API数据
     */
    default Object format(ApiInfo apiInfo) {
        String s = apiInfo.getUserAgent() + "|" + apiInfo.getIp() + "|" + apiInfo.getMethod() + "|"
                + apiInfo.getApi() + "|";
        Map<String, String> parameters = apiInfo.getParameters();
        StringBuilder paramStr = new StringBuilder();
        if (!CollectionUtils.isEmpty(parameters)) {
            Set<Map.Entry<String, String>> entries = parameters.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                paramStr.append(key).append(":").append(value).append("&");
            }
            paramStr.deleteCharAt(paramStr.length() - 1);
        }
        return s + paramStr.toString();
    }

}
