# Apex Signage — 10-Foot Digital Signage & Gapless Video Engine

Aplicativo Android TV / Kiosk minimalista optimizado para pantallas 1080p y hardware con limitaciones de recursos (**Android 6.0 Marshmallow / SoCs legacy**).

## Características Principales

- **Reproducción Gapless Sin Pantalla de Carga (`0ms`)**:
  - Cola nativa pre-concatenada con `Media3 ExoPlayer` (`REPEAT_MODE_ALL`) y retención de fotograma activo en hardware (`SurfaceView` + `setKeepContentOnPlayerReset(true)` + shutter transparente).
  - Pre-carga en segundo plano (`SimpleCache` de 150 MB + `CacheWriter` de cabeceras GOP iniciales) y límite estricto de 16 MB en RAM (`DefaultLoadControl`) para evitar errores de *buffering* o *OutOfMemory* en dispositivos Android 6.
- **Navegación 100% Direccional (`Arriba / Abajo / Izquierda / Derecha` + `OK`)**:
  - **Fila 0**: Barra superior de iconos minimalistas (*Full Screen*, *Play/Pausa*, *Pre-Cargar Caché*, *Modo Bucle*, *Ajustes*).
  - **Fila 1**: Escenario principal en vivo (acceso directo a **Modo Full Screen**) y cambio rápido de pista (*Anterior / Siguiente*).
  - **Filas 2 y 3**: Carruseles visuales `16:9` sin sobrecarga de texto.
- **Modo Full Screen**:
  - Video a pantalla completa sin elementos intrusivos.
  - Cambio instantáneo de video con **Izquierda / Derecha** y barra flotante minimalista con **Arriba / Abajo**.

## Compilación y Descarga

1. En **Google AI Studio**, abre el menú superior derecho (**Settings / Export**) para:
   - **Push to GitHub** (exportar directamente a tu repositorio).
   - **Download APK / AAB** o **Export ZIP**.
2. Para compilar localmente con Gradle:
   ```bash
   ./gradlew assembleDebug
   ./gradlew assembleRelease
   ```
