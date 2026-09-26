package com.uysal23.newchinese.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun lightScheme(palette: String) = when (palette) {
    "BLUE" -> lightColorScheme(primary=Color(0xFF627D98), secondary=Color(0xFF8FAFC7), surface=Color(0xFFF7FAFC))
    "GREEN" -> lightColorScheme(primary=Color(0xFF668F80), secondary=Color(0xFF9BBBAE), surface=Color(0xFFF7FAF8))
    "PEACH" -> lightColorScheme(primary=Color(0xFFB77A68), secondary=Color(0xFFD9A896), surface=Color(0xFFFFF8F5))
    else -> lightColorScheme(primary=Color(0xFF7B6FA6), secondary=Color(0xFFA79BC8), surface=Color(0xFFFAF8FF))
}

private fun darkScheme(palette: String) = when (palette) {
    "BLUE" -> darkColorScheme(primary=Color(0xFFAFC8DA), secondary=Color(0xFF90AFC3))
    "GREEN" -> darkColorScheme(primary=Color(0xFFB6D5C8), secondary=Color(0xFF8FB3A4))
    "PEACH" -> darkColorScheme(primary=Color(0xFFE7B7A6), secondary=Color(0xFFC99584))
    else -> darkColorScheme(primary=Color(0xFFCFC4F2), secondary=Color(0xFFB2A6D2))
}

@Composable
fun NewChineseTheme(darkMode: Boolean, palette: String, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkMode) darkScheme(palette) else lightScheme(palette), content = content)
}
