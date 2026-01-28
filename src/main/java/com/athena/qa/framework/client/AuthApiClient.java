package com.athena.qa.framework.client;

import com.athena.qa.framework.config.ApiConfig;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class AuthApiClient extends BaseApiClient {

    public AuthApiClient(ApiConfig apiConfig) {
        super(apiConfig);
    }

    public String authenticate() {
        Map<String, Object> body = new HashMap<>();

        // gửi cả 2 key để tránh backend case-sensitive
        body.put("client", apiConfig.client());
        body.put("Client", apiConfig.client());

        body.put("username", apiConfig.username());
        body.put("password", apiConfig.password());

        Response res = spec()
                .body(body)
                .post("/api/Login/authenticate");

        res.then().statusCode(200);

        String token = res.jsonPath().getString("token");
        if (token != null && !token.isBlank()) return normalizeToken(token);

        token = res.jsonPath().getString("data.token");
        if (token != null && !token.isBlank()) return normalizeToken(token);

        // fallback: raw string
        return normalizeToken(res.asString());
    }

    private static String normalizeToken(String t) {
        if (t == null) return null;
        t = t.trim();

        // remove surrounding quotes: "eyJ..." -> eyJ...
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1);
        }
        return t.trim();
    }

    public String refreshToken(String oldToken) {

        Response res = spec()
                .queryParam("jwt", oldToken)
                .post("/api/Login/RefreshToken");

        //res.then().statusCode(200);
        // 🔥 Nếu refresh fail → login lại
        if (res.statusCode() != 200) {
            return authenticate();
        }

        String token = res.jsonPath().getString("token");
        if (token != null && !token.isBlank()) return normalizeToken(token);

        token = res.jsonPath().getString("data.token");
        if (token != null && !token.isBlank()) return normalizeToken(token);

        return normalizeToken(res.asString());
    }

}
