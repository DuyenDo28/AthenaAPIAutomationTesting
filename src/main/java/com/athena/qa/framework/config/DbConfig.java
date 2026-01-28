package com.athena.qa.framework.config;

import java.util.Objects;

public class DbConfig {
    private final String jdbcUrl;
    private final String user;
    private final String password;

    public DbConfig(String jdbcUrl, String user, String password) {
        this.jdbcUrl = requireNonBlank(jdbcUrl, "jdbcUrl");
        this.user = requireNonBlank(user, "user");
        this.password = Objects.requireNonNull(password, "password");
    }

    public String jdbcUrl() { return jdbcUrl; }
    public String user() { return user; }
    public String password() { return password; }

    private static String requireNonBlank(String s, String name) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(name + " is blank");
        return s.trim();
    }
}
