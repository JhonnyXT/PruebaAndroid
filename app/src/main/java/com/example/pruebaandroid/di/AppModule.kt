package com.example.pruebaandroid.di

import android.content.Context
import androidx.room.Room
import com.example.pruebaandroid.auth.data.database.AppDatabase
import com.example.pruebaandroid.auth.data.dao.UsuarioDao
import com.example.pruebaandroid.auth.data.repository.UsuarioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2) // Asegura que la BD no se elimine
            .build()

        // 🔹 Forzar apertura para que Room aparezca en el Database Inspector
        db.openHelper.writableDatabase

        return db
    }

    @Provides
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao {
        return database.usuarioDao()
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(usuarioDao: UsuarioDao): UsuarioRepository {
        return UsuarioRepository(usuarioDao)
    }
}
