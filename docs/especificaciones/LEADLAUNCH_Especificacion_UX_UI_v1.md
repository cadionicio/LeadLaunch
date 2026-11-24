# LEADLAUNCH — ESPECIFICACIÓN UX/UI
**Versión:** 1.0 · **Fecha:** 23 de noviembre de 2026 · **Producto:** LeadLaunch · **Plataforma objetivo:** Android nativo (Kotlin)
**Estado:** especificación de reproducción visual y diseño funcional; decisiones pendientes señaladas. No constituye aprobación de funcionalidades nuevas.

![Tablero de referencia visual con las 17 pantallas](sistema_ui_de_leadlaunch_en_español.png)

## 1. Propósito y alcance

Especificar la experiencia (UX), la interfaz (UI), los datos visibles y el comportamiento de las 17 pantallas del tablero visual LeadLaunch. Este documento sirve como contrato de reproducción del diseño para Figma y como referencia de implementación Android nativa en Kotlin. No es un prototipo funcional ni sustituye el diseño fuente editable.

## 2. Fuentes y criterio de fidelidad

Fuente funcional: ChambaDeNeon.pdf (19 páginas), que define misión, arquitectura SOA RESTful, esquema PostgreSQL, endpoints y HU-01 a HU-06. Fuente visual: imagen “sistema_ui_de_leadlaunch_en_español.png” generada en esta conversación con 17 pantallas. Las dimensiones/tokens numéricos no son extraíbles de la maqueta rasterizada: se proponen valores coherentes para replicarla; deben ajustarse por comparación visual en Figma.

## 3. Público y objetivo del producto

Usuario principal: emprendedor de negocio local que registra productos, identifica fricciones de competidores cercanos, adapta campañas y crea cotizaciones con cierre vía WhatsApp. Meta UX: pasar de una oportunidad identificada a una cotización compartida con el menor número de decisiones innecesarias, sin ocultar costos ni inventar datos.

## 4. Trazabilidad: base funcional vs. imagen
**Confirmado por PDF:** autenticación JWT; negocio, catálogo, políticas; radar y fricciones de reseñas ≤ 3 estrellas; IA para alerta/post/guion; cotizaciones paramétricas con snapshot; WhatsApp vía Intent; estados EMITIDA, ENVIADA_WA, GANADA, PERDIDA; métricas y vista materializada. **Referencia visual (no compromiso de backend):** 17 pantallas, navegación de 5 destinos, login Google, fotos de producto, gráficos de evolución, filtros y todos los microtextos de la maqueta. **Propuesto para reproducir la imagen:** dimensiones, tokens, validaciones visuales y componentes; verificar en prototipo antes de implementar.

## 5. Arquitectura de información y navegación
| Destino inferior | Pantallas | Comportamiento |
|---|---|---|
| Inicio | S05 | Dashboard; resumen de catálogo, fricciones y conversiones. |
| Catálogo | S06 | Lista de productos y acceso a creación/edición S07. |
| Cotizar | S13 / S15 | El diseño usa “Cotizar” para creación y también historial; propuesta: toque abre S15 con CTA “Nueva cotización” o abre S13 y acceso a historial. |
| Campañas | S11 | Generar desde fricciones y revisar S12. |
| Más | S17 | Perfil, políticas, notificaciones e integraciones. |

**Reglas:** bottom navigation sólo después del registro; estado de selección persistente; flecha volver usa historial interno; pulsar Back en destino raíz no debe perder cambios sin confirmar; cuando se abre S07, S10, S12 o S14, ocultar barra inferior si compromete espacio (propuesta a validar).

### Flujos principales
- **Flujo A · Primer acceso:** `S01 → S02 → S03 → S04 → S05`. Cuenta y negocio quedan operativos; en apertura futura S01 → S05 con JWT vigente.
- **Flujo B · Catálogo y tarifas:** `S05 → S06 → S07 → S06 → S08 → S05`. Los cambios aparecen en catálogo; reglas sólo impactan cotizaciones futuras.
- **Flujo C · Oportunidad a campaña:** `S05 → S09 → S10 → S11 → S12`. El usuario ve el origen de la fricción, genera tres bloques IA y revisa/copia su contenido.
- **Flujo D · Oportunidad a venta:** `S05/S13 → S13 → datos de prospecto → S14 → WhatsApp → S15 → S16`. El backend calcula importes, crea snapshot y marca GANADA únicamente al actualizar el estado.
- **Flujo E · Configuración:** `S05 → Más (S17) → S04 modo editar / S08`. Configuración consistente y guardada por negocio.

## 6. Sistema de diseño (tokens propuestos; calibrar contra imagen)
| Token | Valor de reconstrucción | Uso/notas |
|---|---|---|
| Color primario / CTA | `#175CF4` | Azul eléctrico de la referencia; estimación visual. |
| Primario oscuro | `#113CBA` | Marca, texto destacado y elevación de contraste. |
| Azul suave | `#EAF0FF` | Fondo de tarjetas de información y seleccionado. |
| Índigo secundario | `#7754E8` | Marcadores del mapa, chips de fricciones y campañas. |
| Fondo app | `#F8FAFE` | Lienzo de pantallas y tarjetas. |
| Superficie | `#FFFFFF` | Tarjetas, inputs, navegación y formularios. |
| Texto principal | `#16233E` | Titulares, importes y contenido principal. |
| Texto secundario | `#68738A` | Ayuda, metadata, placeholders. |
| Bordes | `#E3EAF4` | Inputs, separadores y tarjetas. |
| Éxito / WhatsApp | `#129E61` | Éxito y CTA WhatsApp; el verde real de WhatsApp puede utilizar su marca. |
| Error / fricción | `#D94D69` | Errores, pendientes y chips de dolor. |
| Tipografía | `Inter (propuesta)` | Alternativa Android: Roboto/Roboto Flex. La imagen no identifica una fuente exacta. |
| Base Android | `360 × 800 dp` | Diseño adaptable; soportar 320–430 dp y font scaling. |
| Retícula | `8 dp` | Espaciado 4, 8, 12, 16, 24, 32 dp. |
| Margen horizontal | `16 dp` | Contenido legible en móvil. |
| Radio de tarjeta | `12–16 dp` | Tarjetas amplias; chips 16–20 dp; inputs 8–12 dp. |
| Altura botones | `48–52 dp` | Área táctil >= 48 × 48 dp. |
| Tipografía de títulos | `22–24 sp, semibold/bold` | Títulos; 16–18 sp secciones; 14–16 sp cuerpo; 12 sp metadata. |
| Íconos | `Material Symbols Rounded / Lucide` | Elegir una sola familia consistente. |
| Sombras | `Elevación 1–3 dp` | Muy discretas; prioridad a borde suave y jerarquía. |

