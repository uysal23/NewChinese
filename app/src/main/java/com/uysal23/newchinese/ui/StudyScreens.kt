package com.uysal23.newchinese.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.uysal23.newchinese.data.DialogueLine
import com.uysal23.newchinese.data.SentenceExercise
import com.uysal23.newchinese.data.VocabularyItem
import com.uysal23.newchinese.data.progress.SceneProgressEntity
import com.uysal23.newchinese.media.AssetAudioPlayer
import com.uysal23.newchinese.media.VoiceRecorder

@Composable
fun StudyHubScreen(
    progress: SceneProgressEntity?,
    onVocabulary: () -> Unit,
    onSentence: () -> Unit,
    onShadowing: () -> Unit,
    onExam: () -> Unit,
    onBack: () -> Unit
) {
    val studyReady = progress?.let {
        it.vocabularyCompleted && it.sentencePracticeCompleted && it.shadowingCompleted
    } == true

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Çalışma", style = MaterialTheme.typography.headlineMedium)
        StudyStatus("Kelime", progress?.vocabularyCompleted == true)
        StudyStatus("Cümle", progress?.sentencePracticeCompleted == true)
        StudyStatus("Shadowing", progress?.shadowingCompleted == true)
        Button(onClick = onVocabulary, modifier = Modifier.fillMaxWidth()) { Text("Kelime Çalışması") }
        Button(onClick = onSentence, modifier = Modifier.fillMaxWidth()) { Text("Cümle Çalışması") }
        Button(onClick = onShadowing, modifier = Modifier.fillMaxWidth()) { Text("Shadowing") }
        Button(
            onClick = onExam,
            enabled = studyReady,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    progress?.sentenceExamPassed == true -> "Sahne Sınavı ✓"
                    progress?.wordExamPassed == true -> "Cümle Sınavına Devam Et"
                    else -> "Sahne Sınavına Geç"
                }
            )
        }
        if (!studyReady) {
            Text("Sınav için üç çalışma bölümünü de tamamla.")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Diyaloğa Dön") }
    }
}

@Composable
private fun StudyStatus(label: String, complete: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (complete) "✓" else "○")
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun VocabularyScreen(
    items: List<VocabularyItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var showMeaning by remember { mutableStateOf(false) }
    val item = items[index]
    val favorite = item.id in favoriteIds

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Geri") }
            TextButton(onClick = { onToggleFavorite(item.id) }) {
                Text(if (favorite) "★" else "☆")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(item.hanzi, style = MaterialTheme.typography.displayMedium)
                Text(item.pinyin, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(18.dp))
                if (showMeaning) Text(item.turkish, style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { showMeaning = !showMeaning }) {
                    Text(if (showMeaning) "Anlamı Gizle" else "Anlamı Göster")
                }
                OutlinedButton(onClick = {}) { Text("🔊 Dinle") }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    if (index > 0) {
                        index--
                        showMeaning = false
                    }
                },
                enabled = index > 0
            ) { Text("← Önceki") }

            Text("${index + 1} / ${items.size}")

            if (index < items.lastIndex) {
                Button(onClick = { index++; showMeaning = false }) { Text("Sonraki →") }
            } else {
                Button(onClick = onComplete) { Text("Tamamla ✓") }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    allWords: List<VocabularyItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    var selectedLevel by remember { mutableStateOf("ALL") }
    val favorites = allWords.filter { word ->
        word.id in favoriteIds && (selectedLevel == "ALL" || word.id.contains("_${selectedLevel}_"))
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Favoriler", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "HSK1", "HSK2", "HSK3").forEach { level ->
                FilterChip(
                    selected = selectedLevel == level,
                    onClick = { selectedLevel = level },
                    label = { Text(if (level == "ALL") "Tümü" else level) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("HSK4", "HSK5", "HSK6").forEach { level ->
                FilterChip(
                    selected = selectedLevel == level,
                    onClick = { selectedLevel = level },
                    label = { Text(level) }
                )
            }
        }
        if (favorites.isEmpty()) {
            Text("Henüz favori kelime seçmedin.")
        } else {
            favorites.forEach { word ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(word.hanzi, style = MaterialTheme.typography.titleLarge)
                            Text(word.pinyin)
                            Text(word.turkish)
                        }
                        TextButton(onClick = { onToggleFavorite(word.id) }) { Text("★") }
                    }
                }
            }
        }
    }
}

