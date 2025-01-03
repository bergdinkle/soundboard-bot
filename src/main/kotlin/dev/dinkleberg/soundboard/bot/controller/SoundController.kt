package dev.dinkleberg.soundboard.bot.controller

import dev.dinkleberg.soundboard.bot.controller.dto.*
import dev.dinkleberg.soundboard.bot.controller.swagger.DefaultApiResponses
import dev.dinkleberg.soundboard.bot.controller.swagger.ErrorApiResponses
import dev.dinkleberg.soundboard.bot.soundboard.SoundboardService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.server.types.files.SystemFile
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory

@Controller("/api/sound")
class SoundController(
    private val soundboardService: SoundboardService,
    private val securityService: SecurityService
) {
    private val logger = LoggerFactory.getLogger(SoundController::class.java)

    @Get
    @Operation(summary = "List All Sounds")
    @DefaultApiResponses
    suspend fun listAllSounds(@Header("Auth-Token") authToken: String): List<dev.dinkleberg.soundboard.bot.controller.dto.SoundDto> {
        val user = securityService.verifyToken(authToken)
        return soundboardService.listAllSounds(user)
    }

    @Post
    @Status(HttpStatus.CREATED)
    @Operation(summary = "Add Sound")
    @ApiResponse(
        responseCode = "201",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = SoundUploadDto::class)
        )]
    )
    @ApiResponse(responseCode = "400")
    @ErrorApiResponses
    suspend fun addSound(@Header("Auth-Token") authToken: String, @Body soundUploadDto: FileSoundUploadDto): dev.dinkleberg.soundboard.bot.controller.dto.SoundDto {
        if (soundUploadDto.tags != null && soundUploadDto.tags.size > 3) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "")
        }
        val user = securityService.verifyToken(authToken)
        return soundboardService.addFileSound(user, soundUploadDto)
    }

    @Post("/youtube")
    @Status(HttpStatus.CREATED)
    @Operation(summary = "Add Sound")
    @ApiResponse(
        responseCode = "201",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = SoundUploadDto::class)
        )]
    )
    @ApiResponse(responseCode = "400")
    @ErrorApiResponses
    suspend fun addYouTubeSound(@Header("Auth-Token") authToken: String, @Body soundUploadDto: YouTubeSoundUploadDto): dev.dinkleberg.soundboard.bot.controller.dto.SoundDto {
        if (soundUploadDto.tags != null && soundUploadDto.tags.size > 3) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "")
        }
        val user = securityService.verifyToken(authToken)
        return soundboardService.addYouTubeSound(user, soundUploadDto)
    }

    @Delete("/{soundId}")
    @Operation(summary = "Delete Sound")
    @DefaultApiResponses
    @ApiResponse(responseCode = "404")
    suspend fun deleteSound(@Header("Auth-Token") authToken: String, soundId: String): HttpResponse<Any> {
        val user = securityService.verifyToken(authToken)
        soundboardService.deleteSound(soundId, user)
        return HttpResponse.ok()
    }

    @Post("/{soundId}/play")
    @Operation(summary = "Play Sound")
    @DefaultApiResponses
    @ApiResponse(responseCode = "404")
    suspend fun playSound(@Header("Auth-Token") authToken: String, soundId: String): HttpResponse<Any> {
        securityService.verifyToken(authToken)
        soundboardService.playSound(soundId)
        return HttpResponse.ok()
    }

    @Post("/{soundId}/favorite")
    @Operation(summary = "Favorite Sound for own User")
    @DefaultApiResponses
    @ApiResponse(responseCode = "404")
    suspend fun favoriteSound(@Header("Auth-Token") authToken: String, soundId: String, @Body soundFavoriteDto: SoundFavoriteDto): HttpResponse<Any> {
        val user = securityService.verifyToken(authToken)
        soundboardService.favoriteSound(user, soundId, soundFavoriteDto.favorite)
        return HttpResponse.ok()
    }

    @Get("/{soundId}/download")
    @Operation(summary = "Download Sound")
    @ErrorApiResponses
    @ApiResponse(
        responseCode = "200",
        content = [Content(mediaType = "audio/mpeg")]
    )
    @ApiResponse(responseCode = "404")
    suspend fun downloadSound(@Header("Auth-Token") authTokenHeader: String?, @CookieValue("Auth-Token") authTokenCookie: String?, soundId: String): SystemFile {
        val authToken = authTokenHeader ?: authTokenCookie ?: throw HttpStatusException(HttpStatus.UNAUTHORIZED, "")
        securityService.verifyToken(authToken)
        val path = soundboardService.downloadSound(soundId)
        return SystemFile(path.toFile()).attach("$soundId.mp3")
    }
}
