package com.mobile.compose.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.compose.R
import com.mobile.compose.viewmodel.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import coil.compose.rememberAsyncImagePainter
import com.mobile.compose.utils.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoard(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onUnggahDokumen: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Untuk meluncurkan coroutine Snackbar

    // Memuat profil saat screen pertama kali di-compose atau saat ViewModel diinisialisasi
    // ViewModel sudah memanggil getProfile() di init{}
    // LaunchedEffect(Unit) { viewModel.getProfile() } // Tidak perlu lagi jika sudah di init{}

    LaunchedEffect(profileState) {
        when (profileState) {
            is Resource.Error -> {
                val errorMessage = profileState.message ?: "Gagal memuat data profil."
                snackbarHostState.showSnackbar(errorMessage, withDismissAction = true)
            }
            else -> {
                // Do nothing for Loading or Success, data already handled
            }
        }
    }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Logout Berhasil!", withDismissAction = true)
                onLogout() // Panggil navigasi setelah logout sukses
                viewModel.resetLogoutState()
            }
            is Resource.Error -> {
                val errorMessage = logoutState.message ?: "Terjadi kesalahan saat logout"
                snackbarHostState.showSnackbar("Logout Gagal: $errorMessage", withDismissAction = true)
                viewModel.resetLogoutState()
            }
            is Resource.Loading -> { /* Indikator loading ada di tombol */ }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Tampilkan loading/error untuk foto profil
            val profileImageUrl = (profileState as? Resource.Success)?.data?.foto
            val isLoadingProfile = profileState is Resource.Loading

            if (isLoadingProfile) {
                CircularProgressIndicator(
                    modifier = Modifier.size(120.dp),
                    color = Color.White
                )
            } else {
                Image(
                    painter = if (!profileImageUrl.isNullOrEmpty()) {
                        rememberAsyncImagePainter(model = "http://10.0.2.2:8000/storage/$profileImageUrl") // Ganti URL base storage Laravel
                    } else {
                        painterResource(id = R.drawable.profile_placeholder) // Gambar default jika tidak ada
                    },
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tampilkan nama pengguna
            val userName = (profileState as? Resource.Success)?.data?.nama
            if (isLoadingProfile) {
                Text(text = "Memuat Nama...", fontSize = 20.sp, color = Color.White)
            } else {
                Text(text = userName ?: "Nama Tidak Tersedia", fontSize = 20.sp, color = Color.White)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onEditProfile() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text("Edit Profile")
                }

                Button(
                    onClick = {
                        viewModel.logout() // Panggil fungsi logout dari ViewModel
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                    enabled = logoutState !is Resource.Loading // Disable saat loading
                ) {
                    if (logoutState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Keluar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(Color.White)
                    .padding(top = 64.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable { onUnggahDokumen() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_upload),
                            contentDescription = "Unggah",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unggah Dokumen",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
