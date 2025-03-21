package com.example.pruebaandroid.menu.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pruebaandroid.auth.domain.AuthViewModel
import com.example.pruebaandroid.menu.domain.MenuViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    menuViewModel: MenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usuario by authViewModel.usuario.collectAsState()
    val listaPdf = remember { mutableStateListOf<String>() }
    var isDownloading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val yaDescontado = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val usuarioActual = authViewModel.obtenerUsuarioActual()
        if (usuarioActual != null) {
            authViewModel.obtenerUsuario()
        } else {
            navController.navigate("login") { popUpTo("menu") { inclusive = true } }
        }
    }

    LaunchedEffect(usuario) {
        usuario?.let { currentUser ->
            if (!yaDescontado.value) {
                yaDescontado.value = true
                if (currentUser.periodoValidacion > 0) {
                    authViewModel.disminuirAccesos(currentUser.id, context) {
                        navController.navigate("login") { popUpTo("menu") { inclusive = true } }
                    }
                } else {
                    authViewModel.cerrarSesion(currentUser.id) {
                        navController.navigate("login") { popUpTo("menu") { inclusive = true } }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Menú") },
                actions = {
                    IconButton(onClick = {
                        isRefreshing = true
                        menuViewModel.refrescarDatos(context) { success, filePath ->
                            isRefreshing = false
                            if (success && filePath != null) {
                                listaPdf.clear()
                                listaPdf.add(filePath)
                                pdfPages = renderPdfPages(filePath)
                            } else {
                                Toast.makeText(context, "No hay nuevos datos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refrescar")
                        }
                    }
                    IconButton(onClick = { /* TODO: Implementar navegación al mapa */ }) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = "Mapa")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    isDownloading = true
                    menuViewModel.descargarYGuardarPDF(context) { success, filePath ->
                        isDownloading = false
                        if (success && filePath != null) {
                            listaPdf.add(filePath)
                            pdfPages = renderPdfPages(filePath)
                        } else {
                            Toast.makeText(context, "Error al descargar el archivo", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isDownloading
            ) {
                Text(if (isDownloading) "Descargando..." else "Obtener archivo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Accesos restantes: ${usuario?.periodoValidacion ?: 0}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(pdfPages) { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Página PDF",
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
            }
        }
    }
}

// Función para renderizar todas las páginas del PDF
fun renderPdfPages(pdfPath: String): List<Bitmap> {
    val file = File(pdfPath)
    val bitmapList = mutableListOf<Bitmap>()

    if (!file.exists()) {
        Log.e("PDF_VIEWER", "El archivo PDF no existe en la ruta: $pdfPath")
        return bitmapList
    }

    try {
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)

        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmapList.add(bitmap)
            page.close()
        }

        pdfRenderer.close()
        fileDescriptor.close()
    } catch (e: Exception) {
        Log.e("PDF_VIEWER", "Error al abrir el PDF: ${e.message}")
    }

    return bitmapList
}