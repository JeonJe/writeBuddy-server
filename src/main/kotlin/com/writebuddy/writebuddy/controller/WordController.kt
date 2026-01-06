package com.writebuddy.writebuddy.controller

import com.writebuddy.writebuddy.controller.dto.response.AiGrammarResponse
import com.writebuddy.writebuddy.controller.dto.response.AiWordResponse
import com.writebuddy.writebuddy.service.WordService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/words")
class WordController(
    private val wordService: WordService
) {
    private val logger: Logger = LoggerFactory.getLogger(WordController::class.java)

    @GetMapping("/search")
    fun searchWords(@RequestParam keyword: String): AiWordResponse {
        logger.info("Word search requested: keyword={}", keyword)
        return wordService.lookupWordWithAi(keyword)
    }

    @GetMapping("/grammar/search")
    fun searchGrammar(@RequestParam keyword: String): AiGrammarResponse {
        logger.info("Grammar search requested: keyword={}", keyword)
        return wordService.lookupGrammarWithAi(keyword)
    }
}
