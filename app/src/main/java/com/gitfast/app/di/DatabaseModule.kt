package com.gitfast.app.di

import android.content.Context
import androidx.room.Room
import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.local.BodyCompDao
import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.GitFastDatabase
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.local.migrations.MIGRATION_1_2
import com.gitfast.app.data.local.migrations.MIGRATION_2_3
import com.gitfast.app.data.local.migrations.MIGRATION_3_4
import com.gitfast.app.data.local.migrations.MIGRATION_4_5
import com.gitfast.app.data.local.migrations.MIGRATION_5_6
import com.gitfast.app.data.local.migrations.MIGRATION_6_7
import com.gitfast.app.data.local.migrations.MIGRATION_7_8
import com.gitfast.app.data.local.migrations.MIGRATION_8_9
import com.gitfast.app.data.local.migrations.MIGRATION_9_10
import com.gitfast.app.data.local.migrations.MIGRATION_10_11
import com.gitfast.app.data.local.migrations.MIGRATION_11_12
import com.gitfast.app.data.local.migrations.MIGRATION_12_13
import com.gitfast.app.data.local.migrations.MIGRATION_13_14
import com.gitfast.app.data.local.ExerciseDao
import com.gitfast.app.data.local.LapStartPointDao
import com.gitfast.app.data.local.ScreenshotDao
import com.gitfast.app.data.local.SorenessDao
import com.gitfast.app.data.repository.ExerciseRepository
import com.gitfast.app.data.repository.SorenessRepository
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.data.sync.FirestoreSync
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
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
         .build()
    }

    @Provides
    fun provideWorkoutDao(database: GitFastDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideCharacterDao(database: GitFastDatabase): CharacterDao {
        return database.characterDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(workoutDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutSaveManager(
        workoutDao: WorkoutDao,
        characterRepository: CharacterRepository,
        workoutRepository: WorkoutRepository,
        firestoreSync: FirestoreSync,
    ): WorkoutSaveManager {
        return WorkoutSaveManager(workoutDao, characterRepository, workoutRepository, firestoreSync)
    }

    @Provides
    @Singleton
    fun provideCharacterRepository(characterDao: CharacterDao): CharacterRepository {
        return CharacterRepository(characterDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutStateStore(@ApplicationContext context: Context): WorkoutStateStore {
        return WorkoutStateStore(context)
    }

    @Provides
    fun provideBodyCompDao(database: GitFastDatabase): BodyCompDao {
        return database.bodyCompDao()
    }

    @Provides
    fun provideSorenessDao(database: GitFastDatabase): SorenessDao {
        return database.sorenessDao()
    }

    @Provides
    @Singleton
    fun provideSorenessRepository(sorenessDao: SorenessDao): SorenessRepository {
        return SorenessRepository(sorenessDao)
    }

    @Provides
    fun provideExerciseDao(database: GitFastDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    @Singleton
    fun provideExerciseRepository(exerciseDao: ExerciseDao): ExerciseRepository {
        return ExerciseRepository(exerciseDao)
    }

    @Provides
    fun provideLapStartPointDao(database: GitFastDatabase): LapStartPointDao {
        return database.lapStartPointDao()
    }

    @Provides
    fun provideScreenshotDao(database: GitFastDatabase): ScreenshotDao {
        return database.screenshotDao()
    }

    @Provides
    @Singleton
    fun provideHealthConnectManager(@ApplicationContext context: Context): HealthConnectManager {
        return HealthConnectManager(context)
    }

    @Provides
    @Singleton
    fun provideBodyCompRepository(
        bodyCompDao: BodyCompDao,
        healthConnectManager: HealthConnectManager,
    ): BodyCompRepository {
        return BodyCompRepository(bodyCompDao, healthConnectManager)
    }
}
