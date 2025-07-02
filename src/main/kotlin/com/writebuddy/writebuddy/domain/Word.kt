package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "words")
class Word(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val word: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    val meaning: String,
    @Column(nullable = false)
    val difficulty: Int = 5, // 1-10 난이도
    @ElementCollection
    @CollectionTable(name = "word_tags", joinColumns = [JoinColumn(name = "word_id")])
    @Column(name = "tag")
    val tags: MutableSet<String> = mutableSetOf(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: WordCategory = WordCategory.GENERAL,
    @Column(nullable = false)
    val isAiGenerated: Boolean = true,
    @OneToMany(mappedBy = "word", cascade = [CascadeType.ALL])
    val flashcards: MutableList<Flashcard> = mutableListOf()
) : BaseEntity()

enum class WordCategory {
    GRAMMAR,     // 문법
    BUSINESS,    // 비즈니스
    ACADEMIC,    // 학술
    DAILY,       // 일상
    TRAVEL,      // 여행
    TECHNOLOGY,  // 기술
    GENERAL      // 일반
}