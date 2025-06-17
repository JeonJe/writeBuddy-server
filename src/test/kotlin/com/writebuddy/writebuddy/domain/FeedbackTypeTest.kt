package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("FeedbackType Enum 테스트")
class FeedbackTypeTest {

    @Nested
    @DisplayName("Enum 값 검증")
    inner class EnumValueTests {

        @Test
        @DisplayName("모든 FeedbackType 값이 정의되어 있다")
        fun feedbackType_allValuesExist() {
            val feedbackTypes = FeedbackType.entries

            assertThat(feedbackTypes).hasSize(5)
            assertThat(feedbackTypes).containsExactly(
                FeedbackType.GRAMMAR,
                FeedbackType.SPELLING,
                FeedbackType.STYLE,
                FeedbackType.PUNCTUATION,
                FeedbackType.SYSTEM
            )
        }

        @Test
        @DisplayName("각 FeedbackType의 이름이 올바르다")
        fun feedbackType_namesAreCorrect() {
            assertThat(FeedbackType.GRAMMAR.name).isEqualTo("GRAMMAR")
            assertThat(FeedbackType.SPELLING.name).isEqualTo("SPELLING")
            assertThat(FeedbackType.STYLE.name).isEqualTo("STYLE")
            assertThat(FeedbackType.PUNCTUATION.name).isEqualTo("PUNCTUATION")
            assertThat(FeedbackType.SYSTEM.name).isEqualTo("SYSTEM")
        }
    }

    @Nested
    @DisplayName("Enum 변환 테스트")
    inner class EnumConversionTests {

        @Test
        @DisplayName("문자열로부터 FeedbackType을 생성할 수 있다")
        fun valueOf_validString() {
            assertThat(FeedbackType.valueOf("GRAMMAR")).isEqualTo(FeedbackType.GRAMMAR)
            assertThat(FeedbackType.valueOf("SPELLING")).isEqualTo(FeedbackType.SPELLING)
            assertThat(FeedbackType.valueOf("STYLE")).isEqualTo(FeedbackType.STYLE)
            assertThat(FeedbackType.valueOf("PUNCTUATION")).isEqualTo(FeedbackType.PUNCTUATION)
            assertThat(FeedbackType.valueOf("SYSTEM")).isEqualTo(FeedbackType.SYSTEM)
        }

        @Test
        @DisplayName("ordinal 값이 예상과 일치한다")
        fun ordinal_valuesAreCorrect() {
            assertThat(FeedbackType.GRAMMAR.ordinal).isEqualTo(0)
            assertThat(FeedbackType.SPELLING.ordinal).isEqualTo(1)
            assertThat(FeedbackType.STYLE.ordinal).isEqualTo(2)
            assertThat(FeedbackType.PUNCTUATION.ordinal).isEqualTo(3)
            assertThat(FeedbackType.SYSTEM.ordinal).isEqualTo(4)
        }
    }

    @Nested
    @DisplayName("비즈니스 로직 테스트")
    inner class BusinessLogicTests {

        @Test
        @DisplayName("기본 피드백 타입은 GRAMMAR이다")
        fun defaultFeedbackType_isGrammar() {
            val defaultType = FeedbackType.GRAMMAR
            
            assertThat(defaultType).isEqualTo(FeedbackType.entries.first())
        }

        @Test
        @DisplayName("SYSTEM 타입은 fallback 용도로 사용된다")
        fun systemType_isFallback() {
            val systemType = FeedbackType.SYSTEM
            
            assertThat(systemType).isIn(FeedbackType.entries)
            assertThat(systemType.name).isEqualTo("SYSTEM")
        }
    }
}