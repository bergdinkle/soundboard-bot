package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow

@R2dbcRepository(dialect = Dialect.H2)
interface EventSoundRepository: CoroutineCrudRepository<EventSound, String> {
    //Workaround
    override fun findAll(): Flow<EventSound>
}