package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CreateUserRequest
import com.writebuddy.writebuddy.controller.dto.request.UserRegistrationRequest
import com.writebuddy.writebuddy.controller.dto.response.UserResponse
import com.writebuddy.writebuddy.service.UserService
import com.writebuddy.writebuddy.service.CorrectionStatisticsService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val correctionStatisticsService: CorrectionStatisticsService
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    @GetMapping
    fun getAllUsers(): List<UserResponse> {
        val users = userService.getAllUsers()
        return users.map { UserResponse.from(it) }
    }
    
    @GetMapping("/{username}")
    fun getUserByUsername(@PathVariable username: String): UserResponse {
        val user = userService.getUserByUsername(username)
        requireNotNull(user) { "사용자를 찾을 수 없습니다: $username" }
        return UserResponse.from(user)
    }
    
    /**
     * 새로운 사용자 등록 (비밀번호 포함)
     */
    @PostMapping("/register")
    fun registerUser(@RequestBody @Valid request: UserRegistrationRequest): ResponseEntity<UserResponse> {
        logger.info("사용자 등록 요청: username={}, email={}", request.username, request.email)
        val userResponse = userService.registerUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
    }
}
