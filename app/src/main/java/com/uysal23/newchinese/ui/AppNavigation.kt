package com.uysal23.newchinese.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.uysal23.newchinese.data.ContentRepository
import com.uysal23.newchinese.data.UserPreferences
import com.uysal23.newchinese.data.UserSettings
import com.uysal23.newchinese.data.progress.ProgressRepository
import com.uysal23.newchinese.notifications.ReminderScheduler
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    preferences: UserPreferences,
    repository: ContentRepository,
    progressRepository: ProgressRepository
) {
    val current by preferences.settings.collectAsState(initial = UserSettings())
    val allProgress by progressRepository.observeAll().collectAsState(initial = emptyList())
    var activeSceneId by remember { mutableStateOf("HSK1_SC001") }
    var freeStudyMode by remember { mutableStateOf(false) }
    val sceneProgressFlow = remember(activeSceneId) { progressRepository.observeScene(activeSceneId) }
    val sceneProgress by sceneProgressFlow.collectAsState(initial = null)
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val scene = remember(activeSceneId) { repository.loadSceneById(activeSceneId) }
    val placementQuestions = remember { repository.loadPlacementQuestions() }
    val availableScenes = remember { (1..6).flatMap { repository.availableSceneIds(it) }.toSet() }
    val appContext = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        progressRepository.ensureInitialScene()
    }

    LaunchedEffect(current.userName) {
        if (current.userName.isNotBlank() && nav.currentDestination?.route == "welcome") {
            nav.navigate("dashboard") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    NewChineseTheme(current.darkMode, current.palette) {
        NavHost(
            navController = nav,
            startDestination = "welcome",
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
        ) {
            composable("welcome") {
                WelcomeScreen { name ->
                    scope.launch {
                        preferences.setUserName(name)
                        nav.navigate("dashboard") { popUpTo("welcome") { inclusive = true } }
                    }
                }
            }
            composable("dashboard") {
                MainScaffold(nav = nav, currentRoute = "dashboard") {
                    DashboardScreen(
                        userName = current.userName,
                        progress = allProgress,
                        favoriteCount = current.favoriteWordIds.size,
                        onNavigate = nav::navigate
                    )
                }
            }
            composable("levels") {
                MainScaffold(nav = nav, currentRoute = "dashboard") {
                    LevelsScreen(progress = allProgress) { level -> nav.navigate("scenes/$level") }
                }
            }
            composable(
                "scenes/{level}",
                arguments = listOf(navArgument("level") { type = NavType.StringType })
            ) {
                val levelName = it.arguments?.getString("level").orEmpty()
                val levelNumber = levelName.removePrefix("HSK").toIntOrNull() ?: 1
                val available = remember(levelNumber) { repository.availableSceneIds(levelNumber) }
                val unlocked = if (current.adminMode) {
                    available
                } else {
                    allProgress.filter { p -> p.unlocked }.map { p -> p.sceneId }.toSet()
                }
                val sceneTitles = remember(levelNumber, available) {
                    available.associateWith { sceneId ->
                        runCatching { repository.loadSceneById(sceneId).titleTr }
                            .getOrDefault("")
                    }
                }
                MainScaffold(nav = nav, currentRoute = "dashboard") {
                    SceneListScreen(
                        level = levelName,
                        unlockedSceneIds = unlocked,
                        availableSceneIds = available,
                        sceneTitles = sceneTitles
                    ) { sceneId ->
                        activeSceneId = sceneId
                        freeStudyMode = false
                        nav.navigate("sceneIntro")
                    }
                }
            }
            composable("sceneIntro") {
                SceneIntroScreen(
                    scene = scene,
                    onStartDialogue = { nav.navigate("dialogue") },
                    onStudy = { nav.navigate("study") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("dialogue") {
                KeepScreenOn()
                DialogueScreen(
                    scene = scene,
                    showPinyinDefault = current.showPinyin,
                    showTurkishDefault = current.showTurkish,
                    playbackSpeed = current.playbackSpeed,
                    initialLineIndex = sceneProgress?.lastDialogueLineIndex ?: 0,
                    onPositionChanged = { lineIndex, positionMs ->
                        scope.launch {
                            progressRepository.saveDialoguePosition(scene.sceneId, lineIndex, positionMs)
                        }
                    },
                    onStudy = { nav.navigate("study") },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("study") {
                KeepScreenOn()
                StudyHubScreen(
                    progress = sceneProgress,
                    freeStudyMode = freeStudyMode,
                    vocabularyCount = scene.vocabulary.size,
                    dialogueLineCount = scene.lines.size,
                    onVocabulary = { nav.navigate("vocabulary") },
                    onSentence = { nav.navigate("sentences") },
                    onShadowing = { nav.navigate("shadowing") },
                    onExam = {
                        if (sceneProgress?.wordExamPassed == true) nav.navigate("sentenceExam")
                        else nav.navigate("wordExam")
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("vocabulary") {
                KeepScreenOn()
                VocabularyScreen(
                    sceneId = scene.sceneId,
                    items = scene.vocabulary,
                    favoriteIds = current.favoriteWordIds,
                    playbackSpeed = current.playbackSpeed,
                    onToggleFavorite = { id -> scope.launch { preferences.toggleFavorite(id) } },
                    onProgress = { percent ->
                        if (!freeStudyMode) {
                            scope.launch {
                                progressRepository.recordVocabularyProgress(scene.sceneId, percent)
                            }
                        }
                    },
                    onComplete = {
                        if (!freeStudyMode) scope.launch { progressRepository.markVocabularyComplete(scene.sceneId) }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("sentences") {
                KeepScreenOn()
                SentencePracticeScreen(
                    sceneId = scene.sceneId,
                    lines = scene.lines,
                    playbackSpeed = current.playbackSpeed,
                    onProgress = { percent ->
                        if (!freeStudyMode) {
                            scope.launch {
                                progressRepository.recordSentencePracticeProgress(scene.sceneId, percent)
                            }
                        }
                    },
                    onComplete = {
                        if (!freeStudyMode) {
                            scope.launch {
                                progressRepository.markSentencePracticeComplete(scene.sceneId)
                            }
                        }
                        nav.popBackStack("study", inclusive = false)
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("shadowing") {
                KeepScreenOn()
                ShadowingSetupScreen(
                    sceneId = scene.sceneId,
                    lines = scene.lines,
                    playbackSpeed = current.playbackSpeed,
                    onSimilarityResult = { similarity ->
                        if (!freeStudyMode) {
                            scope.launch {
                                progressRepository.recordShadowingSimilarity(scene.sceneId, similarity)
                            }
                        }
                    },
                    onProgress = { percent ->
                        if (!freeStudyMode) {
                            scope.launch {
                                progressRepository.recordShadowingProgress(scene.sceneId, percent)
                            }
                        }
                    },
                    onComplete = {
                        if (!freeStudyMode) scope.launch { progressRepository.markShadowingComplete(scene.sceneId) }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("wordExam") {
                KeepScreenOn()
                WordExamScreen(
                    words = scene.vocabulary,
                    onFinished = { score ->
                        scope.launch {
                            progressRepository.recordWordExam(scene.sceneId, score)
                            if (score >= 90) nav.navigate("sentenceExam")
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("sentenceExam") {
                KeepScreenOn()
                SentenceExamScreen(
                    exercises = scene.exercises,
                    wordExamPassed = sceneProgress?.wordExamPassed == true,
                    onFinished = { score ->
                        scope.launch {
                            progressRepository.recordSentenceExam(scene.sceneId, score)
                            nav.navigate("progress")
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("freeStudy") {
                val unlockedAvailable = if (current.adminMode) {
                    availableScenes.sorted()
                } else {
                    allProgress
                        .filter { it.unlocked && it.sceneId in availableScenes }
                        .map { it.sceneId }
                        .sorted()
                }
                MainScaffold(nav = nav, currentRoute = "freeStudy") {
                    FreeStudyScreen(
                        sceneIds = unlockedAvailable,
                        onOpenScene = { sceneId ->
                            activeSceneId = sceneId
                            freeStudyMode = true
                            nav.navigate("study")
                        }
                    )
                }
            }
            composable("favorites") {
                val favoriteWords = remember(current.favoriteWordIds) {
                    repository.loadFavoriteWords(current.favoriteWordIds)
                }
                MainScaffold(nav = nav, currentRoute = "favorites") {
                    FavoritesScreen(
                        allWords = favoriteWords,
                        favoriteIds = current.favoriteWordIds,
                        onToggleFavorite = { id -> scope.launch { preferences.toggleFavorite(id) } }
                    )
                }
            }
            composable("progress") {
                MainScaffold(nav = nav, currentRoute = "progress") {
                    ProgressScreen(allProgress)
                }
            }
            composable("settings") {
                MainScaffold(nav = nav, currentRoute = "settings") {
                    SettingsScreen(
                        settings = current,
                        onName = { scope.launch { preferences.setUserName(it) } },
                        onDark = { scope.launch { preferences.setDarkMode(it) } },
                        onPalette = { scope.launch { preferences.setPalette(it) } },
                        onPinyin = { scope.launch { preferences.setShowPinyin(it) } },
                        onTurkish = { scope.launch { preferences.setShowTurkish(it) } },
                        onPlaybackSpeed = { scope.launch { preferences.setPlaybackSpeed(it) } },
                        onAdminMode = { enabled ->
                            scope.launch { preferences.setAdminMode(enabled) }
                        },
                        onSaveReminder = { reminder ->
                            scope.launch {
                                preferences.upsertReminder(reminder)
                                ReminderScheduler.schedule(appContext, reminder)
                            }
                        },
                        onDeleteReminder = { reminderId ->
                            scope.launch {
                                preferences.deleteReminder(reminderId)
                                ReminderScheduler.cancel(appContext, reminderId)
                            }
                        }
                    )
                }
            }
            composable("placement") {
                PlacementScreen(
                    questions = placementQuestions,
                    onBack = { nav.popBackStack() },
                    onGoToLevels = { nav.navigate("levels") }
                )
            }
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
