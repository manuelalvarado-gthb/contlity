"""Recurso: Cargas — stages, subir archivos, COPY INTO y tablas externas (alcance «cargas» de la API key)."""
from __future__ import annotations

import os
from typing import TYPE_CHECKING, Optional, Union

if TYPE_CHECKING:
    from contlity.client import ContlityClient


def _partes(stage: str) -> tuple[str, str, str, str]:
    """'@base.esquema.stage/ruta' → (base, esquema, stage, ruta)"""
    s = stage.lstrip("@")
    nombre, _, ruta = s.partition("/")
    p = nombre.split(".")
    if len(p) != 3:
        raise ValueError("El stage debe ser base.esquema.stage, por ejemplo @despacho.integraciones.entrada")
    return p[0], p[1], p[2], ruta


class CargasResource:
    def __init__(self, client: "ContlityClient"):
        self._c = client

    def stages(self) -> list[dict]:
        """Stages que ve el usuario de la API key: fqn, tipo (interno, s3, https), url, formato."""
        return self._c._request("GET", "/stages")["stages"]

    def archivos(self, stage: str) -> list[dict]:
        """
        Archivos de un stage.

        Args:
            stage: '@base.esquema.stage' o '@base.esquema.stage/carpeta/'

        Returns:
            Lista de {nombre, tamano, modificado, ...}
        """
        b, e, n, ruta = _partes(stage)
        return self._c._request("GET", f"/stages/{b}/{e}/{n}/files", params={"ruta": ruta})["files"]

    def subir(self, stage: str, archivo: Union[str, bytes], nombre: Optional[str] = None,
              sobrescribir: bool = True) -> dict:
        """
        Sube un archivo a un stage interno (hasta 50 MB).

        Args:
            stage:   '@base.esquema.stage' o con carpeta: '@base.esquema.stage/ventas/'
            archivo: ruta local del archivo o su contenido en bytes
            nombre:  nombre en el stage (por omisión, el del archivo local; obligatorio con bytes)
            sobrescribir: reemplaza si ya existe

        Uso::

            client.cargas.subir("@despacho.integraciones.entrada/ventas/", "ventas_enero.csv")
        """
        b, e, n, ruta = _partes(stage)
        if isinstance(archivo, (bytes, bytearray)):
            if not nombre:
                raise ValueError("Con bytes indica el nombre del archivo")
            datos = bytes(archivo)
        else:
            with open(archivo, "rb") as f:
                datos = f.read()
            nombre = nombre or os.path.basename(archivo)
        destino = (ruta.strip("/") + "/" if ruta.strip("/") else "") + nombre
        return self._c._request("POST", f"/stages/{b}/{e}/{n}/files",
                                params={"ruta": destino, "sobrescribir": str(sobrescribir).lower()},
                                files={"file": (nombre, datos)}, headers={"Content-Type": None})

    def copiar(self, tabla: str, stage: str, archivos: Optional[list[str]] = None, patron: Optional[str] = None,
               formato: Optional[dict] = None, on_error: str = "abort_statement", force: bool = False,
               purge: bool = False) -> dict:
        """
        COPY INTO tabla FROM @stage: carga archivos a una tabla (la crea si no existe).

        Args:
            tabla:    'base.esquema.tabla'
            stage:    '@base.esquema.stage/ruta/'
            archivos: solo estos archivos (opcional)
            patron:   expresión regular sobre el nombre (opcional)
            formato:  {"type": "csv" | "json" | "parquet" | "excel", "field_delimiter": ";", ...}
            on_error: "abort_statement" (nada se carga si algo falla) o "skip_file"
            force:    vuelve a cargar archivos ya cargados
            purge:    borra del stage los archivos cargados

        Returns:
            dict con filas cargadas y el estado de cada archivo
        """
        return self._c._request("POST", "/copy", json={
            "tabla": tabla, "stage": stage, "files": archivos, "pattern": patron, "file_format": formato,
            "on_error": on_error, "force": force, "purge": purge})

    def actualizar_tabla_externa(self, tabla: str) -> dict:
        """Vuelve a leer el origen de una tabla externa ('base.esquema.tabla')."""
        b, e, t = tabla.split(".")
        return self._c._request("POST", f"/external-tables/{b}/{e}/{t}/refresh")
