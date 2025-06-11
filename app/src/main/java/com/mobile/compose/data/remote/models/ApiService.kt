package com.mobile.compose.data.remote.models

import com.mobile.compose.data.remote.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Autentikasi & Registrasi ---

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerificationRequest): Response<MessageResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @POST("change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    // --- Profil & Dokumen ---

    @GET("profile")
    suspend fun getProfile(): Response<MitraDto> // MitraDto langsung karena responsnya adalah object mitra

    @POST("profile/update")
    @Multipart // Untuk mengunggah foto profil
    suspend fun updateProfile(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>, // Untuk data non-file
        @Part foto: MultipartBody.Part? = null // Untuk file foto (optional)
    ): Response<MitraDto> // MitraDto karena responsnya adalah object mitra yang diperbarui

    @POST("upload-document")
    @Multipart // Untuk mengunggah dokumen
    suspend fun uploadDocument(
        @Part("jenis_dokumen") jenisDokumen: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<UploadDocumentResponse>

    @GET("documents")
    suspend fun getUploadedDocuments(): Response<List<DocumentDto>> // Responsnya array/list dokumen

    @POST("logout")
    suspend fun logout(): Response<MessageResponse>
}