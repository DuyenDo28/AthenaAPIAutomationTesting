package com.athena.qa.framework.client;

public class StaticTokenProvider {

    private final String token;

    public StaticTokenProvider(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("PositionHist token must not be blank");
        }
        this.token = clean(token);
    }

    public String get() {
        return token;
    }

    private static String clean(String t) {
        t = t.trim();

        // allow passing: "Bearer eyJ..." or "\"eyJ...\""
        if (t.startsWith("Bearer ")) {
            t = t.substring(7);
        }

        // remove surrounding quotes
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1);
        }

        return t.trim();
    }
}
