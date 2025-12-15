package com.example.venuehub.model

import kotlinx.serialization.Serializable

/**
 * Model untuk mengirim data checkout (checkouts table)
 */
@Serializable
data class CheckoutRequest(
    val booking_id: Long,
    val user_id: String,
    val notes: String,
    val clean_proof_url: String
)

/**
 * Model untuk data checkout yang diambil (untuk loading bukti)
 */
@Serializable
data class CheckoutData(
    val clean_proof_url: String? = null,
    val notes: String? = null
)

/**
 * Model khusus untuk history yang memuat data checkout (Supabase Join)
 * Menggabungkan info Booking + Room + Checkouts
 */
@Serializable
data class BookingWithCheckout(
    val id: Long,
    val event_name: String,
    val start_time: String,
    val status: String,
    val rooms: RoomInfo? = null,
    val checkouts: List<CheckoutData>? = null
)