**Jerarquía:** título de pantalla 22–24 sp; título de tarjeta 16–18 sp; cuerpo 14–16 sp; metadata 12–13 sp. **Retícula:** 8 dp. **Accesibilidad:** contraste WCAG AA en texto, escalado de fuentes, targets ≥48 dp, mensajes de error accesibles, etiquetas descriptivas en iconos y no depender sólo del color. **Estados:** diseñar pantallas de 360×800 dp como baseline; probar 320, 393 y 430 dp y modo texto aumentado. **Moneda:** `S/ 75.00` como ejemplo PEN, importes siempre formateados en el cliente de forma consistente.

### Biblioteca de componentes
| Componente | Especificación | Variantes |
|---|---|---|
| AppBar | Flecha de retorno, título de pantalla, opcional campana/búsqueda. Top-level: saludo y avatar en Inicio. | Default; con back; con acción; título largo. |
| Bottom Navigation | 5 destinos: Inicio, Catálogo, Cotizar, Campañas, Más; ícono 22–24dp + etiqueta. | Activo; inactivo; badge opcional. |
| PrimaryButton | Azul pleno, texto blanco semibold, 48–52dp; ancho completo en formularios. | Enabled; pressed; disabled; loading. |
| WhatsAppButton | Verde, ícono WhatsApp y texto “Enviar por WhatsApp”. | Activo; sin número; sin app instalada. |
| TextField | Label externo, input con borde gris, ícono anterior cuando aporte; error debajo. | Vacío; foco; completo; inválido; deshabilitado. |
| CardKPI | Número grande, etiqueta pequeña, tendencia opcional con color semántico. | Con dato; sin histórico; cargando. |
| ProductRow | Foto 48–56dp, nombre, SKU, precio/stock, menú de acciones. | Normal; sin stock; inactivo; skeleton. |
| FilterChip | Píldora con etiqueta y recuento opcional; colores de selección primario. | Seleccionado; no seleccionado; disabled. |
| FrictionChip | Píldora índigo/rosa por tipo, botón de quitar sólo en selección. | Observación; seleccionada; deseleccionada. |
| SwitchRule | Icono de política, nombre, explicación y switch accesible. | On; Off; guardando; error. |
| QuoteLine | Miniatura, checkbox, nombre, precio, control stepper de cantidad. | Seleccionada; no seleccionada; sin stock. |
| StatusBadge | Etiqueta corta con tono semántico: Emitida, Enviada, Ganada, Perdida. | Neutral; info; success; danger. |
| EmptyState | Ilustración discreta, título y 1 CTA relacionado; nunca confundir con fallo. | No items; no findings; no results. |
| Feedback / Toast | Mensajes de confirmación, snackbar de error con “Reintentar”. | Éxito; error; advertencia. |

## 7. Fichas de las 17 pantallas — contrato visual y de interacción
**Convención de fidelidad:** los textos, estructuras y valores citados corresponden a la imagen demostrativa. Para datos funcionales se debe usar información real del negocio; los cambios y aclaraciones respecto del PDF están expresamente marcados.
### S01 — Splash / bienvenida
**Ruta conceptual:** `S01 · /splash` · **Clasificación:** Ampliación UX
- **Objetivo:** Presentar la marca y dirigir al acceso.
- **Composición visual:** Pantalla azul degradada, cohete blanco como isotipo, logotipo LeadLaunch centrado, ilustración comercial (tienda, bolsa, pin y crecimiento), frase de valor y tres indicadores de página. Sin barra inferior de navegación.
- **Microcopy/valores de maqueta:** LeadLaunch · “Tu aliado para vender más, diferenciarte de la competencia y convertir más clientes.”
- **Campos y datos:** No contiene campos de entrada.
- **Interacciones y navegación:** Al entrar: comprobar sesión local válida y estado de registro del negocio. Si existe sesión válida, abrir S05; en caso contrario, S02. Se conserva el aspecto gráfico de la referencia.
- **Reglas y validación:** No mostrar datos privados ni realizar consultas críticas antes de autenticar. El indicador de 3 puntos se considera decorativo hasta decidir si existirá un onboarding multipágina.
- **Estados de UX:** Inicial, transición, sesión recuperada, sin sesión.
- **Integración:** Validación local de token y, si corresponde, autenticación existente. No requiere endpoint propio.
- **Criterio de aceptación:** Al abrir sin sesión, el usuario llega a “Iniciar sesión”; al abrir con sesión válida y negocio configurado, llega a “Inicio”.
- **Diferencia o pendiente:** La imagen muestra 3 puntos pero el documento base no define 3 diapositivas de onboarding.

