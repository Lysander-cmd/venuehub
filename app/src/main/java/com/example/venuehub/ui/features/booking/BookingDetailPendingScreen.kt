package com.example.venuehub.ui.features.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.query.Columns
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatDateTimeRange(start: String, end: String): String {
    if (start.isBlank() || end.isBlank()) return "-"

    return try {
        val parser = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val formatter = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale("id", "ID")
        ).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }

        val startDate = parser.parse(start)
        val endDate = parser.parse(end)

        "${formatter.format(startDate!!)} - ${formatter.format(endDate!!)}"
    } catch (e: Exception) {
        "-"
    }
}


fun bookingStatusText(status: String): String = when (status.lowercase()) {
    "approved" -> "Disetujui"
    "completed" -> "Selesai"
    "rejected" -> "Ditolak"
    else -> "Menunggu Konfirmasi"
}

fun bookingStatusColor(status: String): Color = when (status.lowercase()) {
    "approved" -> Color(0xFF4CAF50)
    "completed" -> Color(0xFF2196F3)
    "rejected" -> Color(0xFFF44336)
    else -> Color(0xFFFF9800)
}

@Serializable
data class BookingDetailPending(
    val id: Long,
    val event_name: String,
    val start_time: String,
    val end_time: String,
    val status: String,
//    val ktm_url: String,
    val rooms: RoomInfo? = null
)

@Composable
fun BookingDetailPendingScreen(
    bookingId: Long,
    navController: NavController
) {
    var booking by remember { mutableStateOf<BookingDetailPending?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(bookingId) {
        try {
            booking = SupabaseClient.client
                .from("bookings")
                .select(
//                    Columns.raw(
//                        "id,event_name,start_time,end_time,status,ktm_url,rooms(name)"
//                    )
                    Columns.raw(
                        "id,event_name,start_time,end_time,status,rooms(name)"
                    )
                ) {
                    filter { eq("id", bookingId) }
                }
                .decodeSingle()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            AdminHeader(
                title = "Detail Peminjaman",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {

            val detailItems = listOf(
                "Nama Acara" to booking?.event_name,
                "Waktu" to formatDateTimeRange(
                    booking?.start_time ?: "",
                    booking?.end_time ?: ""
                ),
                "Status" to bookingStatusText(booking?.status ?: "pending")
            )


            /** 🔥 LazyColumn DI DETAIL */
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                item {
                    Text(
                        text = booking?.rooms?.name ?: "-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            detailItems.forEach { (label, value) ->
                                Text(label, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(value ?: "-", color = Color.DarkGray)

                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }


                item {
                    Text(
                        text = "Kartu Tanda Mahasiswa (KTM)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

//                item {
//                    Card(
//                        shape = RoundedCornerShape(10.dp),
//                        elevation = CardDefaults.cardElevation(2.dp)
//                    ) {
//                        AsyncImage(
//                            model = booking?.ktm_url,
//                            contentDescription = "KTM",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(240.dp),
//                            contentScale = ContentScale.Fit
//                        )
//                    }
//                }
            }
        }
    }
}
