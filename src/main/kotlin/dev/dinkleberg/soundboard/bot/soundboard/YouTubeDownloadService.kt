package dev.dinkleberg.soundboard.bot.soundboard

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Singleton
class YouTubeDownloadService(
    @Property(name = "yt-dlp-base-path") private val youTubeDownloaderBasePath: String
) {

    private val logger = KotlinLogging.logger {}

    fun downloadYoutubeSound(youTubeUrl: String): Path {
        val tempFileName = "/tmp/${UUID.randomUUID()}.mp3"

        runCommand(
            command = arrayOf(
                Paths.get(youTubeDownloaderBasePath, "yt-dlp").toString(),
                "--extract-audio",
                "--audio-format", "mp3",
                "--audio-quality", "0",
                "--output", tempFileName,
                youTubeUrl
            )
        )

        return Paths.get(tempFileName)
    }

    private fun runCommand(workingDirectory: String = "/", command: Array<String>) {
        logger.debug { "Run command ${command.joinToString { " " }} in directory $workingDirectory" }

        val process = ProcessBuilder()
            .command(*command)
            .directory(File(workingDirectory))
            .start()

        BufferedReader(InputStreamReader(process.inputStream))
            .lines()
            .forEach {
                logger.debug { "Command output: $it" }
            }

        val exitCode = process.waitFor()
        logger.debug { "Command finished with code $exitCode" }
        if (exitCode != 0) {
            throw RuntimeException("Command exited with code $exitCode")
        }
    }

}
