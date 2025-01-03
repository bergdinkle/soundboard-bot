package dev.dinkleberg.soundboard.bot

import dev.kord.core.Kord
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Factory
class KordFactory {
    @Singleton
    fun initKord(@Property(name = "discord.token") botToken: String): Kord = runBlocking {
        Kord(token = botToken)
    }
}