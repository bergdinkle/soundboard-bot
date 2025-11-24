package dev.dinkleberg.soundboard.bot.soundboard

import dev.kord.core.Kord
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton

@Factory
class LavaKordFactory {
    @Singleton
    fun getLavaKord(
        kord: Kord,
        @Property(name = "lavalink.host") lavalinkHost: String,
        @Property(name = "lavalink.port") lavalinkPort: Int,
        @Property(name = "lavalink.password") lavalinkPassword: String,
    ): LavaKord {
        val lavakord = kord.lavakord()
        lavakord.addNode("ws://$lavalinkHost:$lavalinkPort", lavalinkPassword)
        return lavakord
    }
}
