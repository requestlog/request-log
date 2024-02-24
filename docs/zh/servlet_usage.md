# RequestLog Servlet 详细配置

跟本项目其他对外请求的组件不同，Servlet 是服务提供方。

[通用配置(日志、持久化、重试等)](common_usage.md)



### Maven 依赖
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-servlet-starter</artifactId>
    <version>${最新稳定版本}</version>
</dependency>
```


---


### 增强 Controller

使用 `@ReqLog` 修饰接口

```java
import io.github.requestlog.servlet.annotation.ReqLog;

@RestController
public class SomeController {
    
    @ReqLog(whenException = {Exception.class, IOException.class},
            retry = true, retryInterval = 10, retryWaitStrategy = RetryWaitStrategy.FIXED, 
            maxExecuteCount = 10)
    @GetMapping("/some-path")
    public String get() {
        return "some result";
    }
    
}

```
- `whenException`：出现哪些 `Exception` 时才会记录
- `retry`：是否开启重试
- `retryInterval`：重试间隔
- `retryWaitStrategy`：重试间隔计算策略
- `maxExecuteCount`：开启重试后，期望的最大执行次数 (包含首次执行)


如果想要临时关闭功能，配置 `request-log.servlet.disable=true`


---

### 重试

- [RestTemplate 重试](rest_template_usage.md#retry)
- [ApacheHttpClient 重试](apache_http_client_usage.md#retry)
- [OkHttp 重试](ok_http_usage.md#retry)

重试时如果涉及到 重写请求信息，参考 [通用配置-重试](common_usage.md#retry)