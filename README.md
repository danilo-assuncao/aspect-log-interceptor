# Aspect Log Interceptor

A lightweight Kotlin library that provides method logging through AOP annotations. Add comprehensive logging to your methods with minimal code changes.

## Quick Start

1. Add the dependency:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.daniloassuncao:aspect-log-interceptor:0.1.0")
    implementation("org.aspectj:aspectjweaver:1.9.21")
}
```

2. Add the `@Loggable` annotation to your methods:

```kotlin
@Service
class UserService {
    @Loggable
    fun getUser(id: Long): User {
        return User(id, "John Doe")
    }
}
```

That's it! Your methods will now be automatically logged.

## Configuration

### 1. Logging Configuration

Configure your logging framework (e.g., Logback) in `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
    
    <logger name="io.github.daniloassuncao.logging" level="DEBUG"/>
</configuration>
```

### 2. Spring Boot Configuration (Optional)

If you're using Spring Boot, add this configuration class:

```kotlin
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("io.github.daniloassuncao.logging")
class LoggingConfig
```

### 3. Logging Levels

Configure logging levels in your `application.yml` or `application.properties`:

```yaml
logging:
  level:
    root: INFO
    io.github.daniloassuncao.logging: DEBUG
```

### 4. Thread Pool Configuration (Optional)

By default, the library uses a thread pool with:
- Core pool size: 2
- Maximum pool size: 4
- Queue capacity: 1000
- Keep-alive time: 60 seconds

To customize these settings, add this to your configuration:

```kotlin
@Configuration
class LoggingThreadPoolConfig {
    @Bean
    fun loggingThreadPool(): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            corePoolSize = 4,        // Increase for more concurrent logging
            maximumPoolSize = 8,     // Maximum threads for logging
            keepAliveTime = 60L,     // Thread keep-alive time in seconds
            TimeUnit.SECONDS,
            LinkedBlockingQueue(2000) // Queue size for logging tasks
        )
    }
}
```

## Features

- 📝 Automatic method entry/exit logging
- ⏱️ Execution time tracking
- 🔒 Sensitive data masking
- ⚡ Asynchronous logging
- 🛡️ Exception handling with stack traces
- 🎛️ Configurable log levels and options

## Configuration Options

The `@Loggable` annotation supports these options:

```kotlin
@Loggable(
    level = Level.INFO,              // Log level (default: INFO)
    includeArgs = true,             // Log method arguments (default: true)
    includeResult = true,           // Log method result (default: true)
    includeExecutionTime = true,    // Log execution time (default: true)
    maskSensitiveData = false,      // Mask sensitive data (default: false)
    sensitiveFields = ["password"], // Fields to mask
    logOnException = true,          // Log exceptions (default: true)
    logExceptionStack = true        // Include stack trace (default: true)
)
```

## Examples

### Basic Usage
```kotlin
@Loggable
fun simpleMethod(param: String): String {
    return "Hello, $param!"
}
```

### With Sensitive Data Masking
```kotlin
@Loggable(
    maskSensitiveData = true,
    sensitiveFields = ["password", "creditCard"]
)
fun createUser(user: User): User {
    // Implementation
    return user
}
```

### Error Handling
```kotlin
@Loggable(
    level = Level.ERROR,
    logExceptionStack = true
)
fun validateUser(user: User): Boolean {
    if (user.name.isBlank()) {
        throw IllegalArgumentException("Name cannot be blank")
    }
    return true
}
```

## Sample Output

```
INFO UserService - Entering method: getUser with args: [123]
INFO UserService - Exiting method: getUser with result: User(id=123, name=John Doe) (execution time: 0ms)

DEBUG UserService - Entering method: createUser with args: [User(id=1, name=John Doe, password=****)]
DEBUG UserService - Exiting method: createUser with result: User(id=1, name=John Doe, password=****) (execution time: 1ms)

ERROR UserService - Exception in method: validateUser - Name cannot be blank
Stack trace:
java.lang.IllegalArgumentException: Name cannot be blank
    at UserService.validateUser(UserService.kt:42)
```

## Performance

- 🚀 Asynchronous logging using a dedicated thread pool
- ⚡ Minimal impact on method execution time
- 🔒 Thread-safe implementation
- 💾 Memory-efficient string formatting

## License

MIT License - feel free to use it in your projects! 
