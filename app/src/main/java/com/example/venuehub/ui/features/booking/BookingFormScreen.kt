package com.example.venuehub.ui.features.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.venuehub.ui.components.VenueHubButton
import com.example.venuehub.ui.features.admin.AdminHeader
import com.example.venuehub.ui.features.admin.AdminInputRef
import com.example.venuehub.ui.theme.BluePrimary
import java.util.Calendar

@Composable
fun BookingFormScreen(
    navController: NavController,
    roomId: Long,
    viewModel: BookingViewModel = viewModel()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    LaunchedEffect(roomId) {
        viewModel.fetchRoomData(roomId)
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            viewModel.selectedDate = "$dayOfMonth/${month + 1}/$year"
            calendar.set(year, month, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerStart = TimePickerDialog(
        context,
        { _, hour, minute ->
            viewModel.selectedTimeStart = String.format("%02d:%02d", hour, minute)
        }, 8, 0, true
    )

    val timePickerEnd = TimePickerDialog(
        context,
        { _, hour, minute ->
            viewModel.selectedTimeEnd = String.format("%02d:%02d", hour, minute)
        }, 10, 0, true
    )

    val ktmPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.ktmUri = uri
    }

    Scaffold(
        topBar = {
            AdminHeader(title = "Detail Peminjaman", onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            Box(modifier = Modifier.padding(20.dp).imePadding()) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BluePrimary)
                } else {
                    VenueHubButton(
                        text = "Ajukan Peminjaman",
                        onClick = {
                            if (viewModel.eventName.isBlank() || viewModel.selectedDate.isBlank() || viewModel.selectedTimeStart.isBlank() || viewModel.selectedTimeEnd.isBlank()) {
                                Toast.makeText(context, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
                                return@VenueHubButton
                            }

                            viewModel.submitBooking(
                                context = context,
                                roomId = roomId,
                                onSuccess = {
                                    Toast.makeText(context, "Pengajuan Berhasil!", Toast.LENGTH_LONG).show()
                                    navController.popBackStack("home", inclusive = false)
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
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

            viewModel.roomData?.let { room ->
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        AsyncImage(
                            model = room.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Column {
                        Text(room.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Kapasitas: ${room.capacity}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }


            Column(modifier = Modifier.padding(20.dp)) {
                Text("Masukan Detail Peminjaman", style = MaterialTheme.typography.titleMedium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(15.dp))

                AdminInputRef(
                    value = viewModel.eventName,
                    onValueChange = { viewModel.eventName = it },
                    hint = "Keperluan / Nama Acara"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ClickableInput(
                    value = viewModel.selectedDate,
                    hint = "Pilih Tanggal",
                    icon = Icons.Default.CalendarToday,
                    onClick = { datePickerDialog.show() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableInput(
                            value = viewModel.selectedTimeStart,
                            hint = "Mulai",
                            icon = Icons.Default.Schedule,
                            onClick = { timePickerStart.show() }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableInput(
                            value = viewModel.selectedTimeEnd,
                            hint = "Selesai",
                            icon = Icons.Default.Schedule,
                            onClick = { timePickerEnd.show() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Upload KTM",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                        .clickable { ktmPicker.launch("image/*") }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.ktmUri == null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Upload Foto KTM",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap untuk memilih gambar",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }
                        } else {
                            AsyncImage(
                                model = viewModel.ktmUri,
                                contentDescription = "Preview KTM",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ClickableInput(
    value: String,
    hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            if (value.isEmpty()) {
                Text(hint, color = Color.Gray)
            } else {
                Text(value, color = Color.Black)
            }
        }
    }
}