@Composable
fun FreeStudyScreen(
    sceneIds: List<String>,
    onOpenScene: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Serbest Çalışma", style = MaterialTheme.typography.headlineMedium)
        Text("Daha önce açılmış sahnelerden birini seç. Buradaki çalışma sınav veya kilit durumunu sıfırlamaz.")
        if (sceneIds.isEmpty()) {
            Text("Henüz serbest çalışmaya açık sahne yok.")
        } else {
            sceneIds.forEach { sceneId ->
                val level = sceneId.substringBefore("_SC")
                val number = sceneId.substringAfter("_SC").toIntOrNull()?.toString()?.padStart(2, '0') ?: "01"
                val title = when (sceneId) {
                    "HSK1_SC001" -> "İlk Tanışma"
                    "HSK1_SC002" -> "İsimler"
                    else -> "Sahne"
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("$level · SC$number", style = MaterialTheme.typography.titleLarge)
                        Text(title)
                        Button(
                            onClick = { onOpenScene(sceneId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Çalışmayı Aç")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SentencePracticeScreen(
    exercises: List<SentenceExercise>,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    val exercise = exercises[index]
    var selectedTokens by remember(index) { mutableStateOf(emptyList<String>()) }
    var selectedOption by remember(index) { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Cümle Çalışması", style = MaterialTheme.typography.headlineMedium)
        Text("${index + 1} / ${exercises.size}")
        OutlinedButton(onClick = {}) { Text("🔊 Doğru Cümleyi Dinle") }

        when (exercise.type) {
            "reorder" -> {
                Text("Kelimeleri doğru sıraya koy.")
                Card(Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTokens.forEachIndexed { tokenIndex, token ->
                            AssistChip(
                                onClick = {
                                    selectedTokens = selectedTokens.toMutableList().also { it.removeAt(tokenIndex) }
                                },
                                label = { Text(token) }
                            )
                        }
                    }
                }
                FlowLikeRow(
                    tokens = exercise.tokens.filter { token -> token !in selectedTokens },
                    onToken = { selectedTokens = selectedTokens + it }
                )
                if (selectedTokens.size == exercise.tokens.size) {
                    val answer = selectedTokens.joinToString("")
                    Text(if (answer == exercise.tokens.joinToString("")) "✓ Doğru" else "Tekrar dene")
                }
            }
            "fill_blank" -> {
                Text(exercise.sentenceZh.orEmpty().replace("___", " - - - - - - - - "))
                exercise.options.forEach { option ->
                    OutlinedButton(
                        onClick = { selectedOption = option },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option)
                    }
                }
                if (selectedOption != null) {
                    Text(if (selectedOption == exercise.correctAnswer) "✓ Doğru" else "Tekrar dene")
                }
            }
            else -> Text("Bu alıştırma tipi sonraki içerik paketinde etkinleştirilecek.")
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { if (index > 0) index-- }, enabled = index > 0) { Text("←") }
            if (index < exercises.lastIndex) {
                Button(onClick = { index++ }) { Text("→") }
            } else {
                Button(onClick = onComplete) { Text("Tamamla ✓") }
            }
        }
    }
}

@Composable
private fun FlowLikeRow(tokens: List<String>, onToken: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tokens.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { token ->
                    AssistChip(onClick = { onToken(token) }, label = { Text(token) })
                }
            }
        }
    }
}

