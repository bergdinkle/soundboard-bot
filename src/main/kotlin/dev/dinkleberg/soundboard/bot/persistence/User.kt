package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import jakarta.persistence.Table
import java.time.OffsetDateTime

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
@Table(name = "Users")
data class User(
    @field:Id
    val id: String,
    val name: String,
    val tokenHash: String,
    val admin: Boolean = false,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)