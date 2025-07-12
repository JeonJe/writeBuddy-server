package com.writebuddy.writebuddy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val corsConfig: CorsConfig
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfig.corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/login/**", "/oauth2/**", "/error", "/h2-console/**").permitAll()
                    .requestMatchers("/hello/**").permitAll()
                    .requestMatchers("/corrections/**", "/examples/**", "/chat").permitAll()
                    .requestMatchers("/corrections/users/**", "/users/**", "/analytics/**").authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/login/success", true)
                    .failureUrl("/login/failure")
                    .userInfoEndpoint { userInfo ->
                        userInfo.oidcUserService(oidcUserService())
                    }
            }
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
            }
            .headers { headers ->
                headers.frameOptions { it.disable() }
            }

        return http.build()
    }

    @Bean
    fun oidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> {
        return OidcUserService()
    }

}
