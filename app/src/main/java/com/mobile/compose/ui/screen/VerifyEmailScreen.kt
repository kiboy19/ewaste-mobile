//verifyemailscreen
package com.mobile.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    viewModel: AuthViewModel, // ✅ Tambah parameter ViewModel
    onBackToLogin: () -> Unit,
    onLanjutToOtpEmail: (email: String) -> Unit // ✅ Ganti onLanjut
) {
    var email by remember { mutableStateOf("") }

    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState() // ✅ Amati state forgot password
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(forgotPasswordState) {
        when (forgotPasswordState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Kode verifikasi OTP telah dikirim ke email Anda.", withDismissAction = true)
                onLanjutToOtpEmail(email)
                viewModel.resetForgotPasswordState()
            }
            is Resource.Error -> {
                val errorMessage = forgotPasswordState.message ?: "Terjadi kesalahan yang tidak diketahui"
                snackbarHostState.showSnackbar("Gagal mengirim OTP: $errorMessage", withDismissAction = true)
                viewModel.resetForgotPasswordState()
            }
            is Resource.Loading -> { /* Handle loading */ }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masukkan Email Anda",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kami akan mengirimkan kode verifikasi ke email Anda.",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Alamat Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        viewModel.forgotPassword(email)
                    } else {
                        viewModelScope.launch {
                            snackbarHostState.showSnackbar("Alamat Email tidak boleh kosong.", withDismissAction = true)
                        }
                    }
                },
                enabled = email.isNotBlank() && forgotPasswordState !is Resource.Loading,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (forgotPasswordState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Lanjut")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Kembali ke Login")
            }
        }
    }
}
