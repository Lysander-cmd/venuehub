package com.example.venuehub.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.components.VenueHubButton
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun AdminRoomDetailScreen(navController: NavController, roomId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var roomData by remember { mutableStateOf<RoomItemData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        try {
            val result = SupabaseClient.client.from("rooms")
                .select {
                    filter {
                        eq("id", roomId)
                    }
                }.decodeSingle<RoomItemData>()
            roomData = result
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Ruangan?") },
            text = { Text("Data ruangan ini akan dihapus permanen dan tidak bisa dikembalikan.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        scope.launch {
                            try {
                                SupabaseClient.client.from("rooms").delete {
                                    filter { eq("id", roomId) }
                                }
                                Toast.makeText(context, "Ruangan dihapus", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            AdminHeader(title = "Detail Ruangan", onBackClick = { navController.popBackStack() })
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            roomData?.let { room ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = room.imageUrl ?: "https://placehold.co/600x400/png",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = room.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoChip(icon = Icons.Default.Groups, text = "${room.capacity} Orang")
                            Spacer(modifier = Modifier.width(15.dp))

                            InfoChip(icon = Icons.Default.SquareFoot, text = "Fasilitas Lengkap")
                        }

                        Spacer(modifier = Modifier.height(25.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(25.dp))

                        Text("Deskripsi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = room.description ?: "Tidak ada deskripsi",
                            color = Color.Gray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Fasilitas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = room.facilities ?: "-",
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}