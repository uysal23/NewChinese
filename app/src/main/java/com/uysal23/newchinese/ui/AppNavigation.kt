package com.uysal23.newchinese.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.uysal23.newchinese.data.ContentRepository
import com.uysal23.newchinese.data.UserPreferences
import com.uysal23.newchinese.data.UserSettings
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(preferences: UserPreferences, repository: ContentRepository) {
    val current by preferences.settings.collectAsState(initial = UserSettings())
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val scene = remember { repository.loadScene() }

    LaunchedEffect(current.userName) {
        if (current.userName.isNotBlank() && nav.currentDestination?.route == "welcome") {
            nav.navigate("dashboard") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    NewChineseTheme(current.darkMode, current.palette) {
        NavHost(navController = nav, startDestination = "welcome") {
            composable("welcome") {
                WelcomeScreen { name ->
                    scope.launch {
                        preferences.setUserName(name)
                        nav.navigate("dashboard") { popUpTo("welcome") { inclusive = true } }
                    }
                }
            }
            composable("dashboard") {
                DashboardScreen(current.userName, onNavigate = nav::navigate)
            }
            composable("levels") {
                LevelsScreen { level -> nav.navigate("scenes/$level") }
            }
            composable(
                "scenes/{level}",
                arguments = listOf(navArgument("level") { type = NavType.StringType })
            ) {
                SceneListScreen(it.arguments?.getString("level").orEmpty()) {
                    nav.navigate("dialogue")
                }
            }
            composable("dialogue") {
                KeepScreenOn()
                DialogueScreen(
                    scene = scene,
                    showPinyinDefault = current.showPinyin,
                    showTurkishDefault = current.showTurkish,
                    onStudy = { nav.navigate("study") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("study") {
                KeepScreenOn()
                StudyHubScreen(
                    onVocabulary = { nav.navigate("vocabulary") },
                    onSentence = { nav.navigate("sentences") },
                    onShadowing = { nav.navigate("shadowing") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("vocabulary") {
                KeepScreenOn()
                VocabularyScreen(scene.vocabulary, onBack = { nav.popBackStack() })
            }
            composable("sentences") {
                KeepScreenOn()
                SentencePracticeScreen(scene.exercises, onBack = { nav.popBackStack() })
            }
            composable("shadowing") {
                KeepScreenOn()
                ShadowingSetupScreen(scene.lines.size, onBack = { nav.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(
                    settings = current,
                    onName = { scope.launch { preferences.setUserName(it) } },
                    onDark = { scope.launch { preferences.setDarkMode(it) } },
                    onPalette = { scope.launch { preferences.setPalette(it) } },
                    onPinyin = { scope.launch { preferences.setShowPinyin(it) } },
                    onTurkish = { scope.launch { preferences.setShowTurkish(it) } }
                )
            }
            composable("placement") { PlaceholderScreen("Seviye Tespit Sınavı") }
            composable("freeStudy") { PlaceholderScreen("Serbest Çalışma") }
            composable("favorites") { PlaceholderScreen("Favoriler") }
            composable("progress") { PlaceholderScreen("İlerlemem") }
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}
