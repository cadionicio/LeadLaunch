# LeadLaunch — Backend (FastAPI)

Responsable: César · Rama: `cesar/backend`

## Correr en local (Windows, PowerShell)

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
copy .env.example .env      # luego editar .env con el DATABASE_URL de Neon
uvicorn app.main:app --reload
```

- API: http://127.0.0.1:8000/health
- Prueba de base de datos: http://127.0.0.1:8000/health/db
- Swagger: http://127.0.0.1:8000/docs

## Pruebas

```powershell
pytest
```

## Capas

| Carpeta | Capa | Qué va aquí |
|---|---|---|
| `app/routers/` | 1 · Controladores | Endpoints, validación de entrada (Pydantic), códigos HTTP |
| `app/services/` | 2 · Servicios | Reglas de negocio: Auth, Business, Catalog, Policy, Radar, AdaptiveAI, Quote |
| `app/repositories/` | 3 · Acceso a datos | SQL parametrizado contra PostgreSQL |
| `app/schemas/` | DTOs | Modelos Pydantic de request/response |
| `app/core/` | Transversal | Configuración, conexión BD, errores, seguridad JWT |

## Despliegue (Render)
Ver `render.yaml` en la raíz del repo. Variables de entorno: las mismas de `.env.example`.
