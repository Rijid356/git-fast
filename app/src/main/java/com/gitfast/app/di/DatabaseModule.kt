package com.gitfast.app.di

import android.content.Context
import androidx.room.Room
import com.gitfast.app.data.local.GitFastDatabase
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.repository.WorkoutRepository
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
        ).build()
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
}
