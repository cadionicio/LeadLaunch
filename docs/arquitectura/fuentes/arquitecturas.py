from svgkit import *

# =====================================================================
# 1) Arquitectura orientada a servicios (lógica, 4 capas)
# =====================================================================
c = Lienzo(1600, 790)
X, Wm = 30, 1210          # columna principal
XE, WE = 1290, 280        # columna de servicios externos
n, g, pad = 6, 14, 18
cw = (Wm - 2 * pad - (n - 1) * g) / n
col = [X + pad + i * (cw + g) for i in range(n)]

# Capa 0
c.banda(X, 20, Wm, 128, "CAPA 0 · PRESENTACIÓN", "App Android nativa · Kotlin", AZUL, SUAVE)
for i, (t, s, d) in enumerate([("Pantallas", "Jetpack Compose\nS01–S17", 0), ("Estado y navegación", "Navigation · corrutinas", 0),
                               ("Repositorios", "ApiResult · errores", 0), ("Cliente HTTP", "Retrofit + OkHttp\nAuthInterceptor JWT", 0),
                               ("Sesión", "DataStore\nJWT · negocio_id", 0), ("Intent WhatsApp", "wa.me + mensaje", 1)]):
    c.comp(col[i], 52, cw, 78, t, s, dash=bool(d))
c.flecha([(X + Wm / 2, 148), (X + Wm / 2, 190)], doble=True, sw=2.2, etiqueta="HTTPS · REST / JSON · Authorization: Bearer JWT", epos=(X + Wm / 2 + 14, 174))

# Capa 1
c.banda(X, 190, Wm, 160, "CAPA 1 · CONTROLADORES", "API REST · FastAPI (routers)", AZUL_OSC)
for i, (t, s, d) in enumerate([("/auth", "register · login", 0), ("/businesses", "alta · detalle", 0),
                               ("/products /rules", "/dashboard", 0), ("/radar/scan", "escaneo", 0),
                               ("/campaigns", "generar · última", 1), ("/quotes /analytics", "emitir · estado", 1)]):
    c.comp(col[i], 222, cw, 66, t, s, dash=bool(d))
c.comp(col[0], 300, Wm - 2 * pad, 36, "", "", fill=FONDO)
c.texto(X + Wm / 2, 323, "Transversal: validación Pydantic · autenticación JWT · aislamiento por negocio (404) · errores {codigo, mensaje, campos}", 12, 500, TEXTO, "middle")
c.flecha([(X + Wm / 2, 350), (X + Wm / 2, 392)], sw=2.2)

# Capa 2
c.banda(X, 392, Wm, 128, "CAPA 2 · SERVICIOS DE NEGOCIO", "reglas, orquestación e integraciones", INDIGO)
servs = [("AuthService", "bcrypt · emite JWT", 0), ("BusinessService", "alta ACID · propiedad", 0),
         ("Catalog · Policy", "Dashboard", 0), ("RadarService", "filtro 1–3★\nclasificador", 0),
         ("AdaptiveAIService", "1 llamada · respaldo\ncon plantilla", 1), ("QuoteService", "liquidación\nsnapshot · wa.me", 1)]
for i, (t, s, d) in enumerate(servs):
    c.comp(col[i], 424, cw, 78, t, s, dash=bool(d))
c.flecha([(X + Wm / 2, 520), (X + Wm / 2, 562)], sw=2.2)

# Capa 3
c.banda(X, 562, Wm, 104, "CAPA 3 · ACCESO A DATOS", "repositorios · SQL parametrizado", VERDE)
c.comp(col[0], 594, cw * 4 + g * 3, 56, "Repositorios", "usuarios · negocios · productos · reglas · radar · campañas · cotizaciones")
c.comp(col[4], 594, cw * 2 + g, 56, "Pool psycopg", "transacciones ACID · reconexión")
c.flecha([(X + Wm / 2, 666), (X + Wm / 2, 702)], sw=2.2, etiqueta="TLS · SQL parametrizado", epos=(X + Wm / 2 + 14, 690))
c.cilindro(X + Wm / 2 - 170, 702, 340, 80, "PostgreSQL 18 · Neon", "14 tablas · triggers · vista materializada")

