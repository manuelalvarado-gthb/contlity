"""
Contlity Python SDK — cliente oficial para la API de Contlity.

Uso rápido:
    from contlity import ContlityClient, WebhookHandler

    client = ContlityClient("ctly_live_...")

    # Listar clientes
    clients = client.clients.list()

    # Ejecutar SQL
    result = client.sql.execute("SELECT COUNT(*) AS total FROM cfdi")

    # Driver SQL (DB-API 2.0) y SQLAlchemy
    import contlity.dbapi as ctly
    cur = ctly.connect(database="despacho").cursor()
    cur.execute("SELECT * FROM ventas.facturas WHERE total > %s", (1000,))
    # create_engine("contlity:///despacho")  → pip install "contlity[sqlalchemy]"

    # SAT Descarga
    job = client.sat.sync("RFC123456789", "recibidos", "2026-01-01", "2026-01-31")
    status = client.sat.job_status(job["job_id"])

    # Webhooks
    sub = client.webhooks.create(
        url="https://mi-erp.com/hooks/contlity",
        events=["sat.sync.completed", "invoice.created"],
    )
    print(sub["secret"])  # ¡Guárdalo! Solo visible aquí.

    # Verificar webhook entrante
    handler = WebhookHandler(secret="whsec_...")
    payload = handler.verify(request_body, request_headers)
    print(payload["event"])   # "sat.sync.completed"

Documentación: https://contlity.tech-mx.cloud/docs/sdk
"""
from .client          import ContlityClient, ENV_VAR
from .webhook_handler import WebhookHandler
from .exceptions      import (
    ContlityError,
    AuthError,
    NotFoundError,
    RateLimitError,
    CreditError,
    PlanError,
    WebhookError,
)

__version__ = "0.4.0"
__all__     = [
    "ContlityClient",
    "ENV_VAR",
    "WebhookHandler",
    "ContlityError",
    "AuthError",
    "NotFoundError",
    "RateLimitError",
    "CreditError",
    "PlanError",
    "WebhookError",
    "__version__",
]
