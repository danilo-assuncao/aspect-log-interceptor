package io.github.daniloassuncao.logging.annotation

import org.slf4j.event.Level

/**
 * Annotation to enable logging for a method.
 * When applied to a method, it will log:
 * - Method entry with parameters
 * - Method exit with return value
 * - Method execution time
 *
 * @property level The log level to use for logging (default: INFO)
 * @property includeArgs Whether to include method arguments in logs (default: true)
 * @property includeResult Whether to include method result in logs (default: true)
 * @property includeExecutionTime Whether to include execution time in logs (default: true)
 * @property maskSensitiveData Whether to mask sensitive data in arguments (default: false)
 * @property sensitiveFields List of field names to mask if they contain sensitive data (default: empty)
 * @property logOnException Whether to log when an exception occurs (default: true)
 * @property logExceptionStack Whether to include stack trace in exception logs (default: false)
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable(
    val level: Level = Level.INFO,
    val includeArgs: Boolean = true,
    val includeResult: Boolean = true,
    val includeExecutionTime: Boolean = true,
    val maskSensitiveData: Boolean = false,
    val sensitiveFields: Array<String> = [],
    val logOnException: Boolean = true,
    val logExceptionStack: Boolean = false
)
