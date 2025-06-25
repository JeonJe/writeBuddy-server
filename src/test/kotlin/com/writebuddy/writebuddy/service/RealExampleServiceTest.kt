package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import com.writebuddy.writebuddy.repository.RealExampleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
@DisplayName("RealExampleService 테스트")
class RealExampleServiceTest {

    @MockBean
    private lateinit var realExampleRepository: RealExampleRepository

    @Autowired
    private lateinit var realExampleService: RealExampleService

    @Nested
    @DisplayName("키워드 추출 기능")
    inner class KeywordExtraction {

        @Test
        @DisplayName("교정된 문장에서 주요 키워드를 추출한다")
        fun extractKeywordsFromCorrectedSentence() {
            // given
            val correctedSentence = "I want to go to the store today"
            val mockExamples = listOf(
                createMockExample("want", ExampleSourceType.MOVIE),
                createMockExample("store", ExampleSourceType.NEWS)
            )
            
            given(realExampleRepository.searchByKeyword("want")).willReturn(mockExamples.take(1))
            given(realExampleRepository.searchByKeyword("store")).willReturn(mockExamples.drop(1))
            given(realExampleRepository.searchByKeyword("today")).willReturn(emptyList())

            // when
            val result = realExampleService.findRelatedExamples(correctedSentence)

            // then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.phrase }).containsExactlyInAnyOrder(
                "I want to see you",
                "Go to the store"
            )
        }

        @Test
        @DisplayName("짧은 단어는 키워드에서 제외된다")
        fun shortWordsAreExcludedFromKeywords() {
            // given
            val correctedSentence = "I am a student"
            val mockExamples = listOf(
                createMockExample("student", ExampleSourceType.BOOK)
            )
            
            given(realExampleRepository.searchByKeyword("student")).willReturn(mockExamples)

            // when
            val result = realExampleService.findRelatedExamples(correctedSentence)

            // then
            assertThat(result).hasSize(1)
            assertThat(result.first().phrase).isEqualTo("I am a student")
        }

