package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RealExampleService(
    private val openAiClient: OpenAiClient,
    private val promptManager: PromptManager,
    private val objectMapper: ObjectMapper
) {
    private val logger: Logger = LoggerFactory.getLogger(RealExampleService::class.java)
    
    
    // 교정된 문장과 관련된 실제 사용 예시 찾기
    fun findRelatedExamples(correctedSentence: String): List<RealExample> {
        logger.debug("교정된 문장과 관련된 예시 생성: {}", correctedSentence)
        
        // OpenAI로 실시간 예시 생성
        return generateExamplesWithAI(correctedSentence)
    }
    
    private fun generateExamplesWithAI(correctedSentence: String): List<RealExample> {
        return try {
            val systemPrompt = promptManager.getExampleGenerationSystemPrompt()
            val userPrompt = promptManager.getExampleGenerationUserPrompt(correctedSentence)
            
            logger.info("OpenAI로 실제 사용 예시 생성 요청: {}", correctedSentence)
            val response = openAiClient.sendChatRequest(systemPrompt, userPrompt)
            
            // JSON 응답 파싱
            val jsonNode = objectMapper.readTree(response)
            val examplesNode = jsonNode.get("examples")
            
            val examples = mutableListOf<RealExample>()
            if (examplesNode != null && examplesNode.isArray) {
                examplesNode.forEach { exampleNode ->
                    try {
                        val tagsNode = exampleNode.get("tags")
                        val tagsString = if (tagsNode != null && tagsNode.isArray) {
                            tagsNode.map { it.asText() }.joinToString(",")
                        } else {
                            ""
                        }
                        
                        val example = RealExample(
                            phrase = exampleNode.get("phrase")?.asText() ?: "",
                            source = exampleNode.get("source")?.asText() ?: "",
                            sourceType = ExampleSourceType.valueOf(exampleNode.get("sourceType")?.asText() ?: "MOVIE"),
                            context = exampleNode.get("context")?.asText() ?: "",
                            url = exampleNode.get("url")?.asText(),
                            timestamp = exampleNode.get("timestamp")?.asText(),
                            difficulty = exampleNode.get("difficulty")?.asInt() ?: 5,
                            tags = tagsString,
                            isVerified = exampleNode.get("isVerified")?.asBoolean() ?: true
                        )
                        examples.add(example)
                    } catch (e: Exception) {
                        logger.warn("예시 파싱 실패: {}", e.message)
                    }
                }
            }
            
            logger.info("OpenAI 예시 생성 완료: {}개", examples.size)
            examples
            
        } catch (e: Exception) {
            logger.error("OpenAI 예시 생성 실패: {}", e.message)
            // 실패 시 빈 목록 반환
            emptyList()
        }
    }
    
}