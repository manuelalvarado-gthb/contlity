"""
webhook_handler.py — Verificación de firma HMAC-SHA256 para webhooks entrantes de Contlity.

Uso:
    from contlity import WebhookHandler

    handler = WebhookHandler(secret="whsec_...")

    # En tu servidor Flask/FastAPI/Django:
    payload = handler.verify(request.body, request.headers)
    print(payload["event"])   # "sat.sync.completed"
    print(payload["data"])    # campos del evento
"""
from __future__ import annotations

import hashlib
import hmac
import json

from .exceptions import WebhookError


class WebhookHandler:
    """
    Verifica y parsea webhooks entrantes de Contlity.

    Args:
        secret: El `secret` que recibiste al crear la suscripción webhook
                (formato: "whsec_...").
    """

    def __init__(self, secret: str):
        if not secret:
            raise ValueError("secret no puede estar vacío")
        self.secret = secret

    def verify(self, body: bytes | str, headers: dict) -> dict:
        """
        Verifica la firma HMAC-SHA256 del webhook y retorna el payload parseado.

        Args:
            body: Cuerpo de la petición HTTP (bytes o str).
            headers: Headers de la petición HTTP (dict o similar).
                     Busca 'X-Contlity-Signature' (case-insensitive).

        Returns:
            dict con el payload del evento:
                event       — tipo de evento (e.g. "sat.sync.completed")
                delivery_id — UUID único de esta entrega
                timestamp   — ISO 8601 UTC
                account_id  — cuenta que originó el evento
                data        — campos específicos del evento

        Raises:
            WebhookError: si la firma no coincide o el body no es JSON válido.
        """
        raw = body if isinstance(body, bytes) else body.encode("utf-8")

        # Normalize header lookup (case-insensitive)
        sig_header = None
        for k, v in headers.items():
            if k.lower() == "x-contlity-signature":
                sig_header = v
                break

        if not sig_header:
            raise WebhookError("Header X-Contlity-Signature ausente")

        expected = "sha256=" + hmac.new(
            self.secret.encode("utf-8"), raw, hashlib.sha256
        ).hexdigest()

        if not hmac.compare_digest(expected, sig_header):
            raise WebhookError(
                "Firma inválida — verifica que el secret sea correcto y que el body no haya sido modificado"
            )

        try:
            return json.loads(raw)
        except json.JSONDecodeError as exc:
            raise WebhookError(f"Payload no es JSON válido: {exc}") from exc

    def verify_signature_only(self, body: bytes | str, signature: str) -> bool:
        """
        Retorna True si la firma es válida, False si no.
        No parsea el body — útil si ya tienes el JSON parseado y solo quieres verificar.
        """
        raw = body if isinstance(body, bytes) else body.encode("utf-8")
        expected = "sha256=" + hmac.new(
            self.secret.encode("utf-8"), raw, hashlib.sha256
        ).hexdigest()
        return hmac.compare_digest(expected, signature)
