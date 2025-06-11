package com.mobile.compose.utils

object Constants {
    const val BASE_URL = "http://10.0.2.2:8000/api/" // Untuk emulator, 10.0.2.2 adalah localhost
    // Jika Anda menggunakan perangkat fisik dan server Laravel di komputer Anda,
    // ganti dengan IP Address komputer Anda, contoh: "http://192.168.1.100:8000/api/"

    const val PREFS_TOKEN_FILE = "prefs_token_file"
    const val USER_TOKEN = "user_token"
    const val USER_EMAIL = "user_email" // Digunakan untuk verifikasi OTP dan lupa password
    const val USER_ID = "user_id" // Mungkin berguna untuk menyimpan ID user
}