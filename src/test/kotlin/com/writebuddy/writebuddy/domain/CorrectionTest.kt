package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Correction 도메인 테스트")
class CorrectionTest {

    @Nested
    @DisplayName("Correction 객체 생성")
    inner class CorrectionCreation {

        @Test
        @DisplayName("유효한 값으로 Correction 객체를 생성할 수 있다")
        fun createCorrection_validValues() {
            val originSentence = "hello world"
            val correctedSentence = "Hello, world!"
            val feedback = "대문자로 시작하고 쉼표를 추가해야 합니다."
            val feedbackType = FeedbackType.GRAMMAR

            val correction = Correction(
                originSentence = originSentence,
                correctedSentence = correctedSentence,
                feedback = feedback,
                feedbackType = feedbackType
            )

            assertThat(correction.originSentence).isEqualTo(originSentence)
            assertThat(correction.correctedSentence).isEqualTo(correctedSentence)
            assertThat(correction.feedback).isEqualTo(feedback)
            assertThat(correction.feedbackType).isEqualTo(feedbackType)
            assertThat(correction.id).isEqualTo(0L)
        }

        @Test
        @DisplayName("기본 피드백 타입은 GRAMMAR이다")
        fun createCorrection_defaultFeedbackType() {
            val correction = Correction(
                originSentence = "test sentence",
                correctedSentence = "Test sentence.",
                feedback = "첫 글자를 대문자로 써야 합니다."
            )

            assertThat(correction.feedbackType).isEqualTo(FeedbackType.GRAMMAR)
        }

        @Test
        @DisplayName("모든 피드백 타입으로 Correction을 생성할 수 있다")
        fun createCorrection_allFeedbackTypes() {
            val corrections = FeedbackType.entries.map { feedbackType ->
                Correction(
                    originSentence = "test",
                    correctedSentence = "Test",
                    feedback = "피드백",
                    feedbackType = feedbackType
                )
            }

            assertThat(corrections).hasSize(5)
            assertThat(corrections).extracting("feedbackType")
                .containsExactly(
                    FeedbackType.GRAMMAR,
                    FeedbackType.SPELLING,
                    FeedbackType.STYLE,
                    FeedbackType.PUNCTUATION,
                    FeedbackType.SYSTEM
                )
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    inner class BoundaryValueTests {

        @Test
        @DisplayName("빈 문자열로도 Correction을 생성할 수 있다")
        fun createCorrection_emptyStrings() {
            val correction = Correction(
                originSentence = "",
                correctedSentence = "",
                feedback = ""
            )

            assertThat(correction.originSentence).isEmpty()
            assertThat(correction.correctedSentence).isEmpty()
            assertThat(correction.feedback).isEmpty()
        }

        @Test
        @DisplayName("긴 문장으로도 Correction을 생성할 수 있다")
        fun createCorrection_longSentences() {
            val longSentence = "a".repeat(1000)
            val longFeedback = "feedback ".repeat(100)

            val correction = Correction(
                originSentence = longSentence,
                correctedSentence = longSentence.uppercase(),
                feedback = longFeedback
            )

            assertThat(correction.originSentence).hasSize(1000)
            assertThat(correction.feedback).contains("feedback")
        }
    }

    @Nested
    @DisplayName("BaseEntity 상속 테스트")
    inner class BaseEntityTests {

        @Test
        @DisplayName("Correction은 BaseEntity를 상속받는다")
        fun correction_inheritsBaseEntity() {
            val correction = Correction(
                originSentence = "test",
                correctedSentence = "Test",
                feedback = "피드백"
            )

            assertThat(correction).isInstanceOf(BaseEntity::class.java)
        }
    }
}