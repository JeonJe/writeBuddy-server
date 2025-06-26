package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.*
import com.writebuddy.writebuddy.repository.CorrectionRepository
import com.writebuddy.writebuddy.repository.WeakAreaAnalysisRepository
import com.writebuddy.writebuddy.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LearningAnalyticsService(
    private val correctionRepository: CorrectionRepository,
    private val weakAreaRepository: WeakAreaAnalysisRepository,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger: Logger = LoggerFactory.getLogger(LearningAnalyticsService::class.java)
    
    fun analyzeUserWeakAreas(userId: Long): UserWeakAreasSummary {
        logger.info("사용자 약점 분석 시작: userId={}", userId)
        
        // 최근 3개월간의 교정 기록 가져오기
        val recentCorrections = correctionRepository.findByUserIdAndCreatedAtAfter(
            userId, 
            LocalDateTime.now().minusMonths(3)
        )
        
        if (recentCorrections.isEmpty()) {
            return createEmptyAnalysis(userId)
        }
        
        // 패턴 분석 및 약점 업데이트
        val detectedPatterns = detectWeakPatterns(recentCorrections)
        updateWeakAreaAnalysis(userId, detectedPatterns)
        
        // 최신 약점 분석 결과 조회
        val weakAreas = weakAreaRepository.findByUserIdOrderByFrequencyDesc(userId)
        val topWeakAreas = weakAreas.take(5).map { convertToWeakAreaInfo(it) }
        
        val overallImprovementRate = weakAreaRepository.calculateAverageImprovementRate(userId) ?: 0.0
        val recommendedFocus = determineRecommendedFocus(weakAreas)
        
        logger.info("약점 분석 완료: userId={}, 발견된 약점 수={}", userId, topWeakAreas.size)
        
        return UserWeakAreasSummary(
            userId = userId,
            topWeakAreas = topWeakAreas,
            overallImprovementRate = overallImprovementRate,
            recommendedFocus = recommendedFocus,
            totalMistakes = recentCorrections.size,
            analysisDate = LocalDateTime.now()
        )
    }
    
    private fun detectWeakPatterns(corrections: List<Correction>): Map<WeakAreaType, PatternInfo> {
        val patterns = mutableMapOf<WeakAreaType, PatternInfo>()
        
        corrections.forEach { correction ->
            val detectedTypes = analyzeCorrection(correction)
            detectedTypes.forEach { (type, pattern, example) ->
                val existing = patterns[type] ?: PatternInfo(pattern, 0, mutableListOf())
                existing.frequency++
                existing.examples.add(example)
                patterns[type] = existing
            }
        }
        
        return patterns
    }
    
    private fun analyzeCorrection(correction: Correction): List<Triple<WeakAreaType, String, String>> {
        val results = mutableListOf<Triple<WeakAreaType, String, String>>()
        val original = correction.originSentence.lowercase()
        val corrected = correction.correctedSentence.lowercase()
        val feedback = correction.feedback.lowercase()
        
        // 관사 문제 감지
        if (isArticleIssue(feedback, original, corrected)) {
            results.add(Triple(
                WeakAreaType.GRAMMAR_ARTICLES,
                "관사 누락 또는 잘못된 사용",
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        // 전치사 문제 감지
        if (isPrepositionIssue(feedback)) {
            results.add(Triple(
                WeakAreaType.GRAMMAR_PREPOSITIONS,
                "전치사 사용 오류", 
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        // 시제 문제 감지
        if (isTenseIssue(feedback)) {
            results.add(Triple(
                WeakAreaType.GRAMMAR_TENSES,
                "시제 사용 오류",
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        // 동사 형태 문제 감지
        if (isVerbFormIssue(feedback, original, corrected)) {
            results.add(Triple(
                WeakAreaType.GRAMMAR_VERB_FORMS,
                "동사 형태 오류",
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        // 복수형 문제 감지
        if (isPluralIssue(feedback)) {
            results.add(Triple(
                WeakAreaType.GRAMMAR_PLURALS,
                "복수형 사용 오류",
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        // 철자 문제 감지
        if (correction.feedbackType == FeedbackType.SPELLING) {
            results.add(Triple(
                WeakAreaType.SPELLING_COMMON,
                "철자 오류",
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        // 문체 문제 감지
        if (correction.feedbackType == FeedbackType.STYLE) {
            results.add(Triple(
                WeakAreaType.STYLE_WORD_CHOICE,
                "문체 및 단어 선택 개선",
                "${correction.originSentence} → ${correction.correctedSentence}"
            ))
        }
        
        return results
    }
    
    // 관사 문제 감지 로직
    private fun isArticleIssue(feedback: String, original: String, corrected: String): Boolean {
        val articleKeywords = listOf("관사", "the", "a ", "an ", "정관사", "부정관사")
        return articleKeywords.any { feedback.contains(it) } ||
                hasArticleDifference(original, corrected)
    }
    
    private fun hasArticleDifference(original: String, corrected: String): Boolean {
        val articles = listOf(" a ", " an ", " the ")
        return articles.any { article ->
            (original.contains(article) && !corrected.contains(article)) ||
            (!original.contains(article) && corrected.contains(article))
        }
    }
    
    // 전치사 문제 감지 로직
    private fun isPrepositionIssue(feedback: String): Boolean {
        val prepositionKeywords = listOf("전치사", "in ", "on ", "at ", "for ", "with ", "by ")
        return prepositionKeywords.any { feedback.contains(it) }
    }
    
    // 시제 문제 감지 로직
    private fun isTenseIssue(feedback: String): Boolean {
        val tenseKeywords = listOf("시제", "과거", "현재", "미래", "완료", "진행", "was", "were", "will", "have", "had")
        return tenseKeywords.any { feedback.contains(it) }
    }
    
    // 동사 형태 문제 감지 로직
    private fun isVerbFormIssue(feedback: String, original: String, corrected: String): Boolean {
        val verbKeywords = listOf("동사", "be동사", "일반동사", "주어-동사", "단수", "복수")
        return verbKeywords.any { feedback.contains(it) } ||
                hasSubjectVerbDisagreement(original, corrected)
    }
    
    private fun hasSubjectVerbDisagreement(original: String, corrected: String): Boolean {
        // 간단한 주어-동사 불일치 감지 (예: "I are" -> "I am")
        val commonErrors = mapOf(
            "i are" to "i am",
            "he are" to "he is", 
            "she are" to "she is",
            "they is" to "they are"
        )
        return commonErrors.any { (wrong, right) ->
            original.contains(wrong) && corrected.contains(right)
        }
    }
    
    // 복수형 문제 감지 로직
    private fun isPluralIssue(feedback: String): Boolean {
        val pluralKeywords = listOf("복수", "단수", "가산명사", "불가산명사", "plural", "singular")
        return pluralKeywords.any { feedback.contains(it) }
    }
    
    private fun updateWeakAreaAnalysis(userId: Long, patterns: Map<WeakAreaType, PatternInfo>) {
        patterns.forEach { (type, patternInfo) ->
            val existing = weakAreaRepository.findByUserIdAndWeakAreaType(userId, type)
            
            if (existing != null) {
                // 기존 분석 업데이트
                val updated = WeakAreaAnalysis(
                    id = existing.id,
                    user = existing.user,
                    weakAreaType = type,
                    pattern = patternInfo.pattern,
                    frequency = existing.frequency + patternInfo.frequency,
                    totalOccurrences = existing.totalOccurrences + patternInfo.frequency,
                    lastOccurrence = LocalDateTime.now(),
                    improvementRate = calculateImprovementRate(existing, patternInfo.frequency),
                    severity = determineSeverity(existing.frequency + patternInfo.frequency),
                    exampleMistakes = objectMapper.writeValueAsString(patternInfo.examples.take(5))
                )
                weakAreaRepository.save(updated)
            } else {
                // 새로운 약점 등록
                val user = userRepository.findById(userId).orElse(null)
                if (user != null) {
                    val newAnalysis = WeakAreaAnalysis(
                        user = user,
                        weakAreaType = type,
                        pattern = patternInfo.pattern,
                        frequency = patternInfo.frequency,
                        totalOccurrences = patternInfo.frequency,
                        lastOccurrence = LocalDateTime.now(),
                        improvementRate = 0.0,
                        severity = determineSeverity(patternInfo.frequency),
                        exampleMistakes = objectMapper.writeValueAsString(patternInfo.examples.take(5))
                    )
                    weakAreaRepository.save(newAnalysis)
                }
            }
        }
    }
    
    private fun calculateImprovementRate(existing: WeakAreaAnalysis, newMistakes: Int): Double {
        // 최근 실수 빈도가 줄어들었는지 체크
        val recentPeriodDays = 30
        val oldFrequencyPerDay = existing.frequency.toDouble() / recentPeriodDays
        val currentFrequencyPerDay = newMistakes.toDouble() / recentPeriodDays
        
        return if (oldFrequencyPerDay > 0) {
            maxOf(0.0, 1.0 - (currentFrequencyPerDay / oldFrequencyPerDay))
        } else {
            0.0
        }
    }
    
    private fun determineSeverity(frequency: Int): WeakAreaSeverity {
        return when {
            frequency >= 10 -> WeakAreaSeverity.CRITICAL
            frequency >= 5 -> WeakAreaSeverity.HIGH
            frequency >= 2 -> WeakAreaSeverity.MEDIUM
            else -> WeakAreaSeverity.LOW
        }
    }
    
    private fun determineRecommendedFocus(weakAreas: List<WeakAreaAnalysis>): WeakAreaType? {
        return weakAreas
            .filter { it.severity in listOf(WeakAreaSeverity.HIGH, WeakAreaSeverity.CRITICAL) }
            .maxByOrNull { it.frequency }?.weakAreaType
    }
    
    private fun convertToWeakAreaInfo(analysis: WeakAreaAnalysis): WeakAreaInfo {
        val examples = try {
            objectMapper.readValue(analysis.exampleMistakes ?: "[]", objectMapper.typeFactory.constructCollectionType(List::class.java, String::class.java))
        } catch (e: Exception) {
            emptyList<String>()
        }
        
        return WeakAreaInfo(
            type = analysis.weakAreaType,
            typeDisplay = getWeakAreaDisplayName(analysis.weakAreaType),
            pattern = analysis.pattern,
            frequency = analysis.frequency,
            severity = analysis.severity,
            improvementRate = analysis.improvementRate,
            exampleMistakes = examples.take(3),
            recommendation = getRecommendation(analysis.weakAreaType, analysis.severity)
        )
    }
    
    private fun getWeakAreaDisplayName(type: WeakAreaType): String {
        return when (type) {
            WeakAreaType.GRAMMAR_ARTICLES -> "관사 (a, an, the)"
            WeakAreaType.GRAMMAR_PREPOSITIONS -> "전치사 (in, on, at 등)"
            WeakAreaType.GRAMMAR_TENSES -> "시제"
            WeakAreaType.GRAMMAR_VERB_FORMS -> "동사 형태"
            WeakAreaType.GRAMMAR_PLURALS -> "복수형"
            WeakAreaType.GRAMMAR_SUBJECT_VERB -> "주어-동사 일치"
            WeakAreaType.SPELLING_COMMON -> "철자 오류"
            WeakAreaType.SPELLING_HOMOPHONES -> "동음이의어"
            WeakAreaType.STYLE_WORD_CHOICE -> "단어 선택"
            WeakAreaType.STYLE_SENTENCE_STRUCTURE -> "문장 구조"
            WeakAreaType.PUNCTUATION_COMMAS -> "콤마 사용"
            WeakAreaType.PUNCTUATION_PERIODS -> "마침표 사용"
            WeakAreaType.OTHER -> "기타"
        }
    }
    
    private fun getRecommendation(type: WeakAreaType, severity: WeakAreaSeverity): String {
        val baseMessage = when (type) {
            WeakAreaType.GRAMMAR_ARTICLES -> "관사 사용법을 집중적으로 연습해보세요. 가산명사와 불가산명사 구분이 핵심이에요!"
            WeakAreaType.GRAMMAR_PREPOSITIONS -> "전치사는 많이 접하고 암기하는 게 최고! 자주 쓰이는 전치사 조합을 외워보세요."
            WeakAreaType.GRAMMAR_TENSES -> "시제는 문맥이 중요해요. 언제 일어났는지, 지속되는지 생각해보세요."
            WeakAreaType.GRAMMAR_VERB_FORMS -> "주어에 따른 동사 변화를 체크해보세요. 단수/복수 구분이 포인트!"
            WeakAreaType.SPELLING_COMMON -> "자주 틀리는 단어들을 따로 정리해서 반복 학습해보세요."
            else -> "꾸준한 연습이 답입니다! 같은 실수를 반복하지 않도록 주의해보세요."
        }
        
        val urgency = when (severity) {
            WeakAreaSeverity.CRITICAL -> " 🚨 최우선으로 집중해야 할 부분이에요!"
            WeakAreaSeverity.HIGH -> " ⚠️ 빠른 시일 내에 개선이 필요해요."
            WeakAreaSeverity.MEDIUM -> " 📝 차근차근 개선해나가면 됩니다."
            WeakAreaSeverity.LOW -> " ✅ 조금만 더 신경쓰면 완벽해질 거예요!"
        }
        
        return baseMessage + urgency
    }
    
    private fun createEmptyAnalysis(userId: Long): UserWeakAreasSummary {
        return UserWeakAreasSummary(
            userId = userId,
            topWeakAreas = emptyList(),
            overallImprovementRate = 0.0,
            recommendedFocus = null,
            totalMistakes = 0,
            analysisDate = LocalDateTime.now()
        )
    }
    
    private data class PatternInfo(
        val pattern: String,
        var frequency: Int,
        val examples: MutableList<String>
    )
}