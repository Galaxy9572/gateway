package org.galaxy.collect.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handler注解，用于类上，表名这个类中的方法与哪一批API的URL相映射
 *
 * @author LJY
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Handler {

    /**
     * API的Pattern，写法参考springMVC，例如/api/post/**，表名这个被@Handler注解了的类会匹配/api/post下所有的API
     */
    String path() default "";

}
