package com.example.venuehub.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.venuehub.ui.features.booking.HistoryBookingScreen
import com.example.venuehub.ui.features.home.HomeScreen
import com.example.venuehub.ui.features.report.ReportScreen
import com.example.venuehub.ui.navigation.BottomNavItem
import com.example.venuehub.ui.theme.BluePrimary

@Composable
fun MainScreen(rootNavController: NavController) {
    var currentTab by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = BluePrimary
            ) {
                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.History,
                    BottomNavItem.Report,
                    BottomNavItem.Profile
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentTab == item,
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
                BottomNavItem.Home -> {
                    HomeScreen(navController = rootNavController)
                }
                BottomNavItem.History -> {
                    HistoryBookingScreen(navController = rootNavController)
                }
                BottomNavItem.Report -> {
                    ReportScreen(navController = rootNavController)
                }
                BottomNavItem.Profile -> {
                    com.example.venuehub.ui.features.profile.ProfileScreen(navController = rootNavController)
                }
            }
        }
    }
}