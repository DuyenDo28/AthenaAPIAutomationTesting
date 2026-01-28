package com.athena.qa.tests.security;

import com.athena.qa.framework.client.SecurityApiClient;
import com.athena.qa.framework.client.SecurityDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityDbApiCompareTests extends BaseApiTest {

    private static final ObjectMapper FILE_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void compareSecurityDbVsApi() throws Exception {

        // ======================================================
        // 1) FETCH DB
        // ======================================================
        SecurityDbClient dbClient = new SecurityDbClient(dbConfig);
        List<Map<String, Object>> dbRows = dbClient.fetchSecurities();

        System.out.println("DB rows = " + dbRows.size());
        writeJson("out/security_db.json", dbRows);

        // ======================================================
        // 2) FETCH API (SYNC – ROOT ARRAY)
        // ======================================================
        SecurityApiClient apiClient =
                new SecurityApiClient(apiConfig, tokenProvider);

        Response apiRes = apiClient.getAllSecurities(-1);

        if (apiRes.statusCode() != 200) {
            throw new RuntimeException(
                    "Security API failed. status="
                            + apiRes.statusCode()
                            + " body=" + apiRes.asString()
            );
        }

        List<Map<String, Object>> apiRows =
                FILE_MAPPER.readValue(
                        apiRes.asString(),
                        new TypeReference<List<Map<String, Object>>>() {
                        }
                );

        System.out.println("API rows = " + apiRows.size());
        writeJson("out/security_api.json", apiRows);

        // ======================================================
        // 3) COMPARE
        // ======================================================
        SecurityCompareEngine engine = new SecurityCompareEngine();
        SecurityCompareEngine.CompareReport report =
                engine.compare(dbRows, apiRows);

        // ======================================================
        // 4) EXPORT JSON REPORT
        // ======================================================
        writeJson("out/security_compare_report.json", report);

        // ======================================================
        // 5) ASSERT
        // ======================================================
        assertTrue(
                report.isAllMatch(),
                "Security DB vs API mismatch. See out/security_compare_report.json"
        );
    }

    // ======================================================
    // Helper
    // ======================================================


}
