package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("WeakAreaAnalysis 도메인 테스트")
class WeakAreaAnalysisTest {

    @Nested
    @DisplayName("WeakAreaAnalysis 생성")
    inner class WeakAreaAnalysisCreation {

        @Test
        @DisplayName("유효한 값으로 WeakAreaAnalysis 생성")
        fun createWeakAreaAnalysis_validValues() {
            val user = User(username = "testuser", email = "test@example.com")
            val lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0)

            val weakArea = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.GRAMMAR_ARTICLES,
                pattern = "missing articles",
                frequency = 5,
                totalOccurrences = 10,
                lastOccurrence = lastOccurrence,
                improvementRate = 0.5,
                severity = WeakAreaSeverity.MEDIUM
            )

            assertThat(weakArea.user).isEqualTo(user)
            assertThat(weakArea.weakAreaType).isEqualTo(WeakAreaType.GRAMMAR_ARTICLES)
            assertThat(weakArea.pattern).isEqualTo("missing articles")
            assertThat(weakArea.frequency).isEqualTo(5)
            assertThat(weakArea.totalOccurrences).isEqualTo(10)
            assertThat(weakArea.lastOccurrence).isEqualTo(lastOccurrence)
            assertThat(weakArea.improvementRate).isEqualTo(0.5)
            assertThat(weakArea.severity).isEqualTo(WeakAreaSeverity.MEDIUM)
        }

        @Test
        @DisplayName("exampleMistakes가 null인 경우")
        fun createWeakAreaAnalysis_nullExampleMistakes() {
            val user = User(username = "testuser", email = "test@example.com")

            val weakArea = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.SPELLING_COMMON,
                pattern = "typos",
                frequency = 3,
                totalOccurrences = 5,
                lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0),
                severity = WeakAreaSeverity.LOW
            )

            assertThat(weakArea.exampleMistakes).isNull()
            assertThat(weakArea.improvementRate).isEqualTo(0.0)
        }

        @Test
        @DisplayName("exampleMistakes JSON 포함")
        fun createWeakAreaAnalysis_withExampleMistakes() {
            val user = User(username = "testuser", email = "test@example.com")
            val examples = """["the cat", "an apple", "a university"]"""

            val weakArea = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.GRAMMAR_ARTICLES,
                pattern = "article errors",
                frequency = 8,
                totalOccurrences = 12,
                lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0),
                severity = WeakAreaSeverity.HIGH,
                exampleMistakes = examples
            )

            assertThat(weakArea.exampleMistakes).isEqualTo(examples)
        }
    }

    @Nested
    @DisplayName("WeakAreaType Enum 테스트")
    inner class WeakAreaTypeTests {

        @Test
        @DisplayName("모든 WeakAreaType 열거값 확인")
        fun checkAllWeakAreaTypes() {
            val types = WeakAreaType.entries

            assertThat(types).hasSize(13)
            assertThat(types).contains(
                WeakAreaType.GRAMMAR_ARTICLES,
                WeakAreaType.GRAMMAR_PREPOSITIONS,
                WeakAreaType.GRAMMAR_TENSES,
                WeakAreaType.GRAMMAR_VERB_FORMS,
                WeakAreaType.GRAMMAR_PLURALS,
                WeakAreaType.GRAMMAR_SUBJECT_VERB,
                WeakAreaType.SPELLING_COMMON,
                WeakAreaType.SPELLING_HOMOPHONES,
                WeakAreaType.STYLE_WORD_CHOICE,
                WeakAreaType.STYLE_SENTENCE_STRUCTURE,
                WeakAreaType.PUNCTUATION_COMMAS,
                WeakAreaType.PUNCTUATION_PERIODS,
                WeakAreaType.OTHER
            )
        }

        @Test
        @DisplayName("WeakAreaType별 분류 생성")
        fun createWithDifferentWeakAreaTypes() {
            val user = User(username = "testuser", email = "test@example.com")
            val lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0)

            val grammarAnalysis = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.GRAMMAR_TENSES,
                pattern = "past tense errors",
                frequency = 4,
                totalOccurrences = 7,
                lastOccurrence = lastOccurrence,
                severity = WeakAreaSeverity.MEDIUM
            )

            val spellingAnalysis = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.SPELLING_HOMOPHONES,
                pattern = "their/there/they're",
                frequency = 2,
                totalOccurrences = 3,
                lastOccurrence = lastOccurrence,
                severity = WeakAreaSeverity.LOW
            )

            assertThat(grammarAnalysis.weakAreaType).isEqualTo(WeakAreaType.GRAMMAR_TENSES)
            assertThat(spellingAnalysis.weakAreaType).isEqualTo(WeakAreaType.SPELLING_HOMOPHONES)
        }
    }

    @Nested
    @DisplayName("WeakAreaSeverity Enum 테스트")
    inner class WeakAreaSeverityTests {

        @Test
        @DisplayName("모든 WeakAreaSeverity 열거값 확인")
        fun checkAllWeakAreaSeverities() {
            val severities = WeakAreaSeverity.entries

            assertThat(severities).hasSize(4)
            assertThat(severities).containsExactly(
                WeakAreaSeverity.LOW,
                WeakAreaSeverity.MEDIUM,
                WeakAreaSeverity.HIGH,
                WeakAreaSeverity.CRITICAL
            )
        }

        @Test
        @DisplayName("각 심각도별 WeakAreaAnalysis 생성")
        fun createWithDifferentSeverities() {
            val user = User(username = "testuser", email = "test@example.com")
            val lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0)

            val lowSeverity = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.PUNCTUATION_PERIODS,
                pattern = "missing period",
                frequency = 1,
                totalOccurrences = 2,
                lastOccurrence = lastOccurrence,
                severity = WeakAreaSeverity.LOW
            )

            val criticalSeverity = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.GRAMMAR_SUBJECT_VERB,
                pattern = "subject-verb mismatch",
                frequency = 15,
                totalOccurrences = 18,
                lastOccurrence = lastOccurrence,
                severity = WeakAreaSeverity.CRITICAL
            )

            assertThat(lowSeverity.severity).isEqualTo(WeakAreaSeverity.LOW)
            assertThat(criticalSeverity.severity).isEqualTo(WeakAreaSeverity.CRITICAL)
        }
    }

    @Nested
    @DisplayName("ImprovementRate 검증")
    inner class ImprovementRateTests {

        @Test
        @DisplayName("improvementRate 범위 검증 (0.0 ~ 1.0)")
        fun improvementRate_validRange() {
            val user = User(username = "testuser", email = "test@example.com")
            val lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0)

            val zeroRate = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.OTHER,
                pattern = "pattern",
                frequency = 5,
                totalOccurrences = 10,
                lastOccurrence = lastOccurrence,
                improvementRate = 0.0,
                severity = WeakAreaSeverity.MEDIUM
            )

            val halfRate = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.OTHER,
                pattern = "pattern",
                frequency = 5,
                totalOccurrences = 10,
                lastOccurrence = lastOccurrence,
                improvementRate = 0.5,
                severity = WeakAreaSeverity.MEDIUM
            )

            val fullRate = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.OTHER,
                pattern = "pattern",
                frequency = 5,
                totalOccurrences = 10,
                lastOccurrence = lastOccurrence,
                improvementRate = 1.0,
                severity = WeakAreaSeverity.MEDIUM
            )

            assertThat(zeroRate.improvementRate).isEqualTo(0.0)
            assertThat(halfRate.improvementRate).isEqualTo(0.5)
            assertThat(fullRate.improvementRate).isEqualTo(1.0)
        }

        @Test
        @DisplayName("기본값 improvementRate는 0.0")
        fun improvementRate_defaultValue() {
            val user = User(username = "testuser", email = "test@example.com")

            val weakArea = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.GRAMMAR_ARTICLES,
                pattern = "articles",
                frequency = 3,
                totalOccurrences = 5,
                lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0),
                severity = WeakAreaSeverity.LOW
            )

            assertThat(weakArea.improvementRate).isEqualTo(0.0)
        }
    }

    @Nested
    @DisplayName("Frequency 및 TotalOccurrences 테스트")
    inner class FrequencyTests {

        @Test
        @DisplayName("frequency와 totalOccurrences 값 검증")
        fun frequency_totalOccurrences() {
            val user = User(username = "testuser", email = "test@example.com")

            val weakArea = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.SPELLING_COMMON,
                pattern = "common spelling",
                frequency = 7,
                totalOccurrences = 20,
                lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0),
                severity = WeakAreaSeverity.MEDIUM
            )

            assertThat(weakArea.frequency).isEqualTo(7)
            assertThat(weakArea.totalOccurrences).isEqualTo(20)
        }

        @Test
        @DisplayName("frequency는 totalOccurrences보다 작거나 같아야 함")
        fun frequency_shouldBeLessThanOrEqualToTotal() {
            val user = User(username = "testuser", email = "test@example.com")

            val weakArea = WeakAreaAnalysis(
                user = user,
                weakAreaType = WeakAreaType.GRAMMAR_ARTICLES,
                pattern = "articles",
                frequency = 5,
                totalOccurrences = 5,
                lastOccurrence = LocalDateTime.of(2025, 12, 25, 10, 0),
                severity = WeakAreaSeverity.HIGH
            )

            assertThat(weakArea.frequency).isLessThanOrEqualTo(weakArea.totalOccurrences)
        }
    }

    @Nested
    @DisplayName("UserWeakAreasSummary 데이터 클래스")
    inner class UserWeakAreasSummaryTests {

        @Test
        @DisplayName("UserWeakAreasSummary 생성")
        fun createUserWeakAreasSummary() {
            val weakAreaInfo = WeakAreaInfo(
                type = WeakAreaType.GRAMMAR_ARTICLES,
                typeDisplay = "관사 오류",
                pattern = "missing articles",
                frequency = 5,
                severity = WeakAreaSeverity.MEDIUM,
                improvementRate = 0.3,
                exampleMistakes = listOf("cat", "dog"),
                recommendation = "관사 사용 규칙 복습"
            )

            val summary = UserWeakAreasSummary(
                userId = 1L,
                topWeakAreas = listOf(weakAreaInfo),
                overallImprovementRate = 0.25,
                recommendedFocus = WeakAreaType.GRAMMAR_ARTICLES,
                totalMistakes = 10,
                analysisDate = LocalDateTime.of(2025, 12, 25, 10, 0)
            )

            assertThat(summary.userId).isEqualTo(1L)
            assertThat(summary.topWeakAreas).hasSize(1)
            assertThat(summary.overallImprovementRate).isEqualTo(0.25)
            assertThat(summary.recommendedFocus).isEqualTo(WeakAreaType.GRAMMAR_ARTICLES)
            assertThat(summary.totalMistakes).isEqualTo(10)
        }

        @Test
        @DisplayName("recommendedFocus가 null인 경우")
        fun userWeakAreasSummary_nullRecommendedFocus() {
            val summary = UserWeakAreasSummary(
                userId = 1L,
                topWeakAreas = emptyList(),
                overallImprovementRate = 1.0,
                recommendedFocus = null,
                totalMistakes = 0,
                analysisDate = LocalDateTime.of(2025, 12, 25, 10, 0)
            )

            assertThat(summary.recommendedFocus).isNull()
            assertThat(summary.topWeakAreas).isEmpty()
        }
    }

    @Nested
    @DisplayName("WeakAreaInfo 데이터 클래스")
    inner class WeakAreaInfoTests {

        @Test
        @DisplayName("WeakAreaInfo 생성 및 검증")
        fun createWeakAreaInfo() {
            val info = WeakAreaInfo(
                type = WeakAreaType.GRAMMAR_PREPOSITIONS,
                typeDisplay = "전치사 오류",
                pattern = "wrong preposition",
                frequency = 8,
                severity = WeakAreaSeverity.HIGH,
                improvementRate = 0.15,
                exampleMistakes = listOf("in Monday", "on the morning", "at Christmas"),
                recommendation = "전치사 시간/장소 규칙 학습"
            )

            assertThat(info.type).isEqualTo(WeakAreaType.GRAMMAR_PREPOSITIONS)
            assertThat(info.typeDisplay).isEqualTo("전치사 오류")
            assertThat(info.pattern).isEqualTo("wrong preposition")
            assertThat(info.frequency).isEqualTo(8)
            assertThat(info.severity).isEqualTo(WeakAreaSeverity.HIGH)
            assertThat(info.improvementRate).isEqualTo(0.15)
            assertThat(info.exampleMistakes).hasSize(3)
            assertThat(info.recommendation).isEqualTo("전치사 시간/장소 규칙 학습")
        }

        @Test
        @DisplayName("빈 exampleMistakes 리스트")
        fun weakAreaInfo_emptyExampleMistakes() {
            val info = WeakAreaInfo(
                type = WeakAreaType.OTHER,
                typeDisplay = "기타",
                pattern = "pattern",
                frequency = 1,
                severity = WeakAreaSeverity.LOW,
                improvementRate = 0.5,
                exampleMistakes = emptyList(),
                recommendation = "복습 필요"
            )

            assertThat(info.exampleMistakes).isEmpty()
        }
    }
}