### S02 — Iniciar sesión
**Ruta conceptual:** `S02 · /auth/login` · **Clasificación:** Base + extensión visual
- **Objetivo:** Autenticar al emprendedor sin sobrecargar el ingreso.
- **Composición visual:** Fondo blanco; logotipo pequeño superior; título “Iniciar sesión”; campos con íconos; enlace de recuperación; CTA azul de ancho completo; divisor “o continúa con”; botón blanco de Google; enlace a crear cuenta.
- **Microcopy/valores de maqueta:** “Accede a tu cuenta y sigue haciendo crecer tu negocio.” · “Correo electrónico” · “Contraseña” · “¿Olvidaste tu contraseña?” · “Continuar con Google” · “Crear cuenta”.
- **Campos y datos:** Correo electrónico (requerido, tipo email); contraseña (requerida, ocultable).
- **Interacciones y navegación:** Ingresar credenciales → “Iniciar sesión” → obtener JWT → verificar existencia de negocio → S04 o S05. Enlace “Crear cuenta” → S03. Mostrar/ocultar contraseña mediante ícono ojo.
- **Reglas y validación:** Correo válido; contraseña no vacía; deshabilitar el CTA durante la petición; errores de credenciales sin revelar qué dato es incorrecto. “Olvidaste tu contraseña” y Google son acciones visualizadas, no implementadas en la API documentada.
- **Estados de UX:** Vacío, escribiendo, válido, cargando, 401, sin internet, autenticado.
- **Integración:** POST /api/v1/auth/login (confirmado).
- **Criterio de aceptación:** La autenticación correcta conduce a negocio/Inicio; el fallo informa “No pudimos iniciar sesión. Revisa tus datos.”
- **Diferencia o pendiente:** Login con Google y recuperación de contraseña requieren endpoints y decisiones propias.

### S03 — Crear cuenta
**Ruta conceptual:** `S03 · /auth/register` · **Clasificación:** Base + propuesta de criterios
- **Objetivo:** Dar de alta una cuenta con validaciones claras.
- **Composición visual:** Ícono de cohete en cabecera y forma geométrica tenue; título “Crear cuenta”; campos verticales con bordes grises; lista de criterios con checks verdes; CTA azul y enlace a iniciar sesión.
- **Microcopy/valores de maqueta:** “Regístrate en LeadLaunch y configura tu negocio en minutos.” · “Nombre completo” · “Correo electrónico” · “Contraseña” · “Crear cuenta”.
- **Campos y datos:** Nombre completo obligatorio; correo electrónico único obligatorio; contraseña obligatoria. Los checks visibles en maqueta son: mínimo 8 caracteres, una mayúscula y un número.
- **Interacciones y navegación:** La persona ingresa datos; los checks se actualizan en vivo; toca “Crear cuenta”; si el servidor responde 201, guardar sesión y abrir S04; “Iniciar sesión” → S02.
- **Reglas y validación:** Usar los criterios de contraseña de la maqueta como propuesta a confirmar con backend; validar unicidad del correo mediante respuesta de servidor; jamás guardar contraseña en claro.
- **Estados de UX:** Inicial, requisitos satisfechos/parciales, enviando, cuenta existente, éxito, error temporal.
- **Integración:** POST /api/v1/auth/register (confirmado).
- **Criterio de aceptación:** El botón únicamente envía el formulario válido; si el correo ya existe, se informa junto al campo y se preserva el resto de datos.
- **Diferencia o pendiente:** Los requisitos exactos de complejidad de contraseña no están fijados en el DDL ni en los contratos.

### S04 — Registrar negocio
**Ruta conceptual:** `S04 · /business/onboarding` · **Clasificación:** Base + ajuste para DDL
- **Objetivo:** Configurar una empresa y su área de operación.
- **Composición visual:** Pantalla blanca con cohete superior, título “Registrar negocio”; campos con íconos de persona/tienda, categoría, pin, WhatsApp y radio; CTA “Guardar y continuar”.
- **Microcopy/valores de maqueta:** “Cuéntanos sobre tu negocio para personalizar tu experiencia.” · “Nombre del negocio” · “Categoría” · “Ubicación” · “WhatsApp del negocio” · “Radio de delivery”.
- **Campos y datos:** Razón comercial; rubro; ubicación; WhatsApp (10–15 dígitos según esquema, sin signos); distrito/latitud/longitud; radio de cobertura de 0,50 a 25,00 km. Añadir descripción obligatoria del negocio según DDL, aunque no se ve en la imagen.
- **Interacciones y navegación:** Elegir rubro; seleccionar ubicación (mapa/geocodificación propuestos); definir radio; guardar negocio y políticas base; navegar a S05.
- **Reglas y validación:** Si no se obtienen coordenadas, no permitir enviar un punto ficticio; aclarar país/código de WhatsApp; radio y teléfono deben cumplir formato de backend.
- **Estados de UX:** Edición, mapa cargando, dirección no encontrada, permisos de ubicación denegados, guardando, guardado.
- **Integración:** POST /api/v1/businesses (confirmado).
- **Criterio de aceptación:** El alta completa persiste rubro, razón comercial, descripción, distrito, coordenadas, número y radio.
- **Diferencia o pendiente:** La maqueta usa “radio de delivery”; el esquema lo usa como radio competitivo. Separar conceptos o renombrarlo a “Radio del radar” según decisión.

### S05 — Dashboard / Inicio
**Ruta conceptual:** `S05 · /home` · **Clasificación:** Base + KPIs ampliados
- **Objetivo:** Entregar una visión inmediata del desempeño comercial.
- **Composición visual:** Cabecera con avatar de tienda, “¡Hola, Ana!” y “Dulce Punto”; campana; tarjetas 2×2 y tarjeta amplia de última campaña; tendencia miniatura azul, variaciones verdes y módulo “Resumen del mes”; barra inferior.
- **Microcopy/valores de maqueta:** “Productos activos” (24) · “Fricciones recientes” (3) · “Última campaña” · “Tasa de conversión” (36%) · “Ventas ganadas” (S/ 2,340).
- **Campos y datos:** Solo filtros de período o enlaces a detalle; no existen formularios.
- **Interacciones y navegación:** Tocar productos → S06; fricciones → S10; última campaña → S12 si existe; “Resumen del mes” → S16; campana → centro de notificaciones (no diseñado).
- **Reglas y validación:** Tratar valores como demostrativos; cuando no hay ventas usar 0% y S/ 0.00; indicar fecha del dato y estado de actualización; no inventar cambios porcentuales si no existe histórico.
- **Estados de UX:** Cargando con skeleton, datos disponibles, primera visita sin datos, actualización fallida con último valor disponible.
- **Integración:** GET /api/v1/businesses/{id}/dashboard (confirmado); serie temporal y variaciones necesitan API adicional.
- **Criterio de aceptación:** Al entrar con negocio activo, se muestran productos, fricciones, última alerta, cotizaciones ganadas y tasa de conversión; el acceso a los módulos funciona.
- **Diferencia o pendiente:** El dashboard original sólo establece datos agregados; mini-gráficos y variaciones comparativas no están especificados en contrato.

