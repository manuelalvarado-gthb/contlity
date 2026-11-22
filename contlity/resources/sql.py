"""Recurso: SQL — ejecución de consultas fiscales."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from contlity.client import ContlityClient


class SQLResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def execute(self, query: str, database: str | None = None, schema: str | None = None) -> dict:
        """
        Ejecuta una consulta de lectura (SELECT, WITH, TABLE, VALUES o EXPLAIN).

        Corre con los permisos de datos y la cartera de clientes del usuario de la API key
        (alcance «sql»), en una transacción de solo lectura.

        Args:
            query:    una sentencia. Ejemplo: "SELECT count(*) FROM comprobantes"
            database: base de datos en uso para esquema.objeto (por omisión, la principal)
            schema:   esquema en uso, para escribir solo el nombre del objeto

        Returns:
            dict con columns ([{name, type}]), rows, total y error (None si todo salió bien)

        Raises:
            CreditError: si no hay créditos suficientes (1–2 créditos por consulta)
        """
        body = {"query": query}
        if database:
            body["database"] = database
        if schema:
            body["schema_name"] = schema
        return self._c._request("POST", "/sql", json=body)

    def catalog(self, database: str | None = None) -> dict:
        """
        Esquemas, tablas y columnas que el usuario de la API key puede consultar en una base.

        Returns:
            dict con database, databases, default_schema («contlity»: vistas del núcleo) y
            schemas ([{name, tables: [{name, type, description, columns: [{name, type}]}]}])
        """
        return self._c._request("GET", "/catalog", params={"database": database} if database else None)
