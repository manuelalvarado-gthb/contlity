"""
contlity.dbapi — driver SQL de Contlity (PEP 249 / DB-API 2.0).

Habla con Contlity por HTTPS (como los drivers de Snowflake o Athena): cada consulta corre en el
motor de Contlity con los permisos, la cartera de clientes y los créditos del usuario de la API key
(alcance «sql»). Solo lectura.

Uso::

    import contlity.dbapi as ctly

    con = ctly.connect()                                  # API key en CONTLITY_API_KEY
    cur = con.cursor()
    cur.execute("SELECT rfc_emisor, sum(total) FROM comprobantes WHERE fecha >= %(desde)s GROUP BY 1",
                {"desde": datetime.date(2026, 1, 1)})
    for rfc, total in cur:
        print(rfc, total)

Con pandas::

    import pandas as pd
    df = pd.read_sql("SELECT * FROM ventas.facturas", ctly.connect(database="despacho"))
"""
from __future__ import annotations

import datetime as _dt
import decimal as _dec
import math as _math
import re as _re
import time as _time
import uuid as _uuid
from typing import Any, Iterable, Optional, Sequence

from . import exceptions as _exc

# ── Globales PEP 249 ──────────────────────────────────────────────────────────

apilevel = "2.0"
threadsafety = 1          # los hilos comparten el módulo, no las conexiones
paramstyle = "pyformat"   # %(nombre)s y %s, como psycopg


class Warning(Exception):  # noqa: A001 (nombre exigido por PEP 249)
    pass


class Error(Exception):
    pass


class InterfaceError(Error):
    pass


class DatabaseError(Error):
    pass


class DataError(DatabaseError):
    pass


class OperationalError(DatabaseError):
    pass


class IntegrityError(DatabaseError):
    pass


class InternalError(DatabaseError):
    pass


class ProgrammingError(DatabaseError):
    pass


class NotSupportedError(DatabaseError):
    pass


# ── Tipos ─────────────────────────────────────────────────────────────────────

class _Tipo:
    def __init__(self, *nombres: str):
        self.nombres = frozenset(nombres)

    def __eq__(self, other: object) -> bool:
        return isinstance(other, str) and _base_tipo(other) in self.nombres

    def __hash__(self) -> int:
        return hash(self.nombres)


def _base_tipo(t: str) -> str:
    return _re.sub(r"\(.*\)", "", (t or "text").lower()).strip()


STRING = _Tipo("text", "varchar", "character varying", "character", "char", "uuid", "json", "jsonb", "name")
BINARY = _Tipo("bytea")
NUMBER = _Tipo("integer", "bigint", "smallint", "numeric", "decimal", "real", "double", "double precision", "float")
DATETIME = _Tipo("date", "timestamp", "timestamptz", "timestamp with time zone", "timestamp without time zone",
                 "time", "time without time zone", "interval")
ROWID = _Tipo("oid")

Date = _dt.date
Time = _dt.time
Timestamp = _dt.datetime


def DateFromTicks(ticks: float) -> _dt.date:
    return _dt.date(*_time.localtime(ticks)[:3])


def TimeFromTicks(ticks: float) -> _dt.time:
    return _dt.time(*_time.localtime(ticks)[3:6])


def TimestampFromTicks(ticks: float) -> _dt.datetime:
    return _dt.datetime(*_time.localtime(ticks)[:6])


def Binary(x: bytes) -> bytes:
    return bytes(x)


# ── Parámetros (se incrustan como literales seguros; el servidor recibe una sola sentencia) ──

def literal(v: Any) -> str:
    """Valor de Python → literal SQL."""
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, int):
        return str(v)
    if isinstance(v, float):
        if _math.isnan(v) or _math.isinf(v):
            return f"'{v}'::float8"
        return repr(v)
    if isinstance(v, _dec.Decimal):
        return f"'{v}'::numeric" if not v.is_finite() else str(v)
    if isinstance(v, _dt.datetime):
        return f"'{v.isoformat()}'::" + ("timestamptz" if v.tzinfo else "timestamp")
    if isinstance(v, _dt.date):
        return f"'{v.isoformat()}'::date"
    if isinstance(v, _dt.time):
        return f"'{v.isoformat()}'::time"
    if isinstance(v, _uuid.UUID):
        return f"'{v}'::uuid"
    if isinstance(v, (bytes, bytearray, memoryview)):
        return f"'\\x{bytes(v).hex()}'::bytea"
    if isinstance(v, tuple):                       # IN %s
        if not v:
            raise ProgrammingError("Una tupla vacía no es válida en IN")
        return "(" + ", ".join(literal(x) for x in v) + ")"
    if isinstance(v, list):
        return "ARRAY[" + ", ".join(literal(x) for x in v) + "]"
    if isinstance(v, dict):
        import json
        return _texto(json.dumps(v, ensure_ascii=False, default=str)) + "::jsonb"
    return _texto(str(v))


