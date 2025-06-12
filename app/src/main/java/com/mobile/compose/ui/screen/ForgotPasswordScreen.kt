package com.mobile.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel, // ✅ Tambah parameter ViewModel
    onBackToLogin: () -> Unit,
    onSubmitSuccess: () -> Unit // ✅ Ganti onSubmit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val resetPasswordState by viewModel.resetPasswordState.collectAsState() // ✅ Amati state reset password
    val snackbarHostState = remember { SnackbarHostState() }
    val emailForReset by viewModel.currentEmailForOtp.collectAsState() // Ambil email yang disimpan saat forgot password

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Password berhasil direset! Silakan login.", withDismissAction = true)
                onSubmitSuccess()
                viewModel.resetResetPasswordState()
            }
            is Resource.Error -> {
                val errorMessage = resetPasswordState.message ?: "Terjadi kesalahan yang tidak diketahui"
                snackbarHostState.showSnackbar("Reset Password Gagal: $errorMessage", withDismissAction = true)
                viewModel.resetResetPasswordState()
            }
            is Resource.Loading -> { /* Handle loading */ }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tombol X
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onBackToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "RESET PASSWORD",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("NEW PASSWORD") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("CONFIRM PASSWORD") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        // Panggil fungsi reset password dari ViewModel
                        if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && emailForReset != null) {
                            if (newPassword == confirmPassword) {
                                viewModel.resetPassword(emailForReset, viewModel.currentEmailForOtp.value ?: "", newPassword, confirmPassword) // Email OTP dari vm
                            } else {
                                viewModelScope.launch { snackbarHostState.showSnackbar("Password tidak cocok.", withDismissAction = true) }
                            }
                        } else {
                            viewModelScope.launch { snackbarHostState.showSnackbar("Semua field harus diisi.", withDismissAction = true) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    enabled = resetPasswordState !is Resource.Loading
                ) {
                    if (resetPasswordState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SUBMIT")
                    }
                }
            }
        }
    }
}
