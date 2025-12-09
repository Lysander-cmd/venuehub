package com.example.venuehub.ui.features.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import io.github.jan.supabase.postgrest.query.Order



@Serializable
data class BookingHistoryItem(
    val id: Long,
    val event_name: String,
    val start_time: String,
    val status: String,
    val rooms: RoomInfo? = null
)

@Serializable
data class RoomInfo(
    val name: String
)

@Composable
fun HistoryBookingScreen(navController: NavController) {
    var bookingList by remember { mutableStateOf<List<BookingHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            if (user != null) {
                // Fetch bookings user ini, join dengan tabel rooms untuk ambil nama ruangan
                val result = SupabaseClient.client.from("bookings")
                    .select(columns = Columns.raw("*, rooms(name)")) {
                        filter {
                            eq("user_id", user.id)
                        }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<BookingHistoryItem>()
                bookingList = result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        AdminHeader(title = "Riwayat Peminjaman", onBackClick = { })

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (bookingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat peminjaman.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(bookingList) { booking ->
                    BookingHistoryCard(booking = booking, navController = navController)
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: BookingHistoryItem,navController: NavController) {
    val statusColor = when (booking.status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }

    val statusText = when (booking.status.lowercase()) {
        "approved" -> "Disetujui"
        "rejected" -> "Ditolak"
        else -> "Menunggu Konfirmasi"
    }

    val displayDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = parser.parse(booking.start_time)
        formatter.format(date ?: "")
    } catch (e: Exception) {
        booking.start_time
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = booking.rooms?.name ?: "Ruangan Dihapus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BluePrimary
                )

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = booking.event_name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = displayDate, fontSize = 12.sp, color = Color.Gray)
            }
            if (booking.status == "approved") {
                Spacer(modifier = Modifier.height(15.dp))
                Button(
                    onClick = {
                        navController.navigate("checkout/${booking.id}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Checkout / Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}