### S06 — Catálogo de productos
**Ruta conceptual:** `S06 · /catalog` · **Clasificación:** Base + vista ampliada
- **Objetivo:** Consultar rápidamente los productos y su disponibilidad.
- **Composición visual:** Título “Catálogo”; buscador; botón filtro; chips “Todos (24)”, “Activos (24)”, “Sin stock (0)”; tarjetas de producto con foto, nombre, SKU, precio S/, stock, menú contextual y FAB + azul.
- **Microcopy/valores de maqueta:** Ejemplos de referencia: “Torta de chocolate” S/ 75.00 · “Cheesecake de frutos” S/ 65.00 · “Cupcake (x6)” S/ 35.00 · “Postre individual” S/ 12.00.
- **Campos y datos:** Buscar texto; filtro de estado; acción contextual por producto. Los datos visibles no representan inventario real.
- **Interacciones y navegación:** Buscar/filtrar; tocar tarjeta → S07; FAB → S07 en modo crear; menú de 3 puntos → editar/desactivar (acciones a precisar).
- **Reglas y validación:** Mostrar producto sin imagen con placeholder; precio > 0; stock >= 0; SKU único por negocio; desactivación lógica para conservar citas históricas.
- **Estados de UX:** Cargando, lista, búsqueda vacía, sin productos, error, actualización.
- **Integración:** GET /api/v1/businesses/{id}/products; POST /api/v1/businesses/{id}/products (confirmados).
- **Criterio de aceptación:** El catálogo se pagina; un producto sin stock se reconoce; crear conduce al formulario y después a la lista.
- **Diferencia o pendiente:** Imágenes, filtros y edición/desactivación precisan atributos/endpoints adicionales; el DDL no contiene campo de foto.

### S07 — Agregar / editar producto
**Ruta conceptual:** `S07 · /catalog/product` · **Clasificación:** Base + edición visual
- **Objetivo:** Crear y mantener un producto con reglas de disponibilidad.
- **Composición visual:** Título “Editar producto”; foto con botón de cámara; SKU; campos precio base y stock en una fila; tiempo de preparación selector; switch express; CTA fijo “Guardar cambios”.
- **Microcopy/valores de maqueta:** Ejemplo: “Torta de chocolate” · “TOR-001” · “S/ 75.00” · “8” · “45 min” · “¿Disponible en modalidad express?”.
- **Campos y datos:** Nombre; SKU; descripción (adicional del DDL); precio base > 0; stock >= 0; tiempo de preparación; permite express; foto (visual/pendiente).
- **Interacciones y navegación:** Modo nuevo: campos vacíos, CTA “Agregar producto”. Modo editar: datos precargados, CTA “Guardar cambios”. Tras confirmar, volver S06 mostrando cambio.
- **Reglas y validación:** Precio no puede ser cero/negativo; SKU duplicado bloqueado; stock entero >= 0; advertir cambios sin guardar al salir.
- **Estados de UX:** Nuevo, edición, modificado, validación fallida, guardando, guardado, error.
- **Integración:** POST /api/v1/businesses/{id}/products (confirmado). GET detalle y PATCH/PUT de producto (pendientes).
- **Criterio de aceptación:** Los cambios válidos se reflejan en catálogo y no reescriben snapshots de cotizaciones antiguas.
- **Diferencia o pendiente:** La maqueta indica “45 min” pero BD guarda tiempo_prep_horas SMALLINT >= 1. Debe corregirse unidad de datos o diseño.

### S08 — Reglas de precios
**Ruta conceptual:** `S08 · /settings/rules` · **Clasificación:** Base + pagos ilustrativos
- **Objetivo:** Administrar políticas comerciales sin cálculos manuales.
- **Composición visual:** Título “Reglas de precios”; tarjetas separadas: recargo express con rayo y switch 15%, descuento de bienvenida con regalo y switch 10%, medios de pago con casillas. Barra inferior presente.
- **Microcopy/valores de maqueta:** “Recargo por express” · “Aplica un recargo por preparación prioritaria” · “Descuento de bienvenida” · “Medios de pago”.
- **Campos y datos:** Permite express (booleano); valor porcentual recargo; descuento bienvenida; política de acumulación; acepta Yape/Plin. Las tarifas por medios de pago de la imagen son ilustrativas.
- **Interacciones y navegación:** Cambiar switches y porcentajes, guardar mediante interacción explícita o autoguardado (por definir); mostrar confirmación “Reglas actualizadas”.
- **Reglas y validación:** Porcentajes entre 0 y 100; las reglas aplican a cotizaciones nuevas; las cotizaciones emitidas mantienen precio y reglas del momento.
- **Estados de UX:** Cargado, editando, inválido, guardando, guardado, error con reversión.
- **Integración:** PUT /api/v1/businesses/{id}/rules (confirmado).
- **Criterio de aceptación:** Actualizar recargo 15% modifica futuras cotizaciones, no cotizaciones previas.
- **Diferencia o pendiente:** Tarjeta 2,5% y Yape/Plin -3% no están modelados en enum de reglas; definir si se eliminan o amplía motor.

