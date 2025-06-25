package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
class RealExample(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(length = 500)
    val phrase: String,              // 실제 사용된 표현
    
    @Column(length = 200)
    val source: String,              // 출처 (영화명, 노래명 등)
    
    @Enumerated(EnumType.STRING)
    val sourceType: ExampleSourceType, // 출처 타입
    
    @Lob
    val context: String,             // 사용된 맥락/상황 설명
    
    @Column(length = 500)
    val url: String? = null,         // 관련 링크 (YouTube, 기사 등)
    
    @Column(length = 20)
    val timestamp: String? = null,   // 영상의 경우 타임스탬프
    
    val difficulty: Int = 5,         // 1-10 난이도
    
    @Column(length = 100)
    val tags: String? = null,        // 검색용 태그 (쉼표 구분)
    
    val isVerified: Boolean = false  // 검증된 예시인지 여부
) : BaseEntity()

enum class ExampleSourceType(val displayName: String, val emoji: String) {
    MOVIE("영화/드라마", "🎬"),
    SONG("음악/가사", "🎵"), 
    NEWS("뉴스/기사", "📰"),
    BOOK("문학/도서", "📚"),
    INTERVIEW("인터뷰", "🎤"),
    SOCIAL("소셜미디어", "📱"),
    SPEECH("연설/강연", "🎙️"),
    PODCAST("팟캐스트", "🎧")
}