package com.uysal23.newchinese.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer

class AssetAudioPlayer(private val context: Context) {
    private val player = ExoPlayer.Builder(context).build()

    fun exists(assetPath: String): Boolean =
        runCatching {
            context.assets.open(assetPath).use { }
            true
        }.getOrDefault(false)

    fun play(assetPath: String, speed: Float = 1.0f): Boolean {
        if (!exists(assetPath)) return false
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri("asset:///$assetPath"))
        player.playbackParameters = PlaybackParameters(speed)
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

    fun pause() {
        player.pause()
    }

    fun release() {
        player.release()
    }
}
