package com.writebuddy.writebuddy.controller.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegistrationRequest(
    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 3, max = 20, message = "사용자명은 3-20자 사이여야 합니다")
    val username: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이어야 합니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val password: String
)