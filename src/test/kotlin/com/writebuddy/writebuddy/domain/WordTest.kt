package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Word 도메인 테스트")
class WordTest {

    @Nested
    @DisplayName("Word 생성")
    inner class WordCreation {

        @Test
        @DisplayName("기본 속성으로 Word를 생성할 수 있다")
        fun createWord_withBasicProperties() {
            val word = Word(
                word = "beautiful",
                meaning = "아름다운",
                difficulty = 6,
                tags = mutableSetOf("형용사", "감정"),
                category = com.writebuddy.writebuddy.domain.WordCategory.DAILY
            )

            assertThat(word.word).isEqualTo("beautiful")
            assertThat(word.meaning).isEqualTo("아름다운")
            assertThat(word.difficulty).isEqualTo(6)
            assertThat(word.tags).containsExactlyInAnyOrder("형용사", "감정")
            assertThat(word.category).isEqualTo(com.writebuddy.writebuddy.domain.WordCategory.DAILY)
            assertThat(word.isAiGenerated).isTrue()
        }

        @Test
        @DisplayName("태그가 없는 Word를 생성할 수 있다")
        fun createWord_withoutTags() {
            val word = Word(
                word = "hello",
                meaning = "안녕",
                tags = mutableSetOf()
            )

            assertThat(word.tags).isEmpty()
            assertThat(word.difficulty).isEqualTo(5) // 기본값
            assertThat(word.category).isEqualTo(com.writebuddy.writebuddy.domain.WordCategory.GENERAL) // 기본값
        }

        @Test
        @DisplayName("수동으로 생성된 Word를 만들 수 있다")
        fun createWord_manuallyGenerated() {
            val word = Word(
                word = "custom",
                meaning = "사용자 정의",
                isAiGenerated = false
            )

            assertThat(word.isAiGenerated).isFalse()
        }
    }

    @Nested
    @DisplayName("Word 카테고리")
    inner class WordCategory {

        @Test
        @DisplayName("모든 카테고리가 정의되어 있다")
        fun allCategoriesDefined() {
            val categories = com.writebuddy.writebuddy.domain.WordCategory.values()

            assertThat(categories).hasSize(7)
            assertThat(categories).containsExactlyInAnyOrder(
                com.writebuddy.writebuddy.domain.WordCategory.GRAMMAR,
                com.writebuddy.writebuddy.domain.WordCategory.BUSINESS,
                com.writebuddy.writebuddy.domain.WordCategory.ACADEMIC,
                com.writebuddy.writebuddy.domain.WordCategory.DAILY,
                com.writebuddy.writebuddy.domain.WordCategory.TRAVEL,
                com.writebuddy.writebuddy.domain.WordCategory.TECHNOLOGY,
                com.writebuddy.writebuddy.domain.WordCategory.GENERAL
            )
        }

        @Test
        @DisplayName("카테고리별로 Word를 생성할 수 있다")
        fun createWordWithDifferentCategories() {
            val grammarWord = Word(
                word = "although",
                meaning = "비록 ~이지만",
                category = com.writebuddy.writebuddy.domain.WordCategory.GRAMMAR
            )

            val businessWord = Word(
                word = "profit",
                meaning = "이익",
                category = com.writebuddy.writebuddy.domain.WordCategory.BUSINESS
            )

            assertThat(grammarWord.category).isEqualTo(com.writebuddy.writebuddy.domain.WordCategory.GRAMMAR)
            assertThat(businessWord.category).isEqualTo(com.writebuddy.writebuddy.domain.WordCategory.BUSINESS)
        }
    }

    @Nested
    @DisplayName("태그 관리")
    inner class TagManagement {

        @Test
        @DisplayName("태그를 추가할 수 있다")
        fun addTags() {
            val word = Word(
                word = "excellent",
                meaning = "뛰어난",
                tags = mutableSetOf("형용사")
            )

            word.tags.add("품질")
            word.tags.add("평가")

            assertThat(word.tags).containsExactlyInAnyOrder("형용사", "품질", "평가")
        }

        @Test
        @DisplayName("중복된 태그는 추가되지 않는다")
        fun duplicateTagsNotAdded() {
            val word = Word(
                word = "great",
                meaning = "훌륭한",
                tags = mutableSetOf("형용사")
            )

            word.tags.add("형용사")
            word.tags.add("좋은")

            assertThat(word.tags).containsExactlyInAnyOrder("형용사", "좋은")
            assertThat(word.tags).hasSize(2)
        }
    }

    @Nested
    @DisplayName("난이도 검증")
    inner class DifficultyValidation {

        @Test
        @DisplayName("1-10 범위의 난이도를 설정할 수 있다")
        fun validDifficultyRange() {
            val easyWord = Word(word = "cat", meaning = "고양이", difficulty = 1)
            val hardWord = Word(word = "sophisticated", meaning = "정교한", difficulty = 10)

            assertThat(easyWord.difficulty).isEqualTo(1)
            assertThat(hardWord.difficulty).isEqualTo(10)
        }

        @Test
        @DisplayName("기본 난이도는 5이다")
        fun defaultDifficulty() {
            val word = Word(word = "test", meaning = "테스트")

            assertThat(word.difficulty).isEqualTo(5)
        }
    }
}