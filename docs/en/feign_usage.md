# RequestLog Feign Configuration


[Common Configuration (Logging, Persistence, Retry, etc.)](common_usage.md)


### Maven Dependency
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-feign-starter</artifactId>
    <version>${last stable version}</version>
</dependency>
```

---

### Enhance Feign

Feign adopts an AOP approach and is automatically enhanced after introducing the dependency.

If you want to temporarily disable it, configure `request-log.feign.disable=true`.

<br/>

**Note**: FeignClient must use service discovery. If a URL is specified, it will not be intercepted by AOP.

---

### Usage


```java
import io.github.requestlog.feign.clients.TestFeignClient; // Feign for testing, can be found in the test package

// Original Request
ResponseModel responseModel = testFeignClient.get();

// Wrap the request using LogContext
ResponseModel responseModel = LogContext.log().execute(() -> {
    return testFeignClient.get();
});
```

---

### Retry

Feign retry is not supported; you can use other HttpClient retry mechanisms

- [RestTemplate Retry](rest_template_usage.md#retry)
- [ApacheHttpClient Retry](apache_http_client_usage.md#retry)
- [OkHttp Retry](ok_http_usage.md#retry)

If rewriting request information is involved during retry, refer to [Common Usage - Retry](common_usage.md#retry)
