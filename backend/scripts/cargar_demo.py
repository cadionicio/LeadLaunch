"""Carga los datos de la maqueta (solo demostración) en la base de DATABASE_URL (backend/.env).

Uso (desde la carpeta backend, con el entorno virtual activo):
    python -m scripts.cargar_demo

Crea, si no existen: la cuenta ana@dulcepunto.com (contraseña Dulce123), su negocio Dulce Punto
y los 4 productos de la maqueta (TOR-001, CHE-002, CUP-006, POS-011). Se puede ejecutar varias veces.
"""
from app.core.database import close_pool, get_conn
from app.core.security import hash_password
from app.repositories import negocios_repo, productos_repo, usuarios_repo

CORREO, PASSWORD, NOMBRE = "ana@dulcepunto.com", "Dulce123", "Ana Rodríguez"
NEGOCIO = {
    "rubro_id": 1, "razon_comercial": "Dulce Punto",
    "descripcion": "Repostería artesanal y tortas personalizadas.", "whatsapp_numero": "51987654321",
    "distrito": "Miraflores", "latitud": -12.1211, "longitud": -77.0297, "radio_cobertura_km": 5.0,
}
PRODUCTOS = [
    {"sku": "TOR-001", "nombre": "Torta de chocolate", "descripcion": "Torta húmeda de chocolate, 12 porciones",
     "precio_base": 75.00, "tiempo_prep_horas": 24, "stock_disponible": 8, "permite_express": True},
    {"sku": "CHE-002", "nombre": "Cheesecake de frutos", "descripcion": "Cheesecake con frutos rojos",
     "precio_base": 65.00, "tiempo_prep_horas": 24, "stock_disponible": 10, "permite_express": True},
    {"sku": "CUP-006", "nombre": "Cupcake (x6)", "descripcion": "Caja de 6 cupcakes surtidos",
     "precio_base": 35.00, "tiempo_prep_horas": 4, "stock_disponible": 20, "permite_express": True},
    {"sku": "POS-011", "nombre": "Postre individual", "descripcion": "Postre del día en vaso",
     "precio_base": 12.00, "tiempo_prep_horas": 2, "stock_disponible": 30, "permite_express": False},
]


def main() -> None:
    with get_conn() as conn:
        usuario = usuarios_repo.obtener_por_correo(conn, CORREO)
        if usuario is None:
            usuario = usuarios_repo.crear(conn, CORREO, hash_password(PASSWORD), NOMBRE)
            print(f"Cuenta creada: {CORREO} / {PASSWORD}")
        else:
            print(f"Cuenta existente: {CORREO} (se mantiene su contraseña actual)")
        uid = usuario["usuario_id"]

        nid = negocios_repo.obtener_id_activo_por_usuario(conn, uid)
        if nid is None:
            nid = negocios_repo.crear(conn, uid, NEGOCIO)
            negocios_repo.crear_politica_base(conn, nid)
            negocios_repo.crear_reglas_base(conn, nid)
            print("Negocio creado: Dulce Punto")
        else:
            print("Negocio existente: se mantiene")

        for p in PRODUCTOS:
            if productos_repo.sku_existe(conn, nid, p["sku"]):
                print(f"  {p['sku']} ya existe")
            else:
                productos_repo.crear(conn, nid, p)
                print(f"  {p['sku']} creado: {p['nombre']} S/ {p['precio_base']:.2f}")

    with get_conn() as conn:
        negocios_repo.refrescar_resumen(conn)
    print(f"\nListo. negocio_id = {nid}")


if __name__ == "__main__":
    try:
        main()
    finally:
        close_pool()
