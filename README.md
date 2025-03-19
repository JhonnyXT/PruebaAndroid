# Prueba Técnica - Desarrollador Android con Kotlin y Jetpack Compose

## 📌 Objetivo
Desarrollar una aplicación Android que implemente autenticación, almacenamiento de datos en **Room Database** y **Firestore**, manejo de archivos en **PDF**, mapas con **Google Maps**, integración con **Firebase Authentication**, y despliegue continuo en **Firebase Distribution**.

## 🛠️ Tecnologías y Herramientas
- **Lenguaje:** Kotlin
- **Arquitectura:** MVVM + Kotlin Flows
- **Inyección de Dependencias:** Dagger Hilt
- **Base de Datos Local:** Room Database
- **Autenticación:** Firebase Authentication (Google Sign-In)
- **HTTP Requests:** Volley
- **UI:** Jetpack Compose
- **Persistencia de Datos:** Firestore + Room
- **Mapas:** Google Maps API
- **Gestión de Estados:** StateFlow + LiveData
- **Pruebas:** JUnit + UI Tests
- **Despliegue:** GitHub + Firebase Distribution

## 📂 Estructura del Proyecto (Screaming Architecture)
El proyecto sigue una estructura modular con **carpetas por feature**:

```
com.example.pruebaandroid
 ├── auth
 │   ├── data
 │   │   ├── dao
 │   │   │   ├── UsuarioDao.kt
 │   │   ├── database
 │   │   │   ├── AppDatabase.kt
 │   │   ├── model
 │   │   │   ├── Usuario.kt
 │   │   ├── repository
 │   │   │   ├── UsuarioRepository.kt
 │   ├── domain.usecase
 │   │   ├── ObtenerUsuarioUseCase.kt
 │   │   ├── InsertarUsuarioUseCase.kt
 │   │   ├── DisminuirAccesosUseCase.kt
 │   │   ├── EliminarUsuarioUseCase.kt
 │   │   ├── ValidarCredencialesUseCase.kt
 │   ├── ui
 │   │   ├── viewmodel
 │   │   │   ├── LoginViewModel.kt
 │   │   ├── LoginScreen.kt
 ├── menu
 │   ├── domain.usecase
 │   │   ├── DescargarPDFUseCase.kt
 │   │   ├── RefrescarDatosUseCase.kt
 │   ├── ui
 │   │   ├── MenuScreen.kt
 ├── splash.ui
 │   ├── SplashScreen.kt
 ├── di
 │   ├── AppModule.kt
 ├── navigation
 │   ├── NavGraph.kt
 ├── theme
 ├── MainActivity.kt
 ├── MainApplication.kt
```

## 💻 Descripción de la Aplicación

### 1️⃣ Splash Screen
- **Duración:** 2 segundos.
- **Funcionalidad:** Verifica si el usuario está registrado en **Room Database**.
  - Si el usuario está registrado, navega a **Menú**.
  - Si no está registrado, navega a **Login**.

### 2️⃣ Login
- **Componentes:**
  - Campos de **usuario** y **contraseña**.
  - Botón de **Login**.
  - Botón de **Autenticación con Google**.
- **Funcionalidad:**
  - Se realiza una solicitud **POST** a:
    ```
    https://noderedtest.coordinadora.com/api/v1/validacion-usuario/
    ```
  - Si el login **falla**, muestra un mensaje y **limpia los campos**.
  - Si el login **es exitoso**, almacena los datos en **Room Database** y **Firestore**.

### 3️⃣ Menú
- **Componentes:**
  - Icono de **Refrescar**.
  - Botón para **Obtener archivo**.
  - Lista con el **PDF** descargado.
- **Funcionalidad:**
  - Al ingresar, realiza una solicitud **GET** a:
    ```
    https://noderedtest.coordinadora.com/api/v1/obtenerimagen/
    ```
  - Convierte el archivo **Base64** en **PDF** y lo almacena en:
    ```
    /descargas/pruebacoordi/archivo.pdf
    ```
  - Muestra el **PDF completo** permitiendo desplazamiento.
  - **Refrescar:** Vuelve a obtener los datos desde la API.

### 4️⃣ Gestión de Accesos
- **Campo `periodo_validacion`**:
  - Cada acceso **disminuye en 1** el contador.
  - Si llega a **cero**, se cierra sesión automáticamente y se elimina el usuario de **Firestore**.
  - **Modo Offline:** Si no hay internet, se almacena el decremento y se sincroniza cuando la conexión vuelva.

### 5️⃣ Cierre de Sesión
- **Cuando `periodo_validacion` llega a 0**:
  - Se **elimina** el usuario de **Room Database** y **Firestore**.
  - Se bloquea el acceso **incluso sin internet**.

## 💪 Despliegue Continuo con Firebase Distribution
- **Repositorio:** GitHub con ramas `dev` y `test`.
- **CI/CD:** Firebase Distribution despliega automáticamente al hacer **merge en `test`**.
- **Pruebas:** Se ejecutan **tests unitarios** antes del despliegue.

## ✅ Pruebas Implementadas
- **Pruebas Unitarias:** Mínimo **2 por pantalla** con **JUnit**.
- **Pruebas UI:** Interacción con Compose y validaciones visuales.
- **Pipeline de CI/CD:** Ejecuta pruebas antes del despliegue.

## 📲 Compatibilidad y Permisos
- **Soporta Android 8 a Android 15**.
- **Manejo de permisos:** Se solicitan según versión de Android.

## 🚀 Instalación y Ejecución
1. Clona el repositorio:
   ```sh
   git clone https://github.com/JhonnyXT/pruebaandroid.git
   cd pruebaandroid
   ```
2. Abre el proyecto en **Android Studio**.
3. Compila y ejecuta en un emulador o dispositivo físico.
4. Para probar con Firebase Distribution:
   ```sh
   git checkout test
   git merge dev
   git push origin test
   ```

## 🏆 Criterios de Evaluación
✔️ Funcionalidad completa.  
✔️ Correcta implementación de **MVVM**.  
✔️ Uso adecuado de **Dagger Hilt** para inyección de dependencias.  
✔️ Persistencia con **Room Database y Firestore**.  
✔️ Peticiones HTTP con **Volley** y manejo de errores.  
✔️ Flujo de ramas y despliegue continuo con **Firebase Distribution**.  
✔️ Pruebas unitarias y de UI ejecutadas en **CI/CD**.  
✔️ Experiencia de usuario e interfaz optimizada con **Jetpack Compose**.  

