package com.example.sunshine.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.example.sunshine.db.SunshineDatabase
import com.example.sunshine.utils.Constants
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
    fun provideSunshineDatabase(@ApplicationContext context: Context?): SunshineDatabase {
        return databaseBuilder(
            context!!,
            SunshineDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }
}