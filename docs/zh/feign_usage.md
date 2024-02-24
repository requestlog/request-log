# RequestLog Feign 详细配置


[通用配置(日志、持久化、重试等)](common_usage.md)


### Maven 依赖
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-feign-starter</artifactId>
    <version>${最新稳定版本}</version>
</dependency>
```

---

### 增强 Feign

Feign 采用 AOP 方式，引入依赖后自动增强

如果想要临时关闭的话，配置 `request-log.ok-http.disable=true`

<br/>

注意事项：FeignClient 需要使用服务发现方式，如果指定 url 方式，则失效


---

### 使用


```java
import io.github.requestlog.feign.clients.TestFeignClient; // 测试用的 feign，可以在 test 中找到

// 原始请求
ResponseModel responseModel = testFeignClient.get();

// 使用 LogContext 包装请求
ResponseModel responseModel = LogContext.log().execute(() -> {
    return testFeignClient.get();
});
```

---

### 重试

不支持使用 feign 重试，可以采用其他 HttpClient 重试

- [RestTemplate 重试](rest_template_usage.md#retry)
- [ApacheHttpClient 重试](apache_http_client_usage.md#retry)
- [OkHttp 重试](ok_http_usage.md#retry)

重试时如果涉及到 重写请求信息，参考 [通用配置-重试](common_usage.md#retry)
