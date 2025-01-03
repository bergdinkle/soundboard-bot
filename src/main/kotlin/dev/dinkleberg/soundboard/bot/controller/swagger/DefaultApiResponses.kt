package dev.dinkleberg.soundboard.bot.controller.swagger

import io.micronaut.http.MediaType
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ApiResponse(responseCode = "200")
@ErrorApiResponses
annotation class DefaultApiResponses()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@ApiResponse(
    responseCode = "401"
)
@ApiResponse(
    responseCode = "500",
    description = "Unexpected error occured",
    content = [Content(
        mediaType = MediaType.APPLICATION_JSON
    )]
)
annotation class ErrorApiResponses()
