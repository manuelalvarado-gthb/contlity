package cloud.techmx.contlity.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Resultado de solo avance: la primera página llega con la consulta; las demás se piden al recorrerlo. */
public class ContlityResultSet extends BaseResultSet {
    final String[] nombres;
    final Util.Tipo[] tipos;
    private final ContlityStatement stmt;
    private List<Object> filas;
    private String rid;
    private final int paginas;
    private int sig = 1;
    private int i = -1;
    private long fila = 0;
    private final long total;
    private final int maxRows;
    private List<Object> actual;
    private boolean nulo, cerrado, alFinal;

    ContlityResultSet(ContlityStatement stmt, List<Map<String, Object>> cols, List<Object> filas, String rid,
                      int paginas, long total, int maxRows) {
        this.stmt = stmt;
        int n = cols == null ? 0 : cols.size();
        nombres = new String[n];
        tipos = new Util.Tipo[n];
        for (int k = 0; k < n; k++) {
            nombres[k] = String.valueOf(cols.get(k).get("name"));
            tipos[k] = Util.tipo((String) cols.get(k).get("type"));
        }
        this.filas = filas == null ? new ArrayList<>() : filas;
        this.rid = rid;
        this.paginas = paginas;
        this.total = total;
        this.maxRows = maxRows;
    }

    /** Resultado en memoria (metadatos): tipos como «text», «integer», «smallint», «boolean», «bigint». */
    static ContlityResultSet tabla(String[] nombres, String[] tipos, List<Object[]> filas) {
        List<Map<String, Object>> cols = new ArrayList<>();
        for (int k = 0; k < nombres.length; k++) cols.add(Map.of("name", nombres[k], "type", tipos[k]));
        List<Object> fs = new ArrayList<>();
        for (Object[] f : filas) fs.add(java.util.Arrays.asList(f));
        return new ContlityResultSet(null, cols, fs, null, 1, filas.size(), 0);
    }

    static ContlityResultSet vacio() {
        return tabla(new String[0], new String[0], Collections.emptyList());
    }

