package com.athena.qa.tests.TradeHist.Asyn;

import com.athena.qa.framework.client.TradeHistApiClient;
import com.athena.qa.framework.client.TradeHistDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradeHistDbApiCompareTests extends BaseApiTest {
    /*
    public class TradeHistDbApiCompareTests extends BaseApiTest
JUnit thấy đây là test class (@Test)

Vì BaseApiTest có:
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
 JUnit tạo 1 instance duy nhất của TradeHistDbApiCompareTests

     */

    // dùng chung cho việc ghi file JSON (pretty print)
    private static final ObjectMapper FILE_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void compareTradeHistDbVsApi() throws Exception {

        // ======================================================
        // 0) DATE RANGE (from ApiConfig)
        // ======================================================
        LocalDate startDate = apiConfig.startDate();
        LocalDate endDate = apiConfig.endDate();

        if (startDate == null || endDate == null) {
            throw new IllegalStateException(
                    "startDate / endDate must be set in ApiConfig for TradeHist test"
            );
        }

        // ======================================================
        // 1) FETCH DB
        // ======================================================
        TradeHistDbClient dbClient = new TradeHistDbClient(dbConfig);
        List<Map<String, Object>> dbRows = dbClient.fetch(startDate, endDate);

        System.out.println("DB rows = " + dbRows.size());
        writeJson("out/tradehist_db.json", dbRows);

        // ======================================================
        // 2) FETCH API (ASYNC + POLL)
        // ======================================================
        TradeHistApiClient apiClient =
                new TradeHistApiClient(apiConfig, tokenProvider);

        String operationId = apiClient.startAsync(startDate, endDate);
        System.out.println("TradeHist operationId = " + operationId);
        Thread.sleep(2000);
        Response apiRes = apiClient.poll(operationId);

        if (apiRes.statusCode() != 200) {
            throw new RuntimeException(
                    "TradeHist API failed. status="
                            + apiRes.statusCode()
                            + " body=" + apiRes.asString()
            );
        }

        // ======================================================
        // 3) PARSE API RESPONSE (result.elements)
        // ======================================================
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(apiRes.asString());

        // optional nhưng rất nên có
        assertTrue(
                root.path("status").asText().equalsIgnoreCase("Successful"),
                "Async status is not Successful"
        );
        assertTrue(
                root.path("result").path("success").asBoolean(),
                "Async result.success = false"
        );

        JsonNode elementsNode = root
                .path("result")
                .path("elements");

        if (!elementsNode.isArray()) {
            throw new RuntimeException(
                    "Expected result.elements to be an array. Response="
                            + apiRes.asString()
            );
        }

        List<Map<String, Object>> apiRows =
                mapper.convertValue(
                        elementsNode,
                        new TypeReference<List<Map<String, Object>>>() {
                        }
                );

        System.out.println("API rows = " + apiRows.size());
        writeJson("out/tradehist_api.json", apiRows);

        // ======================================================
        // 4) COMPARE DB vs API
        // ======================================================
        TradeHistCompareEngine engine = new TradeHistCompareEngine();
        TradeHistCompareEngine.CompareReport report =
                engine.compare(dbRows, apiRows);

        // ======================================================
        // 5) EXPORT REPORTS (GIỐNG CODE CŨ)
        // ======================================================
        // text report


        // JSON report (cái bạn đang cần)
        writeJson("out/tradehist_compare_report.json", report);

        // ======================================================
        // 6) ASSERT
        // ======================================================
        assertTrue(
                report.isAllMatch(),
                "TradeHist DB vs API mismatch. See out/tradehist_compare_report.json"
        );
    }

    // ======================================================
    // Helpers
    // ======================================================


}
