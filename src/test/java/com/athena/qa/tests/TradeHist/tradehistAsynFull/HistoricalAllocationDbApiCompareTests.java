package com.athena.qa.tests.TradeHist.tradehistAsynFull;

import com.athena.qa.framework.client.TradeAllocDbClient;
import com.athena.qa.tests.base.BaseApiTest;
import com.athena.qa.tests.trade.TradeCompareEngine;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoricalAllocationDbApiCompareTests extends BaseApiTest {

    @Test
    public void compareHistoricalAllocations() throws Exception {

        // =====================================================
        // DEBUG WORKDIR
        // =====================================================
        System.out.println("WORKDIR = " + new File(".").getAbsolutePath());

        // =====================================================
        // 1️⃣ START ASYNC (AsyncFull)
        // =====================================================
        Response startRes =
                tradeApiClient.startHistoricalAsync("2024-11-01", "2024-11-02");

        startRes.then().statusCode(202);

        String operationId = startRes.jsonPath().getString("operationId");

        System.out.println("⏳ Waiting 10 seconds for historical async to complete...");
        Thread.sleep(10_000);

        // =====================================================
        // 2️⃣ GET ASYNC RESULT
        // =====================================================
        Response res =
                tradeApiClient.getHistoricalAsyncResult(operationId);

        res.then().statusCode(200);

        @SuppressWarnings("unchecked")
        Map<String, Object> root =
                (Map<String, Object>) res.jsonPath().get("");

        String status = String.valueOf(root.get("status"));

        if (!"Successful".equalsIgnoreCase(status)) {
            writeJson("out/hist_alloc_async_not_ready.json", root);
            throw new RuntimeException(
                    "Historical async not ready, status=" + status
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result =
                (Map<String, Object>) root.get("result");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trades =
                (List<Map<String, Object>>) result.get("elements");

        // 🔥 RAW API
        writeJson("out/hist_alloc_api_raw.json", trades);

        // =====================================================
        // 3️⃣ DB: get historical allocations
        // =====================================================
        TradeAllocDbClient dbClient =
                new TradeAllocDbClient(dbConfig);

        List<Map<String, Object>> dbRows =
                dbClient.fetchHistoricalAllocations("2024-11-01", "2024-11-02");

        // 🔥 RAW DB
        writeJson("out/hist_alloc_db_raw.json", dbRows);

        // =====================================================
        // 🔍 DEBUG: API vs DB (ALLOCATION – SAME STYLE AS TRADE)
        // =====================================================
        System.out.println(
                "\n================ DEBUG HIST ALLOCATION API vs DB =================");

        for (Map<String, Object> trade : trades) {

            Object tradeClOrdId = trade.get("clOrdID");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocs =
                    (List<Map<String, Object>>) trade.get("allocations");

            if (allocs == null) continue;

            for (Map<String, Object> apiAlloc : allocs) {

                Object allocId = apiAlloc.get("allocID");
                if (allocId == null) continue;

                Map<String, Object> dbRow = null;
                for (Map<String, Object> r : dbRows) {
                    if (allocId.equals(r.get("AllocID"))) {
                        dbRow = r;
                        break;
                    }
                }

                if (dbRow == null) {
                    System.out.println("❌ DB NOT FOUND for AllocID=" + allocId);
                    continue;
                }

                System.out.println("\n--- ALLOCATION AllocID = " + allocId + " ---");

                for (Map.Entry<String, String> e
                        : HistoricalAllocationCompareEngine.API_TO_DB.entrySet()) {

                    String apiKey = e.getKey();
                    String dbKey = e.getValue();

                    Object apiVal;

                    if (apiKey.startsWith("comm")) {
                        Map<String, Object> comm =
                                (Map<String, Object>) apiAlloc.get("commissions");
                        apiVal = comm != null ? comm.get(apiKey) : null;

                    } else if (apiKey.startsWith("fee")) {
                        Map<String, Object> fee =
                                (Map<String, Object>) apiAlloc.get("fees");
                        apiVal = fee != null ? fee.get(apiKey) : null;

                    } else {
                        apiVal = null;
                        for (String k : apiAlloc.keySet()) {
                            if (k != null && k.equalsIgnoreCase(apiKey)) {
                                apiVal = apiAlloc.get(k);
                                break;
                            }
                        }
                    }

                    Object dbVal = dbRow.get(dbKey);

                    if (apiVal == null && dbVal == null) continue;

                    System.out.println(
                            "API." + apiKey + " = " + apiVal
                                    + " | DB." + dbKey + " = " + dbVal
                                    + (apiVal != null && dbVal != null
                                    ? " | TYPE API="
                                    + apiVal.getClass().getSimpleName()
                                    + " DB="
                                    + dbVal.getClass().getSimpleName()
                                    : "")
                    );
                }
            }
        }

        System.out.println("\n================ END DEBUG =================\n");

        // =====================================================
        // 6️⃣ COMPARE (HISTORICAL ENGINE)
        // =====================================================
        HistoricalAllocationCompareEngine engine =
                new HistoricalAllocationCompareEngine();

        TradeCompareEngine.CompareReport report =
                engine.compare(dbRows, trades);

        // 🔥 GHI FILE
        writeJson("out/hist_alloc_compare_report.json", report);

        // =====================================================
        // 7️⃣ ASSERT CUỐI
        // =====================================================
        assertTrue(report.isAllMatch(), report.toString());
    }
}
