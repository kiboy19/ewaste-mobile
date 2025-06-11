package com.mobile.compose.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.compose.data.local.entity.MitraEntity
import com.mobile.compose.data.remote.models.MessageResponse
import com.mobile.compose.data.repository.MitraRepository
import com.mobile.compose.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: MitraRepository
) : ViewModel() {

    // --- State untuk data profil ---
    private val _profileState = MutableStateFlow<Resource<MitraEntity>>(Resource.Loading())
    val profileState: StateFlow<Resource<MitraEntity>> = _profileState

    // --- State untuk update profil ---
    private val _updateProfileState = MutableStateFlow<Resource<MitraEntity>>(Resource.Loading())
    val updateProfileState: StateFlow<Resource<MitraEntity>> = _updateProfileState

    // --- State untuk Logout ---
    private val _logoutState = MutableStateFlow<Resource<MessageResponse>>(Resource.Loading())
    val logoutState: StateFlow<Resource<MessageResponse>> = _logoutState

    init {
        getProfile() // Muat profil saat ViewModel diinisialisasi
    }

    fun getProfile() {
        viewModelScope.launch {
            repository.getProfile().collect {
                _profileState.value = it
            }
        }
    }

    fun updateProfile(
        nama: String?,
        alamat: String?,
        tanggalLahir: String?,
        rekeningBank: String?,
        fotoUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            repository.updateProfile(
                nama,
                alamat,
                tanggalLahir,
                rekeningBank,
                fotoUri,
                context
            ).collect {
                _updateProfileState.value = it
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout().collect {
                _logoutState.value = it
            }
        }
    }

    fun resetUpdateProfileState() { _updateProfileState.value = Resource.Loading() }
    fun resetLogoutState() { _logoutState.value = Resource.Loading() }
}