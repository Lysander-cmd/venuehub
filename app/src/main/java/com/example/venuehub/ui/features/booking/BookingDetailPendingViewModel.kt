package com.example.venuehub.ui.features.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venuehub.model.BookingDetailPending
import com.kelompok.venuehub.data.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class BookingDetailPendingViewModel : ViewModel() {
    var booking by mutableStateOf<BookingDetailPending?>(null)
    var isLoading by mutableStateOf(true)

    fun fetchBookingDetail(bookingId: Long) {
        viewModelScope.launch {
            try {
                isLoading = true
                booking = SupabaseClient.client
                    .from("bookings")
                    .select(
//                    Columns.raw(
//                        "id,event_name,start_time,end_time,status,ktm_url,rooms(name)"
//                    )
                        Columns.raw(
                            "id,event_name,start_time,end_time,status,rooms(name)"
                        )
                    ) {
                        filter { eq("id", bookingId) }
                    }
                    .decodeSingle()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}