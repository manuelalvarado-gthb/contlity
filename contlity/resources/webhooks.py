"""Recurso: Webhooks — gestión de suscripciones de webhooks outbound."""
from __future__ import annotations
from typing import TYPE_CHECKING, Optional

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class WebhooksResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def list(self) -> list[dict]:
        """Lista todas las suscripciones webhook activas e inactivas de la cuenta."""
        return self._c._request("GET", "/webhooks").get("subscriptions", [])

    def create(
        self,
        url:         str,
        events:      list[str] = None,
        description: str = "",
    ) -> dict:
        """
        Crea una nueva suscripción webhook.

        ⚠️  El campo `secret` del response solo se muestra una vez.
        Guárdalo de forma segura — lo necesitarás para verificar las firmas HMAC.

        Args:
            url:         URL HTTPS que recibirá los eventos.
            events:      Lista de tipos de evento, e.g. ["sat.sync.completed", "invoice.created"].
                         Usa ["*"] para suscribirte a todos los eventos.
            description: Etiqueta descriptiva (opcional).

        Returns:
            dict con subscription_id, url, events, active, secret (solo aquí), etc.
        """
        if events is None:
            events = ["*"]
        return self._c._request(
            "POST", "/webhooks",
            json={"url": url, "events": events, "description": description},
        )

    def get(self, subscription_id: str) -> dict:
        """Detalle de una suscripción (sin el secret)."""
        return self._c._request("GET", f"/webhooks/{subscription_id}")

    def update(
        self,
        subscription_id: str,
        events:          Optional[list[str]] = None,
        description:     Optional[str] = None,
        active:          Optional[bool] = None,
    ) -> dict:
        """Actualiza eventos, descripción o estado activo de una suscripción."""
        payload: dict = {}
        if events is not None:
            payload["events"] = events
        if description is not None:
            payload["description"] = description
        if active is not None:
            payload["active"] = active
        return self._c._request("PUT", f"/webhooks/{subscription_id}", json=payload)

    def delete(self, subscription_id: str) -> dict:
        """Elimina permanentemente una suscripción webhook."""
        return self._c._request("DELETE", f"/webhooks/{subscription_id}")

    def test(self, subscription_id: str) -> dict:
        """Envía un evento de prueba (ping) a la URL de la suscripción."""
        return self._c._request("POST", f"/webhooks/{subscription_id}/test")

    def deliveries(
        self,
        subscription_id: str,
        limit:  int = 50,
        offset: int = 0,
    ) -> dict:
        """
        Historial de entregas para una suscripción.

        Returns:
            dict con keys: total, limit, offset, deliveries (list)
        """
        return self._c._request(
            "GET", f"/webhooks/{subscription_id}/deliveries",
            params={"limit": limit, "offset": offset},
        )

    def list_event_types(self) -> list[dict]:
        """Lista todos los tipos de evento disponibles para suscripciones."""
        return self._c._request("GET", "/events").get("events", [])
