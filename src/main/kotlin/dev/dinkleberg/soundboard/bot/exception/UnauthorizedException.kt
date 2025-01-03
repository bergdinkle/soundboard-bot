package dev.dinkleberg.soundboard.bot.exception

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

class UnauthorizedException : HttpStatusException(
    HttpStatus.UNAUTHORIZED,
    ""
)