package com.uysal23.newchinese

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NewChineseApp() }
    }
}

@Composable
private fun NewChineseApp() {
    MaterialTheme {
        var userName by remember { mutableStateOf("Ayhan") }
        Surface(Modifier.fillMaxSize()) {
            Dashboard(userName = userName, onNameChange = { userName = it })
        }
    }
}

@Composable
private fun Dashboard(userName: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("你好，$userName！", style = MaterialTheme.typography.headlineMedium)
        Text("Bugün Çince çalışmaya devam edelim.")
        MetricCard("Telaffuz Doğruluk", "Henüz veri yok")
        DashboardButton("HSK Seviyeleri")
        DashboardButton("Seviye Tespit Sınavı")
        DashboardButton("Serbest Çalışma")
        DashboardButton("Favoriler")
        DashboardButton("İlerlemem")
        DashboardButton("Ayarlar")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Kullanıcı adı (geçici iskelet)") },
            singleLine = true
        )
    }
}

@Composable
private fun DashboardButton(text: String) {
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
