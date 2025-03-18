package com.example.pruebaandroid.auth.domain.usecase

import com.example.pruebaandroid.auth.data.repository.UsuarioRepository
import javax.inject.Inject

class EliminarUsuarioUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(userId: String) {
        usuarioRepository.eliminarUsuario(userId)
    }
}
