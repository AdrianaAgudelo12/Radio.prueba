# Documento Técnico - Radio .prueba (App de Streaming y Multimedia en Android)

---

## 1. Portada y Estructura Organizativa

* **Título de la Actividad:** Desarrollo de Aplicación Móvil Nativa en Jetpack Compose - Radio .prueba
* **Modalidad Seleccionada:** Individual / Software House
* **Integrantes del Equipo / Roles Asumidos:**
  * **Arquitectura y UI (Punto 1 y 2):** Diseño de la interfaz declarativa en Jetpack Compose y estructura del MVP.
  * **Core Hardware & Permisos (Rol 4):** Implementación de la cámara nativa, permisos en *runtime* (`CAMERA`) y retroalimentación háptica (`Vibrator`).
  * **Audio & Integración (Rol 5):** Configuración del reproductor multimedia con `Media3 ExoPlayer` para streaming en vivo.
  * **QA & DevOps (Rol 6):** Pruebas funcionales, compilación del APK y validación general.

---

## 2. Definición de Arquitectura y Componentes

La aplicación está construida utilizando **Jetpack Compose** bajo un enfoque modular y declarativo:
* **Gestión de Estado:** Uso de `mutableStateOf` y `rememberSaveable` para preservar el estado de reproducción (`isPlaying`), silencio (`isMuted`), emisora activa (`selectedStation`) y foto de perfil frente a cambios de configuración (como rotación de pantalla).
* **Reproducción de Audio (ExoPlayer):** Integración de `androidx.media3.exoplayer.ExoPlayer` con `LaunchedEffect` para gestionar flujos de audio en streaming (Icecast/Shoutcast de emisoras colombianas).
* **Acceso a Hardware:**
  * *Cámara:* Uso de `ActivityResultContracts.TakePicturePreview()` disparado por un permiso en tiempo de ejecución (`Manifest.permission.CAMERA`).
  * *Haptics:* Integración de `HapticFeedback` y servicio `Vibrator` para respuesta táctil en los controles del reproductor.

---

## 3. Requerimientos Funcionales Implementados (RF-01 al RF-07)

1. **RF-01 (Maquetación Declarativa):** Pantalla principal organizada en secciones (Perfil/Cámara superior, Tarjeta de reproductor con ecualizador animado, y catálogo inferior de emisoras).
2. **RF-02 y RF-03 (Perfil y Permisos):** Botón flotante para solicitar autorización de cámara en *runtime* y actualizar el avatar del usuario con la foto capturada.
3. **RF-05 (Retroalimentación Háptica):** Vibración corta activada al pulsar los botones de reproducción y selección de emisoras.
4. **RF-07 (Reproductor de Streaming):** Conexión a streams de audio en vivo de emisoras populares de Colombia (*Olímpica Stereo, La X, Caracol Radio, Blu Radio, Tropicana, La Z* e *IU Digital Radio*).

---

## 4. Evidencias y Capturas de Pantalla (Para pegar en el PDF)

* *(Inserta aquí la captura de pantalla de la app ejecutándose en el emulador mostrando el gradiente, el ecualizador y las emisoras).*
* *(Inserta aquí la captura del cuadro de diálogo solicitando permiso de cámara).*
* *(Inserta aquí la captura del perfil con la foto tomada).*
* *(Inserta aquí la captura de la compilación exitosa en Android Studio - "Build Successful").*

---

## 5. Enlaces de Interés

* **Enlace al Repositorio Git:** [GitHub - Radio .prueba](https://github.com/tu-usuario/radioprueba) *(Reemplazar con tu enlace público)*
* **Enlace al Video Demostrativo (Google Drive):** [Video de Demostración (2-4 min)](https://drive.google.com/file/d/tu-enlace-de-video/view) *(Reemplazar con tu enlace de Drive)*