# Servicios externos
c.texto(XE, 396, "SERVICIOS EXTERNOS", 12, 700, GRIS)
c.texto(XE + 150, 396, "vía HTTPS · API key", 11, 400, GRIS)
c.comp(XE, 416, WE, 62, "Google Gemini API", "generación de campañas", dash=True, fill=FONDO)
c.comp(XE, 490, WE, 62, "Fuente de reseñas", "dataset demo · proveedor (futuro)", fill=FONDO)
c.flecha([(X + Wm, 447), (XE, 447)], dash=True)
c.flecha([(X + Wm, 521), (XE, 521)])
leyenda(c, XE, 600)
c.texto(XE, 672, "Cada capa solo conoce a la siguiente:", 11, 400, GRIS)
c.texto(XE, 688, "bajo acoplamiento, alta cohesión.", 11, 400, GRIS)
c.guardar("soa.svg")

# =====================================================================
# 2) Arquitectura tecnológica (despliegue e integración)
# =====================================================================
c = Lienzo(1600, 790)
# Dispositivo
c.banda(30, 20, 400, 520, "DISPOSITIVO DEL EMPRENDEDOR", "", AZUL, SUAVE)
c.texto(46, 64, "Android 7.0+ (API 24)", 12, 400, GRIS)
c.comp(56, 90, 348, 108, "App LeadLaunch", "Kotlin · Jetpack Compose · Material 3\nRetrofit + OkHttp · DataStore · osmdroid", fill="white", stroke=AZUL)
c.comp(56, 300, 348, 70, "WhatsApp", "app del sistema (externa)")
c.flecha([(230, 198), (230, 300)], etiqueta="Intent ACTION_VIEW\nhttps://wa.me/51…?text=…", epos=(242, 240))
c.comp(56, 430, 348, 70, "Firebase App Tester", "instala y actualiza la app", dash=True)

# Render
c.banda(560, 20, 460, 520, "RENDER · WEB SERVICE", "plan gratuito", AZUL_OSC)
c.comp(586, 70, 408, 62, "Uvicorn", "servidor ASGI · HTTPS gestionado por Render")
c.comp(586, 160, 408, 108, "API LeadLaunch · FastAPI", "Python 3.12 · Pydantic · PyJWT · bcrypt\nrouters → servicios → repositorios", stroke=AZUL_OSC)
c.comp(586, 296, 408, 62, "Pool de conexiones psycopg", "conexiones verificadas y renovadas")
c.flecha([(790, 132), (790, 160)])
c.flecha([(790, 268), (790, 296)])
c.rect(586, 392, 408, 120, fill=FONDO, stroke=BORDE, dash=True)
c.texto(606, 418, "Secretos (variables de entorno)", 12, 700, TEXTO)
for i, s in enumerate(["DATABASE_URL · JWT_SECRET · GEMINI_API_KEY", "Nunca en el repositorio ni en el celular", "Despliegue automático en cada push"]):
    c.texto(606, 444 + i * 20, s, 12, 400, GRIS)

c.flecha([(404, 214), (586, 214)], doble=True, sw=2.2)
c.texto(495, 180, "HTTPS :443", 12, 600, TEXTO, "middle")
c.texto(495, 198, "REST/JSON · JWT", 11, 400, GRIS, "middle")

# Neon, Gemini, reseñas
c.banda(1150, 20, 420, 190, "NEON · POSTGRESQL", "TLS :5432 · sslmode=require", VERDE)
c.cilindro(1250, 66, 220, 120, "PostgreSQL 18", "BD leadlaunch\n14 tablas · triggers · MV")
c.banda(1150, 240, 420, 120, "GOOGLE AI STUDIO", "HTTPS · API key", INDIGO)
c.comp(1176, 278, 368, 62, "Gemini API (modelo Flash)", "plan gratuito · configurable", dash=True)
c.banda(1150, 390, 420, 150, "FUENTE DE RESEÑAS", "lectura en el servidor", GRIS)
c.comp(1176, 428, 176, 90, "Dataset demo", "en el repositorio\n(activo hoy)")
c.comp(1368, 428, 176, 90, "Google Places", "reseñas reales\n(trabajo futuro)", dash=True)
c.flecha([(994, 172), (1045, 172), (1045, 126), (1250, 126)], sw=2.2)
c.flecha([(994, 222), (1080, 222), (1080, 309), (1176, 309)], dash=True)
c.flecha([(994, 340), (1115, 340), (1115, 473), (1176, 473)])

# Entrega continua
c.banda(30, 580, 1540, 190, "CICLO DE ENTREGA", "código → producción", GRIS, FONDO)
c.comp(56, 640, 300, 96, "GitHub", "main · develop · ramas por persona\nPull Requests · tags v0.5.x")
c.comp(560, 640, 300, 96, "GitHub Actions", "compila y firma el APK", dash=True)
c.comp(1000, 640, 300, 96, "Firebase App Distribution", "distribuye cada versión", dash=True)
c.flecha([(356, 668), (470, 668), (470, 520), (560, 520)], etiqueta="auto-deploy (push)", epos=(366, 660))
c.flecha([(356, 708), (560, 708)], dash=True, etiqueta="merge a main", epos=(380, 726))
c.flecha([(860, 688), (1000, 688)], dash=True)
c.flecha([(1150, 640), (1150, 560), (500, 560), (500, 465), (404, 465)], dash=True)
leyenda(c, 1350, 660)
c.guardar("tecnologia.svg")