### S09 — Radar local / mapa
**Ruta conceptual:** `S09 · /radar` · **Clasificación:** Base
- **Objetivo:** Ejecutar un escaneo geográfico de competidores.
- **Composición visual:** Cabecera “Radar local”, subtítulo descriptivo, mapa predominante de calles claras, círculo azul semitransparente de cobertura, marcador central de tienda y pines violetas; selector “Radio: 5 km”; CTA azul “Escanear competencia”.
- **Microcopy/valores de maqueta:** “Descubre y analiza a tus competidores cercanos en tu zona.” · “Escanear competencia” · “Analiza reseñas, precios y fricciones de negocios similares.”
- **Campos y datos:** Radio de búsqueda en kilómetros (0,5–25). Centro: ubicación registrada del negocio; no ubicación personal automática salvo decisión.
- **Interacciones y navegación:** Seleccionar radio → dibujar círculo → iniciar escaneo → mostrar progreso → S10 resultados. Si API externa falla, aceptar respaldo controlado y avisar que son datos de referencia.
- **Reglas y validación:** No prometer resultados en tiempo real si vienen del dataset de respaldo; marcar fecha/fuente y permiso de ubicación cuando corresponda.
- **Estados de UX:** Mapa cargando, vacío, escaneando, escaneo completado, sin hallazgos, fallback, error.
- **Integración:** POST /api/v1/businesses/{id}/radar/scan (confirmado).
- **Criterio de aceptación:** El usuario identifica visualmente su radio; un escaneo válido genera un resultado consultable.
- **Diferencia o pendiente:** La imagen usa mapa ilustrativo; el proveedor de mapas y sus condiciones de uso son decisiones técnicas pendientes.

### S10 — Hallazgos y fricciones
**Ruta conceptual:** `S10 · /radar/findings` · **Clasificación:** Base + exploración
- **Objetivo:** Convertir opiniones de competidores en problemas del mercado que se puedan atender.
- **Composición visual:** Título “Hallazgos del radar”; subtítulo de negocios encontrados y radio; pestañas “Competidores” y “Fricciones” (esta activa en referencia); tarjetas con imagen de negocio, estrellas, puntaje, conteo de reseñas, chips de fricción y chevrón; resumen inferior con contadores.
- **Microcopy/valores de maqueta:** Ejemplos: “La Dulcería” 2.8 · “Postres & Más” 3.1 · “Sweet House” 2.9; etiquetas “Demora”, “Precio alto”, “Sin stock”, “Mala atención”.
- **Campos y datos:** Tab activos, opcional filtro por tipo de fricción; no capturar comentarios editables.
- **Interacciones y navegación:** Cambiar pestaña; seleccionar competidor → detalle de sus reseñas (variante no ilustrada); seleccionar fricción para alimentar S11; regresar S09.
- **Reglas y validación:** El filtro de reseñas aplica a reseñas individuales <= 3 estrellas; el promedio de un competidor puede superar 3. Etiquetar fecha y fuente de datos.
- **Estados de UX:** Cargando, competidores, fricciones, sin quejas, fuente externa no disponible.
- **Integración:** Resultado de /radar/scan y persistencia en hallazgos_friccion (confirmados); GET histórico/detalle pendiente.
- **Criterio de aceptación:** Se distingue la calificación del local de las reseñas negativas clasificadas y se pueden utilizar fricciones en campañas.
- **Diferencia o pendiente:** Falta endpoint de consulta de escaneos/hallazgos posteriores, además de detalle de competidor.

### S11 — Crear campaña con IA
**Ruta conceptual:** `S11 · /campaigns/new` · **Clasificación:** Base + parámetros visuales
- **Objetivo:** Preparar la generación de contenido contextual.
- **Composición visual:** Título “Campañas con IA”; texto de apoyo; chips seleccionados de fricción con X; campo “Objetivo de la campaña”; tarjeta de “Canal principal”; selector “Tono de comunicación”; botón azul con estrella “Generar campaña con IA”.
- **Microcopy/valores de maqueta:** Chips “Demora”, “Precio alto”, “Sin stock”; objetivo “Atraer más pedidos”; canal “Redes sociales (Instagram, Facebook, TikTok)”; tono “Cercano y confiable”.
- **Campos y datos:** Fricciones seleccionadas (1+); objetivo de campaña; canal principal; tono.
- **Interacciones y navegación:** Preseleccionar fricciones de S10; permitir quitar; generar → loader no bloqueante con progreso textual → S12; botón desactivado si no hay hallazgos.
- **Reglas y validación:** No llamar IA repetidamente por taps dobles; advertir si se va a enviar texto de reseñas a un tercero conforme a política de datos; la campaña debe basarse en evidencia real.
- **Estados de UX:** Sin hallazgos, lista, generando, generado, timeout, cuota agotada, error reintentable.
- **Integración:** POST /api/v1/businesses/{id}/campaigns/generate (confirmado).
- **Criterio de aceptación:** Con 1+ fricciones válidas, el servicio devuelve una alerta, un post y un guion; el resultado queda guardado.
- **Diferencia o pendiente:** Objetivo, canal y tono no están en el esquema de campanas_adaptativas; pueden ser sólo parámetros de solicitud o requerir persistencia.

### S12 — Resultado de campaña IA
**Ruta conceptual:** `S12 · /campaigns/result` · **Clasificación:** Base + vista de salida
- **Objetivo:** Revisar, copiar y aprovechar el contenido generado.
- **Composición visual:** Título “Campaña generada”; aviso verde “Campaña adaptativa lista”; sección “Post para redes sociales” con miniatura de postre y texto; “Copiar texto”; sección “Guion para reel (15–30 s)” con miniatura de vídeo, pasos numerados y “Copiar guion”.
- **Microcopy/valores de maqueta:** Ejemplo de post: “¿Antojo de postre? En Dulce Punto te atendemos rápido, con precios justos y siempre con stock. ¡Haz tu pedido hoy!”
- **Campos y datos:** Alerta adaptativa; copy publicitario; guion del reel; estado de la campaña (Borrador/Publicada/Archivada).
- **Interacciones y navegación:** Copiar texto y guion al portapapeles; volver a ajustar campaña; guardar o publicar (si se incorpora el flujo); no publicar automáticamente en redes sociales.
- **Reglas y validación:** Texto IA editable y revisable antes de publicar; evitar afirmaciones no verificadas (“siempre con stock”) si inventario no respalda; accesibilidad para texto largo.
- **Estados de UX:** Contenido, copia exitosa, contenido muy largo, fallo de generación, borrador archivado.
- **Integración:** campaigns/generate confirmado; GET/PATCH campaña individual y publicación pendientes.
- **Criterio de aceptación:** El usuario copia individualmente el post o guion y distingue claramente el contenido generado de una publicación real.
- **Diferencia o pendiente:** El DDL llama guion_reel_15s, mientras la imagen etiqueta “15–30 s”: duración por unificar.

