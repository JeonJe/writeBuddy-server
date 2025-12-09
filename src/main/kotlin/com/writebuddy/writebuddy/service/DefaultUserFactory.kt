package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.User
import com.writebuddy.writebuddy.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultUserFactory(
    private val userRepository: UserRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(DefaultUserFactory::class.java)

    fun getOrCreateDefaultUser(): User {
        return userRepository.findAll().firstOrNull() ?: run {
            logger.info("기본 사용자 생성")
            userRepository.save(User(
                username = "demo_user",
                email = "demo@writebuddy.com"
            ))
        }
    }
}
