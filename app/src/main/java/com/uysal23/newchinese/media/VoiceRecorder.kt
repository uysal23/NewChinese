package com.uysal23.newchinese.media

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(): Result<File> = runCatching {
        stopAndRelease()
        val file = File.createTempFile("shadowing_", ".m4a", context.cacheDir)
        val mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        currentFile = file
        file
    }

    fun stop(): Result<File?> = runCatching {
        val file = currentFile
        recorder?.stop()
        stopAndRelease()
        file
    }

    fun cancel() {
        val file = currentFile
        runCatching { recorder?.stop() }
        stopAndRelease()
        file?.delete()
    }

    fun release() {
        stopAndRelease()
    }

    private fun stopAndRelease() {
        recorder?.runCatching { reset() }
        recorder?.release()
        recorder = null
        currentFile = null
    }
}
