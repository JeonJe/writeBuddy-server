package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.domain.User
import com.writebuddy.writebuddy.repository.UserRepository
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class OAuth2UserService(
    private val userRepository: UserRepository
) : OidcUserService() {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        
        val provider = userRequest.clientRegistration.registrationId
        val providerId = oidcUser.subject
        val email = oidcUser.email
        val name = oidcUser.fullName ?: oidcUser.givenName ?: email
        val profileImageUrl = oidcUser.picture
        
        val existingUser = userRepository.findByOauthProviderAndOauthProviderId(provider, providerId)
        
        if (existingUser == null) {
            val newUser = User(
                username = generateUniqueUsername(name),
                email = email,
                oauthProvider = provider,
                oauthProviderId = providerId,
                profileImageUrl = profileImageUrl
            )
            userRepository.save(newUser)
        }
        
        return oidcUser
    }
    
    private fun generateUniqueUsername(baseName: String): String {
        var username = baseName.replace("\\s+".toRegex(), "_").lowercase()
        var counter = 1
        
        while (userRepository.findByUsername(username).isPresent) {
            username = "${baseName.replace("\\s+".toRegex(), "_").lowercase()}_$counter"
            counter++
        }
        
        return username
    }
}