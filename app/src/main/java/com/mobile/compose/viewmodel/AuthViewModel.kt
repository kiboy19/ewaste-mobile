*view model*
package com.mobile.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.compose.data.remote.models.*
import com.mobile.compose.data.repository.MitraRepository
import com.mobile.compose.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: MitraRepository
) : ViewModel() {

    // --- State untuk Registrasi ---
    private val _registerState = MutableStateFlow<Resource<RegisterResponse>>(Resource.Loading())
    val registerState: StateFlow<Resource<RegisterResponse>> = _registerState

    // --- State untuk Verifikasi OTP (Registrasi atau Lupa Password) ---
    private val _otpVerificationState = MutableStateFlow<Resource<MessageResponse>>(Resource.Loading())
    val otpVerificationState: StateFlow<Resource<MessageResponse>> = _otpVerificationState

    // --- State untuk Login ---
    private val _loginState = MutableStateFlow<Resource<LoginResponse>>(Resource.Loading())
    val loginState: StateFlow<Resource<LoginResponse>> = _loginState

    // --- State untuk Lupa Password (Kirim OTP) ---
    private val _forgotPasswordState = MutableStateFlow<Resource<MessageResponse>>(Resource.Loading())
    val forgotPasswordState: StateFlow<Resource<MessageResponse>> = _forgotPasswordState

    // --- State untuk Reset Password ---
    private val _resetPasswordState = MutableStateFlow<Resource<MessageResponse>>(Resource.Loading())
    val resetPasswordState: StateFlow<Resource<MessageResponse>> = _resetPasswordState

    // --- State untuk Ubah Password ---
    private val _changePasswordState = MutableStateFlow<Resource<MessageResponse>>(Resource.Loading())
    val changePasswordState: StateFlow<Resource<MessageResponse>> = _changePasswordState

    // --- Data yang tersimpan di SharedPreferences (untuk OTP dan Reset Password) ---
    private val _currentEmailForOtp = MutableStateFlow<String?>(null)
    val currentEmailForOtp: StateFlow<String?> = _currentEmailForOtp

    init {
        // Inisialisasi email yang tersimpan jika ada (misal setelah registrasi atau forgot password)
        _currentEmailForOtp.value = repository.getEmail()
    }

    // --- Fungsi API Calls ---

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            repository.register(request).collect {
                _registerState.value = it
                if (it is Resource.Success) {
                    // Simpan email di SharedPreferences untuk verifikasi OTP
                    repository.sharedPreferences.edit().putString(Constants.USER_EMAIL, request.email).apply()
                    _currentEmailForOtp.value = request.email
                }
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            val request = OtpVerificationRequest(email = email, emailOrPhone = null, otp = otp)
            repository.verifyOtp(request).collect {
                _otpVerificationState.value = it
                if (it is Resource.Success) {
                    // Bersihkan email yang disimpan setelah verifikasi berhasil
                    repository.sharedPreferences.edit().remove(Constants.USER_EMAIL).apply()
                    _currentEmailForOtp.value = null
                }
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            repository.login(request).collect {
                _loginState.value = it
            }
        }
    }

    fun forgotPassword(emailOrPhone: String) {
        viewModelScope.launch {
            val request = ForgotPasswordRequest(emailOrPhone)
            repository.forgotPassword(request).collect {
                _forgotPasswordState.value = it
            }
        }
    }

    fun resetPassword(emailOrPhone: String, otp: String, newPassword: String, newPasswordConfirmation: String) {
        viewModelScope.launch {
            val request = ResetPasswordRequest(emailOrPhone, otp, newPassword, newPasswordConfirmation)
            repository.resetPassword(request).collect {
                _resetPasswordState.value = it
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String) {
        viewModelScope.launch {
            val request = ChangePasswordRequest(oldPassword, newPassword, newPasswordConfirmation)
            repository.changePassword(request).collect {
                _changePasswordState.value = it
            }
        }
    }

    // Fungsi untuk mereset state setelah digunakan
    fun resetRegisterState() { _registerState.value = Resource.Loading() }
    fun resetOtpVerificationState() { _otpVerificationState.value = Resource.Loading() }
    fun resetLoginState() { _loginState.value = Resource.Loading() }
    fun resetForgotPasswordState() { _forgotPasswordState.value = Resource.Loading() }
    fun resetResetPasswordState() { _resetPasswordState.value = Resource.Loading() }
    fun resetChangePasswordState() { _changePasswordState.value = Resource.Loading() }
}
