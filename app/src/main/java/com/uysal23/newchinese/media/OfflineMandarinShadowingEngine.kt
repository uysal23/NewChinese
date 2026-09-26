package com.uysal23.newchinese.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import kotlin.math.log10
import kotlin.math.sqrt

data class OfflineShadowingResult(
    val recognizedText: String,
    val similarityPercent: Int,
    val recordingPath: String
)

class OfflineMandarinShadowingEngine(
    private val context: Context
) {
    companion object {
        private const val MODEL_ASSET_PATH = "models/vosk-model-small-cn-0.22"
        private const val SAMPLE_RATE = 16_000
        private const val MAX_RECORDING_MS = 15_000L
        private const val NO_SPEECH_TIMEOUT_MS = 5_000L
        private const val SILENCE_AFTER_SPEECH_MS = 950L
        private const val SPEECH_DB_THRESHOLD = -42.0
    }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var model: Model? = null

    @Volatile
    private var preparing = false

    @Volatile
    private var recording = false

    @Volatile
    private var cancelled = false

    private var recordingThread: Thread? = null
    private var activeAudioRecord: AudioRecord? = null
    private var activeFile: File? = null

    fun isReady(): Boolean = model != null

    fun prepare(
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (model != null) {
            mainHandler.post(onReady)
            return
        }
        if (preparing) return

        preparing = true
        executor.execute {
            try {
                val modelDir = File(appContext.filesDir, "vosk-model-small-cn-0.22")
                if (!isModelInstalled(modelDir)) {
                    modelDir.deleteRecursively()
                    modelDir.mkdirs()
                    copyAssetTree(MODEL_ASSET_PATH, modelDir)
                }

                val loadedModel = Model(modelDir.absolutePath)
                model = loadedModel
                preparing = false
                mainHandler.post(onReady)
            } catch (t: Throwable) {
                preparing = false
                mainHandler.post {
                    onError(
                        "Çevrimdışı Mandarin modeli hazırlanamadı: " +
                            (t.message ?: "bilinmeyen hata")
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start(
        targetText: String,
        onResult: (OfflineShadowingResult) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        val loadedModel = model ?: run {
            onError("Mandarin modeli henüz hazır değil.")
            return false
        }
        if (recording) return false

        cancelled = false
        recording = true

        val outputFile = File.createTempFile("shadowing_", ".wav", appContext.cacheDir)
        activeFile = outputFile

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) {
            recording = false
            onError("Mikrofon başlatılamadı.")
            return false
        }

        val bufferSamples = maxOf(minBuffer / 2, 3200)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSamples * 2
        )
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            recording = false
            onError("Mikrofon kullanılamıyor.")
            return false
        }

        activeAudioRecord = audioRecord

        val thread = Thread {
            var recognizer: Recognizer? = null
            var output: FileOutputStream? = null
            var totalSamples = 0L
            var speechStarted = false
            var lastSpeechAt = 0L
            val startedAt = System.currentTimeMillis()
            val recognizedParts = mutableListOf<String>()

            try {
                recognizer = Recognizer(loadedModel, SAMPLE_RATE.toFloat())
                output = FileOutputStream(outputFile)
                output.write(ByteArray(44))

                audioRecord.startRecording()
                if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                    throw IllegalStateException("Mikrofon kaydı başlatılamadı.")
                }

                val buffer = ShortArray(bufferSamples)
                while (recording && !cancelled) {
                    val count = audioRecord.read(buffer, 0, buffer.size)
                    if (count <= 0) continue

                    writePcm16(output, buffer, count)
                    totalSamples += count

                    val now = System.currentTimeMillis()
                    val db = calculateDb(buffer, count)
                    if (db >= SPEECH_DB_THRESHOLD) {
                        speechStarted = true
                        lastSpeechAt = now
                    }

                    val endpoint = recognizer.acceptWaveForm(buffer, count)
                    if (endpoint) {
                        parseText(recognizer.result)
                            .takeIf { it.isNotBlank() }
                            ?.let(recognizedParts::add)
                        if (speechStarted) break
                    }

                    if (speechStarted && now - lastSpeechAt >= SILENCE_AFTER_SPEECH_MS) {
                        break
                    }

                    if (!speechStarted && now - startedAt >= NO_SPEECH_TIMEOUT_MS) {
                        break
                    }

                    if (now - startedAt >= MAX_RECORDING_MS) {
                        break
                    }
                }

                runCatching { audioRecord.stop() }
                output.flush()
                output.close()
                output = null

                if (!cancelled) {
                    parseText(recognizer.finalResult)
                        .takeIf { it.isNotBlank() }
                        ?.let(recognizedParts::add)

                    finalizeWav(outputFile, totalSamples)
                    val recognized = recognizedParts
                        .joinToString("")
                        .replace(" ", "")
                        .trim()

                    recording = false
                    activeAudioRecord = null

                    if (recognized.isBlank()) {
                        mainHandler.post {
                            onError("Konuşma anlaşılamadı. Tekrar deneyebilirsin.")
                        }
                    } else {
                        val result = OfflineShadowingResult(
                            recognizedText = recognized,
                            similarityPercent = similarityPercent(targetText, recognized),
                            recordingPath = outputFile.absolutePath
                        )
                        mainHandler.post { onResult(result) }
                    }
                } else {
                    outputFile.delete()
                }
            } catch (t: Throwable) {
                runCatching { audioRecord.stop() }
                if (!cancelled) {
                    mainHandler.post {
                        onError("Mandarin konuşma tanıma tamamlanamadı: " + (t.message ?: "hata"))
                    }
                }
                outputFile.delete()
            } finally {
                recording = false
                activeAudioRecord = null
                runCatching { output?.close() }
                runCatching { audioRecord.release() }
                runCatching { recognizer?.close() }
            }
        }.apply {
            name = "VoskShadowingRecorder"
            isDaemon = true
        }

        recordingThread = thread
        thread.start()
        return true
    }

    fun stop() {
        // Let the recorder loop exit cleanly on its next short read so
        // Vosk can still emit a final result for manual stop.
        recording = false
    }

    fun cancel() {
        cancelled = true
        recording = false
        runCatching { activeAudioRecord?.stop() }
        activeFile?.delete()
        activeFile = null
    }

    fun release() {
        cancel()
        runCatching { recordingThread?.join(500) }
        recordingThread = null
        executor.shutdownNow()
        runCatching { model?.close() }
        model = null
    }

    private fun isModelInstalled(dir: File): Boolean =
        File(dir, "am/final.mdl").isFile &&
            File(dir, "conf/mfcc.conf").isFile

    private fun copyAssetTree(assetPath: String, destination: File) {
        val children = appContext.assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            destination.parentFile?.mkdirs()
            appContext.assets.open(assetPath).use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return
        }

        destination.mkdirs()
        children.forEach { child ->
            copyAssetTree(
                "$assetPath/$child",
                File(destination, child)
            )
        }
    }

    private fun parseText(json: String): String =
        runCatching { JSONObject(json).optString("text", "") }
            .getOrDefault("")

    private fun calculateDb(samples: ShortArray, count: Int): Double {
        if (count <= 0) return -120.0
        var sum = 0.0
        for (i in 0 until count) {
            val value = samples[i].toDouble()
            sum += value * value
        }
        val rms = sqrt(sum / count)
        if (rms <= 1.0) return -120.0
        return 20.0 * log10(rms / Short.MAX_VALUE.toDouble())
    }

    private fun writePcm16(
        output: FileOutputStream,
        samples: ShortArray,
        count: Int
    ) {
        val bytes = ByteArray(count * 2)
        var offset = 0
        for (i in 0 until count) {
            val value = samples[i].toInt()
            bytes[offset++] = (value and 0xFF).toByte()
            bytes[offset++] = ((value shr 8) and 0xFF).toByte()
        }
        output.write(bytes)
    }

    private fun finalizeWav(file: File, totalSamples: Long) {
        val dataSize = totalSamples * 2L
        val riffSize = dataSize + 36L

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeLittleEndianInt(riffSize.toInt())
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeLittleEndianInt(16)
            raf.writeLittleEndianShort(1)
            raf.writeLittleEndianShort(1)
            raf.writeLittleEndianInt(SAMPLE_RATE)
            raf.writeLittleEndianInt(SAMPLE_RATE * 2)
            raf.writeLittleEndianShort(2)
            raf.writeLittleEndianShort(16)
            raf.writeBytes("data")
            raf.writeLittleEndianInt(dataSize.toInt())
        }
    }

    private fun RandomAccessFile.writeLittleEndianInt(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
        write((value shr 16) and 0xFF)
        write((value shr 24) and 0xFF)
    }

    private fun RandomAccessFile.writeLittleEndianShort(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
    }
}
