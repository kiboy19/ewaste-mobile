package com.mobile.compose.viewmodel



import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.compose.data.remote.models.DocumentDto
import com.mobile.compose.data.remote.models.UploadDocumentResponse
import com.mobile.compose.data.repository.MitraRepository
import com.mobile.compose.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: MitraRepository
) : ViewModel() {

    // --- State untuk unggah dokumen ---
    private val _uploadDocumentState = MutableStateFlow<Resource<UploadDocumentResponse>>(Resource.Loading())
    val uploadDocumentState: StateFlow<Resource<UploadDocumentResponse>> = _uploadDocumentState

    // --- State untuk daftar dokumen ---
    private val _documentsState = MutableStateFlow<Resource<List<DocumentDto>>>(Resource.Loading())
    val documentsState: StateFlow<Resource<List<DocumentDto>>> = _documentsState

    init {
        getUploadedDocuments() // Muat daftar dokumen saat ViewModel diinisialisasi
    }

    fun uploadDocument(jenisDokumen: String, fileUri: Uri, context: Context) {
        viewModelScope.launch {
            repository.uploadDocument(jenisDokumen, fileUri, context).collect {
                _uploadDocumentState.value = it
            }
        }
    }

    fun getUploadedDocuments() {
        viewModelScope.launch {
            repository.getUploadedDocuments().collect {
                _documentsState.value = it
            }
        }
    }

    fun resetUploadDocumentState() { _uploadDocumentState.value = Resource.Loading() }
}