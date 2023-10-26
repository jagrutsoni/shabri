package com.sor.shabri.ui

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    fun providesFirebaseService(): FirebaseService {
        return FirebaseService()
    }
}