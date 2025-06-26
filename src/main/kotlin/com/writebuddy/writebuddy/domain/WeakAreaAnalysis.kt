package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
class WeakAreaAnalysis(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,
    
    @Enumerated(EnumType.STRING)
    val weakAreaType: WeakAreaType,
    
    val pattern: String, // 실제 약점 패턴 (예: "missing articles", "wrong preposition")
    val frequency: Int, // 해당 실수 빈도
    val totalOccurrences: Int, // 전체 발생 횟수
    val lastOccurrence: java.time.LocalDateTime, // 마지막 발생 시점
    val improvementRate: Double = 0.0, // 개선율 (0.0 ~ 1.0)
    val severity: WeakAreaSeverity, // 심각도
    @Lob
    val exampleMistakes: String? = null // JSON 형태의 실수 예시들
) : BaseEntity()

enum class WeakAreaType {
    GRAMMAR_ARTICLES,      // 관사 (a, an, the)
    GRAMMAR_PREPOSITIONS,  // 전치사 (in, on, at, etc.)
    GRAMMAR_TENSES,        // 시제
    GRAMMAR_VERB_FORMS,    // 동사 형태
    GRAMMAR_PLURALS,       // 복수형
    GRAMMAR_SUBJECT_VERB,  // 주어-동사 일치
    SPELLING_COMMON,       // 일반적인 철자 오류
    SPELLING_HOMOPHONES,   // 동음이의어 (their/there/they're)
    STYLE_WORD_CHOICE,     // 단어 선택
    STYLE_SENTENCE_STRUCTURE, // 문장 구조
    PUNCTUATION_COMMAS,    // 콤마 사용
    PUNCTUATION_PERIODS,   // 마침표 사용
    OTHER                  // 기타
}

enum class WeakAreaSeverity {
    LOW,     // 가끔 실수하는 정도
    MEDIUM,  // 자주 실수하는 편
    HIGH,    // 매우 자주 실수
    CRITICAL // 거의 대부분 틀림
}

// 약점 분석 결과를 담는 데이터 클래스
data class UserWeakAreasSummary(
    val userId: Long,
    val topWeakAreas: List<WeakAreaInfo>,
    val overallImprovementRate: Double,
    val recommendedFocus: WeakAreaType?,
    val totalMistakes: Int,
    val analysisDate: java.time.LocalDateTime
)

data class WeakAreaInfo(
    val type: WeakAreaType,
    val typeDisplay: String, // 한국어 표시명
    val pattern: String,
    val frequency: Int,
    val severity: WeakAreaSeverity,
    val improvementRate: Double,
    val exampleMistakes: List<String>,
    val recommendation: String // 개선 추천사항
)