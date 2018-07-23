package org.galaxy.route;

import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.galaxy.common.constants.HttpConstants;
import org.galaxy.common.util.HttpUtil;
import org.galaxy.route.config.ServiceConfig;
import org.galaxy.route.util.ServicesConfigUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author LJY
 */
@Service
@Slf4j
public class RoutingService {

    /**
     * Proxy Key
     */
    private static final String PROXY_KEY = "proxy";

    /**
     * requestURI key
     */
    private static final String REQUEST_URI_KEY = "requestURI";

    private Map<String, ServiceConfig> serviceConfigMap;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    /**
     * 加载服务配置信息
     *
     * @param activeProfileName 当前spring启用的配置
     */
    public void loadConfig(String activeProfileName) {
        serviceConfigMap = ServicesConfigUtil.parseServiceConfig(activeProfileName);
    }

    /**
     * 分配请求到配置的指定服务
     *
     * @param ctx RequestContext
     * @throws MalformedURLException me
     */
    public void routing(RequestContext ctx) throws MalformedURLException {
        HttpServletRequest request = ctx.getRequest();
        String requestUri = urlPathHelper.getOriginatingRequestUri(request);

        // 重新组装request
        String ipAddress = HttpUtil.getIpAddress(request);
        ctx.addZuulRequestHeader(HttpConstants.HTTP_HEADER_X_FORWARDED_FOR, ipAddress);
        ctx.setSendZuulResponse(true);
        String method = request.getMethod();

        ctx.set(REQUEST_URI_KEY, requestUri);
        String actualProxy = (String) ctx.get(PROXY_KEY);
        ServiceConfig serviceConfig = this.serviceConfigMap.get(actualProxy);
        if (serviceConfig == null) {
            log.error("No Such Proxy: " + actualProxy + " Was Defined.");
            return;
        }
        String protocol = serviceConfig.getProtocol();
        String ip = serviceConfig.getIp();
        Integer port = serviceConfig.getPort();
        ctx.setRouteHost(new URL(protocol, ip, port, "/"));
        log.info("Start Routing...Method: " + method + ", RequestURI: " + requestUri
                + ", Protocol: " + protocol + ", Destiny IP: " + ip + ", Port: "
                + port);
    }

}
