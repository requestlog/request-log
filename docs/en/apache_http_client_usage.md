# RequestLog ApacheHttpClient Configuration


[Common Configuration (Logging, Persistence, Retry, etc.)](common_usage.md)


### Maven Dependency
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-apache-http-client-starter</artifactId>
    <version>${last stable version}</version>
</dependency>
```

---

### Enhance HttpClient

#### 1.Enhance through Annotation
```java
@RequestLogEnhanced
@Bean
public HttpClient httpClient() {
    return HttpClients.createDefault();
}
```

#### 2.Enhance through `Enhancer`
```java
@Autowired
private ApacheHttpClientRequestLogEnhancer enhancer;

// Use the returned object
HttpClient enhancedHttpClient = enhancer.enhance(httpClient);
```

#### 3.Enhance all `HttpClient` Beans through Configuration
```properties
request-log.apache-http-client.enhance-all=true
```


---

### Usage


```java
import org.apache.http.client.HttpClient;
        
/**
 * Original Request
 *  {@link HttpClient#execute} declares checked exceptions `IOException`, `ClientProtocolException`
 */
HttpResponse response = httpClient.execute(new HttpGet("url"));

/**
 * Wrap the request using LogContext
 * Since checked exceptions are declared, use `executeWithExp` instead of `execute`
 */
HttpResponse response = LogContext.log().executeWithExp(() -> {
    return httpClient.execute(new HttpGet("url"));
});
```

---

### Retry <a name="retry"></a>

```java
import io.github.requestlog.apachehttpclient.context.retry.ApacheHttpClientRetryClient;

RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .successWhenResponse((requestContext) -> requestContext.getResponseCode() == 200))
        // Specify using HttpClient as the Retry client
        .with(ApacheHttpClientRetryClient.class, httpClient)
        .execute();
```
