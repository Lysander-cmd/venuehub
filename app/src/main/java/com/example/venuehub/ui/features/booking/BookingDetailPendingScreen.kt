package com.example.venuehub.ui.features.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun BookingDetailPendingScreen(
    bookingId: Long,
    navController: NavController,
    viewModel: BookingDetailPendingViewModel = viewModel() // Panggil ViewModel
) {
    // Ambil data lewat ViewModel
    LaunchedEffect(bookingId) {
        viewModel.fetchBookingDetail(bookingId)
    }

    Scaffold(
        topBar = {
            AdminHeader(
                title = "Detail Peminjaman",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF5F5F5) // Memberikan kontras pada card
    ) { padding ->

        if (viewModel.isLoading) {
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
                "Nama Acara" to viewModel.booking?.event_name,
                "Waktu" to formatDateTimeRange(
                    viewModel.booking?.start_time ?: "",
                    viewModel.booking?.end_time ?: ""
                ),
                "Status" to bookingStatusText(viewModel.booking?.status ?: "pending")
            )

            // LazyColumn DI DETAIL
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text(
                        text = viewModel.booking?.rooms?.name ?: "-",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            detailItems.forEachIndexed { index, (label, value) ->
                                Column {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = value ?: "-",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (label == "Status") bookingStatusColor(viewModel.booking?.status ?: "pending") else Color.Black
                                    )
                                }

                                if (index < detailItems.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kartu Tanda Mahasiswa (KTM)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    if (!viewModel.booking?.ktm_url.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            AsyncImage(
                                model = viewModel.booking?.ktm_url,
                                contentDescription = "KTM",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Text(
                            text = "KTM tidak tersedia",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }

            }
        }
    }
}