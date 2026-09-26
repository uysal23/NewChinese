package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

private data class BottomDestination(
    val route: String,
    val label: String
)

private val bottomDestinations = listOf(
    BottomDestination("dashboard", "Ana Sayfa"),
    BottomDestination("freeStudy", "Serbest Çalışma"),
    BottomDestination("favorites", "Favoriler"),
    BottomDestination("progress", "İlerleme"),
    BottomDestination("settings", "Ayarlar")
)

@Composable
fun MainScaffold(
    nav: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            nav.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("dashboard") { saveState = true }
                            }
                        },
                        icon = { Text(destinationIcon(destination.route)) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            content()
        }
    }
}

private fun destinationIcon(route: String): String = when (route) {
    "dashboard" -> "⌂"
    "freeStudy" -> "学"
    "favorites" -> "★"
    "progress" -> "↗"
    "settings" -> "⚙"
    else -> "•"
}
