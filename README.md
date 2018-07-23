# Gateway

## 一、概要

Gateway是运行在API服务接收到请求之前的一个服务，可以在其它各个服务接收到请求之前拦截请求，做一些处理，然后将对应的请求分发到各自的服务当中去。
在本服务中，会预先拦截部分请求，收集请求某些接口的API的数据，然后分发到各自的服务。

本服务使用zuul进行API请求的拦截、分发和数据的收集，Zuul有4种Filter，分别是Pre、Route、Post、Error。
本服务的请求分发采用RouteFilter，数据的收集采用PostFilter。

收集到的API数据会以日志的形式收集下来。

## 二、配置文件的使用

#### 2.1、application.properties

application.properties分为三个环境的配置文件(local、test、prod，分别用于本地、测试环境、生产环境)，在application.properties中控制使用哪个配置

该配置中有几个配置比较重要
    
    zuul的配置，该配置代表本服务会拦截/api开头的所有API请求，sensitive-headers设置为null就不会过滤request和response中的header了
    
   - zuul.routes.api.path=/api/**
   - zuul.sensitive-headers=
   
    使用哪个logback配置文件的配置
   
   - logging.config=logback.test.xml
   
    该配置代表是否开启API日志的收集，取值有true/false
   
   - collect.enabled=true
   
#### 2.2、logback.xml

    分为三个环境的配置文件(local、test、prod，分别用于本地、测试环境、生产环境)，在application-{运行环境}.properties中控制使用哪个配置

    API访问接口后，LogFileApiInfoCollectorImpl类通过Logback进行日志的记录，该配置是记录日志的配置文件，其中可以修改日志的存放目录以及日志的记录规则和格式，如下

    - API数据日志的存放路径：
   
   ```xml
   <property name="API_INFO_LOG_HOME" value="/data/logs/api-info" />
   ```
       
      - API数据日志的记录规则和格式，格式如果修改了需要在data-collect-service项目中代码中对应位置修改解析日志的代码，需要保证两者的格式一致，不然解析会出错  
       
   ```xml
   <!-- API数据日志，按照每天生成日志文件 -->
   <appender name="ApiInfoCollectLogger" class="ch.qos.logback.core.rolling.RollingFileAppender">
       <filter class="ch.qos.logback.classic.filter.LevelFilter">
           <level>INFO</level>
           <onMatch>ACCEPT</onMatch>
           <onMismatch>DENY</onMismatch>
       </filter>
       <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
           <FileNamePattern>${LOG_HOME}/ApiInfo-%d{yyyy-MM-dd}.log</FileNamePattern>
           <MaxHistory>30</MaxHistory>
       </rollingPolicy>
       <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
           <pattern>%msg|%d{yyyy-MM-dd HH:mm:ss.SSS}%n</pattern>
       </encoder>
   </appender>
   ```
   
    - App日志的存放路径：
   
   ```xml
   <property name="APP_LOG_HOME" value="/data/logs/app" />
   ```
       
      - API数据日志的记录规则和格式，格式如果修改了需要在data-collect-service项目中代码中对应位置修改解析日志的代码，需要保证两者的格式一致，不然解析会出错  
       
   ```xml
   <!-- App日志，按照每天生成日志文件 -->
   <appender name="AppLogger" class="ch.qos.logback.core.rolling.RollingFileAppender">
       <filter class="ch.qos.logback.classic.filter.LevelFilter">
           <level>ERROR</level>
           <onMatch>ACCEPT</onMatch>
           <onMismatch>DENY</onMismatch>
       </filter>
       <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
           <FileNamePattern>${APP_LOG_HOME}/app-%d{yyyy-MM-dd}.log</FileNamePattern>
           <MaxHistory>30</MaxHistory>
       </rollingPolicy>
       <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
           <pattern>%msg|%d{yyyy-MM-dd HH:mm:ss.SSS}%n</pattern>
       </encoder>
   </appender>
   ```
   

#### 2.3、service-address.properties

该配置文件可以配置哪些请求转发到哪些服务，格式为xxx.service.proxy=value，xxx的值和value要相同，这个配置的值就是API开头的部分，如/api/demo/hello/{id}，该API开头的部分是api。
所以在配置时就配置成api.service.proxy=api，其余几项如protocol/ip/port按实际情况配置

```text
api.service.proxy=api
api.service.protocol=http
api.service.address=127.0.0.1:8080
```

**注意**

该配置与application.properties中的zuul.routes.api.path=/api/**配置不同，service-address.properties是允许哪些API进入API数据收集

## 三、API数据收集

收集API数据的类需要继承BaseHandler，其中注入了记录日志的实现类

数据收集类的代码中还会使用到以下几个注解

#### 3.1、@Handler
   
   - 该注解标注在类上，表明这是处理某一批API的一个handler
   
   - Handler注解中的path参数实际上写法与springMVC的@RequestMapping写法一致，用于匹配满足这个pattern的某一批API的URL
   
   - 在项目启动时，ApiInfoCollectFilter类会扫描所有的带有@Handler的类，等到API请求进入时进行匹配从而进入相应的Handler类中进行下一步处理

#### 3.2、@Mapping
   
   - 该注解标注在Handler类中的方法上，表明它是处理某一个API的方法，用法与springMVC的@RequestMapping一样
   
   - Mapping中的method参数是枚举，分别代表GET、POST、PUT、DELETE、HEAD、PATCH方法，位于HttpMethod类中
   
   - 在项目启动时，ApiInfoCollectFilter类会扫描所有的带有@Mapping的方法，等到API请求进入时进行匹配从而进入相应的方法类中进行下一步处理

#### 3.3、@PathValue

   - 标注方法的某个参数来自于URL中的路径中的某个部分，用法类似springMVC的@PathVariable

#### 3.4、@QueryValue

   - 标注方法的某个参数来自于URL中的某个查询参数，用法类似springMVC的@RequestParam

以上功能参考了springMVC中的部分实现

例如@Handler中的path和@Mapping中的url(其实是就是Pattern)与客户端实际请求的API的URL的匹配，参考了spring的UrlPathHelper和AntPathMatcher

## 四、其他日志记录方式的实现方式

本服务默认采用日志记录的方式进行数据记录，但是可以通过实现IApiInfoCollector接口的collect方法进行其他的实现。

有其他实现后需要在BaseHandler中注入IApiInfoCollector的其他实现类，目前默认注入的是LogFileApiInfoCollectorImpl

## 五、代码示例

#### 5.1、Handler和Mapping的配置

```java

/**
 * 演示Handler
 * @author LJY
 */