### S13 — Nueva cotización
**Ruta conceptual:** `S13 · /quotes/new` · **Clasificación:** Base + paso faltante
- **Objetivo:** Armar un pedido sin calculadora externa.
- **Composición visual:** Título “Nueva cotización”; descripción; buscador; listado con checkbox, miniatura, precio y control de cantidad; pie con contador “3 productos seleccionados” y CTA “Continuar”.
- **Microcopy/valores de maqueta:** Productos de ejemplo: Torta de chocolate 1 × S/ 75; Cheesecake de frutos 2 × S/ 65; cupcake y postre individual sin seleccionar.
- **Campos y datos:** Cliente nombre (en paso de datos antes de emitir), número WhatsApp opcional según BD pero práctico para envío; productos; cantidades >0; marca de express; cliente nuevo.
- **Interacciones y navegación:** Buscar → seleccionar 1+ productos → modificar unidades → continuar → capturar datos de cliente y condiciones (paso/intermedio no ilustrado) → S14.
- **Reglas y validación:** Al menos 1 línea; cantidad positiva, stock suficiente si se pretende reserva de existencias; no asumir descuento para todos los clientes; bloquear express si política lo deshabilita.
- **Estados de UX:** Catálogo, sin selección, selección válida, stock insuficiente, cargando.
- **Integración:** POST /api/v1/businesses/{id}/quotes (confirmado, al confirmar emisión).
- **Criterio de aceptación:** La cantidad seleccionada determina subtotales; se solicita el nombre del prospecto antes de crear la cotización.
- **Diferencia o pendiente:** El diseño no muestra captura de prospecto/express/nuevo cliente; la implementación necesita incorporarla en un paso, hoja modal o S14.

### S14 — Vista previa de cotización
**Ruta conceptual:** `S14 · /quotes/preview` · **Clasificación:** Base + integración nativa
- **Objetivo:** Validar montos y abrir WhatsApp con mensaje listo.
- **Composición visual:** Título “Cotización #COT-0278”; chip verde “Lista para enviar”; tarjeta de cliente; desglose de productos; subtotal; recargo express; descuento bienvenida; total neto en negrita; botón verde con ícono WhatsApp y acción secundaria “Guardar cotización”.
- **Microcopy/valores de maqueta:** Ejemplo visual: subtotal S/ 205.00; recargo 15% = S/ 30.75; descuento 10% = S/ 20.50; total neto = S/ 215.25.
- **Campos y datos:** Nombre, teléfono, detalle, subtotal, recargos, descuentos, total, mensaje. Montos calculados por backend; snapshot inmutable.
- **Interacciones y navegación:** “Guardar cotización” crea EMITIDA si aún no existe; “Enviar por WhatsApp” crea si hace falta, abre Intent de WhatsApp con número y mensaje. Gestión posterior del estado ENVIADA_WA según confirmación establecida.
- **Reglas y validación:** Equivalencia 205 + 30,75 - 20,50 = 215,25 es correcta. Pero, si permite_acumular_desc = FALSE, decidir si recargo + descuento pueden coexistir (no necesariamente son dos descuentos). Evitar doble emisión por reintentos.
- **Estados de UX:** Previsualizando, generando, lista, app WhatsApp no disponible, mensaje compartido, error de total.
- **Integración:** POST /api/v1/businesses/{id}/quotes + PATCH /api/v1/quotes/{quote_id}/status (confirmados).
- **Criterio de aceptación:** Total consistente con snapshot; se abre WhatsApp con destinatario y texto precargado, sin afirmar envío antes de confirmación.
- **Diferencia o pendiente:** La imagen ilustra 2 líneas de productos con 3 unidades; S13 dice “3 productos seleccionados”, ambigüedad entre SKU y unidades.

### S15 — Historial de cotizaciones
**Ruta conceptual:** `S15 · /quotes/history` · **Clasificación:** Base + consulta faltante
- **Objetivo:** Hacer seguimiento de oportunidades comerciales.
- **Composición visual:** Título “Cotizaciones”; lupa; chips “Todas (28)”, “Emitida (6)”, “Enviada (8)”, “Ganada (9)”; filas con número, prospecto, monto, fecha y chip de estado; barra inferior activa en “Cotizar”.
- **Microcopy/valores de maqueta:** Referencias: COT-0278 María Torres Enviada; COT-0277 Carlos Mendoza Ganada; COT-0276 Lucía Prado Emitida; COT-0275 Jorge Rivas Perdida.
- **Campos y datos:** Filtro estado; búsqueda por código o prospecto; lista histórica.
- **Interacciones y navegación:** Abrir cotización para detalle/seguimiento; marcar Ganada o Perdida desde un menú contextual o detalle; opción para reenviar WhatsApp según reglas.
- **Reglas y validación:** Las cotizaciones históricas no cambian ante edición de precios; cambiar estados debe requerir confirmación si afecta ventas acumuladas; listado de ejemplo no constituye datos reales.
- **Estados de UX:** Lista, filtro vacío, cargando más, actualización de estado, error.
- **Integración:** PATCH /api/v1/quotes/{quote_id}/status confirmado; GET historial y detalle pendientes.
- **Criterio de aceptación:** La persona puede localizar una cotización y cambiar su estado; los indicadores del dashboard se recalculan.
- **Diferencia o pendiente:** El contrato de API sólo cubre creación y cambio de estado; requiere GET de historial y GET detalle.

