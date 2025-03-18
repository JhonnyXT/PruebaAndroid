package com.example.pruebaandroid.auth.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pruebaandroid.auth.data.model.Usuario

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE id = :userId LIMIT 1")
    suspend fun obtenerUsuario(userId: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    @Query("UPDATE usuarios SET periodoValidacion = periodoValidacion - 1 WHERE id = :userId")
    suspend fun disminuirAccesos(userId: String): Int

    @Query("DELETE FROM usuarios WHERE id = :userId")
    suspend fun eliminarUsuario(userId: String)

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerPrimerUsuario(): Usuario?
}
