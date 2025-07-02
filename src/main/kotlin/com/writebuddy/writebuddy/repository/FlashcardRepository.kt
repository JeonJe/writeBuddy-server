package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.Flashcard
import com.writebuddy.writebuddy.domain.MemoryStatus
import com.writebuddy.writebuddy.domain.User
import com.writebuddy.writebuddy.domain.Word
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FlashcardRepository : JpaRepository<Flashcard, Long> {
    
    fun findByUserAndWord(user: User, word: Word): Flashcard?
    
    fun findByUserId(userId: Long, pageable: Pageable): Page<Flashcard>
    
    fun findByUserIdAndMemoryStatus(userId: Long, memoryStatus: MemoryStatus, pageable: Pageable): Page<Flashcard>
    
    fun findByUserIdAndIsFavoriteTrue(userId: Long, pageable: Pageable): Page<Flashcard>
    
    @Query("SELECT f FROM Flashcard f WHERE f.user.id = :userId AND (f.nextReviewAt IS NULL OR f.nextReviewAt <= :now)")
    fun findByUserIdAndNextReviewAtBeforeOrNull(@Param("userId") userId: Long, 
                                                @Param("now") now: LocalDateTime, 
                                                pageable: Pageable): Page<Flashcard>
    
    fun countByUserId(userId: Long): Long
    
    fun countByUserIdAndMemoryStatus(userId: Long, memoryStatus: MemoryStatus): Long
    
    @Query("SELECT COUNT(f) FROM Flashcard f WHERE f.user.id = :userId AND (f.nextReviewAt IS NULL OR f.nextReviewAt <= :now)")
    fun countByUserIdAndNextReviewAtBeforeOrNull(@Param("userId") userId: Long, 
                                                 @Param("now") now: LocalDateTime): Long
    
    @Query("SELECT f FROM Flashcard f WHERE f.user.id = :userId AND f.word.category = :category")
    fun findByUserIdAndWordCategory(@Param("userId") userId: Long, 
                                   @Param("category") category: String, 
                                   pageable: Pageable): Page<Flashcard>
    
    @Query("SELECT f FROM Flashcard f WHERE f.user.id = :userId AND f.word.word LIKE %:keyword%")
    fun findByUserIdAndWordContaining(@Param("userId") userId: Long, 
                                     @Param("keyword") keyword: String, 
                                     pageable: Pageable): Page<Flashcard>
}