@Handler(path = "/api/demo/**")
public class DemoHandler extends BaseHandler {

    @Mapping(method = HttpMethod.GET,url = "/api/demo/test/{id}")
    public void demo(HttpServletRequest request, HttpServletResponse response, @PathValue("id") String id){
        Map<String ,String> param = new HashMap<>();
        param.put("id",id);
        collector.collect(request,param);
    }

}
```

#### 5.2、IApiInfoCollector的实现
   
```java
/**
 * 用日志文件的方式收集API数据的实现类
 *
 * @author LJY
 */
@Component("log")
public class LogFileApiInfoCollectorImpl implements IApiInfoCollector {

    private static final Logger API_INFO_COLLECT_LOGGER;

    static {
        API_INFO_COLLECT_LOGGER = LoggerFactory.getLogger("ApiInfoCollectLogger");
    }

    @Override
    public void collect(HttpServletRequest request, Map<String, String> parameters) {
        String userAgent = HttpUtil.getUserAgent(request);
        String ipAddress = HttpUtil.getIpAddress(request);

        ApiInfo apiInfo = new ApiInfo.Builder()
                .userAgent(userAgent)
                .ip(ipAddress)
                .method(request.getMethod())
                .api(request.getRequestURI())
                .parameters(parameters).build();
        API_INFO_COLLECT_LOGGER.info((String) format(apiInfo));

    }

}
```

