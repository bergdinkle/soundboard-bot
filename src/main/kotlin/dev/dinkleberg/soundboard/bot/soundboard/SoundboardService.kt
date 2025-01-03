package dev.dinkleberg.soundboard.bot.soundboard

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.dinkleberg.soundboard.bot.controller.dto.*
import dev.dinkleberg.soundboard.bot.exception.FileTooLargeException
import dev.dinkleberg.soundboard.bot.exception.SoundNotFoundException
import dev.dinkleberg.soundboard.bot.exception.UnauthorizedException
import dev.dinkleberg.soundboard.bot.persistence.*
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.connect
import dev.kord.core.entity.Member
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

const val AUDIO_CODEC = "libopus"
const val AUDIO_NORMALIZATION_FILTER = "loudnorm"

const val DOWNLOAD_AUDIO_CODEC = "mp3"

@Singleton
open class SoundboardService(
    private val soundRepository: SoundRepository,
    private val userRepository: UserRepository,
    private val favoriteSoundRepository: dev.dinkleberg.soundboard.bot.persistence.FavoriteSoundRepository,
    private val ffmpeg: FFmpeg,
    private val ffprobe: FFprobe,
    @Property(name = "sound.folder") private val soundFolder: String,
    @Property(name = "max-file-size") private val maxFileSize: Int,
    private val eventSoundService: EventSoundService,
    private val youTubeDownloadService: dev.dinkleberg.soundboard.bot.soundboard.YouTubeDownloadService
) {
    private val playerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    private val player = playerManager.createPlayer()
    private val trackScheduler = TrackScheduler(player)

    suspend fun listAllSounds(user: UserDto): List<dev.dinkleberg.soundboard.bot.controller.dto.SoundDto> {
        val users = userRepository.findAll().toList().associateBy { it.id }
        val favorites = favoriteSoundRepository.findByUserId(user.id)
            .associateBy { it.favoriteSoundId.soundId }
        return soundRepository.findAll().map {
            dev.dinkleberg.soundboard.bot.controller.dto.SoundDto(
                id = it.id,
                name = it.name,
                submittedById = it.submittedBy,
                submittedByName = users[it.submittedBy]?.name ?: "",
                tags = it.tags,
                favorite = favorites[it.id] != null
            )
        }.toList().sortedWith(compareBy({ !it.favorite }, { it.name.lowercase()}))
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Transactional
    open suspend fun addFileSound(user: UserDto, soundUploadDto: FileSoundUploadDto): dev.dinkleberg.soundboard.bot.controller.dto.SoundDto {
        return addSound(user, soundUploadDto) { path ->
            val rawData = Base64.decode(soundUploadDto.data)
            if (rawData.size > maxFileSize) {
                throw FileTooLargeException()
            }
            normalizeAndSave(path, rawData)
        }
    }

    @Transactional
    open suspend fun addYouTubeSound(user: UserDto, soundUploadDto: YouTubeSoundUploadDto): dev.dinkleberg.soundboard.bot.controller.dto.SoundDto {
        return addSound(user, soundUploadDto ) { path ->
            val tmpFile = youTubeDownloadService.downloadYoutubeSound(soundUploadDto.link)
            val rawData = Files.readAllBytes(tmpFile)
            if (rawData.size > maxFileSize) {
                throw FileTooLargeException()
            }
            normalizeAndSave(path, rawData)
            Files.delete(tmpFile)
        }
    }

    open suspend fun addSound(user: UserDto, soundUploadDto: SoundUploadDto, saveAction: (path: Path) -> Unit): dev.dinkleberg.soundboard.bot.controller.dto.SoundDto {
        val id = generateIdFromName(soundUploadDto.name)
        val path = Paths.get(soundFolder, "$id.opus")
        val sound = Sound(
            id = id,
            name = soundUploadDto.name,
            localPath = path.toString(),
            submittedBy = user.id,
            tags = soundUploadDto.tags?.filterNotNull() ?: emptyList()
        )
        soundRepository.save(sound)
        withContext(Dispatchers.IO) {
            saveAction(path)
        }
        return dev.dinkleberg.soundboard.bot.controller.dto.SoundDto(
            id = sound.id,
            name = sound.name,
            submittedById = user.id,
            submittedByName = user.name,
            tags = emptyList(),
            favorite = false
        )
    }

    private fun normalizeAndSave(path: Path, rawData: ByteArray) {
        val tmpPath = Paths.get("/tmp", "${UUID.randomUUID()}.tmp")
        try {
            Files.write(tmpPath, rawData)
            normalizeSound(tmpPath, path)
        } finally {
            if (Files.exists(tmpPath)) {
                Files.delete(tmpPath)
            }
        }
    }

    private fun normalizeSound(input: Path, output: Path) {
        val builder = FFmpegBuilder()
            .setInput(input.toString())
            .addOutput(output.toString())
                .setAudioCodec(AUDIO_CODEC)
                .setAudioFilter(AUDIO_NORMALIZATION_FILTER)
            .done()
        val executor = FFmpegExecutor(ffmpeg, ffprobe)
        executor.createJob(builder).run()
    }

    private fun String.replaceAll(mappings: Map<String, String>, ignoreCase: Boolean = false): String {
        var s = this

        for ((oldValue, newValue) in mappings) {
            s = s.replace(oldValue, newValue, ignoreCase)
        }

        return s
    }

    private fun String.substringBeforeMaxSize(delimiter: String, max: Int): String {
        if (!this.contains(delimiter)) {
            return this.substring(0, if (max > this.length) this.length else max)
        }
        var s = this
        while (s.length > max) {
            s = s.substringBeforeLast(delimiter)
        }
        return s
    }

    private val replaceMap = mapOf(
        "ä" to "ae",
        "ö" to "oe",
        "ü" to "ue",
        "ß" to "ss",
    )

    private fun generateIdFromName(name: String): String {
        return name.lowercase()
            .trim()
            .replace(" ", "-")
            .replaceAll(replaceMap)
            .replace(Regex("[^A-Za-z0-9-]"), "")
            .substringBeforeMaxSize("-", 64)
    }

    suspend fun favoriteSound(user: UserDto, soundId: String, favorite: Boolean) {
        soundRepository.findById(soundId) ?: throw SoundNotFoundException(soundId)
        if (favorite) {
            favoriteSoundRepository.insertIgnoreIfPresent(user.id, soundId)
        } else {
            favoriteSoundRepository.deleteById(
                FavoriteSoundId(
                    user.id,
                    soundId
                )
            )
        }
    }

    @OptIn(KordVoice::class)
    private var voiceConnection: VoiceConnection? = null

    @OptIn(KordVoice::class)
    suspend fun playSound(soundId: String) {
        if (voiceConnection == null) {
            return
        }

        val sound = soundRepository.findById(soundId) ?: throw SoundNotFoundException(soundId)
        playerManager.loadItem(sound.localPath, trackScheduler)
    }

    @OptIn(KordVoice::class)
    suspend fun joinChannel(member: Member?) {
        voiceConnection?.shutdown()

        val channel = member?.getVoiceState()?.getChannelOrNull() ?: return
        voiceConnection = channel.connect {
            audioProvider { AudioFrame.fromData(player.provide()?.data) }
        }

        delay(500)
        playRandomSoundForEvent(Event.JOIN_SELF)
    }

    @OptIn(KordVoice::class)
    suspend fun leaveChannel(playSound: Boolean = true) {
        if (playSound) {
            val played = playRandomSoundForEvent(Event.LEAVE_SELF)
            if (played) delay(3000)
        }
        voiceConnection?.shutdown()
        voiceConnection = null

    }

    suspend fun playRandomSoundForEvent(event: Event): Boolean {
        return eventSoundService.getRandomSoundForEvent(event)?.let { playSound(it); it } != null
    }

    @OptIn(KordVoice::class)
    fun clearVoiceConnection() {
        voiceConnection = null
    }

    @Transactional
    open suspend fun deleteSound(soundId: String, user: UserDto) {
        val sound = soundRepository.findById(soundId) ?: throw SoundNotFoundException(soundId)

        if (sound.submittedBy == user.id || user.admin) {
            soundRepository.deleteById(soundId)
            val path = Paths.get(sound.localPath)
            withContext(Dispatchers.IO) {
                if (Files.exists(path)) Files.delete(path)
            }
        } else {
            throw UnauthorizedException()
        }
    }

    suspend fun downloadSound(soundId: String): Path {
        val sound = soundRepository.findById(soundId) ?: throw SoundNotFoundException(soundId)
        val tmpPath = Paths.get("/tmp", "${UUID.randomUUID()}.mp3")
        val builder = FFmpegBuilder()
            .setInput(sound.localPath)
            .addOutput(tmpPath.toString())
            .setAudioCodec(DOWNLOAD_AUDIO_CODEC)
            .done()
        val executor = FFmpegExecutor(ffmpeg, ffprobe)
        executor.createJob(builder).run()
        return tmpPath
    }

}