package org.galaxy.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.galaxy.collect.annotations.Filter;
import org.galaxy.route.RoutingService;
import org.galaxy.zuul.enums.FilterTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.MalformedURLException;

/**
 * 用于分发请求的过滤器
 *
 * @author LJY
 */
@Slf4j
@Filter
public class RoutingFilter extends ZuulFilter implements ApplicationListener<ContextRefreshedEvent> {

    private RoutingService routingService;

    @Autowired
    public RoutingFilter(RoutingService routingService) {
        this.routingService = routingService;
    }

    @Override
    public String filterType() {
        return FilterTypeEnum.ROUTE.getType();
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        try {
            // 转发请求到各自服务
            routingService.routing(ctx);
        } catch (MalformedURLException e) {
            log.error("", e);
        }
        return new Object();
    }

    /**
     * 容器启动后加载service-address.properties配置
     *
     * @param event ContextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String activeProfileName = event.getApplicationContext().getEnvironment().getActiveProfiles()[0];
        routingService.loadConfig(activeProfileName);
    }
}
