package dev.dinkleberg.soundboard.bot.controller

import dev.dinkleberg.soundboard.bot.DiscordBot
import dev.dinkleberg.soundboard.bot.exception.UnauthorizedException
import dev.dinkleberg.soundboard.bot.soundboard.SoundboardService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.views.ModelAndView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Controller
class GuiController(
    private val securityService: SecurityService,
    private val soundboardService: SoundboardService,
    private val discordBot: DiscordBot
) {

    @Get
    @Operation(summary = "Gui")
    @ApiResponse(
        responseCode = "200",
        content = [Content(mediaType = MediaType.TEXT_HTML)]
    )
    suspend fun home(@CookieValue("Auth-Token") token: String?): ModelAndView<Any> {
        if (token == null) {
            return ModelAndView("unauthorized", null)
        }
        try {
            val user = securityService.verifyToken(token)
            return ModelAndView("home", mapOf(
                "sounds" to soundboardService.listAllSounds(user),
                "status" to discordBot.getStatus(),
                "user" to user
            ))
        } catch (e: UnauthorizedException) {
            return ModelAndView("unauthorized", null)
        }
    }
}
