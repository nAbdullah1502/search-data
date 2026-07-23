package com.example.searchdata.di

import com.example.searchdata.data.Repository
import com.example.searchdata.data.DrugDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepository(drugDao: DrugDao): Repository {
        return Repository(drugDao)
    }
}
