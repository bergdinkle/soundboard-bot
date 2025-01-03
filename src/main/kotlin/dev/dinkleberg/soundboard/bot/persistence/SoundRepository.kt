package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow

@R2dbcRepository(dialect = Dialect.H2)
interface SoundRepository: CoroutineCrudRepository<Sound, String> {
    //Workaround
    override suspend fun deleteById(id: String): Int
    override fun findAll(): Flow<Sound>
    override suspend fun findById(id: String): Sound?
    override suspend fun <S : Sound> save(entity: S): S
}