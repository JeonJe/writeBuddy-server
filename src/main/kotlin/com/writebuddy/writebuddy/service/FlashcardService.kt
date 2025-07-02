package com.writebuddy.writebuddy.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.writebuddy.writebuddy.domain.Flashcard
import com.writebuddy.writebuddy.domain.MemoryStatus
import com.writebuddy.writebuddy.domain.Word
import com.writebuddy.writebuddy.domain.WordCategory
import com.writebuddy.writebuddy.repository.FlashcardRepository
import com.writebuddy.writebuddy.repository.UserRepository
import com.writebuddy.writebuddy.repository.WordRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class FlashcardService(
    private val flashcardRepository: FlashcardRepository,
    private val wordRepository: WordRepository,
    private val userRepository: UserRepository,
    private val openAiClient: OpenAiClient,
    private val promptManager: PromptManager,
    private val objectMapper: ObjectMapper
) {
    private val logger: Logger = LoggerFactory.getLogger(FlashcardService::class.java)

    fun createFlashcard(userId: Long, wordText: String): Flashcard {
        logger.info("Creating flashcard for user {} with word: {}", userId, wordText)
        
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        
        // 기존 단어가 있는지 확인
        val word = wordRepository.findByWord(wordText) ?: run {
            logger.info("Word not found, generating with AI: {}", wordText)
            generateWordWithAi(wordText)
        }
        
        // 이미 해당 사용자가 가진 플래시카드인지 확인
        flashcardRepository.findByUserAndWord(user, word)?.let {
            throw IllegalArgumentException("Flashcard already exists for this word")
        }
        
        val flashcard = Flashcard(
            user = user,
            word = word
        )
        
        return flashcardRepository.save(flashcard)
    }
    
    private fun generateWordWithAi(wordText: String): Word {
        val systemPrompt = promptManager.getFlashcardGenerationSystemPrompt()
        val userPrompt = promptManager.getFlashcardGenerationUserPrompt(wordText)
        
        return try {
            val response = openAiClient.sendChatRequest(systemPrompt, userPrompt)
            parseWordFromAiResponse(wordText, response)
        } catch (e: Exception) {
            logger.error("Failed to generate word with AI: {}", e.message)
            createFallbackWord(wordText)
        }
    }
    
    private fun parseWordFromAiResponse(wordText: String, response: String): Word {
        return try {
            val jsonNode = objectMapper.readTree(response)
            
            val meaning = jsonNode.get("meaning")?.asText() 
                ?: throw IllegalArgumentException("Missing meaning in AI response")
            val difficulty = jsonNode.get("difficulty")?.asInt() ?: 5
            val categoryText = jsonNode.get("category")?.asText() ?: "GENERAL"
            val category = WordCategory.valueOf(categoryText)
            
            val tags = mutableSetOf<String>()
            jsonNode.get("tags")?.forEach { tagNode ->
                tags.add(tagNode.asText())
            }
            
            val word = Word(
                word = wordText,
                meaning = meaning,
                difficulty = difficulty,
                tags = tags,
                category = category,
                isAiGenerated = true
            )
            
            wordRepository.save(word)
        } catch (e: Exception) {
            logger.error("Failed to parse AI response for word {}: {}", wordText, e.message)
            createFallbackWord(wordText)
        }
    }
    
    private fun createFallbackWord(wordText: String): Word {
        val word = Word(
            word = wordText,
            meaning = "의미를 추가해주세요",
            difficulty = 5,
            tags = mutableSetOf("기본"),
            category = WordCategory.GENERAL,
            isAiGenerated = false
        )
        return wordRepository.save(word)
    }
    
    @Transactional(readOnly = true)
    fun getFlashcards(userId: Long, memoryStatus: MemoryStatus?, pageable: Pageable): Page<Flashcard> {
        return if (memoryStatus != null) {
            flashcardRepository.findByUserIdAndMemoryStatus(userId, memoryStatus, pageable)
        } else {
            flashcardRepository.findByUserId(userId, pageable)
        }
    }
    
    @Transactional(readOnly = true)
    fun getFlashcardsForReview(userId: Long, pageable: Pageable): Page<Flashcard> {
        return flashcardRepository.findByUserIdAndNextReviewAtBeforeOrNull(userId, LocalDateTime.now(), pageable)
    }
    
    @Transactional(readOnly = true)
    fun getFavoriteFlashcards(userId: Long, pageable: Pageable): Page<Flashcard> {
        return flashcardRepository.findByUserIdAndIsFavoriteTrue(userId, pageable)
    }
    
    fun reviewFlashcard(flashcardId: Long, isCorrect: Boolean): Flashcard {
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { IllegalArgumentException("Flashcard not found: $flashcardId") }
        
        if (isCorrect) {
            flashcard.markAsCorrect()
        } else {
            flashcard.markAsIncorrect()
        }
        
        return flashcardRepository.save(flashcard)
    }
    
    fun toggleFavorite(flashcardId: Long): Flashcard {
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { IllegalArgumentException("Flashcard not found: $flashcardId") }
        
        flashcard.isFavorite = !flashcard.isFavorite
        return flashcardRepository.save(flashcard)
    }
    
    fun updateNote(flashcardId: Long, note: String?): Flashcard {
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { IllegalArgumentException("Flashcard not found: $flashcardId") }
        
        flashcard.personalNote = note
        return flashcardRepository.save(flashcard)
    }
    
    fun deleteFlashcard(flashcardId: Long) {
        if (!flashcardRepository.existsById(flashcardId)) {
            throw IllegalArgumentException("Flashcard not found: $flashcardId")
        }
        flashcardRepository.deleteById(flashcardId)
    }
    
    @Transactional(readOnly = true)
    fun getFlashcardStatistics(userId: Long): FlashcardStatistics {
        val totalCount = flashcardRepository.countByUserId(userId)
        val masteredCount = flashcardRepository.countByUserIdAndMemoryStatus(userId, MemoryStatus.MASTERED)
        val reviewingCount = flashcardRepository.countByUserIdAndMemoryStatus(userId, MemoryStatus.REVIEWING)
        val learningCount = flashcardRepository.countByUserIdAndMemoryStatus(userId, MemoryStatus.LEARNING)
        val strugglingCount = flashcardRepository.countByUserIdAndMemoryStatus(userId, MemoryStatus.STRUGGLING)
        val newCount = flashcardRepository.countByUserIdAndMemoryStatus(userId, MemoryStatus.NEW)
        val readyForReviewCount = flashcardRepository.countByUserIdAndNextReviewAtBeforeOrNull(userId, LocalDateTime.now())
        
        return FlashcardStatistics(
            totalCount = totalCount,
            masteredCount = masteredCount,
            reviewingCount = reviewingCount,
            learningCount = learningCount,
            strugglingCount = strugglingCount,
            newCount = newCount,
            readyForReviewCount = readyForReviewCount
        )
    }
}

data class FlashcardStatistics(
    val totalCount: Long,
    val masteredCount: Long,
    val reviewingCount: Long,
    val learningCount: Long,
    val strugglingCount: Long,
    val newCount: Long,
    val readyForReviewCount: Long
)
