//loginscreen

package com.mobile.compose.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.compose.R
import androidx.compose.material3.TextFieldDefaults
import com.mobile.compose.data.remote.models.LoginRequest
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.AuthViewModel
import kotlinx.coroutines.launch // Tambahkan import ini

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Tambahkan CoroutineScope untuk Snackbar

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Login Berhasil!", withDismissAction = true)
                onLoginSuccess() // Panggil navigasi hanya saat sukses
                viewModel.resetLoginState()
            }
            is Resource.Error -> {
                val errorMessage = loginState.message ?: "Terjadi kesalahan yang tidak diketahui"
                snackbarHostState.showSnackbar("Login Gagal: $errorMessage", withDismissAction = true)
                viewModel.resetLoginState()
            }
            is Resource.Loading -> {
                // Tampilkan loading indicator di tombol, tidak perlu disini
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEFEF))
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF202020)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.scooter),
                    contentDescription = "Scooter",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Box(
                modifier = Modifier
                    .offset(y = (-40).dp)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Login", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("Sign in to continue.", fontSize = 14.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        label = { Text("Email atau Nomor Telepon") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (identifier.isNotBlank() && password.isNotBlank()) {
                                viewModel.login(LoginRequest(identifier, password))
                            } else {
                                scope.launch { // Gunakan scope lokal untuk Snackbar
                                    snackbarHostState.showSnackbar("Email/Nomor Telepon dan Password tidak boleh kosong.", withDismissAction = true)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        enabled = loginState !is Resource.Loading // Disable tombol saat loading
                    ) {
                        if (loginState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Log in")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateToForgotPassword) {
                        Text("Forgot Password?", color = Color.Gray, fontSize = 12.sp)
                    }

                    Text(
                        "Signup !",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable {
                                onNavigateToRegister()
                            }
                    )
                }
            }
        }
    }
}}
