package com.example.venuehub.ui.features.booking

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venuehub.model.BookingRequest
import com.example.venuehub.ui.features.admin.RoomItemData
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class BookingViewModel : ViewModel() {
    // UI States
    var roomData by mutableStateOf<RoomItemData?>(null)
    var eventName by mutableStateOf("")
    var selectedDate by mutableStateOf("")
    var selectedTimeStart by mutableStateOf("")
    var selectedTimeEnd by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var ktmUri by mutableStateOf<Uri?>(null)

    // Read Data Ruangan
    fun fetchRoomData(roomId: Long) {
        viewModelScope.launch {
            try {
                val result = SupabaseClient.client.from("rooms")
                    .select { filter { eq("id", roomId) } }
                    .decodeSingle<RoomItemData>()
                roomData = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fungsi submit
    fun submitBooking(
        context: Context,
        roomId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch

                // Logika pembentukan ISO Time dari input
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
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    return sdf.format(cal.time)
                }

                val finalStartIso = makeIso(selectedTimeStart)
                val finalEndIso = makeIso(selectedTimeEnd)

                if (finalEndIso <= finalStartIso) {
                    onError("Jam selesai tidak boleh sebelum jam mulai!")
                    return@launch
                }

                // Cek jadwal bentrok (Read)
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
                    onError("Jadwal penuh! Ruangan sudah dipesan di jam tersebut.")
                    return@launch
                }

                // Upload ke Cloud Storage
                var ktmUrl: String? = null
                if (ktmUri != null) {
                    val inputStream = context.contentResolver.openInputStream(ktmUri!!)
                    val ktmBytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (ktmBytes != null) {
                        val fileName = "ktm_${user.id}_${System.currentTimeMillis()}.jpg"
                        SupabaseClient.client.storage.from("ktm").upload(fileName, ktmBytes)
                        ktmUrl = SupabaseClient.client.storage.from("ktm").publicUrl(fileName)
                    }
                }

                // Insert ke Database
                val booking = BookingRequest(
                    room_id = roomId,
                    user_id = user.id,
                    event_name = eventName,
                    start_time = finalStartIso,
                    end_time = finalEndIso,
                    ktm_url = ktmUrl
                )

                SupabaseClient.client.from("bookings").insert(booking)
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                if (e.message?.contains("no_overlap_booking") == true) {
                    onError("Gagal: Jadwal Bentrok (Database)")
                } else {
                    onError("Error: ${e.message}")
                }
            } finally {
                isLoading = false
            }
        }
    }
}