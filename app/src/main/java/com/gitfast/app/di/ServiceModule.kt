package com.gitfast.app.di

import android.content.Context
import com.gitfast.app.location.GpsTracker
import com.gitfast.app.service.WorkoutStateManager
import com.gitfast.app.util.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideGpsTracker(@ApplicationContext context: Context): GpsTracker {
        return GpsTracker(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutStateManager(): WorkoutStateManager {
        return WorkoutStateManager()
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }
}
