package dev.dinkleberg.soundboard.bot.controller.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class CreateUserResponseDto(
    val name: String,
    val token: String
)
