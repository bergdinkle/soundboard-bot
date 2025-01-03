package dev.dinkleberg.soundboard.bot.soundboard

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import java.nio.file.Paths

@Factory
class FFmpegFactory {

    @Singleton
    fun getFFmpeg(@Property(name = "ffmpeg-base-path") ffmpegBasePath: String) = FFmpeg(Paths.get(ffmpegBasePath, "ffmpeg").toString())

    @Singleton
    fun getFFprobe(@Property(name = "ffmpeg-base-path") ffmpegBasePath: String) = FFprobe(Paths.get(ffmpegBasePath, "ffprobe").toString())
}