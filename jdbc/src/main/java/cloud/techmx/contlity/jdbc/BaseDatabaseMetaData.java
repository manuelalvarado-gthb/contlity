// Generado por generar_bases.py a partir de java.sql.DatabaseMetaData. No editar a mano.
package cloud.techmx.contlity.jdbc;

@SuppressWarnings({"unused", "deprecation"})
public abstract class BaseDatabaseMetaData implements java.sql.DatabaseMetaData {
    public boolean allProceduresAreCallable() throws java.sql.SQLException { return false; }
    public boolean allTablesAreSelectable() throws java.sql.SQLException { return false; }
    public java.lang.String getURL() throws java.sql.SQLException { return ""; }
    public java.lang.String getUserName() throws java.sql.SQLException { return ""; }
    public boolean isReadOnly() throws java.sql.SQLException { return false; }
    public boolean nullsAreSortedHigh() throws java.sql.SQLException { return false; }
    public boolean nullsAreSortedLow() throws java.sql.SQLException { return false; }
    public boolean nullsAreSortedAtStart() throws java.sql.SQLException { return false; }
    public boolean nullsAreSortedAtEnd() throws java.sql.SQLException { return false; }
    public java.lang.String getDatabaseProductName() throws java.sql.SQLException { return ""; }
    public java.lang.String getDatabaseProductVersion() throws java.sql.SQLException { return ""; }
    public java.lang.String getDriverName() throws java.sql.SQLException { return ""; }
    public java.lang.String getDriverVersion() throws java.sql.SQLException { return ""; }
    public int getDriverMajorVersion() { return 0; }
    public int getDriverMinorVersion() { return 0; }
    public boolean usesLocalFiles() throws java.sql.SQLException { return false; }
    public boolean usesLocalFilePerTable() throws java.sql.SQLException { return false; }
    public boolean supportsMixedCaseIdentifiers() throws java.sql.SQLException { return false; }
    public boolean storesUpperCaseIdentifiers() throws java.sql.SQLException { return false; }
    public boolean storesLowerCaseIdentifiers() throws java.sql.SQLException { return false; }
    public boolean storesMixedCaseIdentifiers() throws java.sql.SQLException { return false; }
    public boolean supportsMixedCaseQuotedIdentifiers() throws java.sql.SQLException { return false; }
    public boolean storesUpperCaseQuotedIdentifiers() throws java.sql.SQLException { return false; }
    public boolean storesLowerCaseQuotedIdentifiers() throws java.sql.SQLException { return false; }
    public boolean storesMixedCaseQuotedIdentifiers() throws java.sql.SQLException { return false; }
    public java.lang.String getIdentifierQuoteString() throws java.sql.SQLException { return ""; }
    public java.lang.String getSQLKeywords() throws java.sql.SQLException { return ""; }
    public java.lang.String getNumericFunctions() throws java.sql.SQLException { return ""; }
    public java.lang.String getStringFunctions() throws java.sql.SQLException { return ""; }
    public java.lang.String getSystemFunctions() throws java.sql.SQLException { return ""; }
    public java.lang.String getTimeDateFunctions() throws java.sql.SQLException { return ""; }
    public java.lang.String getSearchStringEscape() throws java.sql.SQLException { return ""; }
    public java.lang.String getExtraNameCharacters() throws java.sql.SQLException { return ""; }
    public boolean supportsAlterTableWithAddColumn() throws java.sql.SQLException { return false; }
    public boolean supportsAlterTableWithDropColumn() throws java.sql.SQLException { return false; }
    public boolean supportsColumnAliasing() throws java.sql.SQLException { return false; }
    public boolean nullPlusNonNullIsNull() throws java.sql.SQLException { return false; }
    public boolean supportsConvert() throws java.sql.SQLException { return false; }
    public boolean supportsConvert(int p0, int p1) throws java.sql.SQLException { return false; }
    public boolean supportsTableCorrelationNames() throws java.sql.SQLException { return false; }
    public boolean supportsDifferentTableCorrelationNames() throws java.sql.SQLException { return false; }
    public boolean supportsExpressionsInOrderBy() throws java.sql.SQLException { return false; }
    public boolean supportsOrderByUnrelated() throws java.sql.SQLException { return false; }
    public boolean supportsGroupBy() throws java.sql.SQLException { return false; }
    public boolean supportsGroupByUnrelated() throws java.sql.SQLException { return false; }
    public boolean supportsGroupByBeyondSelect() throws java.sql.SQLException { return false; }
    public boolean supportsLikeEscapeClause() throws java.sql.SQLException { return false; }
    public boolean supportsMultipleResultSets() throws java.sql.SQLException { return false; }
    public boolean supportsMultipleTransactions() throws java.sql.SQLException { return false; }
    public boolean supportsNonNullableColumns() throws java.sql.SQLException { return false; }
    public boolean supportsMinimumSQLGrammar() throws java.sql.SQLException { return false; }
    public boolean supportsCoreSQLGrammar() throws java.sql.SQLException { return false; }
    public boolean supportsExtendedSQLGrammar() throws java.sql.SQLException { return false; }
    public boolean supportsANSI92EntryLevelSQL() throws java.sql.SQLException { return false; }
    public boolean supportsANSI92IntermediateSQL() throws java.sql.SQLException { return false; }
    public boolean supportsANSI92FullSQL() throws java.sql.SQLException { return false; }
    public boolean supportsIntegrityEnhancementFacility() throws java.sql.SQLException { return false; }
    public boolean supportsOuterJoins() throws java.sql.SQLException { return false; }
    public boolean supportsFullOuterJoins() throws java.sql.SQLException { return false; }
    public boolean supportsLimitedOuterJoins() throws java.sql.SQLException { return false; }
    public java.lang.String getSchemaTerm() throws java.sql.SQLException { return ""; }
    public java.lang.String getProcedureTerm() throws java.sql.SQLException { return ""; }
    public java.lang.String getCatalogTerm() throws java.sql.SQLException { return ""; }
    public boolean isCatalogAtStart() throws java.sql.SQLException { return false; }
    public java.lang.String getCatalogSeparator() throws java.sql.SQLException { return ""; }
    public boolean supportsSchemasInDataManipulation() throws java.sql.SQLException { return false; }
    public boolean supportsSchemasInProcedureCalls() throws java.sql.SQLException { return false; }
    public boolean supportsSchemasInTableDefinitions() throws java.sql.SQLException { return false; }
    public boolean supportsSchemasInIndexDefinitions() throws java.sql.SQLException { return false; }
    public boolean supportsSchemasInPrivilegeDefinitions() throws java.sql.SQLException { return false; }
    public boolean supportsCatalogsInDataManipulation() throws java.sql.SQLException { return false; }
    public boolean supportsCatalogsInProcedureCalls() throws java.sql.SQLException { return false; }
    public boolean supportsCatalogsInTableDefinitions() throws java.sql.SQLException { return false; }
    public boolean supportsCatalogsInIndexDefinitions() throws java.sql.SQLException { return false; }
    public boolean supportsCatalogsInPrivilegeDefinitions() throws java.sql.SQLException { return false; }
    public boolean supportsPositionedDelete() throws java.sql.SQLException { return false; }
    public boolean supportsPositionedUpdate() throws java.sql.SQLException { return false; }
    public boolean supportsSelectForUpdate() throws java.sql.SQLException { return false; }
    public boolean supportsStoredProcedures() throws java.sql.SQLException { return false; }
    public boolean supportsSubqueriesInComparisons() throws java.sql.SQLException { return false; }
    public boolean supportsSubqueriesInExists() throws java.sql.SQLException { return false; }
    public boolean supportsSubqueriesInIns() throws java.sql.SQLException { return false; }
    public boolean supportsSubqueriesInQuantifieds() throws java.sql.SQLException { return false; }
    public boolean supportsCorrelatedSubqueries() throws java.sql.SQLException { return false; }
    public boolean supportsUnion() throws java.sql.SQLException { return false; }
    public boolean supportsUnionAll() throws java.sql.SQLException { return false; }
    public boolean supportsOpenCursorsAcrossCommit() throws java.sql.SQLException { return false; }
    public boolean supportsOpenCursorsAcrossRollback() throws java.sql.SQLException { return false; }
    public boolean supportsOpenStatementsAcrossCommit() throws java.sql.SQLException { return false; }
    public boolean supportsOpenStatementsAcrossRollback() throws java.sql.SQLException { return false; }
    public int getMaxBinaryLiteralLength() throws java.sql.SQLException { return 0; }
    public int getMaxCharLiteralLength() throws java.sql.SQLException { return 0; }
    public int getMaxColumnNameLength() throws java.sql.SQLException { return 0; }
    public int getMaxColumnsInGroupBy() throws java.sql.SQLException { return 0; }
    public int getMaxColumnsInIndex() throws java.sql.SQLException { return 0; }
    public int getMaxColumnsInOrderBy() throws java.sql.SQLException { return 0; }
    public int getMaxColumnsInSelect() throws java.sql.SQLException { return 0; }
    public int getMaxColumnsInTable() throws java.sql.SQLException { return 0; }
    public int getMaxConnections() throws java.sql.SQLException { return 0; }
    public int getMaxCursorNameLength() throws java.sql.SQLException { return 0; }
    public int getMaxIndexLength() throws java.sql.SQLException { return 0; }
    public int getMaxSchemaNameLength() throws java.sql.SQLException { return 0; }
    public int getMaxProcedureNameLength() throws java.sql.SQLException { return 0; }
    public int getMaxCatalogNameLength() throws java.sql.SQLException { return 0; }
    public int getMaxRowSize() throws java.sql.SQLException { return 0; }
    public boolean doesMaxRowSizeIncludeBlobs() throws java.sql.SQLException { return false; }
    public int getMaxStatementLength() throws java.sql.SQLException { return 0; }
    public int getMaxStatements() throws java.sql.SQLException { return 0; }
    public int getMaxTableNameLength() throws java.sql.SQLException { return 0; }
    public int getMaxTablesInSelect() throws java.sql.SQLException { return 0; }
    public int getMaxUserNameLength() throws java.sql.SQLException { return 0; }
    public int getDefaultTransactionIsolation() throws java.sql.SQLException { return 0; }
    public boolean supportsTransactions() throws java.sql.SQLException { return false; }
    public boolean supportsTransactionIsolationLevel(int p0) throws java.sql.SQLException { return false; }
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws java.sql.SQLException { return false; }
    public boolean supportsDataManipulationTransactionsOnly() throws java.sql.SQLException { return false; }
    public boolean dataDefinitionCausesTransactionCommit() throws java.sql.SQLException { return false; }
    public boolean dataDefinitionIgnoredInTransactions() throws java.sql.SQLException { return false; }
    public java.sql.ResultSet getProcedures(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getProcedureColumns(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getTables(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String[] p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getSchemas() throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getCatalogs() throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getTableTypes() throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getColumns(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getColumnPrivileges(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getTablePrivileges(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getBestRowIdentifier(java.lang.String p0, java.lang.String p1, java.lang.String p2, int p3, boolean p4) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getVersionColumns(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getPrimaryKeys(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getImportedKeys(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getExportedKeys(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getCrossReference(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3, java.lang.String p4, java.lang.String p5) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getTypeInfo() throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getIndexInfo(java.lang.String p0, java.lang.String p1, java.lang.String p2, boolean p3, boolean p4) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public boolean supportsResultSetType(int p0) throws java.sql.SQLException { return false; }
    public boolean supportsResultSetConcurrency(int p0, int p1) throws java.sql.SQLException { return false; }
    public boolean ownUpdatesAreVisible(int p0) throws java.sql.SQLException { return false; }
    public boolean ownDeletesAreVisible(int p0) throws java.sql.SQLException { return false; }
    public boolean ownInsertsAreVisible(int p0) throws java.sql.SQLException { return false; }
    public boolean othersUpdatesAreVisible(int p0) throws java.sql.SQLException { return false; }
    public boolean othersDeletesAreVisible(int p0) throws java.sql.SQLException { return false; }
    public boolean othersInsertsAreVisible(int p0) throws java.sql.SQLException { return false; }
    public boolean updatesAreDetected(int p0) throws java.sql.SQLException { return false; }
    public boolean deletesAreDetected(int p0) throws java.sql.SQLException { return false; }
    public boolean insertsAreDetected(int p0) throws java.sql.SQLException { return false; }
    public boolean supportsBatchUpdates() throws java.sql.SQLException { return false; }
    public java.sql.ResultSet getUDTs(java.lang.String p0, java.lang.String p1, java.lang.String p2, int[] p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.Connection getConnection() throws java.sql.SQLException { return null; }
    public boolean supportsSavepoints() throws java.sql.SQLException { return false; }
    public boolean supportsNamedParameters() throws java.sql.SQLException { return false; }
    public boolean supportsMultipleOpenResults() throws java.sql.SQLException { return false; }
    public boolean supportsGetGeneratedKeys() throws java.sql.SQLException { return false; }
    public java.sql.ResultSet getSuperTypes(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getSuperTables(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getAttributes(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public boolean supportsResultSetHoldability(int p0) throws java.sql.SQLException { return false; }
    public int getResultSetHoldability() throws java.sql.SQLException { return 0; }
    public int getDatabaseMajorVersion() throws java.sql.SQLException { return 0; }
    public int getDatabaseMinorVersion() throws java.sql.SQLException { return 0; }
    public int getJDBCMajorVersion() throws java.sql.SQLException { return 0; }
    public int getJDBCMinorVersion() throws java.sql.SQLException { return 0; }
    public int getSQLStateType() throws java.sql.SQLException { return 0; }
    public boolean locatorsUpdateCopy() throws java.sql.SQLException { return false; }
    public boolean supportsStatementPooling() throws java.sql.SQLException { return false; }
    public java.sql.RowIdLifetime getRowIdLifetime() throws java.sql.SQLException { return java.sql.RowIdLifetime.ROWID_UNSUPPORTED; }
    public java.sql.ResultSet getSchemas(java.lang.String p0, java.lang.String p1) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public boolean supportsStoredFunctionsUsingCallSyntax() throws java.sql.SQLException { return false; }
    public boolean autoCommitFailureClosesAllResultSets() throws java.sql.SQLException { return false; }
    public java.sql.ResultSet getClientInfoProperties() throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getFunctions(java.lang.String p0, java.lang.String p1, java.lang.String p2) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getFunctionColumns(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public java.sql.ResultSet getPseudoColumns(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) throws java.sql.SQLException { return ContlityResultSet.vacio(); }
    public boolean generatedKeyAlwaysReturned() throws java.sql.SQLException { return false; }

    public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new java.sql.SQLException("No envuelve " + iface.getName());
    }
    public boolean isWrapperFor(Class<?> iface) { return iface.isInstance(this); }
}
