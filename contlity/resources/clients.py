"""Recurso: Clients — clientes/RFCs de la cuenta."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class ClientsResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def list(self) -> list[dict]:
        """Lista todos los clientes (RFCs) de la cuenta."""
        return self._c._request("GET", "/clients").get("clients", [])
