// Generado por generar_bases.py a partir de java.sql.Connection. No editar a mano.
package cloud.techmx.contlity.jdbc;

@SuppressWarnings({"unused", "deprecation"})
public abstract class BaseConnection implements java.sql.Connection {
    public java.sql.Statement createStatement() throws java.sql.SQLException { throw Util.noSoportado("Connection.createStatement"); }
    public java.sql.PreparedStatement prepareStatement(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareStatement"); }
    public java.sql.CallableStatement prepareCall(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareCall"); }
    public java.lang.String nativeSQL(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.nativeSQL"); }
    public void setAutoCommit(boolean p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setAutoCommit"); }
    public boolean getAutoCommit() throws java.sql.SQLException { throw Util.noSoportado("Connection.getAutoCommit"); }
    public void commit() throws java.sql.SQLException { throw Util.noSoportado("Connection.commit"); }
    public void rollback() throws java.sql.SQLException { throw Util.noSoportado("Connection.rollback"); }
    public void close() throws java.sql.SQLException { throw Util.noSoportado("Connection.close"); }
    public boolean isClosed() throws java.sql.SQLException { throw Util.noSoportado("Connection.isClosed"); }
    public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException { throw Util.noSoportado("Connection.getMetaData"); }
    public void setReadOnly(boolean p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setReadOnly"); }
    public boolean isReadOnly() throws java.sql.SQLException { throw Util.noSoportado("Connection.isReadOnly"); }
    public void setCatalog(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setCatalog"); }
    public java.lang.String getCatalog() throws java.sql.SQLException { throw Util.noSoportado("Connection.getCatalog"); }
    public void setTransactionIsolation(int p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setTransactionIsolation"); }
    public int getTransactionIsolation() throws java.sql.SQLException { throw Util.noSoportado("Connection.getTransactionIsolation"); }
    public java.sql.SQLWarning getWarnings() throws java.sql.SQLException { throw Util.noSoportado("Connection.getWarnings"); }
    public void clearWarnings() throws java.sql.SQLException { throw Util.noSoportado("Connection.clearWarnings"); }
    public java.sql.Statement createStatement(int p0, int p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.createStatement"); }
    public java.sql.PreparedStatement prepareStatement(java.lang.String p0, int p1, int p2) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareStatement"); }
    public java.sql.CallableStatement prepareCall(java.lang.String p0, int p1, int p2) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareCall"); }
    public java.util.Map<java.lang.String, java.lang.Class<?>> getTypeMap() throws java.sql.SQLException { throw Util.noSoportado("Connection.getTypeMap"); }
    public void setTypeMap(java.util.Map<java.lang.String, java.lang.Class<?>> p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setTypeMap"); }
    public void setHoldability(int p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setHoldability"); }
    public int getHoldability() throws java.sql.SQLException { throw Util.noSoportado("Connection.getHoldability"); }
    public java.sql.Savepoint setSavepoint() throws java.sql.SQLException { throw Util.noSoportado("Connection.setSavepoint"); }
    public java.sql.Savepoint setSavepoint(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setSavepoint"); }
    public void rollback(java.sql.Savepoint p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.rollback"); }
    public void releaseSavepoint(java.sql.Savepoint p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.releaseSavepoint"); }
    public java.sql.Statement createStatement(int p0, int p1, int p2) throws java.sql.SQLException { throw Util.noSoportado("Connection.createStatement"); }
    public java.sql.PreparedStatement prepareStatement(java.lang.String p0, int p1, int p2, int p3) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareStatement"); }
    public java.sql.CallableStatement prepareCall(java.lang.String p0, int p1, int p2, int p3) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareCall"); }
    public java.sql.PreparedStatement prepareStatement(java.lang.String p0, int p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareStatement"); }
    public java.sql.PreparedStatement prepareStatement(java.lang.String p0, int[] p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareStatement"); }
    public java.sql.PreparedStatement prepareStatement(java.lang.String p0, java.lang.String[] p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.prepareStatement"); }
    public java.sql.Clob createClob() throws java.sql.SQLException { throw Util.noSoportado("Connection.createClob"); }
    public java.sql.Blob createBlob() throws java.sql.SQLException { throw Util.noSoportado("Connection.createBlob"); }
    public java.sql.NClob createNClob() throws java.sql.SQLException { throw Util.noSoportado("Connection.createNClob"); }
    public java.sql.SQLXML createSQLXML() throws java.sql.SQLException { throw Util.noSoportado("Connection.createSQLXML"); }
    public boolean isValid(int p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.isValid"); }
    public void setClientInfo(java.lang.String p0, java.lang.String p1) throws java.sql.SQLClientInfoException { throw new UnsupportedOperationException("Contlity: Connection.setClientInfo no está soportado"); }
    public void setClientInfo(java.util.Properties p0) throws java.sql.SQLClientInfoException { throw new UnsupportedOperationException("Contlity: Connection.setClientInfo no está soportado"); }
    public java.lang.String getClientInfo(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.getClientInfo"); }
    public java.util.Properties getClientInfo() throws java.sql.SQLException { throw Util.noSoportado("Connection.getClientInfo"); }
    public java.sql.Array createArrayOf(java.lang.String p0, java.lang.Object[] p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.createArrayOf"); }
    public java.sql.Struct createStruct(java.lang.String p0, java.lang.Object[] p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.createStruct"); }
    public void setSchema(java.lang.String p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.setSchema"); }
    public java.lang.String getSchema() throws java.sql.SQLException { throw Util.noSoportado("Connection.getSchema"); }
    public void abort(java.util.concurrent.Executor p0) throws java.sql.SQLException { throw Util.noSoportado("Connection.abort"); }
    public void setNetworkTimeout(java.util.concurrent.Executor p0, int p1) throws java.sql.SQLException { throw Util.noSoportado("Connection.setNetworkTimeout"); }
    public int getNetworkTimeout() throws java.sql.SQLException { throw Util.noSoportado("Connection.getNetworkTimeout"); }

    public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new java.sql.SQLException("No envuelve " + iface.getName());
    }
    public boolean isWrapperFor(Class<?> iface) { return iface.isInstance(this); }
}
