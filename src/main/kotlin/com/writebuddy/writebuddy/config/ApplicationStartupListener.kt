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
        val railwayServiceName = environment.getProperty("RAILWAY_SERVICE_NAME")
        val isRailway = railwayPublicDomain != null || railwayStaticUrl != null || railwayServiceName != null
        
        val separator = "=".repeat(70)
        val startupMessage = buildString {
            appendLine("\n$separator")
            appendLine("🚀 WriteBuddy Application Started Successfully!")
            appendLine("📍 Port: $port")
            appendLine("🔧 Active Profile: $activeProfile")
            
            if (isRailway) {
                appendLine("☁️  Railway Environment: ${railwayEnvironment ?: "production"}")
                railwayPublicDomain?.let { 
                    appendLine("🌐 Public Domain: https://$it") 
                    appendLine("🔗 API Base URL: https://$it/corrections")
                }
                railwayStaticUrl?.let { 
                    appendLine("🔗 Static URL: $it") 
                }
            } else {
                appendLine("💻 Local Development Mode")
                appendLine("🌐 Local URL: http://localhost:$port")
                appendLine("🔗 API Base: http://localhost:$port/corrections")
            }
            
            if (profiles.contains("prod")) {
                appendLine("🚀 Production Mode Enabled")
            }
            appendLine("$separator\n")
        }
        
        // 콘솔과 로그 파일 모두에 출력
        println(startupMessage)
        logger.info("WriteBuddy Application Started - Port: $port, Profile: $activeProfile, Railway: $isRailway")
    }
}
