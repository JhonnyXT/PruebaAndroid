package com.example.pruebaandroid.menu.domain

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.pruebaandroid.menu.domain.usecase.DescargarPDFUseCase
import com.example.pruebaandroid.menu.domain.usecase.RefrescarDatosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val descargarPDFUseCase: DescargarPDFUseCase,
    private val refrescarDatosUseCase: RefrescarDatosUseCase
) : ViewModel() {

    fun descargarYGuardarPDF(context: Context, onResult: (Boolean, String?) -> Unit) {
        descargarPDFUseCase.execute(context, onResult)
    }

    fun refrescarDatos(context: Context, onResult: (Boolean, String?) -> Unit) {
        refrescarDatosUseCase.execute(context, onResult)
    }
}