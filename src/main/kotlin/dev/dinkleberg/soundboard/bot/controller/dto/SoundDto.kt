package dev.dinkleberg.soundboard.bot.controller.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class SoundDto(
    val id: String,
    val name: String,
    val submittedById: String,
    val submittedByName: String,
    val tags: List<String>,
    val favorite: Boolean
)