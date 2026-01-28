package com.athena.qa.tests.position;

import com.athena.qa.framework.client.PositionApiClient;
import com.athena.qa.framework.client.PositionDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PositionDbApiCompareTests extends BaseApiTest {

    @Test
    void comparePositionDbVsApi() throws Exception {

        // ======================================================
        // 1) DB
        // ======================================================
        PositionDbClient dbClient = new PositionDbClient(dbConfig);
        List<Map<String, Object>> dbRows = dbClient.fetchPositions();
        System.out.println("DB rows = " + dbRows.size());
        writeJson("out/position_db.json", dbRows);

        // ======================================================
        // 2) API (SYNC)
        // ======================================================
        PositionApiClient apiClient = new PositionApiClient(apiConfig, tokenProvider);
        Response apiRes = apiClient.getPositions();

        if (apiRes.statusCode() != 200) {
            throw new RuntimeException(
                    "Position API failed. status=" + apiRes.statusCode()
                            + " body=" + apiRes.asString()
            );
        }

        // Response thường là JSON array, nhưng để chắc chắn:
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(apiRes.asString());
        if (!root.isArray()) {
            throw new RuntimeException("Expected /api/Positions to return an ARRAY. Response=" + apiRes.asString());
        }

        List<Map<String, Object>> apiRows = mapper.convertValue(
                root,
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        System.out.println("API rows = " + apiRows.size());
        writeJson("out/position_api.json", apiRows);

        // ======================================================
        // 3) COMPARE + DEBUG
        // ======================================================
        PositionCompareEngine engine = new PositionCompareEngine();
        boolean debug = true; // bật debug
        PositionCompareEngine.CompareReport report =
                engine.compare(dbRows, apiRows, debug);

        writeJson("out/position_compare_report.json", report);

        // ======================================================
        // 4) ASSERT
        // ======================================================
        assertTrue(
                report.isAllMatch(),
                "Position DB vs API mismatch. See out/position_compare_report.json"
        );
    }
}
