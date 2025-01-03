package dev.dinkleberg.soundboard.bot.persistence

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow

@R2dbcRepository(dialect = Dialect.H2)
interface UserRepository: CoroutineCrudRepository<User, String> {
    suspend fun findByTokenHash(tokenHash: String): User?
    fun findByAdminTrue(): Flow<User>

    //Workaround
    override fun findAll(): Flow<User>
    override suspend fun <S : User> save(entity: S): S
}