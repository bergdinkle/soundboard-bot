package dev.dinkleberg.soundboard.bot

import dev.dinkleberg.soundboard.bot.controller.dto.StatusDto
import dev.dinkleberg.soundboard.bot.soundboard.SoundboardService
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.annotation.Property
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count

const val ERROR_MESSAGE = ":("

@Singleton
open class DiscordBot(
    private val soundboardService: SoundboardService,
    private val kord: Kord,
    @Property(name = "auto-join") private val autoJoin: Boolean
) : ApplicationEventListener<StartupEvent> {

    private val logger = KotlinLogging.logger {}

    private var currentChannel: String? = null
    private var currentChannelId: Snowflake? = null

    private suspend fun init() {
        kord.on<MessageCreateEvent> {
            if (message.author?.isBot != false) return@on

            val messageContent = message.content
            val author = message.author ?: return@on

            logger.debug { "Received message from ${author.username}: $message" }


            when (messageContent) {
                "!join" -> soundboardService.joinChannel(member)
                "!leave" -> soundboardService.leaveChannel()
            }
        }

        kord.on<VoiceStateUpdateEvent> {
            if (kord.selfId == state.userId) {
                currentChannel = state.getChannelOrNull()?.asChannelOrNull()?.name
                currentChannelId = state.getChannelOrNull()?.id
            }

            val botLeftChannel = kord.selfId == state.userId && old?.channelId != null && state.channelId == null
            if (botLeftChannel) {
                logger.info { "Cleared voice connection" }
                soundboardService.clearVoiceConnection()
            }

            val userJoinedChannel = kord.selfId != state.userId && //Was not the bot
                old?.channelId != state.channelId && //User changed channel
                state.channelId != null //User did not disconnect
            if (autoJoin && userJoinedChannel) {
                val channel = state.getChannelOrNull() ?: return@on
                val voiceStates = channel.voiceStates

                if (voiceStates.count() >= 2) {
                    delay(1000)
                    val channelName = channel.asChannelOrNull()?.name
                    logger.info { "Auto join channel $channelName" }
                    soundboardService.joinChannel(state.getMemberOrNull() ?: return@on)
                }
            }

            val userLeftOwnChannel = kord.selfId != state.userId && //Was not the bot
                currentChannelId != null && //Bot is connected
                old?.channelId == currentChannelId && //User left current channel
                old?.channelId != state.channelId //User changed channel
            if (userLeftOwnChannel) {
                val channel = old?.getChannelOrNull() ?: return@on
                val voiceStates = channel.voiceStates

                if (voiceStates.count() == 1) {
                    delay(1000)
                    logger.info { "Leaved channel because no one is connected" }
                    soundboardService.leaveChannel(playSound = false)
                }
            }

        }

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onApplicationEvent(event: StartupEvent?) {
        GlobalScope.launch { init() }
    }

    fun getStatus() = StatusDto(
        connected = currentChannel != null,
        currentChannel = currentChannel
    )
}
