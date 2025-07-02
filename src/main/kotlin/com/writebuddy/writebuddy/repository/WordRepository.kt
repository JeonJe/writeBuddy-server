package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.Word
import com.writebuddy.writebuddy.domain.WordCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WordRepository : JpaRepository<Word, Long> {
    
    fun findByWord(word: String): Word?
    
    fun findByCategory(category: WordCategory, pageable: Pageable): Page<Word>
    
    fun findByDifficultyBetween(minDifficulty: Int, maxDifficulty: Int, pageable: Pageable): Page<Word>
    
    @Query("SELECT w FROM Word w WHERE w.word LIKE %:keyword% OR w.meaning LIKE %:keyword%")
    fun findByKeyword(keyword: String, pageable: Pageable): Page<Word>
    
    @Query("SELECT w FROM Word w WHERE :tag MEMBER OF w.tags")
    fun findByTag(tag: String, pageable: Pageable): Page<Word>
    
    fun findByIsAiGenerated(isAiGenerated: Boolean, pageable: Pageable): Page<Word>
}