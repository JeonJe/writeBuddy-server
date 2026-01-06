package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.ReviewRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReviewRecordRepository : JpaRepository<ReviewRecord, Long> {

    fun findByUserIdAndCorrectionIdOrderByReviewDateDesc(
        userId: Long,
        correctionId: Long
    ): List<ReviewRecord>

    @Query("""
        SELECT COUNT(r) FROM ReviewRecord r 
        WHERE r.user.id = :userId 
        AND r.correctionId = :correctionId 
        AND r.isCorrect = true
    """)
    fun countCorrectByUserIdAndCorrectionId(
        @Param("userId") userId: Long,
        @Param("correctionId") correctionId: Long
    ): Int

    @Query("""
        SELECT COUNT(r) FROM ReviewRecord r 
        WHERE r.user.id = :userId 
        AND r.correctionId = :correctionId
    """)
    fun countByUserIdAndCorrectionId(
        @Param("userId") userId: Long,
        @Param("correctionId") correctionId: Long
    ): Int

    @Query("""
        SELECT r.correctionId as correctionId, 
               MAX(r.reviewDate) as lastReviewedAt, 
               COUNT(r) as reviewCount,
               SUM(CASE WHEN r.isCorrect = true THEN 1 ELSE 0 END) as correctCount
        FROM ReviewRecord r 
        WHERE r.user.id = :userId 
        AND r.correctionId IN :correctionIds
        GROUP BY r.correctionId
    """)
    fun findReviewStatsByUserIdAndCorrectionIds(
        @Param("userId") userId: Long,
        @Param("correctionIds") correctionIds: List<Long>
    ): List<ReviewStatsProjection>

    @Query("""
        SELECT r FROM ReviewRecord r 
        WHERE r.user.id = :userId 
        AND r.reviewDate >= :since
        ORDER BY r.reviewDate DESC
    """)
    fun findByUserIdAndReviewDateAfter(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<ReviewRecord>
}

interface ReviewStatsProjection {
    fun getCorrectionId(): Long
    fun getLastReviewedAt(): LocalDateTime?
    fun getReviewCount(): Long
    fun getCorrectCount(): Long
}
