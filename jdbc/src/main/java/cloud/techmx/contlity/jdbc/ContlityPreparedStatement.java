package cloud.techmx.contlity.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Los parámetros «?» se incrustan como literales seguros (texto con comillas duplicadas, números,
 * fechas tipadas) y la sentencia viaja completa: el servidor recibe una sola consulta de lectura.
 */
public class ContlityPreparedStatement extends BasePreparedStatement {
    private final List<String> partes = new ArrayList<>();   // texto entre marcadores
    private final Object[] valores;
    private final boolean[] puestos;
    private static final Object NULO = new Object();

    ContlityPreparedStatement(ContlityConnection con, String sql) {
        super(con);
        partir(sql);
        valores = new Object[partes.size() - 1];
        puestos = new boolean[valores.length];
    }

    /** Divide en los «?» que no están en textos, identificadores, comentarios ni cuerpos $$. */
    private void partir(String sql) {
        int n = sql.length(), ini = 0, k = 0;
        while (k < n) {
            char c = sql.charAt(k);
            if (c == '\'' || c == '"') {
                int j = k + 1;
                while (j < n) {
                    if (sql.charAt(j) == c) {
                        if (j + 1 < n && sql.charAt(j + 1) == c) { j += 2; continue; }
                        break;
                    }
                    j++;
                }
                k = j + 1;
            } else if (c == '-' && sql.startsWith("--", k)) {
                int j = sql.indexOf('\n', k);
                k = j < 0 ? n : j;
            } else if (c == '/' && sql.startsWith("/*", k)) {
                int j = sql.indexOf("*/", k + 2);
                k = j < 0 ? n : j + 2;
            } else if (c == '$') {
                int j = k + 1;
                while (j < n && (Character.isLetterOrDigit(sql.charAt(j)) || sql.charAt(j) == '_')) j++;
                if (j < n && sql.charAt(j) == '$' && (k == 0 || !Character.isLetterOrDigit(sql.charAt(k - 1)))) {
                    String tag = sql.substring(k, j + 1);
                    int fin = sql.indexOf(tag, j + 1);
                    k = fin < 0 ? n : fin + tag.length();
                } else {
                    k = j;
                }
            } else if (c == '?' && !(k + 1 < n && (sql.charAt(k + 1) == '|' || sql.charAt(k + 1) == '&' || sql.charAt(k + 1) == '?'))
                    && !(k > 0 && sql.charAt(k - 1) == '?')) {
                partes.add(sql.substring(ini, k));
                ini = k + 1;
                k++;
            } else {
                k++;
            }
        }
        partes.add(sql.substring(ini));
    }

    String armar() throws SQLException {
        StringBuilder b = new StringBuilder(partes.get(0));
        for (int k = 0; k < valores.length; k++) {
            if (!puestos[k]) throw new SQLException("Falta el parámetro " + (k + 1), "07001");
            b.append(valores[k] == NULO ? "NULL" : Util.literal(valores[k])).append(partes.get(k + 1));
        }
        return b.toString();
    }

    private void poner(int i, Object v) throws SQLException {
        abierta();
        if (i < 1 || i > valores.length) throw new SQLException("Parámetro fuera de rango: " + i, "07009");
        valores[i - 1] = v == null ? NULO : v;
        puestos[i - 1] = true;
    }

    @Override
    public ResultSet executeQuery() throws SQLException { return consultar(armar()); }

    @Override
    public boolean execute() throws SQLException { consultar(armar()); return true; }

    @Override
    public int executeUpdate() throws SQLException { return executeUpdate(""); }

    @Override
    public void clearParameters() {
        java.util.Arrays.fill(valores, null);
        java.util.Arrays.fill(puestos, false);
    }

