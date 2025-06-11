package com.mobile.compose.data.remote.models

import com.ewaste.mitrakurir.data.local.entity.MitraEntity // Akan kita buat nanti
import com.google.gson.annotations.SerializedName

// Respons umum untuk pesan sukses
data class MessageResponse(
    @SerializedName("message") val message: String
)

// Respons untuk registrasi
data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("mitra_id") val mitraId: Int? // Opsional, tergantung API
)

// Respons untuk login
data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("mitra") val mitra: MitraDto // Menggunakan MitraDto yang lebih lengkap
)

// Data Mitra yang diterima dari API (DTO)
data class MitraDto(
    @SerializedName("id_mitra") val idMitra: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("no_telp") val noTelp: String,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("foto") val foto: String?,
    @SerializedName("rekening_bank") val rekeningBank: String?,
    @SerializedName("tanggal_lahir") val tanggalLahir: String?, // Akan diubah ke Date di Room Entity
    @SerializedName("is_verified") val isVerified: Boolean
) {
    // Fungsi untuk mengonversi MitraDto ke MitraEntity (untuk Room)
    fun toMitraEntity(): MitraEntity {
        return MitraEntity(
            idMitra = this.idMitra,
            nama = this.nama,
            email = this.email,
            noTelp = this.noTelp,
            alamat = this.alamat,
            foto = this.foto,
            rekeningBank = this.rekeningBank,
            tanggalLahir = this.tanggalLahir, // Tetap string dulu, konversi di DAO jika perlu
            isVerified = this.isVerified
        )
    }
}
