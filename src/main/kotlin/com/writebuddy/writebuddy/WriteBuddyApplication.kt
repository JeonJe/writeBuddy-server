package com.writebuddy.writebuddy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class WriteBuddyApplication

fun main(args: Array<String>) {
	runApplication<WriteBuddyApplication>(*args)
}

