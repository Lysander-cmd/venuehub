package com.example.venuehub.ui.features.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailReportScreen(
    navController: NavController,
    reportId: Long // Kita terima ID laporan
) {
    var reportData by remember { mutableStateOf<ReportHistoryItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(reportId) {
        try {
            val result = SupabaseClient.client.from("damage_reports")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, rooms(name)")) {
                    filter { eq("id", reportId) }
                }
                .decodeSingle<ReportHistoryItem>()
            reportData = result
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Laporan") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("report_history") {
                            popUpTo("report_history") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (reportData != null) {
            val item = reportData!!

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // 1. Gambar Besar
                if (item.proof_url != null) {
                    AsyncImage(
                        model = item.proof_url,
                        contentDescription = "Bukti Full",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) // Lebih besar dari card
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Fit // Agar seluruh gambar terlihat
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = item.rooms?.name ?: "Unknown Room",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                val statusColor = if (item.status == "fixed") Color(0xFF4CAF50) else Color(0xFFFF9800)
                val statusText = if (item.status == "open") "Belum Diperbaiki" else "Selesai Diperbaiki"

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if(item.status == "fixed") Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = statusColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Deskripsi Kerusakan:", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Text(item.description, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(15.dp))

                Text("Tingkat Keparahan:", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Text(item.severity, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(15.dp))

                Text("Tanggal Laporan:", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Text(item.created_at.take(10), fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

            }
        }
    }
}