### S16 — Resultados y conversión
**Ruta conceptual:** `S16 · /analytics` · **Clasificación:** Base + módulo analítico ampliado
- **Objetivo:** Medir cotizaciones, tasa de cierre y ventas atribuidas al sistema.
- **Composición visual:** Título “Resultados y conversión”; selector “Últimos 30 días”; 4 tarjetas de KPI y gráfica azul de evolución de conversión con dos series (Cotizaciones / Ganadas), eje temporal.
- **Microcopy/valores de maqueta:** Ejemplo de referencia: 28 cotizaciones; 36% conversión; S/ 2,340 ventas ganadas; S/ 130 ticket promedio.
- **Campos y datos:** Rango temporal (7/30/90 días propuesto); KPIs y series cronológicas.
- **Interacciones y navegación:** Cambiar período; tocar serie para ver valores; permitir estado vacío con explicaciones de cálculo.
- **Reglas y validación:** Tasa = (total ganadas / total emitidas) × 100, con cero si denominador=0; ventas ganadas se agregan solo por estado GANADA; ticket promedio debe definir denominador exacto.
- **Estados de UX:** Cargando, con datos, sin ventas, sin red, período no disponible.
- **Integración:** Dashboard agregado confirmado; serie histórica, ticket promedio y comparación por período necesitan contrato nuevo.
- **Criterio de aceptación:** La tasa y el monto coinciden con la cuenta del negocio; los gráficos se basan en datos históricos, no en curvas inventadas.
- **Diferencia o pendiente:** La maqueta muestra un eje con fechas “ene”; esas fechas son ficticias y deben adaptarse al período seleccionado.

### S17 — Mi perfil y configuración
**Ruta conceptual:** `S17 · /settings` · **Clasificación:** Base + ajustes de cuenta
- **Objetivo:** Centralizar datos de cuenta, negocio y opciones de la app.
- **Composición visual:** Título “Mi perfil”; avatar de tienda y “Ana Rodríguez / Dulce Punto”; enlace “Ver y editar perfil”; filas con íconos y chevron para negocio, reglas, notificaciones, integraciones, ayuda.
- **Microcopy/valores de maqueta:** “Configuración del negocio” · “Reglas de precios” · “Notificaciones” · “Integraciones” · “Ayuda y soporte”.
- **Campos y datos:** Perfil y negocio (datos existentes); preferencia de notificaciones; acceso a integraciones.
- **Interacciones y navegación:** Configuración del negocio → S04 en modo edición; reglas → S08; notificaciones on/off; integraciones y ayuda → flujos no ilustrados; cerrar sesión como acción adicional necesaria.
- **Reglas y validación:** No mezclar datos de varios negocios; limpiar token al cerrar sesión; no declarar conectadas redes sociales si sólo se generan copies.
- **Estados de UX:** Perfil listo, guardando preferencia, sesión expirada, error de carga.
- **Integración:** Modelo usuarios/negocios confirmado; GET/PATCH de perfil/negocio y preferencias pendientes.
- **Criterio de aceptación:** Desde perfil se accede a configuraciones; se puede cerrar sesión y proteger el acceso con token.
- **Diferencia o pendiente:** La UI no ilustra cierre de sesión, selección multi-negocio o edición de cuenta; documentar decisión para implementación.

## 8. Estados transversales y microinteracciones
- Carga: skeleton en tarjetas/listas; spinner con texto contextual en generación de IA y escaneo; bloquear taps duplicados sin bloquear navegación innecesaria.
- Sin datos: diferenciar “Aún no has agregado productos” (CTA Agregar producto), “No se encontraron fricciones” (CTA Volver a escanear) y “Aún no hay cotizaciones” (CTA Nueva cotización).
- Errores: distinguir 400 (regla de negocio), 401 (sesión), 404 (recurso ajeno o inexistente), 422 (campo inválido), 5xx/sin conexión (reintentar). No mostrar JSON al usuario.
- Confirmación: snackbar para cambios no críticos; confirmación modal para marcar una cotización Ganada/Perdida si afecta la analítica.
- Accesibilidad: el estado seleccionado tiene ícono/indicador además de color; rangos de porcentaje legibles por lector de pantalla.
- Privacidad: no incluir credenciales de API del motor IA en cliente; proteger JWT y eliminar sesión al cerrar.

## 9. Datos demostrativos para reproducir el mockup
Estos datos se ofrecen **exclusivamente para la maqueta/prototipo**; no representan observaciones verificadas ni cifras de un negocio real.

| Entidad | Datos para semilla de UI |
|---|---|
| Usuario/negocio | Ana Rodríguez; Dulce Punto; categoría Alimentos y bebidas; ejemplo en Miraflores; WhatsApp +51 987 654 321; radio visible 5 km. |
| Productos | Torta de chocolate TOR-001 S/75 stock 8; Cheesecake de frutos CHE-002 S/65 stock 10; Cupcake (x6) CUP-006 S/35 stock 20; Postre individual POS-011 S/12 stock 30. |
| Reglas | Recargo express 15%; descuento bienvenida 10%; ambos visibles en captura; medios de pago adicionales sólo demostrativos. |
| Radar | La Dulcería; Postres & Más; Sweet House; fricciones de demora, precio alto, falta de stock, mala atención. |
| Cotización | COT-0278 para María Torres; 1×S/75 + 2×S/65 = S/205; recargo 15% S/30.75; descuento 10% S/20.50; neto S/215.25. |
| Indicadores | 24 productos; 3 fricciones recientes; 28 cotizaciones; 36% conversión; S/2,340 ventas ganadas; ticket promedio S/130; valores aislados de la imagen. |

**Importante:** la pantalla S13 muestra “3 productos seleccionados”, pero la cotización S14 contiene 2 SKUs y 3 unidades. Se propone cambiar el contador a “3 unidades” o hacer coincidir el contenido de ambos estados en una revisión de fidelidad.

