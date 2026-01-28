package com.athena.qa.tests.positionhist;

import com.athena.qa.framework.client.HistoricalPositionApiClient;
import com.athena.qa.framework.client.PositionHistDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PositionHistDbApiCompareTests extends BaseApiTest {

    @Test
    void comparePositionHistDbVsApi() throws Exception {

        // ======================================================
        // 0) AS-OF DATE
        // ======================================================
        LocalDate asOfDate = apiConfig.startDate();
        if (asOfDate == null) {
            throw new IllegalStateException(
                    "apiConfig.startDate must be set for PositionHist test"
            );
        }

        // ======================================================
        // 1) FETCH DB
        // ======================================================
        PositionHistDbClient dbClient = new PositionHistDbClient(dbConfig);
        List<Map<String, Object>> dbRows =
                dbClient.fetchPositionHist(asOfDate);

        System.out.println("DB rows = " + dbRows.size());
        writeJson("out/positionhist_db.json", dbRows);

        // ======================================================
        // 2) API — START ASYNC
        // ======================================================
        HistoricalPositionApiClient apiClient =
                new HistoricalPositionApiClient(apiConfig, tokenProvider);

        String operationId = apiClient.startAsync(asOfDate);
        System.out.println("PositionHist operationId = " + operationId);

        // ======================================================
        // 3) API — POLL UNTIL RESULT READY
        // ======================================================
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        JsonNode resultNode = null;

        for (int i = 0; i < apiConfig.maxAttempts(); i++) {

            Response r = apiClient.poll(operationId);
            root = mapper.readTree(r.asString());

            String status = root.path("status").asText();
            resultNode = root.path("result");

            System.out.println(
                    "Poll attempt " + i
                            + " status=" + status
                            + " resultNull=" + resultNode.isNull()
            );

            // 🎯 DATA READY CONDITION
            if (!resultNode.isNull() && resultNode.has("elements")) {
                break;
            }

            Thread.sleep(apiConfig.sleepMs());
        }

        if (resultNode == null || resultNode.isNull()) {
            throw new RuntimeException(
                    "Async result is still null after polling. Last response="
                            + root
            );
        }

        JsonNode elementsNode = resultNode.path("elements");
        if (!elementsNode.isArray()) {
            throw new RuntimeException(
                    "Expected result.elements to be an array. Response="
                            + root
            );
        }

        List<Map<String, Object>> apiRows =
                mapper.convertValue(
                        elementsNode,
                        new TypeReference<List<Map<String, Object>>>() {
                        }
                );

        System.out.println("API rows = " + apiRows.size());
        writeJson("out/positionhist_api.json", apiRows);

        // ======================================================
        // 4) COMPARE
        // ======================================================
        PositionHistCompareEngine engine = new PositionHistCompareEngine();
        PositionHistCompareEngine.CompareReport report =
                engine.compare(dbRows, apiRows);

        writeJson("out/positionhist_compare_report.json", report);

        // ======================================================
        // 5) ASSERT
        // ======================================================
        assertTrue(
                report.isAllMatch(),
                "PositionHist DB vs API mismatch. See out/positionhist_compare_report.json"
        );
    }
}
