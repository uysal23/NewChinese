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

data class SceneContent(
    val sceneId: String,
    val titleZh: String,
    val titleTr: String,
    val lines: List<DialogueLine>
)

class ContentRepository(private val context: Context) {
    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    fun loadScene(scenePath: String = "hsk1/sc001"): SceneContent {
        val scene = JSONObject(readAsset("$scenePath/scene.json"))
        val dialogue = JSONObject(readAsset("$scenePath/dialogue.json"))
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
        return SceneContent(
            sceneId = scene.getString("sceneId"),
            titleZh = scene.getString("titleZh"),
            titleTr = scene.getString("titleTr"),
            lines = lines
        )
    }
}
