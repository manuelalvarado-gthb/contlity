"""Recurso: Workflows — ejecución de workflows de automatización."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class WorkflowsResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def run(self, workflow_id: str) -> dict:
        """
        Dispara la ejecución de un workflow.

        Args:
            workflow_id: ID del workflow a ejecutar.

        Returns:
            dict con keys: run_id, status ("running"), workflow_id

        Raises:
            NotFoundError: si el workflow no existe o no pertenece a la cuenta.
            CreditError:   si no hay créditos (1 crédito por ejecución base).
        """
        return self._c._request("POST", f"/workflows/{workflow_id}/run")

    def run_status(self, run_id: str) -> dict:
        """
        Retorna el estado actual de una ejecución de workflow.

        Returns:
            dict con keys: run_id, status ("running"|"success"|"error"), events (list)
        """
        return self._c._request("GET", f"/workflows/runs/{run_id}")
