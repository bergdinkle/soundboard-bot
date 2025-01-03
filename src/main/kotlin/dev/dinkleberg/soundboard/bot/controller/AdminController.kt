package dev.dinkleberg.soundboard.bot.controller

import dev.dinkleberg.soundboard.bot.controller.dto.CreateUserDto
import dev.dinkleberg.soundboard.bot.controller.dto.CreateUserResponseDto
import dev.dinkleberg.soundboard.bot.controller.swagger.ErrorApiResponses
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Controller("/api/admin")
open class AdminController(
    private val securityService: SecurityService
) {

    @Post("/user")
    @Status(HttpStatus.CREATED)
    @Operation(summary = "Create User")
    @ApiResponse(
        responseCode = "201",
        content = [Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = Schema(implementation = CreateUserResponseDto::class)
        )]
    )
    @ErrorApiResponses
    suspend fun createUser(@Header("Auth-Token") authToken: String, @Body createUserDto: CreateUserDto): CreateUserResponseDto {
        securityService.verifyAdminToken(authToken)
        val token = securityService.createUser(createUserDto.name)
        return CreateUserResponseDto(createUserDto.name, token)
    }
}
