package dev.dinkleberg.soundboard.bot.controller

import dev.dinkleberg.soundboard.bot.controller.dto.UserDto
import dev.dinkleberg.soundboard.bot.exception.UnauthorizedException
import dev.dinkleberg.soundboard.bot.persistence.User
import dev.dinkleberg.soundboard.bot.persistence.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


@Singleton
open class SecurityService(
    private val userRepository: UserRepository,
    @Property(name = "token.secret") private val tokenSecret: String
) : ApplicationEventListener<StartupEvent> {

    private val logger = KotlinLogging.logger {}

    @Async
    override fun onApplicationEvent(event: StartupEvent?) = runBlocking {
        val adminCount = userRepository.findByAdminTrue().count()

        if (adminCount > 0) {
            return@runBlocking
        }

        val token = createUser("Admin", admin = true)
        logger.info { "Admin token: $token" }
    }

    @Cacheable(cacheNames = ["token-cache"])
    open suspend fun verifyToken(token: String): UserDto {
        val tokenHash = hashToken(token, tokenSecret)
        val user = userRepository.findByTokenHash(tokenHash) ?: throw UnauthorizedException()
        return userToUserDto(user)
    }

    suspend fun verifyAdminToken(token: String): UserDto {
        val tokenHash = hashToken(token, tokenSecret)
        val user = userRepository.findByTokenHash(tokenHash) ?: throw UnauthorizedException()
        if (user.admin) return userToUserDto(user) else throw UnauthorizedException()
    }

    private fun userToUserDto(user: User) = UserDto(
        id = user.id,
        name = user.name,
        admin = user.admin,
        createdAt = user.createdAt
    )

    suspend fun createUser(name: String, admin: Boolean = false): String {
        val token = UUID.randomUUID().toString()
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            tokenHash = hashToken(token, tokenSecret),
            admin = admin,
        )
        userRepository.save(user)
        return token
    }

    private fun hashToken(token: String, salt: String): String {
        val saltBytes = salt.toByteArray()
        val spec: KeySpec = PBEKeySpec(token.toCharArray(), saltBytes, 210_000, 128)
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            val bytes = factory.generateSecret(spec).encoded
            return fromBytes(bytes)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Could not hash Password", e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException("Could not hash Password", e)
        }
    }

    private fun fromBytes(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append(((bytes[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }
}
