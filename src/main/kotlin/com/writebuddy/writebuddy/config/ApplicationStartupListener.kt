package com.writebuddy.writebuddy.config

import org.slf4j.LoggerFactory
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class ApplicationStartupListener : ApplicationListener<WebServerInitializedEvent> {
    
    private val logger = LoggerFactory.getLogger(ApplicationStartupListener::class.java)

    override fun onApplicationEvent(event: WebServerInitializedEvent) {
        val port = event.webServer.port
        val environment = event.applicationContext.environment
        val profiles = environment.activeProfiles
        val activeProfile = if (profiles.isNotEmpty()) profiles.joinToString(", ") else "default"
        
        // Railway 환경 변수들
        val railwayPublicDomain = environment.getProperty("RAILWAY_PUBLIC_DOMAIN")
        val railwayStaticUrl = environment.getProperty("RAILWAY_STATIC_URL")
        val railwayEnvironment = environment.getProperty("RAILWAY_ENVIRONMENT")
        val isRailway = railwayPublicDomain != null
        
        println("\n" + "=".repeat(70))
        println("🚀 WriteBuddy Application Started Successfully!")
        println("📍 Port: $port")
        println("🔧 Active Profile: $activeProfile")
        
        if (isRailway) {
            println("☁️  Railway Environment: ${railwayEnvironment ?: "production"}")
            railwayPublicDomain?.let { 
                println("🌐 Public Domain: https://$it") 
                println("🔗 API Base URL: https://$it/corrections")
            }
            railwayStaticUrl?.let { 
                println("🔗 Static URL: $it") 
            }
        } else {
            println("💻 Local Development Mode")
            println("🌐 Local URL: http://localhost:$port")
            println("🔗 API Base: http://localhost:$port/corrections")
        }
        
        if (profiles.contains("prod")) {
            println("🚀 Production Mode Enabled")
        }
        println("=".repeat(70) + "\n")
    }
}
