package com.writebuddy.writebuddy.config

import com.writebuddy.writebuddy.domain.ExampleSourceType
import com.writebuddy.writebuddy.domain.RealExample
import com.writebuddy.writebuddy.repository.RealExampleRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val realExampleRepository: RealExampleRepository
) : ApplicationRunner {
    
    override fun run(args: ApplicationArguments?) {
        if (realExampleRepository.count() == 0L) {
            initializeExamples()
        }
    }
    
    private fun initializeExamples() {
        val examples = listOf(
            RealExample(
                phrase = "I couldn't agree more",
                source = "Friends (TV Show)",
                sourceType = ExampleSourceType.MOVIE,
                context = "Ross agrees enthusiastically with Rachel's opinion about Monica's cooking",
                url = "https://www.youtube.com/watch?v=example",
                timestamp = "05:23",
                difficulty = 6,
                tags = "agreement, enthusiasm, conversation",
                isVerified = true
            ),
            RealExample(
                phrase = "Break a leg",
                source = "Hamilton (Musical)",
                sourceType = ExampleSourceType.SONG,
                context = "Actor wishing good luck to another performer before going on stage",
                difficulty = 7,
                tags = "idiom, good luck, theater",
                isVerified = true
            ),
            RealExample(
                phrase = "It's raining cats and dogs",
                source = "BBC Weather Report",
                sourceType = ExampleSourceType.NEWS,
                context = "Weather presenter describing heavy rainfall in London",
                url = "https://bbc.co.uk/weather",
                difficulty = 8,
                tags = "weather, idiom, heavy rain",
                isVerified = true
            ),
            RealExample(
                phrase = "Time flies when you're having fun",
                source = "The Great Gatsby",
                sourceType = ExampleSourceType.BOOK,
                context = "Nick reflecting on the summer parties at Gatsby's mansion",
                difficulty = 5,
                tags = "time, enjoyment, classic literature",
                isVerified = true
            ),
            RealExample(
                phrase = "That's a game changer",
                source = "Elon Musk Interview - Joe Rogan Podcast",
                sourceType = ExampleSourceType.INTERVIEW,
                context = "Discussing the impact of electric vehicles on the automotive industry",
                url = "https://open.spotify.com/episode/example",
                timestamp = "1:23:45",
                difficulty = 6,
                tags = "innovation, impact, business",
                isVerified = true
            ),
            RealExample(
                phrase = "Going viral",
                source = "Twitter/X Post",
                sourceType = ExampleSourceType.SOCIAL,
                context = "Content creator celebrating their video reaching millions of views",
                difficulty = 4,
                tags = "social media, popularity, internet",
                isVerified = true
            ),
            RealExample(
                phrase = "The ball is in your court",
                source = "Steve Jobs Stanford Commencement Speech",
                sourceType = ExampleSourceType.SPEECH,
                context = "Encouraging graduates to take initiative in their careers",
                url = "https://www.youtube.com/watch?v=UF8uR6Z6KLc",
                timestamp = "12:30",
                difficulty = 7,
                tags = "responsibility, decision making, motivation",
                isVerified = true
            ),
            RealExample(
                phrase = "Think outside the box",
                source = "The Tim Ferriss Show",
                sourceType = ExampleSourceType.PODCAST,
                context = "Guest entrepreneur discussing creative problem-solving strategies",
                difficulty = 6,
                tags = "creativity, innovation, problem solving",
                isVerified = true
            )
        )
        
        realExampleRepository.saveAll(examples)
    }
}