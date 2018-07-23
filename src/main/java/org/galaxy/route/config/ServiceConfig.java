package org.galaxy.route.config;

import lombok.Data;

/**
 * 各微服务IP和端口配置
 *
 * @author LJY
 */
@Data
public class ServiceConfig {

    private String proxy;

    private String protocol;

    private String ip;

    private Integer port;

}
