package com.example.venuehub.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.venuehub.model.BookingHistoryItem
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminBookingApprovalScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var bookingList by remember { mutableStateOf<List<BookingHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val result = SupabaseClient.client.from("bookings")
                .select(columns = Columns.raw("*, rooms(name)")) {

                    order("status", order = Order.DESCENDING)
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<BookingHistoryItem>()
            bookingList = result
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }


    fun updateStatus(bookingId: Long, newStatus: String) {
        scope.launch {
            try {
                SupabaseClient.client.from("bookings").update(
                    {
                        set("status", newStatus)
                    }
                ) {
                    filter { eq("id", bookingId) }
                }
                Toast.makeText(context, "Status diubah menjadi $newStatus", Toast.LENGTH_SHORT).show()
                refreshTrigger++
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { AdminHeader(title = "Persetujuan Booking", onBackClick = {}) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else if (bookingList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data pengajuan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(bookingList) { booking ->
                        AdminApprovalCard(
                            booking = booking,
                            onApprove = { updateStatus(booking.id, "approved") },
                            onReject = { updateStatus(booking.id, "rejected") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminApprovalCard(
    booking: BookingHistoryItem,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val displayDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
        formatter.format(parser.parse(booking.start_time) ?: "")
    } catch (e: Exception) { booking.start_time }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(15.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = booking.rooms?.name ?: "Unknown Room",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(5.dp))


                Text(text = booking.event_name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(5.dp))


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = displayDate, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))


            if (booking.status == "pending") {
                Row {

                    IconButton(
                        onClick = onReject,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tolak", tint = Color.Red)
                    }

                    Spacer(modifier = Modifier.width(8.dp))


                    IconButton(
                        onClick = onApprove,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Terima", tint = Color(0xFF4CAF50))
                    }
                }
            } else {

                val (color, text) = when (booking.status) {
                    "approved" -> Color(0xFF4CAF50) to "Disetujui"
                    "completed" -> Color(0xFF2196F3) to "Selesai"
                    "rejected" -> Color.Red to "Ditolak"
                    else -> Color.Gray to booking.status
                }

                Text(
                    text = text,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 5.dp)
                )
            }
        }
    }
}