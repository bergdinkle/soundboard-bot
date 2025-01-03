package dev.dinkleberg.soundboard.bot.persistence

import dev.dinkleberg.soundboard.bot.soundboard.Event
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import jakarta.persistence.Table

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
@Table(name = "EventSounds")
data class EventSound(
    @field:Id
    val id: String,
    val event: Event,
    val soundId: String
)
