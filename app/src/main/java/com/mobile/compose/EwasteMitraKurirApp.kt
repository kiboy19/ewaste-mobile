package com.mobile.compose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EwasteMitraKurirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Anda bisa menambahkan inisialisasi lain di sini jika diperlukan
    }
}