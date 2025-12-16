package com.example.venuehub.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.venuehub.ui.features.booking.HistoryBookingScreen
import com.example.venuehub.ui.features.home.HomeScreen
import com.example.venuehub.ui.features.profile.ProfileScreen
import com.example.venuehub.ui.features.report.ReportScreen
import com.example.venuehub.ui.theme.BluePrimary
import androidx.compose.runtime.saveable.rememberSaveable

sealed class UserBottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : UserBottomNavItem("home_tab", "Beranda", Icons.Default.Home)
    object History : UserBottomNavItem("history_tab", "Riwayat", Icons.Default.History)
    object Report : UserBottomNavItem("report_tab", "Lapor", Icons.Default.Warning)
    object Profile : UserBottomNavItem("profile_tab", "Profil", Icons.Default.Person)
}



@Composable
fun UserMainScreen(rootNavController: NavController) {
    val items = listOf(
        UserBottomNavItem.Home,
        UserBottomNavItem.History,
        UserBottomNavItem.Report,
        UserBottomNavItem.Profile
    )
    
    // Gunakan rememberSaveable agar state (tab yang dipilih) tersimpan saat navigasi
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    val currentTab = items[selectedItemIndex]

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = BluePrimary,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedItemIndex == index

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = { selectedItemIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary.copy(alpha = 0.1f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                UserBottomNavItem.Home -> {
                    HomeScreen(navController = rootNavController)
                }
                UserBottomNavItem.History -> {
                    HistoryBookingScreen(navController = rootNavController)
                }
                UserBottomNavItem.Report -> {
                    ReportScreen(navController = rootNavController)
                }
                UserBottomNavItem.Profile -> {
                    ProfileScreen(navController = rootNavController)
                }
            }
        }
    }
}