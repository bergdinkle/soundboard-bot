package dev.dinkleberg.soundboard.bot.soundboard

import dev.dinkleberg.soundboard.bot.persistence.EventSoundRepository
import io.micronaut.scheduling.annotation.Async
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

@Singleton
open class EventSoundService(
    private val eventSoundRepository: EventSoundRepository
) {

    private var eventSoundMap = emptyMap<Event, List<String>>()

    @Async
    @Scheduled(fixedDelay = "1m")
    open fun syncEventSounds() = runBlocking {
        eventSoundMap = eventSoundRepository.findAll().toList().groupBy ({ it.event }, { it.soundId })
    }

    fun getRandomSoundForEvent(event: Event): String? = eventSoundMap[event]?.random()
}