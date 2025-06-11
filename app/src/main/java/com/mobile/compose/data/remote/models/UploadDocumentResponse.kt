package com.mobile.compose.data.remote.models

import com.google.gson.annotations.SerializedName

data class UploadDocumentResponse(
    @SerializedName("message") val message: String,
    @SerializedName("dokumen") val dokumen: DocumentDto
)

// Data dokumen yang diterima dari API (DTO)
data class DocumentDto(
    @SerializedName("id_dokumen") val idDokumen: Int,
    @SerializedName("id_mitra") val idMitra: Int,
    @SerializedName("jenis_dokumen") val jenisDokumen: String,
    @SerializedName("file_path") val filePath: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)