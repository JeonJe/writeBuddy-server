package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true)
    val username: String,
    val email: String,
    @Column(name = "oauth_provider")
    val oauthProvider: String? = null,
    @Column(name = "oauth_provider_id")
    val oauthProviderId: String? = null,
    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val corrections: MutableList<Correction> = mutableListOf()
) : BaseEntity()