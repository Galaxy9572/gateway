package org.galaxy.collect.log;

import org.galaxy.common.domain.ApiInfo;
import org.galaxy.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用日志文件的方式收集API数据的实现类
 *
 * @author LJY
 */
@Component("log")
public class LogFileApiInfoCollectorImpl implements IApiInfoCollector {

    private static final Logger API_INFO_COLLECT_LOGGER;

    static {
        API_INFO_COLLECT_LOGGER = LoggerFactory.getLogger("ApiInfoCollectLogger");
    }

    @Override
    public void collect(HttpServletRequest request, Map<String, String> parameters) {
        String userAgent = HttpUtil.getUserAgent(request);
        String ipAddress = HttpUtil.getIpAddress(request);

        ApiInfo apiInfo = new ApiInfo.Builder()
                .userAgent(userAgent)
                .ip(ipAddress)
                .method(request.getMethod())
                .api(request.getRequestURI())
                .parameters(parameters).build();
        API_INFO_COLLECT_LOGGER.info((String) format(apiInfo));

    }

}
