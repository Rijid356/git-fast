package com.gitfast.app.di

import android.content.Context
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.location.GpsTracker
import com.gitfast.app.location.StepTracker
import com.gitfast.app.service.AutoPauseDetector
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
    fun provideStepTracker(@ApplicationContext context: Context): StepTracker {
        return StepTracker(context)
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

    @Provides
    @Singleton
    fun provideAutoPauseDetector(): AutoPauseDetector {
        return AutoPauseDetector()
    }

    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore {
        return SettingsStore(context)
    }
}
