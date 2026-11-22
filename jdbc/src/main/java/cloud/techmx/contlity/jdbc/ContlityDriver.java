package cloud.techmx.contlity.jdbc;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Driver JDBC de Contlity. Habla con la API por HTTPS (como los drivers de Snowflake o Athena): las
 * consultas corren en el motor de Contlity con los permisos, la cartera y los créditos del usuario de
 * la API key (alcance «SQL»). Solo lectura.
 *
 * <pre>
 * jdbc:contlity://[servidor[:puerto]]/[base][?schema=esquema&amp;pageSize=10000&amp;timeout=120]
 * usuario: cualquiera      contraseña: la API key (ctly_live_...)
 * </pre>
 * Sin servidor se usa contlity-api.tech-mx.cloud. La API key también se puede dar como propiedad
 * apiKey o en la variable de entorno CONTLITY_API_KEY.
 */
public final class ContlityDriver implements Driver {
    public static final String PREFIJO = "jdbc:contlity:";
    public static final String SERVIDOR = "contlity-api.tech-mx.cloud";
    public static final String VERSION = version();

    static {
        try {
            DriverManager.registerDriver(new ContlityDriver());
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String version() {
        try (InputStream in = ContlityDriver.class.getResourceAsStream("version.properties")) {
            Properties p = new Properties();
            if (in != null) p.load(in);
            return p.getProperty("version", "0.0.0");
        } catch (Exception e) {
            return "0.0.0";
        }
    }

    /** java -jar contlity-jdbc.jar: versión y forma de uso. */
    public static void main(String[] args) {
        System.out.println("Contlity JDBC " + VERSION);
        System.out.println("Clase del driver: " + ContlityDriver.class.getName());
        System.out.println("URL: jdbc:contlity://[servidor]/[base][?schema=esquema]   usuario: cualquiera   contraseña: API key (alcance SQL)");
    }

    @Override
    public boolean acceptsURL(String url) {
        return url != null && url.startsWith(PREFIJO);
    }

    /** URL + propiedades → configuración (las propiedades ganan a los parámetros de la URL). */
    static Properties leer(String url, Properties info) throws SQLException {
        Properties p = new Properties();
        String resto = url.substring(PREFIJO.length());
        int q = resto.indexOf('?');
        String consulta = q >= 0 ? resto.substring(q + 1) : "";
        resto = q >= 0 ? resto.substring(0, q) : resto;
        if (resto.startsWith("//")) {
            resto = resto.substring(2);
            int barra = resto.indexOf('/');
            String hostPuerto = barra >= 0 ? resto.substring(0, barra) : resto;
            String base = barra >= 0 ? resto.substring(barra + 1) : "";
            if (!hostPuerto.isEmpty()) p.setProperty("host", hostPuerto);
            if (!base.isEmpty()) p.setProperty("database", decode(base));
        } else if (!resto.isEmpty()) {
            throw new SQLException("URL inválida: usa jdbc:contlity://[servidor]/[base]", "08001");
        }
        for (String par : consulta.split("&")) {
            if (par.isEmpty()) continue;
            int eq = par.indexOf('=');
            p.setProperty(decode(eq >= 0 ? par.substring(0, eq) : par), eq >= 0 ? decode(par.substring(eq + 1)) : "");
        }
        if (info != null) {
            for (String k : info.stringPropertyNames()) p.setProperty(k, info.getProperty(k));
        }
        return p;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) return null;
        Properties p = leer(url, info);
        String key = primero(p.getProperty("apiKey"), p.getProperty("api_key"), p.getProperty("password"),
                System.getenv("CONTLITY_API_KEY"));
        if (key == null || !key.startsWith("ctly_")) {
            throw new java.sql.SQLInvalidAuthorizationSpecException(
                    "Falta la API key: ponla como contraseña (ctly_live_...) o en CONTLITY_API_KEY", "28000");
        }
        boolean ssl = !"false".equalsIgnoreCase(p.getProperty("ssl", "true"));
        String host = p.getProperty("host", SERVIDOR);
        String ruta = p.getProperty("apiPath", "/api");
        String base = (ssl ? "https://" : "http://") + host + ruta;
        int timeout = entero(p.getProperty("timeout"), 120);
        int pagina = Math.max(100, Math.min(100000, entero(p.getProperty("pageSize"), 10000)));
        Api api = new Api(base, key.trim(), timeout);
        return new ContlityConnection(api, url, emptyToNull(p.getProperty("database")),
                emptyToNull(p.getProperty("schema")), pagina);
    }

    private static String primero(String... vs) {
        for (String v : vs) if (v != null && !v.trim().isEmpty()) return v;
        return null;
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    static int entero(String s, int def) {
        try {
            return s == null ? def : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        DriverPropertyInfo pass = new DriverPropertyInfo("password", null);
        pass.description = "API key de Contlity con alcance SQL (ctly_live_...)";
        pass.required = true;
        DriverPropertyInfo db = new DriverPropertyInfo("database", null);
        db.description = "Base en uso (por omisión, la principal)";
        DriverPropertyInfo sch = new DriverPropertyInfo("schema", null);
        sch.description = "Esquema en uso";
        DriverPropertyInfo pg = new DriverPropertyInfo("pageSize", "10000");
        pg.description = "Filas por página (100–100000)";
        DriverPropertyInfo to = new DriverPropertyInfo("timeout", "120");
        to.description = "Segundos de espera por consulta";
        return new DriverPropertyInfo[]{pass, db, sch, pg, to};
    }

    @Override
    public int getMajorVersion() {
        return entero(VERSION.split("\\.")[0], 0);
    }

    @Override
    public int getMinorVersion() {
        String[] v = VERSION.split("\\.");
        return v.length > 1 ? entero(v[1], 0) : 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("cloud.techmx.contlity.jdbc");
    }
}
