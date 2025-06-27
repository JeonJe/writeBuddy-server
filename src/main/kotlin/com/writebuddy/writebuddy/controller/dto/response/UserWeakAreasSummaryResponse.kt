package com.writebuddy.writebuddy.controller.dto.response

import com.writebuddy.writebuddy.domain.UserWeakAreasSummary
import com.writebuddy.writebuddy.domain.WeakAreaInfo
import com.writebuddy.writebuddy.domain.WeakAreaSeverity
import com.writebuddy.writebuddy.domain.WeakAreaType
import java.time.LocalDateTime

data class UserWeakAreasSummaryResponse(
    val userId: Long,
    val topWeakAreas: List<WeakAreaInfoResponse>,
    val overallImprovementRate: Double,
    val improvementRateDisplay: String,
    val recommendedFocus: String?,
    val recommendedFocusDisplay: String?,
    val totalMistakes: Int,
    val analysisDate: LocalDateTime,
    val summary: AnalysisSummary
) {
    companion object {
        fun from(summary: UserWeakAreasSummary): UserWeakAreasSummaryResponse {
            return UserWeakAreasSummaryResponse(
                userId = summary.userId,
                topWeakAreas = summary.topWeakAreas.map { WeakAreaInfoResponse.from(it) },
                overallImprovementRate = summary.overallImprovementRate,
                improvementRateDisplay = formatImprovementRate(summary.overallImprovementRate),
                recommendedFocus = summary.recommendedFocus?.name,
                recommendedFocusDisplay = summary.recommendedFocus?.let { getWeakAreaDisplayName(it) },
                totalMistakes = summary.totalMistakes,
                analysisDate = summary.analysisDate,
                summary = createAnalysisSummary(summary)
            )
        }
        
        private fun formatImprovementRate(rate: Double): String {
            return when {
                rate >= 0.8 -> "🚀 엄청난 성장! (${(rate * 100).toInt()}%)"
                rate >= 0.6 -> "📈 빠른 개선 중 (${(rate * 100).toInt()}%)"
                rate >= 0.3 -> "📊 꾸준히 성장 (${(rate * 100).toInt()}%)"
                rate >= 0.1 -> "🌱 조금씩 나아짐 (${(rate * 100).toInt()}%)"
                else -> "💪 노력이 필요해요 (${(rate * 100).toInt()}%)"
            }
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
        
        private fun createAnalysisSummary(summary: UserWeakAreasSummary): AnalysisSummary {
            val criticalCount = summary.topWeakAreas.count { it.severity == WeakAreaSeverity.CRITICAL }
            val highCount = summary.topWeakAreas.count { it.severity == WeakAreaSeverity.HIGH }
            
            val message = when {
                criticalCount > 0 -> "🚨 집중적인 학습이 필요한 영역이 ${criticalCount}개 있어요!"
                highCount > 0 -> "⚠️ 우선적으로 개선할 영역이 ${highCount}개 있어요."
                summary.topWeakAreas.isNotEmpty() -> "📝 조금씩 개선해나가면 되는 영역들이에요."
                else -> "🎉 완벽해요! 약점이 발견되지 않았습니다."
            }
            
            return AnalysisSummary(
                criticalAreas = criticalCount,
                highPriorityAreas = highCount,
                totalWeakAreas = summary.topWeakAreas.size,
                message = message
            )
        }
    }
}

data class WeakAreaInfoResponse(
    val type: String,
    val typeDisplay: String,
    val pattern: String,
    val frequency: Int,
    val frequencyDisplay: String,
    val severity: String,
    val severityDisplay: String,
    val severityColor: String,
    val improvementRate: Double,
    val improvementRateDisplay: String,
    val exampleMistakes: List<String>,
    val recommendation: String
) {
    companion object {
        fun from(info: WeakAreaInfo): WeakAreaInfoResponse {
            return WeakAreaInfoResponse(
                type = info.type.name,
                typeDisplay = info.typeDisplay,
                pattern = info.pattern,
                frequency = info.frequency,
                frequencyDisplay = "${info.frequency}회 개선 기회",
                severity = info.severity.name,
                severityDisplay = getSeverityDisplayName(info.severity),
                severityColor = getSeverityColor(info.severity),
                improvementRate = info.improvementRate,
                improvementRateDisplay = formatImprovementRate(info.improvementRate),
                exampleMistakes = info.exampleMistakes,
                recommendation = info.recommendation
            )
        }
        
        private fun getSeverityDisplayName(severity: WeakAreaSeverity): String {
            return when (severity) {
                WeakAreaSeverity.CRITICAL -> "🔴 매우 심각"
                WeakAreaSeverity.HIGH -> "🟠 심각"
                WeakAreaSeverity.MEDIUM -> "🟡 보통"
                WeakAreaSeverity.LOW -> "🟢 경미"
            }
        }
        
        private fun getSeverityColor(severity: WeakAreaSeverity): String {
            return when (severity) {
                WeakAreaSeverity.CRITICAL -> "#ef4444"
                WeakAreaSeverity.HIGH -> "#f97316"
                WeakAreaSeverity.MEDIUM -> "#eab308"
                WeakAreaSeverity.LOW -> "#22c55e"
            }
        }
        
        private fun formatImprovementRate(rate: Double): String {
            return when {
                rate >= 0.5 -> "🚀 빠른 개선"
                rate >= 0.2 -> "📈 개선 중"
                rate > 0.0 -> "🌱 조금씩 개선"
                else -> "💪 더 노력 필요"
            }
        }
    }
}

data class AnalysisSummary(
    val criticalAreas: Int,
    val highPriorityAreas: Int,
    val totalWeakAreas: Int,
    val message: String
)