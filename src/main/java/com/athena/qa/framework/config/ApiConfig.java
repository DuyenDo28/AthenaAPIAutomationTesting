package com.athena.qa.framework.config;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

public class ApiConfig {
    private final String baseUrl;
    private final String client;
    private final String username;
    private final String password;

    private final LocalDate startDate;
    private final LocalDate endDate;

    private final int maxAttempts;
    private final long sleepMs;

    private final Duration timeout;

    public ApiConfig(
            String baseUrl,
            String client,
            String username,
            String password,
            LocalDate startDate,
            LocalDate endDate,
            int maxAttempts,
            long sleepMs,
            Duration timeout
    ) {
        this.baseUrl = requireNonBlank(baseUrl, "baseUrl");
        this.client = requireNonBlank(client, "client");
        this.username = requireNonBlank(username, "username");
        this.password = Objects.requireNonNull(password, "password");

        this.startDate = startDate;
        this.endDate = endDate;

        this.maxAttempts = maxAttempts <= 0 ? 1 : maxAttempts;
        this.sleepMs = Math.max(0, sleepMs);

        this.timeout = timeout == null ? Duration.ofSeconds(60) : timeout;
    }

    public String baseUrl() { return baseUrl; }
    public String client() { return client; }
    public String username() { return username; }
    public String password() { return password; }

    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }

    public int maxAttempts() { return maxAttempts; }
    public long sleepMs() { return sleepMs; }

    public Duration timeout() { return timeout; }

    private static String requireNonBlank(String s, String name) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(name + " is blank");
        return s.trim();
    }
}
