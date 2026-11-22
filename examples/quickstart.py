"""
quickstart.py — Ejemplo de inicio rápido del SDK de Contlity.

Requisitos:
  - Cuenta Contlity con plan Enterprise
  - API key generada en /desarrollador
  - pip install contlity

Antes de ejecutar:
  export CONTLITY_API_KEY="ctly_live_..."
  python quickstart.py
"""

import os
import sys
import time

from contlity import ContlityClient, AuthError, RateLimitError, CreditError

# ── 1. Inicializar el cliente ─────────────────────────────────────────────────
#
# Opción A (recomendada): desde variable de entorno
#   export CONTLITY_API_KEY="ctly_live_..."
#
# Opción B (explícita, no usar en producción):
#   client = ContlityClient("ctly_live_...")

print("=" * 60)
print("Contlity SDK — Quickstart")
print("=" * 60)

try:
    client = ContlityClient.from_env()
    print(f"Cliente: {client}")
except EnvironmentError as e:
    print(f"\n[ERROR] {e}")
    sys.exit(1)


# ── 2. Verificar conexión ─────────────────────────────────────────────────────

print("\n[1/6] Verificando conexión...")
try:
    info = client.ping()
    print(f"  ✓ Conectado como: {info['name']}")
    print(f"  ✓ Plan: {info['plan']}")
    print(f"  ✓ account_id: {info['account_id']}")
except AuthError:
    print("[ERROR] API key inválida o revocada. Genera una nueva en /desarrollador.")
    sys.exit(1)


# ── 3. Balance de créditos ────────────────────────────────────────────────────

print("\n[2/6] Consultando créditos...")
bal = client.credits.balance()
if bal["enterprise"]:
    print("  ✓ Plan Enterprise — sin límite de créditos")
else:
    pct   = bal["pct"]
    color = "✓" if pct > 20 else "⚠"
    print(f"  {color} Balance: {bal['balance']} / {bal['bundle']} ({pct}%)")
    if bal["low_credit"]:
        print("  ⚠ Créditos bajos — considera recargar")


# ── 4. Listar clientes ────────────────────────────────────────────────────────

print("\n[3/6] Listando clientes (RFCs)...")
clients = client.clients.list()
total   = len(clients)
print(f"  ✓ {total} cliente(s) registrado(s)")
for c in clients[:3]:
    name = c.get("name") or c.get("rfc", "Sin nombre")
    print(f"     • {c['rfc']} — {name}")
if total > 3:
    print(f"     … y {total - 3} más")


# ── 5. CFDIs del primer cliente ───────────────────────────────────────────────

if clients:
    rfc = clients[0]["rfc"]
    print(f"\n[4/6] CFDIs de {rfc} (últimos 5)...")
    result = client.cfdis.list(rfc, limit=5)
    print(f"  ✓ Total: {result['total']:,} CFDIs")
    for cfdi in result["cfdis"][:3]:
        fecha = cfdi.get("fecha", "")[:10]
        tipo  = cfdi.get("tipo_comprobante", "?")
        total_cfdi = cfdi.get("total", 0)
        print(f"     • [{tipo}] {fecha}  ${total_cfdi:,.2f}  {cfdi.get('uuid', '')[:8]}...")
else:
    print("\n[4/6] Sin clientes — omitiendo consulta de CFDIs")


# ── 6. Consulta SQL ───────────────────────────────────────────────────────────

print("\n[5/6] Ejecutando SQL fiscal...")
try:
    result = client.sql.execute(
        "SELECT tipo_comprobante, COUNT(*) AS total, SUM(total) AS monto "
        "FROM cfdi "
        "GROUP BY tipo_comprobante "
        "ORDER BY total DESC "
        "LIMIT 5"
    )
    print(f"  ✓ Columnas: {result['columns']}")
    for row in result["rows"]:
        print(f"     {row}")
except CreditError as e:
    print(f"  ⚠ Sin créditos para SQL (balance: {e.balance})")


# ── 7. Jobs SAT ───────────────────────────────────────────────────────────────

print("\n[6/6] Últimos jobs de SAT Descarga Masiva...")
jobs = client.sat.jobs(limit=5)
total_jobs = len(jobs.get("jobs", []))
if total_jobs == 0:
    print("  — Sin jobs previos. Usa client.sat.sync(rfc, tipo) para iniciar una descarga.")
else:
    print(f"  ✓ {total_jobs} job(s):")
    for job in jobs["jobs"][:3]:
        status = job.get("status", "?")
        rfc_j  = job.get("rfc", "?")
        ingested = job.get("cfdis_ingested", 0)
        icon = "✓" if status == "done" else ("✗" if status == "error" else "⟳")
        print(f"     {icon} {rfc_j}  {status}  {ingested:,} CFDIs")


# ── Fin ───────────────────────────────────────────────────────────────────────

print("\n" + "=" * 60)
print("Quickstart completado. SDK listo para usar.")
print("Docs: https://contlity.tech-mx.cloud/docs/sdk")
print("=" * 60)
