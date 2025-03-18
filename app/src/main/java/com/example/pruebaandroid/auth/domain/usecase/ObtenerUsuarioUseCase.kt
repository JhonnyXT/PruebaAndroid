package com.example.pruebaandroid.auth.domain.usecase

import com.example.pruebaandroid.auth.data.repository.UsuarioRepository
import com.example.pruebaandroid.auth.data.model.Usuario
import javax.inject.Inject

class ObtenerUsuarioUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(id: String): Usuario? {
        return usuarioRepository.obtenerUsuario(id)
    }
}
