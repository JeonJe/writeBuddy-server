package com.writebuddy.writebuddy.domain.common

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreatedDate
    var createdAt: LocalDateTime? = null

    @CreatedBy
    var createdBy: String? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    @LastModifiedBy
    var updatedBy: String? = null
}
