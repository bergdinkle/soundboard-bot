package dev.dinkleberg.soundboard.bot.controller

import dev.dinkleberg.soundboard.bot.DiscordBot
import dev.dinkleberg.soundboard.bot.controller.dto.StatusDto
import dev.dinkleberg.soundboard.bot.controller.swagger.DefaultApiResponses
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/status")
class StatusController(
    private val securityService: SecurityService,
    private val discordBot: DiscordBot
) {

    @Get
    @Operation(summary = "Get Status of the Bot")
    @DefaultApiResponses
    suspend fun getStatus(@Header("Auth-Token") authToken: String): StatusDto {
        securityService.verifyToken(authToken)
        return discordBot.getStatus()
    }
}
