# Pruebas de la API (QA)

Responsable: Ernesto · Matriz obligatoria: CP-01 a CP-10 (documento maestro §7.2, RNF-05).

| Archivo | Contenido |
|---|---|
| `RESULTADOS.md` | Matriz con resultado esperado vs. obtenido, observaciones y detalle de cada solicitud |
| `LeadLaunch_QA.postman_collection.json` | Colección de Postman para repetir las pruebas (Import → Run collection) |
| `ejecucion_2026-11-24.json` | Registro completo de la ejecución contra producción (JWT omitidos) |
| `evidencias/CP-xx.png` | Evidencia visual de cada caso ejecutado |


## Guía de ejecución de pruebas automatizadas (Postman)

### 1. Requisitos previos
- Tener instalado **Postman** (versión de escritorio o web).
- Contar con acceso a Internet para conectar con el entorno desplegado en Render (`https://leadlaunch-api.onrender.com`).

### 2. Importar la colección
1. Abrir Postman y hacer clic en **Import** (arriba a la izquierda).
2. Seleccionar el archivo `docs/pruebas/LeadLaunch_QA.postman_collection.json` de este repositorio.
3. Verificar que en el panel izquierdo aparezca la colección **LeadLaunch QA** con los casos `CP-01` a `CP-10`.

### 3. Ejecución completa (Collection Runner)
1. Hacer clic en los tres puntos (`...`) junto al nombre de la colección y seleccionar **Run collection**.
2. Asegurarse de mantener el **orden secuencial** de las peticiones (el registro y login generan el token JWT y el `negocio_id` que usan las pruebas siguientes).
3. Presionar **Run LeadLaunch QA**.

### 4. Notas sobre datos de prueba
- **Correos únicos:** Las peticiones de registro utilizan la variable dinámica `{{$timestamp}}` para crear cuentas limpias (`qa_<timestamp>@correo-prueba.com`) en cada ejecución sin generar conflictos de duplicidad.
- **Cold start de Render:** Si la API lleva inactiva varios minutos, la primera petición (`CP-01`) puede tardar entre 20 y 40 segundos mientras el servidor despierta; las siguientes responderán en menos de 2 segundos.
