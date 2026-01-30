package com.athena.qa.tests.base;

import com.athena.qa.framework.config.ApiConfig;
import com.athena.qa.framework.config.DbConfig;
import com.athena.qa.framework.config.TradeMultiConfig;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

public class ConfigLoader {

    public static ApiConfig loadApi() {
        Properties p = loadProps();

        String baseUrl = p.getProperty("api.baseUrl");
        String client = p.getProperty("api.client");

        // 🔐 RESOLVE ENV VARIABLES HERE
        String username = resolveEnv(p.getProperty("api.username"));
        String password = resolveEnv(p.getProperty("api.password"));

        int timeoutSeconds = Integer.parseInt(p.getProperty("api.timeoutSeconds", "60"));
        int maxAttempts = Integer.parseInt(p.getProperty("polling.maxAttempts", "90"));
        long sleepMs = Long.parseLong(p.getProperty("polling.sleepMs", "3000"));

        LocalDate startDate = parseDate(p.getProperty("tradehist.startDate"));
        LocalDate endDate = parseDate(p.getProperty("tradehist.endDate"));

        return new ApiConfig(
                baseUrl,
                client,
                username,
                password,
                startDate,
                endDate,
                maxAttempts,
                sleepMs,
                Duration.ofSeconds(timeoutSeconds)
        );
    }

    public static DbConfig loadDb() {
        Properties p = loadProps();

        return new DbConfig(
                p.getProperty("db.url"),
                resolveEnv(p.getProperty("db.user")),
                resolveEnv(p.getProperty("db.password"))
        );
    }

    public static TradeMultiConfig loadTradeMulti() {
        Properties p = loadProps();

        return new TradeMultiConfig(
                Integer.parseInt(p.getProperty("tradeMulti.count", "1")),
                Integer.parseInt(p.getProperty("tradeMulti.userId", "2")),
                p.getProperty("tradeMulti.userCode", "ATHENATEST"),

                Integer.parseInt(p.getProperty("tradeMulti.targetQty", "19")),
                Integer.parseInt(p.getProperty("tradeMulti.filledQty", "19")),
                Double.parseDouble(p.getProperty("tradeMulti.avgPrice", "0")),

                LocalDateTime.parse(p.getProperty("tradeMulti.tradeDate")),
                LocalDateTime.parse(p.getProperty("tradeMulti.settleDate")),

                Integer.parseInt(p.getProperty("tradeMulti.priceCurrencyId", "151")),
                p.getProperty("tradeMulti.priceCurrencyCode", "USD"),
                Integer.parseInt(p.getProperty("tradeMulti.settleCurrencyId", "151")),
                p.getProperty("tradeMulti.settleCurrencyCode", "USD"),

                p.getProperty("tradeMulti.accountCode", "DUMMY_TRADE_ACCOUNT"),
                Integer.parseInt(p.getProperty("tradeMulti.batchSize", "200"))
        );
    }

    private static Properties loadProps() {
        try (InputStream is = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application-test.properties")) {

            if (is == null) {
                throw new IllegalStateException(
                        "Cannot find application-test.properties in src/test/resources"
                );
            }

            Properties p = new Properties();
            p.load(is);
            return p;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load application-test.properties", e);
        }
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s.trim());
    }

    // 🔐 ENV VARIABLE RESOLVER (QUAN TRỌNG)
    private static String resolveEnv(String value) {
        if (value == null) return null;

        value = value.trim();

        // Supports ${ENV_NAME}
        if (value.startsWith("${") && value.endsWith("}")) {
            String envKey = value.substring(2, value.length() - 1);
            String envVal = System.getenv(envKey);

            if (envVal == null || envVal.isBlank()) {
                throw new RuntimeException("Missing environment variable: " + envKey);
            }
            return envVal;
        }

        return value;
    }
}
