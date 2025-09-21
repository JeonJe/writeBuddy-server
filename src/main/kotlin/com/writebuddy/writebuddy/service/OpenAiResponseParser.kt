package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import org.springframework.stereotype.Component

@Component
class OpenAiResponseParser(
    private val objectMapper: ObjectMapper
) {
    
    companion object {
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        const val CORRECTED_PREFIX_KOR = "교정문:"
        const val FEEDBACK_PREFIX_KOR = "피드백:"
        const val TYPE_PREFIX_KOR = "유형:"
        const val SCORE_PREFIX_KOR = "점수:"
        const val ORIGIN_TRANSLATION_PREFIX_KOR = "원문번역:"
        const val CORRECTED_TRANSLATION_PREFIX_KOR = "교정번역:"
        
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
    
    fun parseIntegratedResponse(content: String): Triple<Sextuple<String, String, String, Int, String?, String?>, List<RealExample>, Boolean> {
        return try {
            val jsonNode = objectMapper.readTree(content)
            
            val corrected = jsonNode.get("correctedSentence")?.asText() ?: ""
            val feedback = jsonNode.get("feedback")?.asText() ?: ""
            val feedbackType = jsonNode.get("feedbackType")?.asText() ?: DEFAULT_FEEDBACK_TYPE
            val score = jsonNode.get("score")?.asInt() ?: DEFAULT_SCORE
            val originTranslation = jsonNode.get("originTranslation")?.asText()
            val correctedTranslation = jsonNode.get("correctedTranslation")?.asText()
            
            val correctionData = Sextuple(corrected, feedback, feedbackType, score, originTranslation, correctedTranslation)
            
            val examples = mutableListOf<RealExample>()
            val examplesNode = jsonNode.get("relatedExamples")
            if (examplesNode != null && examplesNode.isArray) {
                for (exampleNode in examplesNode) {
                    try {
                        val example = RealExample(
                            phrase = exampleNode.get("phrase")?.asText() ?: "",
                            source = exampleNode.get("source")?.asText() ?: "",
                            sourceType = ExampleSourceType.valueOf(exampleNode.get("sourceType")?.asText() ?: "OTHER"),
                            context = exampleNode.get("context")?.asText() ?: "",
                            url = null,  // URL 제거로 신뢰도 향상
                            timestamp = null,  // 타임스탬프 제거로 신뢰도 향상
                            difficulty = exampleNode.get("difficulty")?.asInt() ?: 5,
                            tags = exampleNode.get("tags")?.joinToString(",") { it.asText() },
                            isVerified = exampleNode.get("isVerified")?.asBoolean() ?: true
                        )
                        examples.add(example)
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
            
            Triple(correctionData, examples, true)
        } catch (e: Exception) {
            val correctionData = parseResponseWithTranslations(content)
            Triple(correctionData, emptyList(), false)
        }
    }
}