package com.writebuddy.writebuddy.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
open class AsyncConfig : AsyncConfigurer {

    private val logger: Logger = LoggerFactory.getLogger(AsyncConfig::class.java)

    @Bean(name = ["asyncExecutor"])
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 25
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("WriteBuddy-Async-")
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)
        executor.initialize()

        logger.info("Async Executor configured - Core: ${executor.corePoolSize}, Max: ${executor.maxPoolSize}")
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { ex, method, params ->
            logger.error("Uncaught exception in async method '${method.name}': ${ex.message}", ex)
            logger.error("Method parameters: ${params.contentToString()}")
        }
    }
}