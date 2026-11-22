package cloud.techmx.contlity.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Catálogos JDBC = bases de la cuenta; esquemas = esquemas de cada base. Las vistas del núcleo están
 * en el esquema «contlity» de cada base (contlity.comprobantes o base.contlity.comprobantes).
 * Los metadatos salen de GET /v1/catalog: no consumen créditos ni cuentan como consultas.
 */
public class ContlityDatabaseMetaData extends BaseDatabaseMetaData {
    static final String NUCLEO = "contlity";
    private final ContlityConnection con;

    ContlityDatabaseMetaData(ContlityConnection con) {
        this.con = con;
    }

    private static final String T = "text", I = "integer", S = "smallint", B = "boolean", L = "bigint";

    // ── Catálogo ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<String> bases(String patron) throws SQLException {
        List<String> out = new ArrayList<>();
        Object dbs = con.catalogo(null).get("databases");
        if (dbs instanceof List) for (Object d : (List<Object>) dbs) if (Util.coincide(patron, String.valueOf(d))) out.add(String.valueOf(d));
        Collections.sort(out);
        return out;
    }

    /** catalog de JDBC: null = todas las bases, "" = ninguna, otro = esa base. */
    private List<String> basesDe(String catalog) throws SQLException {
        if (catalog == null) return bases(null);
        if (catalog.isEmpty()) return Collections.emptyList();
        List<String> todas = bases(null);
        for (String b : todas) if (b.equalsIgnoreCase(catalog)) return Collections.singletonList(b);
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> esquemas(String base) throws SQLException {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object s : (List<Object>) con.catalogo(base).get("schemas")) {
            Map<String, Object> m = (Map<String, Object>) s;
            if (!"gobierno".equals(m.get("name"))) out.add(m);
        }
        return out;
    }

    private static String tipoTabla(Object t) {
        String s = String.valueOf(t);
        if (s.equals("view")) return "VIEW";
        if (s.equals("materialized_view")) return "MATERIALIZED VIEW";
        return "TABLE";
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        List<Object[]> f = new ArrayList<>();
        for (String b : bases(null)) f.add(new Object[]{b});
        return ContlityResultSet.tabla(new String[]{"TABLE_CAT"}, new String[]{T}, f);
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return getSchemas(null, null);
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        List<Object[]> f = new ArrayList<>();
        for (String b : basesDe(catalog))
            for (Map<String, Object> s : esquemas(b))
                if (Util.coincide(schemaPattern, String.valueOf(s.get("name")))) f.add(new Object[]{s.get("name"), b});
        f.sort(Comparator.comparing((Object[] r) -> (String) r[1]).thenComparing(r -> (String) r[0]));
        return ContlityResultSet.tabla(new String[]{"TABLE_SCHEM", "TABLE_CATALOG"}, new String[]{T, T}, f);
    }

    @Override
    public ResultSet getTableTypes() {
        List<Object[]> f = new ArrayList<>();
        for (String t : new String[]{"MATERIALIZED VIEW", "TABLE", "VIEW"}) f.add(new Object[]{t});
        return ContlityResultSet.tabla(new String[]{"TABLE_TYPE"}, new String[]{T}, f);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResultSet getTables(String catalog, String schemaPattern, String tablePattern, String[] types) throws SQLException {
        List<Object[]> f = new ArrayList<>();
        for (String b : basesDe(catalog))
            for (Map<String, Object> s : esquemas(b)) {
                String esq = String.valueOf(s.get("name"));
                if (!Util.coincide(schemaPattern, esq)) continue;
                for (Object o : (List<Object>) s.get("tables")) {
                    Map<String, Object> t = (Map<String, Object>) o;
                    String nombre = String.valueOf(t.get("name"));
                    String tipo = tipoTabla(t.get("type"));
                    if (!Util.coincide(tablePattern, nombre) || !tipoPedido(types, tipo)) continue;
                    f.add(new Object[]{b, esq, nombre, tipo, t.get("description"), null, null, null, null, null});
                }
            }
        f.sort(Comparator.comparing((Object[] r) -> (String) r[3]).thenComparing(r -> (String) r[0])
                .thenComparing(r -> (String) r[1]).thenComparing(r -> (String) r[2]));
        return ContlityResultSet.tabla(new String[]{"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS",
                        "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION"},
                new String[]{T, T, T, T, T, T, T, T, T, T}, f);
    }

    private static boolean tipoPedido(String[] types, String tipo) {
        if (types == null) return true;
        for (String t : types) if (t != null && t.equalsIgnoreCase(tipo)) return true;
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResultSet getColumns(String catalog, String schemaPattern, String tablePattern, String columnPattern) throws SQLException {
        List<Object[]> f = new ArrayList<>();
        for (String b : basesDe(catalog))
            for (Map<String, Object> s : esquemas(b)) {
                String esq = String.valueOf(s.get("name"));
                if (!Util.coincide(schemaPattern, esq)) continue;
                for (Object o : (List<Object>) s.get("tables")) {
                    Map<String, Object> t = (Map<String, Object>) o;
                    String tabla = String.valueOf(t.get("name"));
                    if (!Util.coincide(tablePattern, tabla)) continue;
                    int pos = 0;
                    for (Object co : (List<Object>) t.get("columns")) {
                        Map<String, Object> c = (Map<String, Object>) co;
                        pos++;
                        String col = String.valueOf(c.get("name"));
                        if (!Util.coincide(columnPattern, col)) continue;
                        Util.Tipo ti = Util.tipo((String) c.get("type"));
                        boolean num = ti.sql == Types.NUMERIC || ti.sql == Types.DOUBLE || ti.sql == Types.REAL
                                || ti.sql == Types.INTEGER || ti.sql == Types.BIGINT || ti.sql == Types.SMALLINT;
                        f.add(new Object[]{b, esq, tabla, col, ti.sql, ti.nombre, ti.precision, null,
                                ti.sql == Types.NUMERIC ? ti.escala : null, num ? 10 : null, columnNullableUnknown,
                                c.get("description"), null, null, null,
                                ti.sql == Types.VARCHAR || ti.sql == Types.CHAR ? ti.precision : null, pos, "",
                                null, null, null, null, "NO", "NO"});
                    }
                }
            }
        return ContlityResultSet.tabla(new String[]{"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE",
                        "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS",
                        "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE",
                        "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN"},
                new String[]{T, T, T, T, I, T, I, I, I, I, I, T, T, I, I, I, I, T, T, T, T, S, T, T}, f);
    }

    @Override
    public ResultSet getTypeInfo() {
        List<Object[]> f = new ArrayList<>();
        Object[][] tipos = {
                {"boolean", Types.BOOLEAN, 1, null, null}, {"smallint", Types.SMALLINT, 5, null, null},
                {"integer", Types.INTEGER, 10, null, null}, {"bigint", Types.BIGINT, 19, null, null},
                {"real", Types.REAL, 8, null, null}, {"double precision", Types.DOUBLE, 17, null, null},
                {"numeric", Types.NUMERIC, 1000, null, "precision,scale"}, {"varchar", Types.VARCHAR, 65535, "'", "length"},
                {"text", Types.VARCHAR, 65535, "'", null}, {"date", Types.DATE, 10, "'", null},
                {"time", Types.TIME, 15, "'", null}, {"timestamp", Types.TIMESTAMP, 26, "'", null},
                {"bytea", Types.VARBINARY, 65535, "'", null},
        };
        for (Object[] t : tipos) {
            boolean num = t[3] == null && (int) t[1] != Types.BOOLEAN;
            f.add(new Object[]{t[0], t[1], t[2], t[3], t[3], t[4], (short) typeNullable, t[3] != null,
                    (short) typeSearchable, !num, false, false, t[0], (short) 0, (short) ((int) t[1] == Types.NUMERIC ? 1000 : 0),
                    null, null, num ? 10 : null});
        }
        return ContlityResultSet.tabla(new String[]{"TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX", "LITERAL_SUFFIX",
                        "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE",
                        "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE", "SQL_DATA_TYPE", "SQL_DATETIME_SUB",
                        "NUM_PREC_RADIX"},
                new String[]{T, I, I, T, T, T, S, B, S, B, B, B, T, S, S, I, I, I}, f);
    }

    // Sin llaves, índices ni rutinas expuestas: resultados vacíos con las columnas del estándar
    private static ContlityResultSet vacio(String nombres, String tipos) {
        return ContlityResultSet.tabla(nombres.split(","), tipos.split(","), Collections.emptyList());
    }

    @Override
    public ResultSet getPrimaryKeys(String c, String s, String t) {
        return vacio("TABLE_CAT,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,KEY_SEQ,PK_NAME", "text,text,text,text,smallint,text");
    }

    private static final String FK = "PKTABLE_CAT,PKTABLE_SCHEM,PKTABLE_NAME,PKCOLUMN_NAME,FKTABLE_CAT,FKTABLE_SCHEM,FKTABLE_NAME,"
            + "FKCOLUMN_NAME,KEY_SEQ,UPDATE_RULE,DELETE_RULE,FK_NAME,PK_NAME,DEFERRABILITY";
    private static final String FK_T = "text,text,text,text,text,text,text,text,smallint,smallint,smallint,text,text,smallint";

    @Override
    public ResultSet getImportedKeys(String c, String s, String t) { return vacio(FK, FK_T); }

    @Override
    public ResultSet getExportedKeys(String c, String s, String t) { return vacio(FK, FK_T); }

    @Override
    public ResultSet getCrossReference(String a, String b, String c, String d, String e, String f) { return vacio(FK, FK_T); }

    @Override
    public ResultSet getIndexInfo(String c, String s, String t, boolean unique, boolean approximate) {
        return vacio("TABLE_CAT,TABLE_SCHEM,TABLE_NAME,NON_UNIQUE,INDEX_QUALIFIER,INDEX_NAME,TYPE,ORDINAL_POSITION,COLUMN_NAME,"
                + "ASC_OR_DESC,CARDINALITY,PAGES,FILTER_CONDITION", "text,text,text,boolean,text,text,smallint,smallint,text,text,bigint,bigint,text");
    }

    @Override
    public ResultSet getProcedures(String c, String s, String p) {
        return vacio("PROCEDURE_CAT,PROCEDURE_SCHEM,PROCEDURE_NAME,R1,R2,R3,REMARKS,PROCEDURE_TYPE,SPECIFIC_NAME",
                "text,text,text,text,text,text,text,smallint,text");
    }

    @Override
    public ResultSet getProcedureColumns(String c, String s, String p, String col) {
        return vacio("PROCEDURE_CAT,PROCEDURE_SCHEM,PROCEDURE_NAME,COLUMN_NAME,COLUMN_TYPE,DATA_TYPE,TYPE_NAME,PRECISION,LENGTH,"
                + "SCALE,RADIX,NULLABLE,REMARKS", "text,text,text,text,smallint,integer,text,integer,integer,smallint,smallint,smallint,text");
    }

    @Override
    public ResultSet getFunctions(String c, String s, String f) {
        return vacio("FUNCTION_CAT,FUNCTION_SCHEM,FUNCTION_NAME,REMARKS,FUNCTION_TYPE,SPECIFIC_NAME", "text,text,text,text,smallint,text");
    }

    @Override
    public ResultSet getFunctionColumns(String c, String s, String f, String col) {
        return vacio("FUNCTION_CAT,FUNCTION_SCHEM,FUNCTION_NAME,COLUMN_NAME,COLUMN_TYPE,DATA_TYPE,TYPE_NAME,PRECISION,LENGTH,"
                + "SCALE,RADIX,NULLABLE,REMARKS", "text,text,text,text,smallint,integer,text,integer,integer,smallint,smallint,smallint,text");
    }

    @Override
    public ResultSet getTablePrivileges(String c, String s, String t) {
        return vacio("TABLE_CAT,TABLE_SCHEM,TABLE_NAME,GRANTOR,GRANTEE,PRIVILEGE,IS_GRANTABLE", "text,text,text,text,text,text,text");
    }

    @Override
    public ResultSet getColumnPrivileges(String c, String s, String t, String col) {
        return vacio("TABLE_CAT,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,GRANTOR,GRANTEE,PRIVILEGE,IS_GRANTABLE", "text,text,text,text,text,text,text,text");
    }

    @Override
    public ResultSet getBestRowIdentifier(String c, String s, String t, int scope, boolean nullable) {
        return vacio("SCOPE,COLUMN_NAME,DATA_TYPE,TYPE_NAME,COLUMN_SIZE,BUFFER_LENGTH,DECIMAL_DIGITS,PSEUDO_COLUMN",
                "smallint,text,integer,text,integer,integer,smallint,smallint");
    }

    @Override
    public ResultSet getVersionColumns(String c, String s, String t) {
        return vacio("SCOPE,COLUMN_NAME,DATA_TYPE,TYPE_NAME,COLUMN_SIZE,BUFFER_LENGTH,DECIMAL_DIGITS,PSEUDO_COLUMN",
                "smallint,text,integer,text,integer,integer,smallint,smallint");
    }

    @Override
    public ResultSet getUDTs(String c, String s, String t, int[] types) {
        return vacio("TYPE_CAT,TYPE_SCHEM,TYPE_NAME,CLASS_NAME,DATA_TYPE,REMARKS,BASE_TYPE", "text,text,text,text,integer,text,smallint");
    }

    @Override
    public ResultSet getClientInfoProperties() {
        return vacio("NAME,MAX_LEN,DEFAULT_VALUE,DESCRIPTION", "text,integer,text,text");
    }

    // ── Producto y capacidades ───────────────────────────────────────────────

    @Override public Connection getConnection() { return con; }
    @Override public String getURL() { return con.url; }
    @Override public String getUserName() { return "api-key"; }
    @Override public boolean isReadOnly() { return true; }
    @Override public String getDatabaseProductName() { return "Contlity"; }
    @Override public String getDatabaseProductVersion() { return "1.0 (PostgreSQL 16 SQL)"; }
    @Override public int getDatabaseMajorVersion() { return 1; }
    @Override public int getDatabaseMinorVersion() { return 0; }
    @Override public String getDriverName() { return "Contlity JDBC"; }
    @Override public String getDriverVersion() { return ContlityDriver.VERSION; }
    @Override public int getDriverMajorVersion() { return new ContlityDriver().getMajorVersion(); }
    @Override public int getDriverMinorVersion() { return new ContlityDriver().getMinorVersion(); }
    @Override public int getJDBCMajorVersion() { return 4; }
    @Override public int getJDBCMinorVersion() { return 2; }
    @Override public int getSQLStateType() { return sqlStateSQL; }

    @Override public String getIdentifierQuoteString() { return "\""; }
    @Override public String getSearchStringEscape() { return "\\"; }
    @Override public String getExtraNameCharacters() { return ""; }
    @Override public String getCatalogTerm() { return "database"; }
    @Override public String getSchemaTerm() { return "schema"; }
    @Override public String getProcedureTerm() { return "procedure"; }
    @Override public String getCatalogSeparator() { return "."; }
    @Override public boolean isCatalogAtStart() { return true; }
    @Override public String getSQLKeywords() { return "ILIKE,LIMIT,OFFSET,RETURNING,QUALIFY,FILTER,WINDOW"; }
    @Override public String getNumericFunctions() { return "ABS,CEIL,CEILING,FLOOR,ROUND,TRUNC,MOD,POWER,SQRT,EXP,LN,LOG,SIGN,PI,RANDOM"; }
    @Override public String getStringFunctions() { return "CONCAT,LENGTH,LOWER,UPPER,TRIM,LTRIM,RTRIM,SUBSTRING,REPLACE,POSITION,LEFT,RIGHT,LPAD,RPAD,SPLIT_PART"; }
    @Override public String getSystemFunctions() { return "COALESCE,NULLIF,GREATEST,LEAST"; }
    @Override public String getTimeDateFunctions() { return "CURRENT_DATE,CURRENT_TIMESTAMP,NOW,DATE_TRUNC,DATE_PART,EXTRACT,AGE,TO_CHAR,TO_DATE"; }

    @Override public boolean allTablesAreSelectable() { return true; }
    @Override public boolean nullsAreSortedHigh() { return true; }
    @Override public boolean storesLowerCaseIdentifiers() { return true; }
    @Override public boolean storesMixedCaseQuotedIdentifiers() { return true; }
    @Override public boolean supportsMixedCaseQuotedIdentifiers() { return true; }
    @Override public boolean supportsAlterTableWithAddColumn() { return false; }
    @Override public boolean supportsColumnAliasing() { return true; }
    @Override public boolean nullPlusNonNullIsNull() { return true; }
    @Override public boolean supportsTableCorrelationNames() { return true; }
    @Override public boolean supportsExpressionsInOrderBy() { return true; }
    @Override public boolean supportsOrderByUnrelated() { return true; }
    @Override public boolean supportsGroupBy() { return true; }
    @Override public boolean supportsGroupByUnrelated() { return true; }
    @Override public boolean supportsGroupByBeyondSelect() { return true; }
    @Override public boolean supportsLikeEscapeClause() { return true; }
    @Override public boolean supportsNonNullableColumns() { return true; }
    @Override public boolean supportsMinimumSQLGrammar() { return true; }
    @Override public boolean supportsCoreSQLGrammar() { return true; }
    @Override public boolean supportsANSI92EntryLevelSQL() { return true; }
    @Override public boolean supportsOuterJoins() { return true; }
    @Override public boolean supportsFullOuterJoins() { return true; }
    @Override public boolean supportsLimitedOuterJoins() { return true; }
    @Override public boolean supportsSchemasInDataManipulation() { return true; }
    @Override public boolean supportsSchemasInProcedureCalls() { return true; }
    @Override public boolean supportsSchemasInTableDefinitions() { return true; }
    @Override public boolean supportsSchemasInIndexDefinitions() { return true; }
    @Override public boolean supportsSchemasInPrivilegeDefinitions() { return true; }
    @Override public boolean supportsCatalogsInDataManipulation() { return true; }
    @Override public boolean supportsSubqueriesInComparisons() { return true; }
    @Override public boolean supportsSubqueriesInExists() { return true; }
    @Override public boolean supportsSubqueriesInIns() { return true; }
    @Override public boolean supportsSubqueriesInQuantifieds() { return true; }
    @Override public boolean supportsCorrelatedSubqueries() { return true; }
    @Override public boolean supportsUnion() { return true; }
    @Override public boolean supportsUnionAll() { return true; }
    @Override public int getMaxColumnNameLength() { return 63; }
    @Override public int getMaxSchemaNameLength() { return 63; }
    @Override public int getMaxTableNameLength() { return 63; }
    @Override public int getMaxCatalogNameLength() { return 63; }
    @Override public int getDefaultTransactionIsolation() { return Connection.TRANSACTION_READ_COMMITTED; }
    @Override public boolean supportsTransactions() { return false; }
    @Override public boolean supportsTransactionIsolationLevel(int level) { return level == Connection.TRANSACTION_READ_COMMITTED; }
    @Override public boolean supportsResultSetType(int type) { return type == ResultSet.TYPE_FORWARD_ONLY; }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        return type == ResultSet.TYPE_FORWARD_ONLY && concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    @Override public boolean supportsResultSetHoldability(int h) { return h == ResultSet.CLOSE_CURSORS_AT_COMMIT; }
    @Override public int getResultSetHoldability() { return ResultSet.CLOSE_CURSORS_AT_COMMIT; }
    @Override public java.sql.RowIdLifetime getRowIdLifetime() { return java.sql.RowIdLifetime.ROWID_UNSUPPORTED; }
    @Override public boolean generatedKeyAlwaysReturned() { return false; }
}
