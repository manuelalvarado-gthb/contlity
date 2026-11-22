# Contlity Python SDK

SDK oficial para la [API de Contlity](https://contlity.tech-mx.cloud/docs/api) — automatización fiscal, SAT Descarga Masiva, timbrado e integraciones ERP.

> **Requiere plan Enterprise.** Las API keys se generan en la sección **Desarrollador** de tu cuenta (`/desarrollador`).

## Instalación

```bash
pip install contlity
pip install "contlity[sqlalchemy]"   # con el driver para SQLAlchemy y pandas
```

## Inicialización

```bash
# Define la API key como variable de entorno (recomendado):
export CONTLITY_API_KEY="ctly_live_..."
```

```python
from contlity import ContlityClient

# Recomendado — lee CONTLITY_API_KEY del entorno
client = ContlityClient.from_env()

# Explícito (no usar en código fuente/repositorios)
client = ContlityClient("ctly_live_...")

# Verificar conexión
info = client.ping()
print(f"Conectado como: {info['name']} (plan: {info['plan']})")
```

## Inicio rápido

```python
from contlity import ContlityClient

client = ContlityClient.from_env()

# Listar clientes
clients = client.clients.list()
print(clients)  # [{"rfc": "ABC123...", "name": "Mi Empresa", ...}, ...]

# CFDIs de un RFC
result = client.cfdis.list("RFC123456789", limit=50)
print(result["total"])  # 1234

# CFDI por UUID
cfdi = client.cfdis.get("9A1B2C3D-...")
print(cfdi["uuid"])

# SQL fiscal
rows = client.sql.execute("SELECT COUNT(*) AS total, SUM(total) AS monto FROM cfdi")
print(rows["rows"])

# Balance de créditos
bal = client.credits.balance()
print(f"Créditos: {bal['balance']} / {bal['bundle']}")  # enterprise: bundle=null
```

## SAT Descarga Masiva

```python
# Listar jobs existentes
jobs = client.sat.jobs(rfc="RFC123456789", limit=10)

# Iniciar sync
job = client.sat.sync(
    rfc="RFC123456789",
    tipo="recibidos",
    fecha_inicial="2026-01-01",
    fecha_final="2026-01-31",
)
print(job["job_id"])

# Revisar estado
import time
while True:
    status = client.sat.job_status(job["job_id"])
    if status["status"] in ("done", "error", "timeout"):
        break
    time.sleep(10)

print(f"CFDIs descargados: {status['cfdis_ingested']}")
```

## Workflows

```python
# Ejecutar workflow
run = client.workflows.run("workflow-uuid-aqui")
run_id = run["run_id"]

# Revisar estado
status = client.workflows.run_status(run_id)
print(status["status"])  # "running" | "success" | "error"
```

## Webhooks

### Suscribir tu URL

```python
sub = client.webhooks.create(
    url="https://mi-erp.com/hooks/contlity",
    events=["sat.sync.completed", "invoice.created", "credits.low"],
    description="Integración ERP principal",
)
# ⚠️ Guarda el secret ahora — no se puede recuperar después
secret = sub["secret"]   # "whsec_abc123..."
print(f"Subscription ID: {sub['subscription_id']}")

# Listar suscripciones
subs = client.webhooks.list()

# Enviar ping de prueba
client.webhooks.test(sub["subscription_id"])

# Ver historial de entregas
deliveries = client.webhooks.deliveries(sub["subscription_id"], limit=20)

# Actualizar eventos
client.webhooks.update(sub["subscription_id"], events=["*"])  # todos los eventos

# Eliminar
client.webhooks.delete(sub["subscription_id"])
```

### Verificar webhooks entrantes

```python
from contlity import WebhookHandler, WebhookError

handler = WebhookHandler(secret="whsec_abc123...")

# En tu endpoint Flask/FastAPI/Django:
@app.post("/hooks/contlity")
async def handle_webhook(request: Request):
    body    = await request.body()
    headers = dict(request.headers)

    try:
        payload = handler.verify(body, headers)
    except WebhookError:
        return Response(status_code=401)

    event = payload["event"]
    data  = payload["data"]

    if event == "sat.sync.completed":
        print(f"SAT sync ✓ — {data['cfdis_ingested']} CFDIs")
    elif event == "invoice.created":
        print(f"Factura emitida: {data['uuid']}")
    elif event == "credits.low":
        print(f"⚠️ Créditos bajos: {data['balance']} ({data['pct']}%)")
    elif event == "credits.depleted":
        print("🚨 Créditos agotados")

    return {"ok": True}
```

### Listar tipos de evento disponibles

```python
events = client.webhooks.list_event_types()
# [{"event": "sat.sync.completed", "description": "..."}, ...]
```

### Todos los eventos disponibles

| Evento | Descripción |
|--------|-------------|
| `cfdi.ingested` | Lote de CFDIs cargado al catálogo |
| `sat.sync.completed` | Sincronización SAT completada |
| `sat.sync.failed` | Sincronización SAT falló |
| `invoice.created` | Factura electrónica emitida |
| `invoice.cancelled` | Factura electrónica cancelada |
| `portal.completed` | Job de portal autofactura completado |
| `portal.failed` | Job de portal autofactura falló |
| `workflow.completed` | Workflow completado exitosamente |
| `workflow.failed` | Workflow falló |
| `credits.low` | Balance < 20% del bundle mensual |
| `credits.depleted` | Créditos agotados |

## Manejo de errores

```python
from contlity import (
    ContlityClient,
    AuthError,
    CreditError,
    RateLimitError,
    NotFoundError,
    PlanError,
    WebhookError,
)
import time

client = ContlityClient("ctly_live_...")

try:
    result = client.sql.execute("SELECT * FROM cfdi LIMIT 1000")
except AuthError:
    print("API key inválida o revocada")
except CreditError as e:
    print(f"Sin créditos: tienes {e.balance}, la operación cuesta {e.cost}")
except RateLimitError as e:
    print(f"Rate limit — espera {e.retry_after}s")
    time.sleep(e.retry_after)
    # reintentar...
except NotFoundError:
    print("Recurso no encontrado")
except PlanError:
    print("Operación no disponible en tu plan actual")
```

## Cargas: stages y COPY INTO

Requiere una API key con el alcance **Cargas**; actúa con los permisos del usuario de la key.

```python
client = ContlityClient.from_env()

print(client.cargas.stages())                                     # stages visibles
client.cargas.subir("@despacho.integraciones.entrada/ventas/", "ventas_enero.csv")
print(client.cargas.archivos("@despacho.integraciones.entrada/ventas/"))

r = client.cargas.copiar("despacho.integraciones.ventas",           # la crea si no existe
                         "@despacho.integraciones.entrada/ventas/",
                         formato={"type": "csv"}, on_error="skip_file")
print(r["filas"], "filas cargadas")

client.cargas.actualizar_tabla_externa("despacho.integraciones.clientes_erp")
```

Para traer datos de un servidor de la oficina sin programar, usa el conector local (ver /docs/integraciones).

## Driver SQL: DB-API 2.0, SQLAlchemy y pandas

`contlity.dbapi` es un driver estándar de Python (PEP 249). Como los de Snowflake o Athena, habla con
Contlity por HTTPS: no hay puertos que abrir y cada consulta corre con los permisos, la cartera y los
créditos del usuario de la API key (alcance **SQL**). Solo lectura.

```python
import datetime
import contlity.dbapi as ctly

con = ctly.connect(database="despacho")          # API key en CONTLITY_API_KEY
cur = con.cursor()
cur.execute("SELECT rfc_emisor, sum(total) FROM comprobantes WHERE fecha >= %(desde)s GROUP BY 1",
            {"desde": datetime.date(2026, 1, 1)})
for rfc, total in cur:                            # total llega como Decimal, fechas como date/datetime
    print(rfc, total)
```

Con SQLAlchemy (`pip install "contlity[sqlalchemy]"`) funcionan pandas, Jupyter, Superset, Streamlit y
cualquier herramienta que acepte una URL de SQLAlchemy:

```python
from sqlalchemy import create_engine, inspect
import pandas as pd

eng = create_engine("contlity:///despacho")       # o contlity://:ctly_live_...@/despacho?schema=ventas
df = pd.read_sql("SELECT * FROM ventas.facturas", eng)
inspect(eng).get_table_names(schema="ventas")
```

La conexión queda en una base (por omisión, la principal). Los esquemas son los de esa base; las vistas
del núcleo están en el esquema `contlity` y se consultan sin calificar (`FROM comprobantes`).
`client.sql.catalog(database)` devuelve el mismo catálogo (esquemas, tablas, columnas y tipos).

## Configuración avanzada

```python
# URL personalizada (instancia self-hosted o staging)
client = ContlityClient(
    api_key="ctly_live_...",
    base_url="https://mi-instancia.ejemplo.com/api",
    timeout=60,  # segundos
)
```

## Referencia de endpoints `/v1`

| Método | Endpoint | Descripción | Créditos |
|--------|----------|-------------|---------|
| GET | `/v1/account` | Info de la cuenta | 0 |
| GET | `/v1/clients` | Lista de clientes | 0 |
| GET | `/v1/clients/{rfc}/cfdis` | CFDIs de un RFC | 0 |
| GET | `/v1/cfdis/{uuid}` | CFDI por UUID | 0 |
| POST | `/v1/sql` | Ejecutar SQL | 1–2 |
| GET | `/v1/catalog` | Esquemas, tablas y columnas visibles | 0 |
| GET | `/v1/stages` | Stages visibles | 0 |
| GET/POST | `/v1/stages/{base}/{esquema}/{stage}/files` | Listar / subir archivos | 0 |
| POST | `/v1/copy` | COPY INTO tabla desde un stage | 1–2 |
| POST | `/v1/external-tables/{base}/{esquema}/{tabla}/refresh` | Actualizar tabla externa | 1–2 |
| GET | `/v1/sat/jobs` | Lista jobs SAT | 0 |
| GET | `/v1/sat/jobs/{id}` | Estado de un job | 0 |
| POST | `/v1/sat/sync/{rfc}` | Disparar sync SAT | 5 |
| POST | `/v1/workflows/{id}/run` | Ejecutar workflow | 1 |
| GET | `/v1/workflows/runs/{id}` | Estado de ejecución | 0 |
| GET | `/v1/credits` | Balance de créditos | 0 |
| GET | `/v1/worksheets` | Lista worksheets | 0 |
| GET | `/v1/worksheets/{id}` | Obtener worksheet | 0 |
| POST | `/v1/upload/{rfc}` | Subir XMLs de CFDIs | 0 |
| GET | `/v1/webhooks` | Lista suscripciones | 0 |
| POST | `/v1/webhooks` | Crear suscripción | 0 |
| GET | `/v1/webhooks/{id}` | Detalle suscripción | 0 |
| PUT | `/v1/webhooks/{id}` | Actualizar suscripción | 0 |
| DELETE | `/v1/webhooks/{id}` | Eliminar suscripción | 0 |
| POST | `/v1/webhooks/{id}/test` | Enviar ping de prueba | 0 |
| GET | `/v1/webhooks/{id}/deliveries` | Historial entregas | 0 |
| GET | `/v1/events` | Tipos de evento | 0 |

**Rate limit:** 120 peticiones/minuto por API key. Respuesta 429 con `retry_after: 60`.

## Verificación de firma — detalles

El SDK incluye `WebhookHandler` que verifica firmas HMAC-SHA256:

```python
from contlity import WebhookHandler, WebhookError

handler = WebhookHandler(secret="whsec_...")

# Verificar + parsear payload
payload = handler.verify(body_bytes, headers_dict)

# Solo verificar firma (sin parsear)
is_valid = handler.verify_signature_only(body_bytes, "sha256=abc...")
```

La firma se calcula sobre el body JSON crudo con HMAC-SHA256 usando el `secret` como clave. El header verificado es `X-Contlity-Signature`.
