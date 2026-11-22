package cloud.techmx.contlity.jdbc;

import java.sql.SQLException;
import java.sql.Types;

public class ContlityResultSetMetaData extends BaseResultSetMetaData {
    private final String[] nombres;
    private final Util.Tipo[] tipos;

    ContlityResultSetMetaData(String[] nombres, Util.Tipo[] tipos) {
        this.nombres = nombres;
        this.tipos = tipos;
    }

    private Util.Tipo t(int c) throws SQLException {
        if (c < 1 || c > tipos.length) throw new SQLException("Columna fuera de rango: " + c, "07009");
        return tipos[c - 1];
    }

    @Override public int getColumnCount() { return nombres.length; }
    @Override public String getColumnName(int c) throws SQLException { t(c); return nombres[c - 1]; }
    @Override public String getColumnLabel(int c) throws SQLException { return getColumnName(c); }
    @Override public int getColumnType(int c) throws SQLException { return t(c).sql; }
    @Override public String getColumnTypeName(int c) throws SQLException { return t(c).nombre; }
    @Override public String getColumnClassName(int c) throws SQLException { return t(c).clase; }
    @Override public int getPrecision(int c) throws SQLException { return t(c).precision; }
    @Override public int getScale(int c) throws SQLException { return t(c).escala; }

    @Override
    public int getColumnDisplaySize(int c) throws SQLException {
        Util.Tipo x = t(c);
        switch (x.sql) {
            case Types.BOOLEAN: return 5;
            case Types.DATE: return 10;
            case Types.TIMESTAMP: return 29;
            case Types.NUMERIC: return x.precision + 2;
            default: return Math.min(x.precision, 65535);
        }
    }

    @Override public int isNullable(int c) throws SQLException { t(c); return columnNullableUnknown; }

    @Override
    public boolean isSigned(int c) throws SQLException {
        int s = t(c).sql;
        return s == Types.SMALLINT || s == Types.INTEGER || s == Types.BIGINT || s == Types.REAL || s == Types.DOUBLE || s == Types.NUMERIC;
    }

    @Override public boolean isCaseSensitive(int c) throws SQLException { int s = t(c).sql; return s == Types.VARCHAR || s == Types.CHAR; }
    @Override public boolean isSearchable(int c) throws SQLException { t(c); return true; }
    @Override public boolean isCurrency(int c) throws SQLException { t(c); return false; }
    @Override public boolean isAutoIncrement(int c) throws SQLException { t(c); return false; }
    @Override public boolean isReadOnly(int c) throws SQLException { t(c); return true; }
    @Override public boolean isWritable(int c) throws SQLException { t(c); return false; }
    @Override public boolean isDefinitelyWritable(int c) throws SQLException { t(c); return false; }
    @Override public String getTableName(int c) throws SQLException { t(c); return ""; }
    @Override public String getSchemaName(int c) throws SQLException { t(c); return ""; }
    @Override public String getCatalogName(int c) throws SQLException { t(c); return ""; }
}
