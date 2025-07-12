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
        val profiles = event.applicationContext.environment.activeProfiles
        val activeProfile = if (profiles.isNotEmpty()) profiles.joinToString(", ") else "default"
        
        println("\n" + "=".repeat(60))
        println("🚀 WriteBuddy Application Started Successfully!")
        println("📍 Port: $port")
        println("🔧 Active Profile: $activeProfile")
        if (profiles.contains("prod")) {
            println("🚀 Production Mode Enabled")
        }
        println("=".repeat(60) + "\n")
    }
}
