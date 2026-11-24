# LeadLaunch — Radar comercial y copiloto adaptativo local

App Android nativa (Kotlin) + API REST propia (FastAPI) + PostgreSQL en la nube (Neon) para emprendimientos de repostería y comida local: detecta fricciones de competidores cercanos, genera campañas con IA y crea cotizaciones listas para WhatsApp.

## Equipo
| Persona | Área | Rama | Carpeta |
|---|---|---|---|
| César | Líder · Backend API | `cesar/backend` | `backend/` |
| Giancarlos | App Android | `giancarlos/android` | `android/` |
| Ernesto | Base de datos · Pruebas | `ernesto/db-qa` | `database/`, `docs/pruebas/` |

## Estructura
```text
backend/     API REST FastAPI (4 capas)
android/     Proyecto Android Studio (Kotlin + Compose)
database/    DDL, semillas y dataset de respaldo del radar
docs/        Planificación, especificaciones, contratos de API y pruebas
render.yaml  Despliegue del backend en Render
```

## Ramas
```text
main      ← versiones estables (v0.5.0 = Fase 1, v1.0.0 = final)
develop   ← integración en cada checkpoint (por Pull Request)
  ├── cesar/backend
  ├── giancarlos/android
  └── ernesto/db-qa
```
Nadie hace push directo a `main` ni a `develop`. Detalles en `docs/planificacion/00_PLAN_GENERAL.md`.

## Commits
`tipo(HU-XX): descripción` — tipos: `feat`, `fix`, `docs`, `test`, `chore`.

## Documentos
- Plan general: `docs/planificacion/00_PLAN_GENERAL.md`
- Especificación funcional: `docs/especificaciones/Documento_Maestro_LeadLaunch.md`
- Especificación UX/UI: `docs/especificaciones/LEADLAUNCH_Especificacion_UX_UI_v1.md`
- Contratos de API: `docs/api/CONTRATOS_API.md`
