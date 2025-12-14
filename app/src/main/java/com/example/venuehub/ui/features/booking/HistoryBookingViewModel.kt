package com.example.venuehub.ui.features.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venuehub.model.BookingHistoryItem
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    var bookingList by mutableStateOf<List<BookingHistoryItem>>(emptyList())
    var isLoading by mutableStateOf(true)

    // Database Read (Fetch History)
    fun fetchHistory() {
        viewModelScope.launch {
            try {
                isLoading = true
                val user = SupabaseClient.client.auth.currentUserOrNull()
                if (user != null) {
                    // Fetch bookings user ini, join dengan tabel rooms untuk ambil nama ruangan
                    val result = SupabaseClient.client.from("bookings")
                        .select(columns = Columns.raw("*, rooms(name)")) {
                            filter {
                                eq("user_id", user.id)
                            }
                            order("created_at", order = Order.DESCENDING)
                        }
                        .decodeList<BookingHistoryItem>()
                    bookingList = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}