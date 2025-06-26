package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.Correction
import com.writebuddy.writebuddy.domain.FeedbackType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface CorrectionRepository : JpaRepository<Correction, Long> {
    
    fun findByUserIdAndCreatedAtAfter(userId: Long, createdAt: LocalDateTime): List<Correction>
    
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Correction>
    
    @Query("""
        SELECT c FROM Correction c 
        WHERE c.user.id = :userId 
        AND c.feedbackType = :feedbackType
        AND c.createdAt >= :since
        ORDER BY c.createdAt DESC
    """)
    fun findByUserIdAndFeedbackTypeAndCreatedAtAfter(
        @Param("userId") userId: Long,
        @Param("feedbackType") feedbackType: FeedbackType,
        @Param("since") since: LocalDateTime
    ): List<Correction>
    
    @Query("""
        SELECT COUNT(c) FROM Correction c 
        WHERE c.user.id = :userId 
        AND c.createdAt >= :since
    """)
    fun countByUserIdAndCreatedAtAfter(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): Long
    
    @Query("""
        SELECT AVG(c.score) FROM Correction c 
        WHERE c.user.id = :userId 
        AND c.score IS NOT NULL
        AND c.createdAt >= :since
    """)
    fun calculateAverageScoreByUserIdAndCreatedAtAfter(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): Double?
}
