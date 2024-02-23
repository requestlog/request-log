# RequestLog OkHttp 详细配置


[通用配置(日志、持久化、重试等)](common_usage.md)


### Maven 依赖
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-ok-http-starter</artifactId>
    <version>${最新稳定版本}</version>
</dependency>
```

---

### 增强 OkHttpClient

#### 1.通过注解增强
```java
@RequestLogEnhanced
@Bean
public OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder().build();
}
```

#### 2.通过 `Enhancer` 增强
```java
@Autowired
private OkHttpRequestLogEnhancer enhancer;

// 使用返回的对象
OkHttpClient enhancedOkHttpClient = enhancer.enhance(okHttpClient);
```

#### 3.通过配置，增强所有 `OkHttpClient` Bean
```properties
request-log.ok-http.enhance-all=true
```


---

### 使用


```java
Request request = new Request.Builder().url("url").build();

/**
 * 原始请求
 * {@link okhttp3.Call#execute} 声明检查异常 `IOException`
 */
Response response = okHttpClient.newCall(request).execute()

/**
 * 包装后请求
 * 因为有声明检查异常，使用 `executeWithExp` 代替 `execute`
 */
Response result = LogContext.log().executeWithExp(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

---

### 重试 <a name="retry"></a>

```java
import io.github.requestlog.okhttp.context.retry.OkHttpRetryClient;

RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .successWhenResponse((requestContext) -> requestContext.getResponseCode() == 200))
        // 指定使用 OkHttpClient 作为 Retry 客户端
        .with(OkHttpRetryClient.class, okHttpClient)
        .execute();
```
