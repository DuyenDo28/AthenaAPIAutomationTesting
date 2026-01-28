package com.athena.qa.framework.client;

public class TokenProvider {

    private final AuthApiClient authApiClient;
    private String cached;

    public TokenProvider(AuthApiClient authApiClient) {
        this.authApiClient = authApiClient;
    }

    /**
     * Lấy token (giữ nguyên hành vi cũ)
     */
    public synchronized String get() {
        if (cached == null || cached.isBlank()) {
            cached = authApiClient.authenticate();
        }
        return clean(cached);
    }

    /**
     * 🔄 Refresh token khi bị expire
     * (KHÔNG ảnh hưởng code cũ)
     */
    public synchronized String refresh() {
        if (cached == null || cached.isBlank()) {
            cached = authApiClient.authenticate();
        } else {
            cached = authApiClient.refreshToken(clean(cached));
        }
        return clean(cached);
    }

    public synchronized void invalidate() {
        cached = null;
    }

    private static String clean(String t) {
        if (t == null) return null;
        t = t.trim();
        if (t.startsWith("Bearer ")) {
            t = t.substring(7);
        }
        return t.replaceAll("^\"|\"$", "");
    }
}
