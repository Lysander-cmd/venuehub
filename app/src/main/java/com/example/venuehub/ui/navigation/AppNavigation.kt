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
import com.example.venuehub.ui.features.booking.BookingDetailPendingScreen
import com.example.venuehub.ui.features.booking.BookingFormScreen
import com.example.venuehub.ui.features.booking.CheckoutScreen
import com.example.venuehub.ui.features.booking.UserRoomDetailScreen
import com.example.venuehub.ui.features.home.AlurScreen
import com.example.venuehub.ui.features.home.CategoryRoomScreen
import com.example.venuehub.ui.features.home.HomeScreen
import com.example.venuehub.ui.features.home.KetentuanScreen
import com.example.venuehub.ui.features.main.AdminMainScreen
import com.example.venuehub.ui.features.main.UserMainScreen
import com.example.venuehub.ui.features.report.AddReportScreen
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val currentUser = SupabaseClient.client.auth.currentUserOrNull()

    val startDest = if (currentUser != null) {
        val isAdmin = currentUser.userMetadata?.get("role_key")?.toString()?.contains("admin") == true
        if(isAdmin) "admin_home" else "home"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDest) {

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
        composable(
            route = "category_rooms/{categoryType}/{categoryName}",
            arguments = listOf(
                navArgument("categoryType") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("categoryType") ?: ""
            val name = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryRoomScreen(navController, type, name)
        }

        composable(
            "booking_detail_pending/{bookingId}"
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments
                ?.getString("bookingId")
                ?.toLongOrNull() ?: return@composable

            BookingDetailPendingScreen(
                bookingId = bookingId,
                navController = navController
            )
        }

    }
}