package com.example.venuehub.ui.features.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venuehub.ui.features.admin.RoomItemData
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class RoomDetailViewModel : ViewModel() {
    // UI States
    var roomData by mutableStateOf<RoomItemData?>(null)
    var isLoading by mutableStateOf(true)

    // Database Read (Fetch Detail Ruangan)
    fun fetchRoomDetail(roomId: Long) {
        viewModelScope.launch {
            try {
                isLoading = true
                val result = SupabaseClient.client.from("rooms")
                    .select { filter { eq("id", roomId) } }
                    .decodeSingle<RoomItemData>()
                roomData = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}