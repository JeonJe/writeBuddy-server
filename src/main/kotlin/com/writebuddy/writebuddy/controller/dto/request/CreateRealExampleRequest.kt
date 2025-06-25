package com.writebuddy.writebuddy.controller.dto.request

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import jakarta.validation.constraints.*

data class CreateRealExampleRequest(
    @field:NotBlank(message = "표현은 필수입니다")
    @field:Size(max = 500, message = "표현은 500자 이하여야 합니다")
    val phrase: String,
    
    @field:NotBlank(message = "출처는 필수입니다")
    @field:Size(max = 200, message = "출처는 200자 이하여야 합니다")
    val source: String,
    
    @field:NotNull(message = "출처 타입은 필수입니다")
    val sourceType: ExampleSourceType,
    
    @field:NotBlank(message = "맥락 설명은 필수입니다")
    val context: String,
    
    @field:Size(max = 500, message = "URL은 500자 이하여야 합니다")
    val url: String? = null,
    
    @field:Size(max = 20, message = "타임스탬프는 20자 이하여야 합니다")
    val timestamp: String? = null,
    
    @field:Min(value = 1, message = "난이도는 1 이상이어야 합니다")
    @field:Max(value = 10, message = "난이도는 10 이하여야 합니다")
    val difficulty: Int = 5,
    
    @field:Size(max = 100, message = "태그는 100자 이하여야 합니다")
    val tags: String? = null,
    
    val isVerified: Boolean = false
) {
    fun toEntity(): RealExample {
        return RealExample(
            phrase = phrase,
            source = source,
            sourceType = sourceType,
            context = context,
            url = url,
            timestamp = timestamp,
            difficulty = difficulty,
            tags = tags,
            isVerified = isVerified
        )
    }
}