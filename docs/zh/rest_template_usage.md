# RequestLog RestTemplate 详细配置


[通用配置(日志、持久化、重试等)](common_usage.md)


### Maven 依赖
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-resttemplate-starter</artifactId>
    <version>${最新稳定版本}</version>
</dependency>
```

---

### 增强 RestTemplate

#### 1.通过注解增强
```java
@RequestLogEnhanced 
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

#### 2.通过 `Enhancer` 增强
```java
@Autowired
private ApacheHttpClientRequestLogEnhancer enhancer;

enhancer.enhance(restTemplate); // 返回值为入参对象，不需要刻意使用返回的引用
```

#### 3.通过配置，增强所有 `RestTemplate` Bean
```properties
request-log.rest-template.enhance-all=true
```


---

### 使用


```java
// 原始请求
String result = restTemplate.getForObject("url", String.class);

// 包装后请求
String result = LogContext.log().execute(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

---

### 重试

```java
RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        // 自定义判断 response 是否成功
        .successWhenResponse((requestContext) -> requestContext.getResponseCode() == 200))
        // 指定使用 RestTemplate 作为 Retry 客户端
        .with(RestTemplateRetryClient.class, restTemplate)
        .execute();
```

详细的重试配置 [重试](common_usage.md#retry)