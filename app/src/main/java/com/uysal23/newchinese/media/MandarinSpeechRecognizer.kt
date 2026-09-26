package com.uysal23.newchinese.media

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlin.math.roundToInt

data class ShadowingRecognitionResult(
    val recognizedText: String,
    val similarityPercent: Int,
    val confidence: Float?
)

class MandarinSpeechRecognizer(
    private val context: Context
) {
    private var recognizer: SpeechRecognizer? = null
    private var targetText: String = ""
    private var onSpeechEnded: (() -> Unit)? = null
    private var onResult: ((ShadowingRecognitionResult) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start(
        targetText: String,
        onSpeechEnded: () -> Unit,
        onResult: (ShadowingRecognitionResult) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (!isAvailable()) {
            onError("Bu telefonda konuşma tanıma servisi bulunamadı.")
            return false
        }

        destroy()
        this.targetText = targetText
        this.onSpeechEnded = onSpeechEnded
        this.onResult = onResult
        this.onError = onError

        val speechRecognizer = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        recognizer = speechRecognizer
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                this@MandarinSpeechRecognizer.onSpeechEnded?.invoke()
            }

            override fun onError(error: Int) {
                this@MandarinSpeechRecognizer.onSpeechEnded?.invoke()
                this@MandarinSpeechRecognizer.onError?.invoke(errorMessage(error))
            }

            override fun onResults(results: Bundle?) {
                this@MandarinSpeechRecognizer.onSpeechEnded?.invoke()

                val candidates = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    .orEmpty()
                    .filter { it.isNotBlank() }

                if (candidates.isEmpty()) {
                    this@MandarinSpeechRecognizer.onError?.invoke(
                        "Konuşma anlaşılamadı. Tekrar deneyebilirsin."
                    )
                    return
                }

                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val scored = candidates.mapIndexed { index, text ->
                    val similarity = similarityPercent(this@MandarinSpeechRecognizer.targetText, text)
                    Triple(text, similarity, confidences?.getOrNull(index))
                }
                val best = scored.maxWithOrNull(
                    compareBy<Triple<String, Int, Float?>> { it.second }
                        .thenBy { it.third ?: -1f }
                ) ?: return

                this@MandarinSpeechRecognizer.onResult?.invoke(
                    ShadowingRecognitionResult(
                        recognizedText = best.first,
                        similarityPercent = best.second,
                        confidence = best.third
                    )
                )
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                900L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                600L
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putStringArrayListExtra(
                    RecognizerIntent.EXTRA_BIASING_STRINGS,
                    arrayListOf(targetText)
                )
            }
        }

        return runCatching {
            speechRecognizer.startListening(intent)
            true
        }.getOrElse {
            destroy()
            onError("Konuşma tanıma başlatılamadı.")
            false
        }
    }

    fun stop() {
        runCatching { recognizer?.stopListening() }
    }

    fun cancel() {
        runCatching { recognizer?.cancel() }
    }

    fun destroy() {
        runCatching { recognizer?.cancel() }
        runCatching { recognizer?.destroy() }
        recognizer = null
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Konuşma anlaşılamadı. Tekrar deneyebilirsin."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Konuşma algılanmadı. Tekrar deneyebilirsin."
        SpeechRecognizer.ERROR_AUDIO -> "Mikrofon sesi alınamadı."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni gerekli."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Konuşma tanıma servisine ulaşılamadı."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Konuşma tanıma servisi meşgul. Tekrar dene."
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Mandarin konuşma tanıma bu cihazda kullanılamıyor."
        else -> "Konuşma tanıma tamamlanamadı. Tekrar deneyebilirsin."
    }
}

internal fun similarityPercent(target: String, recognized: String): Int {
    val a = normalizeMandarin(target)
    val b = normalizeMandarin(recognized)
    if (a.isEmpty() && b.isEmpty()) return 100
    if (a.isEmpty() || b.isEmpty()) return 0

    val distance = levenshtein(a, b)
    val longest = maxOf(a.length, b.length)
    return ((1.0 - distance.toDouble() / longest.toDouble()) * 100.0)
        .coerceIn(0.0, 100.0)
        .roundToInt()
}

private fun normalizeMandarin(text: String): String =
    text.lowercase()
        .filter { ch ->
            ch.isLetterOrDigit() || ch.code in 0x3400..0x9FFF
        }

private fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    var previous = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)

    for (i in 1..a.length) {
        current[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            current[j] = minOf(
                current[j - 1] + 1,
                previous[j] + 1,
                previous[j - 1] + cost
            )
        }
        val temp = previous
        previous = current
        current = temp
    }
    return previous[b.length]
}
