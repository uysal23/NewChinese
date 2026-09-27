package com.uysal23.newchinese.data.progress

import com.uysal23.newchinese.domain.ProgressRules
import kotlinx.coroutines.flow.Flow

class ProgressRepository(private val dao: SceneProgressDao) {
    fun observeScene(sceneId: String): Flow<SceneProgressEntity?> = dao.observe(sceneId)
    fun observeAll(): Flow<List<SceneProgressEntity>> = dao.observeAll()

    suspend fun ensureInitialScene() {
        if (dao.get("HSK1_SC001") == null) {
            dao.upsert(SceneProgressEntity(sceneId = "HSK1_SC001", unlocked = true))
        }
    }

    suspend fun saveDialoguePosition(
        sceneId: String,
        lineIndex: Int,
        playbackPositionMs: Long = 0L
    ) = mutate(sceneId) {
        copy(
            lastDialogueLineIndex = lineIndex.coerceAtLeast(0),
            lastPlaybackPositionMs = playbackPositionMs.coerceAtLeast(0L),
            lastStudiedAt = System.currentTimeMillis()
        )
    }

    suspend fun recordVocabularyProgress(sceneId: String, percent: Int) = mutate(sceneId) {
        val p = percent.coerceIn(0, 100)
        copy(
            vocabularyProgressPercent = maxOf(vocabularyProgressPercent, p),
            vocabularyCompleted = vocabularyCompleted || p >= 100,
            lastStudiedAt = System.currentTimeMillis()
        )
    }

    suspend fun recordSentencePracticeProgress(sceneId: String, percent: Int) = mutate(sceneId) {
        val p = percent.coerceIn(0, 100)
        copy(
            sentencePracticeProgressPercent = maxOf(sentencePracticeProgressPercent, p),
            sentencePracticeCompleted = sentencePracticeCompleted || p >= 100,
            lastStudiedAt = System.currentTimeMillis()
        )
    }

    suspend fun recordShadowingProgress(sceneId: String, percent: Int) = mutate(sceneId) {
        val p = percent.coerceIn(0, 100)
        copy(
            shadowingProgressPercent = maxOf(shadowingProgressPercent, p),
            shadowingCompleted = shadowingCompleted || p >= 100,
            lastStudiedAt = System.currentTimeMillis()
        )
    }

    suspend fun markVocabularyComplete(sceneId: String) = recordVocabularyProgress(sceneId, 100)

    suspend fun markSentencePracticeComplete(sceneId: String) = recordSentencePracticeProgress(sceneId, 100)

    suspend fun markShadowingComplete(sceneId: String) = recordShadowingProgress(sceneId, 100)

    suspend fun recordShadowingSimilarity(sceneId: String, similarityPercent: Int) = mutate(sceneId) {
        copy(
            shadowingCompleted = true,
            shadowingBestSimilarity = maxOf(shadowingBestSimilarity, similarityPercent.coerceIn(0, 100)),
            lastStudiedAt = System.currentTimeMillis()
        )
    }

    suspend fun recordWordExam(sceneId: String, score: Int) = mutate(sceneId) {
        copy(
            wordExamPassed = wordExamPassed || ProgressRules.wordPassed(score),
            wordExamBestScore = maxOf(wordExamBestScore, score),
            lastStudiedAt = System.currentTimeMillis()
        )
    }

    suspend fun recordSentenceExam(sceneId: String, score: Int) {
        val before = dao.get(sceneId) ?: SceneProgressEntity(sceneId = sceneId, unlocked = sceneId == "HSK1_SC001")
        val passed = ProgressRules.sentencePassed(before.wordExamPassed, score)
        val completed = before.sceneCompleted || passed
        dao.upsert(
            before.copy(
                sentenceExamPassed = before.sentenceExamPassed || passed,
                sentenceExamBestScore = maxOf(before.sentenceExamBestScore, score),
                sceneCompleted = completed,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
        if (completed) unlockNext(sceneId)
    }

    private suspend fun unlockNext(sceneId: String) {
        val next = ProgressRules.nextSceneId(sceneId) ?: return
        val current = dao.get(next) ?: SceneProgressEntity(sceneId = next)
        if (!current.unlocked) dao.upsert(current.copy(unlocked = true))
    }

    private suspend fun mutate(sceneId: String, block: SceneProgressEntity.() -> SceneProgressEntity) {
        val current = dao.get(sceneId) ?: SceneProgressEntity(sceneId = sceneId, unlocked = sceneId == "HSK1_SC001")
        dao.upsert(current.block())
    }
}
