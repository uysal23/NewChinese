package com.uysal23.newchinese.data

import android.content.Context
import org.json.JSONObject

data class DialogueLine(
    val id: String,
    val speakerId: String,
    val chinese: String,
    val pinyin: String,
    val turkish: String,
    val voiceId: String,
    val audioFile: String
)

data class VocabularyItem(
    val id: String,
    val hanzi: String,
    val pinyin: String,
    val turkish: String
)

data class SentenceExercise(
    val id: String,
    val type: String,
    val correctZh: String,
    val pinyin: String,
    val turkish: String,
    val tokens: List<String>,
    val sentenceZh: String?,
    val correctAnswer: String?,
    val options: List<String>
)

data class SceneContent(
    val sceneId: String,
    val titleZh: String,
    val titleTr: String,
    val lines: List<DialogueLine>,
    val vocabulary: List<VocabularyItem>,
    val exercises: List<SentenceExercise>
)

class ContentRepository(private val context: Context) {
    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    fun loadScene(scenePath: String = "hsk1/sc001"): SceneContent {
        val scene = JSONObject(readAsset("$scenePath/scene.json"))
        val dialogue = JSONObject(readAsset("$scenePath/dialogue.json"))
        val vocabularyJson = JSONObject(readAsset("$scenePath/vocabulary.json"))
        val exercisesJson = JSONObject(readAsset("$scenePath/sentence_exercises.json"))

        val linesJson = dialogue.getJSONArray("lines")
        val lines = buildList {
            for (i in 0 until linesJson.length()) {
                val x = linesJson.getJSONObject(i)
                add(
                    DialogueLine(
                        id = x.getString("lineId"),
                        speakerId = x.getString("speakerId"),
                        chinese = x.getString("textZh"),
                        pinyin = x.getString("pinyin"),
                        turkish = x.getString("translationTr"),
                        voiceId = x.getString("voiceId"),
                        audioFile = x.getString("audioFile")
                    )
                )
            }
        }

        val wordsJson = vocabularyJson.getJSONArray("words")
        val vocabulary = buildList {
            for (i in 0 until wordsJson.length()) {
                val x = wordsJson.getJSONObject(i)
                add(
                    VocabularyItem(
                        id = x.getString("wordId"),
                        hanzi = x.getString("hanzi"),
                        pinyin = x.getString("pinyin"),
                        turkish = x.getString("translationTr")
                    )
                )
            }
        }

        val exerciseArray = exercisesJson.getJSONArray("exercises")
        val exercises = buildList {
            for (i in 0 until exerciseArray.length()) {
                val x = exerciseArray.getJSONObject(i)
                val tokens = buildList {
                    val a = x.optJSONArray("tokens")
                    if (a != null) for (j in 0 until a.length()) add(a.getString(j))
                }
                val options = buildList {
                    val a = x.optJSONArray("options")
                    if (a != null) for (j in 0 until a.length()) add(a.getString(j))
                }
                add(
                    SentenceExercise(
                        id = x.getString("exerciseId"),
                        type = x.getString("type"),
                        correctZh = x.optString("correctZh", x.optString("fullCorrectSentenceZh", "")),
                        pinyin = x.optString("pinyin", ""),
                        turkish = x.optString("translationTr", ""),
                        tokens = tokens,
                        sentenceZh = x.optString("sentenceZh").ifBlank { null },
                        correctAnswer = x.optString("correctAnswer").ifBlank { null },
                        options = options
                    )
                )
            }
        }

        return SceneContent(
            sceneId = scene.getString("sceneId"),
            titleZh = scene.getString("titleZh"),
            titleTr = scene.getString("titleTr"),
            lines = lines,
            vocabulary = vocabulary,
            exercises = exercises
        )
    }
}
