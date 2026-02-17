package com.gitfast.app.di

import android.content.Context
import androidx.room.Room
import com.gitfast.app.data.local.GitFastDatabase
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.local.migrations.MIGRATION_1_2
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
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
    fun provideDatabase(@ApplicationContext context: Context): GitFastDatabase {
        return Room.databaseBuilder(
            context,
            GitFastDatabase::class.java,
            "gitfast-database"
        ).addMigrations(MIGRATION_1_2)
         .build()
    }

    @Provides
    fun provideWorkoutDao(database: GitFastDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(workoutDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutSaveManager(workoutDao: WorkoutDao): WorkoutSaveManager {
        return WorkoutSaveManager(workoutDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutStateStore(@ApplicationContext context: Context): WorkoutStateStore {
        return WorkoutStateStore(context)
    }
}
