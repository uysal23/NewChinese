package com.uysal23.newchinese

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uysal23.newchinese.data.ContentRepository
import com.uysal23.newchinese.data.UserPreferences
import com.uysal23.newchinese.data.progress.AppDatabase
import com.uysal23.newchinese.data.progress.ProgressRepository
import com.uysal23.newchinese.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val preferences = UserPreferences(applicationContext)
        val contentRepository = ContentRepository(applicationContext)
        val progressRepository = ProgressRepository(
            AppDatabase.get(applicationContext).sceneProgressDao()
        )
        setContent {
            AppNavigation(
                preferences = preferences,
                repository = contentRepository,
                progressRepository = progressRepository
            )
        }
    }
}