    // ── Recorrido ────────────────────────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public boolean next() throws SQLException {
        abierto();
        if (alFinal) return false;
        if (maxRows > 0 && fila >= maxRows) return terminar();
        i++;
        while (i >= filas.size()) {
            if (rid == null || sig >= paginas) return terminar();
            filas = stmt.con.pagina(rid, sig++);
            i = 0;
            if (sig >= paginas) soltar();
        }
        actual = (List<Object>) filas.get(i);
        fila++;
        return true;
    }

    private boolean terminar() {
        alFinal = true;
        actual = null;
        soltar();
        return false;
    }

    private void soltar() {
        if (rid != null && stmt != null) {
            String r = rid;
            rid = null;
            stmt.con.liberar(r);
        }
    }

    @Override
    public void close() throws SQLException {
        if (cerrado) return;
        cerrado = true;
        soltar();
        filas = Collections.emptyList();
        if (stmt != null) stmt.resultadoCerrado(this);
    }

    @Override
    public boolean isClosed() { return cerrado; }

    private void abierto() throws SQLException {
        if (cerrado) throw new SQLException("El resultado está cerrado", "HY010");
    }

    private Object valor(int col) throws SQLException {
        abierto();
        if (actual == null) throw new SQLException("No hay fila actual: llama a next()", "24000");
        if (col < 1 || col > nombres.length) throw new SQLException("Columna fuera de rango: " + col, "07009");
        Object v = actual.get(col - 1);
        nulo = v == null;
        return v;
    }

    @Override
    public boolean wasNull() { return nulo; }

    @Override
    public int findColumn(String label) throws SQLException {
        for (int k = 0; k < nombres.length; k++) if (nombres[k].equals(label)) return k + 1;
        for (int k = 0; k < nombres.length; k++) if (nombres[k].equalsIgnoreCase(label)) return k + 1;
        throw new SQLException("No existe la columna «" + label + "»", "42703");
    }

    // ── Conversión ───────────────────────────────────────────────────────────

    private static BigDecimal decimal(Object v) throws SQLException {
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return new BigDecimal(v.toString());
        if (v instanceof Boolean) return ((Boolean) v) ? BigDecimal.ONE : BigDecimal.ZERO;
        try {
            return new BigDecimal(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new SQLException("No es un número: " + v, "22018");
        }
    }

    private static Timestamp timestamp(Object v) throws SQLException {
        if (v instanceof Timestamp) return (Timestamp) v;
        String s = v.toString().trim();
        try {
            if (s.length() == 10) return Timestamp.valueOf(LocalDate.parse(s).atStartOfDay());
            String t = s.replace(' ', 'T');
            if (t.matches(".*([+-]\\d\\d(:?\\d\\d)?|Z)$") && t.indexOf('T') > 0) return Timestamp.from(OffsetDateTime.parse(
                    t.matches(".*[+-]\\d\\d$") ? t + ":00" : t).toInstant());
            return Timestamp.valueOf(LocalDateTime.parse(t));
        } catch (DateTimeParseException e) {
            throw new SQLException("No es una fecha y hora: " + s, "22007");
        }
    }

    private static Date fecha(Object v) throws SQLException {
        if (v instanceof Date) return (Date) v;
        String s = v.toString().trim();
        try {
            return Date.valueOf(LocalDate.parse(s.length() >= 10 ? s.substring(0, 10) : s));
        } catch (DateTimeParseException e) {
            throw new SQLException("No es una fecha: " + s, "22007");
        }
    }

    private static Time hora(Object v) throws SQLException {
        if (v instanceof Time) return (Time) v;
        String s = v.toString().trim();
        int t = s.indexOf('T');
        if (t >= 0) s = s.substring(t + 1);
        try {
            return Time.valueOf(LocalTime.parse(s.replaceAll("([+-]\\d\\d(:?\\d\\d)?|Z)$", "")));
        } catch (DateTimeParseException e) {
            throw new SQLException("No es una hora: " + s, "22007");
        }
    }

    private static String texto(Object v) {
        if (v == null) return null;
        if (v instanceof String) return (String) v;
        if (v instanceof BigDecimal) return ((BigDecimal) v).toPlainString();
        if (v instanceof List || v instanceof Map) return Json.escribir(v);
        return v.toString();
    }

    @Override
    public String getString(int c) throws SQLException { return texto(valor(c)); }

    @Override
    public boolean getBoolean(int c) throws SQLException {
        Object v = valor(c);
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).intValue() != 0;
        String s = v.toString().trim().toLowerCase();
        return s.equals("true") || s.equals("t") || s.equals("1") || s.equals("yes") || s.equals("y");
    }

    @Override
    public byte getByte(int c) throws SQLException { Object v = valor(c); return v == null ? 0 : decimal(v).byteValue(); }

    @Override
    public short getShort(int c) throws SQLException { Object v = valor(c); return v == null ? 0 : decimal(v).shortValue(); }

    @Override
    public int getInt(int c) throws SQLException { Object v = valor(c); return v == null ? 0 : decimal(v).intValue(); }

    @Override
    public long getLong(int c) throws SQLException { Object v = valor(c); return v == null ? 0 : decimal(v).longValue(); }

    @Override
    public float getFloat(int c) throws SQLException { Object v = valor(c); return v == null ? 0 : decimal(v).floatValue(); }

    @Override
    public double getDouble(int c) throws SQLException { Object v = valor(c); return v == null ? 0 : decimal(v).doubleValue(); }

    @Override
    public BigDecimal getBigDecimal(int c) throws SQLException { Object v = valor(c); return v == null ? null : decimal(v); }

    @Override
    @SuppressWarnings("deprecation")
    public BigDecimal getBigDecimal(int c, int scale) throws SQLException {
        BigDecimal d = getBigDecimal(c);
        return d == null ? null : d.setScale(scale, RoundingMode.HALF_UP);
    }

    @Override
    public byte[] getBytes(int c) throws SQLException {
        Object v = valor(c);
        if (v == null) return null;
        String s = texto(v);
        if (tipos[c - 1].sql == Types.VARBINARY && s.startsWith("\\x")) {
            byte[] b = new byte[(s.length() - 2) / 2];
            for (int k = 0; k < b.length; k++) b[k] = (byte) Integer.parseInt(s.substring(2 + 2 * k, 4 + 2 * k), 16);
            return b;
        }
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Date getDate(int c) throws SQLException { Object v = valor(c); return v == null ? null : fecha(v); }

    @Override
    public Time getTime(int c) throws SQLException { Object v = valor(c); return v == null ? null : hora(v); }

    @Override
    public Timestamp getTimestamp(int c) throws SQLException { Object v = valor(c); return v == null ? null : timestamp(v); }

    @Override
    public Date getDate(int c, Calendar cal) throws SQLException { return getDate(c); }

    @Override
    public Time getTime(int c, Calendar cal) throws SQLException { return getTime(c); }

    @Override
    public Timestamp getTimestamp(int c, Calendar cal) throws SQLException { return getTimestamp(c); }

    @Override
    public Object getObject(int c) throws SQLException {
        Object v = valor(c);
        if (v == null) return null;
        switch (tipos[c - 1].sql) {
            case Types.BOOLEAN: return getBoolean(c);
            case Types.SMALLINT: case Types.INTEGER: return decimal(v).intValue();
            case Types.BIGINT: return decimal(v).longValue();
            case Types.REAL: return decimal(v).floatValue();
            case Types.DOUBLE: return decimal(v).doubleValue();
            case Types.NUMERIC: return decimal(v);
            case Types.DATE: return fecha(v);
            case Types.TIMESTAMP: return timestamp(v);
            case Types.TIME: return hora(v);
            case Types.VARBINARY: return getBytes(c);
            default: return texto(v);
        }
    }

    @Override
    public Object getObject(int c, Map<String, Class<?>> map) throws SQLException { return getObject(c); }

    @Override
    public <T> T getObject(int c, Class<T> tipo) throws SQLException {
        if (tipo == null) throw new SQLException("Tipo nulo", "HY009");
        Object v = valor(c);
        if (v == null) return null;
        Object r;
        if (tipo == String.class) r = texto(v);
        else if (tipo == Integer.class) r = decimal(v).intValue();
        else if (tipo == Long.class) r = decimal(v).longValue();
        else if (tipo == Short.class) r = decimal(v).shortValue();
        else if (tipo == Double.class) r = decimal(v).doubleValue();
        else if (tipo == Float.class) r = decimal(v).floatValue();
        else if (tipo == BigDecimal.class) r = decimal(v);
        else if (tipo == Boolean.class) r = getBoolean(c);
        else if (tipo == LocalDate.class) r = fecha(v).toLocalDate();
        else if (tipo == LocalDateTime.class) r = timestamp(v).toLocalDateTime();
        else if (tipo == LocalTime.class) r = hora(v).toLocalTime();
        else if (tipo == OffsetDateTime.class) r = OffsetDateTime.parse(texto(v).replace(' ', 'T'));
        else if (tipo == Timestamp.class) r = timestamp(v);
        else if (tipo == Date.class) r = fecha(v);
        else if (tipo == Time.class) r = hora(v);
        else if (tipo == byte[].class) r = getBytes(c);
        else if (tipo == Object.class) r = getObject(c);
        else throw Util.noSoportado("getObject(" + tipo.getName() + ")");
        return tipo.cast(r);
    }

    @Override
    public Reader getCharacterStream(int c) throws SQLException { String s = getString(c); return s == null ? null : new StringReader(s); }

    @Override
    public InputStream getAsciiStream(int c) throws SQLException { byte[] b = getBytes(c); return b == null ? null : new ByteArrayInputStream(b); }

    @Override
    public InputStream getBinaryStream(int c) throws SQLException { byte[] b = getBytes(c); return b == null ? null : new ByteArrayInputStream(b); }

    @Override
    public String getNString(int c) throws SQLException { return getString(c); }

    @Override
    public Reader getNCharacterStream(int c) throws SQLException { return getCharacterStream(c); }

    // Por nombre de columna
    @Override public String getString(String l) throws SQLException { return getString(findColumn(l)); }
    @Override public boolean getBoolean(String l) throws SQLException { return getBoolean(findColumn(l)); }
    @Override public byte getByte(String l) throws SQLException { return getByte(findColumn(l)); }
    @Override public short getShort(String l) throws SQLException { return getShort(findColumn(l)); }
    @Override public int getInt(String l) throws SQLException { return getInt(findColumn(l)); }
    @Override public long getLong(String l) throws SQLException { return getLong(findColumn(l)); }
    @Override public float getFloat(String l) throws SQLException { return getFloat(findColumn(l)); }
    @Override public double getDouble(String l) throws SQLException { return getDouble(findColumn(l)); }
    @Override public BigDecimal getBigDecimal(String l) throws SQLException { return getBigDecimal(findColumn(l)); }
    @Override @SuppressWarnings("deprecation")
    public BigDecimal getBigDecimal(String l, int s) throws SQLException { return getBigDecimal(findColumn(l), s); }
    @Override public byte[] getBytes(String l) throws SQLException { return getBytes(findColumn(l)); }
    @Override public Date getDate(String l) throws SQLException { return getDate(findColumn(l)); }
    @Override public Time getTime(String l) throws SQLException { return getTime(findColumn(l)); }
    @Override public Timestamp getTimestamp(String l) throws SQLException { return getTimestamp(findColumn(l)); }
    @Override public Date getDate(String l, Calendar cal) throws SQLException { return getDate(findColumn(l)); }
    @Override public Time getTime(String l, Calendar cal) throws SQLException { return getTime(findColumn(l)); }
    @Override public Timestamp getTimestamp(String l, Calendar cal) throws SQLException { return getTimestamp(findColumn(l)); }
    @Override public Object getObject(String l) throws SQLException { return getObject(findColumn(l)); }
    @Override public Object getObject(String l, Map<String, Class<?>> m) throws SQLException { return getObject(findColumn(l)); }
    @Override public <T> T getObject(String l, Class<T> t) throws SQLException { return getObject(findColumn(l), t); }
    @Override public Reader getCharacterStream(String l) throws SQLException { return getCharacterStream(findColumn(l)); }
    @Override public InputStream getAsciiStream(String l) throws SQLException { return getAsciiStream(findColumn(l)); }
    @Override public InputStream getBinaryStream(String l) throws SQLException { return getBinaryStream(findColumn(l)); }
    @Override public String getNString(String l) throws SQLException { return getNString(findColumn(l)); }
    @Override public Reader getNCharacterStream(String l) throws SQLException { return getNCharacterStream(findColumn(l)); }

    // ── Propiedades ──────────────────────────────────────────────────────────

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        abierto();
        return new ContlityResultSetMetaData(nombres, tipos);
    }

    @Override
    public Statement getStatement() { return stmt; }

    @Override
    public int getType() { return ResultSet.TYPE_FORWARD_ONLY; }

    @Override
    public int getConcurrency() { return ResultSet.CONCUR_READ_ONLY; }

    @Override
    public int getHoldability() { return ResultSet.CLOSE_CURSORS_AT_COMMIT; }

    @Override
    public int getFetchDirection() { return ResultSet.FETCH_FORWARD; }

    @Override
    public void setFetchDirection(int d) throws SQLException {
        if (d != ResultSet.FETCH_FORWARD) throw Util.noSoportado("recorrer hacia atrás");
    }

    @Override
    public int getFetchSize() { return filas.size(); }

    @Override
    public void setFetchSize(int rows) {}

    @Override
    public int getRow() { return actual == null ? 0 : (int) fila; }

    @Override
    public boolean isBeforeFirst() { return fila == 0 && !alFinal && (!filas.isEmpty() || rid != null); }

    @Override
    public boolean isAfterLast() { return alFinal && fila > 0; }

    @Override
    public boolean isFirst() { return actual != null && fila == 1; }

    @Override
    public boolean isLast() {
        if (actual == null) return false;
        if (total >= 0) return fila == (maxRows > 0 ? Math.min(total, maxRows) : total);
        return i == filas.size() - 1 && (rid == null || sig >= paginas);
    }

    @Override
    public SQLWarning getWarnings() { return null; }

    @Override
    public void clearWarnings() {}

    @Override
    public String getCursorName() throws SQLException { throw Util.noSoportado("getCursorName"); }
}
