package com.example.venuehub.ui.features.admin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.components.VenueHubButton
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Room(
    val name: String,
    val capacity: Int,
    val description: String?,
    val facilities: String?,
    @SerialName("image_url") val imageUrl: String?
)

@Composable
fun AdminAddRoomScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- STATE INPUT FORM ---
    var name by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var facilities by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            AdminHeader(
                title = "Tambah Ruangan",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .imePadding()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BluePrimary
                    )
                } else {
                    VenueHubButton(
                        text = "Simpan Ruangan",
                        onClick = {
                            if (name.isBlank() || capacity.isBlank() || imageUri == null) {
                                Toast.makeText(context, "Nama, Kapasitas, dan Foto wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@VenueHubButton
                            }

                            scope.launch {
                                isLoading = true
                                try {
                                    val byteArray = context.contentResolver.openInputStream(imageUri!!)?.use {
                                        it.readBytes()
                                    } ?: throw Exception("Gagal membaca gambar")

                                    val fileName = "room_${System.currentTimeMillis()}.jpg"
                                    val bucket = SupabaseClient.client.storage.from("room-images") // Pastikan bucket ini ada
                                    bucket.upload(fileName, byteArray)

                                    val publicUrl = bucket.publicUrl(fileName)

                                    val newRoom = Room(
                                        name = name,
                                        capacity = capacity.toIntOrNull() ?: 0,
                                        description = description,
                                        facilities = facilities,
                                        imageUrl = publicUrl
                                    )

                                    SupabaseClient.client.from("rooms").insert(newRoom)

                                    Toast.makeText(context, "Ruangan berhasil ditambahkan!", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Foto Ruangan",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F0F0))
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(50.dp)
                            )
                            Text("Klik untuk upload foto", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Detail Informasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(15.dp))


                AdminInputRef(
                    value = name,
                    onValueChange = { name = it },
                    hint = "Nama Ruangan (misal: Gedung A1)"
                )

                Spacer(modifier = Modifier.height(10.dp))

                AdminInputRef(
                    value = capacity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) capacity = it },
                    hint = "Kapasitas (Orang)",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(10.dp))

                AdminInputRef(
                    value = facilities,
                    onValueChange = { facilities = it },
                    hint = "Fasilitas (misal: AC, WiFi, Proyektor)",
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                AdminInputRef(
                    value = description,
                    onValueChange = { description = it },
                    hint = "Deskripsi Tambahan",
                    singleLine = false,
                    maxLines = 4
                )

                // Spacer tambahan agar tidak ketutup tombol
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AdminInputRef(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)) // Border abu tipis
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah default
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            maxLines = maxLines
        )
    }
}

@Composable
fun AdminHeader(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(BluePrimary)
            .clip(RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
    ) {
        Box(
            modifier = Modifier.offset(x = (-20).dp, y = (-30).dp).size(100.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
        )
        Box(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 20.dp, y = (-20).dp)
                .size(120.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}