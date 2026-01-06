package com.writebuddy.writebuddy.security

import com.writebuddy.writebuddy.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// @Component  // JWT 임시 비활성화
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val userId = jwtTokenProvider.getUserIdFromToken(token)
                val user = userRepository.findById(userId).orElse(null)

                if (user != null) {
                    val authorities = user.roles.map {
                        SimpleGrantedAuthority("ROLE_${it.name}")
                    }

                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("JWT 인증 성공: userId=$userId, roles=${user.roles}")
                }
            }
        } catch (e: Exception) {
            logger.error("JWT 인증 처리 중 오류 발생", e)
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (bearerToken?.startsWith(BEARER_PREFIX) == true) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else null
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}