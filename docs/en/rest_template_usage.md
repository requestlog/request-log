# RequestLog RestTemplate Configuration


[Common Configuration (Logging, Persistence, Retry, etc.)](common_usage.md)


### Maven Dependency
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-resttemplate-starter</artifactId>
    <version>${latest_stable_version}</version>
</dependency>
```

---

### Enhance RestTemplate

#### 1.Enhance through Annotation
```java
@RequestLogEnhanced 
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

#### 2.Enhance through `Enhancer`
```java
@Autowired
private RestTemplateRequestLogEnhancer enhancer;

// The return value is the input object; there's no need to deliberately use the returned reference
enhancer.enhance(restTemplate);
```

#### 3.Enhance All `RestTemplate` Beans through Configuration
```properties
request-log.rest-template.enhance-all=true
```


---

### Usage


```java
// Original request
String result = restTemplate.getForObject("url", String.class);

// Wrap the request using LogContext
String result = LogContext.log().execute(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

---

### Retry <a name="retry"></a>

```java
import io.github.requestlog.resttemplate.context.retry.RestTemplateRetryClient;

RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .successWhenResponse((requestContext) -> requestContext.getResponseCode() == 200))
        // Specify using RestTemplate as the Retry client
        .with(RestTemplateRetryClient.class, restTemplate)
        .execute();
```
