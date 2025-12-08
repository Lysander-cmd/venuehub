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

sealed class UserBottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : UserBottomNavItem("home_tab", "Beranda", Icons.Default.Home)
    object History : UserBottomNavItem("history_tab", "Riwayat", Icons.Default.History)
    object Report : UserBottomNavItem("report_tab", "Lapor", Icons.Default.Warning)
    object Profile : UserBottomNavItem("profile_tab", "Profil", Icons.Default.Person)
}

@Composable
fun UserMainScreen(rootNavController: NavController) {
    var currentTab by remember { mutableStateOf<UserBottomNavItem>(UserBottomNavItem.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = BluePrimary,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    UserBottomNavItem.Home,
                    UserBottomNavItem.History,
                    UserBottomNavItem.Report,
                    UserBottomNavItem.Profile
                )

                items.forEach { item ->
                    val isSelected = currentTab == item

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = { currentTab = item },
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