package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.SentenceExercise
import com.uysal23.newchinese.data.VocabularyItem
import com.uysal23.newchinese.data.progress.SceneProgressEntity
import kotlin.math.roundToInt

@Composable
fun WordExamScreen(
    words: List<VocabularyItem>,
    onFinished: (Int) -> Unit,
    onBack: () -> Unit
) {
    val questions = remember(words) { words.shuffled() }
    var index by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        val score = ((correct.toDouble() / questions.size) * 100).roundToInt()
        ExamResultScreen(
            title = "Kelime Sınavı",
            score = score,
            passScore = 90,
            passed = score >= 90,
            onContinue = { onFinished(score) },
            onBack = onBack
        )
        return
    }

    val q = questions[index]
    val options = remember(q.id, words) {
        (listOf(q.turkish) + words.filter { it.id != q.id }.shuffled().take(3).map { it.turkish }).shuffled()
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Kelime Sınavı", style = MaterialTheme.typography.headlineMedium)
        Text("${index + 1} / ${questions.size}")
        LinearProgressIndicator(
            progress = { (index + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(q.hanzi, style = MaterialTheme.typography.displaySmall)
        Text(q.pinyin)
        Text("Doğru Türkçe anlamı seç.")

        options.forEach { option ->
            OutlinedButton(
                onClick = {
                    if (!answered) {
                        selected = option
                        answered = true
                        if (option == q.turkish) correct++
                    }
                },
                enabled = !answered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(option)
            }
        }

        if (answered) {
            Text(if (selected == q.turkish) "✓ Doğru" else "Yanlış. Doğru cevap: ${q.turkish}")
            Button(
                onClick = {
                    if (index == questions.lastIndex) {
                        finished = true
                    } else {
                        index++
                        answered = false
                        selected = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (index == questions.lastIndex) "Sınavı Bitir" else "Sonraki")
            }
        }
    }
}

@Composable
fun SentenceExamScreen(
    exercises: List<SentenceExercise>,
    wordExamPassed: Boolean,
    onFinished: (Int) -> Unit,
    onBack: () -> Unit
) {
    if (!wordExamPassed) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cümle Sınavı", style = MaterialTheme.typography.headlineMedium)
            Text("Önce kelime sınavını en az %90 ile geçmelisin.")
            OutlinedButton(onClick = onBack) { Text("Geri") }
        }
        return
    }

    var index by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var answered by remember(index) { mutableStateOf(false) }
    var chosen by remember(index) { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        val score = ((correct.toDouble() / exercises.size) * 100).roundToInt()
        ExamResultScreen(
            title = "Cümle Sınavı",
            score = score,
            passScore = 85,
            passed = score >= 85,
            onContinue = { onFinished(score) },
            onBack = onBack
        )
        return
    }

    val exercise = exercises[index]
    val correctAnswer = when (exercise.type) {
        "fill_blank" -> exercise.correctAnswer.orEmpty()
        "reorder" -> exercise.tokens.joinToString("")
        else -> exercise.correctZh
    }

    val options = remember(exercise.id) {
        when (exercise.type) {
            "fill_blank" -> exercise.options.shuffled()
            "reorder" -> listOf(
                exercise.tokens.joinToString(""),
                exercise.tokens.reversed().joinToString(""),
                exercise.tokens.shuffled().joinToString("")
            ).distinct().shuffled()
            else -> listOf(correctAnswer)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Cümle Sınavı", style = MaterialTheme.typography.headlineMedium)
        Text("${index + 1} / ${exercises.size}")

        when (exercise.type) {
            "fill_blank" -> Text(exercise.sentenceZh.orEmpty().replace("___", " - - - - - - - - "))
            "reorder" -> Text("Doğru kelime sırasını seç.")
            else -> Text(exercise.correctZh)
        }

        options.forEach { option ->
            OutlinedButton(
                onClick = {
                    if (!answered) {
                        chosen = option
                        answered = true
                        if (option == correctAnswer) correct++
                    }
                },
                enabled = !answered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(option)
            }
        }

        if (answered) {
            Text(if (chosen == correctAnswer) "✓ Doğru" else "Yanlış. Doğru cevap: $correctAnswer")
            Button(
                onClick = {
                    if (index == exercises.lastIndex) finished = true else index++
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (index == exercises.lastIndex) "Sınavı Bitir" else "Sonraki")
            }
        }
    }
}

@Composable
private fun ExamResultScreen(
    title: String,
    score: Int,
    passScore: Int,
    passed: Boolean,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("%$score", style = MaterialTheme.typography.displayMedium)
        Text(if (passed) "Başarılı ✓" else "Geçmek için en az %$passScore gerekli.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(if (passed) "Devam Et" else "Sonucu Kaydet")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
    }
}

@Composable
fun ProgressScreen(progress: List<SceneProgressEntity>) {
    val completed = progress.count { it.sceneCompleted }
    val unlocked = progress.count { it.unlocked }
    val sc001 = progress.firstOrNull { it.sceneId == "HSK1_SC001" }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("İlerlemem", style = MaterialTheme.typography.headlineMedium)
        MetricCard("Tamamlanan sahne", "$completed / 300")
        MetricCard("Açılmış sahne", "$unlocked / 300")
        MetricCard("Telaffuz Doğruluk", "Henüz veri yok")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("HSK1 · SC01", style = MaterialTheme.typography.titleLarge)
                Text("Kelime sınavı: %${sc001?.wordExamBestScore ?: 0}")
                Text("Cümle sınavı: %${sc001?.sentenceExamBestScore ?: 0}")
                Text(if (sc001?.sceneCompleted == true) "Durum: Tamamlandı ✓" else "Durum: Devam ediyor")
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
