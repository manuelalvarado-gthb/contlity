"""Recurso: Credits — balance de créditos de cómputo."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class CreditsResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def balance(self) -> dict:
        """
        Retorna el balance actual de créditos de la cuenta.

        Returns:
            dict con keys:
                balance    — créditos disponibles
                plan       — "free" | "pro" | "enterprise"
                bundle     — créditos incluidos por mes (null para enterprise)
                enterprise — True si la cuenta es Enterprise (sin límite)
                low_credit — True si el balance es < 20% del bundle mensual
                pct        — porcentaje del bundle restante (0-100)
        """
        return self._c._request("GET", "/credits")
