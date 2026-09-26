package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.SentenceExercise
import com.uysal23.newchinese.data.VocabularyItem

@Composable
fun StudyHubScreen(
    onVocabulary: () -> Unit,
    onSentence: () -> Unit,
    onShadowing: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Çalışma", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onVocabulary, modifier = Modifier.fillMaxWidth()) { Text("Kelime Çalışması") }
        Button(onClick = onSentence, modifier = Modifier.fillMaxWidth()) { Text("Cümle Çalışması") }
        Button(onClick = onShadowing, modifier = Modifier.fillMaxWidth()) { Text("Shadowing") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Diyaloğa Dön") }
    }
}

@Composable
fun VocabularyScreen(
    items: List<VocabularyItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
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

            Button(
                onClick = {
                    if (index < items.lastIndex) {
                        index++
                        showMeaning = false
                    }
                },
                enabled = index < items.lastIndex
            ) { Text("Sonraki →") }
        }
    }
}

@Composable
fun FavoritesScreen(
    allWords: List<VocabularyItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit
) {
    val favorites = allWords.filter { it.id in favoriteIds }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Favoriler", style = MaterialTheme.typography.headlineMedium)
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
    onVocabulary: () -> Unit,
    onSentence: () -> Unit,
    onShadowing: () -> Unit,
    onDialogue: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Serbest Çalışma", style = MaterialTheme.typography.headlineMedium)
        Text("Açılmış sahne")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("HSK1 · SC01", style = MaterialTheme.typography.titleLarge)
                Text("第一次见面 · İlk Tanışma")
            }
        }
        OutlinedButton(onClick = onDialogue, modifier = Modifier.fillMaxWidth()) { Text("Diyaloğu Aç") }
        Button(onClick = onVocabulary, modifier = Modifier.fillMaxWidth()) { Text("Kelime Çalışması") }
        Button(onClick = onSentence, modifier = Modifier.fillMaxWidth()) { Text("Cümle Çalışması") }
        Button(onClick = onShadowing, modifier = Modifier.fillMaxWidth()) { Text("Shadowing") }
    }
}

@Composable
fun SentencePracticeScreen(exercises: List<SentenceExercise>, onBack: () -> Unit) {
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
            Button(onClick = { if (index < exercises.lastIndex) index++ }, enabled = index < exercises.lastIndex) { Text("→") }
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
fun ShadowingSetupScreen(dialogueCount: Int, onBack: () -> Unit) {
    val availableOptions = listOf(6, 10, 15).filter { it <= dialogueCount }
    var selected by remember { mutableIntStateOf(availableOptions.firstOrNull() ?: dialogueCount) }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Shadowing", style = MaterialTheme.typography.headlineMedium)
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
            onClick = { selected = dialogueCount },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tüm diyalog${if (selected == dialogueCount) " ✓" else ""}")
        }
        Text("Varsayılan ve minimum hedef: 6 cümle")
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Shadowing'i Başlat")
        }
    }
}
