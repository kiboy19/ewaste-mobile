package com.mobile.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPEmailScreen(
    viewModel: AuthViewModel, // ✅ Tambah parameter ViewModel
    email: String, // ✅ Menerima email sebagai argumen
    onVerified: () -> Unit
) {
    var otp by remember { mutableStateOf("") }

    val otpVerificationState by viewModel.otpVerificationState.collectAsState() // ✅ Amati state verifikasi OTP
    val snackbarHostState = remember { SnackbarHostState() }
    val currentEmailFromVm by viewModel.currentEmailForOtp.collectAsState() // Ambil email dari ViewModel

    LaunchedEffect(otpVerificationState) {
        when (otpVerificationState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Verifikasi OTP Berhasil!", withDismissAction = true)
                onVerified()
                viewModel.resetOtpVerificationState() // Reset state setelah berhasil
            }
            is Resource.Error -> {
                val errorMessage = otpVerificationState.message ?: "Terjadi kesalahan yang tidak diketahui"
                snackbarHostState.showSnackbar("Verifikasi OTP Gagal: $errorMessage", withDismissAction = true)
                viewModel.resetOtpVerificationState() // Reset state setelah error
            }
            is Resource.Loading -> {
                // Tampilkan loading indicator
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Verifikasi Email", fontSize = 28.sp, color = Color.Black) // Ganti judul
            Spacer(modifier = Modifier.height(8.dp))
            Text("Silahkan cek email anda ($email) untuk kode verifikasi.", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) { // Pastikan hanya angka dan max 6 digit
                        otp = it
                    }
                },
                label = { Text("Masukkan Kode OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Gunakan KeyboardType.Number
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Panggil fungsi verifikasi OTP dari ViewModel
                    val emailToVerify = currentEmailFromVm ?: email // Gunakan email dari ViewModel atau yang dilewatkan
                    if (emailToVerify.isNotBlank() && otp.length == 6) {
                        viewModel.verifyOtp(emailToVerify, otp)
                    } else {
                        viewModelScope.launch {
                            snackbarHostState.showSnackbar("Email dan OTP tidak boleh kosong atau OTP tidak valid.", withDismissAction = true)
                        }
                    }
                },
                enabled = otp.length == 6 && otpVerificationState !is Resource.Loading, // Enable tombol saat loading
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (otpVerificationState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Didn’t receive verification OTP?")
                Spacer(modifier = Modifier.width(4.dp))
                // Tombol "Resend again" bisa memanggil fungsi resend OTP di ViewModel jika API mendukungnya
                // Untuk saat ini, asumsikan API hanya kirim OTP saat register pertama kali
                Text("Resend again", color = Color(0xFF4B4FED), fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        // TODO: Implement resend OTP logic if API supports it
                        viewModelScope.launch {
                            snackbarHostState.showSnackbar("Fungsi Resend OTP belum diimplementasikan di backend.", withDismissAction = true)
                        }
                    })
            }
        }
    }
}
