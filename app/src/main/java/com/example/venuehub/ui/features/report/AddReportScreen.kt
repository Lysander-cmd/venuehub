package com.example.venuehub.ui.features.report

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.components.VenueHubButton
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.features.admin.AdminInputRef
import com.example.venuehub.ui.features.admin.RoomItemData
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class DamageReportRequest(
    val room_id: Long,
    val user_id: String,
    val description: String,
    val severity: String,
    val proof_url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Ringan") } // Default
    var selectedRoom by remember { mutableStateOf<RoomItemData?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var roomsList by remember { mutableStateOf<List<RoomItemData>>(emptyList()) }
    var expandedDropdown by remember { mutableStateOf(false) } // Untuk Dropdown Ruangan
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        imageUri = it
    }
    LaunchedEffect(Unit) {
        try {
            val result = SupabaseClient.client.from("rooms").select().decodeList<RoomItemData>()
            roomsList = result
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = { AdminHeader(title = "Buat Laporan", onBackClick = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 1. PILIH RUANGAN (Dropdown)
            Text("Lokasi Kerusakan", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = selectedRoom?.name ?: "Pilih Ruangan",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    roomsList.forEach { room ->
                        DropdownMenuItem(
                            text = { Text(room.name) },
                            onClick = {
                                selectedRoom = room
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text("Deskripsi Kerusakan", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            AdminInputRef(
                value = description,
                onValueChange = { description = it },
                hint = "Contoh: AC Bocor air menetes ke lantai",
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text("Tingkat Kerusakan", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("Ringan", "Sedang", "Parah").forEach { level ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (severity == level),
                            onClick = { severity = level },
                            colors = RadioButtonDefaults.colors(selectedColor = BluePrimary)
                        )
                        Text(level)
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text("Bukti Foto", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F0F0))
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    .clickable { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                        Text("Upload Foto", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = BluePrimary)
            } else {
                VenueHubButton(
                    text = "Kirim Laporan",
                    onClick = {
                        if (selectedRoom == null || description.isBlank() || imageUri == null) {
                            Toast.makeText(context, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
                            return@VenueHubButton
                        }

                        scope.launch {
                            isLoading = true
                            try {
                                val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch

                                val byteArray = context.contentResolver.openInputStream(imageUri!!)?.readBytes() ?: throw Exception("Gagal baca gambar")
                                val fileName = "report_${System.currentTimeMillis()}.jpg"
                                val bucket = SupabaseClient.client.storage.from("damage-proofs")
                                bucket.upload(fileName, byteArray)
                                val publicUrl = bucket.publicUrl(fileName)

                                val report = DamageReportRequest(
                                    room_id = selectedRoom!!.id,
                                    user_id = user.id,
                                    description = description,
                                    severity = severity,
                                    proof_url = publicUrl
                                )
                                SupabaseClient.client.from("damage_reports").insert(report)

                                Toast.makeText(context, "Laporan Terkirim!", Toast.LENGTH_LONG).show()
                                navController.popBackStack()

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }
}