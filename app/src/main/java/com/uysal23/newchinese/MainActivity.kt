package com.uysal23.newchinese

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uysal23.newchinese.data.ContentRepository
import com.uysal23.newchinese.data.UserPreferences
import com.uysal23.newchinese.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = UserPreferences(applicationContext)
        val repository = ContentRepository(applicationContext)
        setContent {
            AppNavigation(preferences = preferences, repository = repository)
        }
    }
}
