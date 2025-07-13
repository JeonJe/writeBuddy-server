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
    
    // 통계용 최적화 쿼리들
    @Query("""
        SELECT c.feedbackType as feedbackType, COUNT(c) as count 
        FROM Correction c 
        GROUP BY c.feedbackType
    """)
    fun getFeedbackTypeStatistics(): List<FeedbackTypeCount>
    
    @Query("SELECT AVG(c.score) FROM Correction c WHERE c.score IS NOT NULL")
    fun calculateOverallAverageScore(): Double?
    
    @Query("""
        SELECT COUNT(c) as totalCorrections,
               AVG(c.score) as averageScore
        FROM Correction c 
        WHERE c.createdAt >= :startOfDay
        AND c.score IS NOT NULL
    """)
    fun getDailyStatistics(@Param("startOfDay") startOfDay: LocalDateTime): DailyStats?
    
    @Query("""
        SELECT c.feedbackType as feedbackType, COUNT(c) as count 
        FROM Correction c 
        WHERE c.createdAt >= :startOfDay
        GROUP BY c.feedbackType
    """)
    fun getDailyFeedbackTypeCount(@Param("startOfDay") startOfDay: LocalDateTime): List<FeedbackTypeCount>
    
    @Query("""
        SELECT c FROM Correction c 
        WHERE c.score IS NOT NULL 
        ORDER BY c.createdAt DESC
        LIMIT 20
    """)
    fun findTop20ByOrderByCreatedAtDesc(): List<Correction>
    
    @Query("""
        SELECT c.feedbackType as feedbackType, c.originSentence as sentence
        FROM Correction c
        WHERE c.feedbackType != 'SYSTEM'
        ORDER BY c.createdAt DESC
    """)
    fun findAllErrorPatterns(): List<ErrorPattern>
}

interface FeedbackTypeCount {
    fun getFeedbackType(): FeedbackType
    fun getCount(): Long
}

interface DailyStats {
    fun getTotalCorrections(): Long
    fun getAverageScore(): Double?
}

interface ErrorPattern {
    fun getFeedbackType(): FeedbackType
    fun getSentence(): String
}
