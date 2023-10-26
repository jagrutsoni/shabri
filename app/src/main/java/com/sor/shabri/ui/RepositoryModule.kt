package com.sor.shabri.ui

import com.sor.shabri.data.CaptionRepository
import com.sor.shabri.data.CaptionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun providesCaptionRepository(firebaseService: FirebaseService): CaptionRepository {
            return CaptionRepositoryImpl(firebaseService)
    }
}