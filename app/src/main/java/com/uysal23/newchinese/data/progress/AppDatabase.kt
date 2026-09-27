package com.uysal23.newchinese.data.progress

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SceneProgressEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sceneProgressDao(): SceneProgressDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scene_progress ADD COLUMN lastDialogueLineIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scene_progress ADD COLUMN lastPlaybackPositionMs INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scene_progress ADD COLUMN shadowingBestSimilarity INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scene_progress ADD COLUMN vocabularyProgressPercent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scene_progress ADD COLUMN sentencePracticeProgressPercent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scene_progress ADD COLUMN shadowingProgressPercent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE scene_progress SET vocabularyProgressPercent = 100 WHERE vocabularyCompleted = 1")
                db.execSQL("UPDATE scene_progress SET sentencePracticeProgressPercent = 100 WHERE sentencePracticeCompleted = 1")
                db.execSQL("UPDATE scene_progress SET shadowingProgressPercent = 100 WHERE shadowingCompleted = 1")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "newchinese.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
    }
}
