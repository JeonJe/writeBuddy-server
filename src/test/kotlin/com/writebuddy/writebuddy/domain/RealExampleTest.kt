package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("RealExample 도메인 테스트")
class RealExampleTest {

    @Nested
    @DisplayName("실제 사용 예시 생성")
    inner class CreateRealExample {

        @Test
        @DisplayName("유효한 정보로 실제 사용 예시를 생성한다")
        fun createRealExampleWithValidData() {
            // given
            val phrase = "I couldn't agree more"
            val source = "Friends (TV Show)"
            val sourceType = ExampleSourceType.MOVIE
            val context = "Ross agrees enthusiastically with Rachel's opinion"
            val url = "https://www.youtube.com/watch?v=example"
            val timestamp = "05:23"
            val difficulty = 6
            val tags = "agreement, enthusiasm, conversation"
            val isVerified = true

            // when
            val realExample = RealExample(
                phrase = phrase,
                source = source,
                sourceType = sourceType,
                context = context,
                url = url,
                timestamp = timestamp,
                difficulty = difficulty,
                tags = tags,
                isVerified = isVerified
            )

            // then
            assertThat(realExample.phrase).isEqualTo(phrase)
            assertThat(realExample.source).isEqualTo(source)
            assertThat(realExample.sourceType).isEqualTo(sourceType)
            assertThat(realExample.context).isEqualTo(context)
            assertThat(realExample.url).isEqualTo(url)
            assertThat(realExample.timestamp).isEqualTo(timestamp)
            assertThat(realExample.difficulty).isEqualTo(difficulty)
            assertThat(realExample.tags).isEqualTo(tags)
            assertThat(realExample.isVerified).isEqualTo(isVerified)
        }

        @Test
        @DisplayName("필수 정보만으로 실제 사용 예시를 생성한다")
        fun createRealExampleWithRequiredDataOnly() {
            // given
            val phrase = "Break a leg"
            val source = "Hamilton (Musical)"
            val sourceType = ExampleSourceType.SONG
            val context = "Actor wishing good luck to another performer"

            // when
            val realExample = RealExample(
                phrase = phrase,
                source = source,
                sourceType = sourceType,
                context = context
            )

            // then
            assertThat(realExample.phrase).isEqualTo(phrase)
            assertThat(realExample.source).isEqualTo(source)
            assertThat(realExample.sourceType).isEqualTo(sourceType)
            assertThat(realExample.context).isEqualTo(context)
            assertThat(realExample.url).isNull()
            assertThat(realExample.timestamp).isNull()
            assertThat(realExample.difficulty).isEqualTo(5) // 기본값
            assertThat(realExample.tags).isNull()
            assertThat(realExample.isVerified).isFalse() // 기본값
        }
    }

    @Nested
    @DisplayName("출처 타입별 특성")
    inner class SourceTypeCharacteristics {

        @Test
        @DisplayName("영화/드라마 출처 타입의 표시명과 이모지를 확인한다")
        fun movieSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.MOVIE

            // then
            assertThat(sourceType.displayName).isEqualTo("영화/드라마")
            assertThat(sourceType.emoji).isEqualTo("🎬")
        }

        @Test
        @DisplayName("음악/가사 출처 타입의 표시명과 이모지를 확인한다")
        fun songSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.SONG

