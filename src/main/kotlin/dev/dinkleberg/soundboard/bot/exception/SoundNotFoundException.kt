package dev.dinkleberg.soundboard.bot.exception

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

class SoundNotFoundException(soundId: String) : HttpStatusException(
    HttpStatus.NOT_FOUND,
    "Sound with ID $soundId not found"
)