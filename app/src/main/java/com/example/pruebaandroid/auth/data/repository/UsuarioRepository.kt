package com.example.pruebaandroid.auth.data.repository

import com.example.pruebaandroid.auth.data.dao.UsuarioDao
import com.example.pruebaandroid.auth.data.model.Usuario
import javax.inject.Inject

class UsuarioRepository @Inject constructor(private val usuarioDao: UsuarioDao) {
    suspend fun obtenerUsuario(id: String) = usuarioDao.obtenerUsuario(id)

    suspend fun insertarUsuario(usuario: Usuario) = usuarioDao.insertarUsuario(usuario)

    suspend fun disminuirAccesos(id: String): Int = usuarioDao.disminuirAccesos(id)

    suspend fun eliminarUsuario(id: String) = usuarioDao.eliminarUsuario(id)

    suspend fun obtenerPrimerUsuario() = usuarioDao.obtenerPrimerUsuario()
}
