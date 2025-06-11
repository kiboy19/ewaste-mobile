package com.mobile.compose.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mobile.compose.data.local.dao.MitraDao
import com.mobile.compose.data.local.entity.MitraEntity

@Database(entities = [MitraEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mitraDao(): MitraDao
}