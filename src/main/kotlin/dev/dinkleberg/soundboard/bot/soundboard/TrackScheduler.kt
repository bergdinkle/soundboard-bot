package dev.dinkleberg.soundboard.bot.soundboard

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackScheduler(private val player: AudioPlayer) : AudioLoadResultHandler {
    override fun trackLoaded(track: AudioTrack?) {
        player.playTrack(track)
    }

    override fun playlistLoaded(playlist: AudioPlaylist?) {
        println("playlist")
    }

    override fun noMatches() {
        println("No Matches")
    }

    override fun loadFailed(exception: FriendlyException?) {
        println(exception)
    }
}