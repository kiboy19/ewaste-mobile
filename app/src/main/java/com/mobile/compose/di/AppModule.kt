package com.mobile.compose.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.mobile.compose.data.local.AppDatabase
import com.mobile.compose.data.local.dao.MitraDao
import com.mobile.compose.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ewaste_database"
        ).fallbackToDestructiveMigration() // Hati-hati dengan ini di produksi!
            .build()
    }

    @Singleton
    @Provides
    fun provideMitraDao(database: AppDatabase): MitraDao {
        return database.mitraDao()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.PREFS_TOKEN_FILE, Context.MODE_PRIVATE)
    }
}