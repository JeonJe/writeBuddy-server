package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CreateUserRequest
import com.writebuddy.writebuddy.domain.User
import com.writebuddy.writebuddy.repository.UserRepository
import com.writebuddy.writebuddy.repository.CorrectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val correctionRepository: CorrectionRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    
    fun createUser(request: CreateUserRequest): User {
        logger.info("사용자 생성 요청: username={}", request.username)

        require(!userRepository.existsByUsername(request.username)) {
            "이미 존재하는 사용자명입니다: ${request.username}"
        }
        
        val user = User(
            username = request.username,
            email = request.email
        )
        
        val savedUser = userRepository.save(user)
        logger.info("사용자 생성 완료: id={}, username={}", savedUser.id, savedUser.username)
        
        return savedUser
    }
    
    fun getUserByUsername(username: String): User? {
        logger.debug("사용자 조회: username={}", username)
        return userRepository.findByUsername(username).orElse(null)
    }
    
    fun getAllUsers(): List<User> {
        logger.debug("전체 사용자 목록 조회")
        return userRepository.findAll()
    }
    
    fun getUserStatistics(userId: Long): Map<String, Any> {
        logger.debug("사용자 통계 조회: userId={}", userId)
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        
        val corrections = user.corrections
        val scoresWithData = corrections.mapNotNull { it.score }
        
        return mapOf(
            "totalCorrections" to corrections.size,
            "averageScore" to if (scoresWithData.isNotEmpty()) scoresWithData.average() else 0.0,
            "favoriteCount" to corrections.count { it.isFavorite },
            "feedbackTypeDistribution" to corrections.groupBy { it.feedbackType.name }
                .mapValues { it.value.size }
        )
    }
    
    fun getAllUsersStatistics(): Map<String, Any> {
        logger.debug("전체 사용자 통계 조회")
        
        // 기본 사용자 존재 확인 및 생성
        val defaultUser = ensureDefaultUser()
        
        // 기본 사용자의 통계를 조회
        return getUserStatistics(defaultUser.id)
    }
    
    fun ensureDefaultUser(): User {
        // 첫 번째 사용자를 찾거나 생성
        return userRepository.findAll().firstOrNull() ?: run {
            logger.info("기본 사용자 생성")
            val defaultUser = User(
                username = "demo_user",
                email = "demo@writebuddy.com"
            )
            userRepository.save(defaultUser)
        }
    }
}
