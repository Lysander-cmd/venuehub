package com.example.venuehub.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.features.booking.RoomInfo
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class AdminReportItem(
    val id: Long,
    val description: String,
    val severity: String,
    val status: String,
    val proof_url: String?,
    val created_at: String,
    val rooms: RoomInfo? = null
)

@Composable
fun AdminReportListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var reportList by remember { mutableStateOf<List<AdminReportItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val result = SupabaseClient.client.from("damage_reports")
                .select(columns = Columns.raw("*, rooms(name)")) {
                    order("status", order = Order.DESCENDING)
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<AdminReportItem>()
            reportList = result
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    fun markAsFixed(reportId: Long) {
        scope.launch {
            try {
                SupabaseClient.client.from("damage_reports").update(
                    { set("status", "fixed") }
                ) {
                    filter { eq("id", reportId) }
                }

                Toast.makeText(context, "Laporan ditandai Selesai!", Toast.LENGTH_SHORT).show()
                refreshTrigger++
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { AdminHeader(title = "Laporan Kerusakan", onBackClick = {}) }
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
            } else if (reportList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada laporan kerusakan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reportList) { report ->
                        AdminReportCard(report = report, onFix = { markAsFixed(report.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportCard(report: AdminReportItem, onFix: () -> Unit) {
    val isOpen = report.status == "open"
    val statusColor = if (isOpen) Color(0xFFFF9800) else Color(0xFF4CAF50)
    val statusText = if (isOpen) "Belum Diperbaiki" else "Sudah Diperbaiki"

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
                    text = report.rooms?.name ?: "Unknown Room",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BluePrimary
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(70.dp)
                ) {
                    AsyncImage(
                        model = report.proof_url ?: "https://placehold.co/100",
                        contentDescription = "Bukti Kerusakan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(text = "Tingkat: ${report.severity}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.description,
                        fontSize = 14.sp,
                        maxLines = 3,
                        lineHeight = 18.sp
                    )
                }
            }

            if (isOpen) {
                Spacer(modifier = Modifier.height(15.dp))
                Button(
                    onClick = onFix,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tandai Sudah Diperbaiki", fontSize = 12.sp)
                }
            }
        }
    }
}