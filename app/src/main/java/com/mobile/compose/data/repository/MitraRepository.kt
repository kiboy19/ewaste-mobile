package com.mobile.compose.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.mobile.compose.data.local.dao.MitraDao
import com.mobile.compose.data.local.entity.MitraEntity
import com.mobile.compose.data.remote.models.ApiService
import com.mobile.compose.data.remote.models.*
import com.mobile.compose.utils.Constants
import com.mobile.compose.utils.Resource
import com.google.gson.Gson
import com.mobile.compose.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MitraRepository @Inject constructor(
    private val apiService: ApiService,
    private val mitraDao: MitraDao,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson // Inject Gson untuk parsing error body
) {

    // --- Autentikasi & Registrasi ---

    suspend fun register(request: RegisterRequest): Flow<Resource<RegisterResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.register(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Terjadi kesalahan registrasi"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    suspend fun verifyOtp(request: OtpVerificationRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.verifyOtp(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    // Update isVerified status di Room setelah verifikasi sukses
                    mitraDao.getLoggedInMitra().collect { mitra ->
                        if (mitra != null) {
                            mitraDao.updateMitra(mitra.copy(isVerified = true))
                        }
                    }
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Verifikasi OTP gagal."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    suspend fun login(request: LoginRequest): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Simpan token dan data mitra ke SharedPreferences dan Room
                    sharedPreferences.edit().apply {
                        putString(Constants.USER_TOKEN, loginResponse.accessToken)
                        putString(Constants.USER_EMAIL, loginResponse.mitra.email) // Simpan email
                        putInt(Constants.USER_ID, loginResponse.mitra.idMitra) // Simpan ID
                        apply()
                    }
                    mitraDao.insertMitra(loginResponse.mitra.toMitraEntity()) // Simpan ke Room
                    emit(Resource.Success(loginResponse))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Login gagal."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.forgotPassword(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    // Simpan email yang digunakan untuk lupa password
                    sharedPreferences.edit().putString(Constants.USER_EMAIL, request.emailOrPhone).apply()
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Permintaan reset password gagal."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.resetPassword(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    // Bersihkan email yang disimpan setelah reset password berhasil
                    sharedPreferences.edit().remove(Constants.USER_EMAIL).apply()
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Reset password gagal."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.changePassword(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Gagal mengubah password."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    // --- Profil & Dokumen ---

    fun getProfile(): Flow<Resource<MitraEntity>> = flow {
        emit(Resource.Loading(mitraDao.getLoggedInMitra().asLiveData().value)) // Emit data dari Room dulu
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                response.body()?.let { mitraDto ->
                    val mitraEntity = mitraDto.toMitraEntity()
                    mitraDao.insertMitra(mitraEntity) // Update data di Room
                    emit(Resource.Success(mitraEntity))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Gagal mengambil profil."
                emit(Resource.Error(errorMessage, mitraDao.getLoggedInMitra().asLiveData().value))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}", mitraDao.getLoggedInMitra().asLiveData().value))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda.", mitraDao.getLoggedInMitra().asLiveData().value))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}", mitraDao.getLoggedInMitra().asLiveData().value))
        }
    }

    // Fungsi tambahan untuk mendapatkan profil dari Room saja (misal untuk menampilkan cepat)
    fun getLocalProfile(): Flow<MitraEntity?> {
        return mitraDao.getLoggedInMitra()
    }


    // Untuk update profile
    suspend fun updateProfile(
        nama: String?,
        alamat: String?,
        tanggalLahir: String?, // Sesuaikan dengan format API
        rekeningBank: String?,
        fotoUri: Uri?, // Menggunakan URI dari perangkat
        context: Context // Context dibutuhkan untuk mengambil file dari URI
    ): Flow<Resource<MitraEntity>> = flow {
        emit(Resource.Loading())

        val params = mutableMapOf<String, RequestBody>()
        nama?.let { params["nama"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
        alamat?.let { params["alamat"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
        tanggalLahir?.let { params["tanggal_lahir"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }
        rekeningBank?.let { params["rekening_bank"] = it.toRequestBody("text/plain".toMediaTypeOrNull()) }

        var fotoPart: MultipartBody.Part? = null
        fotoUri?.let { uri ->
            // Mengambil File dari URI
            val file = getFileFromUri(context, uri)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)
            } else {
                emit(Resource.Error("Gagal memproses file foto dari URI."))
                return@flow
            }
        }

        try {
            val response = apiService.updateProfile(params, fotoPart)
            if (response.isSuccessful) {
                response.body()?.let { mitraDto ->
                    val mitraEntity = mitraDto.toMitraEntity()
                    mitraDao.updateMitra(mitraEntity) // Update data di Room
                    emit(Resource.Success(mitraEntity))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Gagal memperbarui profil."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    // Fungsi helper untuk mendapatkan File dari Uri
    // (Anda mungkin perlu menambahkan fungsi ini di Utils atau di sini)
    // Implementasi lebih lengkap ada di langkah selanjutnya
//    private fun getFileFromUri(context: Context, uri: Uri): File? {
//
//
////        val contentResolver = context.contentResolver
////        val tempFile = File(context.cacheDir, "temp_upload_file")
////        tempFile.createNewFile()
////
////        try {
////            contentResolver.openInputStream(uri)?.use { inputStream ->
////                tempFile.outputStream().use { outputStream ->
////                    inputStream.copyTo(outputStream)
////                }
////            }
////            return tempFile
////        } catch (e: Exception) {
////            e.printStackTrace()
////            return null
//        }
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return FileUtils.uriToFile(context, uri)
    }
    }


    suspend fun uploadDocument(
        jenisDokumen: String,
        fileUri: Uri, // Menggunakan URI dari perangkat
        context: Context // Context dibutuhkan untuk mengambil file dari URI
    ): Flow<Resource<UploadDocumentResponse>> = flow {
        emit(Resource.Loading())

        val file = getFileFromUri(context, fileUri)
        if (file == null) {
            emit(Resource.Error("Gagal memproses file dokumen dari URI."))
            return@flow
        }

        val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val jenisDokumenPart = jenisDokumen.toRequestBody("text/plain".toMediaTypeOrNull())

        try {
            val response = apiService.uploadDocument(jenisDokumenPart, filePart)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Gagal mengunggah dokumen."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        } finally {
            file.delete() // Hapus file sementara setelah diunggah
        }
    }

    fun getUploadedDocuments(): Flow<Resource<List<DocumentDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getUploadedDocuments()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Respons kosong dari server"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Gagal mengambil daftar dokumen."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    suspend fun logout(): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                // Hapus token dan data mitra dari SharedPreferences dan Room
                sharedPreferences.edit().apply {
                    remove(Constants.USER_TOKEN)
                    remove(Constants.USER_EMAIL)
                    remove(Constants.USER_ID)
                    apply()
                }
                mitraDao.clearMitraData()
                emit(Resource.Success(response.body() ?: MessageResponse("Logout berhasil.")))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: "Gagal logout."
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Kesalahan jaringan. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error("Terjadi kesalahan tak terduga: ${e.message}"))
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(Constants.USER_TOKEN, null)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(Constants.USER_EMAIL, null)
    }
}