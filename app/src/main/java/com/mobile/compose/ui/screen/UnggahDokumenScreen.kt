package com.mobile.compose.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.mobile.compose.R
import com.mobile.compose.utils.Resource
import com.mobile.compose.viewmodel.DocumentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnggahDokumenScreen(
    viewModel: DocumentViewModel,
    onBack: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current

    var selectedDocumentType by remember { mutableStateOf("KTP") }
    var selectedFileUri: Uri? by remember { mutableStateOf(null) }
    var fileName by remember { mutableStateOf("Tidak ada file dipilih") }

    val uploadDocumentState by viewModel.uploadDocumentState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Untuk snackbar

    // Launcher untuk memilih file dari perangkat
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            // Mendapatkan nama file dari URI
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst()) {
                    fileName = it.getString(nameIndex)
                } else {
                    fileName = uri.lastPathSegment ?: "File tidak dikenal"
                }
            } ?: run {
                fileName = uri.lastPathSegment ?: "File tidak dikenal"
            }
        } else {
            fileName = "Tidak ada file dipilih"
        }
    }

    // Efek untuk menangani hasil upload dokumen
    LaunchedEffect(uploadDocumentState) {
        when (uploadDocumentState) {
            is Resource.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Dokumen berhasil diunggah!", withDismissAction = true)
                }
                onUploadSuccess()
                viewModel.resetUploadDocumentState()
                // Reset UI after success
                selectedFileUri = null
                fileName = "Tidak ada file dipilih"
            }
            is Resource.Error -> {
                val errorMessage = uploadDocumentState.message ?: "Terjadi kesalahan saat mengunggah dokumen"
                scope.launch {
                    snackbarHostState.showSnackbar("Unggah Dokumen Gagal: $errorMessage", withDismissAction = true)
                }
                viewModel.resetUploadDocumentState()
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
                    .padding(24.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Unggah Dokumen",
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dropdown untuk memilih jenis dokumen
                Text("Jenis Dokumen", fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) } // State untuk dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedDocumentType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("KTP") },
                            onClick = {
                                selectedDocumentType = "KTP"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("KK") },
                            onClick = {
                                selectedDocumentType = "KK"
                                expanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Pilih File Dokumen", fontSize = 16.sp, color = Color.Black)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                        .clickable { pickFileLauncher.launch("*/*") }, // Membuka file picker untuk semua tipe
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_upload),
                            contentDescription = "Upload Dokumen",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(fileName, color = Color.Black, fontSize = 12.sp) // Tampilkan nama file
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = {
                        if (selectedFileUri != null) {
                            viewModel.uploadDocument(selectedDocumentType, selectedFileUri!!, context)
                        } else {
                            scope.launch { // Gunakan scope lokal
                                snackbarHostState.showSnackbar("Silakan pilih file dokumen terlebih dahulu.", withDismissAction = true)
                            }
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
                    enabled = uploadDocumentState !is Resource.Loading
                ) {
                    if (uploadDocumentState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("UPLOAD")
                    }
                }
            }
        }
    }
}
