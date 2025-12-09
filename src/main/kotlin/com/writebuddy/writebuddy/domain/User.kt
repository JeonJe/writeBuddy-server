package com.writebuddy.writebuddy.domain

import com.writebuddy.writebuddy.domain.common.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
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

    @Column(nullable = false)
    val email: String,

    @Column(nullable = true)
    var password: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @BatchSize(size = 100)
    val roles: MutableSet<UserRole> = mutableSetOf(UserRole.USER),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val corrections: MutableList<Correction> = mutableListOf()
) : BaseEntity() {

    fun hasRole(role: UserRole): Boolean {
        return roles.contains(role)
    }

    fun addRole(role: UserRole) {
        roles.add(role)
    }

    fun isAdmin(): Boolean {
        return hasRole(UserRole.ADMIN)
    }

    fun addCorrection(correction: Correction) {
        correction.assignToUser(this)
        corrections.add(correction)
    }

    fun updatePassword(encodedPassword: String) {
        password = encodedPassword
    }

    fun hasPassword(): Boolean = password != null
}