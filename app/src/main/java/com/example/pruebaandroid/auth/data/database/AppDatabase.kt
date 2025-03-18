package com.example.pruebaandroid.auth.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pruebaandroid.auth.data.dao.UsuarioDao
import com.example.pruebaandroid.auth.data.model.Usuario

@Database(entities = [Usuario::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2) // Agregar migración en lugar de destruir
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Definir la migración
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Aquí defines cualquier cambio en la base de datos entre la versión 1 y 2
                database.execSQL("ALTER TABLE usuarios ADD COLUMN nueva_columna TEXT DEFAULT ''")
            }
        }
    }
}
