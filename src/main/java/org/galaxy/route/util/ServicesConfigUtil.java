package org.galaxy.route.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.galaxy.route.config.ServiceConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析service-address.properties的配置类
 *
 * @author LJY
 */
@Slf4j
public class ServicesConfigUtil {

    private static final Pattern SERVICE_PROXY_PATTERN;

    private static final String SERVICE_PROXY_REGEX = "(.*?).service.proxy";

    private static Properties properties;

    static {
        SERVICE_PROXY_PATTERN = Pattern.compile(SERVICE_PROXY_REGEX);
        properties = new Properties();
    }

    /**
     * 解析service-address.properties的配置 Map<proxy,Map<seq,ServiceConfig>>
     *
     * @param activeProfileName 这个是spring的application.properties中的spring.active.profile的值，
     *                          service-address.properties的做法与spring一致,spring启用哪个，service-address就启用哪个
     * @return Map<String                                                               ,                                                                                                                               ServiceConfig>
     */
    public static Map<String, ServiceConfig> parseServiceConfig(
            String activeProfileName) {
        String serviceAddressProfile = "/service-address-" + activeProfileName + ".properties";
        InputStream inputStream = ServicesConfigUtil.class.getResourceAsStream(serviceAddressProfile);
        try {
            properties.load(inputStream);
            log.info("service-address-" + activeProfileName + ".properties Have Been Loaded.");
        } catch (IOException e) {
            log.error("Load service-address-" + activeProfileName + ".properties Failed.", e);
        }
        // 获取properties里所有的key
        Set<Object> keySet = properties.keySet();
        Map<String, ServiceConfig> serviceConfigMap = new HashMap<>(16);
        keySet.forEach((key) -> {
            Matcher proxyMatcher = SERVICE_PROXY_PATTERN.matcher((String) key);
            if (proxyMatcher.find()) {
                // 先把所有的proxy放入Map中
                String proxyKey = proxyMatcher.group();
                String proxy = properties.getProperty(proxyKey);
                serviceConfigMap.putIfAbsent(proxy, null);
            }
        });
        Set<String> proxyKeys = serviceConfigMap.keySet();
        proxyKeys.forEach((proxy) -> {
            String protocol = properties.getProperty(proxy + ".service.protocol");
            String address = properties.getProperty(proxy + ".service.address");
            if (StringUtils.isBlank(protocol)) {
                throw new IllegalArgumentException("Protocol Config Can Not Be Null, Proxy: " + proxy);
            }
            if (StringUtils.isBlank(address)) {
                throw new IllegalArgumentException("Address Config Can Not Be Null, Proxy: " + proxy);
            }
            ServiceConfig serviceConfig = new ServiceConfig();
            String[] ipAndPort = address.split(":");
            serviceConfig.setProxy(proxy);
            serviceConfig.setProtocol(protocol);
            serviceConfig.setIp(ipAndPort[0]);
            serviceConfig.setPort(Integer.parseInt(ipAndPort[1]));
            serviceConfigMap.put(proxy, serviceConfig);

        });
        return serviceConfigMap;
    }

}
