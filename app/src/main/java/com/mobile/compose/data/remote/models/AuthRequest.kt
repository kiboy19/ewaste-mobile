package com.mobile.compose.data.remote.models

import com.google.gson.annotations.SerializedName

// Untuk registrasi
data class RegisterRequest(
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("no_telp") val noTelp: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

// Untuk verifikasi OTP setelah registrasi, dan lupa password step 2 (reset password)
data class OtpVerificationRequest(
    @SerializedName("email") val email: String, // Untuk registrasi verifikasi
    @SerializedName("email_or_phone") val emailOrPhone: String?, // Untuk lupa password
    @SerializedName("otp") val otp: String
)

// Untuk login
data class LoginRequest(
    @SerializedName("identifier") val identifier: String, // Bisa email atau no_telp
    @SerializedName("password") val password: String
)

// Untuk lupa password step 1 (mengirim OTP)
data class ForgotPasswordRequest(
    @SerializedName("email_or_phone") val emailOrPhone: String // Bisa email atau no_telp
)

// Untuk reset password (setelah OTP valid)
data class ResetPasswordRequest(
    @SerializedName("email_or_phone") val emailOrPhone: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)

// Untuk ubah password (saat sudah login)
data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)
