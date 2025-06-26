package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.WeakAreaAnalysis
import com.writebuddy.writebuddy.domain.WeakAreaSeverity
import com.writebuddy.writebuddy.domain.WeakAreaType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface WeakAreaAnalysisRepository : JpaRepository<WeakAreaAnalysis, Long> {
    
    fun findByUserIdOrderByFrequencyDesc(userId: Long): List<WeakAreaAnalysis>
    
    fun findByUserIdAndWeakAreaType(userId: Long, weakAreaType: WeakAreaType): WeakAreaAnalysis?
    
    @Query("""
        SELECT w FROM WeakAreaAnalysis w 
        WHERE w.user.id = :userId 
        AND w.severity IN (:severities)
        ORDER BY w.frequency DESC, w.lastOccurrence DESC
    """)
    fun findByUserIdAndSeverityInOrderByFrequencyDesc(
        @Param("userId") userId: Long, 
        @Param("severities") severities: List<WeakAreaSeverity>
    ): List<WeakAreaAnalysis>
    
    @Query("""
        SELECT w FROM WeakAreaAnalysis w 
        WHERE w.user.id = :userId 
        AND w.lastOccurrence >= :since
        ORDER BY w.frequency DESC
    """)
    fun findRecentWeakAreasByUserId(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<WeakAreaAnalysis>
    
    @Query("""
        SELECT AVG(w.improvementRate) FROM WeakAreaAnalysis w 
        WHERE w.user.id = :userId
    """)
    fun calculateAverageImprovementRate(@Param("userId") userId: Long): Double?
}