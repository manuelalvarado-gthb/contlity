"""
Dialecto de SQLAlchemy para Contlity (``pip install "contlity[sqlalchemy]"``).

URL::

    contlity://:API_KEY@/BASE?schema=ESQUEMA          # producción
    contlity:///BASE                                  # API key en CONTLITY_API_KEY
    contlity://:API_KEY@mi-servidor:8443/BASE         # otra instalación (https://host:puerto/api)

Uso::

    from sqlalchemy import create_engine, inspect
    import pandas as pd

    eng = create_engine("contlity:///despacho")
    df = pd.read_sql("SELECT * FROM ventas.facturas", eng)
    inspect(eng).get_table_names(schema="ventas")

La conexión queda en una base (BASE en la URL; por omisión la principal). Los esquemas de SQLAlchemy
son los esquemas de esa base; las vistas del núcleo están en el esquema «contlity» (el de por omisión)
y se consultan sin calificar. Solo lectura: las consultas corren en el motor de Contlity con los
permisos y créditos del usuario de la API key.
"""
from __future__ import annotations

import re
from typing import Any

from sqlalchemy import types as sqltypes
from sqlalchemy.dialects.postgresql.base import PGDialect, PGIdentifierPreparer
from sqlalchemy.engine import default

from . import dbapi as _dbapi

NUCLEO = "contlity"


class ContlityIdentifierPreparer(PGIdentifierPreparer):
    """Las tablas del esquema «contlity» (núcleo) se escriben sin calificar."""

    def __init__(self, dialect: Any, **kw: Any):
        super().__init__(dialect, **kw)
        self._envolver()

    def _envolver(self) -> None:
        original = self.schema_for_object

        def _esquema(obj: Any) -> Any:
            s = original(obj)
            return None if s == NUCLEO else s
        self.schema_for_object = _esquema

    def _with_schema_translate(self, schema_translate_map: Any) -> Any:
        prep = super()._with_schema_translate(schema_translate_map)
        prep._envolver()
        return prep


