# OttPlusV2 - Documento de Diseno

## 1. Resumen del Proyecto

Cliente OTT (Over-The-Top) para Android TV que permite la reproduccion de contenido de video bajo demanda (VOD) y television en vivo (Live) mediante la integracion de ExoPlayer y un backend remoto. La autenticacion se realiza mediante un flujo de activacion remota tipo device-code.

---

## 2. Flujo de Autenticacion - Activacion Remota

El dispositivo Android TV solicita un codigo de activacion al backend, lo muestra en pantalla (texto + QR), y el usuario lo autoriza desde un frontend web (tuweb.com/activate) vinculandolo a su cuenta.

**Secuencia:**

1. TV llama a `POST /api/v1/devices/register` con `device_id` y `device_name`
2. Backend devuelve `activation_code` con TTL de 10 minutos
3. TV muestra el codigo formateado (ABC-DEF-123) y URL/QR
4. Usuario ingresa el codigo en el frontend web
5. Frontend llama a `POST /api/v1/devices/activate` con el codigo y token de cuenta
6. TV hace polling a `POST /api/v1/devices/status` cada 5 segundos
7. Backend confirma `status: "activated"` y entrega `access_token` + `refresh_token`
8. TV guarda tokens en SecureSharedPreferences y navega a Home

**Estados del Dispositivo:**

| Estado | Descripcion |
|--------|-------------|
| pending | Esperando que el usuario active el codigo en el frontend |
| activated | Dispositivo autorizado, tokens entregados |
| expired | Codigo de activacion caducado (10 min TTL) |

---

## 3. Arquitectura del Proyecto

### 3.1 Capas (Clean Architecture)

**Presentation Layer**
- ActivationActivity: Activity principal que contiene el Fragment
- ActivationFragment: UI con codigo, progreso, estado, retry
- ActivationViewModel: Maneja estado UI con StateFlow

**Domain Layer**
- ActivationUseCase: Orquesta registro + polling como Flow
- ActivationUiState: Sealed class con Loading, WaitingForUser, Activated, Error

**Data Layer**
- ActivationRepository: Logica de registro y polling al backend
- OttApiService: Interface Retrofit para endpoints
- OttApiClient: Singleton Retrofit + OkHttp
- AuthTokenStore: Almacenamiento de tokens y estado
- DeviceIdProvider: UUID persistente por dispositivo

### 3.2 Flujo de Datos

```
UI (Fragment) --> ViewModel --> UseCase --> Repository --> API (Retrofit)
                                         --> Storage (SharedPrefs)
```

### 3.3 Patrones Aplicados

- MVVM para capa de presentacion
- UseCase como orquestador de negocio
- Repository como abstraction de fuente de datos
- StateFlow para observable de estado UI
- Coroutines para operaciones asincronas

---

## 4. Stack Tecnologico

| Componente | Tecnologia |
|------------|------------|
| Lenguaje | Kotlin 1.9.22 |
| SDK minimo | API 21 (Android 5.0) |
| UI TV | AndroidX Leanback 1.0.0 |
| Video Player | ExoPlayer (pendiente integracion) |
| Networking | Retrofit 2.9.0 + OkHttp 4.12.0 |
| Serializacion | Gson |
| Coroutines | kotlinx-coroutines-android 1.7.3 |
| Seguridad | AndroidX Security-Crypto 1.1.0 |
| Build | Gradle 8.4, AGP 8.2.2 |

---

## 5. Contratos de API

### 5.1 Register Device

```
POST /api/v1/devices/register

Request:
{
    "device_id": "uuid",
    "device_name": "Samsung Android TV",
    "device_type": "android_tv"
}

Response 200:
{
    "activation_code": "ABC-DEF-123",
    "expires_in": 600
}
```

### 5.2 Check Activation Status

```
POST /api/v1/devices/status

Request:
{
    "device_id": "uuid",
    "activation_code": "ABC-DEF-123"
}

Response 200:
{
    "status": "activated",
    "access_token": "eyJhbGci...",
    "refresh_token": "dGhpcyBp...",
    "expires_in": 3600
}
```

---

## 6. Estructura de Directorios

```
OttPlusV2/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ott/tv/
│       │   ├── OttApplication.kt
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   ├── OttApiClient.kt
│       │   │   │   └── OttApiService.kt
│       │   │   ├── model/
│       │   │   │   └── ActivationModels.kt
│       │   │   ├── repository/
│       │   │   │   └── ActivationRepository.kt
│       │   │   └── storage/
│       │   │       ├── AuthTokenStore.kt
│       │   │       └── DeviceIdProvider.kt
│       │   ├── domain/
│       │   │   └── ActivationUseCase.kt
│       │   ├── presentation/
│       │   │   └── activation/
│       │   │       ├── ActivationActivity.kt
│       │   │       ├── ActivationFragment.kt
│       │   │       └── ActivationViewModel.kt
│       │   └── util/
│       │       └── NetworkUtils.kt
│       └── res/
│           ├── drawable/
│           ├── layout/
│           ├── mipmap-*/
│           └── values/
├── build.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── settings.gradle.kts
```

---

## 7. Proximos Pasos

1. Integrar ExoPlayer - Player con soporte HLS/DASH
2. Pantalla Home - Catalogo de contenido con rows
3. Detail Screen - Ficha de contenido con playback
4. DRM Widevine - Proteccion de contenido
5. EPG - Guia de programacion para canales en vivo
6. Subtitulos y multi-audio
7. Favoritos y continuar viendo
8. Busqueda por voz
9. Login con email/password como alternativa
10. QA y testing end-to-end
