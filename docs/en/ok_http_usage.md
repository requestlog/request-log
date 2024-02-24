# RequestLog OkHttp Configuration


[Common Configuration (Logging, Persistence, Retry, etc.)](common_usage.md)


### Maven Dependency
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-ok-http-starter</artifactId>
    <version>${latest_stable_version}</version>
</dependency>
```

---

### Enhance OkHttpClient

#### 1.Enhance through Annotation
```java
@RequestLogEnhanced
@Bean
public OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder().build();
}
```

#### 2.Enhance through `Enhancer`
```java
@Autowired
private OkHttpRequestLogEnhancer enhancer;

// Use the returned object
OkHttpClient enhancedOkHttpClient = enhancer.enhance(okHttpClient);
```

#### 3.Enhance All `OkHttpClient` Beans through Configuration
```properties
request-log.ok-http.enhance-all=true
```


---

### Usage


```java
Request request = new Request.Builder().url("url").build();

/**
 * Original request
 * {@link okhttp3.Call#execute} declares checked exception `IOException`
 */
Response response = okHttpClient.newCall(request).execute()

/**
 * Wrap the request using LogContext
 * Since checked exceptions are declared, use `executeWithExp` instead of `execute`
 */
Response result = LogContext.log().executeWithExp(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

---

### Retry <a name="retry"></a>

```java
import io.github.requestlog.okhttp.context.retry.OkHttpRetryClient;

RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .successWhenResponse((requestContext) -> requestContext.getResponseCode() == 200))
        // Specify using OkHttpClient as the Retry client
        .with(OkHttpRetryClient.class, okHttpClient)
        .execute();
```
