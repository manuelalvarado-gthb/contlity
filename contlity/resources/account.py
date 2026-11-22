"""resources/account.py — AccountResource del SDK de Contlity."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..client import ContlityClient


class AccountResource:
    """
    Operaciones sobre la cuenta asociada al API key.

    Acceso:
        client.account.info()
    """

    def __init__(self, client: "ContlityClient"):
        self._client = client

    def info(self) -> dict:
        """
        Retorna información básica de la cuenta: account_id, nombre, plan y
        número de miembros.

        Returns:
            {
                "account_id": "acc_xyz",
                "name": "Despacho Ejemplo",
                "plan": "enterprise",
                "member_count": 3
            }
        """
        return self._client._request("GET", "/account")
