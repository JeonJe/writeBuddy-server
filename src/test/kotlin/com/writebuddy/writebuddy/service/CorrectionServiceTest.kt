package com.writebuddy.writebuddy.service

import com.writebuddy.writebuddy.controller.dto.request.CorrectionRequest
import com.writebuddy.writebuddy.repository.CorrectionRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
@Transactional
class CorrectionServiceTest @Autowired constructor(
    val correctionService: CorrectionService,
    val corrrectionRepository: CorrectionRepository
){

    @Test
    fun `save() should persist correction`() {
        val request = CorrectionRequest(
            originSentence = "This is a test sentence.",
            correctedSentence = "This is a test sentence.",
            feedback = "No errors found."
        )

        val savedCorrection = correctionService.save(request)

        assertThat(savedCorrection).isNotNull
    }
}
