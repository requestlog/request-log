# RequestLog

[中文文档](README-zh.md)

<br/>

## Introduction

**RequestLog** is an HTTP request logging and retry compensation tool based on Spring Boot.
<br/>
**Features**: Low intrusion and easy integration.

---

<br/>

## Quick Start <a name="quick_start"></a>

Taking **RestTemplate** as an example.

#### Maven Dependency
```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-resttemplate-starter</artifactId>
    <version>${latest_stable_version}</version>
</dependency>
```

---

### Enhance RestTemplate

Enhance the RestTemplate client using annotations.
```java
@RequestLogEnhanced 
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

---

#### Define Repository for Persistence

```java
@Component
public class MyRequestLogRepository implements IRequestLogRepository {
    @Override
    public void saveRequestLog(RequestLog requestLog) {
        // save request log
    }
    @Override
    public void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestRetryJob) {
        // save request log and retry job
    }
}
```

---

#### Wrap Request Code

```java
// Original request code
String result = restTemplate.getForObject("url", String.class);

// Wrap the request using LogContext
String wrappedResult = LogContext.log().execute(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

For the wrapped request, when an exception occurs or the response status code is not `2xx`, the corresponding save method of the custom `IRequestLogRepository` will be invoked.


---

<br/>

## Detailed Documentation

- [Common Configuration](/docs/en/common_usage.md)
- [RestTemplate Usage](/docs/en/rest_template_usage.md)
- [ApacheHttpClient Usage](/docs/en/apache_http_client_usage.md)
- [OKHttp Usage](/docs/en/ok_http_usage.md)
- [Feign Usage](/docs/en/feign_usage.md)
- [Servlet Usage](/docs/en/servlet_usage.md)