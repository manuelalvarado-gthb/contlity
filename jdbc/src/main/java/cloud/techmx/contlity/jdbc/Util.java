package cloud.techmx.contlity.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Util {
    private Util() {}

    static SQLFeatureNotSupportedException noSoportado(String que) {
        return new SQLFeatureNotSupportedException("Contlity: " + que + " no está soportado (conexión de solo lectura)", "0A000");
    }

    // ── Tipos ────────────────────────────────────────────────────────────────

    /** Tipo de Contlity/PostgreSQL ("numeric(14,2)", "timestamp with time zone", "integer[]"...). */
    static final class Tipo {
        final String nombre;     // tal como lo reporta el servidor
        final int sql;           // java.sql.Types
        final int precision, escala;
        final String clase;

        Tipo(String nombre, int sql, int precision, int escala, String clase) {
            this.nombre = nombre; this.sql = sql; this.precision = precision; this.escala = escala; this.clase = clase;
        }
    }

    private static final Pattern PARAMS = Pattern.compile("\\((\\d+)(?:\\s*,\\s*(\\d+))?\\)");

    static Tipo tipo(String nombre) {
        String n = nombre == null ? "text" : nombre.trim().toLowerCase();
        int p = 0, e = 0;
        Matcher m = PARAMS.matcher(n);
        if (m.find()) {
            p = Integer.parseInt(m.group(1));
            if (m.group(2) != null) e = Integer.parseInt(m.group(2));
        }
        String b = n.replaceAll("\\(.*?\\)", "").trim();
        if (b.endsWith("[]") || b.equals("array")) return new Tipo(nombre, Types.VARCHAR, 65535, 0, String.class.getName());
        switch (b) {
            case "boolean": case "bool":
                return new Tipo("boolean", Types.BOOLEAN, 1, 0, Boolean.class.getName());
            case "smallint": case "int2":
                return new Tipo("smallint", Types.SMALLINT, 5, 0, Integer.class.getName());
            case "integer": case "int": case "int4": case "serial":
                return new Tipo("integer", Types.INTEGER, 10, 0, Integer.class.getName());
            case "bigint": case "int8": case "bigserial":
                return new Tipo("bigint", Types.BIGINT, 19, 0, Long.class.getName());
            case "real": case "float4":
                return new Tipo("real", Types.REAL, 8, 8, Float.class.getName());
            case "double": case "double precision": case "float8": case "float":
                return new Tipo("double precision", Types.DOUBLE, 17, 17, Double.class.getName());
            case "numeric": case "decimal":
                return new Tipo(nombre, Types.NUMERIC, p > 0 ? p : 38, p > 0 ? e : 6, BigDecimal.class.getName());
            case "date":
                return new Tipo("date", Types.DATE, 10, 0, java.sql.Date.class.getName());
            case "timestamp": case "timestamp without time zone":
                return new Tipo("timestamp", Types.TIMESTAMP, 26, 6, java.sql.Timestamp.class.getName());
            case "timestamptz": case "timestamp with time zone":
                return new Tipo("timestamptz", Types.TIMESTAMP, 32, 6, java.sql.Timestamp.class.getName());
            case "time": case "time without time zone":
                return new Tipo("time", Types.TIME, 15, 6, java.sql.Time.class.getName());
            case "bytea":
                return new Tipo("bytea", Types.VARBINARY, 65535, 0, "[B");
            case "character": case "char": case "bpchar":
                return new Tipo(nombre, Types.CHAR, p > 0 ? p : 1, 0, String.class.getName());
            case "character varying": case "varchar":
                return new Tipo(nombre, Types.VARCHAR, p > 0 ? p : 65535, 0, String.class.getName());
            default:   // text, uuid, json, jsonb, interval, ...
                return new Tipo(nombre, Types.VARCHAR, 65535, 0, String.class.getName());
        }
    }

    // ── Literales para PreparedStatement ─────────────────────────────────────

    static String literal(Object v) throws SQLException {
        if (v == null) return "NULL";
        if (v instanceof Boolean) return ((Boolean) v) ? "TRUE" : "FALSE";
        if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte || v instanceof BigInteger)
            return v.toString();
        if (v instanceof BigDecimal) return ((BigDecimal) v).toPlainString();
        if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) return "'" + d + "'::float8";
            return BigDecimal.valueOf(d).toPlainString();
        }
        if (v instanceof java.sql.Timestamp) return "'" + ((java.sql.Timestamp) v).toLocalDateTime() + "'::timestamp";
        if (v instanceof java.sql.Date) return "'" + ((java.sql.Date) v).toLocalDate() + "'::date";
        if (v instanceof java.sql.Time) return "'" + ((java.sql.Time) v).toLocalTime() + "'::time";
        if (v instanceof java.util.Date) return "'" + new java.sql.Timestamp(((java.util.Date) v).getTime()).toLocalDateTime() + "'::timestamp";
        if (v instanceof LocalDateTime) return "'" + v + "'::timestamp";
        if (v instanceof OffsetDateTime) return "'" + v + "'::timestamptz";
        if (v instanceof LocalDate) return "'" + v + "'::date";
        if (v instanceof LocalTime) return "'" + v + "'::time";
        if (v instanceof UUID) return "'" + v + "'::uuid";
        if (v instanceof byte[]) {
            StringBuilder b = new StringBuilder("'\\x");
            for (byte x : (byte[]) v) b.append(String.format("%02x", x));
            return b.append("'::bytea").toString();
        }
        return texto(v.toString());
    }

    static String texto(String s) throws SQLException {
        if (s.indexOf('\0') >= 0) throw new SQLException("El texto contiene un carácter nulo", "22021");
        String t = s.replace("'", "''");
        if (t.indexOf('\\') >= 0) return "E'" + t.replace("\\", "\\\\") + "'";
        return "'" + t + "'";
    }

    // ── Patrones de DatabaseMetaData (% y _, escape \) ───────────────────────

    static boolean coincide(String patron, String valor) {
        if (patron == null) return true;
        if (valor == null) return false;
        StringBuilder re = new StringBuilder();
        for (int k = 0; k < patron.length(); k++) {
            char c = patron.charAt(k);
            if (c == '\\' && k + 1 < patron.length()) re.append(Pattern.quote(String.valueOf(patron.charAt(++k))));
            else if (c == '%') re.append(".*");
            else if (c == '_') re.append('.');
            else re.append(Pattern.quote(String.valueOf(c)));
        }
        return Pattern.compile(re.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(valor).matches();
    }
}
