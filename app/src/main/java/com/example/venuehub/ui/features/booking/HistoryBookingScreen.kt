package com.example.venuehub.ui.features.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.venuehub.model.BookingWithCheckout
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.theme.BluePrimary
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.draw.clip
import java.util.TimeZone
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun HistoryBookingScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        AdminHeader(title = "Riwayat Peminjaman", onBackClick = { navController.popBackStack() })

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (viewModel.bookingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat peminjaman.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.bookingList) { booking ->
                    BookingHistoryCard(booking = booking, navController = navController)
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: BookingWithCheckout, navController: NavController) {
    val statusColor = when (booking.status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "completed" -> Color(0xFF2196F3)
        "rejected" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }

    val statusText = when (booking.status.lowercase()) {
        "approved" -> "Disetujui"
        "completed" -> "Selesai"
        "rejected" -> "Ditolak"
        else -> "Menunggu Konfirmasi"
    }

    // FORMAT TANGGAL: Konversi dari UTC (Database) ke Asia/Jakarta (WIB)
    val displayDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        val date = parser.parse(booking.start_time)
        formatter.format(date!!)
    } catch (e: Exception) {
        booking.start_time // Fallback jika error
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = booking.status == "pending"
            ) {
                navController.navigate("booking_detail_pending/${booking.id}")
            }
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
                    onClick = { navController.navigate("checkout/${booking.id}") },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Checkout / Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (booking.status == "completed") {
                Spacer(modifier = Modifier.height(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Peminjaman telah selesai.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    val checkoutData = booking.checkouts?.firstOrNull()
                    val proofUrl = checkoutData?.clean_proof_url
                    val notes = checkoutData?.notes

                    if (proofUrl != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        var showDialog by remember { mutableStateOf(false) }

                        Text(
                            text = "Lihat Bukti Kebersihan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            modifier = Modifier
                                .clickable { showDialog = true }
                                .padding(4.dp)
                        )

                        if (showDialog) {
                            Dialog(onDismissRequest = { showDialog = false }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = proofUrl,
                                            contentDescription = "Bukti Kebersihan",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )

                                        if (!notes.isNullOrBlank()) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Catatan:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = notes,
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = { showDialog = false },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                                        ) {
                                            Text("Tutup")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}