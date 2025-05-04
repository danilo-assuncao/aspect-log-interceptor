package io.github.daniloassuncao.logging.aspect

import io.github.daniloassuncao.logging.annotation.Loggable
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import jakarta.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.lang.reflect.Method

/**
 * Aspect that provides method logging functionality using AOP.
 * This aspect intercepts methods annotated with @Loggable and provides:
 * - Method entry/exit logging
 * - Argument and result logging
 * - Execution time tracking
 * - Sensitive data masking
 * - Exception logging
 */
@Aspect
class LoggingAspect {
    private val threadCounter = AtomicInteger(0)
    private val queue = LinkedBlockingQueue<Runnable>(1000)
    private val executor = ThreadPoolExecutor(
        1, 1,
        60L, TimeUnit.SECONDS,
        queue,
        { r -> Thread(r, "logging-aspect-${threadCounter.incrementAndGet()}").apply {
            isDaemon = true
            priority = Thread.MIN_PRIORITY
        }},
        ThreadPoolExecutor.CallerRunsPolicy()
    )

    // Cache for method signatures and annotations to avoid repeated reflection
    private val methodCache = ConcurrentHashMap<Method, MethodMetadata>()

    // Thread-local StringBuilder for string formatting
    private val stringBuilder = ThreadLocal.withInitial { StringBuilder(256) }

    data class MethodMetadata(
        val loggable: Loggable,
        val logger: org.slf4j.Logger,
        val methodName: String
    )

    init {
        executor.prestartCoreThread()
    }

    /**
     * Shuts down the logging executor service gracefully.
     * This method is called when the application context is closed.
     */
    @PreDestroy
    fun shutdown() {
        try {
            executor.shutdown()
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: Exception) {
            // Silently ignore shutdown errors
        }
    }