            // then
            assertThat(sourceType.displayName).isEqualTo("음악/가사")
            assertThat(sourceType.emoji).isEqualTo("🎵")
        }

        @Test
        @DisplayName("뉴스/기사 출처 타입의 표시명과 이모지를 확인한다")
        fun newsSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.NEWS

            // then
            assertThat(sourceType.displayName).isEqualTo("뉴스/기사")
            assertThat(sourceType.emoji).isEqualTo("📰")
        }

        @Test
        @DisplayName("문학/도서 출처 타입의 표시명과 이모지를 확인한다")
        fun bookSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.BOOK

            // then
            assertThat(sourceType.displayName).isEqualTo("문학/도서")
            assertThat(sourceType.emoji).isEqualTo("📚")
        }

        @Test
        @DisplayName("인터뷰 출처 타입의 표시명과 이모지를 확인한다")
        fun interviewSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.INTERVIEW

            // then
            assertThat(sourceType.displayName).isEqualTo("인터뷰")
            assertThat(sourceType.emoji).isEqualTo("🎤")
        }

        @Test
        @DisplayName("소셜미디어 출처 타입의 표시명과 이모지를 확인한다")
        fun socialSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.SOCIAL

            // then
            assertThat(sourceType.displayName).isEqualTo("소셜미디어")
            assertThat(sourceType.emoji).isEqualTo("📱")
        }

        @Test
        @DisplayName("연설/강연 출처 타입의 표시명과 이모지를 확인한다")
        fun speechSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.SPEECH

            // then
            assertThat(sourceType.displayName).isEqualTo("연설/강연")
            assertThat(sourceType.emoji).isEqualTo("🎙️")
        }

        @Test
        @DisplayName("팟캐스트 출처 타입의 표시명과 이모지를 확인한다")
        fun podcastSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.PODCAST

            // then
            assertThat(sourceType.displayName).isEqualTo("팟캐스트")
            assertThat(sourceType.emoji).isEqualTo("🎧")
        }

        @Test
        @DisplayName("기타 출처 타입의 표시명과 이모지를 확인한다")
        fun otherSourceTypeDisplayAndEmoji() {
            // given & when
            val sourceType = ExampleSourceType.OTHER

            // then
            assertThat(sourceType.displayName).isEqualTo("기타")
            assertThat(sourceType.emoji).isEqualTo("📄")
        }

        @Test
        @DisplayName("모든 출처 타입이 정의되어 있다")
        fun allSourceTypesAreDefined() {
            // given & when
            val sourceTypes = ExampleSourceType.values()

            // then
            assertThat(sourceTypes).hasSize(9)
            assertThat(sourceTypes).containsExactlyInAnyOrder(
                ExampleSourceType.MOVIE,
                ExampleSourceType.SONG,
                ExampleSourceType.NEWS,
                ExampleSourceType.BOOK,
                ExampleSourceType.INTERVIEW,
                ExampleSourceType.SOCIAL,
                ExampleSourceType.SPEECH,
                ExampleSourceType.PODCAST,
                ExampleSourceType.OTHER
            )
        }
    }

    @Nested
    @DisplayName("난이도 설정")
    inner class DifficultySettings {

        @Test
        @DisplayName("난이도 범위를 확인한다")
        fun difficultyRange() {
            // given
            val beginnerExample = RealExample(
                phrase = "Hello world",
                source = "Basic English",
                sourceType = ExampleSourceType.BOOK,
                context = "Simple greeting",
                difficulty = 1
            )

            val advancedExample = RealExample(
                phrase = "Notwithstanding the aforementioned circumstances",
                source = "Legal Document",
                sourceType = ExampleSourceType.NEWS,
                context = "Complex legal text",
                difficulty = 10
            )

            // then
            assertThat(beginnerExample.difficulty).isEqualTo(1)
            assertThat(advancedExample.difficulty).isEqualTo(10)
        }

        @Test
        @DisplayName("기본 난이도는 5이다")
        fun defaultDifficultyIsFive() {
            // given & when
            val example = RealExample(
                phrase = "How are you?",
                source = "Everyday Conversation",
                sourceType = ExampleSourceType.SOCIAL,
                context = "Common greeting"
            )

            // then
            assertThat(example.difficulty).isEqualTo(5)
        }
    }

    @Nested
    @DisplayName("검증 상태")
    inner class VerificationStatus {

        @Test
        @DisplayName("기본적으로 검증되지 않은 상태이다")
        fun defaultIsNotVerified() {
            // given & when
            val example = RealExample(
                phrase = "See you later",
                source = "Casual Conversation",
                sourceType = ExampleSourceType.SOCIAL,
                context = "Farewell expression"
            )

            // then
            assertThat(example.isVerified).isFalse()
        }

        @Test
        @DisplayName("검증된 예시로 설정할 수 있다")
        fun canSetAsVerified() {
            // given & when
            val example = RealExample(
                phrase = "Once upon a time",
                source = "Classic Fairy Tales",
                sourceType = ExampleSourceType.BOOK,
                context = "Traditional story opening",
                isVerified = true
            )

            // then
            assertThat(example.isVerified).isTrue()
        }
    }
}