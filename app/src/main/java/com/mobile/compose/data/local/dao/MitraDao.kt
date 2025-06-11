package com.mobile.compose.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobile.compose.data.local.entity.MitraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MitraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMitra(mitra: MitraEntity)

    @Update
    suspend fun updateMitra(mitra: MitraEntity)

    @Query("SELECT * FROM mitra_kurir WHERE idMitra = :idMitra LIMIT 1")
    fun getMitraById(idMitra: Int): Flow<MitraEntity?>

    @Query("SELECT * FROM mitra_kurir LIMIT 1") // Mengambil satu mitra (asumsi hanya ada satu yang login)
    fun getLoggedInMitra(): Flow<MitraEntity?>

    @Query("DELETE FROM mitra_kurir")
    suspend fun clearMitraData()
}