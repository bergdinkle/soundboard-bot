package dev.dinkleberg.soundboard.bot.controller.dto

import java.time.OffsetDateTime

data class UserDto(
    val id: String,
    val name: String,
    val admin: Boolean,
    val createdAt: OffsetDateTime,
)
