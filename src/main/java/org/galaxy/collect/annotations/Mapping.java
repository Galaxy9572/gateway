package org.galaxy.collect.annotations;

import org.galaxy.common.enums.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mapping注解，用于方法上，表名这个方法与哪个API的URL相映射
 *
 * @author LJY
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    /**
     * Http请求方法
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * URL的Pattern，写法参考springMVC
     */
    String url();

}
