package com.example.venuehub.ui.features.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Calendar


@Serializable
data class BookingRequest(
    val room_id: Long,
    val user_id: String,
    val event_name: String,
    val start_time: String,
    val end_time: String,
    val status: String = "pending"
)

@Composable
fun BookingFormScreen(navController: NavController, roomId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var roomData by remember { mutableStateOf<RoomItemData?>(null) }

    var eventName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTimeStart by remember { mutableStateOf("") }
    var selectedTimeEnd by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        try {
            val result = SupabaseClient.client.from("rooms")
                .select { filter { eq("id", roomId) } }
                .decodeSingle<RoomItemData>()
            roomData = result
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            calendar.set(year, month, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerStart = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTimeStart = String.format("%02d:%02d", hour, minute)
        }, 8, 0, true
    )

    val timePickerEnd = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTimeEnd = String.format("%02d:%02d", hour, minute)
        }, 10, 0, true
    )

    Scaffold(
        topBar = {
            AdminHeader(title = "Detail Peminjaman", onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            Box(modifier = Modifier.padding(20.dp).imePadding()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BluePrimary)
                } else {
                    VenueHubButton(
                        text = "Ajukan Peminjaman",
                        onClick = {
                            if (eventName.isBlank() || selectedDate.isBlank() || selectedTimeStart.isBlank() || selectedTimeEnd.isBlank()) {
                                Toast.makeText(context, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
                                return@VenueHubButton
                            }
                            scope.launch {
                                isLoading = true
                                try {
                                    val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch

                                    val dateParts = selectedDate.split("/")
                                    val day = dateParts[0].toInt()
                                    val month = dateParts[1].toInt()
                                    val year = dateParts[2].toInt()
                                    fun makeIso(timeStr: String): String {
                                        val timeParts = timeStr.split(":")
                                        val hour = timeParts[0].toInt()
                                        val minute = timeParts[1].toInt()
                                        val cal = Calendar.getInstance()

                                        cal.set(year, month - 1, day, hour, minute, 0)
                                        cal.set(Calendar.MILLISECOND, 0)

                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

                                        return sdf.format(cal.time)

                                    }

                                    val finalStartIso = makeIso(selectedTimeStart)
                                    val finalEndIso = makeIso(selectedTimeEnd)

                                    if (finalEndIso <= finalStartIso) {
                                        Toast.makeText(context, "Jam selesai tidak boleh sebelum jam mulai!", Toast.LENGTH_LONG).show()
                                        return@launch
                                    }

                                    val existingBooking = SupabaseClient.client.from("bookings")
                                        .select {
                                            filter {
                                                eq("room_id", roomId)
                                                neq("status", "rejected")

                                                lt("start_time", finalEndIso)
                                                gt("end_time", finalStartIso)
                                            }
                                        }.decodeList<BookingRequest>()
                                    if (existingBooking.isNotEmpty()) {
                                        Toast.makeText(context, "Jadwal penuh! Ruangan sudah dipesan di jam tersebut.", Toast.LENGTH_LONG).show()
                                        isLoading = false
                                        return@launch
                                    }
                                    val booking = BookingRequest(
                                        room_id = roomId,
                                        user_id = user.id,
                                        event_name = eventName,
                                        start_time = finalStartIso,
                                        end_time = finalEndIso
                                    )

                                    SupabaseClient.client.from("bookings").insert(booking)

                                    Toast.makeText(context, "Pengajuan Berhasil!", Toast.LENGTH_LONG).show()
                                    navController.popBackStack("home", inclusive = false)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                      if (e.message?.contains("no_overlap_booking") == true) {
                                        Toast.makeText(context, "Gagal: Jadwal Bentrok (Database)", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
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

            roomData?.let { room ->
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
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }


            Column(modifier = Modifier.padding(20.dp)) {
                Text("Masukan Detail Peminjaman", style = MaterialTheme.typography.titleMedium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(15.dp))

                AdminInputRef(
                    value = eventName,
                    onValueChange = { eventName = it },
                    hint = "Keperluan / Nama Acara"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ClickableInput(
                    value = selectedDate,
                    hint = "Pilih Tanggal",
                    icon = Icons.Default.CalendarToday,
                    onClick = { datePickerDialog.show() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableInput(
                            value = selectedTimeStart,
                            hint = "Mulai",
                            icon = Icons.Default.Schedule,
                            onClick = { timePickerStart.show() }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableInput(
                            value = selectedTimeEnd,
                            hint = "Selesai",
                            icon = Icons.Default.Schedule,
                            onClick = { timePickerEnd.show() }
                        )
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