package com.example.venuehub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home_tab", "Beranda", Icons.Default.Home)
    object History : BottomNavItem("history_tab", "Riwayat", Icons.Default.History)
    object Report : BottomNavItem("report_tab", "Lapor", Icons.Default.Warning)
    object Profile : BottomNavItem("profile_tab", "Profil", Icons.Default.Person)
}