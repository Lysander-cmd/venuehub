package com.example.venuehub.model

import kotlinx.serialization.Serializable

/**
 * Model untuk mengambil informasi Ruangan
 */
@Serializable
data class RoomInfo(
    val name: String
)

/**
 * Model lengkap Ruangan untuk form booking
 */
@Serializable
data class RoomItemData(
    val id: Long,
    val name: String,
    val capacity: Int,
    val imageUrl: String
)

/**
 * Model untuk mengirim data booking baru ke Supabase
 */
@Serializable
data class BookingRequest(
    val room_id: Long,
    val user_id: String,
    val event_name: String,
    val start_time: String,
    val end_time: String,
//    val ktm_url: String,
    val ktm_url: String? = null,
    val status: String = "pending"
)

/**
 * Model untuk menampilkan item di daftar Riwayat
 */
@Serializable
data class BookingHistoryItem(
    val id: Long,
    val event_name: String,
    val start_time: String,
    val status: String,
    val rooms: RoomInfo? = null // Join Table
)

/**
 * Model untuk menampilkan detail lengkap peminjaman
 */
@Serializable
data class BookingDetailPending(
    val id: Long,
    val event_name: String,
    val start_time: String,
    val end_time: String,
    val status: String,
//    val ktm_url: String,
    val rooms: RoomInfo? = null
)