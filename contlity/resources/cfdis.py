"""Recurso: CFDIs — consulta de comprobantes fiscales."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class CFDIsResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def list(self, rfc: str, limit: int = 100, offset: int = 0) -> dict:
        """
        Lista CFDIs para un RFC (emitidos y recibidos).

        Returns:
            dict con keys: rfc, cfdis (list), total, limit, offset
        """
        return self._c._request(
            "GET", f"/clients/{rfc}/cfdis",
            params={"limit": limit, "offset": offset},
        )

    def get(self, uuid: str) -> dict:
        """Obtiene un CFDI por su UUID."""
        return self._c._request("GET", f"/cfdis/{uuid}")
