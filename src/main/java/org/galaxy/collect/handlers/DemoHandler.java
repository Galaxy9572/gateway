package org.galaxy.collect.handlers;

import org.galaxy.collect.annotations.Handler;
import org.galaxy.collect.annotations.Mapping;
import org.galaxy.collect.annotations.PathValue;
import org.galaxy.common.enums.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示Handler
 *
 * @author LJY
 */
@Handler(path = "/api/demo/**")
public class DemoHandler extends BaseHandler {

    @Mapping(method = HttpMethod.GET, url = "/api/demo/hello/{id}")
    public void demo(HttpServletRequest request, HttpServletResponse response, @PathValue("id") String id) {
        Map<String, String> param = new HashMap<>(1);
        param.put("id", id);
        collector.collect(request, param);
    }

}
