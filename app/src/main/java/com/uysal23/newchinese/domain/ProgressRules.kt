package com.uysal23.newchinese.domain

object ProgressRules {
    const val WORD_PASS_SCORE = 90
    const val SENTENCE_PASS_SCORE = 85

    fun wordPassed(score: Int): Boolean = score >= WORD_PASS_SCORE
    fun sentencePassed(wordExamPassed: Boolean, score: Int): Boolean =
        wordExamPassed && score >= SENTENCE_PASS_SCORE

    fun nextSceneId(sceneId: String): String? {
        val match = Regex("""HSK(\d)_SC(\d{3})""").matchEntire(sceneId) ?: return null
        val level = match.groupValues[1].toInt()
        val scene = match.groupValues[2].toInt()
        return when {
            scene < 50 -> "HSK${level}_SC${(scene + 1).toString().padStart(3, '0')}"
            level < 6 -> "HSK${level + 1}_SC001"
            else -> null
        }
    }
}
