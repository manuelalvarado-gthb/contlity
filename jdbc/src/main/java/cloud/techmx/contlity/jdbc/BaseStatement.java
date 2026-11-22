// Generado por generar_bases.py a partir de java.sql.Statement. No editar a mano.
package cloud.techmx.contlity.jdbc;

@SuppressWarnings({"unused", "deprecation"})
public abstract class BaseStatement implements java.sql.Statement {
    public java.sql.ResultSet executeQuery(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.executeQuery"); }
    public int executeUpdate(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.executeUpdate"); }
    public void close() throws java.sql.SQLException { throw Util.noSoportado("Statement.close"); }
    public int getMaxFieldSize() throws java.sql.SQLException { throw Util.noSoportado("Statement.getMaxFieldSize"); }
    public void setMaxFieldSize(int p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setMaxFieldSize"); }
    public int getMaxRows() throws java.sql.SQLException { throw Util.noSoportado("Statement.getMaxRows"); }
    public void setMaxRows(int p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setMaxRows"); }
    public void setEscapeProcessing(boolean p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setEscapeProcessing"); }
    public int getQueryTimeout() throws java.sql.SQLException { throw Util.noSoportado("Statement.getQueryTimeout"); }
    public void setQueryTimeout(int p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setQueryTimeout"); }
    public void cancel() throws java.sql.SQLException { throw Util.noSoportado("Statement.cancel"); }
    public java.sql.SQLWarning getWarnings() throws java.sql.SQLException { throw Util.noSoportado("Statement.getWarnings"); }
    public void clearWarnings() throws java.sql.SQLException { throw Util.noSoportado("Statement.clearWarnings"); }
    public void setCursorName(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setCursorName"); }
    public boolean execute(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.execute"); }
    public java.sql.ResultSet getResultSet() throws java.sql.SQLException { throw Util.noSoportado("Statement.getResultSet"); }
    public int getUpdateCount() throws java.sql.SQLException { throw Util.noSoportado("Statement.getUpdateCount"); }
    public boolean getMoreResults() throws java.sql.SQLException { throw Util.noSoportado("Statement.getMoreResults"); }
    public void setFetchDirection(int p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setFetchDirection"); }
    public int getFetchDirection() throws java.sql.SQLException { throw Util.noSoportado("Statement.getFetchDirection"); }
    public void setFetchSize(int p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setFetchSize"); }
    public int getFetchSize() throws java.sql.SQLException { throw Util.noSoportado("Statement.getFetchSize"); }
    public int getResultSetConcurrency() throws java.sql.SQLException { throw Util.noSoportado("Statement.getResultSetConcurrency"); }
    public int getResultSetType() throws java.sql.SQLException { throw Util.noSoportado("Statement.getResultSetType"); }
    public void addBatch(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.addBatch"); }
    public void clearBatch() throws java.sql.SQLException { throw Util.noSoportado("Statement.clearBatch"); }
    public int[] executeBatch() throws java.sql.SQLException { throw Util.noSoportado("Statement.executeBatch"); }
    public java.sql.Connection getConnection() throws java.sql.SQLException { throw Util.noSoportado("Statement.getConnection"); }
    public boolean getMoreResults(int p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.getMoreResults"); }
    public java.sql.ResultSet getGeneratedKeys() throws java.sql.SQLException { throw Util.noSoportado("Statement.getGeneratedKeys"); }
    public int executeUpdate(java.lang.String p0, int p1) throws java.sql.SQLException { throw Util.noSoportado("Statement.executeUpdate"); }
    public int executeUpdate(java.lang.String p0, int[] p1) throws java.sql.SQLException { throw Util.noSoportado("Statement.executeUpdate"); }
    public int executeUpdate(java.lang.String p0, java.lang.String[] p1) throws java.sql.SQLException { throw Util.noSoportado("Statement.executeUpdate"); }
    public boolean execute(java.lang.String p0, int p1) throws java.sql.SQLException { throw Util.noSoportado("Statement.execute"); }
    public boolean execute(java.lang.String p0, int[] p1) throws java.sql.SQLException { throw Util.noSoportado("Statement.execute"); }
    public boolean execute(java.lang.String p0, java.lang.String[] p1) throws java.sql.SQLException { throw Util.noSoportado("Statement.execute"); }
    public int getResultSetHoldability() throws java.sql.SQLException { throw Util.noSoportado("Statement.getResultSetHoldability"); }
    public boolean isClosed() throws java.sql.SQLException { throw Util.noSoportado("Statement.isClosed"); }
    public void setPoolable(boolean p0) throws java.sql.SQLException { throw Util.noSoportado("Statement.setPoolable"); }
    public boolean isPoolable() throws java.sql.SQLException { throw Util.noSoportado("Statement.isPoolable"); }
    public void closeOnCompletion() throws java.sql.SQLException { throw Util.noSoportado("Statement.closeOnCompletion"); }
    public boolean isCloseOnCompletion() throws java.sql.SQLException { throw Util.noSoportado("Statement.isCloseOnCompletion"); }

    public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new java.sql.SQLException("No envuelve " + iface.getName());
    }
    public boolean isWrapperFor(Class<?> iface) { return iface.isInstance(this); }
}
