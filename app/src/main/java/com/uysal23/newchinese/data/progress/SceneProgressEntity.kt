package com.uysal23.newchinese.data.progress

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scene_progress")
data class SceneProgressEntity(
    @PrimaryKey val sceneId: String,
    val vocabularyCompleted: Boolean = false,
    val sentencePracticeCompleted: Boolean = false,
    val shadowingCompleted: Boolean = false,
    val wordExamPassed: Boolean = false,
    val wordExamBestScore: Int = 0,
    val sentenceExamPassed: Boolean = false,
    val sentenceExamBestScore: Int = 0,
    val sceneCompleted: Boolean = false,
    val unlocked: Boolean = false,
    val lastStudiedAt: Long = 0L,
    val lastDialogueLineIndex: Int = 0,
    val lastPlaybackPositionMs: Long = 0L
)
