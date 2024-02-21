# RequestLog

[English](README.md)

## 简介

**RequestLog** 是一个基于 spring-boot 的 Http 请求日志记录 与 重试补偿工具。
<br/>
**特点**：侵入小、集成简单。

## 快速开始

以 `RestTemplate` 为例

#### Maven 依赖

```xml
<dependency>
    <groupId>io.github.requestlog</groupId>
    <artifactId>request-log-resttemplate-starter</artifactId>
    <version>${最新稳定版本}</version>
</dependency>
```

---

#### 增强 RestTemplate

使用注解增强 `RestTemplate` 客户端
```java
@RequestLogEnhanced 
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

---

#### 定义 Repository 用以持久化

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

#### 包装请求代码

```java
// 原始请求
String result = restTemplate.getForObject("url", String.class);

// 包装后请求
String wrappedResult = LogContext.log().execute(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

包装后的请求，在出现异常 或 响应状态码非 `2xx` 时，自定义 `IRequestLogRepository` 的对应 save 方法会被调用。
