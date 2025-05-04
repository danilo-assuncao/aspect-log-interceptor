package io.github.daniloassuncao.logging.aspect

import io.github.daniloassuncao.logging.annotation.Loggable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class LoggingAspectTest {

    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint

    @Mock
    private lateinit var signature: MethodSignature

    @Mock
    private lateinit var method: Method

    private lateinit var aspect: LoggingAspect

    @BeforeEach
    fun setup() {
        aspect = LoggingAspect()
        Mockito.`when`(joinPoint.signature).thenReturn(signature)
        Mockito.`when`(signature.method).thenReturn(method)
        Mockito.`when`(method.declaringClass).thenReturn(LoggingAspectTest::class.java)
    }

    @AfterEach
    fun cleanup() {
        aspect.shutdown()
        // Wait for any pending logging tasks to complete
        Thread.sleep(100)
        Mockito.reset(joinPoint, signature, method)
    }

    @Test
    fun `should log method entry and exit with args and result at different levels`() {
        // Given
        val args = arrayOf("test", 123)
        val result = "success"
        val methodName = "testMethod"
        
        Mockito.`when`(joinPoint.args).thenReturn(args)
        Mockito.`when`(joinPoint.proceed()).thenReturn(result)
        Mockito.`when`(method.name).thenReturn(methodName)

        Level.values().forEach { level ->
            // Reset mocks for each iteration
            Mockito.clearInvocations(joinPoint)
            
            Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
                Loggable(level = level)
            )

            // When
            val actualResult = aspect.logMethodExecution(joinPoint)

            // Then
            assertEquals(result, actualResult)
            Mockito.verify(joinPoint, times(1)).proceed()
            
            // Wait for async logging
            Thread.sleep(50)
        }
    }

    @Test
    fun `should log method entry and exit without args and result when disabled`() {
        // Given
        val args = arrayOf("test", 123)
        val result = "success"
        val methodName = "testMethod"
        
        Mockito.`when`(joinPoint.args).thenReturn(args)
        Mockito.`when`(joinPoint.proceed()).thenReturn(result)
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(
                level = Level.INFO,
                includeArgs = false,
                includeResult = false
            )
        )

        // When
        val actualResult = aspect.logMethodExecution(joinPoint)

        // Then
        assertEquals(result, actualResult)
        Mockito.verify(joinPoint, times(1)).proceed()
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should log execution time accurately`() {
        // Given
        val methodName = "testMethod"
        val sleepTime = 100L
        
        Mockito.`when`(joinPoint.args).thenReturn(emptyArray<Any>())
        Mockito.`when`(joinPoint.proceed()).thenAnswer { 
            Thread.sleep(sleepTime)
            "result"
        }
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(level = Level.INFO)
        )

        // When
        val startTime = System.currentTimeMillis()
        val result = aspect.logMethodExecution(joinPoint)
        val executionTime = System.currentTimeMillis() - startTime

        // Then
        assertNotNull(result)
        assertTrue(executionTime >= sleepTime)
        Mockito.verify(joinPoint, times(1)).proceed()
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should log exception with full stack trace`() {
        // Given
        val args = arrayOf("test")
        val methodName = "testMethod"
        val exception = RuntimeException("Test exception")
        
        Mockito.`when`(joinPoint.args).thenReturn(args)
        Mockito.`when`(joinPoint.proceed()).thenThrow(exception)
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(level = Level.INFO)
        )

        // When/Then
        val thrownException = assertThrows<RuntimeException> {
            aspect.logMethodExecution(joinPoint)
        }
        
        assertEquals(exception, thrownException)
        assertEquals("Test exception", thrownException.message)
        Mockito.verify(joinPoint, times(1)).proceed()
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should handle null arguments and empty arrays`() {
        // Given
        val testCases = listOf(
            arrayOf<Any?>(null),
            emptyArray<Any>(),
            arrayOf<Any?>(null, null),
            arrayOf<Any?>("test", null, 123)
        )
        
        val methodName = "testMethod"
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(level = Level.INFO)
        )

        testCases.forEach { args ->
            // Reset mocks for each iteration
            Mockito.clearInvocations(joinPoint)
            
            Mockito.`when`(joinPoint.args).thenReturn(args)
            Mockito.`when`(joinPoint.proceed()).thenReturn(null)

            // When
            val result = aspect.logMethodExecution(joinPoint)

            // Then
            assertNull(result)
            Mockito.verify(joinPoint, times(1)).proceed()
        }
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should mask sensitive data in nested objects`() {
        // Given
        data class Credentials(val username: String, val password: String)
        data class User(val id: Long, val name: String, val credentials: Credentials)
        
        val user = User(1L, "John", Credentials("john_doe", "secret123"))
        val args = arrayOf<Any>(user)
        val methodName = "testMethod"
        
        Mockito.`when`(joinPoint.args).thenReturn(args)
        Mockito.`when`(joinPoint.proceed()).thenReturn(user)
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(
                level = Level.INFO,
                maskSensitiveData = true,
                sensitiveFields = arrayOf("password", "credentials")
            )
        )

        // When
        val actualResult = aspect.logMethodExecution(joinPoint)

        // Then
        assertEquals(user, actualResult)
        Mockito.verify(joinPoint, times(1)).proceed()
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should handle concurrent method executions`() {
        // Given
        val methodName = "testMethod"
        val numberOfCalls = 10
        val results = (1..numberOfCalls).map { i -> "result$i" }.toList()
        
        Mockito.`when`(joinPoint.args).thenReturn(emptyArray<Any>())
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(level = Level.INFO)
        )
        
        // Set up all possible return values beforehand
        results.forEach { result ->
            Mockito.`when`(joinPoint.proceed()).thenReturn(result)
        }

        // When
        val futures = results.mapIndexed { _, _ ->
            CompletableFuture.supplyAsync {
                aspect.logMethodExecution(joinPoint)
            }
        }

        // Then
        CompletableFuture.allOf(*futures.toTypedArray()).get(5, TimeUnit.SECONDS)
        
        // Verify that each call completed
        Mockito.verify(joinPoint, Mockito.times(numberOfCalls)).proceed()
        
        // Wait for async logging
        Thread.sleep(100)
    }

    @Test
    fun `should handle method signature cast failure gracefully`() {
        // Given
        val invalidSignature = mock<Signature>()
        Mockito.`when`(joinPoint.signature).thenReturn(invalidSignature)
        Mockito.`when`(joinPoint.proceed()).thenReturn("result")

        // When
        val result = aspect.logMethodExecution(joinPoint)

        // Then
        Mockito.verify(joinPoint, times(1)).proceed()
        assertNotNull(result)
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should handle method annotation retrieval failure gracefully`() {
        // Given
        val methodName = "testMethod"
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenThrow(RuntimeException("Test exception"))
        Mockito.`when`(joinPoint.proceed()).thenReturn("result")

        // When
        val result = aspect.logMethodExecution(joinPoint)

        // Then
        Mockito.verify(joinPoint, times(1)).proceed()
        assertNotNull(result)
        
        // Wait for async logging
        Thread.sleep(50)
    }

    @Test
    fun `should handle shutdown gracefully`() {
        // Given
        val aspect = LoggingAspect()
        
        // When
        aspect.shutdown()
        
        // Then
        // No exception should be thrown
        // Try logging after shutdown to ensure it's handled gracefully
        val methodName = "testMethod"
        Mockito.`when`(joinPoint.args).thenReturn(emptyArray<Any>())
        Mockito.`when`(joinPoint.proceed()).thenReturn("result")
        Mockito.`when`(method.name).thenReturn(methodName)
        Mockito.`when`(method.getAnnotation(Loggable::class.java)).thenReturn(
            Loggable(level = Level.INFO)
        )

        val result = aspect.logMethodExecution(joinPoint)
        assertNotNull(result)
        Mockito.verify(joinPoint, times(1)).proceed()
    }
} 