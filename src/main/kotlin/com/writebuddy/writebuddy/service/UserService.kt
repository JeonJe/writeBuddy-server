package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CreateUserRequest
import com.writebuddy.writebuddy.controller.dto.request.UserRegistrationRequest
import com.writebuddy.writebuddy.controller.dto.response.UserResponse
import com.writebuddy.writebuddy.domain.User
import com.writebuddy.writebuddy.exception.ValidationException
import com.writebuddy.writebuddy.repository.UserRepository
import com.writebuddy.writebuddy.repository.CorrectionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val correctionRepository: CorrectionRepository,
    private val passwordEncoder: PasswordEncoder,
    private val defaultUserFactory: DefaultUserFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    
    @Deprecated(
        message = "Use registerUser() for authenticated user registration or DefaultUserFactory.getOrCreateDefaultUser() for demo users",
        replaceWith = ReplaceWith("registerUser(UserRegistrationRequest(username, email, password))"),
        level = DeprecationLevel.WARNING
    )
    fun createDemoUser(request: CreateUserRequest): User {
        logger.info("데모 사용자 생성 요청: username={}", request.username)

        require(!userRepository.existsByUsername(request.username)) {
            "이미 존재하는 사용자명입니다: ${request.username}"
        }

        val user = User(
            username = request.username,
            email = request.email
        )

        val savedUser = userRepository.save(user)
        logger.info("데모 사용자 생성 완료: id={}, username={}", savedUser.id, savedUser.username)

        return savedUser
    }
    
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username).orElse(null)
    }
    
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
    
    fun ensureDefaultUser(): User {
        return defaultUserFactory.getOrCreateDefaultUser()
    }

    /**
     * 새로운 사용자 등록 (비밀번호 포함)
     */
    @Transactional
    fun registerUser(request: UserRegistrationRequest): UserResponse {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.email)) {
            throw ValidationException("이미 사용중인 이메일입니다: ${request.email}")
        }

        // 사용자명 중복 검사
        if (userRepository.existsByUsername(request.username)) {
            throw ValidationException("이미 사용중인 사용자명입니다: ${request.username}")
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(request.password)

        // 사용자 생성
        val user = User(
            username = request.username,
            email = request.email,
            password = encodedPassword
        )

        val savedUser = userRepository.save(user)
        logger.info("사용자 등록 성공: userId={}, email={}", savedUser.id, savedUser.email)

        return UserResponse.from(savedUser)
    }

    /**
     * 사용자 정보 조회
     */
    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            ValidationException("사용자를 찾을 수 없습니다: $id")
        }
        return UserResponse.from(user)
    }

    /**
     * 이메일로 사용자 조회
     */
    fun getUserByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ValidationException("사용자를 찾을 수 없습니다: $email")
        return UserResponse.from(user)
    }

    @Transactional
    fun updatePassword(userId: Long, currentPassword: String, newPassword: String) {
        val user = userRepository.findById(userId).orElseThrow {
            ValidationException("사용자를 찾을 수 없습니다: $userId")
        }

        if (user.password == null || !passwordEncoder.matches(currentPassword, user.password)) {
            throw ValidationException("현재 비밀번호가 일치하지 않습니다")
        }

        val encodedPassword = passwordEncoder.encode(newPassword)
        user.updatePassword(encodedPassword)

        userRepository.save(user)
        logger.info("비밀번호 변경 성공: userId={}", userId)
    }
}
