package org.galaxy.common.util;

import org.springframework.context.ApplicationContext;

/**
 * @author LJY
 */
public class SpringContextUtil {

    private static ApplicationContext applicationContext;

    /**
     * 获取上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 设置上下文
     *
     * @param applicationContext ApplicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 通过名字获取上下文中的bean
     *
     * @param name BeanName
     * @return Object bean
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 通过类型获取上下文中的bean
     *
     * @param requiredType Class<?>
     * @return Object bean
     */
    public static Object getBean(Class<?> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * 获取当前启用的配置文件环境名
     *
     * @return 例如启用的是application-test.properties，那么返回test
     */
    public static String getActiveProfileName() {
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }

}
