package org.galaxy.collect.handlers;

import org.galaxy.collect.log.IApiInfoCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 处理器实现类需要继承的类，每个处理器按照实际情况决定需要Override哪些方法
 *
 * @author LJY
 */
@Component
public class BaseHandler {

    @Qualifier("log")
    @Autowired
    protected IApiInfoCollector collector;

}
