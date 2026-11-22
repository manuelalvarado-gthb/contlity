package cloud.techmx.contlity.jdbc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JSON mínimo (sin dependencias): objetos → Map, arreglos → List, números → BigDecimal. */
final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    static Object parse(String texto) {
        Json j = new Json(texto);
        j.ws();
        Object v = j.valor();
        j.ws();
        if (j.i != j.s.length()) throw new IllegalArgumentException("JSON inválido en " + j.i);
        return v;
    }

    private void ws() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private Object valor() {
        if (i >= s.length()) throw new IllegalArgumentException("JSON incompleto");
        char c = s.charAt(i);
        switch (c) {
            case '{': return objeto();
            case '[': return arreglo();
            case '"': return texto();
            case 't': lit("true"); return Boolean.TRUE;
            case 'f': lit("false"); return Boolean.FALSE;
            case 'n': lit("null"); return null;
            default: return numero();
        }
    }

    private void lit(String l) {
        if (!s.startsWith(l, i)) throw new IllegalArgumentException("JSON inválido en " + i);
        i += l.length();
    }

    private Map<String, Object> objeto() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++;
        ws();
        if (s.charAt(i) == '}') { i++; return m; }
        while (true) {
            ws();
            String k = texto();
            ws();
            if (s.charAt(i++) != ':') throw new IllegalArgumentException("JSON: falta ':' en " + i);
            ws();
            m.put(k, valor());
            ws();
            char c = s.charAt(i++);
            if (c == '}') return m;
            if (c != ',') throw new IllegalArgumentException("JSON: falta ',' en " + i);
        }
    }

    private List<Object> arreglo() {
        List<Object> l = new ArrayList<>();
        i++;
        ws();
        if (s.charAt(i) == ']') { i++; return l; }
        while (true) {
            ws();
            l.add(valor());
            ws();
            char c = s.charAt(i++);
            if (c == ']') return l;
            if (c != ',') throw new IllegalArgumentException("JSON: falta ',' en " + i);
        }
    }

    private String texto() {
        if (s.charAt(i) != '"') throw new IllegalArgumentException("JSON: se esperaba texto en " + i);
        i++;
        StringBuilder b = null;
        int ini = i;
        while (true) {
            char c = s.charAt(i);
            if (c == '"') {
                String r = b == null ? s.substring(ini, i) : b.append(s, ini, i).toString();
                i++;
                return r;
            }
            if (c == '\\') {
                if (b == null) b = new StringBuilder();
                b.append(s, ini, i);
                char e = s.charAt(i + 1);
                switch (e) {
                    case 'n': b.append('\n'); break;
                    case 't': b.append('\t'); break;
                    case 'r': b.append('\r'); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'u': b.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16)); i += 4; break;
                    default: b.append(e);
                }
                i += 2;
                ini = i;
            } else {
                i++;
            }
        }
    }

    private BigDecimal numero() {
        int ini = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (ini == i) throw new IllegalArgumentException("JSON inválido en " + i);
        return new BigDecimal(s.substring(ini, i));
    }

    static String escribir(Object v) {
        StringBuilder b = new StringBuilder();
        escribir(v, b);
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    private static void escribir(Object v, StringBuilder b) {
        if (v == null) b.append("null");
        else if (v instanceof String) cadena((String) v, b);
        else if (v instanceof Number || v instanceof Boolean) b.append(v);
        else if (v instanceof Map) {
            b.append('{');
            boolean primero = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
                if (!primero) b.append(',');
                primero = false;
                cadena(e.getKey(), b);
                b.append(':');
                escribir(e.getValue(), b);
            }
            b.append('}');
        } else cadena(v.toString(), b);
    }

    private static void cadena(String t, StringBuilder b) {
        b.append('"');
        for (int k = 0; k < t.length(); k++) {
            char c = t.charAt(k);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
            }
        }
        b.append('"');
    }
}
