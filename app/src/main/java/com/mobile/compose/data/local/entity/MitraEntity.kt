package com.mobile.compose.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobile.compose.data.remote.models.MitraDto // Opsional, untuk konversi

@Entity(tableName = "mitra_kurir")
data class MitraEntity(
    @PrimaryKey val idMitra: Int,
    val nama: String,
    val email: String,
    val noTelp: String,
    val alamat: String?,
    val foto: String?,
    val rekeningBank: String?,
    val tanggalLahir: String?, // Simpan sebagai String atau konversi ke LocalDate jika mau
    val isVerified: Boolean
)