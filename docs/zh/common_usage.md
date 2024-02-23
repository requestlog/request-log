# RequestLog 通用配置

建议先看完 [快速开始](../../README-zh.md#quick_start)

<br/>

## 日志 <a name="log"></a>

#### 简单示例
```java
// 原始请求
String result = restTemplate.getForObject("url", String.class);

// 包装后请求
String result = LogContext.log().execute(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

---

#### 日志上下文

```java
import io.github.requestlog.core.repository.IRequestLogRepository;
import io.github.requestlog.core.model.RequestLog;
import io.github.requestlog.core.model.RequestRetryJob;

/**
 * 创建一个日志上下文
 * 满足条件时，保存 `日志`
 * 
 * 调用 {@link IRequestLogRepository#saveRequestLog}
 * 保存 {@link RequestLog}
 */
LogContext.log();

/**
 * 创建一个日志上下文
 * 满足条件时，保存 `日志` 与 `重试任务`
 * 
 * 调用 {@link IRequestLogRepository#saveRequestLogAndRetryJob}
 * 保存 {@link RequestLog} {@link RequestRetryJob}
 */
LogContext.retry()
        // 生成的重试任务，重试时间相关配置，后边细讲
        .retryWaitStrategy(RetryWaitStrategy.FIXED).retryInterval(60);
```

---

#### 实体

```java
package io.github.requestlog.core.model;

/**
 * 日志实体
 * 包含：
 * - 请求客户端
 * - 记录原因
 * - 请求上下文
 * - 出现的异常
 * - 响应上下文（出现异常时为空）
 */
RequestLog

/**
 * 重试任务
 * 针对 RequestLog 的重试任务
 * 包含
 * - 上一次重试时间
 * - 下一次重试时间
 * - 已执行次数（包含记录日志时 首次执行次数）
 */
RequestRetryJob

/**
 * 重试日志
 * 重试补偿的执行日志
 */
RequestRryLog
```



---

#### 记录日志条件

```java
LogContext.log()

        /**
         * 出现这些 Exception 时，依然算作成功，也就是不记录。
         * 
         * 显示指定 一个或多个 Exception.class
         * 或传入一个 Predicate<Exception>
         * 调用哪个都会覆盖另一个
         */
        .ignoreException(RuntimeException.class, IOException.class, NumberFormatException.class)
        .ignoreException(exception -> exception instanceof ClassCastException)

        /**
         * 根据 http response 相关信息判断
         * 这里可以拿到大多数 http request response 相关信息
         */
        .successWhenResponse(requestContext -> {
            return requestContext.getResponseCode() == 200;
        });
```

---

#### 记录日志条件（全局的）

条件大体相同，判断逻辑重复，不想要每次都指定

```java
import io.github.requestlog.core.support.Predicates;

/**
 * 注册 Exception 判断
 */
Predicates.registerIgnoreExceptionPredicate(exception -> exception instanceof RuntimeException);

/**
 * 注册 http response 判断
 */
Predicates.registerSuccessResponsePredicate(requestContext -> requestContext.getResponseCode() == 200);

        
/**
 * 上边两个 API 都支持传入 `RequestContextType...`，可以根据 客户端类型指定
 */
Predicates.registerIgnoreExceptionPredicate(exception -> exception instanceof RuntimeException, RequestContextType.REST_TEMPLATE, RequestContextType.APACHE_HTTP_CLIENT);
Predicates.registerSuccessResponsePredicate(requestContext -> requestContext.getResponseCode() == 200, RequestContextType.FEIGN, RequestContextType.OK_HTTP);
```


---

#### 包装代码段，执行请求

提供 4个 `execute` 方法
`executeWithExp` 是为了适配抛出检查异常。

- `LogContext#execute(Runnable)`：执行请求
- `LogContext#execute(Supplier<T>)`：执行请求并返回
- `LogContext#executeWithExp(RunnableExp<E>)`：执行请求，允许指定一个检查异常
- `LogContext#executeWithExp(SupplierExp<T,E>)`：执行请求并返回，允许指定一个检查异常


---

<br/>

## 持久化 <a name="repository"></a>


#### 内置持久化

- `Slf4jRequestLogRepository`：未自定义时，默认加载。
- `InMemoryRequestLogRepository`：基于内存，可用于单元测试。

---

#### 自定义持久化

注册一个 `IRequestLogRepository` 类型 `Bean` 即可

```java
@Component
public class MyRequestLogRepository implements IRequestLogRepository {

    /**
     * LogContext#log() 符合条件时调用
     */
    @Override
    public void saveRequestLog(RequestLog requestLog) {
    }

    /**
     * LogContext#retry() 符合条件时调用
     */
    @Override
    public void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestRetryJob) {
    }
    
}
```

---

<br/>

## 重试 <a name="retry"></a>

#### 重试任务，执行时间配置

```java
import io.github.requestlog.core.enums.RetryWaitStrategy;

LogContext.retry()
        // 重试任务执行基础间隔（默认 60秒）
        .retryInterval(60)
        // 重试任务，下次执行时间计算策略（默认固定时间）
        .retryWaitStrategy(RetryWaitStrategy.FIXED);
```

重试间隔计算策略 (60s 间隔为例):
- `FIXED`: 60s、60s、60s、60s ...
- `INCREMENT`: 60s、120s、180s、240s ...
- `FIBONACCI`: 60s、60s、120s、180s、300s ...

---


#### 手动生成 重试任务

忘记了使用 `LogContext.retry()`，只生成了 `RequestLog`

根据 `RequestLog` 生成一个 `RequestRetryJob`


```java
/**
 * `generateNewRetryJob` 为 default 方法，不需要实现
 */
IRequestLogRepository.generateNewRetryJob(RequestLog);
IRequestLogRepository.generateNewRetryJob(RequestLog, RetryWaitStrategy, retryInterval);

/**
 * - 实现 `saveRequestRetryJob` 方法
 * - 或者按照自己逻辑保存即可
 */
IRequestLogRepository.saveRequestRetryJob(generatedRetryJob);
```

---

#### 重试请求示例

```java
RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .rewritePath(retryRequestPath) // 多个 rewrite 方法，下边详细解释
        .ignoreException(NumberFormatException.class, IOException.class)
        .successWhenResponse((requestContext) -> HttpUtils.isSuccess(requestContext.getResponseCode()))
        .with(RestTemplateRetryClient.class, restTemplate) // 可以替换成其他的 RetryClient
        .execute();
```

---

#### 重试请求 覆盖方法

- `rewriteUrl`：重写整个 `URL`，会忽略其他 重写部分 `URL` 的方法
- `rewriteScheme`：重写协议
- `rewriteHost`：重写主机
- `rewritePort`：重写端口
- `rewritePath`：重写路径
- `rewriteQuery`：重写查询参数
- `rewriteFragment`：重写片段
- `rewriteHeader`：重写请求 header，有则覆盖，没有则添加
- `appendHeader`：添加请求 header
- `rewriteBody`：重写请求 body

---

#### 重试结果 判断条件

- `ignoreException`：出现下列异常时，依然算成功。默认出现异常就算失败。
- `successWhenResponse`：根据请求响应上下文自定判断。默认状态码 `2xx` 算成功。

---

#### 重试后的 操作建议

重试 `execute` 之后，会得到一个 `RetryResult`

- `succeed()`：判断当前重试是否成功，可以将 `RequestRetryJob` 进行删除或者归档
- `updateRetryJob()`：修改重试任务在重试失败后，修改重试任务用
- `generateRetryLog()`：生成一个重试日志。

```java

if (retryResult.succeed) {
    // 执行成功，删除 RequestRetryJob 或归档
        
} else {
    // 执行失败    
    
    /**
     * 修改原始构建对象，会影响如下字段
     * - `lastExecuteTimeMillis`：上一次执行时间
     * - `nextExecuteTimeMillis`：下一次执行时间
     * - `executeCount`：总执行次数（包含记录日志时的次数）
     */
    retryResult.updateRetryJob();
        
    // 如果需要获取 获取构建重试时传入的 `RequestRetryJob` 对象
    RequestRetryJob requestRetryJob = retryResult.getRetryContext().getRequestRetryJob();
}

// 无论重试结果如何，都可以生成一个重试日志，自行选择如何处理
RequestRryLog retryLog = retryResult.generateRetryLog();

```
