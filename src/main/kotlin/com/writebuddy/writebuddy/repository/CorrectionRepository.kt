package com.writebuddy.writebuddy.repository

import com.writebuddy.writebuddy.domain.Correction
import org.springframework.data.jpa.repository.JpaRepository

interface CorrectionRepository : JpaRepository<Correction, Long> {
}
