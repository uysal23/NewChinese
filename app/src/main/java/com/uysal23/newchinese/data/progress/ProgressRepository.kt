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

    suspend fun markVocabularyComplete(sceneId: String) = mutate(sceneId) {
        copy(vocabularyCompleted = true, lastStudiedAt = System.currentTimeMillis())
    }

    suspend fun markSentencePracticeComplete(sceneId: String) = mutate(sceneId) {
        copy(sentencePracticeCompleted = true, lastStudiedAt = System.currentTimeMillis())
    }

    suspend fun markShadowingComplete(sceneId: String) = mutate(sceneId) {
        copy(shadowingCompleted = true, lastStudiedAt = System.currentTimeMillis())
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
