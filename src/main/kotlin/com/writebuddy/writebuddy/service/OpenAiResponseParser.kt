package com.writebuddy.writebuddy.service

import org.springframework.stereotype.Component

@Component
class OpenAiResponseParser {
    
    companion object {
        // API Role Constants
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        
        // Response Parsing Prefixes
        const val CORRECTED_PREFIX_KOR = "교정문:"
        const val FEEDBACK_PREFIX_KOR = "피드백:"
        const val TYPE_PREFIX_KOR = "유형:"
        const val SCORE_PREFIX_KOR = "점수:"
        const val ORIGIN_TRANSLATION_PREFIX_KOR = "원문번역:"
        const val CORRECTED_TRANSLATION_PREFIX_KOR = "교정번역:"
        
        // Default Values
        const val DEFAULT_FEEDBACK_TYPE = "Grammar"
        const val FALLBACK_FEEDBACK_TYPE = "SYSTEM"
        const val DEFAULT_SCORE = 5
    }
    
    fun parseScore(scoreStr: String): Int {
        return try {
            val cleanScore = scoreStr.replace(Regex("[^0-9]"), "")
            val score = cleanScore.toIntOrNull() ?: DEFAULT_SCORE
            score.coerceIn(1, 10)
        } catch (e: Exception) {
            DEFAULT_SCORE
        }
    }
    
    fun extractValue(lines: List<String>, prefix: String): String? {
        return lines.find { it.startsWith(prefix) }?.removePrefix(prefix)?.trim()
    }
    
    fun parseResponseWithTranslations(content: String): Sextuple<String, String, String, Int, String?, String?> {
        val lines = content.lines()
        
        val corrected = extractValue(lines, CORRECTED_PREFIX_KOR)
            ?: content.split("\n").firstOrNull()?.trim() ?: ""
        
        val feedback = extractValue(lines, FEEDBACK_PREFIX_KOR)
            ?: content.split("\n").drop(1).joinToString(" ").trim()
        
        val feedbackType = extractValue(lines, TYPE_PREFIX_KOR) ?: DEFAULT_FEEDBACK_TYPE
        
        val scoreStr = extractValue(lines, SCORE_PREFIX_KOR) ?: DEFAULT_SCORE.toString()
        val score = parseScore(scoreStr)
        
        val originTranslation = extractValue(lines, ORIGIN_TRANSLATION_PREFIX_KOR)
        val correctedTranslation = extractValue(lines, CORRECTED_TRANSLATION_PREFIX_KOR)
        
        return Sextuple(corrected, feedback, feedbackType, score, originTranslation, correctedTranslation)
    }
}