def _texto(s: str) -> str:
    if "\x00" in s:
        raise DataError("El texto contiene un carácter nulo")
    s = s.replace("'", "''")
    if "\\" in s:                                  # independiente de standard_conforming_strings
        return "E'" + s.replace("\\", "\\\\") + "'"
    return "'" + s + "'"


_MARCA = _re.compile(r"%(?:\((?P<nombre>[^)]+)\))?(?P<tipo>[sr%])")


def formatear(sql: str, params: Any) -> str:
    """Aplica `params` a la sentencia como psycopg: con params=None no se toca (ni los %)."""
    if params is None:
        return sql
    posicional = iter(params) if isinstance(params, (list, tuple)) else None

    def _rep(m: _re.Match) -> str:
        if m.group("tipo") == "%":
            return "%"
        nombre = m.group("nombre")
        if nombre is not None:
            if not isinstance(params, dict):
                raise ProgrammingError("La sentencia usa %(nombre)s pero los parámetros no son un dict")
            if nombre not in params:
                raise ProgrammingError(f"Falta el parámetro «{nombre}»")
            return literal(params[nombre])
        if posicional is None:
            raise ProgrammingError("La sentencia usa %s pero los parámetros no son una secuencia")
        try:
            return literal(next(posicional))
        except StopIteration:
            raise ProgrammingError("Faltan parámetros para la sentencia") from None

    out = _MARCA.sub(_rep, sql)
    if posicional is not None and next(posicional, _FIN) is not _FIN:
        raise ProgrammingError("Sobran parámetros para la sentencia")
    return out


_FIN = object()


# ── Conversión de resultados ──────────────────────────────────────────────────

def _convertidor(tipo: str):
    t = _base_tipo(tipo)
    if t in ("numeric", "decimal"):
        return lambda v: v if v is None or isinstance(v, _dec.Decimal) else _dec.Decimal(str(v))
    if t == "date":
        return lambda v: _dt.date.fromisoformat(v) if isinstance(v, str) else v
    if t.startswith("timestamp"):
        return lambda v: _dt.datetime.fromisoformat(v) if isinstance(v, str) else v
    if t.startswith("time"):
        return lambda v: _dt.time.fromisoformat(v) if isinstance(v, str) else v
    return None


# ── Conexión y cursor ─────────────────────────────────────────────────────────

PAGINA = 10000   # filas por página que pide el driver


def connect(api_key: Optional[str] = None, database: Optional[str] = None, schema: Optional[str] = None,
            base_url: Optional[str] = None, timeout: int = 120, client: Any = None,
            page_size: int = PAGINA) -> "Connection":
    """
    Abre una conexión.

    Args:
        api_key:  API key con alcance «sql» (por omisión, la variable CONTLITY_API_KEY)
        database: base en uso para nombres esquema.objeto (por omisión, la principal)
        schema:   esquema en uso, para escribir solo el nombre del objeto
        base_url: URL de la API (por omisión, la de producción)
        timeout:  segundos de espera por consulta
        client:   un ContlityClient ya creado (en lugar de api_key/base_url/timeout)
        page_size: filas por página; las páginas siguientes se descargan al recorrer el cursor
    """
    if client is None:
        from .client import ContlityClient
        try:
            client = (ContlityClient(api_key, base_url=base_url, timeout=timeout) if api_key
                      else ContlityClient.from_env(base_url=base_url, timeout=timeout))
        except (ValueError, EnvironmentError) as e:
            raise InterfaceError(str(e)) from e
    return Connection(client, database, schema, page_size)


def _error(e: Exception) -> Error:
    if isinstance(e, Error):
        return e
    if isinstance(e, _exc.ContlityError):
        return OperationalError(str(e))
    return InterfaceError(str(e))


