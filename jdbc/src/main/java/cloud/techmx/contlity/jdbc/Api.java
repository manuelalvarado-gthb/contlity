package cloud.techmx.contlity.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientException;
import java.time.Duration;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/** Llamadas HTTPS a la API pública de Contlity (/v1) con la API key como Bearer. */
final class Api {
    private final HttpClient http;
    private final String base;     // https://host/api
    private final String apiKey;
    private volatile int timeoutS;
    volatile long ultimoOk;

    Api(String base, String apiKey, int timeoutS) {
        this.base = base.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.timeoutS = timeoutS;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    String base() { return base; }

    void timeout(int s) { if (s > 0) timeoutS = s; }

    int timeout() { return timeoutS; }

    Map<String, Object> post(String ruta, Map<String, Object> cuerpo) throws SQLException {
        return llamar(HttpRequest.newBuilder(uri(ruta, null)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.escribir(cuerpo), StandardCharsets.UTF_8)), ruta);
    }

    Map<String, Object> get(String ruta, Map<String, String> query) throws SQLException {
        return llamar(HttpRequest.newBuilder(uri(ruta, query)).GET(), ruta);
    }

    void delete(String ruta) {
        try {
            llamar(HttpRequest.newBuilder(uri(ruta, null)).DELETE(), ruta);
        } catch (SQLException ignorado) {
            // liberar es opcional: el resultado expira solo
        }
    }

    private URI uri(String ruta, Map<String, String> query) {
        StringBuilder u = new StringBuilder(base).append("/v1").append(ruta);
        if (query != null && !query.isEmpty()) {
            char sep = '?';
            for (Map.Entry<String, String> e : query.entrySet()) {
                if (e.getValue() == null) continue;
                u.append(sep).append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)).append('=')
                        .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
                sep = '&';
            }
        }
        return URI.create(u.toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> llamar(HttpRequest.Builder b, String ruta) throws SQLException {
        HttpRequest req = b.header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json").header("Accept-Encoding", "gzip")
                .header("User-Agent", "contlity-jdbc/" + ContlityDriver.VERSION)
                .timeout(Duration.ofSeconds(timeoutS)).build();
        HttpResponse<byte[]> r;
        try {
            r = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new SQLTimeoutException("Contlity no respondió en " + timeoutS + " s", "HYT00", e);
        } catch (IOException e) {
            throw new SQLNonTransientConnectionException("No se pudo conectar con Contlity (" + base + "): " + e.getMessage(), "08001", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrumpido", "HY008", e);
        }
        String texto = cuerpo(r);
        Object j;
        try {
            j = texto.isEmpty() ? null : Json.parse(texto);
        } catch (RuntimeException e) {
            j = null;
        }
        int sc = r.statusCode();
        if (sc >= 200 && sc < 300) {
            ultimoOk = System.currentTimeMillis();
            if (!(j instanceof Map)) throw new SQLException("Respuesta inesperada de " + ruta, "08S01");
            return (Map<String, Object>) j;
        }
        String msg = mensaje(j, texto, sc);
        if (sc == 401) throw new SQLInvalidAuthorizationSpecException(msg, "28000");
        if (sc == 403) throw new SQLInvalidAuthorizationSpecException(msg, "42501");
        if (sc == 404) throw new SQLException(msg, "42S02");
        if (sc == 402) throw new SQLException(msg, "53000");
        if (sc == 429) throw new SQLTransientException(msg, "53400");
        throw new SQLException(msg, sc >= 500 ? "08S01" : "HY000");
    }

    private static String cuerpo(HttpResponse<byte[]> r) throws SQLException {
        byte[] b = r.body();
        try {
            boolean gz = r.headers().firstValue("Content-Encoding").map(v -> v.toLowerCase().contains("gzip")).orElse(false)
                    || (b.length > 2 && (b[0] & 0xff) == 0x1f && (b[1] & 0xff) == 0x8b);
            if (gz) {
                try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(b))) {
                    b = in.readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new SQLException("Respuesta comprimida inválida", "08S01", e);
        }
        return new String(b, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private static String mensaje(Object j, String texto, int sc) {
        if (j instanceof Map) {
            Object d = ((Map<String, Object>) j).get("detail");
            if (d instanceof Map && ((Map<String, Object>) d).get("message") != null)
                return String.valueOf(((Map<String, Object>) d).get("message"));
            if (d != null) return String.valueOf(d);
        }
        return "HTTP " + sc + (texto.isEmpty() ? "" : ": " + (texto.length() > 300 ? texto.substring(0, 300) : texto));
    }
}
