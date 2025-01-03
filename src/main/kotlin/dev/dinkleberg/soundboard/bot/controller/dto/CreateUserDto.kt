package dev.dinkleberg.soundboard.bot.controller.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class CreateUserDto(
    val name: String
)
