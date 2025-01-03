package dev.dinkleberg.soundboard.bot.controller.dto

import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable
abstract class SoundUploadDto{
    abstract val name: String
    abstract val tags: List<String>?
}

@Serdeable
data class FileSoundUploadDto(
    override val name: String,
    override val tags: List<String>?,
    @Schema(description = "Base64 Encoded String of Sound Data") val data: String,
) : SoundUploadDto()

@Serdeable
data class YouTubeSoundUploadDto(
    override val name: String,
    override val tags: List<String>?,
    val link: String,
) : SoundUploadDto()