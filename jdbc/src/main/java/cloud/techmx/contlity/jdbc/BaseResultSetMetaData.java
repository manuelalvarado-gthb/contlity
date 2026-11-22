// Generado por generar_bases.py a partir de java.sql.ResultSetMetaData. No editar a mano.
package cloud.techmx.contlity.jdbc;

@SuppressWarnings({"unused", "deprecation"})
public abstract class BaseResultSetMetaData implements java.sql.ResultSetMetaData {
    public int getColumnCount() throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnCount"); }
    public boolean isAutoIncrement(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isAutoIncrement"); }
    public boolean isCaseSensitive(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isCaseSensitive"); }
    public boolean isSearchable(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isSearchable"); }
    public boolean isCurrency(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isCurrency"); }
    public int isNullable(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isNullable"); }
    public boolean isSigned(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isSigned"); }
    public int getColumnDisplaySize(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnDisplaySize"); }
    public java.lang.String getColumnLabel(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnLabel"); }
    public java.lang.String getColumnName(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnName"); }
    public java.lang.String getSchemaName(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getSchemaName"); }
    public int getPrecision(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getPrecision"); }
    public int getScale(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getScale"); }
    public java.lang.String getTableName(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getTableName"); }
    public java.lang.String getCatalogName(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getCatalogName"); }
    public int getColumnType(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnType"); }
    public java.lang.String getColumnTypeName(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnTypeName"); }
    public boolean isReadOnly(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isReadOnly"); }
    public boolean isWritable(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isWritable"); }
    public boolean isDefinitelyWritable(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.isDefinitelyWritable"); }
    public java.lang.String getColumnClassName(int p0) throws java.sql.SQLException { throw Util.noSoportado("ResultSetMetaData.getColumnClassName"); }

    public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new java.sql.SQLException("No envuelve " + iface.getName());
    }
    public boolean isWrapperFor(Class<?> iface) { return iface.isInstance(this); }
}
