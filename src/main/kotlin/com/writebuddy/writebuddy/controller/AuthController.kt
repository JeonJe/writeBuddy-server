package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.UserResponse
import com.writebuddy.writebuddy.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository
) {

    @GetMapping("/user")
    fun getCurrentUser(@AuthenticationPrincipal oidcUser: OidcUser?): ResponseEntity<UserResponse> {
        if (oidcUser == null) {
            return ResponseEntity.ok(null)
        }
        
        val provider = oidcUser.issuer.toString().substringAfterLast("/")
        val providerId = oidcUser.subject
        
        val user = userRepository.findByOauthProviderAndOauthProviderId(provider, providerId)
            ?: return ResponseEntity.notFound().build()
            
        return ResponseEntity.ok(UserResponse.from(user))
    }
    
    @GetMapping("/status")
    fun getAuthStatus(@AuthenticationPrincipal oidcUser: OidcUser?): ResponseEntity<Map<String, Any?>> {
        val isAuthenticated = oidcUser != null
        val response = mapOf(
            "authenticated" to isAuthenticated,
            "user" to if (isAuthenticated && oidcUser != null) {
                mapOf(
                    "name" to (oidcUser.fullName ?: oidcUser.givenName),
                    "email" to oidcUser.email,
                    "picture" to oidcUser.picture
                )
            } else null
        )
        return ResponseEntity.ok(response)
    }
}