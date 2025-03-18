package com.example.pruebaandroid.auth.domain.usecase

import com.example.pruebaandroid.auth.data.repository.UsuarioRepository
import javax.inject.Inject

class DisminuirAccesosUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(userId: String): Int {
        return usuarioRepository.disminuirAccesos(userId)
    }
}