    /**
     * Main aspect method that intercepts methods annotated with @Loggable.
     * Handles method execution logging, including entry, exit, and exception cases.
     *
     * @param joinPoint The join point representing the intercepted method
     * @return The result of the intercepted method
     */
    @Around("@annotation(io.github.daniloassuncao.logging.annotation.Loggable)")
    fun logMethodExecution(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = try {
            joinPoint.signature as? MethodSignature
        } catch (e: Exception) {
            return joinPoint.proceed()
        } ?: return joinPoint.proceed()

        val method = try {
            methodSignature.method
        } catch (e: Exception) {
            return joinPoint.proceed()
        }

        // Get or compute method metadata
        val metadata = methodCache.computeIfAbsent(method) { m ->
            val loggable = try {
                m.getAnnotation(Loggable::class.java)
                    ?: m.kotlinFunction?.findAnnotation()
            } catch (e: Exception) {
                return@computeIfAbsent MethodMetadata(
                    Loggable(),
                    LoggerFactory.getLogger(m.declaringClass),
                    m.name
                )
            } ?: return@computeIfAbsent MethodMetadata(
                Loggable(),
                LoggerFactory.getLogger(m.declaringClass),
                m.name
            )

            val logger = try {
                LoggerFactory.getLogger(m.declaringClass)
            } catch (e: Exception) {
                LoggerFactory.getLogger(m.declaringClass)
            }

            val methodName = try {
                m.name
            } catch (e: Exception) {
                "unknown"
            }

            MethodMetadata(loggable, logger, methodName)
        }

        val startTime = System.currentTimeMillis()

        // Log method entry asynchronously
        if (metadata.loggable.includeArgs) {
            submitLoggingTask {
                try {
                    val formattedArgs = if (metadata.loggable.maskSensitiveData) {
                        formatArgsWithMasking(joinPoint.args, metadata.loggable.sensitiveFields)
                    } else {
                        formatArgs(joinPoint.args)
                    }
                    log(metadata.logger, metadata.loggable.level, "Entering method: ${metadata.methodName} with args: $formattedArgs")
                } catch (e: Exception) {
                    // Silently ignore logging errors
                }
            }
        } else {
            submitLoggingTask {
                try {
                    log(metadata.logger, metadata.loggable.level, "Entering method: ${metadata.methodName}")
                } catch (e: Exception) {
                    // Silently ignore logging errors
                }
            }
        }

        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - startTime

            // Log method exit asynchronously
            submitLoggingTask {
                try {
                    val exitMessage = buildExitMessage(
                        metadata.methodName,
                        result,
                        executionTime,
                        metadata.loggable
                    )
                    log(metadata.logger, metadata.loggable.level, exitMessage)
                } catch (e: Exception) {
                    // Silently ignore logging errors
                }
            }

            return result
        } catch (e: Exception) {
            if (metadata.loggable.logOnException) {
                submitLoggingTask {
                    try {
                        val errorMessage = buildErrorMessage(metadata.methodName, e, metadata.loggable.logExceptionStack)
                        log(metadata.logger, Level.ERROR, errorMessage)
                    } catch (loggingError: Exception) {
                        // Silently ignore logging errors
                    }
                }
            }
            throw e
        }
    }

    /**
     * Builds the exit message for a method, including result and execution time if configured.
     *
     * @param methodName The name of the method
     * @param result The result of the method execution
     * @param executionTime The execution time in milliseconds
     * @param loggable The Loggable annotation configuration
     * @return The formatted exit message
     */
    private fun buildExitMessage(
        methodName: String,
        result: Any?,
        executionTime: Long,
        loggable: Loggable
    ): String {
        val sb = stringBuilder.get()
        sb.setLength(0) // Clear the StringBuilder
        
        sb.append("Exiting method: ").append(methodName)
        
        if (loggable.includeResult) {
            val formattedResult = if (loggable.maskSensitiveData) {
                formatResultWithMasking(result, loggable.sensitiveFields)
            } else {
                formatResult(result)
            }
            sb.append(" with result: ").append(formattedResult)
        }
        
        if (loggable.includeExecutionTime) {
            sb.append(" (execution time: ").append(executionTime).append("ms)")
        }
        
        return sb.toString()
    }

    /**
     * Builds the error message for an exception, including stack trace if configured.
     *
     * @param methodName The name of the method
     * @param e The exception that occurred
     * @param includeStack Whether to include the stack trace
     * @return The formatted error message
     */
    private fun buildErrorMessage(
        methodName: String,
        e: Exception,
        includeStack: Boolean
    ): String {
        val sb = stringBuilder.get()
        sb.setLength(0) // Clear the StringBuilder
        
        sb.append("Exception in method: ").append(methodName)
            .append(" - ").append(e.message)
        
        if (includeStack) {
            sb.append("\nStack trace:\n")
            e.stackTraceToString().lines().take(5).forEach { 
                sb.append(it).append("\n")
            }
        }
        
        return sb.toString()
    }

    /**
     * Submits a logging task to the executor service.
     *
     * @param task The logging task to execute
     */
    private fun submitLoggingTask(task: () -> Unit) {
        try {
            if (!executor.isShutdown) {
                executor.submit(task)
            }
        } catch (e: Exception) {
            // Silently ignore submission errors
        }
    }

    /**
     * Logs a message at the specified level using the provided logger.
     *
     * @param logger The logger to use
     * @param level The log level
     * @param message The message to log
     */
    private fun log(logger: org.slf4j.Logger, level: Level, message: String) {
        try {
            when (level) {
                Level.TRACE -> logger.trace(message)
                Level.DEBUG -> logger.debug(message)
                Level.INFO -> logger.info(message)
                Level.WARN -> logger.warn(message)
                Level.ERROR -> logger.error(message)
            }
        } catch (e: Exception) {
            // Silently ignore logging errors
        }
    }

    /**
     * Formats method arguments into a string representation.
     *
     * @param args The method arguments to format
     * @return The formatted arguments string
     */
    private fun formatArgs(args: Array<Any?>): String {
        if (args.isEmpty()) return "[]"
        
        val sb = stringBuilder.get()
        sb.setLength(0) // Clear the StringBuilder
        
        sb.append('[')
        for (i in args.indices) {
            if (i > 0) sb.append(", ")
            sb.append(formatValue(args[i]))
        }
        sb.append(']')
        
        return sb.toString()
    }

    /**
     * Formats a method result into a string representation.
     *
     * @param result The method result to format
     * @return The formatted result string
     */
    private fun formatResult(result: Any?): String {
        return formatValue(result)
    }

    /**
     * Formats a value into a string representation.
     *
     * @param value The value to format
     * @return The formatted value string
     */
    private fun formatValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is Array<*> -> value.contentToString()
            is Collection<*> -> value.toString()
            else -> value.toString()
        }
    }

    /**
     * Formats method arguments with sensitive data masking.
     *
     * @param args The method arguments to format
     * @param sensitiveFields The list of sensitive field names to mask
     * @return The formatted arguments string with masked sensitive data
     */
    private fun formatArgsWithMasking(args: Array<Any?>, sensitiveFields: Array<String>): String {
        if (args.isEmpty()) return "[]"
        
        val sb = stringBuilder.get()
        sb.setLength(0) // Clear the StringBuilder
        
        sb.append('[')
        for (i in args.indices) {
            if (i > 0) sb.append(", ")
            sb.append(formatValueWithMasking(args[i], sensitiveFields))
        }
        sb.append(']')
        
        return sb.toString()
    }

    /**
     * Formats a method result with sensitive data masking.
     *
     * @param result The method result to format
     * @param sensitiveFields The list of sensitive field names to mask
     * @return The formatted result string with masked sensitive data
     */
    private fun formatResultWithMasking(result: Any?, sensitiveFields: Array<String>): String {
        return formatValueWithMasking(result, sensitiveFields)
    }

    /**
     * Formats a value with sensitive data masking.
     *
     * @param value The value to format
     * @param sensitiveFields The list of sensitive field names to mask
     * @return The formatted value string with masked sensitive data
     */
    private fun formatValueWithMasking(value: Any?, sensitiveFields: Array<String>): String {
        if (value == null) return "null"
        
        // For simple types, just return the string representation
        if (value is String || value is Number || value is Boolean) {
            return value.toString()
        }
        
        // For complex objects, mask sensitive fields
        return try {
            val sb = stringBuilder.get()
            
            // Clear the StringBuilder
            sb.setLength(0)
            
            sb.append(value.javaClass.simpleName).append('(')
            var first = true
            
            value.javaClass.declaredFields.forEach { field ->
                if (!first) sb.append(", ")
                first = false
                
                field.isAccessible = true
                val fieldValue = field.get(value)
                
                if (sensitiveFields.contains(field.name)) {
                    sb.append(field.name).append("=****")
                } else {
                    sb.append(field.name).append('=').append(formatValue(fieldValue))
                }
            }
            
            sb.append(')')
            sb.toString()
        } catch (e: Exception) {
            "[Error formatting with masking]"
        }
    }
} 