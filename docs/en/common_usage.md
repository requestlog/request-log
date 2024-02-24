# RequestLog Configuration

First go through [Quick Start](../../README.md#quick_start)

<br/>

## Logging <a name="log"></a>

#### Simple Example
```java
// Original request
String result = restTemplate.getForObject("url", String.class);

// Wrap the request using LogContext
String result = LogContext.log().execute(() -> {
    return restTemplate.getForObject("url", String.class);
});
```

---

#### Logging Context

```java
import io.github.requestlog.core.repository.IRequestLogRepository;
import io.github.requestlog.core.model.RequestLog;
import io.github.requestlog.core.model.RequestRetryJob;

/**
 * Create a logging context
 * Save the `log` when conditions are met.
 * 
 * When saving, call {@link IRequestLogRepository#saveRequestLog}
 * Save {@link RequestLog}
 */
LogContext.log();

/**
 * Create a logging context
 * Save the `log` and `retry job` when conditions are met.
 *
 * When saving, call {@link IRequestLogRepository#saveRequestLogAndRetryJob}
 * Save {@link RequestLog} and {@link RequestRetryJob}
 */
LogContext.retry()
        // Retry task generation, retry time-related configuration explained later
        .retryWaitStrategy(RetryWaitStrategy.FIXED).retryInterval(60);
```

---

#### Entities

```java
package io.github.requestlog.core.model;

/**
 * Log entity
 * Contains:
 * - Request client
 * - Recording reason
 * - Request context
 * - Occurred exceptions
 * - Response context (empty when an exception occurs)
 */
RequestLog

/**
 * Retry task for RequestLog
 * Contains
 * - Last execution time
 * - Next execution time
 * - Number of executions performed (includes the first execution count when logging is recorded)
 * - Expected max execution count (includes the first execution count when logging is recorded)
 */
RequestRetryJob

/**
 * Retry log
 * Execution log for retry compensation
 */
RequestRryLog
```

---

#### Logging Conditions

How to define failed, which exceptions to ignore, and the expected response body.

```java
LogContext.log()

        /**
         * Even when these exceptions occur, they are still considered successful and not recorded.
         *
         * Explicitly specify one or more Exception.class
         * or pass a Predicate<Exception>
         * Calling either will override the other
         *
         * Default: Any exception is considered a failure
         */
        .ignoreException(RuntimeException.class, IOException.class, NumberFormatException.class)
        .ignoreException(exception -> exception instanceof ClassCastException)

        /**
         * Determine based on http response-related information
         * Here you can get most of the http request-response related information
         *
         * Default: http code == 2xx
         */
        .successWhenResponse(requestContext -> {
            return requestContext.getResponseCode() == 200;
        });
```

---

#### Global Logging Conditions

Conditions are largely the same, judging logic is repetitive, don't want to specify every time

```java
import io.github.requestlog.core.support.Predicates;

/**
 * Register Exception judgment
 */
Predicates.registerIgnoreExceptionPredicate(exception -> exception instanceof RuntimeException);

/**
 * Register http response judgment
 */
Predicates.registerSuccessResponsePredicate(requestContext -> requestContext.getResponseCode() == 200);


/**
 * Both of the above APIs support passing `RequestContextType...`, which can be specified based on the client type
 */
Predicates.registerIgnoreExceptionPredicate(exception -> exception instanceof RuntimeException, RequestContextType.REST_TEMPLATE, RequestContextType.APACHE_HTTP_CLIENT);
Predicates.registerSuccessResponsePredicate(requestContext -> requestContext.getResponseCode() == 200, RequestContextType.FEIGN, RequestContextType.OK_HTTP);
```


---

#### Wrapping Code Blocks, Executing Requests

Provides 4个 `execute` methods
`executeWithExp` is for adapting checked exceptions.。

- `LogContext#execute(Runnable)`：Execute the request
- `LogContext#execute(Supplier<T>)`：Execute the request and return
- `LogContext#executeWithExp(RunnableExp<E>)`：Execute the request, allowing specification of a checked exception
- `LogContext#executeWithExp(SupplierExp<T,E>)`：Execute the request and return, allowing specification of a checked exception


---


#### Custom Log Attributes

Custom attributes, such as bizCode, tenantId, etc.

```java
LogContext.log()
        .addAttribute("bizCode", "userLoginCount")
        .addAttribute("tenantId", 1000L);

// The result will be reflected here. If `addAttribute` has not been called, this map will be null
io.github.requestlog.core.model.RequestLog#attributeMap;
```

---

<br/>

## Persistence <a name="repository"></a>


#### Built-in Persistence

- `Slf4jRequestLogRepository`：Loaded by default if not customized.
- `InMemoryRequestLogRepository`：In-memory based, useful for unit testing.

---

#### Custom Persistence

Register an IRequestLogRepository type Bean and that's it.

```java
@Component
public class MyRequestLogRepository implements IRequestLogRepository {

    /**
     * Called when LogContext#log() meets the conditions
     */
    @Override
    public void saveRequestLog(RequestLog requestLog) {
    }

    /**
     * Called when LogContext#retry() meets the conditions
     */
    @Override
    public void saveRequestLogAndRetryJob(RequestLog requestLog, RequestRetryJob requestRetryJob) {
    }
    
}
```

---

<br/>

## Retry <a name="retry"></a>

#### Retry Execution Time Configuration

```java
import io.github.requestlog.core.enums.RetryWaitStrategy;

LogContext.retry()
        // Basic interval for retry task execution (default 60 seconds)
        .retryInterval(60)
        // Next execution time calculation strategy (default fixed interval)
        .retryWaitStrategy(RetryWaitStrategy.FIXED)
        // Maximum execution count, includes the first execution count when logging is recorded (default 3 times)
        .maxExecuteCount(3);
```

Retry interval calculation strategy (Example with 60s interval):
- `FIXED`: 60s、60s、60s、60s ...
- `INCREMENT`: 60s、120s、180s、240s ...
- `FIBONACCI`: 60s、60s、120s、180s、300s ...

---


#### Manually Generate Retry Tasks

Applicable when forgetting to use LogContext.retry(), only generated `RequestLog`

Generate a `RequestRetryJob` based on `RequestLog`



```java
/**
 * `generateNewRetryJob` is a default method, no need to implement
 */
IRequestLogRepository.generateNewRetryJob(RequestLog);
IRequestLogRepository.generateNewRetryJob(RequestLog, RetryWaitStrategy, retryInterval, maxExecuteCount);

/**
 * - Implement the `saveRequestRetryJob` method
 * - Or save according to your own logic
 */
IRequestLogRepository.saveRequestRetryJob(generatedRetryJob);
```

---

#### Retry Request Example

```java
RetryResult retryResult = RetryContext.create(RequestLogObj, @Nullable RequestRetryJobObj)
        .rewritePath(retryRequestPath) // Multiple rewrite methods, explained below
        .ignoreException(NumberFormatException.class, IOException.class)
        .successWhenResponse((requestContext) -> HttpUtils.isSuccess(requestContext.getResponseCode()))
        .with(RestTemplateRetryClient.class, restTemplate) // Can be replaced with other RetryClients
        .execute();
```

---

#### Retry Request Override Methods

- `rewriteUrl`：Rewrite the entire `URL`, will ignore other `URL` rewrite methods
- `rewriteScheme`：Rewrite the protocol
- `rewriteHost`：Rewrite the host
- `rewritePort`：Rewrite the port
- `rewritePath`：Rewrite the path
- `rewriteQuery`：Rewrite query parameters
- `rewriteFragment`：Rewrite the fragment
- `rewriteHeader`：Rewrite request headers, overwrite if present, add if not
- `appendHeader`：Append request headers
- `rewriteBody`：Rewrite request body

---

#### Retry Result Conditions

- `ignoreException`：Considered successful even if the following exceptions occur. Default is failure if any exception occurs.
- `successWhenResponse`：Custom judgment based on request response context. Default is success if status code is 2xx.

---

#### Actions After Retry

After retrying execute, you will get a RetryResult

- `succeed()`: Determine if the current retry is successful; after success, you can delete or archive the RequestRetryJob.
- `shouldContinue()`: Check if the current retry task should continue, based on whether the current execution is successful and whether the expected maximum execution count is reached.
- `updateRetryJob()`: Modifies the retry task after a retry failure, use this operation to update the original object.
- `generateRetryLog()`：Generate a retry log

```java

if (retryResult.succeed) {
    // Execution succeeded, delete RequestRetryJob or archive
        
} else {
    // Execution failed
    
    // Should continue retrying or not
    if (retryResult.shouldContinue()) {

        /**
         * Modify the original build object, will affect the following fields
         * - `lastExecuteTimeMillis`: Last execution time
         * - `nextExecuteTimeMillis`: Next execution time
         * - `executeCount`: Total number of executions (including the count when logging is recorded)
         * 
         * returns the original `RequestRetryJob` object
         */
        RequestRetryJob retryJob = retryResult.updateRetryJob();

        // You can also obtain the original RequestRetryJob like this.
        RequestRetryJob requestRetryJob = retryResult.getRetryContext().getRequestRetryJob();

    } else {
        // Should not continue retrying, choose whether to delete the task or increase the retry count
    }
    
}

// Regardless of the retry result, you can generate a retry log and choose how to handle it
RequestRryLog retryLog = retryResult.generateRetryLog();

```
