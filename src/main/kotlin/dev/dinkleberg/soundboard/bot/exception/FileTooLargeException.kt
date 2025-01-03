package dev.dinkleberg.soundboard.bot.exception

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

class FileTooLargeException : HttpStatusException(
    HttpStatus.REQUEST_ENTITY_TOO_LARGE,
    "File is too large"
)