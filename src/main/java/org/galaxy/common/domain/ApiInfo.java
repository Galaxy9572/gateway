package org.galaxy.common.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author LJY
 */
@Data
public class ApiInfo {

    /**
     * 浏览器UA
     */
    private String userAgent;

    /**
     * IP
     */
    private String ip;

    /**
     * HTTP请求方式
     */
    private String method;

    /**
     * 请求的API
     */
    private String api;

    /**
     * 请求参数
     */
    private Map<String, String> parameters;

    public ApiInfo() {
    }

    private ApiInfo(Builder builder) {
        this.userAgent = builder.userAgent;
        this.ip = builder.ip;
        this.method = builder.method;
        this.api = builder.api;
        this.parameters = builder.parameters;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIp() {
        return ip;
    }

    public String getMethod() {
        return method;
    }

    public String getApi() {
        return api;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }


    public static class Builder {
        private String userAgent;
        private String ip;
        private String method;
        private String api;
        private Map<String, String> parameters;

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder api(String api) {
            this.api = api;
            return this;
        }

        public Builder parameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public ApiInfo build() {
            return new ApiInfo(this);
        }
    }
}
