package com.athena.qa.tests.trade;

import com.athena.qa.framework.client.TradeApiClient;
import com.athena.qa.framework.client.TradeDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradeDbApiCompareTests extends BaseApiTest {

    private static final ObjectMapper FILE_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void trade_get_db_compare_trade_only() throws Exception {

        // =========================
        // 1) GET API Trades
        // =========================
        TradeApiClient apiClient = new TradeApiClient(apiConfig, tokenProvider);

        Response apiRes = apiClient.getAllTrades();
        if (apiRes.statusCode() != 200) {
            writeJson("out/trade_api_error.json", Map.of(
                    "status", apiRes.statusCode(),
                    "body", apiRes.asString()
            ));
            throw new RuntimeException("GET /api/Trades failed, status=" + apiRes.statusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> apiTrades = mapper.readValue(
                apiRes.asString(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        System.out.println("API trades = " + apiTrades.size());
        writeJson("out/trade_api_raw.json", apiTrades);

        // =========================
        // 2) GET DB proc
        // =========================
        TradeDbClient dbClient = new TradeDbClient(dbConfig);
        List<Map<String, Object>> dbRows = dbClient.fetchIntradayOrders();

        System.out.println("DB rows = " + dbRows.size());
        writeJson("out/trade_db_raw.json", dbRows);

        // =========================
        // STEP: Trade-level compare ONLY
        // =========================
        TradeCompareEngine tradeEngine = new TradeCompareEngine();
        TradeCompareEngine.CompareReport tradeReport =
                tradeEngine.compare(dbRows, apiTrades);

        writeJson("out/trade_compare_report.json", tradeReport);

        // =========================
        // ASSERT (TRADE ONLY)
        // =========================
        assertTrue(tradeReport.isAllMatch(),
                "Trade-level mismatch. See out/trade_compare_report.json");
    }

    protected void writeJson(String relativePath, Object obj) {
        try {
            Path p = Path.of(relativePath);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            FILE_MAPPER.writeValue(new File(relativePath), obj);
            System.out.println(" Written: " + p.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON: " + relativePath, e);
        }
    }
}
