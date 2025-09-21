package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.RealExample
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
open class AsyncCorrectionService(
    private val openAiClient: OpenAiClient
) {
    private val logger: Logger = LoggerFactory.getLogger(AsyncCorrectionService::class.java)

    @Async("asyncExecutor")
    open fun generateCorrectionWithExamplesParallel(origin: String):
            CompletableFuture<Pair<Sextuple<String, String, String, Int, String?, String?>, List<RealExample>>> {
        val startTime = System.currentTimeMillis()
        logger.info("[{}] Starting parallel correction + examples for: {}", Thread.currentThread().name, origin)

        return try {
            val (correctionData, examples, success) = openAiClient.generateCorrectionWithExamples(origin)
            val duration = System.currentTimeMillis() - startTime
            logger.info("[{}] Parallel processing completed in {}ms (success: {})",
                Thread.currentThread().name, duration, success)
            CompletableFuture.completedFuture(Pair(correctionData, examples))
        } catch (e: Exception) {
            logger.error("[{}] Error in parallel processing: {}", Thread.currentThread().name, e.message)
            CompletableFuture.failedFuture(e)
        }
    }
}
