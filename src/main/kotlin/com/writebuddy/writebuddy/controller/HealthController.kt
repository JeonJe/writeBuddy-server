package com.writebuddy.writebuddy.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource
import java.sql.Connection

@RestController
@RequestMapping("/health")
class HealthController(
    private val dataSource: DataSource
) {
    private val logger: Logger = LoggerFactory.getLogger(HealthController::class.java)

    @GetMapping
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to System.currentTimeMillis(),
            "database" to checkDatabaseConnection()
        )
    }

    @GetMapping("/db")
    fun databaseHealth(): Map<String, Any> {
        return checkDatabaseConnection()
    }

    private fun checkDatabaseConnection(): Map<String, Any> {
        return try {
            val startTime = System.currentTimeMillis()
            dataSource.connection.use { connection: Connection ->
                connection.prepareStatement("SELECT 1").use { statement ->
                    statement.executeQuery().use { resultSet ->
                        val connectionTime = System.currentTimeMillis() - startTime
                        if (resultSet.next()) {
                            logger.info("Database connection successful in {}ms", connectionTime)
                            mapOf(
                                "status" to "UP",
                                "connectionTime" to "${connectionTime}ms",
                                "vendor" to connection.metaData.databaseProductName,
                                "version" to connection.metaData.databaseProductVersion,
                                "url" to (connection.metaData.url?.substringBefore("?") ?: "unknown")
                            )
                        } else {
                            mapOf("status" to "DOWN", "error" to "No result from test query")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Database connection failed", e)
            mapOf(
                "status" to "DOWN",
                "error" to (e.message ?: "Unknown error"),
                "exception" to e.javaClass.simpleName
            )
        }
    }
}