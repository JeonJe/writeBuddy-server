package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.request.CreateUserRequest
import com.writebuddy.writebuddy.controller.dto.response.UserResponse
import com.writebuddy.writebuddy.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody @Valid request: CreateUserRequest): UserResponse {
        val user = userService.createUser(request)
        return UserResponse.from(user)
    }
    
    @GetMapping
    fun getAllUsers(): List<UserResponse> {
        val users = userService.getAllUsers()
        return users.map { UserResponse.from(it) }
    }
    
    @GetMapping("/{username}")
    fun getUserByUsername(@PathVariable username: String): UserResponse {
        val user = userService.getUserByUsername(username)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $username")
        return UserResponse.from(user)
    }
    
    @GetMapping("/{userId}/statistics")
    fun getUserStatistics(@PathVariable userId: Long): Map<String, Any> {
        return userService.getUserStatistics(userId)
    }
}