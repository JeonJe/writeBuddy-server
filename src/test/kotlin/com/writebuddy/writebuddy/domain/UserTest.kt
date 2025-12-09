package com.writebuddy.writebuddy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("User 도메인 테스트")
class UserTest {

    @Nested
    @DisplayName("역할 관리")
    inner class RoleManagement {

        @Test
        fun `hasRole_사용자_역할_확인`() {
            val user = User(
                username = "testuser",
                email = "test@example.com",
                roles = mutableSetOf(UserRole.USER)
            )

            assertThat(user.hasRole(UserRole.USER)).isTrue
            assertThat(user.hasRole(UserRole.ADMIN)).isFalse
        }

        @Test
        fun `addRole_역할_추가`() {
            val user = User(
                username = "testuser",
                email = "test@example.com",
                roles = mutableSetOf(UserRole.USER)
            )

            user.addRole(UserRole.ADMIN)

            assertThat(user.roles).hasSize(2)
            assertThat(user.hasRole(UserRole.ADMIN)).isTrue
        }

        @Test
        fun `isAdmin_관리자_역할_확인`() {
            val adminUser = User(
                username = "admin",
                email = "admin@example.com",
                roles = mutableSetOf(UserRole.USER, UserRole.ADMIN)
            )

            val normalUser = User(
                username = "user",
                email = "user@example.com",
                roles = mutableSetOf(UserRole.USER)
            )

            assertThat(adminUser.isAdmin()).isTrue
            assertThat(normalUser.isAdmin()).isFalse
        }

        @Test
        fun `기본_역할은_USER`() {
            val user = User(
                username = "testuser",
                email = "test@example.com"
            )

            assertThat(user.roles).hasSize(1)
            assertThat(user.roles).containsExactly(UserRole.USER)
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드")
    inner class BusinessMethods {

        @Test
        fun addCorrection_교정결과_추가() {
            val user = User(
                username = "testuser",
                email = "test@example.com"
            )
            val correction = Correction(
                originSentence = "test",
                correctedSentence = "Test",
                feedback = "feedback"
            )

            user.addCorrection(correction)

            assertThat(user.corrections).hasSize(1)
            assertThat(user.corrections).contains(correction)
            assertThat(correction.user).isEqualTo(user)
        }

        @Test
        fun updatePassword_비밀번호_업데이트() {
            val user = User(
                username = "testuser",
                email = "test@example.com",
                password = null
            )

            user.updatePassword("encodedPassword123")

            assertThat(user.password).isEqualTo("encodedPassword123")

            user.updatePassword("newEncodedPassword456")

            assertThat(user.password).isEqualTo("newEncodedPassword456")
        }

        @Test
        fun hasPassword_비밀번호_존재_여부() {
            val userWithPassword = User(
                username = "testuser1",
                email = "test1@example.com",
                password = "encodedPassword"
            )
            val userWithoutPassword = User(
                username = "testuser2",
                email = "test2@example.com",
                password = null
            )

            assertThat(userWithPassword.hasPassword()).isTrue
            assertThat(userWithoutPassword.hasPassword()).isFalse
        }
    }
}