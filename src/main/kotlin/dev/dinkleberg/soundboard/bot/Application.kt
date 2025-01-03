package dev.dinkleberg.soundboard.bot

import io.micronaut.runtime.Micronaut.run
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "Soundboard Bot",
        version = "0.1",
        description = "Soundboard Bot"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        run(*args)
    }
}

