package dev.dinkleberg.soundboard.bot

import dev.dinkleberg.soundboard.bot.controller.dto.StatusDto
import dev.dinkleberg.soundboard.bot.soundboard.SoundboardService
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull

const val ERROR_MESSAGE = ":("

@Singleton
open class DiscordBot(
    private val soundboardService: SoundboardService,
    private val kord: Kord
) : ApplicationEventListener<StartupEvent> {

    private val logger = KotlinLogging.logger {}

    private var currentChannel: String? = null

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
            }

            if (old?.channelId != null && state.channelId == null && kord.selfId == state.userId) {
                logger.info { "Cleared voice connection" }
                soundboardService.clearVoiceConnection()
            }

            if (old?.channelId != null && old!!.channelId != state.channelId && kord.selfId != state.userId) {
                val channel = old?.getChannelOrNull() ?: return@on

                val voiceStates = channel.voiceStates

                if (voiceStates.firstOrNull { it.userId == kord.selfId } != null && voiceStates.count() == 1) {
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
