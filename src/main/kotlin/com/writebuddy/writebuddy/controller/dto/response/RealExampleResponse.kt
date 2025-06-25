package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import java.time.LocalDateTime

data class RealExampleResponse(
    val id: Long,
    val phrase: String,
    val source: String,
    val sourceType: ExampleSourceType,
    val sourceTypeDisplay: String,
    val sourceTypeEmoji: String,
    val context: String,
    val url: String?,
    val timestamp: String?,
    val difficulty: Int,
    val tags: List<String>,
    val isVerified: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(realExample: RealExample): RealExampleResponse {
            return RealExampleResponse(
                id = realExample.id,
                phrase = realExample.phrase,
                source = realExample.source,
                sourceType = realExample.sourceType,
                sourceTypeDisplay = realExample.sourceType.displayName,
                sourceTypeEmoji = realExample.sourceType.emoji,
                context = realExample.context,
                url = realExample.url,
                timestamp = realExample.timestamp,
                difficulty = realExample.difficulty,
                tags = realExample.tags?.split(",")?.map { it.trim() } ?: emptyList(),
                isVerified = realExample.isVerified,
                createdAt = realExample.createdAt,
                updatedAt = realExample.updatedAt
            )
        }
    }
}