package org.galaxy.zuul.enums;

/**
 * Zuul过滤器类型枚举类
 *
 * @author LJY
 */
public enum FilterTypeEnum {

    /**
     * 进行路由之前执行的过滤器类型
     */
    PRE("pre"),

    /**
     * 进行路由时执行的过滤器类型
     */
    ROUTE("route"),

    /**
     * 进行路由之后执行的过滤器类型
     */
    POST("post"),

    /**
     * 任何一阶段发生异常时执行的过滤器类型
     */
    ERROR("error");

    private String type;

    FilterTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