        @Test
        @DisplayName("최대 3개의 관련 예시를 반환한다")
        fun returnsMaximumThreeRelatedExamples() {
            // given
            val correctedSentence = "Please help me understand this concept"
            val mockExamples = (1..5).map { 
                createMockExample("example$it", ExampleSourceType.BOOK)
            }
            
            whenever(realExampleRepository.searchByKeyword("Please")).thenReturn(mockExamples.take(2))
            whenever(realExampleRepository.searchByKeyword("help")).thenReturn(mockExamples.drop(2).take(2))
            whenever(realExampleRepository.searchByKeyword("understand")).thenReturn(mockExamples.drop(4))

            // when
            val result = realExampleService.findRelatedExamples(correctedSentence)

            // then
            assertThat(result).hasSize(3)
        }
    }

    @Nested
    @DisplayName("랜덤 예시 조회")
    inner class RandomExamples {

        @Test
        @DisplayName("지정된 개수만큼 랜덤 예시를 반환한다")
        fun returnsSpecifiedNumberOfRandomExamples() {
            // given
            val count = 3
            val mockExamples = (1..5).map { 
                createMockExample("example$it", ExampleSourceType.MOVIE, isVerified = true)
            }
            
            whenever(realExampleRepository.findRandomVerifiedExamples()).thenReturn(mockExamples)

            // when
            val result = realExampleService.getRandomExamples(count)

            // then
            assertThat(result).hasSize(count)
        }

        @Test
        @DisplayName("기본값으로 5개의 랜덤 예시를 반환한다")
        fun returnsDefaultFiveRandomExamples() {
            // given
            val mockExamples = (1..7).map { 
                createMockExample("example$it", ExampleSourceType.SONG, isVerified = true)
            }
            
            whenever(realExampleRepository.findRandomVerifiedExamples()).thenReturn(mockExamples)

            // when
            val result = realExampleService.getRandomExamples()

            // then
            assertThat(result).hasSize(5)
        }
    }

    @Nested
    @DisplayName("일일 추천 예시")
    inner class DailyRecommendation {

        @Test
        @DisplayName("다양한 출처에서 균형있게 추천 예시를 제공한다")
        fun provideBalancedRecommendationsFromVariousSources() {
            // given
            val movieExamples = listOf(createMockExample("movie1", ExampleSourceType.MOVIE))
            val songExamples = listOf(createMockExample("song1", ExampleSourceType.SONG))
            val newsExamples = listOf(createMockExample("news1", ExampleSourceType.NEWS))
            
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.MOVIE)).thenReturn(movieExamples)
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.SONG)).thenReturn(songExamples)
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.NEWS)).thenReturn(newsExamples)
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.BOOK)).thenReturn(emptyList())
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.INTERVIEW)).thenReturn(emptyList())
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.SOCIAL)).thenReturn(emptyList())
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.SPEECH)).thenReturn(emptyList())
            whenever(realExampleRepository.findBySourceType(ExampleSourceType.PODCAST)).thenReturn(emptyList())

            // when
            val result = realExampleService.getDailyRecommendation()

            // then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.sourceType }).containsExactlyInAnyOrder(
                ExampleSourceType.MOVIE,
                ExampleSourceType.SONG,
                ExampleSourceType.NEWS
            )
        }

        @Test
        @DisplayName("최대 5개의 추천 예시를 반환한다")
        fun returnsMaximumFiveRecommendations() {
            // given
            ExampleSourceType.values().forEach { sourceType ->
                val examples = listOf(createMockExample("${sourceType.name.lowercase()}1", sourceType))
                whenever(realExampleRepository.findBySourceType(sourceType)).thenReturn(examples)
            }

            // when
            val result = realExampleService.getDailyRecommendation()

            // then
            assertThat(result).hasSize(5)
        }
    }

    @Nested
    @DisplayName("출처 타입별 조회")
    inner class SourceTypeQuery {

        @Test
        @DisplayName("특정 출처 타입의 예시를 조회한다")
        fun findExamplesBySpecificSourceType() {
            // given
            val sourceType = ExampleSourceType.INTERVIEW
            val mockExamples = listOf(
                createMockExample("interview1", sourceType),
                createMockExample("interview2", sourceType)
            )
            
            whenever(realExampleRepository.findBySourceType(sourceType)).thenReturn(mockExamples)

            // when
            val result = realExampleService.getExamplesBySourceType(sourceType)

            // then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.sourceType }).allMatch { it == sourceType }
        }
    }

    @Nested
    @DisplayName("난이도별 조회")
    inner class DifficultyQuery {

        @Test
        @DisplayName("지정된 난이도 범위의 예시를 조회한다")
        fun findExamplesByDifficultyRange() {
            // given
            val minDifficulty = 3
            val maxDifficulty = 7
            val mockExamples = listOf(
                createMockExampleWithDifficulty("example1", 3),
                createMockExampleWithDifficulty("example2", 5),
                createMockExampleWithDifficulty("example3", 7)
            )
            
            whenever(realExampleRepository.findByDifficultyBetween(minDifficulty, maxDifficulty))
                .thenReturn(mockExamples)

            // when
            val result = realExampleService.getExamplesByDifficulty(minDifficulty, maxDifficulty)

            // then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.difficulty }).allMatch { it in minDifficulty..maxDifficulty }
        }
    }

    private fun createMockExample(
        phrase: String, 
        sourceType: ExampleSourceType,
        isVerified: Boolean = true
    ): RealExample {
        return RealExample(
            phrase = phrase,
            source = "Mock Source",
            sourceType = sourceType,
            context = "Mock context",
            isVerified = isVerified
        )
    }

    private fun createMockExampleWithDifficulty(phrase: String, difficulty: Int): RealExample {
        return RealExample(
            phrase = phrase,
            source = "Mock Source",
            sourceType = ExampleSourceType.BOOK,
            context = "Mock context",
            difficulty = difficulty
        )
    }
}