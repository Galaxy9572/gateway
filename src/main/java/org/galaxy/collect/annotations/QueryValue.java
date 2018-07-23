package org.galaxy.collect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 匹配requestURI中问号后面的查询参数的注解
 * 类似springMVC的<code>@RequestParam</code>
 *
 * @author LJY
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryValue {

    /**
     * 参数名
     */
    String value();
}
