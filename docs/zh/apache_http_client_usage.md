# RequestLog ApacheHttpClient 详细配置


[通用配置(日志、持久化、重试等)](common_usage.md)


### Maven 依赖
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-apache-http-client-starter</artifactId>
    <version>${最新稳定版本}</version>
</dependency>
```

---

### 增强 HttpClient

#### 1.通过注解增强
```java
@RequestLogEnhanced
@Bean
public HttpClient httpClient() {
    return HttpClients.createDefault();
}
```

#### 2.通过 `Enhancer` 增强
```java
@Autowired
private ApacheHttpClientRequestLogEnhancer enhancer;

// 使用返回的对象
HttpClient enhancedHttpClient = enhancer.enhance(httpClient);
```

#### 3.通过配置，增强所有 `HttpClient` Bean
```properties
request-log.apache-http-client.enhance-all=true
```


---

### 使用


```java
import org.apache.http.client.HttpClient;
        
/**
 * 原始请求
 * {@link HttpClient#execute} 声明检查异常 `IOException`, `ClientProtocolException`
 */
HttpResponse response = httpClient.execute(new HttpGet("url"));

/**
 * 包装后请求
 * 因为有声明检查异常，使用 `executeWithExp` 代替 `execute`
 */
HttpResponse response = LogContext.log().executeWithExp(() -> {
    return httpClient.execute(new HttpGet("url"));
});
```

---

### 重试 <a name="retry"></a>

```java
import io.github.requestlog.apachehttpclient.context.retry.ApacheHttpClientRetryClient;

RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .successWhenResponse((requestContext) -> requestContext.getResponseCode() == 200))
        // 指定使用 HttpClient 作为 Retry 客户端
        .with(ApacheHttpClientRetryClient.class, httpClient)
        .execute();
```
