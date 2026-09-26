package com.uysal23.newchinese.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AssetAudioPlayer(private val context: Context) {
    private var onEnded: (() -> Unit)? = null
    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    this@AssetAudioPlayer.onEnded?.invoke()
                }
            }
        })
    }

    fun exists(assetPath: String): Boolean =
        runCatching {
            context.assets.open(assetPath).use { }
            true
        }.getOrDefault(false)

    fun play(assetPath: String, speed: Float = 1.0f, startPositionMs: Long = 0L, onEnded: (() -> Unit)? = null): Boolean {
        if (!exists(assetPath)) return false
        this.onEnded = onEnded
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri("asset:///$assetPath"))
        player.playbackParameters = PlaybackParameters(speed)
        if (startPositionMs > 0L) player.seekTo(startPositionMs)
        player.prepare()
        player.play()
        return true
    }

    fun playFile(absolutePath: String, speed: Float = 1.0f): Boolean {
        val file = java.io.File(absolutePath)
        if (!file.exists() || file.length() == 0L) return false
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        player.playbackParameters = PlaybackParameters(speed)
        player.prepare()
        player.play()
        return true
    }

    fun currentPositionMs(): Long = player.currentPosition.coerceAtLeast(0L)

    fun pause() {
        player.pause()
        onEnded = null
    }

    fun release() {
        onEnded = null
        player.release()
    }
}
