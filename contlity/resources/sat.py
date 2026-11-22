"""Recurso: SAT — Descarga Masiva de CFDIs."""
from __future__ import annotations
from typing import TYPE_CHECKING, Optional

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class SATResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def jobs(self, rfc: Optional[str] = None, limit: int = 20) -> list[dict]:
        """Lista los jobs de SAT Descarga Masiva de la cuenta."""
        params = {"limit": limit}
        if rfc:
            params["rfc"] = rfc
        return self._c._request("GET", "/sat/jobs", params=params).get("jobs", [])

    def job_status(self, job_id: str) -> dict:
        """Retorna el estado actual de un job SAT."""
        return self._c._request("GET", f"/sat/jobs/{job_id}")

    def sync(
        self,
        rfc:           str,
        tipo:          str = "recibidos",
        fecha_inicial: str = "",
        fecha_final:   str = "",
    ) -> dict:
        """
        Dispara una sincronización SAT Descarga Masiva.

        Args:
            rfc:           RFC del cliente registrado en la cuenta.
            tipo:          "recibidos" o "emitidos".
            fecha_inicial: Fecha de inicio en formato YYYY-MM-DD.
            fecha_final:   Fecha de fin en formato YYYY-MM-DD.

        Returns:
            dict con job_id y status inicial.

        Note:
            Requiere FIEL configurada para el RFC.
            Consume 5 créditos de cómputo.
        """
        return self._c._request(
            "POST", f"/sat/sync/{rfc}",
            json={"tipo": tipo, "fecha_inicial": fecha_inicial, "fecha_final": fecha_final},
        )
