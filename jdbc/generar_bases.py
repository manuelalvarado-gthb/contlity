"""Genera las clases base del driver (Base*.java) a partir de `javap -public java.sql.<Interfaz>`.

Cada método abstracto de la interfaz queda con un cuerpo por omisión: en DatabaseMetaData un valor
neutro (false, 0, null o un ResultSet vacío); en las demás, SQLFeatureNotSupportedException. Las
clases Contlity* heredan de estas y sobrescriben lo que el driver sí hace.

Uso: python3 generar_bases.py DIR_JAVAP DIR_SALIDA
"""
import re
import sys
from pathlib import Path

PAQ = "cloud.techmx.contlity.jdbc"
CLASES = {  # interfaz: (clase base, extiende)
    "Connection": ("BaseConnection", None),
    "Statement": ("BaseStatement", None),
    "PreparedStatement": ("BasePreparedStatement", "ContlityStatement"),
    "ResultSet": ("BaseResultSet", None),
    "ResultSetMetaData": ("BaseResultSetMetaData", None),
    "DatabaseMetaData": ("BaseDatabaseMetaData", None),
}
METODO = re.compile(r"^\s*public abstract (?P<gen><[^>]+> )?(?P<ret>[\w.<>\[\]?, ]+?) (?P<nom>\w+)\((?P<par>.*)\)(?: throws (?P<thr>[\w., ]+))?;$")


def params(s: str) -> list[str]:
    out, prof, act = [], 0, ""
    for ch in s:
        if ch == "<":
            prof += 1
        elif ch == ">":
            prof -= 1
        if ch == "," and prof == 0:
            out.append(act.strip()); act = ""
        else:
            act += ch
    if act.strip():
        out.append(act.strip())
    return out


def neutro(ret: str) -> str:
    if ret == "void":
        return ""
    if ret == "boolean":
        return "return false;"
    if ret in ("int", "long", "short", "byte", "float", "double"):
        return "return 0;"
    if ret == "java.lang.String":
        return 'return "";'
    if ret == "java.sql.ResultSet":
        return "return ContlityResultSet.vacio();"
    if ret == "java.sql.RowIdLifetime":
        return "return java.sql.RowIdLifetime.ROWID_UNSUPPORTED;"
    return "return null;"


def generar(interfaz: str, txt: str) -> str:
    base, ext = CLASES[interfaz]
    metodos = []
    for linea in txt.splitlines():
        m = METODO.match(linea)
        if not m:
            continue
        ps = params(m["par"])
        firma = ", ".join(f"{t} p{i}" for i, t in enumerate(ps))
        thr = f" throws {m['thr']}" if m["thr"] else ""
        if interfaz == "DatabaseMetaData":
            cuerpo = neutro(m["ret"].strip())
        else:
            cuerpo = f'throw Util.noSoportado("{interfaz}.{m["nom"]}");'
            if "SQLException" not in (m["thr"] or ""):
                cuerpo = f'throw new UnsupportedOperationException("Contlity: {interfaz}.{m["nom"]} no está soportado");'
        metodos.append(f"    public {m['gen'] or ''}{m['ret'].strip()} {m['nom']}({firma}){thr} {{ {cuerpo} }}")
    cabeza = f"public abstract class {base}" + (f" extends {ext}" if ext else "") + f" implements java.sql.{interfaz}"
    constructor = f"\n    protected {base}(ContlityConnection c) {{ super(c); }}\n" if ext else ""
    wrapper = constructor if ext else """
    public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new java.sql.SQLException("No envuelve " + iface.getName());
    }
    public boolean isWrapperFor(Class<?> iface) { return iface.isInstance(this); }
"""
    return (f"// Generado por generar_bases.py a partir de java.sql.{interfaz}. No editar a mano.\n"
            f"package {PAQ};\n\n@SuppressWarnings({{\"unused\", \"deprecation\"}})\n{cabeza} {{\n"
            + "\n".join(metodos) + "\n" + wrapper + "}\n")


if __name__ == "__main__":
    ent, sal = Path(sys.argv[1]), Path(sys.argv[2])
    for interfaz, (base, _) in CLASES.items():
        (sal / f"{base}.java").write_text(generar(interfaz, (ent / f"{interfaz}.txt").read_text()))
        print(base)
