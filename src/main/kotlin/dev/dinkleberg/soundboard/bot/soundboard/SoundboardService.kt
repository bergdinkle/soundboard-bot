package dev.dinkleberg.soundboard.bot.soundboard

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.dinkleberg.soundboard.bot.controller.dto.*
import dev.dinkleberg.soundboard.bot.exception.FileTooLargeException
import dev.dinkleberg.soundboard.bot.exception.SoundNotFoundException
import dev.dinkleberg.soundboard.bot.exception.UnauthorizedException
import dev.dinkleberg.soundboard.bot.persistence.*
import dev.kord.core.entity.Member
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.rest.loadItem
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.annotation.Property
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
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
    private val favoriteSoundRepository: FavoriteSoundRepository,
    private val ffmpeg: FFmpeg,
    private val ffprobe: FFprobe,
    @Property(name = "sound.folder") private val soundFolder: String,
    @Property(name = "max-file-size") private val maxFileSize: Int,
    private val eventSoundService: EventSoundService,
    private val youTubeDownloadService: YouTubeDownloadService,
    private val lavaKord: LavaKord

) : ApplicationEventListener<StartupEvent> {

    private val logger = KotlinLogging.logger {}

    suspend fun listAllSounds(user: UserDto): List<SoundDto> {
        val users = userRepository.findAll().toList().associateBy { it.id }
        val favorites = favoriteSoundRepository.findByUserId(user.id)
            .associateBy { it.favoriteSoundId.soundId }
        return soundRepository.findAll().map {
            SoundDto(
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
    open suspend fun addFileSound(user: UserDto, soundUploadDto: FileSoundUploadDto): SoundDto {
        return addSound(user, soundUploadDto) { path ->
            val rawData = Base64.decode(soundUploadDto.data)
            if (rawData.size > maxFileSize) {
                throw FileTooLargeException()
            }
            normalizeAndSave(path, rawData)
        }
    }

    @Transactional
    open suspend fun addYouTubeSound(user: UserDto, soundUploadDto: YouTubeSoundUploadDto): SoundDto {
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

    open suspend fun addSound(user: UserDto, soundUploadDto: SoundUploadDto, saveAction: (path: Path) -> Unit): SoundDto {
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
        return SoundDto(
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

    private var link: Link? = null

    suspend fun playSound(soundId: String) {
        if (link == null || link?.state != Link.State.CONNECTED) {
            return
        }

        val sound = soundRepository.findById(soundId) ?: throw SoundNotFoundException(soundId)

        when (val item = link?.loadItem(sound.localPath)) {
            is LoadResult.TrackLoaded -> link?.player?.playTrack(item.data)
            else -> logger.warn { item }
        }
    }

    suspend fun joinChannel(member: Member?) {
        link?.destroy()

        val channel = member?.getVoiceState()?.getChannelOrNull() ?: return
        link = lavaKord.getLink(member.guildId.value)
        link?.connectAudio(channel.id)

        delay(500)
        playRandomSoundForEvent(Event.JOIN_SELF)
    }

    suspend fun leaveChannel(playSound: Boolean = true) {
        if (playSound) {
            val played = playRandomSoundForEvent(Event.LEAVE_SELF)
            if (played) delay(3000)
        }
        link?.destroy()
        link = null
    }

    suspend fun playRandomSoundForEvent(event: Event): Boolean {
        return eventSoundService.getRandomSoundForEvent(event)?.let { playSound(it); it } != null
    }

    fun clearVoiceConnection() {
        link = null
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

    override fun onApplicationEvent(event: StartupEvent?) {
        val soundFolderPath = Paths.get(soundFolder)
        if (Files.exists(soundFolderPath)) {
            return
        }
        Files.createDirectory(soundFolderPath)
    }

}
