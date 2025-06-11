package com.mobile.compose.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {

    // State yang menyimpan nama user, default-nya "Android"
    private val _name = MutableStateFlow("Android")
    val name: StateFlow<String> = _name

    // Fungsi untuk mengubah nama user
    fun setName(newName: String) {
        _name.value = newName
    }
}
