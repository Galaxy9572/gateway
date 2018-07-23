package org.galaxy.common.enums;

/**
 * Http请求方式枚举
 *
 * @author LJY
 */
public enum HttpMethod {

    /**
     * GET
     */
    GET("GET"),

    /**
     * POST
     */
    POST("POST"),

    /**
     * PUT
     */
    PUT("PUT"),

    /**
     * DELETE
     */
    DELETE("DELETE"),

    /**
     * PATCH
     */
    PATCH("PATCH"),

    /**
     * HEAD
     */
    HEAD("HEAD");

    private String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

}