class Connection:
    def __init__(self, client: Any, database: Optional[str] = None, schema: Optional[str] = None,
                 page_size: int = PAGINA):
        self._client = client
        self.database = database
        self.schema = schema
        self.page_size = page_size
        self._cerrada = False
        self._catalogo: Optional[tuple[float, dict]] = None

    # PEP 249
    def close(self) -> None:
        self._cerrada = True

    def commit(self) -> None:          # solo lectura: no hay transacción que confirmar
        self._abierta()

    def rollback(self) -> None:
        self._abierta()

    def cursor(self) -> "Cursor":
        self._abierta()
        return Cursor(self)

    @property
    def closed(self) -> bool:
        return self._cerrada

    def __enter__(self) -> "Connection":
        return self

    def __exit__(self, *a) -> None:
        self.close()

    # Extensiones
    def catalog(self, refrescar: bool = False) -> dict:
        """Esquemas, tablas y columnas visibles en la base de la conexión (en caché 60 s)."""
        self._abierta()
        if refrescar or not self._catalogo or _time.time() - self._catalogo[0] > 60:
            try:
                params = {"database": self.database} if self.database else None
                self._catalogo = (_time.time(), self._client._request("GET", "/catalog", params=params))
            except Exception as e:
                raise _error(e) from e
        return self._catalogo[1]

    def _ejecutar(self, sql: str) -> dict:
        self._abierta()
        body: dict = {"query": sql, "page_size": self.page_size}
        if self.database:
            body["database"] = self.database
        if self.schema:
            body["schema_name"] = self.schema
        try:
            r = self._client._request("POST", "/sql", json=body)
        except Exception as e:
            raise _error(e) from e
        if r.get("error"):
            raise ProgrammingError(r["error"])
        return r

    def _pagina(self, result_id: str, n: int) -> list:
        self._abierta()
        try:
            return self._client._request("GET", f"/sql/results/{result_id}/{n}")["rows"]
        except Exception as e:
            raise _error(e) from e

    def _liberar(self, result_id: str) -> None:
        try:
            self._client._request("DELETE", f"/sql/results/{result_id}")
        except Exception:
            pass

    def _abierta(self) -> None:
        if self._cerrada:
            raise InterfaceError("La conexión está cerrada")


class Cursor:
    def __init__(self, connection: Connection):
        self.connection = connection
        self.arraysize = 1
        self.description: Optional[list[tuple]] = None
        self.rowcount = -1
        self._filas: list = []
        self._i = 0
        self._conv: list = []
        self._rid: Optional[str] = None
        self._paginas = self._sig = 1
        self._cerrado = False

    def execute(self, operation: str, parameters: Any = None) -> "Cursor":
        if self._cerrado:
            raise InterfaceError("El cursor está cerrado")
        self._soltar()
        sql = formatear(operation, parameters)
        r = self.connection._ejecutar(sql)
        cols = r.get("columns") or []
        self._conv = [_convertidor(c.get("type") or "text") for c in cols]
        self.description = [(c["name"], c.get("type") or "text", None, None, None, None, None) for c in cols] or None
        self._filas, self._i = self._convertir(r.get("rows") or []), 0
        self._rid, self._paginas, self._sig = r.get("result_id"), r.get("pages") or 1, 1
        self.rowcount = r.get("total", len(self._filas))
        return self

    def _convertir(self, filas: list) -> list:
        if not any(self._conv):
            return [tuple(f) for f in filas]
        return [tuple(c(v) if c and v is not None else v for c, v in zip(self._conv, f)) for f in filas]

    def _llenar(self) -> bool:
        """Descarga la siguiente página si ya se consumió la actual."""
        if self._i < len(self._filas):
            return True
        if not self._rid or self._sig >= self._paginas:
            return False
        self._filas, self._i = self._convertir(self.connection._pagina(self._rid, self._sig)), 0
        self._sig += 1
        if self._sig >= self._paginas:
            self._soltar()
        return bool(self._filas)

    def _soltar(self) -> None:
        if getattr(self, "_rid", None):        # libera el resultado en el servidor (leído o abandonado)
            rid, self._rid = self._rid, None
            self.connection._liberar(rid)

    def executemany(self, operation: str, seq_of_parameters: Iterable[Any]) -> None:
        for p in seq_of_parameters:
            self.execute(operation, p)

    def fetchone(self) -> Optional[tuple]:
        self._con_resultado()
        if not self._llenar():
            return None
        self._i += 1
        return self._filas[self._i - 1]

    def fetchmany(self, size: Optional[int] = None) -> list[tuple]:
        self._con_resultado()
        n = self.arraysize if size is None else size
        out: list = []
        while len(out) < n and self._llenar():
            parte = self._filas[self._i:self._i + n - len(out)]
            self._i += len(parte)
            out.extend(parte)
        return out

    def fetchall(self) -> list[tuple]:
        self._con_resultado()
        out: list = []
        while self._llenar():
            out.extend(self._filas[self._i:])
            self._i = len(self._filas)
        return out

    def close(self) -> None:
        if not self._cerrado:
            self._soltar()
        self._cerrado = True
        self._filas = []

    def callproc(self, *a, **k):
        raise NotSupportedError("Contlity no ejecuta procedimientos por el driver; usa SELECT")

    def nextset(self) -> None:
        return None

    def setinputsizes(self, sizes: Sequence[Any]) -> None:
        pass

    def setoutputsize(self, size: int, column: Optional[int] = None) -> None:
        pass

    def __iter__(self):
        return self

    def __next__(self) -> tuple:
        fila = self.fetchone()
        if fila is None:
            raise StopIteration
        return fila

    def __enter__(self) -> "Cursor":
        return self

    def __exit__(self, *a) -> None:
        self.close()

    def _con_resultado(self) -> None:
        if self._cerrado:
            raise InterfaceError("El cursor está cerrado")
        if self.description is None:
            raise ProgrammingError("No hay resultado: ejecuta una consulta primero")
