package com.example.venuehub.ui.features.booking

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import com.example.venuehub.ui.theme.BluePrimary
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.example.venuehub.model.CheckoutRequest

@Composable
fun CheckoutScreen(navController: NavController, bookingId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        imageUri = it
    }

    Scaffold(
        topBar = { AdminHeader(title = "Checkout Ruangan", onBackClick = { navController.popBackStack() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentPadding = PaddingValues(20.dp)
        ) {
            item {
                Text(
                    "Terima kasih telah menggunakan ruangan!",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Mohon lampirkan bukti kebersihan sebelum meninggalkan ruangan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Foto Bukti Kebersihan", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F0F0))
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                        .clickable { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                            Text("Upload Foto", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Catatan (Opsional)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                AdminInputRef(
                    value = notes,
                    onValueChange = { notes = it },
                    hint = "Misal: Remote AC ditaruh di meja dosen",
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                } else {
                    VenueHubButton(
                        text = "Selesaikan Peminjaman",
                        onClick = {
                            if (imageUri == null) {
                                Toast.makeText(
                                    context,
                                    "Wajib upload foto bukti kebersihan!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@VenueHubButton
                            }

                            scope.launch {
                                isLoading = true
                                try {
                                    val user =
                                        SupabaseClient.client.auth.currentUserOrNull() ?: return@launch

                                    val byteArray =
                                        context.contentResolver.openInputStream(imageUri!!)?.readBytes()
                                            ?: throw Exception("Gagal baca gambar")
                                    val fileName =
                                        "checkout_${bookingId}_${System.currentTimeMillis()}.jpg"
                                    val bucket =
                                        SupabaseClient.client.storage.from("checkout-proofs")
                                    bucket.upload(fileName, byteArray)
                                    val publicUrl = bucket.publicUrl(fileName)

                                    val checkout = CheckoutRequest(
                                        booking_id = bookingId,
                                        user_id = user.id,
                                        notes = notes,
                                        clean_proof_url = publicUrl
                                    )
                                    SupabaseClient.client.from("checkouts").insert(checkout)

                                    SupabaseClient.client.from("bookings").update(
                                        {
                                            set("status", "completed")
                                        }
                                    ) {
                                        filter {
                                            eq("id", bookingId)
                                        }
                                    }

                                    Toast.makeText(
                                        context,
                                        "Checkout Berhasil! Terima kasih.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack()

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT)
                                        .show()
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
}