package com.mobile.compose.data.remote.models


import com.mobile.compose.data.local.entity.MitraEntity // Import MitraEntity
import com.google.gson.annotations.SerializedName

// Data Mitra yang diterima dari API (DTO)
data class MitraDto(
    @SerializedName("id_mitra") val idMitra: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("no_telp") val noTelp: String,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("foto") val foto: String?,
    @SerializedName("rekening_bank") val rekeningBank: String?,
    @SerializedName("tanggal_lahir") val tanggalLahir: String?, // Akan diubah ke Date di Room Entity jika perlu
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
            tanggalLahir = this.tanggalLahir,
            isVerified = this.isVerified
        )
    }
}