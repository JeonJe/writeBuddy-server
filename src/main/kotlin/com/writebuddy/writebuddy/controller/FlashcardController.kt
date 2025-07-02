package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.domain.Flashcard
import com.writebuddy.writebuddy.domain.MemoryStatus
import com.writebuddy.writebuddy.service.FlashcardService
import com.writebuddy.writebuddy.service.FlashcardStatistics
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/flashcards")
class FlashcardController(
    private val flashcardService: FlashcardService
) {

    @PostMapping
    fun createFlashcard(@Valid @RequestBody request: CreateFlashcardRequest): ResponseEntity<FlashcardResponse> {
        val flashcard = flashcardService.createFlashcard(request.userId, request.word)
        return ResponseEntity.status(HttpStatus.CREATED).body(FlashcardResponse.from(flashcard))
    }

    @GetMapping("/users/{userId}")
    fun getFlashcards(
        @PathVariable userId: Long,
        @RequestParam(required = false) memoryStatus: MemoryStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<FlashcardResponse>> {
        val flashcards = flashcardService.getFlashcards(userId, memoryStatus, pageable)
        return ResponseEntity.ok(flashcards.map { FlashcardResponse.from(it) })
    }

    @GetMapping("/users/{userId}/review")
    fun getFlashcardsForReview(
        @PathVariable userId: Long,
        @PageableDefault(size = 10, sort = ["nextReviewAt"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<Page<FlashcardResponse>> {
        val flashcards = flashcardService.getFlashcardsForReview(userId, pageable)
        return ResponseEntity.ok(flashcards.map { FlashcardResponse.from(it) })
    }

    @GetMapping("/users/{userId}/favorites")
    fun getFavoriteFlashcards(
        @PathVariable userId: Long,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<FlashcardResponse>> {
        val flashcards = flashcardService.getFavoriteFlashcards(userId, pageable)
        return ResponseEntity.ok(flashcards.map { FlashcardResponse.from(it) })
    }

    @PostMapping("/{flashcardId}/review")
    fun reviewFlashcard(
        @PathVariable flashcardId: Long,
        @Valid @RequestBody request: ReviewRequest
    ): ResponseEntity<FlashcardResponse> {
        val flashcard = flashcardService.reviewFlashcard(flashcardId, request.isCorrect)
        return ResponseEntity.ok(FlashcardResponse.from(flashcard))
    }

    @PutMapping("/{flashcardId}/favorite")
    fun toggleFavorite(@PathVariable flashcardId: Long): ResponseEntity<FlashcardResponse> {
        val flashcard = flashcardService.toggleFavorite(flashcardId)
        return ResponseEntity.ok(FlashcardResponse.from(flashcard))
    }

    @PutMapping("/{flashcardId}/note")
    fun updateNote(
        @PathVariable flashcardId: Long,
        @Valid @RequestBody request: UpdateNoteRequest
    ): ResponseEntity<FlashcardResponse> {
        val flashcard = flashcardService.updateNote(flashcardId, request.note)
        return ResponseEntity.ok(FlashcardResponse.from(flashcard))
    }

    @DeleteMapping("/{flashcardId}")
    fun deleteFlashcard(@PathVariable flashcardId: Long): ResponseEntity<Void> {
        flashcardService.deleteFlashcard(flashcardId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users/{userId}/statistics")
    fun getFlashcardStatistics(@PathVariable userId: Long): ResponseEntity<FlashcardStatistics> {
        val statistics = flashcardService.getFlashcardStatistics(userId)
        return ResponseEntity.ok(statistics)
    }
}

// Request DTOs
data class CreateFlashcardRequest(
    @field:Min(1, message = "User ID must be positive")
    val userId: Long,
    
    @field:NotBlank(message = "Word cannot be blank")
    @field:Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    val word: String
)

data class ReviewRequest(
    val isCorrect: Boolean
)

data class UpdateNoteRequest(
    @field:Size(max = 500, message = "Note cannot exceed 500 characters")
    val note: String?
)

// Response DTO
data class FlashcardResponse(
    val id: Long,
    val userId: Long,
    val word: WordResponse,
    val memoryStatus: MemoryStatus,
    val reviewCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val accuracy: Double,
    val lastReviewedAt: String?,
    val nextReviewAt: String?,
    val personalNote: String?,
    val isFavorite: Boolean,
    val isReadyForReview: Boolean,
    val createdAt: String?
) {
    companion object {
        fun from(flashcard: Flashcard): FlashcardResponse {
            return FlashcardResponse(
                id = flashcard.id,
                userId = flashcard.user.id,
                word = WordResponse.from(flashcard.word),
                memoryStatus = flashcard.memoryStatus,
                reviewCount = flashcard.reviewCount,
                correctCount = flashcard.correctCount,
                incorrectCount = flashcard.incorrectCount,
                accuracy = flashcard.getAccuracy(),
                lastReviewedAt = flashcard.lastReviewedAt?.toString(),
                nextReviewAt = flashcard.nextReviewAt?.toString(),
                personalNote = flashcard.personalNote,
                isFavorite = flashcard.isFavorite,
                isReadyForReview = flashcard.isReadyForReview(),
                createdAt = flashcard.createdAt?.toString()
            )
        }
    }
}

data class WordResponse(
    val id: Long,
    val word: String,
    val meaning: String,
    val difficulty: Int,
    val tags: Set<String>,
    val category: String,
    val isAiGenerated: Boolean
) {
    companion object {
        fun from(word: com.writebuddy.writebuddy.domain.Word): WordResponse {
            return WordResponse(
                id = word.id,
                word = word.word,
                meaning = word.meaning,
                difficulty = word.difficulty,
                tags = word.tags,
                category = word.category.name,
                isAiGenerated = word.isAiGenerated
            )
        }
    }
}
