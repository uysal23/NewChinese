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
import com.uysal23.newchinese.media.OfflineMandarinShadowingEngine

@Composable
fun StudyHubScreen(
    progress: SceneProgressEntity?,
    freeStudyMode: Boolean,
    vocabularyCount: Int,
    dialogueLineCount: Int,
    onVocabulary: () -> Unit,
    onSentence: () -> Unit,
    onShadowing: () -> Unit,
    onExam: () -> Unit,
    onBack: () -> Unit
) {
    fun pct(value: Int, complete: Boolean): Int = if (complete) 100 else value.coerceIn(0, 100)
    val vocabularyPercent = pct(progress?.vocabularyProgressPercent ?: 0, progress?.vocabularyCompleted == true)
    val sentencePercent = pct(progress?.sentencePracticeProgressPercent ?: 0, progress?.sentencePracticeCompleted == true)
    val shadowingPercent = pct(progress?.shadowingProgressPercent ?: 0, progress?.shadowingCompleted == true)
    val studyReady = vocabularyPercent >= 50 && sentencePercent >= 50 && shadowingPercent >= 50

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Çalışma", style = MaterialTheme.typography.headlineMedium)
        if (freeStudyMode) {
            Text("Serbest Çalışma modu · ilerleme ve sahne kilitleri değişmez.")
        } else {
            StudyStatus("Kelime", vocabularyPercent, "$vocabularyCount kelime")
            StudyStatus("Cümle", sentencePercent, "$dialogueLineCount cümle × 3 çalışma")
            StudyStatus(
                "Telaffuz Çalışması",
                shadowingPercent,
                progress?.shadowingBestSimilarity
                    ?.takeIf { it > 0 }
                    ?.let { "En iyi benzerlik: %$it" }
                    ?: "Henüz sonuç yok"
            )
            Text(
                "Kelime, cümle ve telaffuz çalışmalarının her birinde en az %50 ilerlediğinde istersen Sahne Sınavı'na geçebilirsin.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(onClick = onVocabulary, modifier = Modifier.fillMaxWidth()) {
            Text("Kelime Çalışması · $vocabularyCount")
        }
        Button(onClick = onSentence, modifier = Modifier.fillMaxWidth()) {
            Text("Cümle Çalışması · ${dialogueLineCount * 3} görev")
        }
        Button(onClick = onShadowing, modifier = Modifier.fillMaxWidth()) {
            Text("Telaffuz Çalışması")
        }
        if (!freeStudyMode) {
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
                Text("Sahne Sınavı için üç çalışma bölümünün her birinde en az %50 ilerleme gerekli.")
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Diyaloğa Dön") }
    }
}

@Composable
private fun StudyStatus(label: String, percent: Int, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text("%$percent", style = MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun VocabularyScreen(
    sceneId: String,
    items: List<VocabularyItem>,
    favoriteIds: Set<String>,
    playbackSpeed: Float,
    onToggleFavorite: (String) -> Unit,
    onProgress: (Int) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val audioPlayer = remember { AssetAudioPlayer(context.applicationContext) }
    DisposableEffect(Unit) { onDispose { audioPlayer.release() } }
    var index by remember { mutableIntStateOf(0) }
    var showMeaning by remember { mutableStateOf(false) }
    var audioMissing by remember(index) { mutableStateOf(false) }
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
                OutlinedButton(
                    onClick = {
                        val ok = item.audioFile.isNotBlank() &&
                            audioPlayer.play("${sceneIdToAssetPath(sceneId)}/${item.audioFile}", playbackSpeed)
                        audioMissing = !ok
                    }
                ) { Text("🔊 Dinle") }
                if (audioMissing) {
                    Text(
                        "Doğal Mandarin kelime sesi henüz eklenmedi.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                Button(onClick = {
                    onProgress((((index + 1).toFloat() / items.size) * 100).toInt())
                    index++
                    showMeaning = false
                }) { Text("Sonraki →") }
            } else {
                Button(onClick = {
                    onProgress(100)
                    onComplete()
                }) { Text("Tamamla ✓") }
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

private enum class DialoguePracticeMode { REORDER, FILL_BLANK, LISTEN_SELECT }

private data class DialoguePracticeTask(
    val line: DialogueLine,
    val mode: DialoguePracticeMode
)

@Composable
fun SentencePracticeScreen(
    sceneId: String,
    lines: List<DialogueLine>,
    playbackSpeed: Float,
    onProgress: (Int) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val audioPlayer = remember { AssetAudioPlayer(context.applicationContext) }
    DisposableEffect(Unit) { onDispose { audioPlayer.release() } }

    val tasks = remember(lines) {
        buildList {
            DialoguePracticeMode.entries.forEach { mode ->
                lines.forEach { line -> add(DialoguePracticeTask(line, mode)) }
            }
        }
    }
    var index by remember { mutableIntStateOf(0) }
    var selectedOption by remember(index) { mutableStateOf<String?>(null) }
    var selectedChunkIndexes by remember(index) { mutableStateOf<List<Int>>(emptyList()) }
    var audioMissing by remember(index) { mutableStateOf(false) }
    val task = tasks[index]
    val line = task.line

    val chunks = remember(line.id) { splitMandarinChunks(line.chinese) }
    val correctReorder = chunks.joinToString("")
    val shuffledChunks = remember(line.id) {
        chunks.mapIndexed { chunkIndex, chunk -> chunkIndex to chunk }.shuffled()
    }
    val reorderAnswer = selectedChunkIndexes.joinToString("") { chunkIndex -> chunks[chunkIndex] }
    val reorderComplete = selectedChunkIndexes.size == chunks.size
    val reorderCorrect = reorderComplete && reorderAnswer == correctReorder
    val blankChunk = remember(line.id) {
        chunks.getOrElse((chunks.size - 1).coerceAtLeast(0) / 2) { line.chinese }
    }
    val fillSentence = remember(line.id, blankChunk) {
        line.chinese.replaceFirst(blankChunk, "＿＿＿")
    }
    val fillOptions = remember(line.id, lines) {
        val distractors = lines
            .filter { it.id != line.id }
            .flatMap { splitMandarinChunks(it.chinese) }
            .filter { it.isNotBlank() && it != blankChunk }
            .distinct()
            .shuffled()
            .take(3)
        (listOf(blankChunk) + distractors).distinct().shuffled()
    }
    val listenOptions = remember(line.id, lines) {
        (listOf(line.chinese) + lines.filter { it.id != line.id }.shuffled().take(3).map { it.chinese })
            .distinct()
            .shuffled()
    }

    val correctAnswer = when (task.mode) {
        DialoguePracticeMode.REORDER -> correctReorder
        DialoguePracticeMode.FILL_BLANK -> blankChunk
        DialoguePracticeMode.LISTEN_SELECT -> line.chinese
    }
    val options = when (task.mode) {
        DialoguePracticeMode.REORDER -> emptyList()
        DialoguePracticeMode.FILL_BLANK -> fillOptions
        DialoguePracticeMode.LISTEN_SELECT -> listenOptions
    }
    val answerCorrect = when (task.mode) {
        DialoguePracticeMode.REORDER -> reorderCorrect
        DialoguePracticeMode.FILL_BLANK,
        DialoguePracticeMode.LISTEN_SELECT -> selectedOption == correctAnswer
    }
    val attempted = when (task.mode) {
        DialoguePracticeMode.REORDER -> reorderComplete
        DialoguePracticeMode.FILL_BLANK,
        DialoguePracticeMode.LISTEN_SELECT -> selectedOption != null
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Cümle Çalışması", style = MaterialTheme.typography.headlineMedium)
        Text("${index + 1} / ${tasks.size} · ${lines.size} diyalog cümlesinin tamamı")

        when (task.mode) {
            DialoguePracticeMode.REORDER -> {
                Text("Sıralama", style = MaterialTheme.typography.titleMedium)
                Text("Parçalara doğru sırayla dokun. Seçtiğin parçaya tekrar dokunarak geri alabilirsin.")

                Card(Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
                    Column(
                        Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedChunkIndexes.chunked(3).forEach { rowIndexes ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowIndexes.forEach { chunkIndex ->
                                    AssistChip(
                                        onClick = {
                                            selectedChunkIndexes = selectedChunkIndexes.toMutableList().also {
                                                it.remove(chunkIndex)
                                            }
                                        },
                                        label = { Text(chunks[chunkIndex]) }
                                    )
                                }
                            }
                        }
                        if (selectedChunkIndexes.isEmpty()) {
                            Text("Seçilen parçalar burada görünecek.")
                        }
                    }
                }

                val remaining = shuffledChunks.filter { (chunkIndex, _) ->
                    chunkIndex !in selectedChunkIndexes
                }
                remaining.chunked(3).forEach { rowChunks ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowChunks.forEach { (chunkIndex, chunk) ->
                            AssistChip(
                                onClick = {
                                    selectedChunkIndexes = selectedChunkIndexes + chunkIndex
                                },
                                label = { Text(chunk) }
                            )
                        }
                    }
                }

                if (reorderComplete) {
                    Text(if (reorderCorrect) "✓ Doğru" else "Tekrar dene")
                }
            }
            DialoguePracticeMode.FILL_BLANK -> {
                Text("Boşluk Doldurma", style = MaterialTheme.typography.titleMedium)
                Text(fillSentence, style = MaterialTheme.typography.headlineSmall)
            }
            DialoguePracticeMode.LISTEN_SELECT -> {
                Text("Dinleme-Anlama", style = MaterialTheme.typography.titleMedium)
                Text("Cümleyi dinle ve duyduğun Çince cümleyi seç.")
            }
        }

        OutlinedButton(
            onClick = {
                val ok = audioPlayer.play(
                    "${sceneIdToAssetPath(sceneId)}/${line.audioFile}",
                    playbackSpeed
                )
                audioMissing = !ok
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("🔊 Cümleyi Dinle") }

        if (audioMissing) {
            Text("Bu cümlenin Mandarin sesi bulunamadı.", color = MaterialTheme.colorScheme.error)
        }

        if (task.mode != DialoguePracticeMode.REORDER) {
            options.forEach { option ->
                OutlinedButton(
                    onClick = { selectedOption = option },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(option)
                }
            }

            if (selectedOption != null) {
                Text(
                    if (selectedOption == correctAnswer) "✓ Doğru" else "Tekrar dene"
                )
            }
        }

        Button(
            onClick = {
                val completed = index + 1
                onProgress(((completed.toFloat() / tasks.size) * 100).toInt())
                if (index == tasks.lastIndex) {
                    onProgress(100)
                    onComplete()
                } else {
                    index++
                }
            },
            enabled = attempted && answerCorrect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (index == tasks.lastIndex) "Cümle Çalışmasını Tamamla ✓" else "Sonraki →")
        }
    }
}

private fun splitMandarinChunks(text: String): List<String> {
    val cleaned = text.trim()
    if (cleaned.length <= 3) return cleaned.map { it.toString() }
    val targetParts = when {
        cleaned.length <= 6 -> 3
        cleaned.length <= 12 -> 4
        else -> 5
    }
    val chunkSize = kotlin.math.ceil(cleaned.length.toDouble() / targetParts).toInt().coerceAtLeast(1)
    return cleaned.chunked(chunkSize)
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
    onSimilarityResult: (Int) -> Unit,
    onProgress: (Int) -> Unit,
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
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var similarityPercent by remember { mutableStateOf<Int?>(null) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    var recognitionInProgress by remember { mutableStateOf(false) }
    var offlineModelReady by remember { mutableStateOf(false) }
    var offlineModelPreparing by remember { mutableStateOf(true) }
    var offlineModelError by remember { mutableStateOf<String?>(null) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val shadowingEngine = remember {
        OfflineMandarinShadowingEngine(context.applicationContext)
    }
    val audioPlayer = remember { AssetAudioPlayer(context.applicationContext) }

    LaunchedEffect(Unit) {
        shadowingEngine.prepare(
            onReady = {
                offlineModelReady = true
                offlineModelPreparing = false
                offlineModelError = null
            },
            onError = { message ->
                offlineModelReady = false
                offlineModelPreparing = false
                offlineModelError = message
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            shadowingEngine.release()
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
        Text("Telaffuz Çalışması", style = MaterialTheme.typography.headlineMedium)

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

            if (offlineModelPreparing) {
                Text(
                    "Mandarin tanıma modeli hazırlanıyor…",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (offlineModelError != null) {
                Text(
                    offlineModelError.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { started = true },
                enabled = (hasMicPermission && offlineModelReady) || listenOnly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Telaffuz Çalışmasını Başlat")
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
                            recognizedText = null
                            similarityPercent = null
                            recognitionError = null
                            recognitionInProgress = false
                            recordedPath = null

                            val ok = shadowingEngine.start(
                                targetText = line.chinese,
                                onResult = { result ->
                                    isRecording = false
                                    recognitionInProgress = false
                                    recordedPath = result.recordingPath
                                    recognizedText = result.recognizedText
                                    similarityPercent = result.similarityPercent
                                    onSimilarityResult(result.similarityPercent)
                                    recognitionError = null
                                },
                                onError = { message ->
                                    isRecording = false
                                    recognitionInProgress = false
                                    recognitionError = message
                                }
                            )
                            if (ok) {
                                isRecording = true
                            }
                        } else {
                            shadowingEngine.stop()
                            isRecording = false
                            recognitionInProgress = true
                        }
                    },
                    enabled = offlineModelReady && !recognitionInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isRecording) "■ Kaydı Durdur" else "🎙 Kaydı Başlat")
                }

                if (isRecording) {
                    Text(
                        "Konuş; sustuğunda kayıt otomatik duracak.",
                        style = MaterialTheme.typography.labelMedium
                    )
                } else if (recognitionInProgress) {
                    Text(
                        "Telaffuz karşılaştırılıyor…",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = { recordedPath?.let { audioPlayer.playFile(it) } },
                    enabled = recordedPath != null && !isRecording,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("▶ Kendi Kaydımı Dinle")
                }

                if (recognizedText != null && similarityPercent != null) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Benzerlik: %$similarityPercent",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Tanınan: $recognizedText",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (recognitionError != null) {
                    Text(
                        recognitionError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!offlineModelReady && offlineModelPreparing) {
                    Text(
                        "Mandarin tanıma modeli hazırlanıyor…",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Text("Yalnız dinleme modu")
            }

            val canAdvance = if (listenOnly) {
                referencePlayed
            } else {
                recordedPath != null && !recognitionInProgress
            }

            Button(
                onClick = {
                    shadowingEngine.cancel()
                    recordedPath = null
                    isRecording = false
                    recognizedText = null
                    similarityPercent = null
                    recognitionError = null
                    recognitionInProgress = false
                    referencePlayed = false
                    referenceMissing = false

                    val completed = currentIndex + 1
                    onProgress(((completed.toFloat() / sessionLines.size) * 100).toInt())
                    if (currentIndex == sessionLines.lastIndex) {
                        onProgress(100)
                        onComplete()
                    } else {
                        currentIndex++
                    }
                },
                enabled = canAdvance && !isRecording,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (currentIndex == sessionLines.lastIndex) {
                        "Oturumu Tamamla ✓"
                    } else {
                        "Sonraki Cümle →"
                    }
                )
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
