package com.example.venuehub.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.venuehub.ui.features.admin.AdminBookingApprovalScreen
import com.example.venuehub.ui.features.admin.AdminReportListScreen
import com.example.venuehub.ui.features.admin.AdminRoomListScreen
import com.example.venuehub.ui.features.profile.ProfileScreen
import com.example.venuehub.ui.theme.BluePrimary

sealed class AdminBottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Rooms : AdminBottomNavItem("rooms_tab", "Ruangan", Icons.Default.MeetingRoom)
    object Approval : AdminBottomNavItem("approval_tab", "Persetujuan", Icons.Default.Assignment)
    object Reports : AdminBottomNavItem("reports_tab", "Laporan", Icons.Default.Build)
    object Profile : AdminBottomNavItem("profile_tab", "Profil", Icons.Default.Person)
}

@Composable
fun AdminMainScreen(rootNavController: NavController) {
    var currentTab by remember { mutableStateOf<AdminBottomNavItem>(AdminBottomNavItem.Rooms) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = BluePrimary) {
                val items = listOf(
                    AdminBottomNavItem.Rooms,
                    AdminBottomNavItem.Approval,
                    AdminBottomNavItem.Reports,
                    AdminBottomNavItem.Profile
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
                            indicatorColor = BluePrimary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                AdminBottomNavItem.Rooms -> {
                    AdminRoomListScreen(navController = rootNavController)
                }
                AdminBottomNavItem.Approval -> {
                    AdminBookingApprovalScreen(navController = rootNavController)
                }
                AdminBottomNavItem.Reports -> {
                    AdminReportListScreen(navController = rootNavController)
                }
                AdminBottomNavItem.Profile -> {
                    ProfileScreen(navController = rootNavController)
                }
            }
        }
    }
}