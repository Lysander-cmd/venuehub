package com.example.venuehub.ui.features.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.model.RoomInfo
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable

@Serializable
data class ReportHistoryItem(
    val id: Long,
    val description: String,
    val severity: String,
    val status: String,
    val created_at: String,
    val proof_url: String?,
    val rooms: RoomInfo? = null
)

@Composable
fun ReportScreen(navController: NavController) {
    var reportList by remember { mutableStateOf<List<ReportHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            if (user != null) {
                val result = SupabaseClient.client.from("damage_reports")
                    .select(columns = Columns.raw("*, rooms(name)")) {
                        filter { eq("user_id", user.id) }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<ReportHistoryItem>()
                reportList = result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_report") },
                containerColor = BluePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Laporan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5))
        ) {
            AdminHeader(
                title = "Laporan Kerusakan",
                onBackClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                })

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else if (reportList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Belum ada laporan kerusakan.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reportList) { report ->
                        ReportItemCard(report = report, onClick = {
                            navController.navigate("detail_report/${report.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItemCard(
    report: ReportHistoryItem,
    onClick: () -> Unit
) {
    val statusColor = if (report.status == "fixed") Color(0xFF4CAF50) else Color(0xFFFF9800)
    val statusText = if (report.status == "open") "Dalam Review" else "Selesai"

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = report.rooms?.name ?: "Ruangan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BluePrimary
                )
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (report.proof_url != null) {
                AsyncImage(
                    model = report.proof_url,
                    contentDescription = "Bukti Kerusakan",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Tinggi gambar
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text("Masalah: ${report.description}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tingkat: ${report.severity}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}