class ContlityDialect(PGDialect):
    name = "contlity"
    driver = "contlity"
    supports_statement_cache = True
    default_paramstyle = "pyformat"
    preparer = ContlityIdentifierPreparer
    supports_sane_rowcount = False
    supports_sane_multi_rowcount = False
    postfetch_lastrowid = False
    implicit_returning = False

    @classmethod
    def import_dbapi(cls) -> Any:
        return _dbapi

    @classmethod
    def dbapi(cls) -> Any:  # SQLAlchemy < 2.0
        return _dbapi

    def create_connect_args(self, url: Any) -> tuple[list, dict]:
        q = dict(url.query)
        kw: dict[str, Any] = {}
        clave = url.password or url.username or q.pop("api_key", None)
        if clave:
            kw["api_key"] = str(clave)
        if url.host:
            kw["base_url"] = q.pop("base_url", None) or f"https://{url.host}{f':{url.port}' if url.port else ''}/api"
        elif "base_url" in q:
            kw["base_url"] = q.pop("base_url")
        if url.database:
            kw["database"] = url.database
        if "schema" in q:
            kw["schema"] = q.pop("schema")
        if "timeout" in q:
            kw["timeout"] = int(q.pop("timeout"))
        return [], kw

    # ── Sesión: sin transacciones ni variables de servidor ───────────────────

    def initialize(self, connection: Any) -> None:
        default.DefaultDialect.initialize(self, connection)
        self.supports_smallserial = True
        self._backslash_escapes = False
        self._supports_drop_index_concurrently = True
        self.supports_identity_columns = True
        self._supports_jsonb_subscripting = True

    def _get_server_version_info(self, connection: Any) -> tuple:
        return (16, 0)

    def _get_default_schema_name(self, connection: Any) -> str:
        return NUCLEO

    def _check_max_identifier_length(self, connection: Any) -> None:
        return None

    def _set_backslash_escapes(self, connection: Any) -> None:
        self._backslash_escapes = False

    def get_isolation_level_values(self, dbapi_conn: Any) -> tuple:
        return ("READ COMMITTED", "AUTOCOMMIT")

    def get_isolation_level(self, dbapi_connection: Any) -> str:
        return "READ COMMITTED"

    def get_default_isolation_level(self, dbapi_conn: Any) -> str:
        return "READ COMMITTED"

    def set_isolation_level(self, dbapi_connection: Any, level: str) -> None:
        pass

    def reset_isolation_level(self, dbapi_connection: Any) -> None:
        pass

    def do_begin(self, dbapi_connection: Any) -> None:
        pass

    def do_rollback(self, dbapi_connection: Any) -> None:
        pass

    def do_commit(self, dbapi_connection: Any) -> None:
        pass

    def do_ping(self, dbapi_connection: Any) -> bool:
        dbapi_connection.catalog(refrescar=True)
        return True

    def is_disconnect(self, e: Any, connection: Any, cursor: Any) -> bool:
        return False

    # ── Catálogo (GET /v1/catalog, en caché por conexión) ────────────────────

    def _catalogo(self, connection: Any) -> dict:
        con = connection.connection.dbapi_connection
        return con.catalog()

    def _esquemas(self, connection: Any) -> dict:
        return {s["name"]: {t["name"]: t for t in s["tables"]} for s in self._catalogo(connection)["schemas"]}

    def _tablas(self, connection: Any, schema: Any) -> dict:
        return self._esquemas(connection).get(schema or NUCLEO, {})

    def _tabla(self, connection: Any, table_name: str, schema: Any) -> dict:
        from sqlalchemy import exc
        t = self._tablas(connection, schema).get(table_name)
        if t is None:
            raise exc.NoSuchTableError(f"{schema}.{table_name}" if schema else table_name)
        return t

    def get_schema_names(self, connection: Any, **kw: Any) -> list:
        return list(self._esquemas(connection))

    def has_schema(self, connection: Any, schema: str, **kw: Any) -> bool:
        return schema in self._esquemas(connection)

    def _nombres(self, connection: Any, schema: Any, tipos: tuple) -> list:
        return [n for n, t in self._tablas(connection, schema).items() if t.get("type", "table") in tipos]

    def get_table_names(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return self._nombres(connection, schema, ("table",))

    def get_view_names(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return self._nombres(connection, schema, ("view",))

    def get_materialized_view_names(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return self._nombres(connection, schema, ("materialized_view",))

    def get_temp_table_names(self, connection: Any, **kw: Any) -> list:
        return []

    def get_temp_view_names(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return []

    def get_sequence_names(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return []

    def has_table(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> bool:
        return table_name in self._tablas(connection, schema)

    def has_sequence(self, connection: Any, sequence_name: str, schema: Any = None, **kw: Any) -> bool:
        return False

    def has_type(self, connection: Any, type_name: str, schema: Any = None, **kw: Any) -> bool:
        return False

    def get_view_definition(self, connection: Any, view_name: str, schema: Any = None, **kw: Any) -> None:
        return None

    def get_enums(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return []

    def get_domains(self, connection: Any, schema: Any = None, **kw: Any) -> list:
        return []

    def _tipo(self, nombre: str) -> Any:
        n = (nombre or "text").lower().strip()
        if n.endswith("[]") or n == "array":
            return sqltypes.ARRAY(sqltypes.NullType())
        base = re.sub(r"\(.*\)", "", n).strip()
        args = [int(x) for x in re.findall(r"\d+", n[len(base):])] if "(" in n else []
        alias = {"double": "double precision", "timestamptz": "timestamp with time zone",
                 "varchar": "character varying"}
        cls = self.ischema_names.get(alias.get(base, base))
        if cls is None:
            return sqltypes.NULLTYPE
        try:
            return cls(*args) if args and base in ("numeric", "character varying", "character") else cls()
        except TypeError:
            return cls()

    def get_columns(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> list:
        return [{"name": c["name"], "type": self._tipo(c.get("type")), "nullable": True, "default": None,
                 "autoincrement": False, "comment": c.get("description")}
                for c in self._tabla(connection, table_name, schema)["columns"]]

    def get_pk_constraint(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> dict:
        return {"constrained_columns": [], "name": None}

    def get_foreign_keys(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> list:
        return []

    def get_indexes(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> list:
        return []

    def get_unique_constraints(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> list:
        return []

    def get_check_constraints(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> list:
        return []

    def get_table_comment(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> dict:
        return {"text": self._tabla(connection, table_name, schema).get("description")}

    def get_table_options(self, connection: Any, table_name: str, schema: Any = None, **kw: Any) -> dict:
        return {}

    # Las versiones «multi» de PostgreSQL consultan pg_catalog; se usan las genéricas (por tabla)
    get_multi_columns = default.DefaultDialect.get_multi_columns
    get_multi_pk_constraint = default.DefaultDialect.get_multi_pk_constraint
    get_multi_foreign_keys = default.DefaultDialect.get_multi_foreign_keys
    get_multi_indexes = default.DefaultDialect.get_multi_indexes
    get_multi_unique_constraints = default.DefaultDialect.get_multi_unique_constraints
    get_multi_check_constraints = default.DefaultDialect.get_multi_check_constraints
    get_multi_table_comment = default.DefaultDialect.get_multi_table_comment
    get_multi_table_options = default.DefaultDialect.get_multi_table_options


dialect = ContlityDialect
