package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import com.writebuddy.writebuddy.repository.RealExampleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RealExampleService(
    private val realExampleRepository: RealExampleRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(RealExampleService::class.java)
    
    fun findExamplesByPhrase(phrase: String): List<RealExample> {
        logger.debug("구문으로 예시 검색: {}", phrase)
        return realExampleRepository.findByPhraseContainingIgnoreCase(phrase)
    }
    
    fun searchExamples(keyword: String): List<RealExample> {
        logger.debug("키워드로 예시 검색: {}", keyword)
        return realExampleRepository.searchByKeyword(keyword)
    }
    
    fun getRandomExamples(count: Int = 5): List<RealExample> {
        logger.debug("랜덤 예시 조회: {}개", count)
        return realExampleRepository.findRandomVerifiedExamples().take(count)
    }
    
    fun getExamplesBySourceType(sourceType: ExampleSourceType): List<RealExample> {
        logger.debug("출처 타입별 예시 조회: {}", sourceType)
        return realExampleRepository.findBySourceType(sourceType)
    }
    
    fun getExamplesByDifficulty(minDifficulty: Int, maxDifficulty: Int): List<RealExample> {
        logger.debug("난이도별 예시 조회: {}-{}", minDifficulty, maxDifficulty)
        return realExampleRepository.findByDifficultyBetween(minDifficulty, maxDifficulty)
    }
    
    fun getDailyRecommendation(): List<RealExample> {
        logger.debug("오늘의 추천 예시 조회")
        // 다양한 소스에서 하나씩 선택해서 균형있게 제공
        val recommendations = mutableListOf<RealExample>()
        
        ExampleSourceType.values().forEach { sourceType ->
            val examples = realExampleRepository.findBySourceType(sourceType)
            if (examples.isNotEmpty()) {
                recommendations.add(examples.random())
            }
        }
        
        return recommendations.take(5) // 최대 5개
    }
    
    // 교정된 문장과 관련된 실제 사용 예시 찾기
    fun findRelatedExamples(correctedSentence: String): List<RealExample> {
        logger.debug("교정된 문장과 관련된 예시 검색: {}", correctedSentence)
        
        // 주요 키워드 추출 (간단한 방식)
        val keywords = extractKeywords(correctedSentence)
        val allExamples = mutableSetOf<RealExample>()
        
        keywords.forEach { keyword ->
            val examples = realExampleRepository.searchByKeyword(keyword)
            allExamples.addAll(examples)
        }
        
        return allExamples.take(3) // 최대 3개
    }
    
    private fun extractKeywords(sentence: String): List<String> {
        // 간단한 키워드 추출 로직
        return sentence.split(" ")
            .filter { it.length > 3 } // 3글자 이상만
            .filter { !it.matches(Regex("[a-z]+")) } // 관사, 전치사 등 제외
            .take(3) // 최대 3개 키워드
    }
    
    fun createExample(example: RealExample): RealExample {
        logger.info("새 예시 생성: {}", example.phrase)
        return realExampleRepository.save(example)
    }
}