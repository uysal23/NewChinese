package com.uysal23.newchinese.data.progress

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneProgressDao {
    @Query("SELECT * FROM scene_progress WHERE sceneId = :sceneId LIMIT 1")
    fun observe(sceneId: String): Flow<SceneProgressEntity?>

    @Query("SELECT * FROM scene_progress ORDER BY sceneId")
    fun observeAll(): Flow<List<SceneProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: SceneProgressEntity)

    @Query("SELECT * FROM scene_progress WHERE sceneId = :sceneId LIMIT 1")
    suspend fun get(sceneId: String): SceneProgressEntity?
}
