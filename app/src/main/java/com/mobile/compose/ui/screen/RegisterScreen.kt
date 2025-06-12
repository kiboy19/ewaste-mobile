//registerscreen
package com.mobile.compose.ui.screen

// app/src/main/java/com/ewaste.mitrakurir/ui/screen/RegisterScreen.kt
package com.ewaste.mitrakurir.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.compose.R
import com.mobile.compose.data.remote.models.RegisterRequest
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onSignUpSuccess: (email: String) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var noTelepon by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Tambahkan CoroutineScope

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                val registeredEmail = email
                snackbarHostState.showSnackbar("Registrasi Berhasil! Cek email untuk verifikasi.", withDismissAction = true)
                onSignUpSuccess(registeredEmail) // Panggil navigasi hanya saat sukses
                viewModel.resetRegisterState()
            }
            is Resource.Error -> {
                val errorMessage = registerState.message ?: "Terjadi kesalahan yang tidak diketahui"
                snackbarHostState.showSnackbar("Registrasi Gagal: $errorMessage", withDismissAction = true)
                viewModel.resetRegisterState()
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
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF202020)),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Image(
                    painter = painterResource(id = R.drawable.scooter),
                    contentDescription = "Decoration",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-40).dp)
                    .background(Color.White, RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create new\nAccount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                RegisterTextField("NAMA", nama) { nama = it }
                RegisterTextField("EMAIL", email) { email = it }
                RegisterTextField("NOMOR TELEPON", noTelepon) { noTelepon = it }
                RegisterTextField("PASSWORD", password, isPassword = true) { password = it }
                RegisterTextField("KONFIRMASI PASSWORD", confirmPassword, isPassword = true) { confirmPassword = it }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sudah punya akun? Login")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (nama.isNotBlank() && email.isNotBlank() && noTelepon.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                            if (password == confirmPassword) {
                                viewModel.register(
                                    RegisterRequest(nama, email, noTelepon, password, confirmPassword)
                                )
                            } else {
                                scope.launch { // Gunakan scope lokal untuk Snackbar
                                    snackbarHostState.showSnackbar("Password dan Konfirmasi Password tidak cocok.", withDismissAction = true)
                                }
                            }
                        } else {
                            scope.launch { // Gunakan scope lokal untuk Snackbar
                                snackbarHostState.showSnackbar("Semua field harus diisi.", withDismissAction = true)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = registerState !is Resource.Loading
                ) {
                    if (registerState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign up")
                    }
                }
            }
        }
    }
}
// RegisterTextField tetap sama
@Composable
fun RegisterTextField(
    label: String,
    value: String,
    placeholder: String = "",
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD6D6D6), shape = RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFE0E0E0),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
