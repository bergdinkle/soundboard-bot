package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.annotation.Embeddable
import io.micronaut.data.annotation.EmbeddedId
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import jakarta.persistence.Column
import jakarta.persistence.Table

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
@Table(name = "FavoriteSounds")
data class FavoriteSound (
    @EmbeddedId
    val favoriteSoundId: FavoriteSoundId
)

@Embeddable
data class FavoriteSoundId (
    @field:Column(name = "userId")
    val userId: String,
    @field:Column(name = "soundId")
    val soundId: String
)