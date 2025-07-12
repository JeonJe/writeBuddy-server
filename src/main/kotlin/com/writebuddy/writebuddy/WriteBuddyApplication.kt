package com.writebuddy.writebuddy

import me.paulschwarz.springdotenv.environment.DotenvPropertySource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class WriteBuddyApplication

fun main(args: Array<String>) {
	val app = runApplication<WriteBuddyApplication>(*args) {
		addInitializers {
			DotenvPropertySource.addToEnvironment(it.environment)
		}
	}
}
