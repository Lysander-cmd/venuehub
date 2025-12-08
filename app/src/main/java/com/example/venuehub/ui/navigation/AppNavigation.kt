package com.example.venuehub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.venuehub.ui.features.admin.AdminAddRoomScreen
import com.example.venuehub.ui.features.admin.AdminRoomDetailScreen
import com.example.venuehub.ui.features.admin.AdminRoomListScreen
// Import layar Login dan Register yang sudah kita buat
import com.example.venuehub.ui.features.auth.LoginScreen
import com.example.venuehub.ui.features.auth.RegisterScreen
import com.example.venuehub.ui.features.booking.BookingFormScreen
import com.example.venuehub.ui.features.booking.CheckoutScreen
import com.example.venuehub.ui.features.booking.UserRoomDetailScreen
import com.example.venuehub.ui.features.home.AlurScreen
import com.example.venuehub.ui.features.home.HomeScreen
import com.example.venuehub.ui.features.home.KetentuanScreen
import com.example.venuehub.ui.features.main.AdminMainScreen
import com.example.venuehub.ui.features.main.UserMainScreen
import com.example.venuehub.ui.features.report.AddReportScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController = navController)

        }

        composable("home") {
            UserMainScreen(rootNavController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("home") {
            UserMainScreen(rootNavController = navController)
        }
        composable("ketentuan") {
            KetentuanScreen(navController)
        }
        composable("alur") {
            AlurScreen(navController)
        }
        composable("admin_home") {
            AdminMainScreen(rootNavController = navController)
        }

        composable("admin_add_room") {
            AdminAddRoomScreen(navController)
        }
        composable(
            route = "admin_room_detail/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->

            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            AdminRoomDetailScreen(navController, roomId)
        }
        composable(
            route = "user_room_detail/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            UserRoomDetailScreen(navController, roomId)
        }
        composable(
            route = "booking_form/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            BookingFormScreen(navController, roomId)
        }
        composable(
            route = "checkout/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getLong("bookingId") ?: 0L
            CheckoutScreen(navController, bookingId)
        }
        composable("add_report") {
            AddReportScreen(navController)
        }
    }
}