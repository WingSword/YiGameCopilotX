package org.walks.rooms;

import java.util.*;

/** Small, strict JSON codec. No reflection: public API views are explicitly constructed maps. */
final class Json {
    static Object parse(String input) {
        Parser p = new Parser(input);
        Object result = p.value(0);
        p.ws();
        if (p.i != input.length()) throw new IllegalArgumentException("Trailing JSON data");
        return result;
    }
    @SuppressWarnings("unchecked")
    static Map<String,Object> object(Object value) {
        if (!(value instanceof Map<?,?>)) throw new IllegalArgumentException("Expected JSON object");
        return (Map<String,Object>) value;
    }
    static String write(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) {
            StringBuilder b = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\");
                    case '\n' -> b.append("\\n"); case '\r' -> b.append("\\r"); case '\t' -> b.append("\\t");
                    default -> { if (c < 32 || Character.isSurrogate(c)) b.append(String.format("\\u%04x", (int)c)); else b.append(c); }
                }
            }
            return b.append('"').toString();
        }
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof Map<?,?> m) {
            StringJoiner j = new StringJoiner(",", "{", "}");
            m.forEach((k,v) -> j.add(write(k.toString()) + ":" + write(v)));
            return j.toString();
        }
        if (value instanceof Collection<?> a) {
            StringJoiner j = new StringJoiner(",", "[", "]");
            a.forEach(v -> j.add(write(v))); return j.toString();
        }
        throw new IllegalArgumentException("Unsupported JSON value");
    }
    private static final class Parser {
        final String s; int i;
        Parser(String s) { this.s = s; }
        void ws() { while (i < s.length() && " \r\n\t".indexOf(s.charAt(i)) >= 0) i++; }
        char take() { if (i == s.length()) throw new IllegalArgumentException("Incomplete JSON"); return s.charAt(i++); }
        Object value(int depth) {
            if (depth > 24) throw new IllegalArgumentException("JSON too deep");
            ws(); char c = take();
            if (c == '"') return string();
            if (c == '{') {
                Map<String,Object> m = new LinkedHashMap<>(); ws();
                if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
                do {
                    ws(); if (take() != '"') throw new IllegalArgumentException("Expected key");
                    String key = string(); ws(); if (take() != ':') throw new IllegalArgumentException("Expected colon");
                    if (m.containsKey(key)) throw new IllegalArgumentException("Duplicate key");
                    m.put(key, value(depth + 1)); ws(); c = take();
                    if (c == '}') return m;
                    if (c != ',') throw new IllegalArgumentException("Expected comma");
                } while (true);
            }
            if (c == '[') {
                List<Object> a = new ArrayList<>(); ws();
                if (i < s.length() && s.charAt(i) == ']') { i++; return a; }
                do { a.add(value(depth + 1)); ws(); c = take();
                    if (c == ']') return a;
                    if (c != ',') throw new IllegalArgumentException("Expected comma");
                } while (true);
            }
            i--;
            for (String token : List.of("true", "false", "null")) {
                if (s.startsWith(token, i)) { i += token.length(); return token.equals("null") ? null : token.equals("true"); }
            }
            int begin = i;
            if (i < s.length() && s.charAt(i) == '-') i++;
            if (i >= s.length() || !Character.isDigit(s.charAt(i))) throw new IllegalArgumentException("Invalid value");
            if (s.charAt(i) == '0') i++; else while (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') i++;
            if (i < s.length() && s.charAt(i) == '.') { i++; digits(); }
            if (i < s.length() && "eE".indexOf(s.charAt(i)) >= 0) {
                i++; if (i < s.length() && "+-".indexOf(s.charAt(i)) >= 0) i++; digits();
            }
            String n = s.substring(begin, i);
            try { return Long.parseLong(n); } catch (NumberFormatException e) {
                double d = Double.parseDouble(n);
                if (!Double.isFinite(d)) throw new IllegalArgumentException("Invalid number");
                return d;
            }
        }
        void digits() { int begin = i; while (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') i++; if (i == begin) throw new IllegalArgumentException("Expected digits"); }
        String string() {
            StringBuilder b = new StringBuilder();
            while (true) {
                char c = take(); if (c == '"') return b.toString();
                if (c < 32) throw new IllegalArgumentException("Control character");
                if (c == '\\') {
                    c = take();
                    switch(c) {
                        case '"', '\\', '/' -> b.append(c);
                        case 'b' -> b.append('\b'); case 'f' -> b.append('\f');
                        case 'n' -> b.append('\n'); case 'r' -> b.append('\r'); case 't' -> b.append('\t');
                        case 'u' -> { StringBuilder h = new StringBuilder(); for(int j=0;j<4;j++) h.append(take()); b.append((char)Integer.parseInt(h.toString(),16)); }
                        default -> throw new IllegalArgumentException("Invalid escape");
                    }
                } else b.append(c);
            }
        }
    }
}
