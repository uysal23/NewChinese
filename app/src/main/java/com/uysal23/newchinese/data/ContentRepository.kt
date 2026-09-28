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
    val turkish: String,
    val voiceId: String,
    val audioFile: String,
    val sourceAssetBase: String
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
    val options: List<String>,
    val voiceId: String,
    val audioFile: String
)

data class PlacementQuestion(
    val id: String,
    val level: String,
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int
)

data class VisualAssets(
    val background: String?,
    val characterA: String?,
    val characterB: String?,
    val foreground: String?,
    val preview: String?
)

data class SceneContent(
    val sceneId: String,
    val titleZh: String,
    val titleTr: String,
    val summaryTr: String,
    val locationTr: String,
    val characterIds: List<String>,
    val estimatedMinutes: Int,
    val lines: List<DialogueLine>,
    val vocabulary: List<VocabularyItem>,
    val exercises: List<SentenceExercise>,
    val visualAssets: VisualAssets
)

class ContentRepository(private val context: Context) {
    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    fun loadPlacementQuestions(): List<PlacementQuestion> {
        val root = JSONObject(readAsset("placement/placement_test.json"))
        val array = root.getJSONArray("questions")
        return buildList {
            for (i in 0 until array.length()) {
                val x = array.getJSONObject(i)
                val options = buildList {
                    val a = x.getJSONArray("options")
                    for (j in 0 until a.length()) add(a.getString(j))
                }
                add(
                    PlacementQuestion(
                        id = x.getString("id"),
                        level = x.getString("level"),
                        prompt = x.getString("prompt"),
                        options = options,
                        answerIndex = x.getInt("answerIndex")
                    )
                )
            }
        }
    }

    fun loadFavoriteWords(wordIds: Set<String>): List<VocabularyItem> {
        val sceneIds = wordIds.mapNotNull { wordId ->
            Regex("""WORD_(HSK\d_SC\d{3})_\d+""").matchEntire(wordId)?.groupValues?.get(1)
        }.toSet()

        return sceneIds
            .flatMap { sceneId -> runCatching { loadSceneById(sceneId).vocabulary }.getOrDefault(emptyList()) }
            .filter { it.id in wordIds }
            .distinctBy { it.id }
    }


    private val globalVocabulary: List<VocabularyItem> by lazy {
        (1..6)
            .flatMap { level -> availableSceneIds(level).sorted() }
            .flatMap { sceneId ->
                runCatching { loadSceneById(sceneId).vocabulary }.getOrDefault(emptyList())
            }
            .distinctBy { item -> item.hanzi to item.turkish }
    }

    fun loadDialogueVocabulary(scene: SceneContent): List<VocabularyItem> {
        val dialogueText = scene.lines.joinToString(separator = "") { it.chinese }
        val nativeWords = scene.vocabulary
        val nativeKeys = nativeWords.map { it.hanzi }.toSet()

        val extraWords = globalVocabulary
            .asSequence()
            .filter { it.hanzi.isNotBlank() }
            .filter { dialogueText.contains(it.hanzi) }
            .filter { it.hanzi !in nativeKeys }
            .distinctBy { it.hanzi }
            .sortedBy { dialogueText.indexOf(it.hanzi).let { index -> if (index < 0) Int.MAX_VALUE else index } }
            .toList()

        return (nativeWords + extraWords).distinctBy { it.hanzi }
    }

    fun availableSceneIds(level: Int): Set<String> {
        val folders = context.assets.list("hsk$level").orEmpty()
        return folders
            .filter { it.matches(Regex("""sc\d{3}""")) }
            .map { folder -> "HSK${level}_SC${folder.removePrefix("sc")}" }
            .toSet()
    }

    fun loadSceneById(sceneId: String): SceneContent {
        val match = Regex("""HSK(\d)_SC(\d{3})""").matchEntire(sceneId)
            ?: error("Invalid sceneId: $sceneId")
        val level = match.groupValues[1]
        val scene = match.groupValues[2].toInt().toString().padStart(3, '0')
        return loadScene("hsk$level/sc$scene")
    }

    fun loadScene(scenePath: String = "hsk1/sc001"): SceneContent {
        val scene = JSONObject(readAsset("$scenePath/scene.json"))
        val dialogue = JSONObject(readAsset("$scenePath/dialogue.json"))
        val vocabularyJson = JSONObject(readAsset("$scenePath/vocabulary.json"))
        val exercisesJson = JSONObject(readAsset("$scenePath/sentence_exercises.json"))
        val visualJson = JSONObject(readAsset("$scenePath/visual_manifest.json"))

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
                        turkish = x.getString("translationTr"),
                        voiceId = x.optString("voiceId", ""),
                        audioFile = x.optString("audioFile", ""),
                        sourceAssetBase = scenePath
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
                        options = options,
                        voiceId = x.optString("voiceId", ""),
                        audioFile = x.optString("audioFile", "")
                    )
                )
            }
        }

        val characterIds = buildList {
            val chars = scene.getJSONArray("characterIds")
            for (i in 0 until chars.length()) add(chars.getString(i))
        }

        val assets = visualJson.optJSONObject("assets")
        val visualAssets = VisualAssets(
            background = assets?.optString("background")?.ifBlank { null },
            characterA = assets?.optString("characterA")?.ifBlank { null },
            characterB = assets?.optString("characterB")?.ifBlank { null },
            foreground = assets?.optString("foreground")?.ifBlank { null },
            preview = assets?.optString("preview")?.ifBlank { null }
        )

        return SceneContent(
            sceneId = scene.getString("sceneId"),
            titleZh = scene.getString("titleZh"),
            titleTr = scene.getString("titleTr"),
            summaryTr = scene.optString("summaryTr", ""),
            locationTr = scene.optString("locationTr", scene.optString("locationId", "")),
            characterIds = characterIds,
            estimatedMinutes = scene.optInt("estimatedMinutes", 0),
            lines = lines,
            vocabulary = vocabulary,
            exercises = exercises,
            visualAssets = visualAssets
        )
    }
}
