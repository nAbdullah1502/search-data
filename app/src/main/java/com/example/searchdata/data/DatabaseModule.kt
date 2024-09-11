package com.example.searchdata.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDrugDatabase(@ApplicationContext context: Context): DrugDatabase {
        return Room.databaseBuilder(
            context,
            DrugDatabase::class.java,
            "drug_database"
        ).build()
    }

    @Provides
    fun provideDrugDao(database: DrugDatabase): DrugDao {
        return database.drugDao()
    }
}
