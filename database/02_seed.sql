-- Fuente: docs/especificaciones/Documento_Maestro_LeadLaunch.md §4.3 (copia fiel).
-- Ya aplicado en Neon. Para una base nueva: ejecutar 01_ddl.sql y luego 02_seed.sql
-- (o desde backend: python -m scripts.aplicar_ddl). Verificar con: python -m scripts.verificar_bd

-- ----------------------------------------------------------------------------
-- 7. CARGA SEMILLA PARAMÉTRICA (VERTICAL GASTRONOMÍA Y REPOSTERÍA)
-- ----------------------------------------------------------------------------

INSERT INTO cat_rubros (codigo, nombre, sector) VALUES
('REPOSTERIA',    'Repostería y Pastelería Artesanal', 'GASTRONOMIA'),
('COMIDA_RAPIDA', 'Comida Rápida y Bocaditos',         'GASTRONOMIA'),
('CAFETERIA',     'Cafeterías y Postres',              'GASTRONOMIA');

INSERT INTO cat_tipos_friccion (codigo, nombre, descripcion) VALUES
('DEMORA_ENTREGA',        'Demora en delivery/entrega',   'Retrasos o incumplimiento en la hora pactada de entrega'),
('CALIDAD_PRESENTACION',  'Presentación o frescura',      'Pedidos mal empacados, dañados o con baja frescura'),
('PRECIO_DELIVERY',       'Precio o envío elevado',       'Percepción de costo alto o tarifa de reparto excesiva'),
('MALA_ATENCION',         'Mala atención al cliente',     'Demoras en responder mensajes o trato deficiente'),
('SIN_OPCIONES_URGENTES', 'Sin atención de último minuto','Falta de combos express o pedidos para el mismo día');
