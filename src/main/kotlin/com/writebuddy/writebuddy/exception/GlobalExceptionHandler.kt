package com.writebuddy.writebuddy.exception

import com.writebuddy.writebuddy.controller.dto.response.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.sql.SQLException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { 
            "${it.field}: ${it.defaultMessage ?: "Invalid value"}" 
        }
        
        logger.warn("Validation error: {}", errors)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = errors,
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(CorrectionException::class)
    fun handleCorrectionException(
        ex: CorrectionException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Correction error: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Correction Error",
            message = ex.message ?: "An error occurred during correction processing",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(OpenAiException::class)
    fun handleOpenAiException(
        ex: OpenAiException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("OpenAI API error: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            error = "External Service Error",
            message = "AI service is temporarily unavailable. Please try again later.",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Custom validation error: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = ex.message ?: "Validation failed",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(java.io.EOFException::class)
    fun handleEOFException(
        ex: java.io.EOFException,
        request: WebRequest
    ): ResponseEntity<Void> {
        // 클라이언트 연결 끊김은 로그하지 않고 조용히 처리
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }

    @ExceptionHandler(java.io.IOException::class)
    fun handleIOException(
        ex: java.io.IOException,
        request: WebRequest
    ): ResponseEntity<Void> {
        // 일반적인 IO 예외는 DEBUG 레벨로만 로그
        if (ex.message?.contains("Broken pipe") == true || 
            ex.message?.contains("Connection reset") == true) {
            logger.debug("Client connection issue: {}", ex.message)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        
        logger.warn("IO error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }

    @ExceptionHandler(SQLException::class)
    fun handleSQLException(ex: SQLException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Database error occurred: ${ex.message}", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Database Error",
                    message = "Database connection or query failed",
                    path = request.getDescription(false).removePrefix("uri=")
                )
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(ex: NoResourceFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.resourcePath}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Resource Not Found",
                    message = "The requested resource '${ex.resourcePath}' was not found",
                    path = request.getDescription(false).removePrefix("uri=")
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        // 연결 관련 예외는 조용히 처리
        if (ex.cause is java.io.EOFException || 
            ex.message?.contains("Broken pipe") == true) {
            logger.debug("Client connection terminated: {}", ex.message)
            return ResponseEntity.badRequest().build()
        }
        
        logger.error("Unexpected error: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please try again later.",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