## 10. Matriz UI → API y brechas de implementación
Rutas mostradas de forma abreviada respecto de `/api/v1`. Disponible = definido en el PDF; parcial = definido pero insuficiente para toda la pantalla.
| UI | Endpoint | Cobertura | Observación |
|---|---|---|---|
| S02 | `POST /auth/login` | Disponible | JWT; gestionar 401 y expiración |
| S03 | `POST /auth/register` | Disponible | Cuenta nueva |
| S04 | `POST /businesses` | Disponible | Alta de negocio |
| S05 | `GET /businesses/{id}/dashboard` | Disponible | KPIs base, no series históricas |
| S06 | `GET /businesses/{id}/products` | Disponible | Catálogo paginado |
| S07 | `POST /businesses/{id}/products` | Parcial | Falta PATCH/PUT de edición |
| S08 | `PUT /businesses/{id}/rules` | Disponible | Políticas y reglas |
| S09/S10 | `POST /businesses/{id}/radar/scan` | Parcial | Falta GET resultados históricos |
| S11/S12 | `POST /businesses/{id}/campaigns/generate` | Parcial | Falta GET campaña/historial |
| S13/S14 | `POST /businesses/{id}/quotes` | Disponible | Emitir, snapshot y mensaje WhatsApp |
| S15 | `PATCH /quotes/{quote_id}/status` | Parcial | Falta GET lista/detalle de quotes |
| S16 | `GET /businesses/{id}/dashboard` | Parcial | Falta API de series/períodos |
| S17 | `Sin GET/PATCH de cuenta/negocio` | Pendiente | Actualizar perfil, preferencias, logout |

## 11. Pendientes de producto — NO son decisiones aprobadas
- **P01 · Autenticación social y recuperar contraseña:** El mockup muestra Google y enlace; no hay contratos. Decidir si implementarlos ahora o representarlos deshabilitados.
- **P02 · Unidad de preparación:** S07 muestra 45 min; base usa SMALLINT horas >=1. Definir minutos o fracciones de horas en modelo y API.
- **P03 · Ubicación vs radio de delivery:** El documento base parametriza radar por km; S04 lo titula delivery. Decidir si son 2 radios independientes.
- **P04 · Imágenes de catálogo:** La pantalla tiene fotos; BD de productos carece de imagen_url/archivo. Definir almacenamiento, permisos y placeholder.
- **P05 · Búsqueda/edición/inactivación productos:** Endpoints sólo GET lista y POST creación; agregar lectura individual, edición y desactivación lógica.
- **P06 · Reglas por método de pago:** La referencia tiene tarjeta 2,5% y Yape/Plin -3%; enum no soporta esas reglas. Definir soporte o retirarlas.
- **P07 · Cliente y condiciones de cotización:** S13 no muestra datos del prospecto, express ni cliente nuevo. Elegir modal/paso para capturarlos.
- **P08 · Estado “enviada” en WhatsApp:** Un Intent prellenado no confirma que se envió. Definir si la app pide confirmación explícita para ENVIADA_WA.
- **P09 · Rango de guion IA:** Imagen: “15–30 s”; BD: guion_reel_15s. Unificar alcance.
- **P10 · Histórico e informes:** Faltan endpoints de listas de cotizaciones, campañas, hallazgos y analítica temporal.
- **P11 · Descuentos y acumulación:** Modelar reglas elegibles y su prioridad; aclarar compatibilidad entre recargo y descuento y entre descuentos.
- **P12 · Elección de navegación Cotizar:** La misma pestaña representa creación e historial en la imagen; decidir destino principal.
- **P13 · Multi-negocio:** Arquitectura permite varios negocios aislados, pero UI muestra uno; decidir selector, gestión y permisos.
- **P14 · Validación de datos del radar:** Marcar fuente, fecha y fallback; evitar afirmar frescura o fiabilidad de reseñas sin respaldo.
- **P15 · Contenido generado:** Permitir revisar y corregir afirmaciones como “siempre con stock”; generar no equivale a publicar.
- **P16 · Permisos, accesibilidad y privacidad:** Definir avisos de uso de mapas, procesamiento de reseñas, notificaciones y políticas de datos.

## 12. Checklist de entrega para diseñador UX/UI
- [ ] Crear página Figma `00_Referencia`, insertar el tablero sin estirar y bloquearlo como base visual.
- [ ] Crear página `01_Foundations`: colores/tipografías, 8dp spacing, radios, elevación, iconografía y responsive.
- [ ] Crear página `02_Components`: variantes de botón, input, chip, switch, lista, KPI, barra inferior, estado vacío y mensajes.
- [ ] Crear página `03_Screens`: 17 frames Android con nombres S01–S17 y orden idéntico al tablero.
- [ ] Crear página `04_Flows`: prototipos A–E con interacciones y navegación de regreso.
- [ ] Crear página `05_States`: loading, empty, validation, offline, success, error para cada pantalla aplicable.
- [ ] Comparar los 17 frames con los recortes de la imagen en contenido, densidad, espaciado, tipografía, orden, iconos y CTA.
- [ ] Presentar un documento de decisiones abiertas; no inventar funciones/servicios ni aprobarlas silenciosamente.
- [ ] Preparar handoff: auto layout, constraints, tokens exportables, componentes, dimensiones, accesibilidad y contratos API.

## 13. Criterios de aceptación para la réplica visual
1. Están presentes **las 17 pantallas** y sus rótulos exactos, con el mismo orden del tablero.
2. Cada pantalla conserva su objetivo, jerarquía, componentes y CTA prioritario.
3. Tipos de componentes consistentes; no se mezclan estilos distintos entre módulos.
4. Los formularios tienen variantes error/carga/éxito; existe ruta de retorno funcional.
5. Los datos de muestra son consistentes entre pantallas y el cálculo de cotización se verifica.
6. La navegación permite completar registro → radar → campaña → cotización → seguimiento.
7. Se distinguen visualmente datos de muestra, estados reales y contenido IA.
8. Se entregan los pendientes de P01–P16 para aprobación antes de programar funciones extra.

## 14. Fuentes internas y control documental
- **Fuente funcional:** `ChambaDeNeon.pdf`, especialmente §§1.2, 2.1, 3, 4.3, 5 y 6.1 (19 páginas, versión facilitada en esta conversación).
- **Fuente visual canónica:** `sistema_ui_de_leadlaunch_en_español.png`, tablero de referencia de 17 pantallas generado en esta conversación.
- **Fecha del documento:** 23/11/2026. **Estado:** V1.0 para diseño / alineación; no define backend adicional como aprobado.
