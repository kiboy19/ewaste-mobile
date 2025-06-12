//ubahprofilescreen
package com.mobile.compose.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mobile.compose.R
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahProfilScreen(
    viewModel: ProfileViewModel,
    onClose: () -> Unit,
    onSimpanSuccess: () -> Unit
) {
    val context = LocalContext.current

    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var noTelp by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var rekeningBank by remember { mutableStateOf("") }
    var fotoUri: Uri? by remember { mutableStateOf(null) }
    var currentProfileImageUrl by remember { mutableStateOf<String?>(null) }

    val profileState by viewModel.profileState.collectAsState()
    val updateProfileState by viewModel.updateProfileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Untuk snackbar

    // Efek untuk mengisi field saat data profil dimuat
    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            val mitra = profileState.data
            mitra?.let {
                nama = it.nama
                email = it.email
                noTelp = it.noTelp
                alamat = it.alamat ?: ""
                tanggalLahir = it.tanggalLahir ?: "" // Format YYYY-MM-DD
                rekeningBank = it.rekeningBank ?: ""
                currentProfileImageUrl = it.foto
            }
        } else if (profileState is Resource.Error) {
            scope.launch {
                snackbarHostState.showSnackbar("Gagal memuat data profil: ${profileState.message}", withDismissAction = true)
            }
        }
    }

    // Efek untuk menangani hasil update profil
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is Resource.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Profil berhasil diperbarui!", withDismissAction = true)
                }
                onSimpanSuccess()
                viewModel.resetUpdateProfileState()
            }
            is Resource.Error -> {
                val errorMessage = updateProfileState.message ?: "Terjadi kesalahan saat memperbarui profil"
                scope.launch {
                    snackbarHostState.showSnackbar("Update Profil Gagal: $errorMessage", withDismissAction = true)
                }
                viewModel.resetUpdateProfileState()
            }
            is Resource.Loading -> { /* Handle loading */ }
        }
    }

    // Launcher untuk memilih foto dari galeri
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUri = uri
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
            ) {
                // Tombol close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onClose() },
                        tint = Color.Black
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tampilkan foto profil
                    val painter = if (fotoUri != null) {
                        rememberAsyncImagePainter(model = fotoUri)
                    } else if (!currentProfileImageUrl.isNullOrEmpty()) {
                        rememberAsyncImagePainter(model = "http://10.0.2.2:8000/storage/$currentProfileImageUrl") // Ganti base URL sesuai API
                    } else {
                        painterResource(id = R.drawable.profile_placeholder)
                    }

                    Image(
                        painter = painter,
                        contentDescription = "Foto Profil",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { pickImageLauncher.launch("image/*") } // Klik untuk ganti foto
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ubah Foto", fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    @Composable
                    fun Field(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
                        TextField(
                            value = value,
                            onValueChange = onValueChange,
                            placeholder = { Text(label) },
                            singleLine = true,
                            enabled = enabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFE0E0E0),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Field("NAMA", nama) { nama = it }
                    Field("EMAIL", email, enabled = false)
                    Field("NOMOR TELEPON", noTelp, enabled = false)
                    Field("ALAMAT", alamat) { alamat = it }
                    Field("TANGGAL LAHIR (YYYY-MM-DD)", tanggalLahir) { tanggalLahir = it }
                    Field("REKENING BANK", rekeningBank) { rekeningBank = it }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(
                    onClick = {
                        // Validasi minimal sebelum panggil API
                        if (nama.isNotBlank() && alamat.isNotBlank() && tanggalLahir.isNotBlank() && rekeningBank.isNotBlank()) {
                            viewModel.updateProfile(
                                nama = nama,
                                alamat = alamat,
                                tanggalLahir = tanggalLahir.ifEmpty { null },
                                rekeningBank = rekeningBank.ifEmpty { null },
                                fotoUri = fotoUri,
                                context = context
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Semua field harus diisi.", withDismissAction = true)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    enabled = updateProfileState !is Resource.Loading
                ) {
                    if (updateProfileState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIMPAN")
                    }
                }
            }
        }
    }
}
