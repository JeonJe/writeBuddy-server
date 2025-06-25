package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RealExampleRepository : JpaRepository<RealExample, Long> {
    
    // 특정 구문과 유사한 예시 찾기 (부분 일치)
    fun findByPhraseContainingIgnoreCase(phrase: String): List<RealExample>
    
    // 출처 타입별 예시 조회
    fun findBySourceType(sourceType: ExampleSourceType): List<RealExample>
    
    // 난이도별 예시 조회
    fun findByDifficultyBetween(minDifficulty: Int, maxDifficulty: Int): List<RealExample>
    
    // 검증된 예시만 조회
    fun findByIsVerifiedTrue(): List<RealExample>
    
    // 태그로 검색
    fun findByTagsContainingIgnoreCase(tag: String): List<RealExample>
    
    // 랜덤 예시 조회 (검증된 것만)
    @Query(value = "SELECT * FROM real_example WHERE is_verified = true ORDER BY RANDOM()", nativeQuery = true)
    fun findRandomVerifiedExamples(): List<RealExample>
    
    // 특정 키워드로 전체 검색 (구문, 출처, 맥락에서)
    @Query(value = """
        SELECT * FROM real_example r 
        WHERE LOWER(r.phrase) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(r.source) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.context) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """, nativeQuery = true)
    fun searchByKeyword(@Param("keyword") keyword: String): List<RealExample>
}