package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository

@R2dbcRepository(dialect = Dialect.H2)
interface FavoriteSoundRepository: CoroutineCrudRepository<dev.dinkleberg.soundboard.bot.persistence.FavoriteSound, dev.dinkleberg.soundboard.bot.persistence.FavoriteSoundId> {
    @Query("MERGE INTO FavoriteSounds(userId, soundId) VALUES (:userId, :soundId)")
    suspend fun insertIgnoreIfPresent(userId: String, soundId: String)
    suspend fun findByUserId(userId: String): List<dev.dinkleberg.soundboard.bot.persistence.FavoriteSound>

    //Workaround
    override suspend fun deleteById(id: dev.dinkleberg.soundboard.bot.persistence.FavoriteSoundId): Int
}