@Composable
fun ShadowingSetupScreen(
    sceneId: String,
    lines: List<DialogueLine>,
    playbackSpeed: Float,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val availableOptions = listOf(6, 10, 15).filter { it <= lines.size }
    var selected by remember { mutableIntStateOf(availableOptions.firstOrNull() ?: lines.size) }
    var started by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordedPath by remember { mutableStateOf<String?>(null) }
    var referenceMissing by remember { mutableStateOf(false) }
    var listenOnly by remember { mutableStateOf(false) }
    var referencePlayed by remember { mutableStateOf(false) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val recorder = remember { VoiceRecorder(context.applicationContext) }
    val audioPlayer = remember { AssetAudioPlayer(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose {
            recorder.release()
            audioPlayer.release()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (!granted) listenOnly = true
    }

    val sessionLines = remember(started, selected, lines) {
        if (started) selectMixedShadowingLines(lines, selected) else emptyList()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Shadowing", style = MaterialTheme.typography.headlineMedium)

        if (!started) {
            Text("Tekrar etmek istediğin cümle sayısını seç.")
            availableOptions.forEach { count ->
                OutlinedButton(
                    onClick = { selected = count },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("$count cümle${if (selected == count) " ✓" else ""}")
                }
            }
            OutlinedButton(
                onClick = { selected = lines.size },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tüm diyalog${if (selected == lines.size) " ✓" else ""}")
            }
            Text("Varsayılan ve minimum hedef: 6 cümle")

            if (!hasMicPermission) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mikrofon İzni Ver")
                }
                TextButton(onClick = { listenOnly = true }) {
                    Text("Yalnız dinleme modunda devam et")
                }
            }

            Button(
                onClick = { started = true },
                enabled = hasMicPermission || listenOnly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Shadowing'i Başlat")
            }
        } else if (sessionLines.isNotEmpty()) {
            val line = sessionLines[currentIndex]
            val assetBase = sceneIdToAssetPath(sceneId)

            Text("Cümle ${currentIndex + 1} / ${sessionLines.size}")
            Text(line.chinese, style = MaterialTheme.typography.headlineSmall)
            Text(line.pinyin)
            Text(line.turkish)

            OutlinedButton(
                onClick = {
                    val ok = audioPlayer.play("$assetBase/${line.audioFile}", playbackSpeed)
                    referenceMissing = !ok
                    referencePlayed = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔊 Referansı Dinle")
            }

            if (referenceMissing) {
                Text(
                    "Doğal Mandarin referans ses asset’i henüz eklenmedi.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (!listenOnly) {
                Button(
                    onClick = {
                        if (!isRecording) {
                            val result = recorder.start()
                            if (result.isSuccess) {
                                isRecording = true
                                recordedPath = null
                            }
                        } else {
                            recordedPath = recorder.stop().getOrNull()?.absolutePath
                            isRecording = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isRecording) "■ Kaydı Durdur" else "🎙 Kaydı Başlat")
                }

                OutlinedButton(
                    onClick = { recordedPath?.let { audioPlayer.playFile(it) } },
                    enabled = recordedPath != null && !isRecording,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("▶ Kendi Kaydımı Dinle")
                }
            } else {
                Text("Yalnız dinleme modu")
            }

            val canAdvance = if (listenOnly) referencePlayed else recordedPath != null
            Button(
                onClick = {
                    recorder.cancel()
                    recordedPath = null
                    isRecording = false
                    referencePlayed = false
                    referenceMissing = false
                    if (currentIndex == sessionLines.lastIndex) {
                        onComplete()
                    } else {
                        currentIndex++
                    }
                },
                enabled = canAdvance && !isRecording,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (currentIndex == sessionLines.lastIndex) "Oturumu Tamamla ✓" else "Sonraki Cümle →")
            }
        }
    }
}

private fun selectMixedShadowingLines(
    lines: List<DialogueLine>,
    count: Int
): List<DialogueLine> {
    if (count >= lines.size) return lines

    val speakers = lines.map { it.speakerId }.distinct()
    if (speakers.size < 2) return lines.shuffled().take(count)

    val buckets = speakers.associateWith { speaker ->
        lines.filter { it.speakerId == speaker }.shuffled().toMutableList()
    }
    val result = mutableListOf<DialogueLine>()
    var speakerIndex = 0

    while (result.size < count) {
        val speaker = speakers[speakerIndex % speakers.size]
        val bucket = buckets[speaker]
        if (bucket != null && bucket.isNotEmpty()) {
            result += bucket.removeAt(0)
        }
        speakerIndex++
        if (buckets.values.all { it.isEmpty() }) break
    }
    return result
}

private fun sceneIdToAssetPath(sceneId: String): String {
    val match = Regex("""HSK(\d)_SC(\d{3})""").matchEntire(sceneId)
        ?: return "hsk1/sc001"
    val level = match.groupValues[1]
    val scene = match.groupValues[2]
    return "hsk$level/sc$scene"
}
