# SmartHealth Monitor  
![Android CI](https://img.shields.io/badge/Android-API26+-green)  
![Compose](https://img.shields.io/badge/Jetpack%20Compose-MD3-blue)  
   
Aplicación Android de monitoreo de salud personal en tiempo real.  
Desarrollada como proyecto integrador — UTNG 9° Cuatrimestre 2025.  
   
## Stack tecnológico  
| Tecnología | Uso |  
|---|---|  
| Kotlin + Jetpack Compose | UI declarativa con Material Design 3 |  
| Wearable Data Layer API  | Comunicación reloj ↔ teléfono (BLE) |  
| Health Services API     | Sensor FC real en background (Wear OS) |  
| Room Database           | Historial persistente de lecturas FC |  
| Jetpack Navigation      | NavHost entre 4 pantallas |  
| GitHub + Conventional Commits | Control de versiones profesional |  
   
## Pantallas  
| Pantalla | Descripción |  
|---|---|  
| LoginScreen | Autenticación con validación y State |  
| DashboardScreen | FC y Pasos en tiempo real del wearable |  
| HistorialScreen | Lecturas persistidas en Room con Flow reactivo |  
| AlertaScreen | AlertDialog MD3 + Snackbar de confirmación |  
   
## Capturas de pantalla  
![Login](screenshots/login.png)  
![Dashboard](screenshots/dashboard.png)  
![Historial](screenshots/historial.png)  
![Alerta](screenshots/alerta.png)  
   
## Arquitectura — SmartHealth Monitor
```text
Sensor PPG (Wear OS)
    │  Health Services API
    ▼
PassiveListenerService (wear)
    │  MessageClient (BLE)
    ▼
WearListenerService (app)
    │  SmartHealthRepository
    ▼
StateFlow<Int> (fcActual)
 ──────────────────────────────────┐
    │                                                        │
    ▼                                                        ▼
DashboardViewModel (app)              TvViewModel (tv)
    │  collectAsState()                    │  collectAsState()
    ▼                                        ▼
DashboardScreen (Compose)          TvCatalogScreen (Compose TV)
    └── CastButton ──► Chromecast (Remote Playback)

Room DB (LecturaFC)  ◄──  Repository  ──►  Flow<List<LecturaFC>>
                                                │
                          ┌─────────────────────┴──────────┐
                          ▼                                ▼
               HistorialScreen (app)        TvCatalogScreen (tv)
```

## Autor  
Carlos Manuel Palma Muñoz — UTNG — Ing. en Desarrollo y Gestión de Software  