    /** Columnas sin traer datos: la consulta envuelta con LIMIT 0 (parámetros faltantes como NULL). */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        StringBuilder b = new StringBuilder(partes.get(0));
        for (int k = 0; k < valores.length; k++) {
            b.append(puestos[k] && valores[k] != NULO ? Util.literal(valores[k]) : "NULL").append(partes.get(k + 1));
        }
        String sql = b.toString().trim().replaceAll(";+\\s*$", "");
        String w = sql.replaceAll("^(\\s*(--[^\\n]*\\n|/\\*.*?\\*/))*\\s*", "").toUpperCase();
        if (!(w.startsWith("SELECT") || w.startsWith("WITH") || w.startsWith("TABLE") || w.startsWith("VALUES"))) return null;
        ContlityStatement s = new ContlityStatement(con);
        try (ResultSet rs = s.consultar("SELECT * FROM (" + sql + "\n) contlity_q LIMIT 0")) {
            return rs.getMetaData();
        } finally {
            s.close();
        }
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        final int n = valores.length;
        return new ParameterMetaData() {
            public int getParameterCount() { return n; }
            public int isNullable(int p) { return parameterNullableUnknown; }
            public boolean isSigned(int p) { return true; }
            public int getPrecision(int p) { return 0; }
            public int getScale(int p) { return 0; }
            public int getParameterType(int p) { return Types.VARCHAR; }
            public String getParameterTypeName(int p) { return "text"; }
            public String getParameterClassName(int p) { return String.class.getName(); }
            public int getParameterMode(int p) { return parameterModeIn; }
            public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("No envuelve " + iface.getName()); }
            public boolean isWrapperFor(Class<?> iface) { return false; }
        };
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    @Override public void setNull(int i, int sqlType) throws SQLException { poner(i, null); }
    @Override public void setNull(int i, int sqlType, String typeName) throws SQLException { poner(i, null); }
    @Override public void setBoolean(int i, boolean x) throws SQLException { poner(i, x); }
    @Override public void setByte(int i, byte x) throws SQLException { poner(i, x); }
    @Override public void setShort(int i, short x) throws SQLException { poner(i, x); }
    @Override public void setInt(int i, int x) throws SQLException { poner(i, x); }
    @Override public void setLong(int i, long x) throws SQLException { poner(i, x); }
    @Override public void setFloat(int i, float x) throws SQLException { poner(i, x); }
    @Override public void setDouble(int i, double x) throws SQLException { poner(i, x); }
    @Override public void setBigDecimal(int i, BigDecimal x) throws SQLException { poner(i, x); }
    @Override public void setString(int i, String x) throws SQLException { poner(i, x); }
    @Override public void setNString(int i, String x) throws SQLException { poner(i, x); }
    @Override public void setBytes(int i, byte[] x) throws SQLException { poner(i, x); }
    @Override public void setDate(int i, Date x) throws SQLException { poner(i, x); }
    @Override public void setTime(int i, Time x) throws SQLException { poner(i, x); }
    @Override public void setTimestamp(int i, Timestamp x) throws SQLException { poner(i, x); }
    @Override public void setDate(int i, Date x, Calendar cal) throws SQLException { poner(i, x); }
    @Override public void setTime(int i, Time x, Calendar cal) throws SQLException { poner(i, x); }
    @Override public void setTimestamp(int i, Timestamp x, Calendar cal) throws SQLException { poner(i, x); }
    @Override public void setObject(int i, Object x) throws SQLException { poner(i, x); }
    @Override public void setObject(int i, Object x, int sqlType) throws SQLException { poner(i, x); }
    @Override public void setObject(int i, Object x, int sqlType, int scale) throws SQLException { poner(i, x); }

    @Override
    public void setCharacterStream(int i, Reader r, int length) throws SQLException { poner(i, leer(r)); }

    @Override
    public void setCharacterStream(int i, Reader r, long length) throws SQLException { poner(i, leer(r)); }

    @Override
    public void setCharacterStream(int i, Reader r) throws SQLException { poner(i, leer(r)); }

    @Override
    public void setAsciiStream(int i, InputStream in, int length) throws SQLException { poner(i, leer(in)); }

    @Override
    public void setBinaryStream(int i, InputStream in, int length) throws SQLException { poner(i, bytes(in)); }

    private static String leer(Reader r) throws SQLException {
        if (r == null) return null;
        try {
            StringBuilder b = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) > 0) b.append(buf, 0, n);
            return b.toString();
        } catch (java.io.IOException e) {
            throw new SQLException("No se pudo leer el parámetro", "HY000", e);
        }
    }

    private static String leer(InputStream in) throws SQLException {
        byte[] b = bytes(in);
        return b == null ? null : new String(b, java.nio.charset.StandardCharsets.US_ASCII);
    }

    private static byte[] bytes(InputStream in) throws SQLException {
        if (in == null) return null;
        try {
            return in.readAllBytes();
        } catch (java.io.IOException e) {
            throw new SQLException("No se pudo leer el parámetro", "HY000", e);
        }
    }

    @Override
    public void addBatch() throws SQLException { throw Util.noSoportado("addBatch"); }
}
