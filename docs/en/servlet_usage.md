# RequestLog Servlet Configuration

Unlike other components that handle external requests in this project, Servlet acts as the service provider.

[Common Configuration (Logging, Persistence, Retry, etc.)](common_usage.md)



### Maven Dependency
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-servlet-starter</artifactId>
    <version>${latest_stable_version}</version>
</dependency>
```


---


### Enhance Controller

Use `@ReqLog` to decorate the endpoint.

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
- `whenException`：Specifies which `Exception` to record.
- `retry`：Enable retry.
- `retryInterval`：Retry interval.
- `retryWaitStrategy`：Retry interval calculation strategy.
- `maxExecuteCount`：Maximum expected execution count after enabling retry (including the first execution).


To temporarily disable the functionality, configure `request-log.servlet.disable=true`



---

### Retry

- [RestTemplate Retry](rest_template_usage.md#retry)
- [ApacheHttpClient Retry](apache_http_client_usage.md#retry)
- [OkHttp Retry](ok_http_usage.md#retry)

If rewriting request information is involved during retry, refer to [Common Usage - Retry](common_usage.md#retry)
