package org.galaxy.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 网关服务配置类
 *
 * @author LJY
 */
@Getter
@Setter
@ToString
@Configuration
public class ApplicationConfig {

    /**
     * 是否启用日志收集
     */
    @Value("${collect.enabled}")
    private Boolean collectEnabled;

    /**
     * 扫描handler的包
     */
    @Value("${handler.scan.package}")
    private String handlerScanPackage;

}