# =====================================================================
# 3) Arquitectura de datos (flujo)
# =====================================================================
c = Lienzo(1600, 790)
cols = [("FUENTES", AZUL), ("INGESTA Y VALIDACIÓN", AZUL_OSC), ("ALMACENAMIENTO", VERDE), ("PROCESAMIENTO", INDIGO), ("CONSUMO", AZUL)]
cw, gap, x0, yb, hb = 276, 35, 30, 20, 610
xs = [x0 + i * (cw + gap) for i in range(5)]
for (t, colr), x in zip(cols, xs):
    c.banda(x, yb, cw, hb, t, "", colr, FONDO if t != "ALMACENAMIENTO" else "white")

def caja(i, y, h, t, s, dash=False, stroke=BORDE):
    c.comp(xs[i] + 16, y, cw - 32, h, t, s, dash=dash, stroke=stroke)

caja(0, 70, 96, "Emprendedor", "registro, catálogo, reglas\n(formularios de la app)")
caja(0, 196, 96, "Reseñas de competidores", "dataset demo hoy\nproveedor externo a futuro")
caja(0, 322, 96, "Prospecto", "pedido recibido\npor WhatsApp", dash=True)
caja(0, 448, 96, "Configuración", "rubros y tipos de fricción\n(semilla paramétrica)")

caja(1, 70, 120, "API REST + Pydantic", "tipos, rangos y formatos\nJWT y propiedad del negocio\n422 / 401 / 404")
caja(1, 230, 120, "Radar", "filtro 1–3★ con texto\nclasificación en 5 tipos\nsin duplicados")
caja(1, 390, 96, "Liquidación", "reglas vigentes · Decimal\nvalida stock y política", dash=True)

caja(2, 70, 110, "Catálogo (4)", "rubros · tipos de fricción\npolíticas · reglas", stroke=INDIGO)
caja(2, 210, 110, "Parque (4)", "usuarios · negocios\nproductos · competidores", stroke=AZUL)
caja(2, 350, 130, "Eventos (6)", "escaneos · hallazgos · campañas\ncotizaciones · detalle\nbitácora", stroke=AZUL_OSC)
c.cilindro(xs[2] + 58, 504, cw - 116, 106, "PostgreSQL", "multi-negocio\npor negocio_id")

caja(3, 70, 106, "Triggers", "acumuladores de ventas\ny escaneos · bitácora")
caja(3, 206, 106, "Vista materializada", "resumen del dashboard\nREFRESH CONCURRENTLY")
caja(3, 342, 106, "Snapshot JSONB", "precios y reglas congelados\npor cotización", dash=True)
caja(3, 478, 106, "IA Gemini", "fricciones + catálogo\n→ alerta, post, guion", dash=True)

caja(4, 70, 84, "Dashboard (S05)", "KPIs en < 500 ms")
caja(4, 174, 84, "Hallazgos (S10)", "fricciones por competidor")
caja(4, 278, 84, "Campaña (S12)", "texto listo para copiar", dash=True)
caja(4, 382, 84, "Mensaje WhatsApp (S14)", "cotización formateada", dash=True)
caja(4, 486, 84, "Analítica (S16)", "conversión y ticket", dash=True)

for i in range(4):
    xa, xb = xs[i] + cw, xs[i + 1]
    c.flecha([(xa, 325), (xb, 325)], sw=2.4)

c.rect(30, 650, 1540, 120, fill=SUAVE, stroke=AZUL, rx=12)
c.texto(50, 678, "GOBIERNO DE DATOS", 13, 700, AZUL)
gob = [("Aislamiento", "cada consulta filtra por negocio_id"), ("Integridad", "FK con ON DELETE RESTRICT · CHECK de dominio"),
       ("Trazabilidad", "bitácora inmutable vía triggers"), ("Historia", "borrado lógico (activo = false) · snapshots"),
       ("Transparencia", "datos demo marcados (es_demo, aviso)")]
for i, (t, s) in enumerate(gob):
    x = 50 + i * 304
    c.texto(x, 712, t, 13, 700, TEXTO)
    c.texto(x, 734, s, 11.5, 400, GRIS)
c.guardar("datos.svg")
print("ok")
